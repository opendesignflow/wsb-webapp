package com.idyria.osi.wsb.webapp

import javax.faces.bean.ManagedBean

trait NamedBean {

  /**
   *
   * The action string is used to map the controler to an "action" request parameter value
   * So that we can call the right action when an HTTP request comes
   */
  var name = ""

  // Action Is per default:
  //  - The name of managed bean Annotation if one is present
  //  - The class name
  //---------
  getClass().getAnnotation(classOf[ManagedBean]) match {
    case null       => name = getClass.getSimpleName
    case annotation => name = annotation.name()
  }

}