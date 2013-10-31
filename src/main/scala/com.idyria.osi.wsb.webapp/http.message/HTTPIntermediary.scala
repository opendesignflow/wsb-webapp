package com.idyria.osi.wsb.webapp.http.message

import com.idyria.osi.wsb.core.broker.tree.MessageIntermediary

class HTTPIntermediary extends MessageIntermediary[HTTPRequest] {

  def messageType = classOf[HTTPRequest]

}