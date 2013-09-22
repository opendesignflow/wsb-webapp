package com.idyria.osi.wsb.webapp.view

import com.idyria.osi.wsb.webapp.http.message.HTTPRequest 
import com.idyria.osi.wsb.webapp._
trait ViewRenderer {
	
  /**
   * Produce the view Result based on application and request
   */
  def produce(application: WebApplication,request: HTTPRequest): String

}
 