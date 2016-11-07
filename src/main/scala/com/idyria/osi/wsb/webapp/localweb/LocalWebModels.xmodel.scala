package com.idyria.osi.wsb.webapp.localweb

import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer
import com.idyria.osi.ooxoo.model.ModelBuilder

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
object LocalWebModels extends ModelBuilder {

  // Message Flow
  //---------------------
  "Done" is {
    
  }
  
  // Messages
  //------------------
  "HeartBeat" is {
    attribute("time") ofType "long"
  }
  
  "UpdateHtml" is {
    "HTML" ofType "cdata"
  }
  
  "UpdateText" is {
    "Id" ofType "string"
    "Text" ofType "cdata"
  }
  
  //

  
  
}