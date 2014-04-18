package com.idyria.osi.wsb.webapp.security.providers

import com.idyria.osi.ooxoo.core.buffers.datatypes.IntegerBuffer
import com.idyria.osi.ooxoo.core.buffers.datatypes.IntegerBuffer.convertIntToIntegerBuffer
import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer
import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer.convertStringToXSDStringBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.xelement
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.security.AuthenticationProvider
import javax.naming.directory.DirContext
import java.util.Hashtable
import javax.naming.directory.InitialDirContext
import javax.naming.Context
import com.idyria.osi.wsb.webapp.security.AuthenticationDatas
import com.idyria.osi.wsb.webapp.security.AuthenticationException
import com.idyria.osi.wsb.webapp.security.AuthenticationException
import javax.naming.ldap.LdapContext
import com.idyria.osi.wsb.webapp.security.AuthToken
import com.idyria.osi.wsb.webapp.view.ViewRenderer
import com.idyria.osi.ooxoo.core.buffers.datatypes.StringMapBuffer

/**
 *
 * Password Provider
 *
 */
@xelement(name = "LDAPProvider")
class LDAPAuthProvider extends ElementBuffer with ViewRenderer with AuthenticationProvider {

  // Settings
  //----------------

  /**
   * The Server to contact
   */
  @xelement
  var server: XSDStringBuffer = "localhost"

  /**
   * The port to the server
   */
  @xelement
  var port: IntegerBuffer = 389

  /**
   *  Base DN where to search for users
   */
  @xelement
  var baseDN: XSDStringBuffer = null

  // Required Parameters Setup
  //-----------------
  this.requiredParameters = this.requiredParameters + ("username" -> "The User name for the LDAP entry, used as username input for binding to LDAP")
  this.requiredParameters = this.requiredParameters + ("password" -> "The password for username, used to Bind to LDAP")

  def produce(application: WebApplication, request: HTTPRequest): String = {
    ""
  }

  /**
   *
   * Authenticate user by binding to LDAP
   */
  def authenticate(datas: AuthenticationDatas, application: WebApplication, request: HTTPRequest) = {

    try {
      // Set up the environment for creating the initial context
      //-----------------------
      var env = new Hashtable[String, String]();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, s"ldap://$server:$port/$baseDN");

      // Authenticate as S. User and password "mysecret"
      env.put(Context.SECURITY_AUTHENTICATION, "simple");

      (datas.getUserName, datas.getPassword) match {

        // Everything there
        //-----------------
        case (Some(username), Some(password)) ⇒

          env.put(Context.SECURITY_PRINCIPAL, s"""uid=${username},$baseDN""");
          env.put(Context.SECURITY_CREDENTIALS, password);

          // Create Dir Content to bind
          //-----------------
          var ctx = new InitialDirContext(env);

          //-- Prepare Results
          var result = new AuthToken()
          result.token = s"""uid=${datas.getUserName.get},$baseDN"""
          result.datas = new StringMapBuffer

          result.datas("username") = datas.getUserName.get

          // Look for user entry and fetch interesting datas
          //--------------
          ctx.lookup(s"""uid=${username}""") match {

            case ldapEntry: LdapContext ⇒

              var attributes = ldapEntry.getAttributes("")
              attributes.get("mail") match {
                case null          ⇒
                case mailAttribute ⇒ result.datas("email") = mailAttribute.get().toString()
              }
            //println("Found User LDAP Entry: " + ldapEntry.getA)

            case _ ⇒
          }
          // println("User Current Entry: " + res)

          // Return Result
          //----------
          result.datas("username") = datas.getUserName.get
          result

        // Missing Stuff
        //-----------------
        case (None, None)           ⇒ throw new AuthenticationException("both User Name and Password are missing for this provider")
        case (Some(username), None) ⇒ throw new AuthenticationException("A Password must be provided for this provider")
        case (None, Some(password)) ⇒ throw new AuthenticationException("A User Name must be provided for this provider")
      }

    } catch {
      case e: javax.naming.AuthenticationException ⇒ throw new AuthenticationException("Could not authenticate against LDAP, are credentials correct?")
      case e: Throwable                            ⇒ throw e
    }

  }

}