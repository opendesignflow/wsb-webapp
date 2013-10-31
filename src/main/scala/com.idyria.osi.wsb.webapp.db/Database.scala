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