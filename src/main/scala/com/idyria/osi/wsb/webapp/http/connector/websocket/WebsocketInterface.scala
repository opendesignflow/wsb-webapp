package com.idyria.osi.wsb.webapp.http.connector.websocket

import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import com.idyria.osi.wsb.core.network.connectors.tcp.TCPNetworkContext
import com.idyria.osi.wsb.core.message.soap.SOAP
import com.idyria.osi.wsb.core.message.soap.Envelope
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import java.nio.ByteBuffer
import com.idyria.osi.ooxoo.lib.json.JsonIO
import com.idyria.osi.wsb.core.message.soap.SOAPMessage
import com.idyria.osi.wsb.core.message.soap.JSONSOAPMessage
import com.idyria.osi.wsb.core.message.soap.EnvelopeBody

/**
 * @author zm4632
 */
class WebsocketInterface(val nc : TCPNetworkContext) {
  
  def writeMessage(el : ElementBuffer) = {
    
    // Produce XML Bytes
    //-------------
    //var res = StAXIOBuffer(el, true)
    //println(s"Converting to JSON WS message");
    var res = JsonIO(el,true)
    
    // Send
    //---------------
    //println(s"Sending WS message");
    nc.relatedConnector.send(ByteBuffer.wrap(res.getBytes), nc)
    
  }
  
  def writeSOAPPayload(el:ElementBuffer) = {
    
    //println(s"1 Converting to JSON WS message");
    var soap = new JSONSOAPMessage
    soap.body = EnvelopeBody()
    soap.body.content += el
    
   // println(s"2 Converting to JSON WS message");
    writeMessage(soap)
    
  }
  
}