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
package com.idyria.osi.wsb.webapp.view.sview

import scala.language.implicitConversions

import org.scalatest.FunSuite

import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer.convertStringToXSDStringBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.xelement
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.lib.html.bootstrap.Bootstrap3
import com.idyria.osi.wsb.webapp.lib.html.messages.MessagesBuilder
import com.idyria.osi.wsb.webapp.navigation.GroupTraitView
import com.idyria.osi.wsb.webapp.view.WebappHTMLBuilder

class HTMLWebappTest extends FunSuite
    with WebappHTMLBuilder with MessagesBuilder {

  var app = new WebApplication("/test")
  app.navigationConfig.views += new GroupTraitView()
  app.navigationConfig.views.last.name = "Test1"

  app.navigationConfig.views += new GroupTraitView()
  app.navigationConfig.views.last.name = "Test2"

  var application = app
  var request: HTTPRequest = new HTTPRequest("GET", "/test", "1.1")

  request.errors = new RuntimeException("Example error") :: request.errors
  request.errors = new RuntimeException("Example error") :: request.errors

  test("Produce HTML") {

    var html = this.html {

      head {

        title("Page title")

        link {
          attribute("rel" -> "stylesheet")

        }

        this.genericElt {

        }
        this.genericElt("tt")

      }

      body {

        h1("Test Title 2")

        /*currentNode ::: (Bootstrap3.topNavBar and {
          navbar ⇒

            navbar.header {
              a("Home", "/")  {
              
            }
            }

            navbar.menusFromNavigation(app)

        })*/

        // Form Test
        //------------------
        this.form {

          // Form Action
          //action(classOf[AuthenticationController])

          // Infos
          //---------------
          this.h2("Please Sign in")

          this.p {

            this.currentNode.textContent = """
	                 Please Enter your Credentials to authenticate
	                 """
          }

          // Errors
          //--------------
          errors()

          // Main Form
          //----------------
          inputText("username") {
            classes("form-control")
            attribute("placeHolder", "Enter your user name")
          }

          inputPassword("password") {
            classes("form-control")
            attribute("placeHolder", "Enter your password")
          }

          // Action
          //---------------
          formSubmit("Ok") {
            classes("btn btn-lg btn-primary btn-block")
          }

        }

      }

    }

    println("Result: " + html.toString())

  }

}