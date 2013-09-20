package com.idyria.osi.wsb.webapp.mains

import com.idyria.osi.wsb.core.WSBEngine

import com.idyria.osi.wsb.webapp.http.connector._

import com.idyria.osi.wsb.webapp._


/**
 *
 * The AppServer class can start applications and is used as embedded application
 */
class AppServer {

  // Create WSB Engine
  //------------------------
  var engine = new WSBEngine()
 
  
  def start() = {
    
	  engine.lInit
	  engine.lStart
    
  } 
   
  // Connectors
  //------------------------
   
  /**
   * Adds a new HTTP connector to the Engine
   * 
   */
  def addHTTPConnector(host: String,port:Int) = {
	  
	  var connector = HTTPConnector(port)
	  engine.network.addConnector(connector)
			  
  }
  
  // Application
  //----------------
  
  /**
   * Add an application as Broker tree candidate 
   * 
   */
  def addApplication(app: WebApplication) = {
    
    engine.broker <= app
  
  }
  
}

object AppServer extends App {

  println("Welcome to WSB Webapp App Server")

  println("This Web Server tries to start webapplications2")

}