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

  def checkParameters(request: HTTPRequest)  : AuthenticationDatas = {
    
    var authDatas = new AuthenticationDatas
    requiredParameters.foreach {
      case (name, description) => request.getURLParameter(name) match {

        //-- Provided, gather
        case Some(value) => authDatas(name -> value)

        //-- Required parameter not found
        case None => throw new AuthenticationException(s"Authentication provider ${getClass.getCanonicalName} requires request parameter $name which has not bee supplied")
      }
    }
    
    authDatas
    
  }
  
  def forget(identity:FederatedIdentity) = {
    
  }
  
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