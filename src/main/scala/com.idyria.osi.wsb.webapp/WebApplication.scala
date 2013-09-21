package com.idyria.osi.wsb.webapp

import com.idyria.osi.wsb.core.broker.tree._
import com.idyria.osi.wsb.core.message._
import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp.http.connector._
import com.idyria.osi.wsb.webapp.view._

import java.nio._
import java.io._
import scala.io.Source

/**
 * A Web Application can simply integrate as a Tree Intermediary
 *
 */
class WebApplication(

  /**
   * The base URL path of the application
   */
  var basePath: String) extends Intermediary {

  // Constructor
  //-----------
  this.filter = s"""http:($basePath.*):.*""".r

  // File Sources
  //-------------------------
  var fileSources = List[String]()

  def addFilesSource(source: String) = {
    fileSources = source :: fileSources

  }

  // Main Web App intermediary
  //----------------------------
  downClosure = {

    message =>

      //---- Session
      //--------------------
      println("[Session] In Session Intermediary")

  }

  upClosure = {

    message =>

      //---- Session
      //---------------------
      if (message.relatedMessage != null && message.relatedMessage.isInstanceOf[HTTPRequest] && message.relatedMessage.asInstanceOf[HTTPRequest].session != null) {

        var httpMessage = message.relatedMessage.asInstanceOf[HTTPRequest]
        message.asInstanceOf[HTTPResponse].session = httpMessage.session
      }
  }

  // Default Intermediary for Content
  //------------------------------
  this <= new Intermediary {

    downClosure = {

      message =>

        // If actual request path can match a file in one of the sources, then return this path
        WebApplication.this.filter.findFirstMatchIn(message.qualifier) match {

          case Some(matched) =>
          	
            var found = false
            fileSources.foreach {
              case fileSource if(!found) =>

                // Try as classpath URL
                //---------------------------
                var urlPath = java.net.URLDecoder.decode(matched.group(1),"UTF-8").drop(1)
                var possiblePath = s"${fileSource}${urlPath}"
                var resultURL = getClass.getClassLoader.getResource(possiblePath)
                
                // IF not found, try as simple content
                //-------------
                if (resultURL==null) {
                  var filePath = new File("./"+urlPath)
                  println(s"-> Trying: $filePath -> ${filePath.exists}")
                  if (filePath.exists && filePath.isFile) {
                    resultURL = filePath.toURI.toURL
                  }
                }
                println(s"-> Content: $possiblePath -> $resultURL ")

                resultURL match {

                  case null =>

                  // Read Content
                  case url =>
                  	
                    found = true
                    var data = ByteBuffer.wrap(com.idyria.osi.tea.io.TeaIOUtils.swallow(url.openStream))

                    url.toString match {
                      case path if (path.endsWith(".html")) => response(HTTPResponse("text/html", data), message)
                      case path if (path.endsWith(".css")) => response(HTTPResponse("text/css", data), message)
                      case path if (path.endsWith(".js")) => response(HTTPResponse("application/javascript", data), message)
                      case path if (path.endsWith(".png")) => response(HTTPResponse("image/png", data), message)
                      case path if (path.endsWith(".jpg")) => response(HTTPResponse("image/jpg", data), message)
                      case path if (path.endsWith(".jpeg")) => response(HTTPResponse("image/jpg", data), message)
                      case path if (path.endsWith(".gif")) => response(HTTPResponse("image/gif", data), message)
                      case path if (path.endsWith(".zip")) => response(HTTPResponse("application/zip", data), message)
                      case _ => response(HTTPResponse("application/octet-stream", data), message)
                    }
                    
                    

                }
              case _ =>
            }

          case None =>
        }
      // EOF file found match

    }
    // EOF down closure for content handler intermediary
  }
  // EOF content intermediary

  // API Definitions
  //------------------------
  def addControler(path: String)(action: HTTPRequest => Option[HTTPResponse]) = {

    this <= new Intermediary {

      this.filter = s"""http:${WebApplication.makePath(basePath, path)}:(POST|PUT)""".r

      downClosure = {
        message =>
          action(message.asInstanceOf[HTTPRequest]) match {
            case Some(responseMessage) => response(responseMessage, message)
            case None =>
          }
      }

    }

  }

  // View Definitions
  //----------------------

  /**
   * Add intermediary on path and bind with ViewRenderer result
   */
  def addView(path: String, renderer: ViewRenderer) = {

    this <= new Intermediary {

      this.filter = s"""http:${WebApplication.makePath(basePath, path)}:GET""".r

      downClosure = {
        message =>

          println(s"Rendering view: ${this.filter}")

          var result = renderer.produce

          response(HTTPResponse("text/html", ByteBuffer.wrap(result.getBytes)), message)

      }
    }

  }

}

object WebApplication {

  def makePath(components: String*) = {

    // Make it
    var path = components.mkString("", "/", "")

    // Replace all "///+" by "/"
    path.replaceAll("/+", "/")

  }

}
