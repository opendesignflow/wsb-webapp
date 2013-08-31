package com.idyria.osi.wsb.webapp

import com.idyria.osi.wsb.core.broker.tree._
import com.idyria.osi.wsb.core.message._
import com.idyria.osi.wsb.core.message.http._
import com.idyria.osi.wsb.webapp.view._

import java.nio._
import java.io._
import scala.io.Source

/**
    A Web Application can simply integrate as a Tree Intermediary

*/
class WebApplication (

    /**
        The base URL path of the application
    */
    var basePath : String) extends Intermediary {

    // Constructor
    //-----------
    this.filter = s"""http:($basePath.*):.*""".r


    // File Sources
    //-------------------------
    var fileSources = List[String]()

    def addFilesSource(source: String) = {
        fileSources = source :: fileSources 

    }

    // Default Intermediary behavior
    //------------------------------
     downClosure = {
        message => 

            // If actual request path can match a file in one of the sources, then return this path
            this.filter.findFirstMatchIn(message.qualifier) match {

                case Some(matched) => 

                    fileSources.foreach {
                        fileSource =>

                            var possiblePath = s"${fileSource}${matched.group(1)}"
                            var resultURL = getClass.getClassLoader.getResource(possiblePath)

                            resultURL match {

                                case null => 

                                // Read Content
                                case url => 


                                    response(HTTPResponse("text/html",Source.fromInputStream(url.openStream).mkString),message)
                            }
                            
                    }

                case None => 
            }

    }

    // API Definitions
    //------------------------
    def addControler(path:String)(action: Message => Unit) = {

        this <= new Intermediary {

            this.filter = s"""http:${WebApplication.makePath(basePath,path)}:(POST|PUT)""".r

            downClosure = {
                message => action(message)
            }

        }

    }

    // View Definitions
    //----------------------

    /**
        Add intermediary on path and bind with ViewRenderer result
    */
    def addView(path:String,renderer: ViewRenderer) = {

        this <= new Intermediary {

            this.filter = s"""http:${WebApplication.makePath(basePath,path)}:GET""".r

            downClosure = {
                message => 

                    println(s"Rendering view: ${this.filter}")

                    var result = renderer.produce

                    response(HTTPResponse("text/html",result),message)

            }
        }

    }

}

object WebApplication {

    def makePath(components:String*) = {

        // Make it
        var path = components.mkString("","/","")

        // Replace all "///+" by "/"
        path.replaceAll("/+","/") 

    }

}
