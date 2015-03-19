package com.idyria.osi.wsb.webapp.appserv

import com.idyria.osi.aib.appserv.AIBApplication
import java.io.File
import com.idyria.osi.wsb.webapp.MavenProjectWebApplication
import com.idyria.osi.wsb.webapp.SimpleFolderWebApplication
import com.idyria.osi.aib.core.bus.aib
import com.idyria.osi.wsb.webapp.view.WWWView
import com.idyria.osi.wsb.webapp.injection.Injector

abstract class AIBWebapp extends AIBApplication {

  var webinfFile: File = null
  var applicationBaseFile: File = null

  var application: SimpleFolderWebApplication = null

  /**
   * Search for webapp base
   */
  def doInit = {
    
   // Injector.clear
    
    WWWView.compiler = this.wrapper.runtimeCompiler.compiler

    // Search for WEB-INF as parent or 2 parents
    List(this.location.getParentFile, this.location.getParentFile.getParentFile).find(_.getName == "WEB-INF") match {
      case Some(webinfFile) =>
        this.webinfFile = webinfFile
        this.applicationBaseFile = webinfFile.getParentFile
      case None =>
        throw new RuntimeException(s"Cannot find WEB-INF folder as parent or super-parent of application location $this.location")
    }

    // Update compiler output
   // this.wrapper.runtimeCompiler.changeOutputPath(new File(this.webinfFile,"target"))
    //this.wrapper.runtimeCompiler.
    
    println(s"Webapp base: ${this.applicationBaseFile} ${this.name}")

    // Create a WSBWebapp application
    this.application = new SimpleFolderWebApplication(applicationBaseFile, s"${this.name}")
    this.application.classloader = this.classloader
    this.application.databaseBasePath = new File(this.webinfFile, "db")

  }

  /**
   * Launch application
   */
  def doStart = {

    println(s"Deploy Start")
    aib.<-!->(new DeployWebApp(this))

  }

  def doStop = {
    aib.<-!->(new RemoveWebApp(this))
  }

}

class DeployWebApp(val app: AIBWebapp)
class RemoveWebApp(val app: AIBWebapp)