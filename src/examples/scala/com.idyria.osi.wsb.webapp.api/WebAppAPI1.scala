package com.idyria.osi.wsb.webapp

//import com.idyria.osi.wsb.core.network.connectors.http._

import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp.http.connector._

import com.idyria.osi.wsb.core._

import com.idyria.osi.vui.lib.gridbuilder._

import scala.io._

import java.io._
import java.nio._

import com.idyria.osi.wsb.webapp.view._

object WebAppAPI1 extends App with GridBuilder {

    println("Welcome to http example WebAppAPI1")

    // Create Engine
    //-----------------------
    var engine = new WSBEngine()

    var connector = HTTPConnector(57300)



    // Add Connector
    //------------------------------
    engine.network.addConnector(connector)

    // Add WebApplication to broker
    //------------------
    var webApp = new WebApplication("/")
    webApp.addFilesSource("com.idyria.osi.wsb.webapp.api/app1")

    engine.broker <= webApp

    webApp addView("/",new ViewRenderer {

        def produce :String = {

            s"""
          <html>
            <head>

            </head>
            <body>
              <h1>Hello World!!</h1>
            </body>
          </html>
          """

        }

    })
    webApp addView("/2",new ViewRenderer {

        def produce :String = {

            s"""
          <html>
            <head>

            </head>
            <body>
              <h1>Hello World 2!!</h1>
            </body>
          </html>
          """

        }

    })

    // Upload
    //-------------------
    webApp.addControler("/upload/configure") {
      message => 

          message.getURLParameter("name") match {
            case Some(name) if(name!="") => 

               println("************  Files will be saved to name: "+name)

               message.getSession("name") match {
                  case Some(existingValue) =>  println("************  Previous name: "+existingValue)
                  case None => 
               }

               message.getSession("name"->name)
               Option(HTTPResponse("application/json","""{ "result" : "ok"}"""))

            case _ => 

                Option(HTTPResponse("application/json","""{ "result" : "error"}"""))

          }
         
         
          
    }

    webApp.addControler("/upload") {

      message  => 

        var fName = message.getSession("name")
        println("Got PUT Message for upload: "+fName)

        // Save File if the part has got the file name
        //-----------------
        (message.getFileName,message.getSession("name")) match {

          case (Some(filename),Some(folderName)) => 

              println(s"Saving ${message.bytes.size} bytes to $folderName/$filename")

              var folder = new File(folderName.toString)
              folder.mkdirs

              var outFile = new File(folder,filename)
              var out = new FileOutputStream(outFile)
              out.write(message.bytes)
              out.flush
              out.close


              Option(HTTPResponse("application/json","""{ "status" : "ok"}"""))

          case _ =>  Option(HTTPResponse("application/json","""{ "status" : "error"}""")) 
        }


    }

    // Start
    //-----------
    engine.lInit
    engine.lStart

    
    // Stop Using VUi
    //---------------------
    var ui = frame {
      f => 

        f title ("WebAppAPI1")
        f size(400,400)

        f <= grid {

          "-" row {

            button("Start") {
              _.onClick {

                 
                 // ui.close
              }
            } | button("Close") {
              _.onClick {

                  engine.lStop
                 // ui.close
              }
            }

          } 

        }
    }
    ui.show

 

 //   engine.lStop


/*
    connector.on("server.started") {

        println("Server started HTTP ")

    }
    connector.on("server.accepted") {

        println("Connection accepted on HTTP ")

    }
    connector.onWith("http.connector.receive") {

        buffer : ByteBuffer =>

                println("Got Datas: "+new String(buffer.array))

    }
    connector.on("server.read") {
        println("Reading some datas ")
    }
    connector.on("server.read.datas") {
        println("Read some datas ")
    }
    connector.on("protocol.receive.endOfData") {
        println("Protocol Found a complete data set")
    }

    // Message Catcher
    //------------------------
    engine.localBus.registerClosure {
        msg : HTTPMessage => 
            println("Received AIB Message")

              // Prepare Write
              var channel = msg.networkContext.asInstanceOf[TCPNetworkContext].socket
              var page = s"""
              <html>
                <head>

                </head>
                <body>
                    <h1>Hello World!!</h1>
                </body>
              </html>
              """

              channel.write(ByteBuffer.wrap("HTTP/1.1 200\n".getBytes))
              channel.write(ByteBuffer.wrap("Content-Type: text/html\n".getBytes))
              channel.write(ByteBuffer.wrap(("Content-Length: "+page.getBytes().length+"\n").getBytes))
              channel.write(ByteBuffer.wrap(page.getBytes))
              channel.close
              channel.socket.close
    }

    // Add
    //------------------------------
    engine.network.addConnector(connector)

    engine.lInit
    engine.lStart

    

    Source.stdin.getLines.foreach {
        f =>
    }

    engine.lStop

    println("End of example")
*/
}
