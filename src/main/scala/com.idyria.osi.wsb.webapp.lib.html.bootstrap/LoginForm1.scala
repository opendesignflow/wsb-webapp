package com.idyria.osi.wsb.webapp.lib.html.bootstrap

import com.idyria.osi.wsb.webapp.view.WWWView
import com.idyria.osi.wsb.webapp.security.AuthenticationController
import com.idyria.osi.wsb.webapp.lib.html.messages.MessagesBuilder

/**
 * Login Form1 implementation
 */
class LoginForm1 extends WWWView with MessagesBuilder {

  override def render = {
    form {
      classes("form-signin")

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