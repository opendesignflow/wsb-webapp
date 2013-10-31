package com.idyria.osi.wsb.webapp.navigation

import com.idyria.osi.wsb.webapp.http.message._
import com.idyria.osi.wsb.webapp._

/**
 *
 * A common trait type for NavigationRules
 * A Rule performs some checks on the request and returns true or false if valid or not
 *
 * The webapplication is then configured to redirect to the correct view
 */
trait NavigationRule {

  /**
   * The View ID String to which we are going if the rule fails
   */
  var outcome = ""

  def evaluate(application: WebApplication, request: HTTPRequest): Boolean

}