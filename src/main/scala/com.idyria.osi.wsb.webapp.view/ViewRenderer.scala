package com.idyria.osi.wsb.webapp.view

import com.idyria.osi.wsb.webapp._
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
trait ViewRenderer {

  /**
   * Produce the view Result based on application and request
   */
  def produce(application: WebApplication, request: HTTPRequest): String

}

/**
 *
 * And Exception that happens during view Rendering
 */
class ViewRendererException(message: String, baseException: Throwable) extends Exception(message, baseException) {

  def this(message: String) = this(message, null)

  /*  def this(message:String,baseException:Throwable) = {
    
  }*/

}