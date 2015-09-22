package com.idyria.osi.wsb.webapp.http.session

import com.idyria.osi.wsb.core.broker.tree.MessageIntermediary
import com.idyria.osi.wsb.core.broker.tree.Intermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPResponse

class CookieIntermediary extends MessageIntermediary[HTTPResponse] {

  def messageType = classOf[HTTPResponse]
  /**
   * Save Cookied on Response
   */
  /* downClosure = {
    case msg: HTTPResponse =>
  }*/

}