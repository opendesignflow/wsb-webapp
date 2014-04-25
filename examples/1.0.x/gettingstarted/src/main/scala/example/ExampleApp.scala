package example

import com.idyria.osi.wsb.core.WSBEngine
import com.idyria.osi.wsb.webapp.http.connector._
import com.idyria.osi.wsb.webapp.WebApplication

import java.io.File

import com.idyria.osi.wsb.webapp.MavenProjectWebApplication

import com.idyria.osi.tea.logging.TLog

object ExampleApp extends App {
  
  println("Hello World")
  
  //TLog.setLevel(classOf[WebApplication],TLog.Level.FULL)
  
  // Create a WSB engine (network I/O + messagebroker)
  //-----------------
  var engine = new WSBEngine()
  
  // Add the HTTP Connector on for 8087
  //-------------
  var port = 8087
  var connector = HTTPConnector(port)
  
  //-- Add to engine
  engine.network.addConnector(connector)

  // Create Web application
  //-----------
  var path = "/ExampleApp"
  var application = new MavenProjectWebApplication(new File("./pom.xml"), path)

  // Add it to message broker
  //----------------
  engine.broker <= application
    
  // Start the engine
  //----------
  engine.lInit
  engine.lStart
  
  // Stop the engine
  //------------
  println(s"Started on http://localhost:$port$path, press any key to stop....")
  Console.readLine
  engine.lStop
  
}