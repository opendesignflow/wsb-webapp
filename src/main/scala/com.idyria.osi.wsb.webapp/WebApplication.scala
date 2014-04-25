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
package com.idyria.osi.wsb.webapp

import java.io._
import java.net.URL
import java.nio._
import scala.util.matching.Regex
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import com.idyria.osi.wsb.core.broker.tree._
import com.idyria.osi.wsb.core.message._
import com.idyria.osi.wsb.webapp.db.Database
import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.navigation._
import com.idyria.osi.wsb.webapp.navigation.NavigationRule
import com.idyria.osi.wsb.webapp.navigation.controller.Controller
import com.idyria.osi.wsb.webapp.navigation.controller.Controller
import com.idyria.osi.wsb.webapp.view._
import com.idyria.osi.wsb.webapp.view.ViewRenderer
import com.idyria.osi.wsb.webapp.view.sview._
import com.idyria.osi.ooxoo.db.store.fs.FSStore
import com.idyria.osi.wsb.webapp.injection.Injector
import com.idyria.osi.wsb.webapp.db.OOXOODatabase
import java.net.URLEncoder

/**
 * A Web Application can simply integrate as a Tree Intermediary
 *
 * Default Configurations:
 *
 *
 *
 */
class WebApplication(

    /**
     * The base URL path of the application
     */
    var basePath: String) extends Intermediary with Injector {

  // Constructor
  //-----------

  //-- Message filter for this base URL path
  this.filter = s"""http:$basePath(.*):.*""".r

  this.name = s"Application on $basePath"

  // Injection Support for various components
  //----------------------

  Injector(this)

  def supportedTypes: List[Class[_]] = List(classOf[Database], classOf[OOXOODatabase], classOf[WebApplication])

  def inject[T](id: String, dataType: Class[T]): Option[T] = {

    dataType match {

      //-- Application Inject
      //----------------
      case t if (t == classOf[WebApplication]) ⇒ Option(this.asInstanceOf[T])

      //-- Database inject  
      //------------
      case t if (t == classOf[Database]) ⇒

        // Get or create
        this.getDatabase(id) match {
          case Some(db) ⇒ Some(db.asInstanceOf[T])
          case None     ⇒ Some(this.createDatabase(id).asInstanceOf[T])
        }

      case t if (t == classOf[OOXOODatabase]) ⇒

        // Get or create
        this.getDatabase(id) match {
          case Some(db) ⇒ Some(db.asInstanceOf[T])
          case None     ⇒ Some(this.createDatabase(id).asInstanceOf[T])
        }

      case _ ⇒ None

    }
  }

  // Databases
  //----------------

  /**
   * Just the list of the configured databases
   */
  var databases = Map[String, Database]()

  /**
   * Base folder path for all the databases
   */
  var databaseBasePath = new File("db")

  /**
   * Add a database to the application
   */
  def addDatabase(db: Database) = {

    db.id match {
      case null ⇒ throw new RuntimeException(s"Cannot add Database if no id is specified")
      case id if (this.databases.find(t ⇒ t._1 == id) == Some) ⇒ throw new RuntimeException(s"Cannot add Database $id which already exists")
      case id ⇒ this.databases = this.databases + (db.id.toString -> db)
    }

  }

  /**
   * Create Database using application configuration on where to setup the databases etc...
   */
  def createDatabase(id: String): Database = {

    var db = new OOXOODatabase(new FSStore(new File(databaseBasePath, id)))
    db.id = id
    this.addDatabase(db)

    db
  }

  /**
   * Get a named database
   */
  def getDatabase(id: String): Option[Database] = this.databases.get(id)

  // Lifecycle
  //---------------------

  /**
   * Start:
   *
   *  - Load Configuration files that can be present in application files
   *
   */
  def lStart = {

    // Navigation
    //----------------------------
    this.searchResource("WEB-INF/navigation.xml") match {
      case Some(navigationURL) ⇒

        println("Parsing navigation: " + navigationURL + " -> " + navigationConfig.fullPath)

        // Parse
        //---------
        var io = new StAXIOBuffer(navigationURL)
        navigationConfig.appendBuffer(io)
        io.streamIn

        //navigationConfig
        //var navigationConfig = Navigation(navigationURL)

        // Read In
        //--------------

        // List of current path components to create views at the right place
        var currentPath = List(basePath)

        // Call Transform to find out all the Rules
        //------
        navigationConfig.onAll {
          case r: GroupTraitRule ⇒

            try {
              var view = r.toView match {
                case null   ⇒ ""
                case toView ⇒ toView.toString
              }
              this.addRule(r.for_, Thread.currentThread().getContextClassLoader().loadClass(r.id.toString).newInstance().asInstanceOf[NavigationRule], view)
            } catch {
              case e: Throwable ⇒ throw new RuntimeException(s"An Error occured while setting navigation rule ${r.fullPath} from $navigationURL: ${e.getMessage()} ")
            }
          case _ ⇒
        }

      case None ⇒
    }
  }

  // Resources and File Sources
  //-------------------------

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

    logFine[WebApplication](s"**** Searching resource in : ${fileSources}")

    var res: Option[URL] = None
    this.fileSources.find {
      source ⇒

        // var possiblePath = new File(s"${source}${extractedPath}").toURI.toURL.toString

        logFine[WebApplication](s"**** Searching as URL: ${extractedPath}")

        // Try class Loader and stanadard file
        getClass.getClassLoader.getResource(extractedPath) match {

          case null ⇒

            var searchFile = new File(source, extractedPath.replace('/', File.separatorChar))
            logFine[WebApplication](s"**** Searching as File: ${searchFile}")
            searchFile match {

              case f if (f.exists) ⇒

                logFine(s"**** Found!")
                res = Option(f.toURI.toURL)
                true

              case f ⇒ false
            }
          case url ⇒
            logFine[WebApplication](s"**** Found!")
            res = Option(url);
            true
        }
    }

    res

  }

  /**
   * Path is the full path, including base application path
   */
  def searchResource(request: HTTPRequest): Option[URL] = {

    // Extract Base Path of application from path
    //----------
    var extractedPath = WebApplication.this.filter.findFirstMatchIn(request.qualifier)
    var res = this.searchResource(extractedPath.get.group(1))

    // var res = this.searchResource(request.qualifier)

    //println(s"*** Request Resource search of ${request.qualifier} against: ${WebApplication.this.filter.pattern.toString} : " + extractedPath + ", result -> " + res)

    res

  }

  // Main Web App intermediary
  //----------------------------
  downClosure = {

    message ⇒

    //---- Session
    //--------------------
    //println("[Session] In Session Intermediary")

  }

  upClosure = {

    message ⇒

      //---- Session
      //---------------------
      if (message.relatedMessage != null && message.relatedMessage.isInstanceOf[HTTPRequest] && message.relatedMessage.asInstanceOf[HTTPRequest].session != null) {

        var httpMessage = message.relatedMessage.asInstanceOf[HTTPRequest]

        // Copy Session info to response if there is one
        message.asInstanceOf[HTTPResponse].session = httpMessage.session

        // Update Session Object Path
        message.asInstanceOf[HTTPResponse].session.path = WebApplication.this.basePath
      }
  }

  // Controllers
  //-------------------
  var controllers = Map[String, Controller]()

  /**
   * Add a controler from name and closure
   * Creates a default controller implementation that relies on provided closure
   */
  def addController(controlerName: String)(closure: (WebApplication, HTTPRequest) ⇒ String): Unit = {

    var newController = new Controller {

      this.name = controlerName

      def execute(application: WebApplication, request: HTTPRequest): String = {
        closure(application, request)
      }
    }
    this.addController(newController)

  }

  /**
   * Add a controler from full implementation
   */
  def addController(controller: Controller): Unit = {

    // Inject
    Injector.inject(controller)

    // Add
    this.controllers = this.controllers + (controller.name -> controller)

    //println(s"Registering Controller: " + controller.name)

  }

  // Navigation Rules 
  //------------------------

  /**
   * Default Navigation configuration that can be enriched
   */
  val navigationConfig = new DefaultNavigation
  navigationConfig.fullPath = basePath

  /**
   * Maps navigation rules to view IDs
   */
  var navigationRules = Map[Regex, NavigationRule]()

  def addRule(paths: Regex, rule: NavigationRule, view: String) = {
    rule.outcome = view
    this.navigationRules = this.navigationRules + (paths -> rule)
  }

  // Controlers Intermediary
  //-------------------------------
  this <= new Intermediary {
    name = "Controllers"

    downClosure = {
      message ⇒

        val httpMessage: HTTPRequest = message.asInstanceOf[HTTPRequest]

        // Controllers
        //------------------
        httpMessage.getURLParameter("action") match {
          case Some(action) ⇒

            println(s"[Action] Should be running action '${action}'")

            WebApplication.this.controllers.get(action) match {
              case Some(controller) ⇒

                //-- Execute Closure
                controller.execute(WebApplication.this, httpMessage) match {

                  //-- Change View Id to Result view ID if not ""
                  case resultView if (resultView != "") ⇒ httpMessage.changePath(resultView)
                  case _                                ⇒
                }

                //-- If no render, stop here
                httpMessage.getURLParameter("noRender") match {
                  case Some(_) ⇒

                    response(HTTPResponse("application/json", "{}"), message)

                  case None ⇒
                }

              case None ⇒ throw new RuntimeException(s"[Action] ...no handler found for action '${action}'")
            }
          case None ⇒
        }

    }

  }

  // Views Intermediary
  //  - Takes care of view navigation rules
  //  - Handles Controllers before view handling
  //  - Creates special views from resources paths
  //---------------------------

  val viewsIntermediary = this <= new Intermediary {

    name = "Views"

    acceptDown { message ⇒ (message.errors.isEmpty && message.upped == false) }

    downClosure = {
      message ⇒

        val httpMessage: HTTPRequest = message.asInstanceOf[HTTPRequest]

        // Navigation Rules 
        //-----------------------
        WebApplication.this.navigationRules.find {

          case (pathMatch, rule) ⇒ (!pathMatch.findFirstIn(httpMessage.path).isEmpty) && ((rule.evaluate(WebApplication.this, message.asInstanceOf[HTTPRequest])) != true)

        } match {
          case Some((pathMatch, rule)) ⇒ httpMessage.changePath(WebApplication.makePath(basePath, rule.outcome))
          case None                    ⇒
        }

        // Special View intermediaries
        //-------------------
        if (message.asInstanceOf[HTTPRequest].path.endsWith(".sview") && !this.intermediaries.exists { i ⇒ i.name.toString == message.asInstanceOf[HTTPRequest].path }) {

          println(s"**** Path for SView, need to create View Intermediary ${message.asInstanceOf[HTTPRequest].path}")

          // Try to locate
          //---------------------
          WebApplication.this.searchResource(message.asInstanceOf[HTTPRequest]) match {
            case Some(url) ⇒

              this <= new Intermediary {

                this.name = message.asInstanceOf[HTTPRequest].path
                this.filter = (s"http:${message.asInstanceOf[HTTPRequest].path}:.*").r

                // Prepare an SView Renderer 
                var sviewRenderer = new SViewRenderer(url)

                println(s"Created new SView intermediary for : $filter and source file: ${sviewRenderer.path}")

                downClosure = {
                  message ⇒
                    println("--> Producing content from SView from Intermediary")
                    response(HTTPResponse("text/html", sviewRenderer.produce(WebApplication.this, message.asInstanceOf[HTTPRequest])), message)

                }

                upClosure = {
                  message ⇒

                }

              }

            case None ⇒
          }

        }

    }

    upClosure = {
      message ⇒

    }

    // Add WWWView intermediary
    //-------------------------
    this <= new HTTPIntermediary {

      filter = """http:.*.view:.*""".r
      name = "WWView"

      var baseView = new WWWView
      baseView.application = WebApplication.this

      this.onDownMessage {
        m ⇒

          // Search Resource 
          searchResource(m) match {
            case Some(url) ⇒

              // Support various outputs
              //---------------------
              m.parameters.get("Accept") match {

                // JSON
                //---------------
                case Some(v) if (v.startsWith("application/json")) ⇒

                  var rendered = WWWView.compile(url).produce(WebApplication.this, m).toString()
                  var jsonRes = s"""{"content":"${URLEncoder.encode(rendered)}"}"""
                  response(HTTPResponse("application/json", jsonRes))

                // Otherwise -> HTML
                //------------
                case _ =>
                  response(HTTPResponse("text/html", WWWView.compile(url).produce(WebApplication.this, m).toString()))
              }

            case None ⇒

              throw new RuntimeException("Cannot Serve Resource because no view file could be found")
          }

      }

    }

  }

  // Default Intermediary for Content
  // -- Try to find requested path as plain resource
  // -- Skip if the request message has previous errors
  //------------------------------
  this <= new Intermediary {

    name = "Simple File Resources"

    acceptDown { message ⇒ (message.errors.isEmpty && message.upped == false) }

    downClosure = {

      message ⇒

        WebApplication.this.searchResource(message.asInstanceOf[HTTPRequest]) match {

          //-- Found Read and return
          case Some(resourceURL) ⇒

            var data = ByteBuffer.wrap(com.idyria.osi.tea.io.TeaIOUtils.swallow(resourceURL.openStream))

            resourceURL.toString match {

              // Standard file contents
              //-----------------------
              case path if (path.endsWith(".html")) ⇒ response(HTTPResponse("text/html", data))
              case path if (path.endsWith(".css"))  ⇒ response(HTTPResponse("text/css", data))
              case path if (path.endsWith(".js"))   ⇒ response(HTTPResponse("application/javascript", data))
              case path if (path.endsWith(".png"))  ⇒ response(HTTPResponse("image/png", data))
              case path if (path.endsWith(".jpg"))  ⇒ response(HTTPResponse("image/jpeg", data))
              case path if (path.endsWith(".jpeg")) ⇒ response(HTTPResponse("image/jpeg", data))
              case path if (path.endsWith(".gif"))  ⇒ response(HTTPResponse("image/gif", data))

              // Special Views
              //------------------------

              //-- SView with not already created intermediary for this view
              //--  * Create the intermediary
              //case path if (path.endsWith(".sview")) =>
              case _                                ⇒ response(HTTPResponse("text/plain", data), message)

            }

          //-- Nothing found -> Continue to handler
          case None ⇒
        }

    }
    // EOF down closure for content handler intermediary
  }
  // EOF content intermediary

  //-- Not Found 404 : only if not "upped", meaning a response has not been sent for this message
  //---------------------------
  this <= new Intermediary {

    name = "404 Not Found"

    acceptDown { message ⇒ (message.errors.isEmpty && message.upped == false) }

    downClosure = {

      message ⇒

        println(s"********** ERROR 404 Not Found Intermediary for (${message.qualifier}) **************")

        // Try to use custom view
        //-----------------
        searchResource("WEB-INF/404.sview") match {
          case Some(customView) ⇒

            var responseMessage = HTTPResponse("text/html", SView(customView).render(WebApplication.this, message.asInstanceOf[HTTPRequest]))
            responseMessage.code = 404
            response(responseMessage, message)

          case None ⇒

            var errorText = s"""
			Could not Find Content for view: ${message.asInstanceOf[HTTPRequest].path} 
			"""
            var responseMessage = HTTPResponse("text/html", errorText)
            responseMessage.code = 404

            response(responseMessage, message)
        }

    }

  }

  //-- General Errors: 500 : only if errors
  //----------------------
  this <= new Intermediary {

    name = "500 Errors"

    acceptDown { m ⇒ !m.errors.isEmpty && m.upped == false }

    downClosure = {
      message ⇒

        println(s"********** ERROR 500 Error while answering Intermediary for (${message.qualifier}) **************")

        var accept = message.asInstanceOf[HTTPRequest].parameters.get("Accept")

        println("********** Error format: " + accept)

        accept match {

          // JSON
          //---------------
          case Some(v) if (v.startsWith("application/json")) ⇒

            var errors = message.errors.collect {
              case e: ForException ⇒

                e.printStackTrace()

                s"""{"error": "${e.getLocalizedMessage()}", "source" : "${e.target}" }"""

              case e: Throwable ⇒

                e.printStackTrace()

                s"""{"error": "${e.getLocalizedMessage()}"}"""

            }.mkString(",")

            var responseMessage = HTTPResponse("application/json", s"""{"errors" : [$errors]}""")
            responseMessage.code = 500

            response(responseMessage, message)

          // Default : HTML
          //----------------------
          case _ =>

            message.errors.foreach {
              e =>
                e.printStackTrace()
            }

            var errorText = s"""       
        		Some errors happenned for view: ${message.asInstanceOf[HTTPRequest].path} :
	        	${
              // message.errors.map(e => List(e.getMessage.replace("""\n""", "<br/>"), e.getStackTrace().mkString("<br/>")).mkString("<br/>")).mkString("<br/>")

              message.errors.map(e ⇒ s"""<div><h3>Error: ${e.getClass().getCanonicalName()} : ${e.getLocalizedMessage()}</h3></div><div><pre>${List(e.getMessage, e.getStackTrace().mkString("\n")).mkString}</pre></div>""").mkString("\n\n")

            }
	
			"""
            var responseMessage = HTTPResponse("text/html", errorText)
            responseMessage.code = 500

            response(responseMessage, message)

        }
      // EOF Accept match

    }

  }

  // View Definitions
  //----------------------

  /**
   * Add intermediary on path and bind with ViewRenderer result
   */
  def addView(path: String, renderer: ViewRenderer) = {

    viewsIntermediary <= new Intermediary {

      this.filter = (s"""http:${WebApplication.makePath(basePath, path)}:GET""").r

      downClosure = {
        message ⇒

          println(s"Rendering view: ${this.filter}")

          var result = renderer.produce(WebApplication.this, message.asInstanceOf[HTTPRequest])

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
