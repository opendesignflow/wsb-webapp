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
import java.io.StringWriter
import java.io.PrintWriter
import java.nio.ByteBuffer
import sun.rmi.transport.proxy.HttpOutputStream
import com.idyria.osi.tea.compile.ClassDomainSupport
import com.sun.xml.internal.ws.wsdl.parser.MexEntityResolver
import javax.swing.JFrame
import javax.swing.JTextPane
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JEditorPane
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder._
import com.idyria.osi.wsb.core.message.soap.Fault
import com.idyria.osi.wsb.core.message.soap.FaultCode_Value
import com.idyria.osi.ooxoo.lib.json.JsonIO
import com.idyria.osi.wsb.core.message.Message
import com.idyria.osi.ooxoo.core.buffers.structural.AnyXList

@xelement
class Ack extends ElementBuffer {

}

class SingleViewIntermediary(basePath: String, var viewClass: Class[_ <: LocalWebHTMLVIew]) extends HTTPPathIntermediary("/" + basePath) with DefaultBasicHTMLBuilder with ClassDomainSupport {

  AnyXList(classOf[Done])

  // View Pool
  //-------------------
  var viewPool = scala.collection.mutable.Map[Session, LocalWebHTMLVIew]()

  // Init -> Compile View once, and be ready for replacements 
  var mainViewInstance = try {
    LocalWebHTMLVIewCompiler.createView(None, viewClass, true)
  } catch {
    case e: Throwable =>
      e.printStackTrace();
      viewClass.newInstance();
  }

  def getMainViewInstance = this.mainViewInstance

  //-- View Replacement
  val replaceViewClosure: (LocalWebHTMLVIew => Unit) = {
    newInstance: LocalWebHTMLVIew =>

      logFine[SingleViewIntermediary]("Replacing main View")

      //-- Set new Class as base class
      this.mainViewInstance.@->("clean")
      this.mainViewInstance = newInstance
      this.viewClass = newInstance.getClass

      //-- Restore listening
      getMainViewInstance.onWith("view.replace")(replaceViewClosure)
      getMainViewInstance.on("refresh") {
        refreshClosure()
      }

      //-- Replace all views in pool
      this.viewPool.keys.foreach {
        session =>
          var instance = newInstance.getClass.newInstance
          instance.viewPath = basePath

          // Events Propagation
          //------------
          //instance.on("refresh") {
          // Refresh must be propagated to the main view instance
          //getMainViewInstance.@->("refresh")
          //}
          instance.onWith("soap.send") {
            payload: ElementBuffer =>

              websocketPool.get(session) match {
                case Some(interface) =>
                  interface.writeSOAPPayload(payload)
                  interface.catchNextDone
                case None =>
              }

          }
          instance.onWith("soap.broadcast") {
            payload: ElementBuffer =>

          }

          this.viewPool.update(session, instance)

      }
      // EOF Replace views

      //-- Trigger main refresh
      getMainViewInstance.@->("refresh")
  }

  def getViewForRequest(req: HTTPRequest) = {
    this.viewPool.get(req.getSession) match {
      case Some(view) =>
        view.request = Some(req)
        Some(view)
      case None => None
    }
  }

  //-- View Refresh can be requested
  val refreshClosure = { () =>

    //-- Send Update to active pools
    websocketPool.foreach {
      case (session, interface) =>

        logFine[SingleViewIntermediary](s"Sending WS Update")

        try {
          var newHtml = this.viewPool.get(session).get.rerender.toString

          logFine[SingleViewIntermediary](s"HTML Out: " + newHtml)

          var message = new UpdateHtml
          message.HTML = newHtml

          logFine[SingleViewIntermediary](s"HTML Out: " + message.HTML)

          interface.writeSOAPPayload(message)
        } catch {
          case e: Throwable =>
            e.printStackTrace()
        }

    }
  }

  //-- Set Listening on Main View
  getMainViewInstance.onWith[LocalWebHTMLVIew]("view.replace")(replaceViewClosure)
  getMainViewInstance.on("refresh") {
    refreshClosure()
  }

  //-- Refuse upped messages
  //---------------
  this.acceptDown { r => !r.upped }

