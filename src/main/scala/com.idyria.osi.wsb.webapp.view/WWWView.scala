/*
 * #%L
 * WSB Webapp
 * %%
 * Copyright (C) 2013 - 2014 OSI / Computer Architecture Group @ Uni. Heidelberg
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.idyria.osi.wsb.webapp.view

import java.net.URL
import com.idyria.osi.aib.core.compiler.EmbeddedCompiler
import com.idyria.osi.vui.core.components.scenegraph.SGNode
import com.idyria.osi.vui.impl.html.HtmlTreeBuilder
import com.idyria.osi.vui.lib.view.View
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.view.sview.SView
import com.idyria.osi.wsb.webapp.view.sview.SViewCompilationResult
import scala.language.implicitConversions
import com.idyria.osi.vui.lib.placeholder.PlaceHolder
import com.idyria.osi.vui.core.styling.ApplyTrait
import com.idyria.osi.vui.impl.html.components.HTMLNode
import com.idyria.osi.aib.core.compiler.SourceCompiler
import com.idyria.osi.wsb.webapp.security.providers.extern.GoogleProviderComponents
import com.idyria.osi.aib.core.compiler.EmbeddedCompiler
import java.net.URLClassLoader
import com.idyria.osi.wsb.webapp.injection.Injector
import com.idyria.osi.tea.logging.TLogSource

/**
 *
 */
class WWWView extends ViewRenderer with WebappHTMLBuilder with PlaceHolder[HTMLNode] with ApplyTrait with TLogSource {

  type Self = WWWView

  // Current Request
  //--------------------------
  var application: WebApplication = null
  var request: HTTPRequest = null

  // Parts
  //---------------

  var parts = Map[String, WWWView]()

  def part(name: String)(cl: (WebApplication, HTTPRequest) => HTMLNode): WWWView = {

   
  
    //-- Create WWWView for part
    var p = new WWWView  {
      this.contentClosure = { v ⇒ cl(v.application, v.request) }
    }
    
     logFine[WWWView](s"[RW] Inside view: $hashCode , registering part $name with prt View ${p.hashCode}")
    
     //logFine[WWWView](s"Saving part: " + name+" -> "+p.hashCode+" -> "+p.contentClosure.hashCode())

    //-- Save
    parts = parts + (name -> p)

    this

  }

  /*/**
   * This method creates the part, renders it with current request, and place it into the result tree
   * The ID of resulting HTML node is set to part-$name
   */
  def placePart(name: String)(cl: (WebApplication, HTTPRequest) ⇒ HTMLNode): WWWView = {

    //- Create
    var view = part(name)(cl)

    //- Render
    var res = add(view.render(application, request))
    res("id" -> s"part-$name")
    view

  }*/

  /**
   * This method creates the part, renders it with current request, and place it into the result tree
   * The ID of resulting HTML node is set to part-$name
   */
  def placePart(name: String): HTMLNode = {

    logFine[WWWView](s"[RW] Inside view: $hashCode looking up part $name")
    
    //- Create
    var view = parts.get(name) match {
      case Some(partView) => partView
      case None => throw new RuntimeException(s"Cannot place part $name because it has not been defined")
    }

    //- Render
    var resNode = view.render(application, request)
    resNode("id" -> s"part-$name")
    add(resNode)
    resNode
    
   /* var resNode = view.render(application, request)
   logFine[WWWView](s"Rendered page part: $name with ${view.hashCode}//${this.hashCode}, top nodes: "+this.topNodes.size)
    logFine[WWWView](s"Res node: $resNode")
    view.topNodes.foreach {
      n =>  add(n)
    }
    add(resNode)
    //var res = add(view.render(application, request))
    resNode("id" -> s"part-$name")*/
    //view

  }

  // View  Composition
  //------------------

  var composedViews = List[WWWView]()
  
