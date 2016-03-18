package com.idyria.osi.wsb.webapp.resources

import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPResponse
import java.nio.ByteBuffer
import java.net.URL
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import java.io.File
import com.idyria.osi.wsb.webapp.http.message.HTTPPathIntermediary

class ResourcesIntermediary(basePath: String) extends HTTPPathIntermediary(basePath) {
  name = "Simple File Resources"

  acceptDown { message =>

    var r = (message.errors.isEmpty && message.upped == false)
    logFine[ResourcesIntermediary](s"Resource acccepts: " + message.path + " -> " + basePath + " -> " + message.errors.isEmpty)
    r
  }

  //Refuse messages with a path containing WEB-INF
  /*acceptDown { message =>

    !message.path.contains("WEB-INF")
  }*/

  // Resource Search
  //-------------------
  var fileSources = List[String]()

  /**
   * Add a new File Source, that can be used as URL/File Search path
   *
   * IF source is the empty string, it is transformed to "." because it means "current folder"
   */
  def addFilesSource(source: String) = {

    if (source == "") {
      fileSources = "." :: fileSources
    } else {
      fileSources = source :: fileSources
    }

  }
  /**
   * Search a resource at a given path
   */
  def searchResource(path: String): Option[URL] = {

    var extractedPath = path

    // Remove leading/trailing / from base path
    //-------------
    extractedPath = extractedPath.replaceAll("(.*)/$", "$1")
    extractedPath = extractedPath.replaceAll("^/(.*)", "$1")

    // Remove Base Path of application from path (/basePath/whatever must become /whatever)
    //----------
    /*var extractedPath = WebApplication.this.filter.findFirstMatchIn(path) match {
      case Some(extracted) => extracted.group(1)
      case None            => path
    }*/

    logFine[ResourcesIntermediary](s"**** Searching resource in : ${fileSources}")

    var res: Option[URL] = None
    // Try class Loader and stanadard file
    logFine[ResourcesIntermediary](s"**** Searching as Resource: ${extractedPath}")
    getClass.getClassLoader.getResource(extractedPath) match {

      case null =>
        this.fileSources.find {
          source =>

            // var possiblePath = new File(s"${source}${extractedPath}").toURI.toURL.toString

            var searchFile = new File(source, extractedPath.replace('/', File.separatorChar))
            logFine[ResourcesIntermediary](s"**** Searching as File: ${searchFile}")
            searchFile match {

              case f if (f.exists) =>

                logFine(s"**** Found!")
                res = Option(f.toURI.toURL)
                true

              case f => false
            }

        }

      case url =>
        logFine[ResourcesIntermediary](s"**** Found!")
        res = Option(url);
        true
    }

    res

  }

  /**
   * Path is the full path, including base application path
   */
  def searchResource(request: HTTPRequest): Option[URL] = {

    // Extract Base Path of application from path
    //----------
    //var extractedPath = WebApplication.this.filter.findFirstMatchIn(s"/${request.qualifier}".replace("//", "/"))
    //var res = this.searchResource(extractedPath.get.group(1))
    var res = this.searchResource(request.path)

    // var res = this.searchResource(request.qualifier)

    //println(s"*** Request Resource search of ${request.qualifier} against: ${WebApplication.this.filter.pattern.toString} : " + extractedPath + ", result -> " + res)

    res

  }

  // Get Message
  //--------------------
  this.onDownMessage {

    message =>
      //message.path = message.path.stripPrefix(basePath)
      logFine[ResourcesIntermediary](s"Resource process: " + message.path + " -> " + basePath)
      searchResource(message) match {

        //-- Found Read and return
        case Some(resourceURL) =>

          var data = ByteBuffer.wrap(com.idyria.osi.tea.io.TeaIOUtils.swallow(resourceURL.openStream))

          resourceURL.toString match {

            // Standard file contents
            //-----------------------
            case path if (path.endsWith(".html")) => response(HTTPResponse("text/html", data),message)
            case path if (path.endsWith(".css")) => response(HTTPResponse("text/css", data),message)
            case path if (path.endsWith(".js")) => response(HTTPResponse("application/javascript", data),message)
            case path if (path.endsWith(".png")) => response(HTTPResponse("image/png", data),message)
            case path if (path.endsWith(".jpg")) => response(HTTPResponse("image/jpeg", data),message)
            case path if (path.endsWith(".jpeg")) => response(HTTPResponse("image/jpeg", data),message)
            case path if (path.endsWith(".gif")) => response(HTTPResponse("image/gif", data),message)

            // Special Views
            //------------------------

            //-- SView with not already created intermediary for this view
            //--  * Create the intermediary
            //case path if (path.endsWith(".sview")) =>
            case _ => response(HTTPResponse("text/plain", data), message)

          }

        //-- Nothing found -> Continue to handler
        case None =>

      }

  }
}