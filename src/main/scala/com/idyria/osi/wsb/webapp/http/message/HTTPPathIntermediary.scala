package com.idyria.osi.wsb.webapp.http.message

class HTTPPathIntermediary(var basePath: String) extends HTTPIntermediary {
  acceptDown { message =>

    message.path.startsWith(basePath)
  }

  this.onDownMessage {

    message =>
      message.path = message.path.stripPrefix(basePath)

  }
  
}