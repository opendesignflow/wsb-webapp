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
package com.idyria.osi.wsb.webapp

import javax.faces.bean.ManagedBean

trait NamedBean {

  /**
   *
   * The action string is used to map the controler to an "action" request parameter value
   * So that we can call the right action when an HTTP request comes
   */
  var name = ""

  // Action Is per default:
  //  - The name of managed bean Annotation if one is present
  //  - The class name
  //---------
  getClass().getAnnotation(classOf[ManagedBean]) match {
    case null       => name = getClass.getSimpleName
    case annotation => name = annotation.name()
  }

}