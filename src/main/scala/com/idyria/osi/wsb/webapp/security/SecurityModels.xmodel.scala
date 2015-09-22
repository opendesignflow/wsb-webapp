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

import com.idyria.osi.wsb.lib.soap.ProtocolBuilder
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
object SecurityModelsBuilder extends ProtocolBuilder {

  // Config
  //-----------
  //parameter("scalaProducer.targetPackage", "com.idyria.osi.wsb.webapp.security")
  namespace("s" -> "http://www.idyria.com/osi/wsb-wbapp/security")

  // Authentication Tokens
  //---------------------

  "s:AuthToken" is {

    "s:Token" ofType ("string") withDocumentation {
      """Unique Static Token always returned for a user, so that the authentication process can federate Users and Authentication Means"""
    }

    "s:Datas" ofType ("map") withDocumentation {

      """Provides some extra data entries to be used by the authenticator as extra informations for the user, like email etc... """
    }

    importElement("FederatedIdentity")
    // "s:FederatedIdentity" ofType("FederatedIdentity")

  }

  // Roles
  //---------------
  "s:SecurityConfig" is {
  
    "s:SecurityRole" multiple {
      attribute("roleId")
    }

    // Rights
    //--------------
    "s:SecurityRights" is {
      "s:ForRole" is {
        attribute("id")
        "Right" is {

        }
      }
    }

  }

  // Remote
  //-----------------

  //-- Update
  //----------------

}