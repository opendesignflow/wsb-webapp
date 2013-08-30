package com.idyria.osi.wsb.webapp

import com.idyria.osi.wsb.core.broker.tree._
import com.idyria.osi.wsb.core.message._
import com.idyria.osi.wsb.core.message.http._
import com.idyria.osi.wsb.webapp.view._

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
    this.filter = s"""http:$basePath.*""".r


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
