package com.idyria.osi.wsb.webapp.localweb

import java.io.File

import org.w3c.dom.html.HTMLElement

import com.idyria.osi.vui.core.view.AView
import com.idyria.osi.vui.core.view.AViewCompiler
import com.idyria.osi.vui.html.HTMLNode

 // with StandaloneHTMLUIBuilder
class LocalWebHTMLVIew extends AView[HTMLElement,HTMLNode[HTMLElement,HTMLNode[HTMLElement,_]]]  {

}

object LocalWebHTMLVIewCompiler extends AViewCompiler[HTMLElement,LocalWebHTMLVIew] {

  var eout = new File("target/classes")
  eout.mkdirs()
  compiler.settings2.outputDirs.setSingleOutput(eout.getAbsolutePath)

}