/**
 *
 */
package com.idyria.osi.wsb.webapp.view.sview

import com.idyria.osi.wsb.webapp.view.ViewRenderer

import java.net.URL

/**
 * @author rleys
 *
 */
class SViewRenderer(var path: URL) extends ViewRenderer {

  def produce: String = {

    println(s"--> Our base file is: $path")

    "hi"
  }

}