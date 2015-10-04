package com.idyria.osi.wsb.webapp.appserv

import java.net.URL
import com.idyria.osi.wsb.webapp.SimpleFolderWebApplication
import java.io.File
import com.idyria.osi.wsb.core.message.soap.SOAPMessagesHandler
import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPResponse
import org.apache.http.HttpMessage
import uni.hd.cag.utils.security.sha1.SHA1Utils
import org.bouncycastle.crypto.digests.SHA1Digest
import java.security.MessageDigest
import java.util.Base64
import java.nio.ByteBuffer
import com.idyria.osi.wsb.core.network.connectors.tcp.TCPNetworkContext
import com.idyria.osi.wsb.webapp.http.connector.websocket.WebsocketInterface

/**
 * @author zm4632
 */
class AIBURLSourceWebApp(val baseURL: URL, val path: String) extends AIBAbstractWebapp {

  this.name = path
  onInit {

    println(s"Init Webapp $path -> ${baseURL.getFile}")

    var fapp = new SimpleFolderWebApplication(new File(baseURL.getFile).getAbsoluteFile, path)
    this.application = fapp

    println(s"Done init: " + fapp.baseDir.getAbsolutePath)

    //fapp <= new 

    fapp.viewsIntermediary <= new SOAPMessagesHandler {

      this.onDownMessage { msg =>

        println(s"SOAP: " + msg)
      }
    }

    fapp.viewsIntermediary <= new HTTPIntermediary {

      filter = """http:/websocket:.*""".r

      name = "WebSocket"

      onDownMessage { req =>

        // Prepare response
        var resp = new HTTPResponse

        // Get Client Handshake
        var clientHandshake = req.getParameter("Sec-WebSocket-Key")
        println(s"Client handshake: " + clientHandshake)
        println(s"NC is: "+req.networkContext.asInstanceOf[TCPNetworkContext])
        // Check version
        var clientVersion = req.getParameter("Sec-WebSocket-Version")
        clientVersion match {
          
          // ok
          case Some(v) if (v.toInt >= 13) =>

            // Create Server Handshake
            /*
         *  A |Sec-WebSocket-Accept| header field.  The value of this
           header field is constructed by concatenating /key/, defined
           above in step 4 in Section 4.2.2, with the string "258EAFA5-
           E914-47DA-95CA-C5AB0DC85B11", taking the SHA-1 hash of this
           concatenated value to obtain a 20-byte value and base64-
           encoding (see Section 4 of [RFC4648]) this 20-byte hash.
         */
            var serverhandshake = s"${clientHandshake.get}258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
            var serverhandshakeSha = MessageDigest.getInstance("SHA-1").digest(serverhandshake.getBytes("UTF-8"))
            var serverHandshadeEncoded = Base64.getEncoder.encodeToString(serverhandshakeSha)

            // Set Fields
            resp.code = 101
            resp.addParameter("Upgrade", "websocket")
            resp.addParameter("Connection", "Upgrade")
            resp.addParameter("Sec-WebSocket-Accept", serverHandshadeEncoded)
            resp.addParameter("Sec-WebSocket-Protocol", "soap")
            
            // Data is just an empty line
            //resp.content = ByteBuffer.wrap("\r\n".getBytes)
            
            // Record Utility Object in session
            //var websocket = req.networkContext.asInstanceOf[TCPNetworkContext]
            req.getSession("websocket" ->  new WebsocketInterface( req.networkContext.asInstanceOf[TCPNetworkContext]))
            //resp.getSession("websocket" ->  new WebsocketInterface( req.networkContext.asInstanceOf[TCPNetworkContext]))
            
            response(resp)

          // Not ok
          case _ =>

            resp.code = 426
            resp.addParameter("Sec-WebSocket-Version", "13")
            response(resp)

        }
        // println(s"Client Version: "+clietnVersion)

      }

    }

  }

}