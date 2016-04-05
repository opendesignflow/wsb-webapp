package com.idyria.osi.wsb.webapp.localweb

import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import com.idyria.osi.tea.logging.TLog
import com.idyria.osi.vui.html.Body
import com.idyria.osi.vui.html.HTMLNode
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder
import com.idyria.osi.wsb.core.WSBEngine
import com.idyria.osi.wsb.core.network.connectors.tcp.TCPNetworkContext
import com.idyria.osi.wsb.webapp.http.connector.HTTPConnector
import com.idyria.osi.wsb.webapp.http.connector.websocket.WebsocketInterface
import com.idyria.osi.wsb.webapp.http.connector.websocket.WebsocketIntermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPPathIntermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.http.message.HTTPResponse
import com.idyria.osi.wsb.webapp.http.session.Session
import com.idyria.osi.wsb.webapp.http.session.SessionIntermediary
import com.idyria.osi.wsb.webapp.resources.ResourcesIntermediary
import com.idyria.osi.wsb.webapp.resources.ResourcesIntermediary
import com.idyria.osi.ooxoo.core.buffers.structural.xelement
import com.idyria.osi.wsb.core.message.soap.SOAPMessage
import com.idyria.osi.wsb.webapp.http.connector.websocket.WebsocketProtocolhandler
import com.idyria.osi.ooxoo.core.buffers.datatypes.DateTimeBuffer
import com.idyria.osi.vui.implementation.javafx.JavaFXRun
import com.idyria.osi.vui.implementation.javafx.JavaFXNodeDelegate
import javafx.scene.control.Hyperlink
import javafx.scene.Scene
import javafx.stage.Stage
import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory
import javafx.scene.paint.Color
import javafx.event.EventHandler
import javafx.stage.WindowEvent

@xelement
class Ack extends ElementBuffer {

}

class SingleViewIntermediary(basePath: String, var viewClass: Class[_ <: LocalWebHTMLVIew]) extends HTTPPathIntermediary(basePath) with DefaultBasicHTMLBuilder {

  var viewPool = scala.collection.mutable.Map[Session, LocalWebHTMLVIew]()

  // Init -> Compile View once, and be ready for replacements 
  var mainViewInstance = try {
    LocalWebHTMLVIewCompiler.createView(viewClass, true)
  } catch {
    case e : Throwable =>
      e.printStackTrace();
      viewClass.newInstance();
  }

  //-- View Replacement
  mainViewInstance.onWith("view.replace") {
    newClass: Class[_ <: LocalWebHTMLVIew] =>

      //-- Set new Class as base class
      this.viewClass = newClass

      //-- Replace all views in pool
      this.viewPool.keys.foreach {
        session =>
          var instance = viewClass.newInstance()
          instance.viewPath = basePath

          instance.on("refresh") {
            // Refresh must be propagated to the main view instance
            mainViewInstance.@->("refresh")
          }

          this.viewPool.update(session, instance)
      }

      //-- Send Update to active pools
      websocketPool.foreach {
        case (session, interface) =>

         // println(s"Sending WS Update")

          this.viewPool.get(session) match {
            case Some(view) =>
              var message = new UpdateHtml
              message.HTML = view.rerender.toString
              interface.writeSOAPPayload(message)
            case None =>
          }

      }
  }

  //-- View Refresh can be requested
  mainViewInstance.on("refresh") {

    //-- Send Update to active pools
    websocketPool.foreach {
      case (session, interface) =>

        println(s"Sending WS Update")
        var message = new UpdateHtml
        message.HTML = this.viewPool.get(session).get.rerender.toString
        interface.writeSOAPPayload(message)
    }
  }

  //-- Resources 
  //-----------------
  this <= new ResourcesIntermediary("/resources")

  // WebSocket
  //---------------------
  var websocketPool = scala.collection.mutable.Map[Session, WebsocketInterface]()
  this <= new WebsocketIntermediary {

    this.onDownMessage {
      req =>

        // TLog.setLevel(classOf[WebsocketProtocolhandler], TLog.Level.FULL)

        if (req.upped) {
          println(s"Websocket opened")
          var interface = new WebsocketInterface(req.networkContext.asInstanceOf[TCPNetworkContext])
          websocketPool.update(req.getSession, interface)

          req.networkContext.on("close") {
           
            websocketPool -= req.getSession
             println("Closing Websocket, remaning: "+websocketPool.size)
          }
          //-- Send ack 
          //println(s"Say Hello");
          var hb = new HeartBeat
          hb.time = System.currentTimeMillis()
          interface.writeSOAPPayload(hb)
          /*var soap = new SOAPMessage
          soap.body.content += new Ack*/

        }
    }
  }

