
package com.idyria.osi.wsb.webapp.view.sview

/**
 *
 * The SVIew Class is used to produce a string view result based on a structured content that can contain dynamically resolved parts
 *
 * It supports a templating feature and so on
 *
 */
abstract class SView {

  /**
   * The main content of the sview
   */
  var content: Any = null

  // Templating
  //------------------

  /**
   * Define the base template to be first extracted for this view
   */
  def template(path: String) = {

  }

  /**
   * Define some content to be injected at a specific placeholder location
   */
  def inject(placeHolder: String)(content: Any) = {

  }
}

