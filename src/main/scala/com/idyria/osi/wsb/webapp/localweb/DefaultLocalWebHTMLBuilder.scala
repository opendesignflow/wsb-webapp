package com.idyria.osi.wsb.webapp.localweb

import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder
import com.idyria.osi.vui.html.Script
import com.idyria.osi.vui.html.Head
import org.w3c.dom.html.HTMLElement
import java.net.URI
import com.idyria.osi.vui.html.HTMLNode
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.vui.html.Html
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder._
import scala.language.implicitConversions

trait DefaultLocalWebHTMLBuilder extends DefaultBasicHTMLBuilder {

  
  var request: Option[HTTPRequest] = None
  var viewPath = ""
  var currentView: LocalWebHTMLVIew = null

  override def html(cl: => Any) = {

    // “<!DOCTYPE html>”.
    var htmlNode = new Html[HTMLElement, Html[HTMLElement, _]] {

      override def toString = {
        s"<!DOCTYPE html>\n${super.toString()}"
      }

    }

    switchToNode(htmlNode, cl)

    htmlNode
  }

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

          var resScript = script(new URI(s"${viewPath}/resources/localweb/localweb.js".replace("//", "/"))) {

          }

          //-- Move
          //node.sgChildren
          // node.sgChildren = node.children.take(jqueryNodeIndex) ::: List(resScript) ::: node.children.takeRight(node.children.size-jqueryNodeIndex).dropRight(1)
        })
      case None =>
        switchToNode(node, {

          var jqueryScript = script(new URI(s"${viewPath}/resources/localweb/jquery.min.js".replace("//", "/"))) {

          }
          script(new URI(s"${viewPath}/resources/localweb/localweb.js".replace("//", "/"))) {

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

    //-- Create Action String
    var currentViewName = "/"

    //
    //var cd = s"localWeb.buttonClick('${this.viewPath}/action/$code')".noDoubleSlash
    
    +@("onclick" -> (s"localWeb.buttonClick('${this.viewPath}/action/$code')").noDoubleSlash)

  }

  // Views
  //----------------------
  var viewPlaces = Map[String, (HTMLNode[HTMLElement, _], LocalWebHTMLVIew)]()

  def reRender = {
    +@("reRender" -> "true")
  }

  def placeView(vc: Class[_ <: LocalWebHTMLVIew], targetId: String): LocalWebHTMLVIew = {

    //-- Compile
    var createdView = LocalWebHTMLVIewCompiler.createView(vc)

    //-- Listen for reloading
    createdView.onWith("view.replace") {
      newClass: Class[_ <: LocalWebHTMLVIew] =>

        println(s"Repalcing placed view")
        //-- Replace view in container map
        var placedView = placeView(newClass.newInstance(), targetId)

        //-- trigger reload
        placedView.getTopParentView.@->("refresh")
    }
    placeView(createdView, targetId)

  }

  def placeView(v: LocalWebHTMLVIew, targetId: String): LocalWebHTMLVIew = {

    //-- Recompile View Using 
    //------------------------------

    //currentNode.apply("reRender" -> "true")
    viewPlaces.get(targetId) match {

      //-- If no record for the placeHolder, create a default
      case None =>

        var viewPLDIV = div {
          id(targetId)
        }

        var viewInstance = v
        viewInstance.parentView = Some(this.currentView)
        viewInstance.viewPath = this.viewPath + "/" + targetId

        viewPlaces = viewPlaces + (targetId -> (viewPLDIV, viewInstance))

        viewInstance
      //-- If A Record exists for the placeHolder, just update
      case Some((container, view)) =>

        var viewInstance = v
        viewInstance.parentView = Some(this.currentView)
        viewInstance.viewPath = this.viewPath + "/" + targetId

        viewPlaces = viewPlaces + (targetId -> (container, viewInstance))

        viewInstance
      //case Some((container, view)) => view 
    }

  }

  def viewPlaceHolder(targetId: String, cla: String)(default: => Unit): HTMLNode[HTMLElement, _] = {

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

  def detachView(targetId: String) = {
    viewPlaces = viewPlaces - targetId
  }

}