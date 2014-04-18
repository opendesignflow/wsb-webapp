package com.idyria.osi.wsb.webapp.navigation

import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import java.net.URL

/**
 * The Base navigation class represents the configuration elements to define Views and navigation rules
 *
 * Example:
 *
 * navigation
 *
 */
class DefaultNavigation extends Navigation {

}

object DefaultNavigation {

  def apply(path: URL): Navigation = {

    //-- Parse
    var navigationConfig = new DefaultNavigation

    var io = new StAXIOBuffer(path)
    navigationConfig.appendBuffer(io)
    io.streamIn

    navigationConfig

  }
}

/**
 *
 */
class DefaultGroup extends GroupTrait with PathTrait {

  /**
   * Execute a closure on all of this Group elements
   * It is not per default recursive, user has to create recursivity
   */
  def transform(cl: PartialFunction[Any, Any]): List[Any] = {

    var res = List[Any]()

    // Transform all. Use recursivity on groups
    //------------
    this.groups.foreach {
      g =>
        g.fullPath = this.fullPath + "/" + g.id;
        res = cl(g) :: res
    }
    this.views.foreach {
      v =>
        v.fullPath = this.fullPath + "/" + v.id;
        res = cl(v) :: res
    }
    this.rules.foreach {
      r =>
        r.fullPath = this.fullPath + r.for_;
        res = cl(r) :: res
    }

    res

  }

  def onAll(cl: PartialFunction[Any, Unit]) = {

    // Transform all. Use recursivity on groups
    //------------
    this.groups.foreach {
      g => g.fullPath = this.fullPath + "/" + g.id; cl(g)
    }
    this.views.foreach {
      v => v.fullPath = this.fullPath + "/" + v.id; cl(v)
    }
    this.rules.foreach {
      r => r.fullPath = this.fullPath + r.for_; cl(r)
    }

  }

}

trait PathTrait {

  /**
   * The Full URL path of the element
   */
  var fullPath: String = ""

}