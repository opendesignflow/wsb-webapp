package com.idyria.osi.wsb.webapp.appserv

import com.idyria.osi.aib.appserv.AIBAppServ
import com.idyria.osi.wsb.webapp.http.connector.HTTPConnector
import com.idyria.osi.wsb.core.network.protocols.simple.SimpleMessageTCPConnector
import com.idyria.osi.wsb.lib.soap.WSAClientEngine
import com.idyria.osi.wsb.lib.discovery.DiscoveryConnector
import com.idyria.osi.wsb.webapp.mains.AppServer
import java.net.InetAddress
import com.idyria.osi.tea.logging.TLog

object WSBGlobalServ extends AIBAppServ with App {

  println(s"Welcome to Global serv")

  // Registering Stuff for AIBAPP applications
  //----------------------

  // Preparing WSBServer 
  //--------------------------
  var appServer = new AppServer()
  var httpConnector: HTTPConnector = appServer.addHTTPConnector("localhost", 8889)
  var wsa = new WSAClientEngine(appServer.engine)

  //-- Add SOAP Simple connector on 8890
  val soapConnector = new SimpleMessageTCPConnector
  soapConnector.port = 8890
  soapConnector.messageType = "soap"
  soapConnector.address = InetAddress.getByName(InetAddress.getLocalHost().getHostName()).getHostAddress()
  appServer.engine.network.addConnector(soapConnector)

  //-- Discovery
  /*var discoveryConnector = new DiscoveryConnector("QualityServer")
  appServer.engine.network.addConnector(discoveryConnector)*/

  // Application deploy/remove
  //-----------------------
  this.aib.registerClosure { event: DeployWebApp =>

    println(s"Deploying webapp: ${event.app.location}")
    this.appServer.addApplication(event.app.application)
    event.app.application.lStart

  }

  // Start 
  //-----------------
  override def start = {

    this.appServer.start()

    super.start
  }

  // Add a dummy application
  //-------------------
  applicationConfig.applications += "src/examples/appserv-simple/WEB-INF/src"

  // Start
  //----------------
  TLog.setLevel(classOf[HTTPConnector], TLog.Level.FULL)
  
  start
  applicationWrappers(0).init
  applicationWrappers(0).start

}