package com.idyria.osi.wsb.webapp.appserver

import com.idyria.osi.wsb.core.WSBEngine
import com.idyria.osi.wsb.webapp.http.connector.HTTPConnector

object WSBAppServer extends App {

  println(s"Welcome to WSB App Server")
  
  // Start A WSB Application server with Webapp enabled
  //--------------------
  
  //-- Create WSB Engine
  var engine = new WSBEngine()
  
  //-- ADD HTTP Connector
  var connector = HTTPConnector(8082)
  engine.network.addConnector(connector)
  
  
  // Add Management and configuration interfaces
  //-----------------------
  
  
  // Load Configuration
  //---------------------
  //var configLocation = args.fin(_ == "")
  
  // Gui
  //---------------
  
  
  
}