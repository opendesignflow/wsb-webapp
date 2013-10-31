/**
 *
 */
package com.idyria.osi.wsb.webapp.security

import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest

/**
 *
 * A class summarising authentication datas for a provider to go through authentication
 */
class AuthenticationDatas {

  var datas = scala.collection.mutable.Map[String, String]()

  // Record datas 
  //----------------
  def apply(data: Tuple2[String, String]) = datas += data

  // Default Standard getters
  //-----------------
  def getUserName = datas.get("username")
  def getPassword = datas.get("password")

}
object AuthenticationDatas {

  def apply(params: Tuple2[String, String]*): AuthenticationDatas = {

    var aDatas = new AuthenticationDatas

    params.foreach(aDatas(_))

    aDatas

  }
}

class AuthenticationException(message: String) extends Exception(message) {

}

/**
 * @author rleys
 *
 */
trait AuthenticationProvider {

  /**
   * map to define required Parameters, and an object to describe the parameter
   * To be defined
   */
  var requiredParameters = Map[String, Object]()

  var optionalParameters = Map[String, Object]()

  /**
   * Can be overriden by the user to init the provider if necessary
   */
  def init() = {

  }

  /**
   * Return a string for the user that can be used to find back to user in application user list
   * This string MUST be stable everytime the user logs in
   * Throw an exception if not
   */
  def authenticate(datas: AuthenticationDatas, application: WebApplication, request: HTTPRequest): AuthToken

}

/**
 * The AuthenticationResult provides a token string that MUST be the same everytime the same user uses the same authentication provider
 *
 * It can also supply some extra generic parameters whose name are specified by #AuthenticationProvider, and can be used to initialise an internal user datas for example
 *
 */
/*class AuthToken(
    
    @xelement(name="Token")
    var token: String) extends ElementBuffer {

  var datas = Map[String, String]()

  def apply(data: (String, String)) = this.datas = this.datas + data

}*/ 