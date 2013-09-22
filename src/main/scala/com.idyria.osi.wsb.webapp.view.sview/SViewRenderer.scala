/**
 *
 */
package com.idyria.osi.wsb.webapp.view.sview

import com.idyria.osi.wsb.webapp.view.ViewRenderer
import com.idyria.osi.wsb.webapp._
import com.idyria.osi.wsb.webapp.http.message._

import java.net.URL

/**
 * @author rleys
 *
 */
class SViewRenderer(var path: URL) extends ViewRenderer {

  def produce(application: WebApplication,request: HTTPRequest): String = {

    println(s"--> Our base file is: $path")

    // Create an SView Object based on base file
    //--------------------
    SView(path).render(application,request) 
     
  }

}