  /**
   * Shortcut to load a View File and create a view from it
   * The path is mapped to URL using application resource search
   */
  def compose(path: String): WWWView = {

    
    
    application.searchResource(path) match {
      case Some(url) ⇒
      
        var parentView = WWWView.compile(url).getClass.newInstance()
        parentView.application = this.application
        parentView.request = this.request
      
         logFine[WWWView](s"[RW] Inside view: $hashCode , composing with $path -> ${parentView.hashCode}")
        
       // parentView.contentClosure(parentView)
        
        composedViews = composedViews :+ parentView
        
        parentView
       /* this.contentClosure = parentView.contentClosure
        this.render
        this*/
      case None ⇒ throw new ViewRendererException(s"Could not render current view because searched view @$path could not be found ")
    }

  }

  def compose(baseClass: Class[_ <: WWWView]): WWWView = {

    // Instanciate
    //-------------
    var instance = baseClass.newInstance()

    // Set parameters
    //---------
    instance.application = this.application
    instance.request = this.request

    // Return
    //-----------
    instance
  }

  // Content/ Render
  //----------------
  var contentClosure: WWWView ⇒ HTMLNode = { v ⇒ null }

  /**
   * Record Content Closure
   */
  /*def apply(cl: WWWView => SGNode[Any]) = {
    this.contentClosure = cl
  }*/

  def render: HTMLNode = {
    logFine[WWWView](s"[RW] Rendering view: "+this.hashCode)
    Injector.inject(this)
    contentClosure(this)
  }
  def render(application: WebApplication, request: HTTPRequest): HTMLNode = {
    this.application = (application)
    this.request = (request)
    Injector.inject(this)
    try {
    var res = contentClosure(this)
    res
    } catch {
      case e : Throwable => 
        e.printStackTrace()
        ""
    }
    //logFine[WWWView](s"-- Rendered res: $res "+this.hashCode)
    //res
  }

  def produce(application: WebApplication, request: HTTPRequest): String = {

    this.application = application
    this.request = request
    Injector.inject(this)
    
    //-- Render full or part
    request.getURLParameter("part") match {

      //-- Try to render part or element id
      case Some(part) ⇒

        logFine[WWWView]("Rendering with parts on view: " + hashCode())

        //- Search
        var p = this.parts.get(part) match {

          //-- Remove part request and product
          case Some(p) ⇒ p

          //-- Maybe the complete view needs to be rerendered because the part definition is somwhere in the view
          case None ⇒

            //-- Try
            logFine[WWWView]("Rendering View to get Part")
            render.toString

            logFine[WWWView](s"parts: " + this.parts)

            //-- Re/Search full part
            (this :: composedViews).collectFirst { case v if(v.parts.get(part)!=None) => v.parts.get(part).get} match {
              case Some(p) => p
              case None => throw new RuntimeException(s"Requested part $part on view ${request.path} which has not been defined, available: ${this.parts.keys}")
            }

        }

        //-- If we reach this point, we have a part
        request.parameters -= request.parameters.find(_._1 == "part").get
        p.produce(application, request)

      case None ⇒ render.toString()
    }

  }

}

object WWWView extends SourceCompiler[WWWView] {

  implicit def viewToSGNode(v: WWWView): HTMLNode = v.render

  
  
  // Configured Imports
  //---------------
  var compileImports = List[Class[_]]()
  var compileImportPackages = List[Package]()

  def addCompileImport(cl: Class[_]): Unit = {
    compileImports.contains(cl) match {
      case false ⇒ compileImports = compileImports :+ cl
      case _ ⇒
    }
  }

  def addCompileImport(p: Package): Unit = {
    compileImportPackages.contains(p) match {
      case false ⇒ compileImportPackages = compileImportPackages :+ p
      case _ ⇒
    }
  }

  var compileTraits = List[Class[_]]()

