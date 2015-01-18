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
package com.idyria.osi.wsb.webapp.http.session

import com.idyria.osi.wsb.webapp.http.message._
import java.util.TimeZone
import com.idyria.osi.tea.logging.TLogSource

/**
 * Persistent memory accross requests for a user
 */
class Session(var id: String, var host: String) extends TLogSource {

  /**
   * The path must be set to the application base path
   */
  var path = "/"

  /**
   * Validity is always for now 30minutes
   */
  var validity = new java.util.GregorianCalendar(TimeZone.getTimeZone("GMT"))
  validity.add(java.util.Calendar.MINUTE, 30)

  var values = Map[String, Any]()

  /**
   * Add Value to session
   */
  def apply(value: (String, Any)) = {

    logFine(s"[Session] Storing value to id $id and instance ${hashCode}")
    values = values + value

  }

  def apply[T<:Any](name: String): Option[T] = {

    logFine(s"[Session] Searching value from id $id and instance ${hashCode}")
    this.values.get(name).asInstanceOf[Option[T]]
  }

  /**
   *
   */
  def validityString: String = String.format("%tc", this.validity);

}

object Session extends TLogSource {

  var sessions = Map[String, Session]()

  var longGenerator = new com.idyria.osi.tea.random.UniqueLongGenerator

  /**
   * Creates a new Session, or returns an existing one for the user
   */
  def apply(message: HTTPRequest): Session = {

    // Return if existing
    if (message.session != null)
      return message.session

    // Return if already in map
    message.cookies.get("SSID") match {
      case Some(ssid) ⇒

        logFine(s"[Session] Looking for existing session id $ssid")
        sessions.get(ssid) match {
          case Some(session) ⇒

            logFine(s"[Session] ... Found")

            return session
          case None ⇒
        }
      case None ⇒
    }

    // Try to find Session from HTTP Parameters or create

    // Determine Session Cookie Host
    //---------------
    var host = "localhost"
    (message.parameters.find(_._1 == "X-Forwarded-Host"), message.parameters.find(_._1 == "Host")) match {
      case (Some(forwaredHost), _) ⇒ host = forwaredHost._2
      case (None, Some(normalHost)) ⇒
        //host = normalHost.replaceAll(":[0-9]+", "")
        host = normalHost._2
      case _ ⇒
    }

    // Try to create an ID
    //----------
    var newSession = new Session(host = host, id = java.lang.Math.abs(longGenerator.generate).toString)

    sessions = sessions + (newSession.id -> newSession)

    logFine(s"[Session] Saving new session id ${newSession.id} for host $host and instance ${newSession.hashCode}")

    newSession
  }
}