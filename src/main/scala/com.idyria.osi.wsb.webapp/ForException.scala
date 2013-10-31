package com.idyria.osi.wsb.webapp

/**
 * The For exception is designed for the application to provide a reference to an element that have caused an error
 *
 * Typically, it will be used by form validation to give a Hint to the client about which input element failed to validate
 *
 */
class ForException(var target: String, c: Throwable) extends RuntimeException(c) {

}