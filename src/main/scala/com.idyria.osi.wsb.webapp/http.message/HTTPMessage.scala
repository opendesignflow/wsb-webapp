/**
 *
 */
package com.idyria.osi.wsb.webapp.http.message

import com.idyria.osi.wsb.webapp.mime._
import com.idyria.osi.wsb.core.message._
import com.idyria.osi.wsb.webapp.http.session._
import java.nio._
import com.idyria.osi.wsb.webapp.mime.MimePart
import com.idyria.osi.tea.logging.TLogSource

trait HTTPMessage {

  /**
   * The current session if available
   */
  var session: Session = null

  def getSession: Session = session

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
    var version: String) extends Message with MimePart with HTTPMessage with TLogSource {

  // Path and URL parameters separation
  //---------------------

  //-- Path may contain some URL encoded parameters, decode them
  path.split("""\?""").lastOption match {

    //-- Query part
    case Some(queryPart) ⇒ queryPart.split("&").foreach("""([\w_-]+)=(.+)""".r.findFirstMatchIn(_) match {

      case Some(parameterMatch) ⇒

        logFine(s"[HTTP] URL Parameter: ${parameterMatch.group(1)} ${parameterMatch.group(2)}")

        this.addParameter(parameterMatch.group(1), parameterMatch.group(2))
      case None ⇒

    })

    case None ⇒
  }

  //-- Ensure path has no URL parameters
  this.path = this.path.split("""\?""").head

  // Use Path as qualifier
  this.qualifier = s"http:$path:$operation"

  def changePath(newPath: String) = {
    this.path = newPath
    this.qualifier = s"http:$newPath:$operation"
  }

  def toBytes = ByteBuffer.wrap(s"$operation $path HTTP/$version".getBytes)

  // Cookies
  //---------------
  var cookies = Map[String, String]()
  /**
   * Catch Cookies
   */
  override def addParameter(name: String, value: String) = {

    if (name == "Cookie") {

      value.trim.split(";").foreach("""([\w]+)=(.+)""".r.findFirstMatchIn(_) match {
        case Some(matched) ⇒

          var (cookieName, cookieValue) = (matched.group(1) -> matched.group(2))

          logFine(s"Got cookie $cookieName -> $cookieValue")

          cookies = cookies + (cookieName -> cookieValue)

        case None ⇒

          logFine(s"Cookie but value regexp did not match")
      })

    } else {
      super.addParameter(name, value)
    }
  }

  // Session
  //-------------------
  override def getSession: Session = {
    this.session = Session(this)
    this.session
  }

  // URL Parameters
  //-------------------------

  /**
   * If the content type matches application/x-www-form-urlencoded
   * Then try to find in bytes string the whished parameter
   */
  def getURLParameter(name: String): Option[String] = {

    // Handle POST form parameters that can be in another MIME part
    // - Consume the MIME part containing the Parameters
    //----------------------------
    this.parameters.get("Content-Type") match {
      case Some(contentType) if (contentType.startsWith("application/x-www-form-urlencoded") && this.nextParts.size > 0) ⇒

        var nextPart = this.nextParts.head
        this.nextParts = this.nextParts.drop(1)

        //-- The Content will only be of One Line, meaning it is in the protocol Line 
        //-- Search for all key=value elements
        ("""([\w%+_\.-]+)=([\w%+_\.-]+)(?:&|$)""").r.findAllMatchIn(nextPart.protocolLines(0)).foreach {
          m ⇒
            this.addParameter(java.net.URLDecoder.decode(m.group(1), "UTF-8"), m.group(2))
        }

      case _ ⇒
    }
    //application/x-www-form-urlencoded

    logFine(s"""[Content Type]${this.parameters.get("Content-Type")}""")

    /*this.parameters.foreach {
      case (pname, value) ⇒ println(s"[URLParameter] Available: ${pname}")
    }*/

    // Try in Normal Part Parameters
    //---------------
    this.parameters.get(name) match {
      case Some(value) ⇒ Option(java.net.URLDecoder.decode(value, "UTF-8"))
      case None        ⇒ None
    }
    /* this.parameters.get(name) match {
      case Some(value) ⇒ Option(java.net.URLDecoder.decode(value, "UTF-8"))

      // Ttry Alternatives
      //-------------------------
      case None ⇒
        // Request has a content of form url encoded
        this.parameters.get("Content-Type") match {
          case Some(contentType) if (contentType.startsWith("application/x-www-form-urlencoded")) ⇒

            logFine(s"[URLParameter] Looking into next part: ${this.nextParts.size}")

            // The Next part should be the content and parameter could be in protocol line
            this.nextParts.headOption match {
              case Some(part) ⇒

                logFine(s"[URLParameter] Protocol Line: : ${part.protocolLines}")

                (name + """=([\w%+_\.-]+)(?:&|$)""").r.findFirstMatchIn(part.protocolLines(0)) match {

                  // Found value for URL parameter, decode it:
                  case Some(matched) ⇒

                    logFine(s"Value for $name : /${java.net.URLDecoder.decode(matched.group(1), "UTF-8")}/")
                    Option(java.net.URLDecoder.decode(matched.group(1), "UTF-8"))

                  case _ ⇒ None
                }
              case None ⇒ None
            }

          case _ ⇒ None
        }

    }*/

  }

  // Multipart Data Support
  //--------------
  def isMultipart: Boolean = {

    this.parameters.get("Content-Type") match {

      case Some(contentType) if (contentType.matches("multipart/form-data.*")) ⇒ true
      case _ ⇒ false
    }

  }
}

