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