  /**
   * Add Trait as compile trait, and also as Import
   */
  def addCompileTrait(cl: Class[_]) = {

    //-- Add To compile traits
    compileTraits = (compileTraits :+ cl).distinct
    /*compileTraits.contains(cl) match {
      case false ⇒ compileTraits = compileTraits :+ cl
      case _ ⇒
    }*/

    //-- Add to imports
    addCompileImport(cl)
  }
  
  // Instances Pool
  //-----------------------
  //def getInstance(source:URL)

  // Compilation
  //-----------------
  def doCompile(source: URL): WWWView = {

    
    this.compiler.settings2.maxClassfileName.value = 20
    
    //this.compiler = new EmbeddedCompiler
    
    /*logFine[WWWView](s"In WWWView domCpile -> "+Thread.currentThread().getId)
    
    Thread.currentThread().getContextClassLoader.getParent match {
      case urlcl : URLClassLoader => 
        urlcl.getURLs.foreach {
          url => logFine[WWWView](s"----> compilingin with: "+url)
        }
        
      case _ => 
    }*/
    
    // File Name
    //--------------
    /*logFine[WWWView](s"VIEW IS AT: "+source.getPath)
    var targetName = source.getPath.split("/").last.replace(".", "_")*/
    
    // Read Content of file
    //---------
    var closureContent = scala.io.Source.fromInputStream(source.openStream).mkString

    // Compile as Object
    //------------------------------

    //logFine[WWWView](s"Adding imports: ${compileImports.map { i ⇒ s"import ${i.getCanonicalName()}" }.mkString("\n")}")
   // logFine[WWWView](s"Adding traits: ${ compileTraits.map(cl ⇒ cl.getCanonicalName()).mkString("with ", " with ", "")}")

    //-- Prepare traits
    var traits = compileTraits.size match {
      case 0 => ""
      case _ => compileTraits.map(cl ⇒ cl.getCanonicalName()).mkString("with ", " with ", "")
    }

    var viewString = s"""
    
    import com.idyria.osi.wsb.webapp.view._  
    import  com.idyria.osi.wsb.webapp.injection.Injector._
    import com.idyria.osi.wsb.webapp.injection._
    
    ${compileImports.map { i ⇒ s"import ${i.getCanonicalName()}" }.mkString("\n")}
    
    ${compileImportPackages.map { p ⇒ s"import ${p.getName()}._" }.mkString("\n")}
    
    
    
    var viewInstance = new WWWView $traits {
    	
	this.contentClosure = {
    	view =>  
    		
    		$closureContent
    
    }
    
  }
    
    """

    // Compile as Clousre, and apply to a new WWWView
    //---------------
    /*var closure = s"""    
v.contentClosure =  { view => 
   $closureContent
}
"""

    var wwwview = new WWWView
    //sview.sourceURL = source

    compiler.bind("v", wwwview)

    //compiler.compile(new File(source.getFile))
    try {

      compiler.interpret(closure)

    } catch {
      case e: Throwable ⇒

        logFine[WWWView](s"Compilation error in SView source file: @$source")
        throw new ViewRendererException(s"An error occured while preparing SView @$source: ${e.getMessage()}", e)
    }*/
    //

    // Compile and return 
    //------------
    compiler.interpret(viewString)

    compiler.imain.valueOfTerm("viewInstance") match {
      case None =>

        throw new RuntimeException("Nothing compiled: " + compiler.interpreterOutput.getBuffer().toString())

      case Some(wwwview) =>
        wwwview.asInstanceOf[WWWView]
    }

    /* var wwwview = compiler.imain.valueOfTerm("viewInstance").get.asInstanceOf[WWWView]

    logFine[WWWView]("Compiling view: " + source + " to " + wwwview.hashCode())

    // Save as compiled Source
    //------------

    wwwview*/

  }

}

class WWWViewCompiler extends SourceCompiler[WWWView] {

  implicit def viewToSGNode(v: WWWView): HTMLNode = v.render

  // Configured Imports
  //---------------
  var compileImports = List[Class[_]]()
  var compileImportPackages = List[Package]()