  //-- Resources 
  //-----------------
  this <= new HTTPPathIntermediary("/resources") {

    this <= new ResourcesIntermediary("/")

    this.onDownMessage {
      req =>

        getViewForRequest(req) match {
          case Some(view) =>

            // Extract path: /viewId/resources/resource/resource/
            //--------------------
            //var actionId = req.path.stripPrefix("/action/")
            var resourcePath = req.path.split("/").filter(_.length > 0).toList

            // Take until "resources" path found
            var (viewPath, localResourcePath) = resourcePath.span { p => p != "resources" } match {
              case (vp, lp) if (lp.size == 0) => (lp, vp)
              case (vp, lp) => (vp, lp.drop(1))
            }

            // println(s"R Searching resource ${req.path} from view $viewPath , $localResourcePath")

            //-- Search vies along path 
            var currentView = view
            viewPath.foreach {
              nextViewName =>
                //println(s"R Searching for view name $nextViewName in current")
                currentView.viewPlaces.get(nextViewName) match {
                  case Some((container, nextView)) => currentView = nextView
                  case None =>
                    throw new RuntimeException(s"Cannot find view named $nextViewName in current view, maybe the action path is wrong ")
                }
            }

            //-- Make sure request is available
            currentView.request = view.request

            //-- Change classloader 
            Thread.currentThread().setContextClassLoader(currentView.getClassLoader)
            // println("new cl for resource intermediary is "+currentView.getClassLoader)
            //-- Let real resource intermediary go on
            req.path = localResourcePath.mkString("/", "/", "")

          case None =>

        }

    }
  }
  // this <= new ResourcesIntermediary("/resources")

  // WebSocket
  //---------------------

  //-- Listen to specific protocol messages for this view

