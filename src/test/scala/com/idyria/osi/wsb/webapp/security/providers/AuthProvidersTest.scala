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
package com.idyria.osi.wsb.webapp.security.providers

import org.scalatest.FunSuite
import com.idyria.osi.wsb.webapp.security.AuthenticationDatas
import com.idyria.osi.wsb.webapp.security.AuthenticationException
import com.idyria.osi.wsb.webapp.TestWebApplication
import com.idyria.osi.wsb.webapp.security.AuthenticationController
import com.idyria.osi.wsb.webapp.security.providers.password.PasswordProvider

/**
 * @author rleys
 *
 */
class AuthProvidersTest extends FunSuite {

  test("LDAPProvider") {

    var provider = new LDAPAuthProvider

    provider.baseDN = "ou=people,dc=ziti,dc=uni-heidelberg,dc=de"
    provider.server = "ldap"

    // Auth Data
    //---------------
    var authDatas = new AuthenticationDatas()
    authDatas("username" -> "gitlab")
    authDatas("password" -> "gitlab")

    // Try Success
    //-------------
    var res = provider.authenticate(authDatas, null, null)

    println("Email: " + res.datas.get("email"))

    // Try Fail
    //-------------------
    intercept[AuthenticationException] {
      authDatas("password" -> "gitab")
      provider.authenticate(authDatas, null, null)
    }

  }

  test("Password Login") {

    // Init auth provider
    //--------------
    var app = new TestWebApplication

    var provider = new PasswordProvider

    app.addController(new AuthenticationController(provider))

    // Record a new user
    //---------------------
    provider.register("test", PasswordProvider.sha256("test"))

    // Auth Success
    //------------------
    var authDatas = new AuthenticationDatas()
    authDatas("username" -> "test")
    authDatas("password" -> PasswordProvider.sha256("test"))

    assert(provider.authenticate(authDatas, app, null) != null, "Positive authentication Result")

    // Auth Failed
    //----------------
    intercept[AuthenticationException] {
      authDatas("password" -> PasswordProvider.sha256("test2"))
      provider.authenticate(authDatas, app, null)

    }

  }

}