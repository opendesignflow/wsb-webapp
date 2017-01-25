package com.idyria.osi.wsb.webapp.localweb

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URI
import java.util.prefs.Preferences

import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

import org.w3c.dom.html.HTMLElement

import com.idyria.osi.ooxoo.core.buffers.datatypes.IntegerBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.AbstractDataBuffer
import com.idyria.osi.tea.logging.TLogSource
import com.idyria.osi.vui.html.HTMLNode
import com.idyria.osi.vui.html.Head
import com.idyria.osi.vui.html.Html
import com.idyria.osi.vui.html.Script
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder._
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import java.util.prefs.PreferenceChangeListener
import java.util.prefs.PreferenceChangeEvent
import com.idyria.osi.vui.html.Input
import com.idyria.osi.vui.html.Textarea
import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer
import com.idyria.osi.ooxoo.core.buffers.datatypes.BooleanBuffer
import scala.collection.mutable.Stack

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

  def deleteFromTempBuffer(name: String) = {
    tempBuffer = tempBuffer - name
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

  def tempBufferRadio[VT <: Any: ClassTag](text: String)(valueNameAndObject: Tuple2[String, VT])(cl: => Any) = {

    // Get actual value
    var actualValue = getTempBufferValue[VT](valueNameAndObject._1)

    // Create Radio
    //-----------------
    var r = input {
      +@("type" -> "radio")
      +@("name" -> valueNameAndObject._1)

      // If actual Value is this one, preselect
      actualValue match {
        case Some(actual) if (actual.toString == valueNameAndObject._2.toString) =>
          +@("checked" -> "true")
        case _ =>
      }

      // Bind value to select current
      bindValue {
        str: String =>
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
  var actionName: Option[String] = None
  var actionData = Stack[(String, String)]()

  /**
   * Action name
   */
  def withActionName(str: String)(cl: => Unit) = {
    try {
      this.actionName = Some(str)
      cl
    } finally {
      this.actionName = None
    }
  }

  /**
   * (name,fetchJavaScript)
   */
  def withActionData(data: (String, String)*)(cl: => Unit) = {
    try {
      data.foreach { tuple => this.actionData.push(tuple) }
      cl
    } finally {
      data.foreach { tuple => this.actionData.pop }
    }
  }

  /**
   * Return empty array if necessary
   */
  def actionDataToJSArray = {
    /* this.actionData.size match {
      case 
    }*/
    this.actionData.map {
      // case (name,js) => s"""{'name': '$name','expr': '$js'}"""
      //case (name,js) => s"""'$name','$js'"""
      case (name, js) => s"""${name}:'$js'"""
    }.mkString("{", ",", "}")
  }

  def registerAction(id: String)(n: HTMLNode[HTMLElement, _])(actionCl: HTMLNode[HTMLElement, _] => Unit): Unit = {
    this.actions = this.actions + (id -> (n, actionCl))
  }

  def getActions = actions

  def getActionString(cl: => Unit, codeprefix: String = ""): String = {

    //-- Get Hash code
    val node = currentNode
    var code = this.actionName match {
      case Some(name) => name
      case None => codeprefix + "" + node.hashCode()
    }

    //-- Register 
    var v = this
    v.actions = v.actions + (code.toString -> (node, { node =>
      cl
    }))

    // println(s"Registered action $code on "+v.viewPath)
    code.toString

  }

  def createSpecialPath(specialType: String, code: String): String = {

    var viewsPath = this.currentView.getParentViews
    viewsPath.size match {
      case 1 =>
        s"/${viewsPath(0).asInstanceOf[LocalWebHTMLVIew].viewPath}/$specialType/$code".noDoubleSlash
      case other =>
        s"/${viewsPath(0).asInstanceOf[LocalWebHTMLVIew].viewPath}/$specialType/${viewsPath.drop(1).map(_.asInstanceOf[LocalWebHTMLVIew].viewPath).mkString("/")}/$specialType/$code".noDoubleSlash
    }

  }

  implicit def strToURI(str: String): URI = new URI(str)

  def onClickReload(cl: => Unit) = {
    reload
    onClick {
      cl
    }
  }

  def onNodeClick(node: Option[HTMLNode[HTMLElement, _]])(cl: => Unit): Unit = {

    //println(s"On Node click: "+node)

    node match {
      case Some(n) =>
        onNode(n) {
          onClick(cl)
        }
      case None =>
    }

  }

  def onNodeMousePressed(node: Option[HTMLNode[HTMLElement, _]])(cl: => Unit): Unit = {

    // println(s"On Node click: "+node)

    node match {
      case Some(n) =>
        onNode(n) {
          onMousePressed(cl)
        }
      case None =>
    }

  }

  def onNodeMouseReleased(node: Option[HTMLNode[HTMLElement, _]])(cl: => Unit): Unit = {

    // println(s"On Node click: "+node)

    node match {
      case Some(n) =>
        onNode(n) {
          onMouseReleased(cl)
        }
      case None =>
    }

  }

  /**
   * Called with true when mouse is downn, with false if up
   */
  def onNodeisPressed(node: Option[HTMLNode[HTMLElement, _]])(cl: PartialFunction[Boolean, Unit]): Unit = {

    node match {
      case Some(n) =>
        onNode(n) {
          onMousePressed {

            if (cl.isDefinedAt(true)) {
              cl(true)
            }

          }
          onMouseReleased {
            if (cl.isDefinedAt(false)) {
              cl(false)
            }
          }
        }
      case None =>
    }

  }

  def onMousePressed(cl: => Unit): Unit = {

    var actionCode = this.getActionString(cl, "mousepressed")

    //println(s"Mouse Pressed -> " + actionCode)

    +@("onmousedown" -> (s"localWeb.buttonClick(this,'${createSpecialPath("action", actionCode)}',noUpdate=true)").noDoubleSlash)
    +@("touchmove" -> (s"localWeb.buttonClick(this,'${createSpecialPath("action", actionCode)}',noUpdate=true)").noDoubleSlash)
    //touchstart
  }

  def onMouseReleased(cl: => Unit): Unit = {

    var actionCode = this.getActionString(cl, "mousereleased")

    // println(s"Mouse Released -> " + actionCode)

    +@("onmouseup" -> (s"localWeb.buttonClick(this,'${createSpecialPath("action", actionCode)}',noUpdate=true)").noDoubleSlash)
    +@("touchend" -> (s"localWeb.buttonClick(this,'${createSpecialPath("action", actionCode)}',noUpdate=true)").noDoubleSlash)
  }

  def onKeyTyped(cl: Char => Unit) = {

    var actionCode = this.getActionString({ cl('a') }, "onkeyup")
    +@("onkeyup" -> (s"localWeb.buttonClick(this,'${createSpecialPath("action", actionCode)}',noUpdate=true)").noDoubleSlash)
  }
  def onFilteredKeyTyped(filters: String*)(cl: Char => Unit) = {

    //-- Get Path
    var actionCode = this.getActionString({ cl('a') }, "onkeydown")

    //-- Get Data
    //var data = 

    +@("onkeydown" -> (s"localWeb.filteredKeyTyped(event,this,[${filters.map { f => s"function (e){return $f;}" }.mkString(",")}],'${createSpecialPath("action", actionCode)}',${actionDataToJSArray})").noDoubleSlash)
  }

  override def onClick(cl: => Unit): Unit = {

    var actionCode = this.getActionString(cl)
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

  def placeView[VT <: LocalWebHTMLVIew: TypeTag](vc: Class[VT], targetId: String)(implicit tag: ClassTag[VT]): LocalWebHTMLVIew = {

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
    placeView[VT](createdView, targetId)

  }

  /**
   * If view ready is true, just take the object as new view
   */
  def placeView[VT <: LocalWebHTMLVIew: TypeTag](v: VT, targetId: String, viewready: Boolean = false)(implicit tag: ClassTag[VT]): VT = {

    //println(s"${hashCode} Place view for: " + targetId + " -> " + viewPlaces.get(targetId).isDefined)

    //-- Recompile View Using  
    //------------------------------
    //-- Compile 
    var createdView = viewready match {
      case true => v
      case false =>
        try {
          LocalWebHTMLVIewCompiler.createView[VT](Some(v), v.getClass.asInstanceOf[Class[VT]], listen = false)
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

  val bindSupportedTypes = List(classOf[Boolean], classOf[Long], classOf[Int], classOf[Integer], classOf[Double], classOf[Number], classOf[String], classOf[AbstractDataBuffer[_]])

  def bindGetType[V](tag: ClassTag[V]) = {
    bindSupportedTypes.find { parentClass => parentClass.isAssignableFrom(tag.runtimeClass) }
  }

  /**
   * Support String, Number and OOXOO Datatypes Buffers
   */
  def bindValue[V](cl: V => Unit)(implicit tag: ClassTag[V]): Unit = {

    var eventName = currentNode match {
      case t: Textarea[_, _] => "onchange"
      case other => "onchange"
    }

    bindSupportedTypes.find { parentClass => parentClass.isAssignableFrom(tag.runtimeClass) } match {

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
        +@(eventName -> s"localWeb.bindValue(this,'${createSpecialPath("action", action)}')".noDoubleSlash)

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
        +@(eventName -> s"localWeb.bindValue(this,'${createSpecialPath("action", action)}')".noDoubleSlash)

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

            case Some("on") =>
              cl(true.asInstanceOf[V])
            case Some("off") =>
              cl(false.asInstanceOf[V])
            case Some(v) =>

              cl(v.toBoolean.asInstanceOf[V])
            case None =>
              sys.error("Cannot run bind value action if value parameter is not set")
          }

        }

        // bind to on change
        +@(eventName -> s"localWeb.bindValue(this,'${createSpecialPath("action", action)}')".noDoubleSlash)

      //-- No Match error
      case None =>
        sys.error("Bind value on supports input types: " + bindSupportedTypes)
    }

  }

  /**
   * BindValue with Buffers
   */
  def bindBufferValue(vb: IntegerBuffer): Unit = {

    +@("value" -> vb.toString())
    this.bindValue {
      v: Int =>
        vb.set(v)
    }

  }

  /**
   * BindValue with Buffers
   */
  def bindBufferValue(vb: XSDStringBuffer): Unit = {

    +@("value" -> vb.toString())
    this.bindValue {
      v: String =>
        vb.data = v

    }

  }

  def bindBufferValue(vb: BooleanBuffer): Unit = {

    vb.data.booleanValue() match {
      case true =>
        +@("checked" -> "true")
      case false =>
    }

    this.bindValue {
      v: Boolean =>
        vb.data = v

    }

  }

  /**
   * Use Preferences to save and recall value
   * This custom run closure is called once when binding is done
   */
  def bindToPreference[V](p: Preferences, key: String, default: V)(cl: V => Unit)(implicit tag: ClassTag[V]): Unit = {

    // Find the data type
    val dataType = bindGetType[V](tag)

    //-- Get Value for preference
    val valueFromPref = dataType match {
      case Some(baseClass) if (baseClass == classOf[Boolean]) =>

        bindValue {
          v: Boolean =>

            p.putBoolean(key, v)

            // Run closure
            cl(v.asInstanceOf[V])
        }

        //println(s"Got value from pref: ")
        val value = p.getBoolean(key, default.asInstanceOf[Boolean]).asInstanceOf[V]

        // Set to default value, used checked attribute for boolean components
        p.getBoolean(key, default.asInstanceOf[Boolean]).asInstanceOf[V] match {
          case true =>
            +@("checked" -> "")
            true
          case false =>
            false
        }

      case Some(baseClass) if (baseClass == classOf[Int]) =>

        bindValue {
          v: Int =>

            p.putInt(key, v)

            // Run closure
            cl(v.asInstanceOf[V])
        }

        p.getInt(key, default.asInstanceOf[Int]).asInstanceOf[V]

      case Some(baseClass) if (baseClass == classOf[Long]) =>

        bindValue {
          v: Long =>

            p.putLong(key, v)

            // Run closure
            cl(v.asInstanceOf[V])
        }

        p.getLong(key, default.asInstanceOf[Long]).asInstanceOf[V]

      case Some(baseClass) if (baseClass == classOf[Double]) =>

        bindValue {
          v: Double =>

            //println(s"Saving to pref: $key -> $v")
            // Save Pref
            p.putDouble(key, v)

            // Run closure
            cl(v.asInstanceOf[V])
        }

        p.getDouble(key, default.asInstanceOf[Double]).asInstanceOf[V]

      case Some(baseClass) if (baseClass == classOf[String]) =>

        bindValue {
          v: String =>

            //println(s"Saving to pref: $key -> $v")
            // Save Pref
            p.put(key, v)

            // Run closure
            cl(v.asInstanceOf[V])
        }

        p.get(key, default.asInstanceOf[String]).asInstanceOf[V]

      case _ => sys.error(s"Not Supported datatype: ${tag.runtimeClass}")
    }

    //-- Run custom closure at least once before begin
    cl(valueFromPref.asInstanceOf[V])

    //-- Set to default value if not set
    this.currentNode match {
      case t: Textarea[_, _] =>
        if (t.textContent == "") {
          textContent(valueFromPref.toString)
        }

      case other =>
        this.currentNode.attributeOption("value") match {
          case None =>
            +@("value" -> valueFromPref.toString)
          case _ =>
        }
    }

    // If no ID on element, set one based on preferences string key
    this.currentNode.attributeOption("id") match {
      case None =>
        id(key)
      case _ =>
    }

  }

  def bindSelectToPref(p: Preferences, key: String, values: List[(String, String)])(cl: String => Unit) = {

    // Get Value from preference with default being the first of provided values
    //--------
    var valueFromPrefs = p.keys().contains(key) match {
      case false if (values.size == 0) =>
        None
      case false if (values.size > 0) =>
        p.put(key, values(0)._1)
        Some(values(0)._1)
      case true =>

        var prefVal = p.get(key, "")

        // If value from Pref is not in list, remove key
        //-------------
        values.find {
          case (value, text) => value == prefVal
        } match {
          case Some(found) => Some(found._1)
          case None if (values.size > 0) =>
            p.put(key, values(0)._1)
            Some(values(0)._1)
          case None if (values.size == 0) =>
            p.remove(key)
            None
        }
    }

    // Create Select
    //----------
    select {
      +@("name" -> key)
      values.foreach {
        case (value, text) =>

          option(value) {
            // +@("value" -> value)
            textContent(text)

            // Mark selected if same value as one from preferences
            valueFromPrefs match {
              case Some(v) =>
                if (value == v) {
                  +@("selected" -> "true")
                }
              case None =>
            }

          }
      }

      bindValue {
        str: String =>
          p.put(key, str)
          cl(str)
      }

    }

    // Run custom closure at least once before begin to allow initalisations
    //--------
    valueFromPrefs match {
      case Some(value) =>
        cl(value)
      case None =>
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
    var code = this.getActionString {

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