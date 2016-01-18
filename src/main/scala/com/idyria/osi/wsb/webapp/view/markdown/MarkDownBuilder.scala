package com.idyria.osi.wsb.webapp.view.markdown

import com.idyria.osi.wsb.webapp.view.WebappHTMLBuilder
import com.idyria.osi.vui.impl.html.components.HTMLNode
import org.markdown4j.Markdown4jProcessor


trait MarkdownBuilder extends WebappHTMLBuilder {
  
  var mdProcessor = new Markdown4jProcessor()
  
  def markdown(str:String) : HTMLNode[_] = {
    
    text(mdProcessor.process(str))
    
  }
  
}