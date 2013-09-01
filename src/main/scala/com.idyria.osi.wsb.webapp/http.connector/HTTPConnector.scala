package com.idyria.osi.wsb.webapp.http.connector

import com.idyria.osi.wsb.core.network._
import com.idyria.osi.wsb.core.network.connectors._
import com.idyria.osi.wsb.core.network.connectors.tcp._
import com.idyria.osi.wsb.core.network.protocols._

import com.idyria.osi.wsb.core.message._
import com.idyria.osi.wsb.webapp.mime._
import com.idyria.osi.wsb.webapp.http.message._

import com.idyria.osi.tea.listeners.ListeningSupport

import java.nio.channels._
import java.nio._
import java.io._

import scala.collection.JavaConversions._
import scala.io.Source

class HTTPConnector( cport : Int) extends TCPProtocolHandlerConnector[MimePart]( ctx => new HTTPProtocolHandler(ctx) ) {

  this.address = "0.0.0.0"
	this.port = cport
	this.messageType = "http"

	Message("http",HTTPRequest)


  /**
    After sending response data to a client, one must close the socket
  */
  override def send(buffer:ByteBuffer, context: TCPNetworkContext) = {
    super.send(buffer,context)

    println("Send datas to client -> close it")
    //context.socket.close 
    //context.socket.socket.close

  }
}
 
object HTTPConnector {

	def apply(port: Int) : HTTPConnector = new HTTPConnector(port)
}

class HTTPProtocolHandler (var localContext : NetworkContext) extends ProtocolHandler[MimePart](localContext) with ListeningSupport {

	// Receive
	//-----------------
  
  /**
    Read lines or bytes depending on data received

    Supported:

      line or bytes
  */
  var readMode = "line"

  /**
    The read mode for upcoming message part
  */
  var nextReadMode = "line"


  var contentLength = 0

  var contentTypeRegexp = """Content-Type: (.*)""".r
  var contentLengthRegexp = """Content-Length: (.*)""".r
	
	// Take Lines and create message
  var currentPart = new DefaultMimePart()

	// Send
	//---------------------

	/**
		REceive some HTTP
	*/
	def receive(buffer : ByteBuffer) : Boolean = {

		@->("http.connector.receive",buffer)
    var bytesArray = buffer.array
		//println("Got HTTP Datas: "+new String(bytesArray))
    	
    	// Use SOurce to read from buffer
    	//--------------------
      var bytes  = bytesArray
    	//var bytesSource = Source.fromInputStream(new ByteArrayInputStream(buffer.array))
      var stop = false

      do {

        // If no bytes to read, put on hold
        if (bytes.size==0) 
          stop = true 
        else
          // Read Mode
          //------------------
          readMode match {

            // Take line
            //---------------
            case "line" =>

                //  Read line
                var currentLineBytes = bytes.takeWhile(_ != '\n')
                bytes = bytes.drop(currentLineBytes.size+1)
                var line = new String(currentLineBytes.toArray).trim

                //-- Parse protocol
                //-------------------------
                line match {

                  //-- Content Length expected
                  case line if(contentLengthRegexp.findFirstMatchIn(line) match {
                      case Some(matched) => 

                        contentLength = matched.group(1).toInt
                        true

                      case None => false
                    }) => 

                      currentPart.addParameter(line)

                  //-- Content Type
                   case line if(contentTypeRegexp.findFirstMatchIn(line) match {

                      // Multipart form data -> just continue using lines
                      case Some(matched) if(matched.group(1).matches("multipart/form-data.*")) => 

                          true

                      // Otherwise, just buffer bytes
                      case Some(matched) => 
                          nextReadMode = "bytes"
                          true

                      case None => false
                    }) => 

                      currentPart.addParameter(line)

                  //-- Normal Line
                  case line if (line!="") => 

                    
                      currentPart.addParameter(line)

                  //-- Empty Line but content is upcomming
                  case line if (line=="" && contentLength!=0 && nextReadMode=="line") =>

                      println(s"Empty Line but some content is expected") 

                      //--> Write this message part to output
                      this.availableDatas += this.currentPart 
                      this.currentPart = new DefaultMimePart
                   

                  //-- Empty line, content is upcoming and next Read mode is not line
                  case line if (line=="" && contentLength!=0 && nextReadMode!="line") =>

                       println(s"Empty Line but some content is expected in read mode: $nextReadMode, for a length of: $contentLength") 
                       readMode = nextReadMode


                  //-- Empty Line and no content
                  case line if (line=="" && contentLength==0) =>

                    println(s"Empty Line and no content expected, end of section")

                    //--> Write this message part to output
                    this.availableDatas += this.currentPart 
                    this.currentPart = new DefaultMimePart
                    this.contentLength = 0
                  
                }

            // Buffer Bytes
            //---------------
            case "bytes" =>
                
                // Read
                this.currentPart += bytes
                bytes = bytes.drop(bytes.size)

                // Report read progress 
                var progress = this.currentPart.bytes.size * 100.0 / contentLength
                println(s"Read state: $progress %, $contentLength expected, and read bytes ${this.currentPart.bytes.size} and content length: ${this.currentPart.contentLength} ")
                if ( (contentLength -this.currentPart.contentLength) < 10 ) {
                //if ( progress == 100 ) {

                  (this.availableDatas.contains(this.currentPart),this.availableDatas.size) match {

                    // Part is already stacked, don't do anything
                    case (true,size) => 

      

                    // Add part
                    case(false,0) =>

                      this.availableDatas += this.currentPart 

                    // Merge part
                    case(false,size) => 

                        println("Merging with head")
                        this.availableDatas.head.append(this.currentPart)
                  }
                  

                  // Reset all
                  this.currentPart = new DefaultMimePart
                  this.contentLength = 0
                  this.nextReadMode = "line"
                  this.readMode = "line"
     

                }

            case mode => throw new RuntimeException(s"HTTP Receive protocol unsupported read mode: $mode")

          }


      } while(!stop)
      
      true


	}

	def send (buffer: ByteBuffer) :  ByteBuffer  = {
		buffer
	}

}
