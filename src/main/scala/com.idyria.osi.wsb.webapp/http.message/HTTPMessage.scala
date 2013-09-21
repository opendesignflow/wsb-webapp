/**
 *
 */
package com.idyria.osi.wsb.webapp.http.message

import com.idyria.osi.wsb.webapp.mime._
import com.idyria.osi.wsb.core.Logsource
import com.idyria.osi.wsb.core.message._

import com.idyria.osi.wsb.webapp.http.session._

import java.nio._


trait HTTPMessage {

  /**
    The current session if available
  */
  var session : Session = null

  def getSession : Session = session

}
/**

  Qualifier: s"http:$path:$operation"
  Example: http:/index:GET

 * @author rleys
 *
 */
class HTTPRequest (
    
      var operation : String,
      var path : String,
      var version : String
    
		)  extends Message with MimePart with HTTPMessage {

  
  // Standard Message Implementation
  //---------------------

	// Use Path as qualifier
  this.qualifier = s"http:$path:$operation"

  def toBytes = ByteBuffer.wrap(s"$operation $path HTTP/$version".getBytes)

  // Cookies
  //---------------
  var cookies = Map[String,String]()
  /**
    Catch Cookies
  */
  override def addParameter(name:String,value:String) = {

    if (name=="Cookie") {

      """([\w]+)=(.+);?""".r.findAllMatchIn(value.trim).toList.lastOption match {
        case Some(matched) => 

          var (cookieName,cookieValue) = (matched.group(1) -> matched.group(2))

          println(s"Got cookie $cookieName -> $cookieValue")

          cookies = cookies + (cookieName -> cookieValue)

        case None =>

          println(s"Cookie but value regexp did not match")
      }
     

    } else {
      super.addParameter(name,value)
    }
  }

  // Session
  //-------------------
  override def getSession : Session = {
    this.session = Session(this)
    this.session
  }

  // URL Parameters
  //-------------------------

  /**
    If the content type matches application/x-www-form-urlencoded
    Then try to find in bytes string the whished parameter
  */
  def getURLParameter(name:String) : Option[String] = {

    this.parameters.get("Content-Type") match {
      case Some(contentType) if(contentType.matches("""\s*application/x-www-form-urlencoded.+""")) => 

          (name+"""=([\w%+_-]+)(?:&|$)""").r.findFirstMatchIn(new String(bytes)) match {

            // Found value for URL parameter, decode it:
            case Some(matched) => 
                Option(java.net.URLDecoder.decode(matched.group(1),"UTF-8"))
            case _ => None
          }


      case _ => None
    }
    

  } 


  // Multipart Data Support
  //--------------
  def isMultipart : Boolean = {

    this.parameters.get("Content-Type") match {

      case Some(contentType) if (contentType.matches("multipart/form-data.*")) => true
      case _ => false
    }

  }
}


object HTTPRequest extends MessageFactory with Logsource {
  
  def apply(data: Any) : HTTPRequest = {

      build(data.asInstanceOf[MimePart])

  }

  var lastFirstMessage : HTTPRequest = null

  /**
   * Create HTTPMessage
   * - 1st line: GET/PUT...  /path/ HTTPVERSION
   */
  def build(part : MimePart): HTTPRequest = {
    
    // Prepare regexps
    //----------------------
    var firstLineRegexp  = """(GET|POST|PUT) (.*) HTTP/([0-9]+\.[0-9]+)""".r
    var parameterLineRegexp = """([\w-])+: (.+)""".r
    
    

    // Parse First Line
    //-----------------------
    firstLineRegexp.findFirstMatchIn(part.protocolLines(0)) match {

      //-- Got First Message
      case Some(matched) =>

        println(s"[HTTP] -> First Message from part ${part.hashCode} with protocol line: "+part.protocolLines(0))
        lastFirstMessage = new HTTPRequest(matched.group(1),matched.group(2),matched.group(3))

        println("Got HTTP Message for path: "+lastFirstMessage.path+" and operation "+lastFirstMessage.operation)

        // Add Parameters
        //-------------
        lastFirstMessage(part)
      

      //-- Maybe a Continued Content in case of a multipart message
      case None if (lastFirstMessage!=null && lastFirstMessage.isMultipart)=> 

        println(s"[HTTP] -> Multipart element, create a request with the same path as previous message")
        var message = new HTTPRequest(lastFirstMessage.operation,lastFirstMessage.path,lastFirstMessage.version)
        message.cookies = lastFirstMessage.cookies
        message.bytes = part.bytes

        // Add Parameters
        //-------------
        message(part)

        return message

      //-- No Idea
      case _ => 
        println(s"[HTTP] -> Not a first message and not a multipart part")

    }

  
    return lastFirstMessage
    
  }
  
}

class HTTPResponse (
    
      var contentType : String,
      var content : ByteBuffer
    
    )  extends Message  with HTTPMessage {

  var code = 200


  def toBytes : ByteBuffer = {

    var headerLines = List[String]()
    headerLines = headerLines :+ s"HTTP/1.1 $code"
    headerLines = headerLines :+ "Status: 200 OK"
    headerLines = headerLines :+ s"Content-Type: $contentType"
    headerLines = headerLines :+ "Cache-Control: no-cache"
    headerLines = headerLines :+ s"Content-Length: ${content.capacity}"

    var sessionId =""
    if (this.getSession!=null) {

      sessionId=s"""Set-Cookie: SSID=${this.getSession.id}; Domain=${this.getSession.host}; Path=/; Expires=${this.getSession.validityString};"""
      headerLines = headerLines :+ sessionId
    }


    /*var header = s"""HTTP/1.1 $code
Status: 200 OK
Content-Type: $contentType
Cache-Control: no-cache
Content-Length: ${content.capacity}
$sessionId
"""*/
    var header = headerLines.mkString("","\n","\n\n")

    println(s"Response Headers: $header //")

    var res = ByteBuffer.allocate(header.getBytes.size+content.capacity)
    res.put(header.getBytes)
    res.put(content)
    //res.put(ByteBuffer.wrap("\n".getBytes))
  
    if (contentType=="text/html") {
      println(s"Sending: "+new String(res.array))
    }
    //println(s"Sending: "+new String(res.array))

    res.flip
    res 

  }

}
object HTTPResponse {

  def apply(contentType: String, content:ByteBuffer) : HTTPResponse = {


    new HTTPResponse(contentType,content)


  }

  def apply(contentType: String, content:String) : HTTPResponse = {


    new HTTPResponse(contentType,ByteBuffer.wrap(content.getBytes))


  }

}