object HTTPRequest extends MessageFactory with TLogSource {

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
      case Some(matched) ⇒

        logFine(s"[HTTP] -> First Message from part ${part.hashCode} with protocol line: " + part.protocolLines(0))
        lastFirstMessage = new HTTPRequest(matched.group(1), matched.group(2), matched.group(3))

        logFine("Got HTTP Message for path: " + lastFirstMessage.path + " and operation " + lastFirstMessage.operation)

        lastFirstMessage.operation match {
          case "POST" ⇒
            println(s"Post message content: ${new String(lastFirstMessage.bytes)}");

          case _ ⇒
        }

        // Add part ot message
        //-------------
        lastFirstMessage(part)

      //-- Maybe a Continued Content in case of a multipart message
      case None if (lastFirstMessage != null && lastFirstMessage.isMultipart) ⇒

        logFine(s"[HTTP] -> Multipart element, create a request with the same path as previous message")
        var message = new HTTPRequest(lastFirstMessage.operation, lastFirstMessage.path, lastFirstMessage.version)
        message.cookies = lastFirstMessage.cookies
        message.bytes = part.bytes

        // Add Parameters
        //-------------
        message(part)

        return message

      //-- No Idea
      case _ ⇒
        logWarn(s"[HTTP] -> Not a first message and not a multipart part")

    }

    return lastFirstMessage

  }

}

class HTTPResponse extends Message with HTTPMessage with MimePart with TLogSource {

  var contentType: String = null
  var content: ByteBuffer = null

  var code = 200

  def toBytes: ByteBuffer = {

    var headerLines = List[String]()
    headerLines = headerLines :+ s"HTTP/1.1 ${HTTPErrorCodes.codeToStatus(code)}"

    // Add Standard Parameters
    //-----------------------------------

    //headerLines = headerLines :+ s"Status: ${HTTPErrorCodes.codeToStatus(code)}"

    contentType match {
      case null =>
      case ct   => headerLines = headerLines :+ s"Content-Type: $ct"
    }

    headerLines = headerLines :+ "Cache-Control: no-cache"

    content match {
      case null =>
      case c    => headerLines = headerLines :+ s"Content-Length: ${c.capacity}"
    }

    var sessionId = ""
    if (this.getSession != null) {

      //sessionId = s"""Set-Cookie: SSID=${this.getSession.id}; Domain=${this.getSession.host}; Path=${this.getSession.path}; Expires=${this.getSession.validityString};"""
      sessionId = s"""Set-Cookie: SSID=${this.getSession.id}; Path=${this.getSession.path}; Expires=${this.getSession.validityString};"""

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
      case _    => headerLines.mkString("", "\r\n", "\r\n\r\n")
    }

    logFine(s"Response Headers: $header //")

    // Create Bytes
    //-------------------
    var totalSize = content match {
      case null => header.getBytes.size
      case _    => header.getBytes.size + content.capacity
    }

    var res = ByteBuffer.allocate(totalSize)
    res.put(header.getBytes)

    content match {
      case null =>
      case c    => res.put(c)
    }

    //res.put(ByteBuffer.wrap("\n".getBytes))

    /*if (contentType=="text/html") {
      println(s"Sending: "+new String(res.array))
    }*/
    //println(s"Sending: "+new String(res.array))

    res.flip
    res

  }

}
object HTTPResponse {

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

}
