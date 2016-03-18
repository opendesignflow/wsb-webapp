package com.idyria.osi.wsb.webapp.http.message

class HTTPPathIntermediary(var basePath: String) extends HTTPIntermediary {
  acceptDown { message =>

    message.path.startsWith(basePath)
  }

  this.onDownMessage {

    message =>
      
      // Remove base path, but not the trailing / if any, to make sure
      /*basePath.endsWith("/") match {
        "/" 
      }*/
      message.changePath( message.path.stripPrefix(basePath))
    

  }
  
}