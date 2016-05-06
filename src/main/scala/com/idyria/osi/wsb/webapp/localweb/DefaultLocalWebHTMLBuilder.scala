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
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import com.idyria.osi.ooxoo.core.buffers.structural.AbstractDataBuffer
import com.idyria.osi.ooxoo.core.buffers.datatypes.IntegerBuffer

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

  def getActions = actions

  def getActionString(cl: => Unit): String = {

    //-- Get Hash code
    val node = currentNode
    var code = node.hashCode()

    //-- Register 
    var v = this
    v.actions = v.actions + (code.toString -> (node, { node =>
      cl
    }))

    code.toString

  }

  override def onClick(cl: => Unit) = {

    var actionCode = this.getActionString(cl)

    //-- Create Action String
    var currentViewName = "/"

    //
    //var cd = s"localWeb.buttonClick('${this.viewPath}/action/$code')".noDoubleSlash

    +@("onclick" -> (s"localWeb.buttonClick(this,'/action/${this.viewPath}/$actionCode')").noDoubleSlash)

  }

  // Views
  //----------------------
  var viewPlaces = Map[String, (HTMLNode[HTMLElement, _], LocalWebHTMLVIew)]()

  def reRender = {
    +@("reRender" -> "true")
  }

  def placeView[VT <: LocalWebHTMLVIew: TypeTag](vc: Class[VT], targetId: String): LocalWebHTMLVIew = {

    //-- Compile
    var createdView = LocalWebHTMLVIewCompiler.createView[VT](None, vc)

    //-- Listen for reloading
    /*createdView.onWith("view.replace") {
      newClass: Class[VT] =>

        println(s"Repalcing placed view")
        //-- Replace view in container map
        var placedView = placeView(LocalWebHTMLVIewCompiler.newInstance(Some(createdView), newClass), targetId)

        //-- trigger reload
        placedView.getTopParentView.@->("refresh")
    }*/
    placeView(createdView, targetId)

  }

  /**
   * If view ready is true, just take the object as new view
   */
  def placeView[VT <: LocalWebHTMLVIew: TypeTag](v: VT, targetId: String, viewready: Boolean = false): VT = {

    //-- Recompile View Using  
    //------------------------------
    //-- Compile 
    var createdView = viewready match {
      case true => v
      case false => LocalWebHTMLVIewCompiler.createView[VT](Some(v), v.getClass.asInstanceOf[Class[VT]], true)
    }

    //-- Listen for reloading
    createdView.onWith("view.replace") {
      newClass: Class[VT] =>

        //println(s"Repalcing placed view")

        //-- Replace view in container map
        var placedView = placeView(LocalWebHTMLVIewCompiler.newInstance(Some(createdView), newClass), targetId, viewready = true)

        //println(s"View is now: "+viewPlaces(targetId))
        // println(s"Should be: "+placedView)

        //-- trigger reload
        placedView.getTopParentView.@->("refresh")
    }

    //currentNode.apply("reRender" -> "true")
    viewPlaces.get(targetId) match {

      //-- If no record for the placeHolder, create a default
      case None =>

        var viewPLDIV = div {
          id(targetId)
        }

        var viewInstance = createdView
        viewInstance.parentView = Some(this.currentView)
        viewInstance.viewPath = this.viewPath + "/" + targetId

        viewPlaces = viewPlaces + (targetId -> (viewPLDIV, viewInstance))

        viewInstance
      //-- If A Record exists for the placeHolder, just update
      case Some((container, view)) =>

        var viewInstance = createdView
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

        // println(s"Replacing container with $currentView")
        container.clearChildren
        container.detach
        switchToNode(container, {
          add(currentView.rerender)
        })

        // println(s"Container is noeew: "+container.toString())
        add(container)
    }

  }

  def detachView(targetId: String) = {
    viewPlaces = viewPlaces - targetId
  }

  // Autobinding
  //---------------------------

  /**
   * Support String, Number and OOXOO Datatypes Buffers
   */
  def bindValue[V](cl: V => Unit)(implicit tag: ClassTag[V]): Unit = {

    val supportedTypes = List(classOf[Boolean], classOf[Long], classOf[Int], classOf[Double], classOf[Number], classOf[String], classOf[AbstractDataBuffer[_]])

    supportedTypes.find { parentClass => parentClass.isAssignableFrom(tag.runtimeClass) } match {

      //-- Number
      //-------------------
      case Some(baseClass) if (baseClass == classOf[Number] || baseClass == classOf[Int] || baseClass == classOf[Long] || baseClass == classOf[Double]) =>

        // Ensure input type is number
        +@("type" -> "number")
        
        // Enable float if necessary
        if (baseClass == classOf[Double]) {
          +@("step" -> "any")
        }

        // Set name on element
        val targetNode = currentNode
        currentNode.attributes.get("name") match {
          case Some(name) => name
          case None =>
            +@("name" -> "value")
        }

        // Register Action
        var action = this.getActionString {

          // Check URL parameters
          currentView.getProxy[LocalWebHTMLVIew].get.request.get.getURLParameter(targetNode.attributes("name").toString) match {

            case Some(v) =>

              // Check type
              //---------------------
              tag.runtimeClass match {

                // Numbers
                //--------------------
                case c if (classOf[Long].isAssignableFrom(c)) =>

                  //cl(java.lang.Long.parseLong(v).toLong.asInstanceOf[V])
                  cl(v.toLong.asInstanceOf[V])

                case c if (classOf[Int].isAssignableFrom(c)) =>

                  cl(v.toInt.asInstanceOf[V])

                case c if (classOf[Double].isAssignableFrom(c)) =>

                  cl(v.toDouble.asInstanceOf[V])

                case c =>

                  sys.error(s"Data type " + c + " is not supported")
              }
            case None =>
              sys.error("Cannot run bind value action if value parameter is not set")
          }

        }

        // bind to on change
        +@("onchange" -> s"localWeb.bindValue(this,'/action/${this.viewPath}/$action')".noDoubleSlash)

      // String
      //------------------
      case Some(baseClass) if (baseClass == classOf[String]) =>

        // Set name on element
        val targetNode = currentNode
        currentNode.attributes.get("name") match {
          case Some(name) => name
          case None =>
            +@("name" -> "value")
        }

        // Register Action
        var action = this.getActionString {

          // Check URL parameters
          currentView.getProxy[LocalWebHTMLVIew].get.request.get.getURLParameter(targetNode.attributes("name").toString) match {

            case Some(v) =>

              cl(v.asInstanceOf[V])
            case None =>
              sys.error("Cannot run bind value action if value parameter is not set")
          }

        }

        // bind to on change
        +@("onchange" -> s"localWeb.bindValue(this,'/action/${this.viewPath}/$action')".noDoubleSlash)

      // Boolean
      //------------------
      case Some(baseClass) if (baseClass == classOf[Boolean]) =>

        // Make sure it is a checkbox
        +@("type" -> "checkbox")

        // Set name on element
        val targetNode = currentNode
        currentNode.attributes.get("name") match {
          case Some(name) => name
          case None =>
            +@("name" -> "value")
        }

        // Register Action
        var action = this.getActionString {

          // Check URL parameters
          currentView.getProxy[LocalWebHTMLVIew].get.request.get.getURLParameter(targetNode.attributes("name").toString) match {

            case Some(v) =>

              cl(v.toBoolean.asInstanceOf[V])
            case None =>
              sys.error("Cannot run bind value action if value parameter is not set")
          }

        }

        // bind to on change
        +@("onchange" -> s"localWeb.bindValue(this,'/action/${this.viewPath}/$action')".noDoubleSlash)

      //-- No Match error
      case None =>
        sys.error("Bind value on supports input types: " + supportedTypes)
    }

  }

  /**
   * BindValue with Buffers
   */
  def bindValue(vb: IntegerBuffer): Unit = {

    +@("value" -> vb.toString())
    this.bindValue { 
      v: Int =>
        vb.data = v
    }

  }

}