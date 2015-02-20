package com.idyria.osi.wsb.webapp.appserv

import java.net.InetAddress
import com.idyria.osi.aib.appserv.AIBAppServ
import com.idyria.osi.tea.logging.TLog
import com.idyria.osi.wsb.core.network.connectors.tcp.TCPConnector
import com.idyria.osi.wsb.core.network.protocols.simple.SimpleMessageTCPConnector
import com.idyria.osi.wsb.lib.soap.WSAClientEngine
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.connector.HTTPConnector
import com.idyria.osi.wsb.webapp.mains.AppServer
import com.idyria.osi.wsb.webapp.security.providers.extern.GoogleProvider
import com.idyria.osi.wsb.webapp.security.providers.extern.GoogleProviderComponents
import com.idyria.osi.wsb.webapp.view.WWWView
import com.idyria.osi.wsb.webapp.http.connector.HTTPProtocolHandler
import com.idyria.osi.wsb.webapp.http.connector.HTTPSConnector
import java.io.File
import com.idyria.osi.wsb.core.network.connectors.tcp.SSLTCPConnector
import com.idyria.osi.wsb.webapp.injection.Injector
import com.idyria.osi.aib.appserv.FolderWatcher

object WSBGlobalServ extends AIBAppServ with App {

  println(s"Welcome to Global serv")

  // Registering Stuff for AIBAPP applications
  //----------------------

  // Preparing WSBServer 
  //--------------------------
  var appServer = new AppServer()
  var httpConnector: HTTPConnector = appServer.addHTTPConnector("localhost", 8889)
  
  /*var httpsConnector: HTTPSConnector = appServer.addHTTPSConnector("localhost", 443)
  httpsConnector.addKeyCertificatePair((new File("localhost.key.pk8"),new File("localhost.crt")))*/
  
  var wsa = new WSAClientEngine(appServer.engine)

  //-- Add SOAP Simple connector on 8890
  val soapConnector = new SimpleMessageTCPConnector
  soapConnector.port = 8890
  soapConnector.messageType = "soap"
  soapConnector.address = InetAddress.getByName(InetAddress.getLocalHost().getHostName()).getHostAddress()
  //appServer.engine.network.addConnector(soapConnector)

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
  this.aib.registerClosure { event: RemoveWebApp =>

    println(s"Removing webapp: ${event.app.location}")
    event.app.application.lStop
    this.appServer.removeApplication(event.app.application)
    

  }

  // Start 
  //-----------------
  override def start = {

    this.appServer.start()

    super.start
  }

  // Add a dummy application
  //-------------------
  args.foreach {
    arg => println(s"Arg: $arg")
  }
  args.zipWithIndex.collect{case (arg,index) if(arg == "--application") => index}.foreach {
    index => 
        
        //applicationConfig.applications += args(index+1)
  }
  //applicationConfig.applications += "src/examples/appserv-simple/WEB-INF/src"

  // Added stuff
  //-------------
  WWWView.addCompileTrait(classOf[GoogleProviderComponents])
  
  // Start
  //----------------
 // TLog.setLevel(classOf[HTTPConnector], TLog.Level.FULL)
 // TLog.setLevel(classOf[TCPConnector], TLog.Level.FULL)
 /// TLog.setLevel(classOf[SSLTCPConnector], TLog.Level.FULL)
  //TLog.setLevel(classOf[HTTPProtocolHandler], TLog.Level.FULL)
  //TLog.setLevel(classOf[FolderWatcher], TLog.Level.FULL)
  
 /* TLog.setLevel(classOf[WebApplication], TLog.Level.FULL)
  TLog.setLevel(classOf[Injector], TLog.Level.FULL)*/
  
  start
  //applicationWrappers(0).init
  //applicationWrappers(0).start

}