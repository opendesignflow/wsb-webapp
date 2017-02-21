package com.idyria.osi.wsb.webapp.security

import com.idyria.osi.wsb.webapp.navigation.controller.Controller
import javax.faces.bean.ManagedBean
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.view.Inject
import com.idyria.osi.wsb.webapp.injection.Injector

import com.idyria.osi.wsb.webapp.db.OOXOODatabase

/*
 * 
 * Flow:
 * 
 * - Authenticated
 *    - Ask for an identify for auth token
 *       - Return one -> Ok
 *       - None Returned -> register
 *       - Error allowed from identity allowed
 */
@ManagedBean(name = "IDController")
abstract class IdentityController {

  /**
   * The application
   */
  @Inject("current")
  var application: WebApplication = null

  @Inject("main")
  var database: OOXOODatabase = null

 // WWWView.addCompileTrait(classOf[IdentityInterfaceProvider])
  Injector.inject(this)
  Injector(this)

  // Roles and stuff
  //------------------------

  var securityConfig = database.container("security").documentFromClass[SecurityConfig]("config.xml")

  // Providers configuration
  //-----------------

  /**
   * The List of configured Providers
   */
  var availableProviders = List[AuthenticationProvider]()

  /**
   * Add the provider to internal list:
   *  - Perform injection
   *  - call init
   *  - add
   */
  def addProvider(p: AuthenticationProvider) = {
    Injector.inject(p)
    Injector(p)
    p.init
    availableProviders = availableProviders :+ p
  }

  // init
  var authController = new AuthenticationController
  //application.addController(authController)

  var fedController = new FederationController
 // application.addController(fedController)

  // Interface
  //-----------------

  var registerViewId = ""

  def loadIdentity(request: HTTPRequest, token: AuthToken): Option[User]

  /**
   * @return A view ID change if necessary
   */
  def createIdentity(request: HTTPRequest, token: AuthToken): String
  def federateIdentity(request: HTTPRequest, user: User, token: AuthToken): String

  // Auth Flow
  //------------------

  /**
   * implements flow
   *
   * @return String View ID to continue
   */
  protected def doAuthenticated(request: HTTPRequest, token: AuthToken): String = {

    println(s"in Do authenticated")

    /*if (token==null) {
      throw new RuntimeException("IdentificationFailed")
    }*/

    // If a user already exists 
    //  -> federate identities
    //  -> otherwise authenticate
    //-------------
    request.getSession.get[User]("user") match {
      case Some(user: User) =>

        // If no federated identity have the same provider as this one, just record
        // Otherwise, return an error if the identities are different
        user.identities.find(id => id.providerId.toString == token.federatedIdentity.providerId.toString()) match {
          case None =>

            user.identities += token.federatedIdentity

            ""

          case Some(identity) if (identity.token.toString() != token.federatedIdentity.token.toString()) =>

            throw new AuthenticationException(s"An Authentication occured for provider ${token.federatedIdentity.providerId}, but the current user has a different token for the same provider. Replacing new identity or it is an attemp to steal identity, we don't know")

          // Do nothing it is ok
          case Some(identity) =>

            ""
        }

      // Authenticate
      case None =>

        println(s"Try to load identify")

        // Notify implementation of authentication
        this.loadIdentity(request, token) match {

          // User loaded -> record to session
          case Some(user) =>

            println(s"Found loaded user")
            request.getSession.get("user" -> user)

            ""

          // No User loaded, maybe go to registration
          case None =>

            println(s"Switching to register view")
            request.getSession.get("token" -> token)
            registerViewId

        }

    }

  }

  @ManagedBean(name = "com.idyria.osi.wsb.webapp.security.identity.federate")
  class FederationController extends Controller {

    def execute(application: WebApplication, request: HTTPRequest): String = {

      try {
        request.getSession.get[User]("user") match {
          case Some(user) =>
            federateIdentity(request, user, request.getSession.get[AuthToken]("token").get)

          case None =>
            createIdentity(request, request.getSession.get[AuthToken]("token").get)
            doAuthenticated(request, request.getSession.get[AuthToken]("token").get)
        }

      } finally {
        // Remove auth token to reset state
        request.getSession.get.values = request.getSession.get.values - "token"
      }

    }
  }

  @ManagedBean(name = "com.idyria.osi.wsb.webapp.security.identity.authenticate")
  class AuthenticationController extends Controller {

    /**
     *
     *
     *
     */
    def execute(application: WebApplication, request: HTTPRequest): String = {

      // Select Provider
      //------------
      var selectedProvider = request.getURLParameter("provider") match {
        case Some(providerName) => availableProviders.find(_.getClass().getCanonicalName== providerName) match {

          //-- Provider Name provided and found
          case Some(provider) => provider

          //-- Provider Name provided and not found
          case None => throw new AuthenticationException(s"Whished Authentication provider ${providerName} has not been setup")
        }

        //-- Provider name not provided, use default
        case None => availableProviders.head
      }
      

      // Extract Parameters from request
      //-----------------
      //Injector.inject(selectedProvider)
      var authDatas = new AuthenticationDatas
     // try {
        selectedProvider.requiredParameters.foreach {
          case (name, description) => request.getURLParameter(name) match {

            //-- Provided, gather
            case Some(value) => 
            authDatas(name -> value)

            //-- Required parameter not found
            case None => throw new AuthenticationException(s"Authentication provider ${selectedProvider} requires request parameter $name which has not bee supplied")
          }
        }

        // Authenticate
        //-------------------

        // Inject
        Injector.inject(selectedProvider)

        // Auth
         selectedProvider.authenticate(authDatas, application, request) match {
          case null => ""
          case res => doAuthenticated(request, res)
        }

        // Save Result to session
        //--------------
        /*var user = new User
      user.authTokens = user.authTokens :+ authResult
      authResult.datas.get("username") match {
        case Some(username) => user.name = username
        case None =>
      }*/
        

     // } catch {
      //  case e: Throwable =>
     //     ""
     // }
      // request.getSession("authenticated" -> user)

    }

  }
  
  def reset(user:User) = {
    user.roles.clear 
    
    user.identities.foreach {
      identity => 
        this.availableProviders.foreach {
          case authProvider if(authProvider.getClass.getCanonicalName == identity.providerId.toString()) => 
            authProvider.forget(identity)
          case _ => 
        }
    }
    user.identities.clear
    
  }

}
/*
trait IdentityInterfaceProvider extends WebappHTMLBuilder {

  def authToken = request.getSession("token").asInstanceOf[Option[AuthToken]]

   @Inject("IDController")
  var identityControler: IdentityController = _
  
  def identityFederateAction = {
    action("com.idyria.osi.wsb.webapp.security.identity.federate")
  }

  def isIdentified[T <: User]: Option[T] = {
    request.getSession[T]("user")
  }
  
  /**
   * Clears the identities and roles of the user
   */
  def identityReset(user:User) = {
    identityControler.reset(user)
  }

}*/

object IdentityController {

}
