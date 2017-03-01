/*
 * #%L
 * WSB Webapp
 * %%
 * Copyright (C) 2013 - 2014 OSI / Computer Architecture Group @ Uni. Heidelberg
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.idyria.osi.wsb.webapp.http.message

import com.idyria.osi.wsb.webapp.mime._
import com.idyria.osi.wsb.core.message._
import com.idyria.osi.wsb.webapp.http.session._
import java.nio._
import com.idyria.osi.wsb.webapp.mime.MimePart
import com.idyria.osi.tea.logging.TLogSource
import com.idyria.osi.wsb.core.network.connectors.tcp.TCPNetworkContext
import com.idyria.osi.wsb.core.network.NetworkContext
import java.net.URL
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
import java.io.ByteArrayOutputStream
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement

trait HTTPMessage extends Message {

  /**
   * The current session if available
   */
  var session: Option[Session] = None

  def getSession  = session
  
  def hasSession = session.isDefined

  // Cookies
  //----------
  var cookies = Map[String, String]()

}

object HTTPMessage extends MessageFactory {

  

  def apply(data: Any): HTTPMessage = {

    var part = data.asInstanceOf[MimePart]

    //println(s"Got http message for factory: " + part.protocolLines)

    //-- Request or Response?
    part.protocolLines(0) match {
      case response if (response.startsWith("HTTP")) => HTTPResponse(data)
      case request => HTTPRequest(data)
    }

  }
}

/**
 *
 * Qualifier: s"http:$path:$operation"
 * Example: http:/index:GET
 *
 * @author rleys
 *
 */
