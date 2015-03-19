package com.idyria.osi.wsb.webapp.appserv

import java.net.InetAddress

import com.idyria.osi.aib.appserv.AIBApplication
import com.idyria.osi.aib.core.bus.aib
import com.idyria.osi.wsb.core.network.protocols.simple.SimpleMessageTCPConnector
import com.idyria.osi.wsb.lib.soap.WSAClientEngine
import com.idyria.osi.wsb.webapp.http.connector.HTTPConnector
import com.idyria.osi.wsb.webapp.mains.AppServer

class WSBServerApplication extends AIBApplication {

  // Components
  //------------------
  var appServer: AppServer = new AppServer()
  // var httpConnector: HTTPConnector = appServer.addHTTPConnector("localhost", 8889)

  def doInit {

    println(s"WSB Init")

    // Preparing WSBServer 
    //--------------------------
    var httpConnector: HTTPConnector = appServer.addHTTPConnector("localhost", 8889)

    /*var httpsConnector: HTTPSConnector = appServer.addHTTPSConnector("localhost", 443)
  httpsConnector.addKeyCertificatePair((new File("localhost.key.pk8"),new File("localhost.crt")))*/

    var wsa = new WSAClientEngine(appServer.engine)

    //-- Add SOAP Simple connector on 8890
    val soapConnector = new SimpleMessageTCPConnector
    soapConnector.port = 8890
    soapConnector.messageType = "soap"
    soapConnector.address = InetAddress.getByName(InetAddress.getLocalHost().getHostName()).getHostAddress()

    aib.registerClosure { event: DeployWebApp =>

      this.childApplications = this.childApplications :+ event.app

      println(s"Deploying webapp: ${event.app.location}")
      this.appServer.addApplication(event.app.application)
      event.app.application.lStart

      this.updated.set(true)

    }

    aib.registerClosure { event: RemoveWebApp =>

      println(s"Removing webapp: ${event.app.location}")
      event.app.application.lStop
      this.appServer.removeApplication(event.app.application)

      this.updated.set(true)

    }

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