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