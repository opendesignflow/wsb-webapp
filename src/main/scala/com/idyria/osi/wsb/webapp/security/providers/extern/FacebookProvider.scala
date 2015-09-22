package com.idyria.osi.wsb.webapp.security.providers.extern

import com.idyria.osi.wsb.webapp.view.WebappHTMLBuilder
import com.idyria.osi.wsb.webapp.view.WWWView
import com.idyria.osi.wsb.webapp.injection.Injector
import com.idyria.osi.wsb.webapp.db.OOXOODatabase
import com.idyria.osi.wsb.webapp.view.Inject
import com.idyria.osi.wsb.webapp.security.AuthenticationProvider
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.security.AuthToken
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.security.AuthenticationDatas
import com.idyria.osi.wsb.webapp.security.FederatedIdentity
import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer

trait FacebookProviderComponents extends WebappHTMLBuilder {

  def facebookLoginButton = {

    element("fb:login-button") {
      attr("scope" -> "email", "onlogin" -> "fb_checkLoginState();")
    }

  }

  /**
   * load SDK
   *
   */
  override def body(cl: => Any) = {

    super.body {
      cl

      // Add at the end
      javaScript("/components/facebook/facebook-interface.js")
   
      script {
        text {
          s"""

// Init
  window.fbAsyncInit = function() {
    FB.init({
      appId      : '${FacebookProvider.config.oAuth.appID}',
      xfbml      : true,
      version    : 'v2.2'
    });
  };

  (function(d, s, id){
     var js, fjs = d.getElementsByTagName(s)[0];
     if (d.getElementById(id)) {return;}
     js = d.createElement(s); js.id = id;
     js.src = "//connect.facebook.net/en_US/sdk.js";
     fjs.parentNode.insertBefore(js, fjs);
   }(document, 'script', 'facebook-jssdk'));

    """
        }
      }
    }
  }

}

class FacebookProvider extends AuthenticationProvider {

  // Required Parameters Setup
  //-----------------
  this.requiredParameters = this.requiredParameters + ("id" -> "unique ID of Facebook authentication")
  this.requiredParameters = this.requiredParameters + ("email" -> "Email of the user to federate accounts")
  this.requiredParameters = this.requiredParameters + ("token" -> "Authentication token to be verified")

  def authenticate(datas: AuthenticationDatas, application: WebApplication, request: HTTPRequest): AuthToken = {

    // Take Tokens and reverify them
    //----------------------
    println(s"[FB] Authenticating for: " + datas.datas("email"))

    // If valid, create an AuthToken with Facebook Federated identity
    //---------------
    var authtoken = AuthToken()
    var identity = new FacebookFederatedIdentity
    identity.email = datas.datas("email")
    identity.accessToken = datas.datas("token")
    authtoken.federatedIdentity = identity
    authtoken.federatedIdentity.token = datas.datas("id")

    return authtoken

  }
}

class FacebookFederatedIdentity extends FederatedIdentity {
  this.providerId = classOf[FacebookProvider].getCanonicalName

  var email: XSDStringBuffer = ""
  var accessToken: XSDStringBuffer = ""
}

object FacebookProvider {
  WWWView.addCompileTrait(classOf[FacebookProviderComponents])
  WWWView.addCompileImport(classOf[FacebookProviderComponents].getPackage)

  // DB
  //-------------
  @Inject("main")
  var configDb: OOXOODatabase = null

  // Configs
  //---------------
  var config = FacebookConfig()

  def apply(): FacebookProvider = {

    Injector.inject(this)

    // Load Config
    //-------------------
    println(s"FB Db: " + configDb)
    configDb.container("facebook").document("facebook.xml", config) match {
      case Some(config) =>

        return new FacebookProvider
      case None => throw new RuntimeException("Cannot load facebook provider if no config has been provided")
    }

  }
}