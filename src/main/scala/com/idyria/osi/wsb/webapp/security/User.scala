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

import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.{ xelement, ElementBuffer, xattribute, XList }

/**
 * Represents a User
 * This object can be simply saved in authenticated session parameter to be seen as authenticated user informations
 */
@xelement
trait User extends ElementBuffer {

  /**
   * A Display Name for the user
   */
  @xattribute
  var name: XSDStringBuffer = null

  @xelement(name = "FederatedIdentity")
  var identities = XList { new FederatedIdentity }
  
  /**
   * Roles
   */
  @xelement(name = "SecurityRole")
  var roles = XList { new SecurityConfigSecurityRole}

  var authTokens = List[AuthToken]()

}

/**
 * A Federated Identity Object can be set to a user to be able to link with other authentication
 * services like google, twitter, facebook etc...
 */
@xelement
class FederatedIdentity extends ElementBuffer {

  /**
   * The Provider ID represents the configured Authentication Provider
   */
  @xattribute
  var providerId: XSDStringBuffer = null

  /**
   * The Token is an identifier always returned when authenticating to provider for this application
   */
  @xelement(name = "Token")
  var token: XSDStringBuffer = null

}

object FederatedIdentity {
  def apply() = new FederatedIdentity
}

/**
 * A Role represents an authorisation for a user
 */
@xelement
class Role extends ElementBuffer {

  /**
   * A display name for this Role
   */
  @xattribute
  var name: XSDStringBuffer = null

  /**
   * A Formal id of the form: "this.is.a.role"  to identify this role
   */
  @xattribute
  var id: XSDStringBuffer = null
}