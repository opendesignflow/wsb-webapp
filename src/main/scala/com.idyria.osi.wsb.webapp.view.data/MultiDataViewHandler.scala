/*
 * #%L
 * WSB Webapp
 * %%
 * Copyright (C) 2013 - 2014 OSI / Computer Architecture Group @ Uni. Heidelberg
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.idyria.osi.wsb.webapp.view.data

import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary
import com.idyria.osi.wsb.webapp.view.ViewHandler
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import java.lang.reflect.Method
import com.idyria.osi.wsb.webapp.view.Inject

/**
 * This base data view handler can accept messages that map to a base Path under the main application URL
 *
 *  When created, it will look for local methods, and accept messages having the following request path:
 *
 *  applicationURL/basePath/localMethod
 *
 *
 *  # Method definition formats
 *
 *  The localMethod must follow one of those format:
 *
 *  - A Prototype definition requiring full request informations
 *
 *   	def localMethod(application:WebApplication,request:HTTPRequest) : Any
 *
 *  - A Prototype definition requiring only  the request
 *
 *   	def localMethod(request:HTTPRequest) : Any
 *
 *  - A Simpler method definition must be enriched by a @ViewHandler annotation, otherwise all methods of the implementing class would be callable:
 *
 *  	@ViewHandler
 *  	def localMethod(): Any
 *
 *
 * # Return Type
 *
 * The Return type can be:
 *
 * - Unit/Void/Nothing: The handler is responsible for calling response function
 * - SGNode of HTML sub type: Generate a string and set response type to html
 *
 *
 *
 */
abstract class MultiDataViewHandler(var basePath: String) extends HTTPIntermediary {

  // Fields
  //-------------

  @Inject("application")
  var application: WebApplication = null

  // Available methods list
  //----------------------
  var methods = Map[String, Method]()

  // base class for sub intermediaries
  abstract class MethodHandlerIntermediary(var method: Method) extends HTTPIntermediary {

    this.filter = "http:$basePath/$name:.*".r
    this.name = s"${MultiDataViewHandler.this.name} : $name "

  }

  // Init
  //---------------

  //-- Accept
  this.filter = "http:$basePath/.*:.*".r

  //-- Find All Methods
  getClass.getDeclaredMethods().foreach {
    m ⇒

      var viewHandlerAnnotation = m.getAnnotation(classOf[ViewHandler])

      m.getParameterTypes() match {

        //-- WebApplication + request
        //--------------------
        case array if (array.size == 2 && array(0) == classOf[WebApplication] && array(1) == classOf[HTTPRequest]) ⇒

          methods = methods + (m.getName() -> m)

          this <= new MethodHandlerIntermediary(m) {

            onDownMessage {
              request ⇒

                m.invoke(MultiDataViewHandler.this, Array(MultiDataViewHandler.this.application, request))

            }

          }

        //-- Request
        case array if (array.size == 1 && array(0) == classOf[HTTPRequest]) ⇒

          methods = methods + (m.getName() -> m)

          this <= new MethodHandlerIntermediary(m) {

            onDownMessage {
              request ⇒

                m.invoke(MultiDataViewHandler.this, Array(request))

            }
          }

        //-- Void + ViewHandler
        case array if (array.size == 0 && viewHandlerAnnotation != null) ⇒

          methods = methods + (m.getName() -> m)

          this <= new MethodHandlerIntermediary(m) {

            onDownMessage {
              request ⇒

                m.invoke(MultiDataViewHandler.this, Array[Any]())

            }
          }

        //-- Unsupported
        case _ ⇒

      }

  }

  //-- For Each add a sub intermediary
  //---------------
  /*methods.foreach {
    case (name, method) ⇒

      this <= new HTTPIntermediary {

        onDownMessage {
          request ⇒

            method.getParameterTypes()

        }

      }

  }*/

}