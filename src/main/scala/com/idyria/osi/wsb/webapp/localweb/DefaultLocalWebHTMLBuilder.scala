package com.idyria.osi.wsb.webapp.localweb

import java.io.PrintWriter
import java.io.StringWriter
import java.net.URI

import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

import org.w3c.dom.html.HTMLElement

import com.idyria.osi.ooxoo.core.buffers.datatypes.IntegerBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.AbstractDataBuffer
import com.idyria.osi.vui.html.HTMLNode
import com.idyria.osi.vui.html.Head
import com.idyria.osi.vui.html.Html
import com.idyria.osi.vui.html.Script
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder._
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.tea.logging.TLogSource
import java.io.File

trait DefaultLocalWebHTMLBuilder extends DefaultBasicHTMLBuilder with TLogSource {

  var request: Option[HTTPRequest] = None

  var viewPath = ""

  var currentView: LocalWebHTMLVIew = null

  // Temp Buffer
  //------------------
  var tempBuffer = Map[String, Any]()

  /**
   * Value will be set to temp buffer map
   */
  def inputToBuffer[VT <: Any](name: String, value: String)(cl: => Any)(implicit tag: ClassTag[VT]) = {

    putToTempBuffer(name, value)

    var node = input {
      +@("value" -> value.toString)
      bindValue {
        v: VT =>
          v.toString match {
            case "" => tempBuffer = tempBuffer - name
            case _ => tempBuffer = tempBuffer.updated(name, v)
          }

      }
    }
    switchToNode(node, cl)
    node
  }
  def inputToBufferWithDefault[VT <: Any](name: String, default: VT)(cl: => Any)(implicit tag: ClassTag[VT]) = {

    //-- Set Default Value
    var actualValue = getTempBufferValue[VT](name) match {
      case None =>
        default
      case Some(v) =>
        v
    }

    //-- Create UI
    inputToBuffer[VT](name, actualValue.toString)(cl)
    /*var node = input {
      +@("value" -> actualValue.toString)
      bindValue {
        v: VT =>
          v.toString match {
            case "" => tempBuffer = tempBuffer - name
            case _ => tempBuffer = tempBuffer.updated(name, v)
          }

      }
    }
    switchToNode(node, cl)*/
    //node
  }

  /**
   * Assume Strig if class tag was not overriden
   */
  def getTempBufferValue[VT <: Any](name: String)(implicit tag: ClassTag[VT]): Option[VT] = this.tempBuffer.get(name) match {
    case None => None
    case Some(v) if (tag.runtimeClass.isInstance(v)) => Some(v.asInstanceOf[VT])
    case Some(v) =>
      throw new RuntimeException(s"Getting input buffer value for $name failed because requested type $tag does not match value's ${v.getClass()}")
  }

  def putToTempBuffer(name: String, v: Any) = {
    tempBuffer = tempBuffer.updated(name, v)
  }

  def tempBufferSelect(name: String, values: (String, String)*): com.idyria.osi.vui.html.Select[HTMLElement, com.idyria.osi.vui.html.Select[HTMLElement, _]] = tempBufferSelect(name, values.toList)
  def tempBufferSelect(name: String, values: List[(String, String)]): com.idyria.osi.vui.html.Select[HTMLElement, com.idyria.osi.vui.html.Select[HTMLElement, _]] = {

    //-- Set Default to First
    var selectedValue = this.getTempBufferValue[String](name) match {
      case None if (values.size > 0) =>
        this.putToTempBuffer(name, values.head._1)
        values.head._1
      // Reset default if necessary
      case Some(v) if (values.find { case (value, text) => value == v }.isEmpty) =>
        this.putToTempBuffer(name, values.head._1)
        values.head._1

      case Some(v) =>

        v
    }

    //-- Create GUI
    select {
      +@("name" -> name)
      values.foreach {
        case (value, text) =>
          option(value) {
            if (selectedValue == value) {
              +@("selected" -> "true")
            }
            textContent(text)
          }
      }

      bindValue {
        str: String =>
          putToTempBuffer(name, str)
      }
    }
  }

