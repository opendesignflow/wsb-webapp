package com.idyria.osi.wsb.webapp.http.message

trait HTTPPathIntermediaryTrait extends HTTPIntermediary {
  var basePath : String
}
class HTTPPathIntermediary(var basePath: String) extends HTTPPathIntermediaryTrait {

  //tlogEnableFull[HTTPPathIntermediary]
  
  // Make sure basePath has no double slash
  require(basePath != null)
  basePath = ("/" + basePath).replaceAll("//+", "/")

  acceptDown[HTTPRequest] { message =>
    val res =  message.path.startsWith(basePath)
    logFine[HTTPPathIntermediary](s"($res)Testing message with path: " + message.originalPath + " against " + basePath +" , subtree: "+this.intermediaries.filter{i => i.isInstanceOf[HTTPPathIntermediary]}.map {i => i.asInstanceOf[HTTPPathIntermediary].basePath})
    res
  }

  this.onDownMessage {

    message =>

      // Remove base path, but not the trailing / if any, to make sure
      /*basePath.endsWith("/") match {
        "/" 
      }*/
      message.changePath(message.path.stripPrefix(basePath))

  }

}