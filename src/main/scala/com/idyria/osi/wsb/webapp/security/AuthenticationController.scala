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
package com.idyria.osi.wsb.webapp.security

import com.idyria.osi.wsb.webapp.navigation.NavigationRule
import com.idyria.osi.wsb.webapp.navigation.controller.Controller
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.WebApplication
import javax.faces.bean.ManagedBean
import com.idyria.osi.wsb.webapp.injection.Injector
import com.idyria.osi.wsb.webapp.view.Inject

/**
 * The Authentication Controller tries to authentication against provider request for selected provider
 *
 * Provider Selection:
 *
 * - Default : First one configured when creating class
 * - request.parameters("provider") containing Provider Name (The Simple Class Name)
 *
 * Result:
 *
 * - Result is saved under session "authenticated" name
 */
/*@ManagedBean(name = "com.idyria.osi.wsb.webapp.security.authenticate")
class AuthenticationController(defaultProvider: AuthenticationProvider) extends Controller {

  /**
   * The application
   */
  @Inject("current")
  var application: WebApplication = null

  // Init
  //-------------
  Injector.inject(defaultProvider)
  Injector(defaultProvider)
  defaultProvider.init
  //addProvider(defaultProvider)

  // Providers configuration
  //-----------------

  /**
   * The List of configured Providers
   */
  var availableProviders = List[AuthenticationProvider](defaultProvider)

  /**
   * Add the provider to internal list:
   *  - Perform injection
   *  - call init
   *  - add
   */
  def addProvider(p: AuthenticationProvider) = {
    Injector.inject(p)
    Injector(defaultProvider)
    p.init
    availableProviders = availableProviders :+ p
  }

  /**
   *
   *
   *
   */
  def execute(application: WebApplication, request: HTTPRequest): String = {

    // Select Provider
    //------------
    var selectedProvider =  request.getURLParameter("provider") match {
      case Some(providerName) ⇒ availableProviders.find(_.getClass().getSimpleName() == providerName) match {

        //-- Provider Name provided and found
        case Some(provider) ⇒ provider

        //-- Provider Name provided and not found
        case None ⇒ throw new AuthenticationException(s"Whished Authentication provider ${providerName} has not been setup")
      }

      //-- Provider name not provided, use default
      case None ⇒ availableProviders.head
    }

    // Extract Parameters from request
    //-----------------
    var authDatas = new AuthenticationDatas
    selectedProvider.requiredParameters.foreach {
      case (name, description) ⇒ request.getURLParameter(name) match {

        //-- Provided, gather
        case Some(value) ⇒ authDatas(name -> value)

        //-- Required parameter not found
        case None ⇒ throw new AuthenticationException(s"Authentication provider ${selectedProvider} requires request parameter $name which has not bee supplied")
      }
    }

    // Authenticate
    //-------------------

    // Inject
    Injector.inject(selectedProvider)

    // Auth
    var authResult = selectedProvider.authenticate(authDatas, application, request)

    // Save Result to session
    //--------------
    var user = new User
    user.authTokens = user.authTokens :+ authResult
    authResult.datas.get("username") match {
      case Some(username) ⇒ user.name = username
      case None ⇒
    }

    request.getSession("authenticated" -> user)

    ""
  }

}*/