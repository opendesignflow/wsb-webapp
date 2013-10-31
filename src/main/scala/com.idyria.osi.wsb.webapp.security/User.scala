package com.idyria.osi.wsb.webapp.security

import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.{ xelement, ElementBuffer, xattribute, XList }

/**
 * Represents a User
 * This object can be simply saved in authenticated session parameter to be seen as authenticated user informations
 */
@xelement
class User extends ElementBuffer {

  /**
   * A Display Name for the user
   */
  @xattribute
  var name: XSDStringBuffer = null

  @xelement(name = "FederatedIdentity")
  var identities = XList { new FederatedIdentity }

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