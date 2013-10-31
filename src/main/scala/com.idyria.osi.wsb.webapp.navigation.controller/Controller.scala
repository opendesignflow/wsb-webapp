package com.idyria.osi.wsb.webapp.navigation.controller

import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.WebApplication
import javax.faces.bean.ManagedBean
import com.idyria.osi.wsb.webapp.NamedBean

/**
 * A trait implemented by All navigation Controlers
 */
trait Controller extends NamedBean {

  /**
   * Execute the Controller, and return the navigation outcome
   */
  def execute(application: WebApplication, request: HTTPRequest): String

}
/**
 *
 * This trait is to be mixed by classes that want to have multiple methods be mapped as action controler.
 *
 * Each method having the following format:
 *
 * def methodName(application: WebApplication, request: HTTPRequest): String
 *
 * will be seen as an action executor under the name:
 *
 * name.of.action.controller.methodName
 *
 *
 */
trait ActionsControllerHandler extends Controller {

}