  // Actions 
  //------------------
  this <= new HTTPPathIntermediary("/action") {

    //this.acceptDown { req => req.path.startsWith("/action/") }

    this.onDownMessage {
      req =>

        //-- Get View to call action on 
        //--------------------
        viewPool.get(req.getSession) match {
          case Some(view) =>

            // Extract path
            //--------------------
            //var actionId = req.path.stripPrefix("/action/")
            var actionPath = req.path.split("/").filter(_.length > 0).toList
            var actionId = actionPath.last
            var viewPath = actionPath.dropRight(1)
            println(s"Action Path:" + actionPath)

            //-- Search vies along path 
            var currentView = view
            viewPath.foreach {
              nextViewName =>
                println(s"Searching for view name $nextViewName in current")
                currentView.viewPlaces.get(nextViewName) match {
                  case Some((container, nextView)) => currentView = nextView
                  case None =>
                    throw new RuntimeException(s"Cannot find view named $nextViewName in current view, maybe the action path is wrong ")
                }
            }

            //-- Now call on current view

            //-- Get Action to call
            //---------------------
            currentView.actions.get(actionId) match {
              case Some((node, action)) =>

                println(s"Found Action to call")
                action(node)

                //-- Prepare Response
                var r = new HTTPResponse();

                //-- ReRender, but get only body
                if (node.attribute("reRender") != "") {
                  r.htmlContent = view.rerender.children.find(_.isInstanceOf[Body[_, _]]).get.asInstanceOf[HTMLNode[_, HTMLNode[_, _]]]
                }
                response(r, req)

              case None =>

                var r = new HTTPResponse();
                r.code = 503
                r.htmlContent = html {
                  head {

                  }
                  body {
                    textContent(s"Cannot Call Action with id $actionId on view ${view.hashCode()}, not registered")
                  }
                }
                response(r, req)
            }

          case None =>

            var r = new HTTPResponse();
            r.code = 503
            r.htmlContent = html {
              head {

              }
              body {
                textContent("Cannot Call Action if view has not been rendred for session")
              }
            }
            response(r, req)
        }
    }

  }
  //-- Min Handler 
  //-------------------

  this <= new HTTPIntermediary {

    this.acceptDown { r => !r.upped }

    //-- Global case 
    this.onDownMessage {

      req =>

        println(s"Got page request "+req.path)

        //-- Get View to call action on 
        //--------------------
        var view = viewPool.getOrElseUpdate(req.getSession, {

        //  println(s"New view instance")
          var instance = viewClass.newInstance()
          instance.viewPath = basePath
          instance.on("refresh") {
            // Refresh must be propagated to the main view instance
            mainViewInstance.@->("refresh")
          }
          instance
        })
        view.request = Some(req)

        println(s"rendering")
        var r = new HTTPResponse();
        try {
          var rendered = view.rerender
          println(s"Done Rendering")
          r.htmlContent = rendered
          response(r, req)
        } catch {
          case e: Throwable =>
            e.printStackTrace()
            req(e)
        }

      //-- Front View Error if none defined
      /*case req if (req.path == "/" && frontViewStack.size == 0) =>
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
        response(r, req)*/

    }
  }
}

/**
 * This Engine starts HTTP Support, and enables easily finding GUI views to be displayed on Web Browser
 */
object LocalWebEngine extends WSBEngine with DefaultBasicHTMLBuilder {

  //-- HTTP Connector
  var httpConnector = new HTTPConnector(8585)
  this.network.addConnector(httpConnector)

  //-- Start
  //this.lInit
  //this.lStart

  // Default Broker
  //----------------------
  def enableDebug = {
    TLog.setLevel(classOf[ResourcesIntermediary], TLog.Level.FULL)
    TLog.setLevel(classOf[WebsocketProtocolhandler], TLog.Level.FULL)

  }

  val topViewsIntermediary = new SessionIntermediary {

    //println(s"Received HTTP: "+req.path)

    // Default Request Handler 
    //----------------
    this <= new HTTPIntermediary {
      this.acceptDown { req => !req.upped }
      this.onDownMessage {
        req =>
          //println(s"Default Handler for ${req.path}")
         // println(s"Errors: ${req.errors.size}")
      }
    }

  }
  this.broker <= topViewsIntermediary

  // Views
  //-----------------
  def addViewHandler(path: String, cl: Class[_ <: LocalWebHTMLVIew]): SingleViewIntermediary = {
    topViewsIntermediary.prepend(new SingleViewIntermediary(path, cl)).asInstanceOf[SingleViewIntermediary]
  }

  // Management
  //--------------
  /*var frontViewStack = Stack[LocalWebHTMLVIew]()

  /**
   * Add the Front view on top of views stack
   *
   */
  def setFrontView(v: LocalWebHTMLVIew) = {
    frontViewStack.push(v)
  }

  /**
   * Replace Front View by this view
   * Also invalidate existing views in pool
   */
  def replaceFrontView(v: LocalWebHTMLVIew) = {

    frontViewStack.size match {
      case 0 => setFrontView(v)
      case _ =>

        //-- Update
        frontViewStack.update(0, v)

      //-- Invalidate Pool
      //viewPool.clear()
    }
  }*/

  // GUI 
  //--------
  var helperGUIMainVBox = new javafx.scene.layout.VBox();
  override def lStart = {
    super.lStart
    //-- Create GUI TO help
    //JavaFXRun.on
    JavaFXRun.onJavaFX {

      var hostServices = HostServicesFactory.getInstance(JavaFXRun.application)

      //-- Create Stage 
      var stage = new Stage
      stage.setWidth(1024)
      stage.setHeight(768)
      var scene = new Scene(helperGUIMainVBox, Color.WHITE);
      stage.setScene(scene)

      //-- Add Text With Link
      var link = new Hyperlink();
      link.setText(s"http://localhost:${LocalWebEngine.httpConnector.port}/")
      new JavaFXNodeDelegate[Hyperlink, JavaFXNodeDelegate[Hyperlink, _]](link).onClicked {
        x =>
          println(s"Clicked")
          hostServices.showDocument(link.getText)
      }
      helperGUIMainVBox.getChildren.add(link)

      stage.show()
      stage.setOnCloseRequest(new EventHandler[WindowEvent] {
        def handle(e: WindowEvent) = {
          //stage.
          LocalWebEngine.lStop

        }
      })

      println(s"Done UI")

    }
  }

}
