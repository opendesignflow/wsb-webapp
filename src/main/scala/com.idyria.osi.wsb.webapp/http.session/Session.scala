package com.idyria.osi.wsb.webapp.http.session

import com.idyria.osi.wsb.webapp.http.message._

/**
    Persistent memory accross requests for a user
*/
class Session(var id : String ) {

    /**
        Validity is always for now 30minutes
    */
    var validity = new java.util.GregorianCalendar
    validity.add(java.util.Calendar.MINUTE,30)

    var values = Map[String,Any]()

    /**
        Add Value to session
    */
    def apply( value: (String, Any)) = {

        println(s"[Session] Storing value to id $id and instance ${hashCode}")
        values = values + value

    }

    def apply( name: String) : Option[Any] = {

        println(s"[Session] Searching value from id $id and instance ${hashCode}")
        this.values.get(name)
    }

    /**

    */
    def validityString : String = String.format("c", this.validity);

} 

object Session {

    var sessions = Map[String,Session]()

    var longGenerator = new com.idyria.osi.tea.random.UniqueLongGenerator

    /**
        Creates a new Session, or returns an existing one for the user
    */
    def apply(message: HTTPRequest) : Session = {

        // Return if existing
        if (message.session!=null)
            return message.session

        // Return if already in map
        message.cookies.get("SSID") match {
            case Some(ssid) => 
                
                println(s"[Session] Looking for existing session id $ssid")
                sessions.get(ssid) match {
                    case Some(session) =>

                        println(s"[Session] ... Found")

                        return session 
                    case None =>
                }
            case None =>
        }

        // Try to find Session from HTTP Parameters or create

        // Try to create an ID
        //----------
        var newSession = new Session(com.idyria.osi.tea.hash.Base64.encodeBytes(com.idyria.osi.tea.hash.HashUtils.hashBytes(Array(longGenerator.generate.toByte),"SHA-1")))

        sessions = sessions + (newSession.id -> newSession)

        println(s"[Session] Saving new session id ${newSession.id} and instance ${newSession.hashCode}")

        newSession
    }
}