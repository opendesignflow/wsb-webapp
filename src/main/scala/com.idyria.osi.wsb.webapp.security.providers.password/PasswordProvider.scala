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
package com.idyria.osi.wsb.webapp.security.providers.password

import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.ooxoo.core.buffers.structural.xelement
import com.idyria.osi.ooxoo.core.buffers.structural.XList
import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer
import com.idyria.osi.wsb.webapp.db.OOXOODatabase
import com.idyria.osi.tea.hash.HashUtils
import com.idyria.osi.tea.hash.Base64
import com.idyria.osi.wsb.webapp.security.AuthenticationProvider
import com.idyria.osi.wsb.webapp.view.Inject
import com.idyria.osi.wsb.webapp.security.AuthenticationDatas
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.ooxoo.core.buffers.datatypes.CDataBuffer
import com.idyria.osi.wsb.webapp.security.AuthToken
import com.idyria.osi.wsb.webapp.security.AuthenticationException
import com.idyria.osi.wsb.core.message.soap.SOAPMessagesHandler
import uni.hd.cag.utils.security.utils.RandomID
import com.idyria.osi.ooxoo.core.buffers.structural.AnyXList

/**
 *
 * Password Provider
 *
 */
class PasswordProvider extends AuthenticationProvider with SOAPMessagesHandler {

  // Required Parameters Setup
  //-----------------
  this.requiredParameters = this.requiredParameters + ("userName" -> "The User name for the user entry")
  this.requiredParameters = this.requiredParameters + ("password" -> "The password for userName, hased in SHA-256")

  // Protocols Registration
  //------------
  AnyXList(classOf[PasswordLoginRequest])
  AnyXList(classOf[PasswordLoginResponse])

  // Init
  //-----------

  @Inject("security.auth.password")
  var database: OOXOODatabase = _

  var authDatas = new Users

  var saltsDatas = new Salts

  /**
   * Prepare database : get authData Document
   */
  override def init = {

    // Open Users
    database.container("authDatas").document("users.xml", authDatas)

    // Open Salts
    database.container("authDatas").document("salts.xml", saltsDatas)

  }

  // Register
  //----------------------

  /**
   * Register a new userName and password
   *
   * - Password Storage:
   *
   * inputPassword : base64(sha256(password)) (provided as is)
   *
   * salt = sha256(randomID) (stored in salts map)
   *
   * password = sha256(inputPassword + salt)
   */
  def register(userName: String, password: String) = {

    // Search to verify userName does not already exist
    //---------------
    authDatas.users.find(user ⇒ user.userName.toString == userName) match {
      case Some(user) ⇒ throw new RuntimeException(s"Cannot register userName $userName which already exists")

      // We Can register
      //-----------------
      case None ⇒

        // Create Salt
        //-----------------
        var salt = SaltsSalt()
        salt.for_ = userName
        salt.data = PasswordProvider.sha256(RandomID.generateSmallBytes())
        saltsDatas.salts += salt
        database.container("authDatas").writeDocument("salts.xml", saltsDatas)

        // Record
        //----------------
        var user = new User()
        user.userName = userName
        user.password = PasswordProvider.sha256(password + salt.data)

        //println(s"Setting passwd: to $password + ${salt.data} -> ${user.password} ")

        authDatas.users += user

        // Save
        database.container("authDatas").writeDocument("users.xml", authDatas)

    }

  }

  // Update
  //  - First initiate update for a userName by authenticating and giving back an Update token
  //  - The application should decide how to deliver the token to the user
  //  - The update takes place by delivering AuthToken + UpdateToken + userName + password
  //----------------

  // Auth
  //-----------------------------------
  /**
   *
   * Authenticate user by binding to LDAP
   */
  def authenticate(datas: AuthenticationDatas, application: WebApplication, request: HTTPRequest): AuthToken = {

    (datas.getUserName, datas.getPassword) match {

      // Everything is there
      //-----------------
      case (Some(userName), Some(password)) ⇒

        // Search for user entry
        //-------------------
        authDatas.users.find(user ⇒ user.userName.toString == userName) match {

          // Try to authenticate
          //----------------
          case Some(user) ⇒

            // Retrieve salt
            //----------------
            var salt = saltsDatas.salts.find(s ⇒ s.for_.toString == userName) match {
              case Some(salt) ⇒ salt
              case None       ⇒ throw new AuthenticationException(s"Password will never match, contact administrator (database content error)")
            }

            // Prepare Compare passwords
            //----------------
            var inputComparePasswords = PasswordProvider.sha256(password + salt.data)

            // println(s"db passwd: is $password + ${salt.data} -> ${user.password} ")

            // compare passwords
            //----------------
            (inputComparePasswords == user.password.data) match {

              // Return results
              case true ⇒

                var result = new AuthToken()
                result.token = s"""$userName"""
                result

              // Password is wrong
              case false ⇒

                throw new AuthenticationException(s"Password does not match")

            }

          // User Unknown
          case None ⇒ throw new AuthenticationException(s"User $userName is unknown")
        }

      // Missing Stuff
      //-----------------
      case (None, None)           ⇒ throw new AuthenticationException("both User Name and Password are missing for this provider")
      case (Some(userName), None) ⇒ throw new AuthenticationException("A Password must be provided for this provider")
      case (None, Some(password)) ⇒ throw new AuthenticationException("A User Name must be provided for this provider")
    }

  }

  // Remote Access
  //----------------------
  this.on[PasswordLoginRequest] {
    (message, request) ⇒

      //-- Authenticate (failure on exception is handled by SOAP handler)
      var authToken = this.authenticate(AuthenticationDatas("userName" -> request.user.userName.toString, "password" -> request.user.password.toString), null, null)

      //-- Send Back
      var resp = new PasswordLoginResponse
      resp.code = new PasswordLoginResponseCode
      resp.code.selectSUCCESS

      resp.authToken = authToken

      response(resp)
  }

  this.on[RegisterRequest] {
    (message, request) ⇒

      //-- Register
      //---------------------
      this.register(request.user.userName, request.user.password)

      println("Registering")

      //-- Answer
      var resp = new RegisterResponse
      resp.code = new RegisterResponseCode
      resp.code.selectSUCCESS
      response(resp)

  }

}
object PasswordProvider {

  /**
   * Utility to hash things
   */
  def sha256(password: String): String = sha256(password.getBytes())

  /**
   * Utility to hash things
   */
  def sha256(bytes: Array[Byte]): String = {

    // hash
    var hashed = HashUtils.hashBytes(bytes, "SHA-256")

    // Base 64
    Base64.encodeBytes(hashed)

  }

}

