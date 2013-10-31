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
@ManagedBean(name = "com.idyria.osi.wsb.webapp.security.authenticate")
class AuthenticationController(defaultProvider: AuthenticationProvider) extends Controller {

  /**
   * The application
   */
  @Inject("current")
  var application: WebApplication = null

  // Init
  //-------------
  Injector.inject(defaultProvider)
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
    var selectedProvider = availableProviders.head
    request.parameters.get("provider") match {
      case Some(providerName) ⇒ availableProviders.find(_.getClass().getSimpleName() == providerName) match {

        //-- Provider Name provided and found
        case Some(provider) ⇒ selectedProvider

        //-- Provider Name provided and not found
        case None           ⇒ throw new AuthenticationException(s"Whished Authentication provider $providerName has not been setup")
      }

      //-- Provider name not provided, use default
      case None ⇒
    }

    // Extract Parameters from request
    //-----------------
    var authDatas = new AuthenticationDatas
    selectedProvider.requiredParameters.foreach {
      case (name, description) ⇒ request.getURLParameter(name) match {

        //-- Provided, gather
        case Some(value) ⇒ authDatas(name -> value)

        //-- Required parameter not found
        case None        ⇒ throw new AuthenticationException(s"Authentication provider ${selectedProvider} requires request parameter $name which has not bee supplied")
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
    authResult.datas.get("username") match {
      case Some(username) ⇒ user.name = username
      case None           ⇒
    }

    request.getSession("authenticated" -> user)

    ""
  }

}