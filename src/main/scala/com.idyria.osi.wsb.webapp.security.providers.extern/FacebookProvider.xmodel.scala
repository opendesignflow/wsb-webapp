package com.idyria.osi.wsb.webapp.security.providers.extern

import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer

@producers(Array(
    new producer(value=classOf[ScalaProducer]),
     new producer(value=classOf[MDProducer])
)) 
object FacebookeProviderModel extends ModelBuilder {
  
  
  "FacebookConfig" is {
    
    "OAuth" is {
      "AppID" ofType "string"
      "Secret" ofType "string"
    }
  }
}