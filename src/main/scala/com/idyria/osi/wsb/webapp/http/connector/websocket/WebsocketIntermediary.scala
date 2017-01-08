package com.idyria.osi.wsb.webapp.http.connector.websocket

import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPResponse
import java.util.Base64

class WebsocketIntermediary extends HTTPIntermediary {

  this.acceptDown { req =>
    (req.getParameter("Connection"),req.getParameter("Upgrade"), req.getParameter("Sec-WebSocket-Version")) match {

      case (Some(conn),Some(protocol), Some("13")) if(conn.toLowerCase().contains("upgrade") && protocol.toLowerCase()=="websocket") => true
      case _ => false
    }
  }

  this.onDownMessage {
    req =>
      println(s"-- Got Websocket message");
      
      //-- Convert Key
      var key = req.getParameter("Sec-WebSocket-Key") match {
        case Some(key) => 
          
          var r = new HTTPResponse
          r.code = 101
          r.addParameter("Connection", "Upgrade")
          r.addParameter("Upgrade", "websocket")
          r.addParameter("Sec-WebSocket-Protocol","soap")
          
           //-- Answer with upgrade
        
          r.addParameter("Sec-WebSocket-Accept",  Base64.getEncoder.encodeToString(WebsocketIntermediary.md.digest((key+"258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes)) )
        
          response(r,req)
          
        case None => 
          var r = new HTTPResponse
          r.code = 400
          response(r,req)
      }
      
     
      
  }

}

object WebsocketIntermediary {
  
  val md = java.security.MessageDigest.getInstance("SHA-1");
  
}