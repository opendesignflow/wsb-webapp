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

import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp._

/**
 *
 * A common trait type for NavigationRules
 * A Rule performs some checks on the request and returns true or false if valid or not
 *
 * The webapplication is then configured to redirect to the correct view
 */
trait NavigationRule {

  /**
   * The View ID String to which we are going if the rule fails
   */
  var outcome = ""

  def evaluate(application: WebApplication, request: HTTPRequest): Boolean

}