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
package com.idyria.osi.wsb.webapp.navigation.controller

import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.WebApplication
import javax.faces.bean.ManagedBean
import com.idyria.osi.wsb.webapp.NamedBean

/**
 * A trait implemented by All navigation Controlers
 */
abstract class Controller extends NamedBean {

  this.name = getClass.getCanonicalName
  
  getClass.getAnnotation(classOf[ManagedBean]) match {
    case null =>  this.name = getClass.getCanonicalName
    case annotation =>   this.name = annotation.name()
  }
  
  /**
   * Execute the Controller, and return the navigation outcome
   */
  def execute(application: WebApplication, request: HTTPRequest): String

}
/**
 *
 * This trait is to be mixed by classes that want to have multiple methods be mapped as action controler.
 *
 * Each method having the following format:
 *
 * def methodName(application: WebApplication, request: HTTPRequest): String
 *
 * will be seen as an action executor under the name:
 *
 * name.of.action.controller.methodName
 *
 *
 */
trait ActionsControllerHandler extends Controller {

}
