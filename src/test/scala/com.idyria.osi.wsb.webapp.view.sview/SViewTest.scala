package com.idyria.osi.wsb.webapp.view.sview

import com.idyria.osi.wsb.webapp.view.sview._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SViewTest extends FunSuite {

  test("Simple View") {

    var sview = SView(getClass().getClassLoader.getResource("SViewTest.sview"))

    println(sview.render(null, null))

    //sview.contentClosure.

  }

  /*  test("Template View") {
    
    var sview = SView(getClass().getClassLoader.getResource("SViewTemplateTest.sview")) 
     
    println(sview.render(null,null))
  } */

}