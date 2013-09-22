
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

class SView {
   
  /**
   * Defined is the SView has been fetched from an URL
   */
  var sourceURL : URL = null
  
  /**
   * The main content of the sview
   */
  var contentClosure : ( SView,WebApplication,HTTPRequest) => Any = null
  
  /**
   * Execute closure on this SView to configure the view
   */
  def apply(cl : SView => Any) = {
    cl(this)
  }
  
  /**
   * Define closure to produce content on request
   */
  def content( cl: ( SView,WebApplication,HTTPRequest) => Any) = contentClosure = cl
  
  // Current Request
  //--------------------------
  var application : WebApplication = null 
  var request : HTTPRequest = null
  
  // Templating
  //------------------

  var tiles = Map[String,( SView,WebApplication,HTTPRequest) => Any]()
  
  /**
   * Define the base template to be first extracted for this view
   */
  var template : String = null

  /**
   * Define some content to be injected at a specific placeholder location
   */
  def inject(placeHolder: String)(content: ( SView,WebApplication,HTTPRequest) => Any) = {
	  
	  this.tiles = this.tiles + (placeHolder -> content)
  }
  
  def tile(placeHolder:String) : Any = {
    
    this.tiles.get(placeHolder) match {
      case Some(closure) => closure(this,application,request)
      case None => ""
    }
  }
  
  /**
   * Render Template or content
   * 
   */
  def render(application: WebApplication,request: HTTPRequest) : String = {
    
    this.application = application
    this.request = request
    
    (template,contentClosure) match {
      case (template,_) if(template!=null) => 
        
        // Determine if we need to find template from class loader or file
        //--------------
        var templateLocation = getClass.getClassLoader.getResource(template) match {
          case null => 
            
            //-- File possible locations are normal file or relative to current
            var locations = List[File]()
            
            // Try to find template relative to this view path
            (new File(template).getParentFile,sourceURL) match  {
          		case (_,null) 		=>
          		case(f,sourceURL) if (f.isAbsolute) 	=>
          		case (f,sourceURL)	=> 
          		  	//locations = new File(s"${sourceURL}"+File.separator+".."+File.separator+template) :: locations
          		   println(s"*** Source URL $sourceURL ")
          		  
          		  var splittedURL = sourceURL.getFile.split("/")
          		  locations = new File(s"${splittedURL.take(splittedURL.size-1).mkString(File.separator)}"+File.separator+template) :: locations
            }
            
            // Add as a normal file
            locations = new File(template) :: locations
            
            println(s"*** Searching from $locations ")
            
            //-- Search
            locations.find(f => f.exists) match {
              case Some(file) => file.toURI.toURL
              case None => throw new RuntimeException(s"Could not locate template from file or classloader: $template")
            }
           
          case url => url
        }
        
        var templateView = SView(templateLocation)
        contentClosure = templateView.contentClosure
        contentClosure(this,application,request).toString
        
      case (null,contentClosure) => contentClosure(this,application,request).toString


        
    }
  }
}

class SViewCompilationResult(var view : SView) {
  
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
  var viewsMap = Map[URL,SViewCompilationResult]()
  
  
  /**
   * Creates an SView from an URL source
   */
  def apply(source: URL) : SView = {
    
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
    
    compiler.bind("v",sview)
    
    //compiler.compile(new File(source.getFile))
    compiler.interpret(closure)
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
  def apply(source: String) : SView = {
    null
  }
  
}
