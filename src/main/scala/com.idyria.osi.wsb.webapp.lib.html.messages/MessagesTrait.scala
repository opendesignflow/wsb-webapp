package com.idyria.osi.wsb.webapp.lib.html.messages

import com.idyria.osi.wsb.webapp.view.WebappHTMLBuilder

/**
 * Trait that offers utility functions to handle application messages
 */
trait MessagesBuilder extends WebappHTMLBuilder {

  /**
   * Handle Errors
   */
  def errors = {

    div {
      id("errors")

      /**
       * Consume Errors
       */
      request.consumeErrors {
        error =>

          //-- Produce message div for current error
          div {
            classes("alert alert-danger")
            text(error.getMessage())
          }
      }
    }

  }

}