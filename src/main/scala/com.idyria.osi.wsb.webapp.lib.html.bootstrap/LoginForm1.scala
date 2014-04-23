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