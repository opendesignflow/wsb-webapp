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
import com.idyria.osi.tea.logging.TLogSource

/**
 *
 * Thsi checks if a User object under the authenticated session variable is present
 */
class RoleCheck extends IdentifiedRule with TLogSource {

  override def evaluate(application: WebApplication, request: HTTPRequest): Boolean = {

    super.evaluate(application, request) match {

      // Check the provided parameter role is present on user
      case true =>

        println(s"--> Rolecheck: "+this.specification.parameters.keys.mkString(","))
        this.specification.parameters.get("role") match {
          case Some(roleToCheck) =>

           
            // Take user and search
            var user = request.getSession[User]("user").get

             println(s"Checking role $roleToCheck to ${user.roles.map { sr =>sr.roleId.toString }}")
            
            user.roles.find { r => r.roleId.toString == roleToCheck.toString } != None

          case None =>
            logWarn("Cannot check role because not name Entry was define on tule parameters")
            false
        }

      case false =>
        false
    }
  }
}
