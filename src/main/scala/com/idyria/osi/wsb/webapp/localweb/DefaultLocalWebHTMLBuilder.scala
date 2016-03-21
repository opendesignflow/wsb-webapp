package com.idyria.osi.wsb.webapp.localweb

import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder
import com.idyria.osi.vui.html.Script
import com.idyria.osi.vui.html.Head
import org.w3c.dom.html.HTMLElement
import java.net.URI
import com.idyria.osi.vui.html.HTMLNode
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest

trait DefaultLocalWebHTMLBuilder extends DefaultBasicHTMLBuilder {

   var request : Option[HTTPRequest] = None
  var viewPath = ""
  
  override def head(cl: => Any) = {

    // Create if necessary 
    var node = currentNode.children.collectFirst {
      case e: Head[_, _] => e
    } match {
      case Some(header) => header
      case None => createHead
    }

    

    //-- Add Standalone Script
    //-- 1. Add First, or After JQuery
    node.children.collectFirst {
      case e: Script[_, _] if (e.src.toString().contains("jquery")) =>
        node.children.indexOf(e)
    } match {
      case Some(jqueryNodeIndex) =>
        switchToNode(node, {
          //println(s"FOUND JQUERY")

          var resScript = script(new URI(s"${viewPath}/resources/localweb/localweb.js")) {

          }

          //-- Move
          //node.sgChildren
          // node.sgChildren = node.children.take(jqueryNodeIndex) ::: List(resScript) ::: node.children.takeRight(node.children.size-jqueryNodeIndex).dropRight(1)
        })
      case None =>
        switchToNode(node, {

          var jqueryScript = script(new URI(s"${viewPath}/resources/localweb/jquery.min.js")) {

          }
          script(new URI(s"${viewPath}/resources/localweb/localweb.js")) {

          }
        })
    }

    // Run Closure 
    switchToNode(node, cl)
    
    //-- Return
    node.asInstanceOf[Head[HTMLElement, Head[HTMLElement, _]]]

  }

  // Clicky stuff
  //-----------------------

  var actions = Map[String, (HTMLNode[HTMLElement, _], HTMLNode[HTMLElement, _] => Unit)]()

  override def onClick(cl: => Unit) = {

    //-- Get Hash code
    val node = currentNode
    var code = node.hashCode()

    //-- Register action
    actions = actions + (code.toString -> (node, { node =>
      cl
    }))

    +@("onclick" -> s"localWeb.buttonClick('$code')")

  }

  // Views
  //----------------------
  var viewPlaces = Map[String, (HTMLNode[HTMLElement, _], LocalWebHTMLVIew)]()

  def reRender = {
    +@("reRender" -> "true")
  }
  
  def placeView(vc:Class[_ <: LocalWebHTMLVIew],targetId:String) = {
    
    //-- Compile
    LocalWebHTMLVIewCompiler.createView(vc)
  }
  
  def placeView(v: LocalWebHTMLVIew, targetId: String) = {
    
    //-- Recompile View Using 
    //------------------------------
    
    //currentNode.apply("reRender" -> "true")
    viewPlaces.get(targetId) match {

      //-- If no record for the placeHolder, create a default
      case None =>

        var viewPLDIV = div {
          id(targetId)
        }

        viewPlaces = viewPlaces + (targetId -> (viewPLDIV, null))

      //-- If A Record exists for the placeHolder, just update
      case Some((container, currentView)) =>
        viewPlaces = viewPlaces + (targetId -> (container, v.getClass.newInstance()))

    }

  }

  def viewPlaceHolder(targetId: String,cla:String)(default: => Unit) : HTMLNode[HTMLElement,_]  ={

    viewPlaces.get(targetId) match {
      //-- If no record for the placeHolder, create a default
      case None =>

        var viewPLDIV = div {
          id(targetId)
          classes(cla)
          default
        }

        viewPlaces = viewPlaces + (targetId -> (viewPLDIV, null))
        
        viewPLDIV
        
        //-- If A Record exists for the placeHolder, and view is null, use default
      case Some((container, null)) =>
        container.clearChildren
        container.detach
        switchToNode(container, default)
        
        add(container)
        
      //-- If A Record exists for the placeHolder, and view is not null, use it
      case Some((container, currentView)) =>
        container.clearChildren 
        container.detach
        switchToNode(container, {
          add(currentView.rerender)
        })
        
        add(container)
    }


  }
  
  def detachView(targetId:String) = {
    viewPlaces = viewPlaces - targetId
  }

}