  def addCompileImport(cl: Class[_]): Unit = {
    compileImports.contains(cl) match {
      case false ⇒ compileImports = compileImports :+ cl
      case _ ⇒
    }
  }

  def addCompileImport(p: Package): Unit = {
    compileImportPackages.contains(p) match {
      case false ⇒ compileImportPackages = compileImportPackages :+ p
      case _ ⇒
    }
  }

  var compileTraits = List[Class[_]]()

  /**
   * Add Trait as compile trait, and also as Import
   */
  def addCompileTrait(cl: Class[_]) = {

    //-- Add To compile traits
    compileTraits = (compileTraits :+ cl).distinct
    /*compileTraits.contains(cl) match {
      case false ⇒ compileTraits = compileTraits :+ cl
      case _ ⇒
    }*/

    //-- Add to imports
    addCompileImport(cl)
  }

  // Compilation
  //-----------------
  def doCompile(source: URL): WWWView = {

    //this.compiler = new EmbeddedCompiler
    
    /*logFine[WWWView](s"In WWWView domCpile -> "+Thread.currentThread().getId)
    
    Thread.currentThread().getContextClassLoader.getParent match {
      case urlcl : URLClassLoader => 
        urlcl.getURLs.foreach {
          url => logFine[WWWView](s"----> compilingin with: "+url)
        }
        
      case _ => 
    }*/
    
    // Read Content of file
    //---------
    var closureContent = scala.io.Source.fromInputStream(source.openStream).mkString

    // Compile as Object
    //------------------------------

    //logFine[WWWView](s"Adding imports: ${compileImports.map { i ⇒ s"import ${i.getCanonicalName()}" }.mkString("\n")}")
   // logFine[WWWView](s"Adding traits: ${ compileTraits.map(cl ⇒ cl.getCanonicalName()).mkString("with ", " with ", "")}")

    //-- Prepare traits
    var traits = compileTraits.size match {
      case 0 => ""
      case _ => compileTraits.map(cl ⇒ cl.getCanonicalName()).mkString("with ", " with ", "")
    }

    var viewString = s"""
    
    import com.idyria.osi.wsb.webapp.view._  
    import  com.idyria.osi.wsb.webapp.injection.Injector._
    import com.idyria.osi.wsb.webapp.injection._
    
    ${compileImports.map { i ⇒ s"import ${i.getCanonicalName()}" }.mkString("\n")}
    
    ${compileImportPackages.map { p ⇒ s"import ${p.getName()}._" }.mkString("\n")}
    
    
    
    var viewInstance = new WWWView $traits {
      
  this.contentClosure = {
      view =>  
        
        $closureContent
    
    }
    
  }
    
    """

    // Compile as Clousre, and apply to a new WWWView
    //---------------
    /*var closure = s"""    
v.contentClosure =  { view => 
   $closureContent
}
"""

    var wwwview = new WWWView
    //sview.sourceURL = source

    compiler.bind("v", wwwview)

    //compiler.compile(new File(source.getFile))
    try {

      compiler.interpret(closure)

    } catch {
      case e: Throwable ⇒

        logFine[WWWView](s"Compilation error in SView source file: @$source")
        throw new ViewRendererException(s"An error occured while preparing SView @$source: ${e.getMessage()}", e)
    }*/
    //

    // Compile and return 
    //------------
    compiler.interpret(viewString)

    compiler.imain.valueOfTerm("viewInstance") match {
      case None =>

        throw new RuntimeException("Nothing compiled: " + compiler.interpreterOutput.getBuffer().toString())

      case Some(wwwview) =>
        wwwview.asInstanceOf[WWWView]
    }

    /* var wwwview = compiler.imain.valueOfTerm("viewInstance").get.asInstanceOf[WWWView]

    logFine[WWWView]("Compiling view: " + source + " to " + wwwview.hashCode())

    // Save as compiled Source
    //------------

    wwwview*/

  }

}