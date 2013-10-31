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

    "p:User" multipleOf ("p:User")
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

      "p:User" is {

      }

    }

    response {

      "p:Code" enum ("SUCCESS", "FAILURE")

    }
  }

  //-- Login
  //------------------
  message("p:PasswordLogin") {

    request {

      "p:User" is {

      }

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