class HTTPRequest(

    var operation: String,
    var path: String,
    var version: String) extends MimePart with HTTPMessage with TLogSource {

  // Path and URL parameters separation
  //---------------------
  path = path.replaceAll("//+", "/")

  var originalPath = this.path.split("""\?""").head
  def originalURL = "http://" + (this.getParameter("Host").get + "/" + originalPath).replace("//", "/")

  //-- Path may contain some URL encoded parameters, decode them
  //-----------

  var urlParameters = scala.collection.mutable.Map[String, String]()
  path.split("""\?""").lastOption match {

    //-- Query part
    case Some(queryPart) => queryPart.split("&").foreach("""([\w_-]+)=(.+)""".r.findFirstMatchIn(_) match {

      case Some(parameterMatch) =>

        logFine(s"[HTTP] URL Parameter: ${parameterMatch.group(1)} ${parameterMatch.group(2)}")

        urlParameters.update(parameterMatch.group(1), java.net.URLDecoder.decode(parameterMatch.group(2),"US-ASCII"))
      //this.addParameter()
      case None =>

    })

    case None =>
  }

  //-- Ensure path has no URL parameters
  this.path = this.path.split("""\?""").head

  // Use Path as qualifier
  this.qualifier = s"http:$path:$operation"

  def getCurrentURL = "http://" + this.getParameter("Host").get + "/" + this.path

  /**
   * Update the path
   * Ensures the path always has a starting "/"
   */
  def changePath(newPath: String) = {
    newPath.startsWith("/") match {
      case true => this.path = newPath
      case false => this.path = "/" + newPath
    }

    this.qualifier = s"http:${this.path}:$operation"
  }
  
  /**
   * Changes the message path by removing a prefix
   */
  def stripPathPrefix(prefix:String) = {
    changePath( path.stripPrefix(prefix))
  }

  def toBytes = {

    //-- Set Content Length if necessary
    if (nextParts.size > 0) {
      addParameter("Content-Length", nextParts.map(_.bytes.length).sum[Int].toString)
    }

    //-- Result
    ByteBuffer.wrap(s"""$operation $path HTTP/$version\r\n${parameters.map(p => s"${p._1}: ${p._2}").mkString("\r\n")}\r\n\r\n${nextParts.map(p => new String(p.bytes)).mkString("", "\r\n", "\r\n\r\n")}""".getBytes("US-ASCII"))
  }

  // Cookies
  //---------------

  /**
   * Catch Cookies
   */
  override def addParameter(name: String, value: String) = {

    if (name == "Cookie") {

      value.trim.split(";").foreach("""([\w]+)=(.+)""".r.findFirstMatchIn(_) match {
        case Some(matched) =>

          var (cookieName, cookieValue) = (matched.group(1) -> matched.group(2))

          logFine(s"Got cookie $cookieName -> $cookieValue")

          cookies = cookies + (cookieName -> cookieValue)

        case None =>

          logFine(s"Cookie but value regexp did not match")
      })

    } else {
      super.addParameter(name, value)
    }
  }

  // Session
  //-------------------
  override def getSession = {
    this.session = Some(Session(this))
    this.session
  }
  
  override  def hasSession = Session.sessionDefined(this)

  // URL Parameters
  //-------------------------

  /**
   * If the content type matches application/x-www-form-urlencoded
   * Then try to find in bytes string the whished parameter
   */
  def getURLParameter(name: String): Option[String] = {

    // First try to find in URL Map
    //--------------
    this.urlParameters.get(name) match {
      case Some(value) => Some(value)

      // Look For POST in Content
      //---------------
      case None =>

        this.parameters.find(_._1 == "Content-Type") match {

          // Some URL parameters can be in content -> in bytes
          //-----------------
          case Some((_, contentType)) if (contentType.trim.startsWith("application/x-www-form-urlencoded")) =>

            var content = new String(this.bytes)
            ("""\b""" + name + """\b""" + """=([\w%+_\.-]+)(?:&|$$)""").r.findFirstMatchIn(content) match {

              // Foudn Something, save and return
              case Some(matched) =>
                var decoded = java.net.URLDecoder.decode(matched.group(1), "UTF-8")
                this.urlParameters.update(name, decoded)
                Option(decoded)
              case None =>

                // Look in normal URL parameters
                this.parameters.collectFirst { case param if (param._1 == name) => java.net.URLDecoder.decode(param._2, "UTF-8") }
            }

          // Multi part form data -> explore other parts
          //-----------------
          case Some((_, contentType)) if (contentType.trim.startsWith("multipart/form-data")) =>

            //  println(s"------- Searching part with parameter name")
            this.nextParts.foreach {
              p =>
              /* println(s"----> Part plline: " + p.protocolLines.mkString("\n"))
            println(s"----> Part params: " + p.parameters.mkString("\n"))
            println(s"----> Part content: " + new String(p.bytes))*/

            }
            this.nextParts.collectFirst {
              case p if (p.getParameter("Content-Disposition") != None && p.getParameter("Content-Disposition").get.trim.matches("form-data;\\s*name=\"" + name + "\".*")) =>

                // Found something, save it 
                var decoded = java.net.URLDecoder.decode(new String(p.bytes), "UTF-8")
                this.urlParameters.update(name, decoded)
                decoded

            }

          // Normal parameters
          case _ => this.parameters.collectFirst { case param if (param._1 == name) => java.net.URLDecoder.decode(param._2, "UTF-8") }

        }

    }

  }

  // Multipart Data Support
  //--------------
  def isMultipart: Boolean = {

    this.parameters.find(_._1 == "Content-Type") match {

      case Some(Tuple2(_, contentType)) if (contentType.trim.matches("multipart/form-data.*")) => true
      case _ => false
    }

  }

  def getMultiPartBoundary: Option[String] = {

    this.parameters.find(_._1 == "Content-Type") match {

      case Some(Tuple2(_, contentType)) if (contentType.matches("multipart/form-data.*")) =>

        """.+; boundary=(.+)\s*""".r.findFirstMatchIn(contentType) match {
          case Some(matched) => Option(matched.group(1))
          case None => None
        }

      case _ => None
    }

  }
  
  
  // General Utilities
  //------------------
  def isLocalHost = {
    this.getParameter("Host") match {
      case Some(host) => host.contains("localhost") ||  host.contains("127.0.0.1") 
      case None => false
    }
  }
  
  
}

