package com.idyria.osi.wsb.webapp

import com.idyria.osi.wsb.core.network.Network
import com.idyria.osi.wsb.core.network.connectors.HTTPConnector
import com.idyria.osi.wsb.core.WSBEngine

object HTTPTry extends App {

  println("Starting HTTP Try")
  
  // Register HTTP Connetor in Network
  Network.connectors += new HTTPConnector(7890)
  
  
  // Start Engine
  //----------------
  WSBEngine.lStart
  
  println("Press Enter to stop")
  Console.readLine
  
  WSBEngine.lStop
}