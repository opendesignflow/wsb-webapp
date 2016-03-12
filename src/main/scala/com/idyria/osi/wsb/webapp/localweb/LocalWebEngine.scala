package com.idyria.osi.wsb.webapp.localweb

import com.idyria.osi.wsb.core.WSBEngine
import com.idyria.osi.wsb.webapp.http.connector.HTTPConnector
import scala.collection.mutable.Stack
import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPResponse
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder
import com.idyria.utils.java.io.TeaIOUtils
import com.idyria.osi.wsb.webapp.resources.ResourcesIntermediary
import com.idyria.osi.tea.logging.TeaLogging
import com.idyria.osi.tea.logging.TLog
import com.sun.xml.internal.ws.api.server.ResourceInjector
import com.idyria.osi.wsb.webapp.resources.ResourcesIntermediary

/**
 * This Engine starts HTTP Support, and enables easily finding GUI views to be displayed on Web Browser
 */
object LocalWebEngine extends WSBEngine with DefaultBasicHTMLBuilder {

  //-- HTTP Connector
  var httpConnector = new HTTPConnector(6666)
  this.network.addConnector(httpConnector)

  //-- Start
  this.lInit
  this.lStart

  // Default Broker
  //----------------------
  TLog.setLevel(classOf[ResourcesIntermediary], TLog.Level.FULL)
  
  this.broker <= new HTTPIntermediary {

    //-- Resources 
    this <= new ResourcesIntermediary("/resources")
    
    //-- Global case 
    this.onDownMessage {

      //-- Front View Error if none defined
      case req if (req.path == "/" && frontViewStack.size == 0) =>
        println(s"Received Message: " + req.path)

        var r = new HTTPResponse();
        r.code = 404

        r.htmlContent = html {
          head {

          }
          body {
            p {
              textContent("No Front View Defined")
            }
          }
        }
        /*r.contentType = "text/html"
        r.content = (html {
          
        }).toString()*/
        response(r, req)

      //-- Normal Front View 
      //-----------------
      case req if (req.path == "/") =>
        println(s"Processing Front")
        var view = frontViewStack.head
        var r = new HTTPResponse();
        var rendered = view.render
        println(s"Done Rendering")
        r.htmlContent = rendered
        response(r, req)
        
      /*case req if(req.path.startsWith("/resources")) => 
        
        var rPath = req.path.stripPrefix("/resources")
        
        //-- Try to search using local file or resource handler
        //-------------
        getClass.getClassLoader.getResource(rPath) match {
          case null => 
          case url => 
            var content = TeaIOUtils.swallow(url.openStream())
            response
        }*/
        
      case _ =>
    }

    //println(s"Received HTTP: "+req.path)

  }

  // Management
  //--------------
  var frontViewStack = Stack[LocalWebHTMLVIew]()

  /**
   * Add the Front view on top of views stack
   *
   */
  def setFrontView(v: LocalWebHTMLVIew) = {
    frontViewStack.push(v)
  }

  /**
   * Replace Front View by this view
   */
  def replaceFrontView(v: LocalWebHTMLVIew) = {
    frontViewStack.size match {
      case 0 => setFrontView(v)
      case _ =>
        frontViewStack.update(0, v)
    }
  }

}
