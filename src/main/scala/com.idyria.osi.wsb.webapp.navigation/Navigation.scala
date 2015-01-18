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
package com.idyria.osi.wsb.webapp.navigation

import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import java.net.URL

trait PathTrait {

  /**
   * The Full URL path of the element
   */
  var fullPath: String = ""

}

/**
 * The Base navigation class represents the configuration elements to define Views and navigation rules
 *
 * Example:
 *
 * navigation
 *
 */
class DefaultNavigation extends Navigation {


  
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