object HTTPRequest extends MessageFactory with TLogSource {

  /**
   * Create a request for an URL
   */
  def GET(urlStr: String): HTTPRequest = {

    var request = prepareRequest(urlStr)

    //-- Set to GET
    request.operation = "GET"

    request

  }

  def POST(urlStr: String): HTTPRequest = {

    //-- Prepare
    var request = prepareRequest(urlStr)

    //-- Set to post
    request.operation = "POST"

    request
  }

  /**
   * Prepare a request based on the URL, with GET action
   */
  def prepareRequest(urlStr: String): HTTPRequest = {

    //-- Split URL
    var url = new URL(urlStr)

    //-- Create message
    //------------------------
    var request = new HTTPRequest("GET", url.getPath(), "1.1")

    //-- Add Host to parameters
    request.addParameter("Host", url.getHost())

    //-- A few Parameters for webbrowsers
    request.addParameter("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:31.0) Gecko/20100101 Firefox/31.0")
    request.addParameter("Accept", """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""")
    request.addParameter("Accept-Encoding", "gzip")
    request.addParameter("Accept-Language", "en-US,en;q=0.5")
    request.addParameter("Connection", "keep-alive")

    //-- If some Query parameters, set the content type, length and add part
    url.getQuery() match {
      case null =>
      case "" =>
      case query =>

        //-- Set type
        request.addParameter("Content-Type", "application/x-www-form-urlencoded")

        //-- Create part for this
        var part = new DefaultMimePart
        part += query.getBytes()

        request.append(part)

    }

    //-- Destination URL is a network context parameter
    //-----------------------
    var ctx =Some( new TCPNetworkContext("tcp+http+http://" + url.getHost()))
    request.networkContext = ctx

    request

  }

  def apply(data: Any): HTTPRequest = {

    build(data.asInstanceOf[MimePart])

  }

  var lastFirstMessage: HTTPRequest = null

  /**
   * Create HTTPMessage
   * - 1st line: GET/PUT...  /path/ HTTPVERSION
   */
  def build(part: MimePart): HTTPRequest = {

    // Prepare regexps
    //----------------------
    var firstLineRegexp = """(GET|POST|PUT) (.*) HTTP/([0-9]+\.[0-9]+)""".r
    var parameterLineRegexp = """([\w-])+: (.+)""".r

    // Parse First Line
    //-----------------------
    firstLineRegexp.findFirstMatchIn(part.protocolLines(0)) match {

      //-- Got First Message
      case Some(matched) =>

        logFine(s"[HTTP] -> First Message from part ${part.hashCode} with protocol line: " + part.protocolLines(0))
        lastFirstMessage = new HTTPRequest(matched.group(1), matched.group(2), matched.group(3))

        logFine("Got HTTP Message for path: " + lastFirstMessage.path + " and operation " + lastFirstMessage.operation)

        lastFirstMessage.operation match {
          case "POST" =>
          //println(s"Post message content: ${new String(part.bytes)}");

          case _ =>
        }

        // Add part ot message
        //-------------
        lastFirstMessage(part)

        // Handle Multipart 
        //--------------------
        //println(s"MP: "+lastFirstMessage.isMultipart+"//"+lastFirstMessage.getParameter("Content-Type"))
        if (lastFirstMessage.isMultipart) {
          var boundary = lastFirstMessage.getMultiPartBoundary.get

          //println(s"Split to $boundary")

          // Split
          new String(lastFirstMessage.bytes).split("--" + boundary).dropRight(1).drop(1).filter { p => p.trim != "" }.map { _.trim }.foreach {
            lines =>

              //println(s"**--> part: $lines")

              var part = new DefaultMimePart
              lastFirstMessage.append(part)

              /*  lines.split("\r\n").foreach {
                l => println(s"**-----> line $l")
              }*/

              // Add all first lines as parameter, if we find a new line, the remaining goes as content
              var contents = lines.split("\r\n\r\n")
              contents(0).split("\r\n").foreach {
                pl => part.addParameter(pl)

              }

              // Add content if any 
              if (contents.size > 1) {
                part += contents(1).getBytes
              }
          }
        }

      //-- Maybe a Continued Content in case of a multipart message
      case None if (lastFirstMessage != null && lastFirstMessage.isMultipart) =>

        logFine(s"[HTTP] -> Multipart element, create a request with the same path as previous message")
        var message = new HTTPRequest(lastFirstMessage.operation, lastFirstMessage.path, lastFirstMessage.version)
        message.cookies = lastFirstMessage.cookies
        message += part.bytes

        // Add Parameters
        //-------------
        message(part)

        return message

      //-- No Idea
      case _ =>
        logWarn(s"[HTTP] -> Not a first message and not a multipart part")

    }

    return lastFirstMessage

  }

}

