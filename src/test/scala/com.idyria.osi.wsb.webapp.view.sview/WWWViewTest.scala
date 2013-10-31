package com.idyria.osi.wsb.webapp.view

import org.scalatest.FunSuite

class SViewTest extends FunSuite {

  test("Compile Test") {

    class Test extends WWWView {

      this.contentClosure = { view =>

        view.compose("whatever") {
          p =>
        }

      }

    }

    var inst = new Test
    inst.contentClosure = {
      view =>
        view.compose("whatever") {
          p =>
        }
    }

  }

}