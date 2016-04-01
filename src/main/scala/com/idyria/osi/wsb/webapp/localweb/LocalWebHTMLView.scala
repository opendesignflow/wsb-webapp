package com.idyria.osi.wsb.webapp.localweb

import java.io.File
import org.w3c.dom.html.HTMLElement
import com.idyria.osi.vui.core.view.AView
import com.idyria.osi.vui.core.view.AViewCompiler
import com.idyria.osi.vui.html.HTMLNode
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest

 // with StandaloneHTMLUIBuilder
class LocalWebHTMLVIew extends AView[HTMLElement,HTMLNode[HTMLElement,HTMLNode[HTMLElement,_]]] with DefaultLocalWebHTMLBuilder {

 this.currentView = this
  
  override def clone = {
    getClass.newInstance()
  }
  
}

object LocalWebHTMLVIewCompiler extends AViewCompiler[HTMLElement,LocalWebHTMLVIew]  {
 
    
  /*val eout = new File("target/web-classes")
  eout.mkdirs()
  compiler.settings2.outputDirs.setSingleOutput(eout.getAbsolutePath)*/
  
  this.tempSourceFolder = new File("target/localweb-sources")
  this.outputClassesFolder = new File("target/localweb-classes")

  this.initCompiler
  
  
}