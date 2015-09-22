/*
 * #%L
 * WSB Webapp
 * %%
 * Copyright (C) 2013 - 2014 OSI / Computer Architecture Group @ Uni. Heidelberg
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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