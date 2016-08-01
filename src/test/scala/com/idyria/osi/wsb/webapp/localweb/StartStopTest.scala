package com.idyria.osi.wsb.webapp.localweb

import org.scalatest.FunSuite
import com.idyria.osi.wsb.webapp.resources.ResourcesIntermediary
import java.net.URL
import java.net.HttpURLConnection
import java.net.SocketTimeoutException

class TestView extends LocalWebHTMLVIew {

}

class StartStopTest extends FunSuite {

  test("Start Stop Test") {

    LocalWebEngine.addViewHandler("/", classOf[TestView])
    LocalWebEngine.lInit
    LocalWebEngine.lStart

    //-- Get Resource
    ResourcesIntermediary.addFilesSource("src/test/resources")
    var rURL = new URL(s"http://localhost:${LocalWebEngine.httpConnector.port}/resources/testext.txt")
    var conn = rURL.openConnection().asInstanceOf[HttpURLConnection]
    conn.setReadTimeout(1000)
    var resp = conn.getResponseCode
    conn.disconnect()
    
    println(s"Response: " + resp)
    assertResult(200)(resp)

    //-- Stop, should get connection timeout
    LocalWebEngine.lStop
    rURL = new URL(s"http://localhost:${LocalWebEngine.httpConnector.port}/resources/testext.txt")
    conn = rURL.openConnection().asInstanceOf[HttpURLConnection]
    conn.setReadTimeout(1000)
    intercept[SocketTimeoutException] {
      resp = conn.getResponseCode
      println(s"Response2: " + resp)
    }
    
    //-- Start, should work again
     LocalWebEngine.lStart
    rURL = new URL(s"http://localhost:${LocalWebEngine.httpConnector.port}/resources/testext.txt")
    conn = rURL.openConnection().asInstanceOf[HttpURLConnection]
    conn.setReadTimeout(1000)
    resp = conn.getResponseCode
    conn.disconnect()
    
    println(s"Response: " + resp)
    assertResult(200)(resp)
  }

}