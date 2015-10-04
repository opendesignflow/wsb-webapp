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