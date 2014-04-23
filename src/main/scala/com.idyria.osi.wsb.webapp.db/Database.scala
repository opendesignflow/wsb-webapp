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
package com.idyria.osi.wsb.webapp.db

import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.xattribute
import com.idyria.osi.ooxoo.core.buffers.datatypes.XSDStringBuffer

/**
 * Abstract representation of a database
 * mainly just for the purpose of storing the reference into the application
 *
 * Database extends ElementBuffer so that the structure could be read from an XML Configuration file
 *
 */
class Database extends ElementBuffer {

  /**
   * All Database structures must have an id
   */
  @xattribute
  var id: XSDStringBuffer = null

}