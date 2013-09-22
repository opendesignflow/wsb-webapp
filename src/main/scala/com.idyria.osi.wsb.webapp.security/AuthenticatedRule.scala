package com.idyria.osi.wsb.webapp.security

import com.idyria.osi.wsb.webapp.navigation._
import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp._

/**
 * 
 * Thsi checks if a User object under the authenticated session variable is present
 */
class AuthenticatedRule extends NavigationRule {
  
  def evaluate(application: WebApplication, request: HTTPRequest) : Boolean = {
     
    request.getSession("authenticated") match {
      case Some(authenticated) => 
        
        println("[Authenticated] Yes")
        true
      case None => 
        
        println("[Authenticated] No")
        false
    }
    
  }
}