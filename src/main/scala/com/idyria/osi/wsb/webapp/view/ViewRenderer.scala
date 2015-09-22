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

import com.idyria.osi.wsb.webapp._
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
trait ViewRenderer {

  /**
   * Produce the view Result based on application and request
   */
  def produce(application: WebApplication, request: HTTPRequest): String

}

/**
 *
 * And Exception that happens during view Rendering
 */
class ViewRendererException(message: String, baseException: Throwable) extends Exception(message, baseException) {

  def this(message: String) = this(message, null)

  /*  def this(message:String,baseException:Throwable) = {
    
  }*/

}