package com.idyria.osi.wsb.webapp

import java.io._

import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp.http.connector._

import com.idyria.osi.wsb.core._

import com.idyria.osi.vui.lib.gridbuilder._

import scala.io._

import java.io._
import java.nio._

import com.idyria.osi.wsb.webapp.view._

object Server extends App {

  println("[WebApp] Welcome to WSB WebApp")

  // Start Server
  //-------------------------
  
  //-- Create Engine
  var engine = new WSBEngine()
  var connector = HTTPConnector(57300)

  //-- Add Connector
  engine.network.addConnector(connector)

  // Look for application on Command line args
  //------------
  args.filter { arg => new File(arg).isDirectory }.foreach {

    filePath =>

      println(s"[WebApp] Application at: $filePath")
      
      //-- Site is at filePath/site
      //-- Application is a folderName.Application
      
      // Create ClassLoader for thsi folder
      //---------------
     // var applicationClassLoader 
  }
  
  // Start
  //----------
  engine.lInit
  engine.lStart
  

}