  def tempBufferRadio[VT <: Any : ClassTag](text:String)(valueNameAndObject: Tuple2[String,VT])(cl: => Any) = {
    
    // Get actual value
    var actualValue = getTempBufferValue[VT](valueNameAndObject._1)
    
    // Create Radio
    //-----------------
    var r = input {
      +@("type" -> "radio")
      +@("name" -> valueNameAndObject._1)
      
      // If actual Value is this one, preselect
      actualValue match {
        case Some(actual) if (actual.toString==valueNameAndObject._2.toString) => 
          +@("checked" -> "true")
        case _ => 
      }
      
      // Bind value to select current
      bindValue {
        str:String => 
          //str match {
           // case v if (v==valueNameAndObject._2.toString) => 
              putToTempBuffer(valueNameAndObject._1, valueNameAndObject._2)
            //case _ => 
          //}
      }
      
      textContent(text)
      
      // Extra stuff
      cl
    }
    
  }
  
  // Edit
  //-----------
  /*def onNode[NT <: HTMLNode[HTMLElement,_]](n:NT)(cl: => Any) : Any = {
    switchToNode(n, cl)
  }*/

  // Elements
  //-----------------

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

  def registerAction(id: String)(n: HTMLNode[HTMLElement, _])(actionCl: HTMLNode[HTMLElement, _] => Unit): Unit = {
    this.actions = this.actions + (id -> (n, actionCl))
  }

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

