/**
 *
 */
package com.idyria.osi.wsb.webapp.classloading

import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.net.URLClassLoader
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent

import scala.collection.JavaConversions.asScalaBuffer

import com.idyria.osi.aib.core.compiler.EmbeddedCompiler
import com.idyria.osi.tea.thread.ThreadLanguage

/**
 * @author zm4632
 *
 */
class WebApplicationClassLoader(arr: Array[java.net.URL], parent: ClassLoader = Thread.currentThread().getContextClassLoader) extends URLClassLoader(arr, parent) with ThreadLanguage {

  //-- Compiler
  new File("target/webapp-classes").mkdirs()
  //super.addURL(new File("target/webapp-classes").toURI().toURL())
  var compiler = new EmbeddedCompiler
  compiler.settings2.outputDirs.setSingleOutput("target/webapp-classes")
  //compiler.imain.settings.outdir.value = "target/webapp-classes"
  //new File("target/webapp-classes").mkdirs()

  // Internal classloader
  //-----------------------
  var internalClassloader = URLClassLoader.newInstance(arr :+ new File("target/webapp-classes").toURI().toURL(), parent)
 
  //-- Source files
  var watcher = FileSystems.getDefault().newWatchService();
  var sources = List[File]()
  var directories = List[Path]()

  def addDirectory(f: File): Unit = {

    // Monitor file changes of this source
    var filePath = FileSystems.getDefault.getPath(f.getAbsolutePath)
    var watchkey = filePath.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE)
    directories = directories :+ filePath

    // Init Sources
    f.listFiles().filter { f => f.getName.endsWith("sscript") }.foreach {
      f =>
        sources = sources :+ f
        compileSource(f)
    }

  }

  //-- Compiling
  //------------------
  def compileSource(s: File): Unit = {

    println(s"Compiling: ${s.getAbsolutePath}")
    compiler.compile(scala.io.Source.fromFile(s, "UTF8").mkString)
    println(s"Compiled")
  }

  // Loading
  //--------------
  
  /*override def loadClass(cl:String,resolve:Boolean) : Class[_] = {
    println(s"****** Loading  class $cl")
    super.loadClass(cl, resolve)
  }*/
  /*override def findClass(cl:String) : Class[_] = {
      
    println(s"****** Looking for class $cl")
    try {
      super.findClass(cl)
    } catch {
      case e : Throwable => 
        //println(s"")
        println("+++Execption during find class")
        null
    }
     /* // Look for class
      //----------------------
      try {
        internalClassloader.loadClass(cl)
      } catch {
        case e : ClassNotFoundException => super.findClass(cl)
      } 
      finally {
        
      }*/
    
  }
 /* override def findLoadedClass(cl:String) : Class[_] = {
      
    println(s"****** Looking for loaded class $cl")
    
      // Look for class
      //----------------------
      try {
        internalClassloader.loadClass(cl)
      } catch {
        case e : ClassNotFoundException => super.findClass(cl)
      } 
      finally {
        
      }
    
  }*/
  
  override def getPackages : Array[Package] = {
    
    
    var gpM = internalClassloader.getClass.getMethod("getPackages")
    gpM.setAccessible(true)
    var res = gpM.invoke(internalClassloader).asInstanceOf[Array[Package]]
    
    println(s"****** Loogin for packages: $res")
    res
    //super.getPackages()
    
  }
  
  override def  getResource(name:String) : URL = {
    println(s"****** Looking for resource: $name")
    var res = super.getResource(name)
    println(s"********** Looking for resource: $name -> $res")
    
    res
  }
  
  override def  getResources(name:String) : java.util.Enumeration[URL] = {
    
    var res = super.getResources(name)
    println(s"****** Looking for resources: $name -> $res")
    
    res
  }
  
  override def getPackage(cl:String) : Package = {
    
    println(s"****** Looking for package $cl")
      // Look for class
      //----------------------
      try {
        var gpM = internalClassloader.getClass.getMethod("getPackage", classOf[Package])
        gpM.setAccessible(true)
        gpM.invoke(internalClassloader, cl).asInstanceOf[Package]
       // internalClassloader.getPackage(cl)
      } catch {
        case e : ClassNotFoundException => super.getPackage(cl)
      } 
      finally {
        
      }
    
  }*/

  /*override def defineClass(name: String,
    b: Array[Byte],
    off: Int,
    len: Int,pd:ProtectionDomain): Class[_] = {
    
    // Try on internal
     try {
        //internalClassloader.defineClass(name,b,off,len,pd)
       internalClassloader.loadClass(name)
      } catch {
        case e : Throwable => super.defineClass(name,b,off,len,pd)
      } 
      finally {
        
      }
  }*/

  def test = {
    
    // Remove Class loader 
    this.internalClassloader=null
    /*sys.runtime.gc()
    sys.runtime.gc()*/
    internalClassloader = URLClassLoader.newInstance(arr :+ new File("target/webapp-classes").toURI().toURL(), parent)
    /*sys.runtime.gc()
    sys.runtime.gc()*/
    /*
    sys.runtime.gc()
    sys.runtime.gc()
    
    this.loadClass("a.b.c.A")
    this.loadClass("a.b.c.A", true)
    this.loadClass("a.b.c.A", false)

    this.findClass("a.b.c.A")
    
    println(s"Trying to reload AA")
    
    var aaFile = new File("target/webapp-classes/a/b/c/AA.class")
    var ds = new DataInputStream(new FileInputStream(aaFile))
    
    var read = new Array[Byte](aaFile.length().toInt)
    ds.readFully(read)
    ds.close()
    
    this.defineClass("a.b.c.AA", read, 0, read.length)
    //this.defineClass("a.b.c.AA", read, 0, read.length)*/

  }

  //-- Watcher
  //----------------
  var watcherThread = createThread {

    var stop = false
    while (!stop) {
      try {

        // Get Key
        var key = watcher.take()

        // Loop over events
        key.pollEvents().filter { ev => ev.kind() != StandardWatchEventKinds.OVERFLOW }.foreach {
          case event: WatchEvent[Path] if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) =>

            //-- Get File 

            var file = directories(0).resolve(event.context()).toFile()
            println(s"Changed File ${file.getAbsolutePath}")

            //-- Recompile
            if (file.getName.endsWith("sscript")) {
              compileSource(file)
              internalClassloader = URLClassLoader.newInstance(Array(new File("target/webapp-classes").toURI().toURL()), this)
              sys.runtime.gc()
            }

          case _ =>

        }

        //-- invalid key
        
        key.reset()

      } catch {
        case e: InterruptedException => stop = true
      }

    }

  }
  watcherThread.setDaemon(true)
  watcherThread.start()

}