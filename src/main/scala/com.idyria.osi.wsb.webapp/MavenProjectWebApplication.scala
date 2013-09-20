package com.idyria.osi.wsb.webapp

import java.io.File

/**
 *
 * Represents an application defined by the location of POM.xml
 *
 * The class will try to locate resources and classes depending on the content found in pom file folder.
 *  - If target/classes is available, it is a source of compiled classes
 *  - It src/main/webapp is available, it is the source of web application content
 */
class MavenProjectWebApplication(var pomFilePath: File, basePathc: String) extends WebApplication(basePathc) {

  // Init File sources with Found stuff
  //-----------------
  var baseDir = pomFilePath.getParentFile

  //-- src/main/webapp
  new File(baseDir, "src/main/webapp") match {
    case f if (f.exists) => this.addFilesSource(f.getAbsolutePath)
    case _               =>
  }

}