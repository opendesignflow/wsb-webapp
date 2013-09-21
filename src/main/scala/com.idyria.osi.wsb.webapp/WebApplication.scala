package com.idyria.osi.wsb.webapp

import com.idyria.osi.wsb.core.broker.tree._
import com.idyria.osi.wsb.core.message._
import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp.http.connector._
import com.idyria.osi.wsb.webapp.view._
import com.idyria.osi.wsb.webapp.view.sview._

import java.nio._
import java.io._
import scala.io.Source

/**
 * A Web Application can simply integrate as a Tree Intermediary
 *
 */
<<<<<<< HEAD
abstract class WebApplication(

    /**
     * The base URL path of the application
     */
    var basePath: String) extends Intermediary {

  // Constructor
  //-----------
  this.filter = s"""http:$basePath(.*):.*""".r

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

            fileSources.foreach {
              fileSource =>

                var possiblePath = s"${fileSource}${matched.group(1)}"

                // Try class Loader and stanadard file
                var resultURL = getClass.getClassLoader.getResource(possiblePath)
                if (resultURL == null) {
                  new File(possiblePath) match {
                    case f if (f.exists) => resultURL = f.toURI.toURL
                    case _               =>
                  }
                }
                println("Resource matcher: " + resultURL + " for " + possiblePath)

                resultURL match {

                  case null =>

                  // Read Content
                  //-------------------  
                  case url =>

                    var data = ByteBuffer.wrap(com.idyria.osi.tea.io.TeaIOUtils.swallow(url.openStream))

                    url.toString match {

                      // Standard file contents
                      //-----------------------
                      case path if (path.endsWith(".html")) => response(HTTPResponse("text/html", data), message)
                      case path if (path.endsWith(".css"))  => response(HTTPResponse("text/css", data), message)
                      case path if (path.endsWith(".js"))   => response(HTTPResponse("application/javascript", data), message)
                      case path if (path.endsWith(".png"))  => response(HTTPResponse("image/png", data), message)
                      case path if (path.endsWith(".jpg"))  => response(HTTPResponse("image/jpeg", data), message)
                      case path if (path.endsWith(".jpeg")) => response(HTTPResponse("image/jpeg", data), message)
                      case path if (path.endsWith(".gif"))  => response(HTTPResponse("image/gif", data), message)

                      // Special Views
                      //------------------------

                      //-- SView with not already created intermediary for this view
                      //--  * Create the intermediary
                      case path if (path.endsWith(".sview") && !WebApplication.this.intermediaries.exists { i => i.name.toString == path }) =>

                        WebApplication.this <= new Intermediary {

                          this.name = path
                          this.filter = s"http:${WebApplication.makePath(basePath, matched.group(1))}:GET".r

                          // Prepare an SView Renderer 
                          var sviewRenderer = new SViewRenderer(url)

                          println(s"Created new SView intermediary for : $filter and source file: ${sviewRenderer.path}")

                          downClosure = {
                            message =>
                              println("--> Producing content from SView from Intermediary")
                              response(HTTPResponse("text/html", sviewRenderer.produce), message)

                          }

                          upClosure = {
                            message =>

                          }

                        }
                      case path if (path.endsWith(".sview")) =>

                      case _                                 => response(HTTPResponse("text/plain", data), message)

                    }

                }

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
            case None                  =>
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
