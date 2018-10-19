/*-
 * #%L
 * WSB Webapp
 * %%
 * Copyright (C) 2013 - 2017 OpenDesignFlow.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package com.idyria.osi.wsb.webapp.security.kerberos

import javax.security.auth.login.LoginContext
import javax.security.auth.Subject
import javax.security.auth.kerberos.KerberosPrincipal
import javax.security.auth.login.Configuration
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.Callback
import javax.security.auth.callback.TextOutputCallback
import javax.security.auth.callback.PasswordCallback
import javax.security.auth.callback.NameCallback

object KerberosTry extends App {

  sys.props.put("java.security.auth.login.config", "gsseg_jaas.conf")
  
  var subject = new Subject()
  subject.getPrincipals.add(new KerberosPrincipal("zm4632"))
  subject.getPublicCredentials.add("abc")

  var c = Configuration.getConfiguration
  c.refresh()
  println(s"Config: "+c.getType)
  

  var lc = new LoginContext("kit", subject, new CallbackHandler {
    def handle(callbacks: Array[Callback]) = {
      
      callbacks.foreach {
        case cb : TextOutputCallback =>
          println(cb.getMessage)
        case cb : NameCallback => 
          
        case cb: PasswordCallback => 
          println(s"Enter Password")
          var pass = Console.readLine()
          cb.setPassword(pass.toCharArray())
      }
      
      
    }
  }, Configuration.getConfiguration)

  lc.login()
  
}