  //-- Create Websocket starter
  var websocketPool = scala.collection.mutable.Map[Session, WebsocketInterface]()
  this <= new WebsocketIntermediary {

    this.onDownMessage {
      req =>

        // TLog.setLevel(classOf[WebsocketProtocolhandler], TLog.Level.FULL)

        if (req.upped) {
          logFine[SingleViewIntermediary](s"Websocket opened")
          var interface = new WebsocketInterface(req.networkContext.asInstanceOf[TCPNetworkContext])
          websocketPool.update(req.getSession, interface)

          req.networkContext.on("close") {

            websocketPool -= req.getSession
            logFine[SingleViewIntermediary](s"Closing Websocket with state: ${req.networkContext}, remaning: " + websocketPool.size)
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
        getViewForRequest(req) match {
          case Some(view) =>

            // Extract path: /action/viewId/actionid
            //--------------------
            //var actionId = req.path.stripPrefix("/action/")
            var actionPath = req.path.split("/").filter(_.length > 0).toList

            // Take until "action" path found
            var (viewPath, localActionPath) = actionPath.span { p => p != "action" } match {
              case (vp, lp) if (lp.size == 0) => (lp, vp)
              case (vp, lp) => (vp, lp.drop(1))
            }

            //-- Search vies along path 
            var currentView = view
            viewPath.foreach {
              nextViewName =>
                //println(s"R Searching for view name $nextViewName in current")
                currentView.viewPlaces.get(nextViewName) match {
                  case Some((container, nextView)) => currentView = nextView
                  case None =>
                    throw new RuntimeException(s"Cannot find view named $nextViewName in current view, maybe the action path is wrong ")
                }
            }

            //-- Make sure request is available
            currentView.request = view.request

            /*var actionId = actionPath.last
            var viewPath = actionPath.dropRight(1)
            logFine[SingleViewIntermediary](s"Action Path:" + actionPath)

            //-- Search vies along path 
            var currentView = view
            viewPath.foreach {
              nextViewName =>
                logFine[SingleViewIntermediary](s"Searching for view name $nextViewName in current")
                currentView.viewPlaces.get(nextViewName) match {
                  case Some((container, nextView)) => currentView = nextView
                  case None =>
                    logFine[SingleViewIntermediary](s"view not found: "+nextViewName)
                    throw new RuntimeException(s"Cannot find view named $nextViewName in current view, maybe the action path is wrong ")
                }
            }*/

            //-- Make sure request is available
            currentView.request = view.request

            //-- Action Id is the last
            var actionId = localActionPath.mkString;

            //-- Get Action to call
            //---------------------
            currentView.getActions.get(actionId) match {
              case Some((node, action)) =>

                logFine[SingleViewIntermediary](s"Found Action to call")

                try {
                  action(node)

                  //-- Prepare Response
                  var r = new HTTPResponse();

                  //-- ReRender, but get only body
                  if (node.attribute("reRender") != "") {
                    r.htmlContent = view.rerender.children.find(_.isInstanceOf[Body[_, _]]).get.asInstanceOf[HTMLNode[_, HTMLNode[_, _]]]
                  } else {
                    r.contentType = "text/plain"
                    r.content = ByteBuffer.wrap("OK".getBytes)
                  }
                  response(r, req)

                } catch {
                  case e: Throwable =>
                    e.printStackTrace()

                    var r = new HTTPResponse();
                    r.code = 400
                    req.getURLParameter("format") match {
                      case Some("json") =>

                        //-- Create
                        var fault = new Fault
                        fault.code.value = new FaultCode_Value
                        fault.code.value.selectReceiver

                        //-- Set Content
                        /*var sw = new StringWriter
                        e.printStackTrace(new PrintWriter(sw))*/
                        fault.reason.text = e.getLocalizedMessage

                        // Convert to String 
                        var res = JsonIO(fault, true)
                        println("ERRROOR RES: " + res)
                        r.contentType = "application/json"
                        r.content = ByteBuffer.wrap(res.getBytes)

                      case _ =>
                        r.htmlContent = html {
                          head {

                          }
                          body {
                            textContent(s"An error occurent during action processing")
                            var sw = new StringWriter
                            e.printStackTrace(new PrintWriter(sw))
                            pre(sw.toString()) {

                            }
                          }
                        }
                    }

                    response(r, req)

                }

              case None =>

                var r = new HTTPResponse();
                r.code = 400
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

  //-- Main Handler 
  //-------------------

  this.onDownMessage {
    r =>

      logFine[SingleViewIntermediary]("Message into: " + basePath)

  }

  this <= new HTTPIntermediary {

    this.acceptDown { r =>
      // println(s"Comparing: " + s"/$basePath".noDoubleSlash + " with " + r.path.toString)
      //!r.upped && r.path.toString.startsWith(s"/$basePath".noDoubleSlash)
      !r.upped && (r.path.toString == "/")

    }

    //-- Global case 
    this.onDownMessage {

      req =>

        logFine[SingleViewIntermediary](s"Got page request on $basePath , path is:" + req.path)

        //-- Get View to call action on 
        //--------------------
        var view = viewPool.getOrElseUpdate(req.getSession, {

          //  println(s"New view instance")
          var instance = viewClass.newInstance()
          instance.viewPath = basePath

          // Events
          //--------------
          instance.on("refresh") {
            // Refresh must be propagated to the main view instance
            getMainViewInstance.@->("refresh")
          }

          instance.onWith("soap.send") {
            payload: ElementBuffer =>

              websocketPool.get(req.getSession) match {
                case Some(interface) =>

                  interface.writeSOAPPayload(payload)
                  interface.catchNextDone

                case None =>
              }

          }
          instance.onWith("soap.broadcast") {
            payload: ElementBuffer =>

          }

          instance
        })
        view.request = Some(req)

        logFine[SingleViewIntermediary](s"rendering")

        var r = new HTTPResponse();
        try {
          var rendered = view.rerender

          logFine[SingleViewIntermediary](s"Done Rendering")

          r.htmlContent = rendered
          response(r, req)

          logFine[SingleViewIntermediary](s"Response Sent")
        } catch {
          case e: Throwable =>
            e.printStackTrace()
            req(e)
        }

    }
  }

  // Errors 
  //--------------------
  this <= new HTTPIntermediary {
    this.acceptDown { r => r.errors.size > 0 }

    this.onDownMessage { 
      req =>

      logFine[SingleViewIntermediary](s"Request has errors")
      try {
        var r = new HTTPResponse();
        r.code = 503

        r.htmlContent = html {
          head {
            
          }
          body {
            p {
              text("Some Errors have been detected")
            }
            req.errors.foreach {
              err =>
                h1("Error: " + err.getLocalizedMessage) {

                }
                var strOut = new StringWriter
                err.printStackTrace(new PrintWriter(strOut))
                pre(strOut.toString()) {

                }
            }
          }
        }

        response(r, req)
        
      } catch {
        case e: Throwable =>
          e.printStackTrace()
         
      }

    }
  }

  // 404 no Found
  //-------------
  this <= new HTTPIntermediary {
    this.acceptDown { r => !r.upped }

    this.onDownMessage { req =>

      logFine[SingleViewIntermediary](s"404 for $basePath")
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
      response(r, req)
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
    // TLog.setLevel(classOf[ResourcesIntermediary], TLog.Level.FULL)
    //TLog.setLevel(classOf[WebsocketProtocolhandler], TLog.Level.FULL)
    TLog.setLevel(classOf[SingleViewIntermediary], TLog.Level.FULL)

  }

  val topViewsIntermediary = new SessionIntermediary {

    //println(s"Received HTTP: "+req.path)

    this.onDownMessage {
      req =>
        req.getSession.validity.add(java.util.Calendar.MINUTE, 30)
    }
    this.onUpMessage[HTTPResponse] {
      m =>
      // println("Session INter,ediary got up response")
    }

    // Default Request Handler 
    //----------------
    /*this <= new HTTPIntermediary {
      this.acceptDown { req => !req.upped }
      this.onDownMessage {
        req =>
          req.getSession.validity.add(java.util.Calendar.MINUTE, 30)
        //println(s"Default Handler for ${req.path}")
        // println(s"Errors: ${req.errors.size}")
      }
    }*/

  }
  this.broker <= topViewsIntermediary

  // Views
  //-----------------
  def addViewHandler(path: String, cl: Class[_ <: LocalWebHTMLVIew]): SingleViewIntermediary = {

    var newIntermediary = new SingleViewIntermediary(path, cl)

    this.addViewHandler(newIntermediary).asInstanceOf[SingleViewIntermediary]

    /*val viewIntermediaries = LocalWebEngine.topViewsIntermediary.intermediaries.collect {
      case i if (classOf[SingleViewIntermediary].isInstance(i)) => i.asInstanceOf[SingleViewIntermediary]
    }
    viewIntermediaries.find {
      i => i.basePath.length()<=newIntermediary.basePath.length
    } match {
      case Some(intermediaryRightAfter) => 
        //println(s"Add Intermediary $path before")
        topViewsIntermediary.addIntermediaryBefore(intermediaryRightAfter, newIntermediary).asInstanceOf[SingleViewIntermediary]
      case None => 
       // topViewsIntermediary.prepend(newIntermediary).asInstanceOf[SingleViewIntermediary]
        topViewsIntermediary <= newIntermediary
        newIntermediary
    }
*/

  }

  def addViewHandler[IT <: HTTPPathIntermediary](newIntermediary: IT): IT = {

    var path = newIntermediary.basePath
    val viewIntermediaries = LocalWebEngine.topViewsIntermediary.intermediaries.collect {
      case i if (classOf[SingleViewIntermediary].isInstance(i)) => i.asInstanceOf[SingleViewIntermediary]
    }
    viewIntermediaries.find {
      i => i.basePath.length() <= newIntermediary.basePath.length
    } match {
      case Some(intermediaryRightAfter) =>
        //println(s"Add Intermediary $path before")
        topViewsIntermediary.addIntermediaryBefore(intermediaryRightAfter, newIntermediary).asInstanceOf[IT]
      case None =>
        // topViewsIntermediary.prepend(newIntermediary).asInstanceOf[SingleViewIntermediary]
        topViewsIntermediary <= newIntermediary
        newIntermediary
    }

  }

  def dispatchMessage(msg: Message) = {
    this.network.dispatch(msg)
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
  // var helperGUIMainVBox = new javafx.scene.layout.VBox();
  var uiFrame: Option[JFrame] = None
  override def lStart = {
    super.lStart

    //-- Create GUI TO help
    //JavaFXRun.on
    /* JavaFXRun.onJavaFX {

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

    }*/

    //-- Create Frame
    uiFrame match {
      case Some(frame) =>
        frame.setVisible(true)
      case None =>
        var f = new JFrame("LocalWeb")
        uiFrame = Some(f)
        f.setSize(800, 600)

        //-- Add text
        var tp = new JEditorPane
        tp.setContentType("text/html")
        tp.setText("""<html>
      <h1>Web API</h1>
      <p>
      Open your Web Browser and navigate to <a href="http://localhost:8585/">http://localhost:8585/</a>
      </p></html>
      """.trim)
        f.setContentPane(new JScrollPane(tp))
        f.setVisible(true)
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    }

  }

  override def lStop = {
    super.lStop

    //-- Close Frame
    uiFrame match {
      case Some(frame) =>
        frame.dispose()
      case None =>
    }
  }

}
