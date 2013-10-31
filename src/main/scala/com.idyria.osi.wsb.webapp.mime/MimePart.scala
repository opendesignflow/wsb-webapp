package com.idyria.osi.wsb.webapp.mime

import scala.Array.canBuildFrom
import com.idyria.osi.tea.logging.TLogSource

/**
 * Data Type to gatehr data as MimePart
 *
 * a MIME part can contain some parameters like content type or length, and also some pure content
 *
 */
trait MimePart extends TLogSource {

  var protocolLines = List[String]()

  var parameters = Map[String, String]()

  val parameterLineRegexp = """([\w-]+): (.+)""".r

  // Merged parts
  //-------------------
  var nextParts = List[MimePart]()

  def append(part: MimePart) = {
    nextParts = part :: nextParts
  }

  // Content
  //-------------
  var contentLength = 0

  var bytes = Array[Byte]()

  // Copy MimeParts
  //------------------------
  def apply(part: MimePart) = {

    // Parameters
    part.parameters.foreach {
      case (key, value) ⇒ this.addParameter(key, value)
    }
    //this.parameters = this.parameters ++ part.parameters

    // Bytes
    this.bytes = part.bytes

    // Append Next parts
    part.nextParts.foreach {
      p ⇒ this.append(p)
    }
  }

  // Parameters
  //---------------------

  /**
   * Record a parameter in internal parameter map
   * Parameters are read from or written as lines like:
   *
   *  NAME: VALUE
   */
  def addParameter(name: String, value: String): Unit = parameters = parameters + (name -> value)

  def addParameter(line: String): Unit = {

    //println(s"Mime Line: $line")

    this.contentLength += line.getBytes.size

    parameterLineRegexp.findFirstMatchIn(line) match {

      case Some(matched) ⇒

        var (name, value) = (matched.group(1), matched.group(2))

        logFine(s"Param: $name -> $value")

        this.addParameter(name, value)
      case None ⇒

        logFine(s"Protocol Line: $line")

        protocolLines = line :: protocolLines
    }

  }

  // Content
  //----------------
  def +=(inputBytes: Array[Byte]): Unit = {
    this.bytes = this.bytes ++ inputBytes
    this.contentLength += inputBytes.size
  }

  // Parameters Exploration to find data
  //-------------------------------------------

  /**
   * Search in parameters like this:
   *
   * Content-Disposition: form-data; name="Filedata"; filename="javafx-2_2_25-apidocs.zip"
   */
  def getFileName(): Option[String] = {

    this.parameters.get("Content-Disposition") match {

      case Some(value) ⇒

        println(s"-> Found Content Disposition: $value")
        """.+; filename="(.+)"\s*""".r.findFirstMatchIn(value) match {
          case Some(matched) ⇒ Option(matched.group(1))
          case None          ⇒ None
        }

      case None ⇒ None

    }

  }

}

class DefaultMimePart extends MimePart {

}