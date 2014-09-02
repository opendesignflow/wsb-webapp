package com.idyria.osi.wsb.webapp.http

import org.scalatest.FunSuite
import org.scalatest.GivenWhenThen
import scala.io.Source
import com.idyria.osi.wsb.webapp.http.connector.HTTPProtocolHandler
import com.idyria.osi.wsb.core.network.connectors.tcp.TCPNetworkContext
import java.nio.ByteBuffer
import com.idyria.osi.tea.logging.TLog

class HTTPContentTypeTest extends FunSuite with GivenWhenThen {

  test("ChunkedEncoding one pass") {

    TLog.setLevel(classOf[HTTPProtocolHandler], TLog.Level.FULL)

    //-- Read Response and print
    var bytes = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("com.idyria.osi.wsb.webapp.http/stream.bin")).grouped(2).map {
      hex =>
        Integer.parseInt(hex.mkString, 16)
    }.map(_.toByte).toArray

    //-- Print
    /*println(s"Number of bytes: " + bytes.length)
    println(s"First byte: " + bytes(0).toChar)
    bytes.foreach {
      b =>
        print(b.toChar)
    }*/

    //-- Parse Using protocol handler
    var context = new TCPNetworkContext("")
    var handler = new HTTPProtocolHandler(context)

    handler.receive(ByteBuffer.wrap(bytes))

    //-- Analyse received data
    assertResult(1, "One part created")(handler.availableDatas.size)

    //-- Bytes size of result
    assertResult(42359, "Total Size of content")(handler.availableDatas.head.bytes.length)

  }

  test("ChunkedEncoding two passes") {

    TLog.setLevel(classOf[HTTPProtocolHandler], TLog.Level.FULL)

    //-- Read Response and print
    var bytes = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("com.idyria.osi.wsb.webapp.http/stream.bin")).grouped(2).map {
      hex =>
        Integer.parseInt(hex.mkString, 16)
    }.map(_.toByte).toArray

    var (firstPass, secondPass) = bytes.splitAt(bytes.size / 2)

    //-- Print
    /*println(s"Number of bytes: " + bytes.length)
    println(s"First byte: " + bytes(0).toChar)
    bytes.foreach {
      b =>
        print(b.toChar)
    }*/

    //-- Parse Using protocol handler
    var context = new TCPNetworkContext("")
    var handler = new HTTPProtocolHandler(context)

    //-- Pass One

    handler.receive(ByteBuffer.wrap(firstPass))

    //-- Pass two
    handler.receive(ByteBuffer.wrap(secondPass))

    //-- Analyse received data
    assertResult(1, "One part created")(handler.availableDatas.size)

    //-- Bytes size of result
    assertResult(42359, "Total Size of content")(handler.availableDatas.head.bytes.length)

  }

}