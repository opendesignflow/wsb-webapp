package com.idyria.osi.wsb.webapp.view.sview

import org.scalatest.FunSuite
import com.idyria.osi.vui.impl.html.HtmlTreeBuilder
import com.idyria.osi.wsb.webapp.lib.html.bootstrap.Bootstrap3
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.navigation.View
import com.idyria.osi.wsb.webapp.view.WebappHTMLBuilder
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.security.AuthenticationController
import com.idyria.osi.wsb.webapp.view.WWWView
import com.idyria.osi.vui.core.components.scenegraph.SGNode
import scala.language.implicitConversions
import com.idyria.osi.wsb.webapp.view.WebappHTMLBuilder
import com.idyria.osi.wsb.webapp.lib.html.messages.MessagesBuilder
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest

class HTMLWebappTest extends FunSuite
    with WebappHTMLBuilder with MessagesBuilder {

  var app = new WebApplication("/test")
  app.navigationConfig.views += new View()
  app.navigationConfig.views.last.name = "Test1"

  app.navigationConfig.views += new View()
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

        currentNode ::: (Bootstrap3.topNavBar and {
          navbar ⇒

            navbar.header {
              a("Home", "/")
            }

            navbar.menusFromNavigation(app)

        })

        // Form Test
        //------------------
        this.form {

          // Form Action
          action(classOf[AuthenticationController])

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