package com.idyria.osi.wsb.webapp.mime

object MimeTypes {
  
  def nameToMime(name:String) : Option[String] = {
    
    name match {

              // Standard file contents
              //-----------------------
              case path if (path.endsWith(".html")) =>

                Some("text/html")
              //response(HTTPResponse("text/html", data), message)
              case path if (path.endsWith(".css")) =>
                Some("text/css")
              //response(HTTPResponse("text/css", data), message)
              case path if (path.endsWith(".js")) =>
                Some("application/javascript")
              //response(HTTPResponse("application/javascript", data), message)
              case path if (path.endsWith(".png")) =>
                Some("image/png")
              // response(HTTPResponse("image/png", data), message)
              case path if (path.endsWith(".jpg")) =>
                Some("image/jpeg")
              //response(HTTPResponse("image/jpeg", data), message)
              case path if (path.endsWith(".jpeg")) =>
                Some("image/jpeg")
              // response(HTTPResponse("image/jpeg", data), message)
              case path if (path.endsWith(".gif")) =>
                Some("image/gif")
              //response(HTTPResponse("image/gif", data), message)
              case path if (path.endsWith(".avi")) =>
                Some("video/x-msvideo")
              //response(HTTPResponse("video/x-msvideo", data), message)
              case path if (path.endsWith(".eps")) =>
                Some("application/postscript")
              //response(HTTPResponse("application/postscript", data), message)
              case path if (path.endsWith(".webm")) =>
                Some("video/webm")
              //response(HTTPResponse("video/webm", data), message)
              case path if (path.endsWith(".mp4")) =>
                Some("video/mp4")
              case path if (path.endsWith(".pdf")) =>
                Some("application/pdf")
              //response(HTTPResponse("video/mp4", data), message)

              // Special Views
              //------------------------

              //-- SView with not already created intermediary for this view
              //--  * Create the intermediary
              //case path if (path.endsWith(".sview")) =>
              case _ =>
                Some("text/plain")
              // response(HTTPResponse("text/plain", data), message)

            }
    
  }
  
}