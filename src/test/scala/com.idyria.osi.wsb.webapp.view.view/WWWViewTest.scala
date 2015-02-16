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
package com.idyria.osi.wsb.webapp.view.view

import scala.collection.JavaConversions._

import com.idyria.osi.wsb.webapp.view.WWWView
import org.scalatest.FunSuite
import com.idyria.osi.aib.core.compiler.EmbeddedCompiler
import java.net.URLClassLoader

class WWWViewTest extends FunSuite {   

  test("Compile Test") {

    println(s"In Test CL: "+Thread.currentThread().getContextClassLoader.asInstanceOf[URLClassLoader].getURLs.mkString)
    //    println(s"-<btc "+this.compiler.bootclasspath)
//    println(s"-<btc "+this.compiler.settings2.bootclasspath.value)
//     println(s"-<btc "+this.compiler.settings2.classpath.value)
     //println(s"-<btc "+this.compiler.settings2.classpathURLs)
     println(s"-<btc "+WWWView.compiler.imain.global.classPath.asClasspathString)
      println(s"-<btc "+ WWWView.compiler.imain.compilerClasspath.mkString(";"))
    
    
    // Source 
    var url=getClass.getClassLoader.getResource("com.idyria.osi.wsb.webapp.view.view/test.view")
    
    // Compiler
    //var obj = WWWView;
    //WWWView.compiler = new EmbeddedCompiler
    //WWWView.compiler.settings2.usejavacp.value = true
   // WWWView.compiler.settings2.usemanifestcp.value = true
    WWWView.compiler.init
    WWWView.compiler.waitReady
    
    WWWView.compiler.imain.bind("cl",Thread.currentThread().getContextClassLoader)
    println("int: "+WWWView.compiler.interpret("Thread.currentThread().setContextClassLoader(cl)"))
    
    println("int: "+WWWView.compiler.interpret("Thread.currentThread.getContextClassLoader.getClass"))
    
    println("int: "+WWWView.compiler.interpret("com.idyria.osi.wsb.webapp.view.WWWView.compileImports"))
    
     //WWWView.compiler.imain.compilerClasspath
    //WWWView.compile(url, "/test.view")
   // println(s"Res: "+)

  }

}