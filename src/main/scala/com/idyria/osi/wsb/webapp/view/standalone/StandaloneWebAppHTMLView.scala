package com.idyria.osi.wsb.webapp.view.standalone


import java.io.File
import com.idyria.osi.vui.core.view.AViewCompiler
import com.idyria.osi.vui.core.view.AView
import com.idyria.osi.vui.html.standalone.StandaloneHTMLNode
import org.w3c.dom.html.HTMLElement

 // with StandaloneHTMLUIBuilder
class StandaloneWebAppHTMLView extends AView[HTMLElement,StandaloneHTMLNode[HTMLElement,StandaloneHTMLNode[HTMLElement,_]]]  {

}

object StandaloneWebAppHTMLViewCompiler extends AViewCompiler[HTMLElement,StandaloneWebAppHTMLView] {

  var eout = new File("target/classes")
  eout.mkdirs()
  compiler.settings2.outputDirs.setSingleOutput(eout.getAbsolutePath)

}