package com.idyria.osi.wsb.webapp

import com.idyria.osi.wsb.core.broker.tree._
import com.idyria.osi.wsb.core.message._
import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp.http.connector._
import com.idyria.osi.wsb.webapp.view._
import com.idyria.osi.wsb.webapp.view.sview._

import com.idyria.osi.wsb.webapp.navigation._

import java.nio._
import java.io._
import scala.io.Source

import java.net.URL
import scala.util.matching.Regex

/**
 * A Web Application can simply integrate as a Tree Intermediary
 *
 */
class WebApplication (
 
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
  
  /**
   * Path is the full path, including base application path
   */
  def searchResource(request : HTTPRequest) : Option[URL] = {
     
    // Extract Base Path of application from path
    //----------
    var extractedPath = WebApplication.this.filter.findFirstMatchIn(request.qualifier)
    
    
    var res : Option[URL] = None
    this.fileSources.foreach {
      
      case source if (res==None) => 
        
        var possiblePath = new File(s"${source}${extractedPath.get.group(1)}").toURI.toURL.toString
 
        println(s"**** Searching as URL: ${extractedPath.get.group(1)}")
        
        // Try class Loader and stanadard file
        getClass.getClassLoader.getResource(extractedPath.get.group(1)) match {
          
          case null => 
            
            var searchFile = new File(source,extractedPath.get.group(1).replace('/',File.separatorChar))
            println(s"**** Searching as File: ${searchFile}")
            searchFile match {
            
            case f if (f.exists) =>  
              
              println(s"**** Found!")
              res =  Option(f.toURI.toURL)
            case f              =>    
          }
          case url => res = Option( url )
        }
        
    }
    
    println(s"*** Resource search of ${request.qualifier} against: ${WebApplication.this.filter.pattern.toString} : "+extractedPath+", result -> "+res)
    
    
    res
    /*
    var collectResult = this.fileSources.collectFirst {
      
      case source =>  
        
        var possiblePath = s"${source}${extractedPath.get.group(1)}"

        // Try class Loader and stanadard file
        getClass.getClassLoader.getResource(possiblePath) match {
          
          case null => new File(possiblePath) match {
            
            case f if (f.exists) =>   f.toURI.toURL
            case _               =>  null  
          }
          case url =>  url
        }
    }
    
    collectResult match {
      case Some(url) if (url==null) => None
      case Some(url) => Option(url.asInstanceOf[URL])
      case None => None
    }*/
   
    
  }
  
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
  
  // Controllers
  //-------------------
   var controlers = Map[String, (WebApplication,HTTPRequest) => String]()
   
  def addControler(controlerName : String)(closure: (WebApplication,HTTPRequest) => String) = {
    this.controlers = this.controlers + (controlerName->closure) 
  }
  
  // Navigation Rules 
  //------------------------
   /**
   * Maps navigation rules to view IDs
   */
  var navigationRules = Map[Regex,NavigationRule]()
     
  def addRule(paths:Regex, rule:NavigationRule,view: String) = {
    rule.outcome = view
    this.navigationRules = this.navigationRules + (paths -> rule)
  }
  
  // Views Intermediary
  //  - Takes care of view navigation rules
  //  - Handles Controllers before view handling
  //  - Creates special views from resources paths
  //---------------------------
  
  val viewsIntermediary = this <= new Intermediary {
    
    downClosure = {
      message => 
        
        val httpMessage : HTTPRequest = message.asInstanceOf[HTTPRequest]
        
        // Controllers
        //------------------
        httpMessage.parameters.get("action") match {
          case Some(action) => 
            
            println(s"[Action] Should be running action '${action}'")
            WebApplication.this.controlers.get(action) match {
	            case Some(actionClosure) => 
	              
	              //-- Execute Closure
	              actionClosure(WebApplication.this,httpMessage) match {
	                
	                 //-- Change View Id to Result view ID if not ""
	                case resultView if (resultView!="") =>  httpMessage.changePath(resultView)
	                case _ =>
	              }
	             
	              
	            case None =>
            }
          case None => 
        }
        
        // Navigation Rules 
        //-----------------------
        WebApplication.this.navigationRules.find {
          
           case (pathMatch,rule) => (!pathMatch.findFirstIn(httpMessage.path).isEmpty) && ((rule.evaluate(WebApplication.this,message.asInstanceOf[HTTPRequest])) != true)
           
        } match {
          case Some((pathMatch,rule)) => httpMessage.changePath(WebApplication.makePath(basePath,rule.outcome))
          case None =>
        }
        
        // Special View intermediaries
        //-------------------
        if (message.asInstanceOf[HTTPRequest].path.endsWith(".sview") && !this.intermediaries.exists{ i => i.name.toString == message.asInstanceOf[HTTPRequest].path }) {
        	
          println(s"**** Path for SView, need to create View Intermediary ${message.asInstanceOf[HTTPRequest].path}")
          
        	// Try to locate
            //---------------------
        	WebApplication.this.searchResource(message.asInstanceOf[HTTPRequest]) match {
        	  case Some(url) =>
        	  		
    	    	this <= new Intermediary {

	              this.name = message.asInstanceOf[HTTPRequest].path
	              this.filter = (s"http:${message.asInstanceOf[HTTPRequest].path}:GET").r
	
	              // Prepare an SView Renderer 
	              var sviewRenderer = new SViewRenderer(url)
	
	              println(s"Created new SView intermediary for : $filter and source file: ${sviewRenderer.path}")
	
	              downClosure = {
	                message =>
	                  println("--> Producing content from SView from Intermediary")
	                  response(HTTPResponse("text/html", sviewRenderer.produce(WebApplication.this,message.asInstanceOf[HTTPRequest])), message)
	
	              }
	
	              upClosure = {
	                message =>
	
	              }
	
	            }
        	    
        	    
        	  case None => 
        	}
          
            
        }
        
    }
    
    upClosure = {
      message => 
        
    }
    
  }
  
  
  // Last Intermediary : Error not found
  //---------------------
  this <= new Intermediary {
    
    downClosure = {
      message => 
        
        println(s"********** ERROR Not Found Intermediary for (${message.qualifier}) **************")
        
        var errorText = s"""
        Could not Find Content for view: ${message.asInstanceOf[HTTPRequest].path} 
        """
        var responseMessage = HTTPResponse("text/html", errorText)
        responseMessage.code = 404
        
        response(responseMessage, message)
        
    }
    
    upClosure = {
      message => 
    }
  }
 
  // API Definitions
  //------------------------
  /*def addControler(path: String)(action: HTTPRequest => Option[HTTPResponse]) = {

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

  }*/

  // View Definitions
  //----------------------
 
  /**
   * Add intermediary on path and bind with ViewRenderer result
   */
  def addView(path: String, renderer: ViewRenderer) = {

    viewsIntermediary <= new Intermediary {

      this.filter = (s"""http:${WebApplication.makePath(basePath, path)}:GET""").r

      downClosure = {
        message =>

          println(s"Rendering view: ${this.filter}")

          var result = renderer.produce(WebApplication.this,message.asInstanceOf[HTTPRequest])
 
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
