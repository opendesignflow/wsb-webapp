package com.idyria.osi.wsb.webapp.http

import org.scalatest.FunSuite
import org.scalatest.GivenWhenThen
import scala.io.Source
import com.idyria.osi.wsb.webapp.http.connector.HTTPProtocolHandler
import com.idyria.osi.wsb.core.network.connectors.tcp.TCPNetworkContext
import java.nio.ByteBuffer
import com.idyria.osi.tea.logging.TLog
import com.idyria.osi.wsb.webapp.mime.MimePart
import com.idyria.osi.wsb.webapp.mime.DefaultMimePart
import com.idyria.osi.wsb.webapp.http.message.HTTPMessage
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest

class HTTPContentTypeTest extends FunSuite with GivenWhenThen {

  test("ChunkedEncoding one pass") {

   // TLog.setLevel(classOf[HTTPProtocolHandler], TLog.Level.FULL)

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

    //TLog.setLevel(classOf[HTTPProtocolHandler], TLog.Level.FULL)

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
  
  test("Fast parse Multipart") {
    
    var mp = new DefaultMimePart
    mp.addParameter("POST /DigitalCamp/admin/registrations.view HTTP/1.1")
    mp.addParameter("Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryu8DtH1NskBwNsPoc")
   
    
    var string = """
------WebKitFormBoundaryu8DtH1NskBwNsPoc
Content-Disposition: form-data; name="form"; filename="MusicCamp15RegForm.3_responses.xml"
Content-Type: text/xml

<?xml version="1.0" encoding="UTF-8"?>
<Data>
  <!--MusicCamp15RegForm.Gregtest.pdf -->
  <fields xmlns:xfdf="http://ns.adobe.com/xfdf-transition/">
    <Age>61</Age>
    <Banj5>1</Banj5>
    <Banj5Level xfdf:original="Banj5 Level">B</Banj5Level>
    <Banjo>Off</Banjo>
    <BanjoLevel xfdf:original="Banjo Level">0</BanjoLevel>
    <Bass>1</Bass>
    <BassLevel xfdf:original="Bass Level">B</BassLevel>
    <Bodhran>Off</Bodhran>
    <BodhranLevel xfdf:original="Bodhran Level">0
    </BodhranLevel>
    <City>St. Peter's</City>
    <Country>Canada</Country>
    <Email>greg@gregsilver.ca</Email>
    <Fees>220</Fees>
    <Fiddle>1</Fiddle>
    <FiddleLevel xfdf:original="Fiddle Level">I</FiddleLevel>
    <Firstname xfdf:original="First name">Greg</Firstname>
    <Guitar>Off</Guitar>
    <GuitarLevel xfdf:original="Guitar Level">I</GuitarLevel>
    <Lastname xfdf:original="Last name">Silver</Lastname>
    <Luthier>1</Luthier>
    <LuthierLevel xfdf:original="Luthier Level">B
    </LuthierLevel>
    <MandoLevel xfdf:original="Mando Level">0</MandoLevel>
    <Mandolin>1</Mandolin>
    <MusicC xfdf:original="Music C">1</MusicC>
    <MusicR xfdf:original="Music R">1</MusicR>
    <MusicCLevel xfdf:original="MusicC Level">B</MusicCLevel>
    <MusicRLevel xfdf:original="MusicR Level">I</MusicRLevel>
    <Othersugg xfdf:original="Other sugg">Saxophone</Othersugg>
    <Phone>902 631 5050</Phone>
    <Piano>1</Piano>
    <PianoLevel xfdf:original="Piano Level">Off</PianoLevel>
    <field xfdf:original="Pos/Zip">B0E 3B0</field>
    <Prov>NS</Prov>
    <SingV>1</SingV>
    <SingVLevel xfdf:original="SingV Level">B</SingVLevel>
    <SongW>Off</SongW>
    <SongWLevel xfdf:original="SongW Level">0</SongWLevel>
    <SqDan>Off</SqDan>
    <SqDanLevel xfdf:original="SqDan Level">0</SqDanLevel>
    <field xfdf:original="Street/PO Box">PO Box 178</field>
    <Ukulele>Off</Ukulele>
    <UkuleleLevel xfdf:original="Ukulele Level">0
    </UkuleleLevel>
    <Whistle>Off</Whistle>
    <WhistleLevel xfdf:original="Whistle Level">0
    </WhistleLevel>
  </fields>
</Data>

------WebKitFormBoundaryu8DtH1NskBwNsPoc
Content-Disposition: form-data; name="eaction"

/DigitalCamp/admin/registrations.view@form.upload
------WebKitFormBoundaryu8DtH1NskBwNsPoc--

"""
    
    mp+=string.getBytes
    
    // Create HTTP Message
    var msg = HTTPRequest.build(mp)
    
    // Check
    assertResult(Some("/DigitalCamp/admin/registrations.view@form.upload"))(msg.getURLParameter("eaction"))
    
    
    
  }

}