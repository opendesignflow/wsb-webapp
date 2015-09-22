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

import java.io.File

/**
 *
 * Represents an application defined by the location of POM.xml
 *
 * The class will try to locate resources and classes depending on the content found in pom file folder.
 *  - If target/classes is available, it is a source of compiled classes
 *  - It src/main/webapp is available, it is the source of web application content
 *
 *  The addFilesSource method is overriden for this purpose to always add a possible resource path both as provided for ClassLoader resolution, and as relative to Determined Folder base directory for file system resolution
 */
class SimpleFolderWebApplication(var baseDir: File, basePathc: String) extends WebApplication(basePathc) {

  // Init File sources with Found stuff
  //-----------------

  //-- src/main/webapp could be our base
  new File(baseDir, "src/main/webapp") match {
    case f if (f.exists) =>

      baseDir = f.getAbsoluteFile

    //  this.addFilesSource(f.getAbsolutePath)

    case _ =>
  }

  // Add Standard paths
  //---------------

  //-- base folder path of pom is a search path
  this.addFilesSource("")

  // Resources Default location
  //----------------------

  /**
   * The source path if modified to be always relative to base dir if it is not  absolute
   */
  override def addFilesSource(source: String) = {

    new File(source).isAbsolute() match {
      case true => super.addFilesSource(source)
      case false =>
        super.addFilesSource(source)
        super.addFilesSource(new File(baseDir, source).getPath())
    }

  }
  
  //-- Change database location
  this.databaseBasePath = new File(baseDir,"db")

}