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
class MavenProjectWebApplication(var pomFilePath: File, basePathc: String) extends WebApplication(basePathc) {

  // Init File sources with Found stuff
  //-----------------
  var baseDir = pomFilePath.getParentFile

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

}