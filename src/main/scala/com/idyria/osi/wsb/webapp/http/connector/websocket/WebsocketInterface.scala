package com.idyria.osi.wsb.webapp.http.connector.websocket

import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import com.idyria.osi.wsb.core.network.connectors.tcp.TCPNetworkContext
import com.idyria.osi.wsb.core.message.soap.SOAP
import com.idyria.osi.wsb.core.message.soap.Envelope
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import java.nio.ByteBuffer
import com.idyria.osi.ooxoo.lib.json.JsonIO

/**
 * @author zm4632
 */
class WebsocketInterface(val nc : TCPNetworkContext) {
  
  def writeMessage(el : ElementBuffer) = {
    
    // Produce XML Bytes
    //-------------
    //var res = StAXIOBuffer(el, true)
    var res = JsonIO(el,true)
    
    // Send
    //---------------
    nc.relatedConnector.send(ByteBuffer.wrap(res.getBytes), nc)
    
  }
  
  def writeSOAPPayload(el:ElementBuffer) = {
    
    var soap = new Envelope
    soap.body.content += el
    
    writeMessage(soap)
    
  }
  
}