class HTTPResponse extends HTTPMessage with MimePart with TLogSource {

  var contentType: String = null
  var content: ByteBuffer = null

  var code = 200

  /**
   * Catch Cookies
   */
  override def addParameter(name: String, value: String) = {

    if (name == "Set-Cookie") {

      value.trim.split(";").foreach("""([\w]+)=(.+)""".r.findFirstMatchIn(_) match {
        case Some(matched) =>

          var (cookieName, cookieValue) = (matched.group(1) -> matched.group(2))

          logFine(s"Got cookie $cookieName -> $cookieValue")

          cookies = cookies + (cookieName -> cookieValue)

        case None =>

          logFine(s"Cookie but value regexp did not match")
      })

    } else {
      super.addParameter(name, value)
    }
  }

  def toBytes: ByteBuffer = {

    var headerLines = List[String]()
    headerLines = headerLines :+ s"HTTP/1.1 ${HTTPCodes.codeToStatus(code)}"

    // Add Standard Parameters
    //-----------------------------------

    //headerLines = headerLines :+ s"Status: ${HTTPErrorCodes.codeToStatus(code)}"

    contentType match {
      case null =>
      case ct => headerLines = headerLines :+ s"Content-Type: $ct"
    }

    //headerLines = headerLines :+ "Cache-Control: no-cache"

    content match {
      case null =>
      case c => headerLines = headerLines :+ s"Content-Length: ${c.capacity}"
    }

    var sessionId = ""
    if (this.getSession.isDefined) {

      //sessionId = s"""Set-Cookie: SSID=${this.getSession.id}; Domain=${this.getSession.host}; Path=${this.getSession.path}; Expires=${this.getSession.validityString};"""
      sessionId = s"""Set-Cookie: SSID=${this.getSession.get.id}; Path=${this.getSession.get.path}; Expires=${this.getSession.get.validityString};"""

      //sessionId = s"""Set-Cookie: SSID=${this.getSession.id}; Expires=${this.getSession.validityString};"""
      //sessionId = s"""Set-Cookie: SSID=${this.getSession.id};"""
      headerLines = headerLines :+ sessionId
    }

    // Add Mime Defined Parameters
    //-------------------------------
    this.parameters.map(v => s"${v._1}: ${v._2}").foreach {
      v => headerLines = headerLines :+ v

    }

    /*var header = s"""HTTP/1.1 $code
Status: 200 OK
Content-Type: $contentType
Cache-Control: no-cache
Content-Length: ${content.capacity}
$sessionId
"""*/
    var header = content match {
      case null => headerLines.mkString("", "\r\n", "\r\n\r\n")
      case _ => headerLines.mkString("", "\r\n", "\r\n\r\n")
    }

    logFine(s"Response Headers: $header //")

    // Create Bytes
    //-------------------
    var totalSize = content match {
      case null => header.getBytes.size
      case _ => header.getBytes.size + content.capacity
    }

    var res = ByteBuffer.allocateDirect(totalSize)
    res.put(header.getBytes)

    content match {
      case null =>
      case c =>
        res.put(c)
        c.clear()
        content = null
    }

    //res.put(ByteBuffer.wrap("\n".getBytes))

    /*if (contentType=="text/html") {
      println(s"Sending: "+new String(res.array))
    }*/
    //println(s"Sending: "+new String(res.array))))))))

    res.flip
    res

  }

