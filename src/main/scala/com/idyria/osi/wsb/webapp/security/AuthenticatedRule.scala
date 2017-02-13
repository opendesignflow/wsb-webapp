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

import com.idyria.osi.wsb.webapp.navigation._
import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp._
import com.idyria.osi.wsb.webapp.view.Inject
import com.idyria.osi.wsb.webapp.injection.Injector

/**
 *
 * Thsi checks if a User object under the authenticated session variable is present
 */
class AuthenticatedRule extends NavigationRule {

  def evaluate(application: WebApplication, request: HTTPRequest): Boolean = {

    request.getSession.get("token") match {
      case Some(authenticated) =>

        println("[Authenticated] Yes")
        true
      case None =>

        println("[Authenticated] No")
        false
    }

  }
}

class AuthenticateRule extends IdentifiedRule {

  @Inject("IDController")
  var controller: IdentityController = _

 

  override def evaluate(application: WebApplication, request: HTTPRequest): Boolean = {

     Injector.inject(this)
    
    super.evaluate(application, request) match {
      case true => true
      case false => 
         try {
           controller.authController.execute(application, request)
         } catch {
           case e : Throwable => 
         }
         
        super.evaluate(application, request)
    }
    
   
  }

}

class IdentifiedRule extends NavigationRule {

  def evaluate(application: WebApplication, request: HTTPRequest): Boolean = {

    request.getSession.get("user") match {
      case Some(authenticated) =>

        println("[Authenticated] Yes")
        true
      case None =>

        println("[Authenticated] No")
        false
    }

  }

}