    // println(s"Registered action $code on "+v.viewPath)
    code.toString

  }

  def createSpecialPath(specialType: String, code: String) = {

    var viewsPath = this.currentView.getParentViews
    viewsPath.size match {
      case 1 =>
        s"/${viewsPath(0).asInstanceOf[LocalWebHTMLVIew].viewPath}/$specialType/$code".noDoubleSlash
      case other =>
        s"/${viewsPath(0).asInstanceOf[LocalWebHTMLVIew].viewPath}/$specialType/${viewsPath.drop(1).map(_.asInstanceOf[LocalWebHTMLVIew].viewPath).mkString("/")}/$specialType/$code".noDoubleSlash
    }

    /*
    var splitted = this.viewPath.noDoubleSlash.split("/").toList
    splitted.size match {
      case 0 => s"/${this.viewPath}/$specialType/$code".noDoubleSlash 
      case 1 => s"/${splitted(0)}/$specialType/$code".noDoubleSlash
      case 2 => s"/${this.viewPath}/$specialType/$code".noDoubleSlash
      case other => s"/${splitted(0)}/$specialType/${splitted.drop(1).mkString("/")}/$specialType/$code".noDoubleSlash
    }*/
  }

  def onClickReload(cl: => Unit) = {
    reload
    onClick {
      cl
    }
  }
  override def onClick(cl: => Unit) = {

    var actionCode = this.getActionString(cl)

    //-- Create Action String
    var currentViewName = "/"

    //
    //var cd = s"localWeb.buttonClick('${this.viewPath}/action/$code')".noDoubleSlash

    +@("onclick" -> (s"localWeb.buttonClick(this,'${createSpecialPath("action", actionCode)}')").noDoubleSlash)

  }

  // Views
  //----------------------
  var viewPlaces = Map[String, (HTMLNode[HTMLElement, _], LocalWebHTMLVIew)]()

  def reRender = {
    +@("reRender" -> "true")
  }
  def reload = {
    +@("reload" -> "true")
  }

  def valuedOnPlacedView[VT <: LocalWebHTMLVIew](pl: String, v: String)(implicit tag: ClassTag[VT]) = {
    viewPlaces.get(pl) match {
      case Some((node, view)) if (view != null && view.getClass.getCanonicalName.startsWith(tag.runtimeClass.getCanonicalName)) =>
        v
      case _ =>
        ""
    }
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

    println(s"${hashCode} Place view for: " + targetId + " -> " + viewPlaces.get(targetId).isDefined)

    //-- Recompile View Using  
    //------------------------------
    //-- Compile 
    var createdView = viewready match {
      case true => v
      case false =>
        try {
          LocalWebHTMLVIewCompiler.createView[VT](Some(v), v.getClass.asInstanceOf[Class[VT]], true)
        } catch {
          case e: Throwable =>
            e.printStackTrace()
            v
        }
    }

    //-- Listen for reloading
    createdView.onWith("view.replace") {
      newView: VT =>

        //println(s"Repalcing placed view")

        //-- Replace view in container map
        //var placedView = placeView(LocalWebHTMLVIewCompiler.newInstance(Some(createdView), newClass), targetId, viewready = true)
        var placedView = placeView(newView, targetId, viewready = true)

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
        viewInstance.viewPath = targetId

        viewPlaces = viewPlaces + (targetId -> (viewPLDIV, viewInstance))

        viewInstance
      //-- If A Record exists for the placeHolder, just update
      case Some((container, view)) =>

        // Update
        //---------
        var viewInstance = createdView
        viewInstance.parentView = Some(this.currentView)
        viewInstance.viewPath = targetId

        viewPlaces = viewPlaces + (targetId -> (container, viewInstance))

        //-- Clean
        if (view != null)
          view.@->("clean")

        //-- Set Content 
        viewPlaceHolder(targetId, "") {

        }

        viewInstance
      //case Some((container, view)) => view 

    }

  }

  def viewPlaceHolder(targetId: String, cla: String)(default: => Unit): HTMLNode[HTMLElement, _] = {

    logFine[DefaultLocalWebHTMLBuilder](s"${hashCode} Calling placeholder for: " + targetId + " -> " + viewPlaces.get(targetId).isDefined)

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
          try {
            var reRendered = currentView.rerender
            add(reRendered)
          } catch {
            case e: Throwable =>
              div {
                textContent(s"An error occurent during action processing")
                var sw = new StringWriter
                e.printStackTrace(new PrintWriter(sw))
                pre(sw.toString()) {

                }
              }
          }

        })

        // println(s"Container is noeew: "+container.toString())
        add(container)
    }

  }

  def detachView(targetId: String) = {
    viewPlaces.get(targetId) match {
      case Some((container, null)) =>
      case Some((container, currentView)) =>
        viewPlaces = viewPlaces + (targetId -> (container, null))
      case _ =>
        viewPlaces = viewPlaces - targetId
    }

  }

  // Autobinding
  //---------------------------

  /**
   * Support String, Number and OOXOO Datatypes Buffers
   */
  def bindValue[V](cl: V => Unit)(implicit tag: ClassTag[V]): Unit = {

    val supportedTypes = List(classOf[Boolean], classOf[Long], classOf[Int], classOf[Integer], classOf[Double], classOf[Number], classOf[String], classOf[AbstractDataBuffer[_]])

    supportedTypes.find { parentClass => parentClass.isAssignableFrom(tag.runtimeClass) } match {

      //-- Number
      //-------------------
      case Some(baseClass) if (baseClass == classOf[Number] || baseClass == classOf[Int] || baseClass == classOf[Integer] || baseClass == classOf[Long] || baseClass == classOf[Double]) =>

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

                case c if (classOf[Integer].isAssignableFrom(c)) =>

                  cl(Integer.parseInt(v).asInstanceOf[V])

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
        +@("onchange" -> s"localWeb.bindValue(this,'${createSpecialPath("action", action)}')".noDoubleSlash)

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
        +@("onchange" -> s"localWeb.bindValue(this,'${createSpecialPath("action", action)}')".noDoubleSlash)

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
        +@("onchange" -> s"localWeb.bindValue(this,'${createSpecialPath("action", action)}')".noDoubleSlash)

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
        vb.set(v)
    }

  }

  // File Drop Stuff
  //------------
  def onFileDrop(cl: File => Unit) = {


    /*var code = this.getActionString({
      () =>
        request.get.getURLParameter("file") match {
          case Some(f) =>
            cl(new File(f))
          case None =>
            throw new RuntimeException("Cannot call action related to file drop without file url argument")
        }
    })*/
    var code = this.getActionString{
    
        request.get.getURLParameter("file") match {
          case Some(f) =>
            cl(new File(f))
          case None =>
            throw new RuntimeException("Cannot call action related to file drop without file url argument")
        }
    }
    var path = createSpecialPath("action", code)

    +@("ondragover" -> "localWeb.allowDropFile(event)")
    +@("ondrop" -> s"localWeb.fileDrop(event,'$path')")

  }

}