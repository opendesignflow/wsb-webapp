package com.idyria.osi.wsb.webapp.view.remote

import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.wsb.lib.soap.ProtocolBuilder
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer

/**
 * @author zm4632
 */
@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])
))
object PartProtocol extends ModelBuilder with ProtocolBuilder {
  
  message("PartReload") {
    
    request {
      
    }
    
    response {
      
      attribute("id")
      "Content" ofType("cdata")
      
    }
  }
  
}