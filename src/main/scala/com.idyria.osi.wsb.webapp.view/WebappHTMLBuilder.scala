/*
 * #%L
 * WSB Webapp
 * %%
 * Copyright (C) 2013 - 2014 OSI / Computer Architecture Group @ Uni. Heidelberg
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.idyria.osi.wsb.webapp.view

import com.idyria.osi.vui.impl.html.HtmlTreeBuilder
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.WebApplication
import java.net.URL
import com.idyria.osi.wsb.webapp.navigation.controller.Controller
import com.idyria.osi.vui.impl.html.components.Form
import javax.faces.bean.ManagedBean
import com.idyria.osi.vui.impl.html.components.Head
import com.idyria.osi.vui.core.components.controls.VUIButton
import com.idyria.osi.vui.impl.html.components.FormSubmit
import com.idyria.osi.vui.impl.html.components.HTMLNode
import com.idyria.osi.vui.core.validation.ValidationTreeBuilderLanguage
import com.idyria.osi.vui.core.validation.ValidationSupport
import com.idyria.osi.wsb.webapp.ForException
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.vui.impl.html.components.Label
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.vui.impl.html.components.FormInput

trait WebappHTMLBuilder extends HtmlTreeBuilder with ValidationTreeBuilderLanguage {

  // Current Request
  //---------------------
  var application: WebApplication
  var request: HTTPRequest

  // Utils
  //-----------

  /**
   * Transforms relative URLS by appending the webapplication path
   */
  def cleanURL(url: String) = {

    url.matches("""([a-z/]|\+)+:.*""") match {

      //-- Absolute
      case true => url

      //-- Relative
      case false =>

        s"""${WebApplication.makePath(application.basePath, url)}"""
    }

    // If Path is a relative URL, prepend the application path to it
    /*try {
      new URL(url).getProtocol()

      // Don't modify URL, as there is a protocol
      // attribute("href" -> s"""${WebApplication.makePath(application.basePath, path)}""")
      url
    } catch {

      // Protocol is: '//' , keep url as is
      case e: Throwable if (url.startsWith("//")) ⇒

        url

      // No Protocol ->  prepend the application path to it
      case e: Throwable ⇒
        s"""${WebApplication.makePath(application.basePath, url)}"""

    }*/

  }

  // Base HTML
  //---------------

  /**
   * This overriden Head function adds some scripts/stylesheets connections for the base framework
   */
  override def head(cl: ⇒ Any) = {

    super.head {

      //-- JQuery
      //---------------
      script {
        attribute("src" -> "http://code.jquery.com/jquery-2.0.3.min.js")
        //attribute("src" -> "http://code.jquery.com/jquery-1.9.1.js")

      }

      //-- UI
      /*
      stylesheet("http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css")

      script {
        attribute("src" -> " http://code.jquery.com/ui/1.10.3/jquery-ui.js")
      }*/

      stylesheet("http://code.jquery.com/ui/1.9.2/themes/smoothness/jquery-ui.css")
      script {
        attribute("src" -> "http://code.jquery.com/ui/1.9.2/jquery-ui.min.js")
      }

      // Validation Stuff
      //---------------
      //  stylesheet("/css/validationEngine.jquery.css")

      //-- Define Application URL base path
      //---------------------
      script {
        text { s"""var basePath = "${application.basePath}"""" }
      }

      //-- Main entry script
      script {
        attribute("src" -> cleanURL("js/wsb-webapp.js"))
      }

      // User Content
      cl

    }

  }

  // Linking / Action
  //-------------------
  override def a(name: String, dest: String) = super.a(name, cleanURL(dest))

  //-- Form
  //--------------

  //---- Class to Store closure to executes (react)
  //----------

  /**
   * An Action created in a form, which get executed as controller
   */
  class FormAction(var form: Form, n: String, var actionClosure: (WebApplication, HTTPRequest) ⇒ Any) extends Controller {

    this.name = n

    /**
     * Validate and execute closure
     */
    def execute(application: WebApplication, request: HTTPRequest): String = {

      // Validation
      //-------------------
      form.onSubNodesMatch {
        case vs: ValidationSupport if (vs.asInstanceOf[HTMLNode].attributes.contains("name")) ⇒

          var name = vs.asInstanceOf[HTMLNode].attributes.get("name").get

          try {
            vs.validate(request.getURLParameter(vs.asInstanceOf[HTMLNode].attributes.get("name").get))
          } catch {

            //-- In Error case, transform to a ForException for better error reporting
            case e: Throwable ⇒

              println(s"Validation failed for $name, with ${vs.validators.length} validators")

              throw new ForException(vs.asInstanceOf[HTMLNode].attributes.get("name").get, e)
          }

        case vs: ValidationSupport if (!vs.asInstanceOf[HTMLNode].attributes.contains("name")) ⇒

          throw new RuntimeException(s"Could not validate form element with validation support, because no name attributes is present")
        case _ ⇒
      }

      // Action
      //---------------
      actionClosure(application, request) match {
        case null ⇒ ""
        case res  ⇒ res.toString
      }

    }

  }

  /**
   * Try to change current Form Node and add an invisible input field
   */
  def action(beanName: String): Unit = {

    currentNode match {
      case n: Form ⇒
        formParameter("action" -> beanName) {

        }
      case _ ⇒
    }

  }

  /**
   * Add an action for the provided name, which will get the current request path@$name string for execution
   *
   * An action is then also registered in application
   *
   */
  def react(name: String)(cl: (WebApplication, HTTPRequest) ⇒ Any): Unit = {

    currentNode match {
      case n: Form ⇒

        // Add Form parameter
        //-----------------------
        var actionPath = s"${request.path}@$name"
        formParameter("action" -> actionPath) {

        }

        // Register an action in application
        //-----------------
        application.controllers.get(actionPath) match {

          //-- Already a controller with incompatible type
          case Some(action) if (!action.isInstanceOf[FormAction]) ⇒ throw new RuntimeException(s"Action controller for form action: $actionPath could not be setup, because another controller with same path, but not carrying the FormAction type was found. This controller won't be replaced, you should solve the name conflict")

          //-- Replace action closure ofr existing form action
          case Some(action) ⇒

            action.asInstanceOf[FormAction].form = n
            action.asInstanceOf[FormAction].actionClosure = cl

          //-- Create
          case None ⇒ application.addController(new FormAction(n, actionPath, cl))

        }

      case _ ⇒
    }

  }

  /**
   * Calls #action(String) first trying to find action definition on Class
   * @see #action(String)
   */
  def action[AT <: Controller](bean: Class[AT]): Unit = {

    bean.getAnnotation(classOf[ManagedBean]) match {
      case null ⇒
      case annotation ⇒

        action(annotation.name())

    }

  }

  /**
   * Override the default submit button to avoid reloading of page and stay in AJAX
   *
   */
  override def submit(text: String)(cl: ⇒ Any): FormSubmit = {

    //-- Add submit
    var r = formSubmit(text) {
      cl
    }

    //-- Set type to button to avoid standard submit
    r("type" -> "button")

    //-- Add action
    r {
      b ⇒ b.onClick("submitForm(this)")
    }

    r

  }

  /**
   *
   * Labels for Inputs
   * Usage:
   *
   * inputText("name") {
   * 	label("Enter Name") {
   *  	}
   * }
   */
  def label(str: String)(cl: ⇒ Any): Label = {

    //-- Check current Node is a FormInput
    currentNode match {
      case input: FormInput if (input.attributes.contains("name")) ⇒

        // Create label
        var lbl = new Label
        lbl("for" -> input.getId)
        lbl.textContent = str

        // Switch
        switchToNode(lbl, cl)

        // Change parent
        currentNode.parent <= lbl

        // Swap with actual
        currentNode.parent.sgChildren = currentNode.parent.sgChildren.updated(currentNode.parent.sgChildren.size - 1, input).updated(currentNode.parent.sgChildren.size - 2, lbl)

        lbl
      case input: FormInput ⇒ throw new RuntimeException("Using label(String) for a form input without name attribute defined")
      case _                ⇒ throw new RuntimeException("Using label(String) method is only allowed direct under a form input subtree")
    }

  }

  // Views parts calls
  //--------------------

  // Styling
  //-----------------

  override def javaScript(path: String) = {
    super.javaScript(cleanURL(path))
  }

  override def stylesheet(path: String) = {

    link {
      attribute("rel" -> "stylesheet")

      attribute("href" -> cleanURL(path))

      /*
      
      // If Path is a relative URL, prepend the application path to it
      try {
        new URL(path).getProtocol()

        // Don't modify URL, as there is a protocol
        // attribute("href" -> s"""${WebApplication.makePath(application.basePath, path)}""")
        attribute("href" -> path)
      } catch {

        // Protocol is: '//' , keep url as is
        case e: Throwable if (path.startsWith("//")) ⇒

          attribute("href" -> path)

        // No Protocol ->  prepend the application path to it
        case e: Throwable ⇒
          attribute("href" -> s"""${WebApplication.makePath(application.basePath, path)}""")

      }
	*/
    }
  }

  //--------------------------
  // Parts Logic
  //-----------------------
  def reRender(str: String) = {
    attribute("reRender" -> str)
  }
}