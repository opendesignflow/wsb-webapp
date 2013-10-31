
package com.idyria.osi.wsb.webapp.view.sview

/**
 *
 * The SVIew Class is used to produce a string view result based on a structured content that can contain dynamically resolved parts
 *
 * It supports a templating feature and so on
 *
 */
import com.idyria.osi.aib.core.compiler.EmbeddedCompiler
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.GregorianCalendar
import com.idyria.osi.wsb.webapp._
import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp.view.ViewRendererException

import com.idyria.osi.wsb.webapp.view._

class SView {

  /**
   * Defined is the SView has been fetched from an URL
   */
  var sourceURL: URL = null

  /**
   * The main content of the sview
   */
  var contentClosure: (SView, WebApplication, HTTPRequest) => Any = null

  /**
   * Execute closure on this SView to configure the view
   */
  def apply(cl: SView => Any) = {
    cl(this)
  }

  /**
   * Define closure to produce content on request
   */
  def content(cl: (SView, WebApplication, HTTPRequest) => Any) = contentClosure = cl

  // Current Request
  //--------------------------
  var application: WebApplication = null
  var request: HTTPRequest = null

  // Templating
  //------------------

  var tiles = Map[String, (SView, WebApplication, HTTPRequest) => Any]()

  /**
   * Define the base template to be first extracted for this view
   */
  var template: String = null

  /**
   * Define some content to be injected at a specific placeholder location
   */
  def inject(placeHolder: String)(content: (SView, WebApplication, HTTPRequest) => Any) = {

    this.tiles = this.tiles + (placeHolder -> content)
  }

  def tile(placeHolder: String): Any = {

    this.tiles.get(placeHolder) match {
      case Some(closure) => closure(this, application, request)
      case None          => ""
    }
  }

  // Sub View
  //---------------

  /**
   * Renders and returns the content of anther sview
   */
  def subview(path: String): String = {

    application.searchResource(path) match {

      case None                  => throw new RuntimeException(s"Could not locate subview from file or classloader: path")
      case Some(subviewLocation) => SView(subviewLocation).render(application, request)

    }

  }

  /**
   * Render Template or content
   *
   */
  def render(application: WebApplication, request: HTTPRequest): String = {

    this.application = application
    this.request = request

    // Resolve the template chain
    // -- For each parent template, copy the template tiles to this SView
    // -- The First template having content stops the chain, the contentClosure is copied to this SView and is rendered
    var currentTemplate = this.template
    while (currentTemplate != null) {

      // Search template
      //-----------
      application.searchResource(currentTemplate) match {

        case None => throw new RuntimeException(s"Could not locate template from file or classloader: $template")
        case Some(urlLocation) =>

          // Create SView
          var templateView = SView(urlLocation)

          // Copy Tiles
          templateView.tiles.foreach {
            case (id, content) => this.inject(id)(content)
          }

          // Copy Content Closure
          contentClosure = templateView.contentClosure

          // Next Template
          currentTemplate = templateView.template

      }

    }

    // Now Try to render
    //-------------
    contentClosure match {

      // No content closure defined locally or from a template
      case null =>
        throw new ViewRendererException(s"Could not render view $sourceURL because no content closure is present locally or found in a template")

      // Render
      case closure => closure(this, application, request).toString

    }

  }
}

class SViewCompilationResult(var view: SView) {

  /**
   * TimeStamp at which the View has been created
   */
  var timestamp = new GregorianCalendar().getTimeInMillis
}

object SView {

  // Create Compiler
  //-------------
  var compiler = new EmbeddedCompiler

  // Map To retain Last compilations
  //-----------
  var viewsMap = Map[URL, SViewCompilationResult]()

  /**
   * Creates an SView from an URL source
   */
  def apply(source: URL): SView = {

    // Read Content of file
    //---------
    var closureContent = scala.io.Source.fromInputStream(source.openStream).mkString

    // Compile as Closure
    //---------------
    var closure = s"""
v.apply { 
  view =>
     $closureContent
}
"""

    var sview = new SView
    sview.sourceURL = source

    compiler.bind("v", sview)

    //compiler.compile(new File(source.getFile))
    try {

      compiler.interpret(closure)

    } catch {
      case e: Throwable =>

        println(s"Compilation error in SView source file: @$source")
        throw new ViewRendererException(s"An error occured while preparing SView @$source: ${e.getMessage()}", e)
    }
    //
    /*  
    var fClosure = s"""
    
package compiledViews
import com.idyria.osi.wsb.webapp.view.sview.SView
    
class ${source.getFile.split('/').last.split('.').head} extends Function1[SView, Any] {
  def apply(view: SView ): Any = {
    $closureContent
  }
}
"""
    var of = new FileOutputStream("viewtestcompile.scala")
    of.write(fClosure.getBytes)
    of.flush
    of.close
    
    compiler.compile(new File("viewtestcompile.scala"))
    */
    sview
  }

  /**
   *
   * Creates an SView from the String source file.
   * @warning This source string is not a path to a file! It must contain valid code
   */
  def apply(source: String): SView = {
    null
  }

}
