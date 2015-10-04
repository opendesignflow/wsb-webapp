package com.idyria.osi.wsb.webapp.appserv

import java.net.InetAddress
import com.idyria.osi.aib.appserv.AIBApplication
import com.idyria.osi.aib.core.bus.aib
import com.idyria.osi.wsb.core.network.protocols.simple.SimpleMessageTCPConnector
import com.idyria.osi.wsb.lib.soap.WSAClientEngine
import com.idyria.osi.wsb.webapp.http.connector.HTTPConnector
import com.idyria.osi.wsb.webapp.mains.AppServer
import com.idyria.osi.aib.appserv.apps.GUIApplication
import com.idyria.osi.vui.core.VBuilder
import com.idyria.osi.vui.lib.gridbuilder.GridBuilder
import com.idyria.osi.wsb.core.network.connectors.AbstractConnector
import com.idyria.osi.wsb.core.network.connectors.tcp.TCPConnector

class WSBServerApplication extends AIBApplication {

  this.name = "WSB Web App Server"

  // Make sure Init get done at the beginning
  //-----------
  this.initialState = Some("init")

  // Components
  //------------------
  var appServer: AppServer = new AppServer()
  // var httpConnector: HTTPConnector = appServer.addHTTPConnector("localhost", 8889)

  // Applciation added 
  //---------------
  this.onMatch("child.added") {
    case app: AIBAbstractWebapp =>

      //println(s"Adding application to main WebServer: "+app.application)
      appServer.addApplication(app.application)

    case app: AIBApplication =>
    //println(s"Added child")
  }

  // GUI App 
  //-----------------
  var uiApp = new GUIApplication with VBuilder with GridBuilder {

    this.name = "WSB Server UI"

    val connectorsTable = table[AbstractConnector[_]] {
      t =>
        t.column("Type") {
          c =>
            c.content { connector => connector.getClass.getSimpleName }
        }
        t.column("Message Type") {
          c =>
            c.content { connector => connector.messageType }
        }
        t.column("Info") {
          c => 
            c.content { 
              case connector : HTTPConnector => s"http://${connector.address}:${connector.port}/"  
              case connector : TCPConnector => s"Port: ${connector.port}"
              case _ => ""
            }
        }
    }

    def createUI = {

      grid {

        "-" row {
          label("Info")
        }

        "Connector" row {

          connectorsTable(expandWidth)
        }

      }

    }
  }
  this.addChildApplication(uiApp)

  // Lifecycle
  //-------------------
  onInit {

    println(s"WSB Init")

    // Preparing WSBServer 
    //--------------------------
    var httpConnector: HTTPConnector = appServer.addHTTPConnector("localhost", 8889)
    
    uiApp.connectorsTable.add(httpConnector)
    
    /*var httpsConnector: HTTPSConnector = appServer.addHTTPSConnector("localhost", 443)
  httpsConnector.addKeyCertificatePair((new File("localhost.key.pk8"),new File("localhost.crt")))*/

    var wsa = new WSAClientEngine(appServer.engine)

    //-- Add SOAP Simple connector on 8890
    val soapConnector = new SimpleMessageTCPConnector
    soapConnector.port = 8890
    soapConnector.messageType = "soap"
    soapConnector.address = InetAddress.getByName(InetAddress.getLocalHost().getHostName()).getHostAddress()

    uiApp.connectorsTable.add(soapConnector)
    
  }

  onStart {
    println(s"WSB Start")

    appServer.start()

  }

  def doStart {

    println(s"WSB Start")
    this.appServer.start()

  }

  def doStop {

  }

  // Children applications
  //-----------------------

}