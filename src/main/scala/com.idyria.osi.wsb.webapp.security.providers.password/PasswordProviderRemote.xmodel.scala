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

import com.idyria.osi.wsb.lib.soap.ProtocolBuilder
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])
))
object PasswordProviderBuilder extends ProtocolBuilder {

  // Config
  //-----------
  parameter("scalaProducer.targetPackage", "com.idyria.osi.wsb.webapp.security.providers.password")
  namespace("p" -> "http://www.idyria.com/osi/wsb-wbapp/security.providers.password")

  // Data Structures
  //---------------------
  "p:User" is {

    "p:UserName" ofType "string"

    "p:Password" ofType "cdata"
  }

  "p:Users" is {

    importElement("p:User").setMultiple

    //"p:User" multipleOf ("p:User")
  }

  //-- Salt Storage
  "p:Salts" is {
    "p:Salt" multiple {

      ofType("cdata")

      attribute("for") and {
        """Referes to the username for which this salt has been used"""
      }

    }
  }

  // Remote
  //-----------------

  //-- Register
  //-----------------
  message("p:Register") {
    request {

      importElement("p:User")
    }

    response {

      "p:Code" enum ("SUCCESS", "FAILURE")

    }
  }

  //-- Login
  //------------------
  message("p:PasswordLogin") {

    request {

      importElement("p:User")

    }

    response {
      "p:Code" enum ("SUCCESS", "FAILURE")

      //-- Auth Token
      importElement("com.idyria.osi.wsb.webapp.security.AuthToken")

    }
  }

  //-- Update
  //----------------

}