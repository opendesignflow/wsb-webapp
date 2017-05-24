/*
 * #%L
 * WSB Webapp
 * %%
 * Copyright (C) 2013 - 2014 OSI / Computer Architecture Group @ Uni. Heidelberg
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.idyria.osi.wsb.webapp.http.websockets

import org.scalatest.FunSuite
import scala.collection.JavaConversions._
import com.idyria.osi.wsb.webapp.http.connector.websocket.WebsocketProtocolhandler

class WebsocketsProtocolTest extends FunSuite {

  test("Unmask") {

    var key = 0xbec5b9e8
    var keyOctets = Array((key >> 0) & 0xFF, (key >>> 8) & 0xFF, (key >>> 16) & 0xFF, (key >>> 24) & 0xFF).reverse

    var input = Array[Int](0x8f, 0xd7, 0xac, 0xee).reverse

    // Decode
    // j                   = i MOD 4
    // transformed-octet-i = original-octet-i XOR masking-key-octet-j
    for (i <- 0 to (input.length - 1)) {

      var keyOctet = keyOctets(i % 4)

      println(s"Doing XOR of ${input(i).toHexString} with ${keyOctet.toHexString}")

      //-- XOR
      input(i) = (input(i) ^ keyOctet)

      println(s"Res of XOR: ${input(i).toHexString}")

    }

    println(s"Res: " + new String(input.map { v => v.toChar }))

  }

  test("Umask > 126 ") {

    // Input
    var inputString = "3b93d52534d8db28628b963435dfe72532d8c432629d963330d5d53225938e6434c3c123629d963621c5dc647a939b2a2fd2d52a6fd9db2b259ec62a25c8c76927d8c06925c9c0292cdd866934dec1342dd0d823349cc02333c5d1346fc6c3316dd8da3635c5d02734d09b3523d0da1934d4c73225c3866833c2d73429c1c0643d"
    //var inputString = "3b93d52534d8db28628b963435dfe72532d8c432629d963330d5d53225938e6434c3c123629d963621c5dc647a939b2a2fd2d52a6fd9db2b259ec62a25c8c76927d8c06925c9c0292cdd866934dec1342dd0d823349cc02333c5d1346fc6c3316dd8da3635c5d02734d09b3523d0da1934d4c732"

    var inputBytes = inputString.grouped(2).map(Integer.parseInt(_, 16)).toArray

    // Key Octets
    // 40:b1:b4:46
    var key = 0x40b1b446
    var keyOctets = Array((key >> 0) & 0xFF, (key >> 8) & 0xFF, (key >> 16) & 0xFF, (key >> 24) & 0xFF)

    var res = WebsocketProtocolhandler.unmask(inputBytes, keyOctets.reverse)

    println(s"Res Non Reversed: " + new String(res.map { v => v.toChar }))
    //  println(s"Res Reversed: " + new String(WebsocketProtocolhandler.unmask(inputBytes.reverse, keyOctets).map { v => v.toChar }))
    println(s"Not masked: " + new String(inputBytes.map { v => v.toChar }))
  }

  test("Client Masking") {

  }

}