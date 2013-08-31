package com.idyria.osi.wsb.webapp

//import com.idyria.osi.wsb.core.network.connectors.http._

import com.idyria.osi.wsb.core.message.http._
import com.idyria.osi.wsb.core.network.connectors.tcp._
import com.idyria.osi.wsb.core.network.connectors.http._
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
    webApp.addControler("/upload") {
      message => 

        println("Got PUT Message for upload")
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
