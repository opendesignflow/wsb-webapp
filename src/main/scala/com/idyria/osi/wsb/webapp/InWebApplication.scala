package com.idyria.osi.wsb.webapp

import com.idyria.osi.tea.listeners.ListeningSupport
import com.idyria.osi.wsb.core.broker.tree.Intermediary
import com.idyria.osi.wsb.webapp.http.message.HTTPMessage
import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary

trait InWebApplication extends HTTPIntermediary with ListeningSupport {

  //-- Required context webapplication
  var application: WebApplication = null

  //-- Register appplication when added a new parent
  this.on("parent.new") {

    //-- Search for parent of type webapplication
    findParentOfType[WebApplication] match {
      case Some(application) => this.application = application
      case None => throw new RuntimeException(s"InWebApplication trait requires the intermediary ${name} to be inserted in the subtree of a WebApplication")
    }
  }

}