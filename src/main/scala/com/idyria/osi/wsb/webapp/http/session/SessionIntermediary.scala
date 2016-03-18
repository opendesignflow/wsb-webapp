package com.idyria.osi.wsb.webapp.http.session

import sun.rmi.transport.proxy.HttpOutputStream
import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.http.message.HTTPResponse

class SessionIntermediary extends HTTPIntermediary {

  //-- Session
  this.onDownMessage { req =>

    logFine[SessionIntermediary](s"Taking care of session...")
    var s = req.getSession

    logFine[SessionIntermediary](s"Session Number: " + s.id)
  }
  this.onUpMessage[HTTPResponse] { resp =>
    
    //println(s"Got Response to answer with " -> resp.relatedMessage)
    
    resp.relatedMessage match {
      case null =>
      case req: HTTPRequest if (req.session != null) =>
        resp.session = req.getSession
      case _ =>
    }
  }

}