  // Content
  //--------------
  var __htmlContent : Option[HTMLNode[HTMLElement, _]] = None
  
  def htmlContent_=(h: HTMLNode[HTMLElement, _]): Unit = {
    __htmlContent = Some(h)
    this.contentType = "text/html"
    this.content = ByteBuffer.wrap(h.toString().getBytes)
    htmlContent
  }

  // Dummy
  def htmlContent = __htmlContent
  
  
  def setTextContent(str:String) = {
    this.contentType = "text/plain"
    this.content= ByteBuffer.wrap(str.getBytes)
  }
  

}
object HTTPResponse extends MessageFactory with TLogSource {

  def apply(): HTTPResponse = new HTTPResponse
  def apply(data: Any): HTTPResponse = {

    var part = data.asInstanceOf[MimePart]

    //-- Parse First line for result info
    var firstLineRegexp = """(HTTP/1.1) ([0-9]+) (.+)""".r

    var lastFirstMessage: HTTPResponse = null

    firstLineRegexp.findFirstMatchIn(part.protocolLines(0)) match {

      //-- Got First Message
      case Some(matched) =>

        logFine[HTTPResponse](s"[HTTP] -> First Message from part ${part.hashCode} with protocol line: " + part.protocolLines(0))
        lastFirstMessage = new HTTPResponse
        lastFirstMessage.code = matched.group(2).toInt

        //logFine("Got HTTP Message for path: " + lastFirstMessage.path + " and operation " + lastFirstMessage.operation)

        // Add part ot message
        //-------------
        lastFirstMessage(part)

        //-- Decode Data if needed
        logFine[HTTPResponse](s"Bytes available: " + lastFirstMessage.bytes.length)

        lastFirstMessage.parameters.find(_._1 == "Content-Encoding") match {
          case Some((_, "gzip")) =>

            logFine[HTTPResponse]("Bytes are encoded using GZIP, unzip them")

            //-- Create ZIP input stream from bytes
            var zipInput = new GZIPInputStream(new ByteArrayInputStream(lastFirstMessage.bytes))

            logFine[HTTPResponse]("Available: " + zipInput.available())

            try {
              // zipInput.getNextEntry()
              //-- Look for the next entry
              zipInput.available() match {

                //-- Error If nothing to read
                case 0 => throw new RuntimeException("No bytes available in ZIP stream (EOF)")

                //-- Otherwise read in a new array stream
                case 1 =>

                  // Init with twice the size to avoid too much internal array resizing
                  /* var outputStream = new ByteArrayOutputStream(lastFirstMessage.bytes.length*2)
                  
                  // Read loop (read in page size buffers)
                  while(zipInput.available()==1) {
                    zipInput.
                  }
                  
                  lastFirstMessage.bytes = new Array[Byte](size)
                  zipInput.read(lastFirstMessage.bytes)*/

                  // rely on scala for now
                  lastFirstMessage += scala.io.Source.fromInputStream(zipInput, "UTF-8").mkString.getBytes()
              }
            } finally {
              zipInput.close()
            }

          case _ =>
        }
      //lastFirstMessage.bytes

      //-- No Idea
      case _ =>
        logWarn(s"[HTTP] -> Not a first message and not a multipart part")

    }

    return lastFirstMessage

  }

  def apply(contentType: String, content: ByteBuffer): HTTPResponse = {

    var r = new HTTPResponse
    r.contentType = contentType
    r.content = content
    r
  }

  def apply(contentType: String, content: String): HTTPResponse = {

    var r = new HTTPResponse
    r.contentType = contentType
    r.content = ByteBuffer.wrap(content.getBytes)
    r

  }
  
  //-- Standard codes
  def c503 = {
    var resp = new HTTPResponse
    resp.code = 503
    resp
  }
  def c404 = {
    var resp = new HTTPResponse
    resp.code = 404
    resp
  }

}


