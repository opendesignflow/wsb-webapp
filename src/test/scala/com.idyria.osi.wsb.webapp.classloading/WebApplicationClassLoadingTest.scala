package com.idyria.osi.wsb.webapp.classloading

import org.scalatest.FunSuite
import java.io.File
import java.io.PrintStream
import java.io.FileOutputStream
import java.net.URL
import com.idyria.osi.tea.thread.ThreadLanguage
import com.idyria.osi.aib.core.compiler.EmbeddedCompiler
import javax.script.ScriptEngineManager
import scala.collection.JavaConversions._

class WebApplicationClassLoadingTest extends FunSuite with ThreadLanguage {

  test("Classloader reloading") {

    //-- Test get
    var resource = new File(s"target/compiletest.sscript")
    assert(resource != null)
    var os = new FileOutputStream(resource)
    var outPrint = new PrintStream(os)
    outPrint.print("""
package a.b.c
class AA {
  var id : Short = 0

}
""")
    outPrint.flush()

    //-- Create Class loader
    var cl = new WebApplicationClassLoader(Array[URL]())

    //-- Add file
    cl.addDirectory(resource.getParentFile)

    // Make Tests From Thread with our special Class laoder 
    //------------------------
    var th = createThread {
     
      
      // Test with first version
      //-----------------
      var compiler = new EmbeddedCompiler
      compiler.interpret("import a.b.c.AA")
      compiler.interpret("var aInst = new a.b.c.AA")
      compiler.interpret("""println(aInst.id)""")


    }
    th.setContextClassLoader(cl.internalClassloader)
    th.start()
    th.join()

    // Second Version
    //-------------------------
    outPrint = new PrintStream(new FileOutputStream(resource))
    outPrint.print("""
package a.b.c

class AA {
  var id : Short = 2

}

""")
    outPrint.flush()
    Thread.sleep(500)
    println(s"Second version")
    cl.test
    th = createThread {
     
      
      // Test with first version
      //-----------------
      var compiler = new EmbeddedCompiler
      compiler.interpret("import a.b.c.AA")
      compiler.interpret("var aInst = new a.b.c.AA")
      compiler.interpret("""println(aInst.id)""")


    }
    th.setContextClassLoader(cl.internalClassloader)
    th.start()
    th.join()
  }

}

