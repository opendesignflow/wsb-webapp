/*-
 * #%L
 * WSB Webapp
 * %%
 * Copyright (C) 2013 - 2017 OpenDesignFlow.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
import org.scalatest.Ignore

class HTTPContentTypeTest extends FunSuite with GivenWhenThen {

    test("ChunkedEncoding one pass") {

        // TLog.setLevel(classOf[HTTPProtocolHandler], TLog.Level.FULL)

        //-- Read Response and print
        var bytes = Source.fromInputStream(getClass().getClassLoader.getResourceAsStream("stream.bin")).grouped(2).map {
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
        var bytes = Source.fromInputStream(getClass().getClassLoader.getResourceAsStream("stream.bin")).grouped(2).map {
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

    /*
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



  }*/

    test("Parse Multipart") {

        var content = """-----------------------------57052814523281
Content-Disposition: form-data; name="importFile"; filename="Template.xml"
Content-Type: text/xml

<?xml version="1.1"?>
<Calculation>
	<CalculationTemplate>

		<StaticParameters>
			<Parameter name="cost.hour" value="400"></Parameter>

		</StaticParameters>
		<RequiredParameters>
			<Parameter name="print.material.count" type="int">
				<Require name="print.material." prefix="true" type="count">

				</Require>
			</Parameter>

			<Parameter name="master.width" type="float">
			</Parameter>
			<Parameter name="master.height" type="float">
			</Parameter>
			<Parameter name="master.parts.total" type="int">
			</Parameter>

		</RequiredParameters>


		<Part name="Substrate">
			<Parameter name="parts.requested" value="1000"></Parameter>
			<Cost name="substrate.length">$master.height * Math.ceil($parts.requested /
				$master.parts.total)
			</Cost>
		</Part>

		<Part name="Setup">
			<Parameter name="print.tool.setup.time" value="0.5"></Parameter>
			<Parameter name="print.tool.resetup.meter" value="300"></Parameter>
			<Parameter name="print.tool.resetup.time" value="0.25"></Parameter>

			<Cost name="setup.print.tool.totalTime">$print.material.count*$print.tool.setup.time</Cost>
			<Cost name="resetup.totalTime">$print.tool.resetup.time* Math.ceil( ($substrate.length/1000)
				/ $print.tool.resetup.meter)</Cost>

			<Cost name="setup.cost">($setup.print.tool.totalTime + $resetup.totalTime) *
				$cost.hour </Cost>

		</Part>



		<Part name="Material">
		</Part>

		<Part name="Overhead">

		</Part>
	</CalculationTemplate>

	<DesignParameters>
		<Parameter name="print.material.count" value="4">

		</Parameter>
		<Parameter name="master.width" value="330">

		</Parameter>
		<Parameter name="master.height" value="410">

		</Parameter>

		<Parameter name="master.parts.total" value="42">
		</Parameter>
	</DesignParameters>

	<Evaluation name="Main" static="true">

	</Evaluation>

	<Evaluation name="PartLength" dynamic="true">
		<Sweep>
			<Name>parts.requested</Name>
			<Start>1</Start>
			<Stop>1000</Stop>
			<Step>100</Step>
		</Sweep>
		<TargetPart>
			<Name>Substrate</Name>
			<Key>substrate.length</Key>
		</TargetPart>
	</Evaluation>

	<Evaluation name="SetupCost" dynamic="true">
		<Sweep>
			<Name>parts.requested</Name>
			<Start>1</Start>
			<Stop>100000</Stop>
			<Step>100</Step>
		</Sweep>
		<TargetPart>
			<Name>Setup</Name>
			<Key>setup.cost</Key>
<!-- 			<Key>substrate.length</Key> -->
		</TargetPart>
	</Evaluation>


</Calculation>
-----------------------------57052814523281
Content-Disposition: form-data; name="_action"

1199427688.1212282582
-----------------------------57052814523281--

        """

        TLog.enableFull[HTTPMessage]

        var part = new DefaultMimePart
        part.addParameter("POST /test HTTP/1.1")
        part.addParameter("Content-Type: multipart/form-data; boundary=---------------------------57052814523281")

        println(s"L0: " + part.protocolLines)

        part += content.getBytes
        var req = HTTPRequest.build(part)
        println(s"Multiplart: " + req.isMultipart)
        println(s"Action: " + req.getURLParameter("_action"))

        //-- Check number of parts
        println(s"Other parts: " + req.nextParts)
        assertResult(2)(req.nextParts.size)

        //-- Part 1
        println(s"Part 1: " + new String(req.nextParts(0).bytes))
        println(s"Part 1: " + req.getPartForFileName("importFile"))

        val filePartDirect = req.nextParts(1)
        println(s"Part File Direct: " + filePartDirect.getParameter("Content-Type"))

        val filePart = req.getPartForFileName("importFile")
        assertResult(true)(filePart.isDefined)
        
       // println("Part File: " + new String(filePart.get.bytes))

    }

    test("Test POST Parameter with '") {

        var message = new HTTPRequest("POST", "/", "1.0")
        message.addParameter("Content-Type", "application/x-www-form-urlencoded")
        message += """_render=none&value=Gordon+C%C3%B4t%C3%A9's+Reel&_format=json""".getBytes("UTF-8")

        println("Parameter: " + message.getURLParameter("value"))

    }

}
