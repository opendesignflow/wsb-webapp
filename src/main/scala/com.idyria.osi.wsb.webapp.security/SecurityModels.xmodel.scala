package com.idyria.osi.wsb.webapp.security

import com.idyria.osi.wsb.lib.soap.ProtocolBuilder
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])
))
object SecurityModelsBuilder extends ProtocolBuilder {

  // Config
  //-----------
  parameter("scalaProducer.targetPackage", "com.idyria.osi.wsb.webapp.security")
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

  }

  // Remote
  //-----------------

  //-- Update
  //----------------

}