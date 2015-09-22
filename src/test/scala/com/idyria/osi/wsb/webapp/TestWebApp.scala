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

import java.io.File
import com.idyria.osi.tea.file.TeaFileUtils
import com.idyria.utils.java.io.TeaIOUtils
import com.idyria.osi.tea.file.DirectoryUtilities

/**
 * Test webapp with some default stuff
 */
class TestWebApplication extends WebApplication("/test") {

  // Database
  //------------------

  //-- Set DB base path to test-db and clean
  this.databaseBasePath = new File("db-test")
  DirectoryUtilities.deleteDirectory(this.databaseBasePath)

}