package com.idyria.osi.wsb.webapp.view.markdown

import org.markdown4j.Markdown4jProcessor
import com.idyria.osi.wsb.webapp.InWebApplication
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPIntermediary
import com.idyria.osi.wsb.webapp.view.WWWView
import com.idyria.osi.wsb.webapp.view.WWWViewCompiler
import java.net.URLEncoder
import com.idyria.osi.wsb.webapp.http.message.HTTPResponse

class MarkdownViewIntermediary extends HTTPIntermediary with InWebApplication {

  filter = """http:.*\.md:.*""".r
  name = "Markdown View"

  var mdProcessor = new Markdown4jProcessor()

  var viewCompiler = new WWWViewCompiler()

  WWWView.addCompileTrait(classOf[MarkdownBuilder])
  
  this.onDownMessage { request =>

    //-- Search Resource
    this.application.searchResource(request) match {
      case Some(resource) =>

        //-- Read String as lines
        var lines = scala.io.Source.fromURL(resource, "UTF-8").getLines()

        //-- Line parser
        //---------------

        // Parts accumulator
        var parts = scala.collection.mutable.HashMap[String, scala.collection.mutable.ListBuffer[String]]()
        var currentPart = ""

        // Config parameters
        var configuration = scala.collection.mutable.HashMap[String, String]()

        // Composition base
        var compose: Option[String] = None

        lines.foreach {

          //-- Config line
          case line if (line.trim().startsWith("---")) =>

            // Config params
            var splitted = line.trim().replace("---", "").split(':')
            (splitted(0).trim, splitted.drop(1).mkString(":").trim()) match {

              case ("compose", value) => compose = Some(value)

              case ("part", part) => currentPart = part

              // Store as variable
              case (variable, value) => configuration += (variable -> value)
            }

          //-- Accumulate line to part
          case line => parts.getOrElseUpdate(currentPart, scala.collection.mutable.ListBuffer[String]()) += line

        }

        // TODO Resolve parts special content
        //-------------------

        // Convert parts to HTML
        //---------------------------
        var htmlparts = parts.mapValues { partcontent =>

          mdProcessor.process(partcontent.mkString(System.lineSeparator()));
        }

        // View Generation
        // -> Composing a view: Get view, and set parts
        // -> Otherwise render all parts after each other in response
        //--------------------
        compose match {
          case Some(view) =>
            // Search for view 
            this.application.searchResource(view) match {
              case Some(viewURL) =>

                // Compile
                var composedView = WWWView.compile(viewURL).getClass.newInstance()

                // Add parts
                htmlparts.foreach {
                  case (part, html) =>
                    composedView.part(part) {
                      (application, request) =>

                        composedView.div {
                          composedView.text(html)
                        }
                    }
                }

                // Render
                // Support various outputs
                //---------------------
                request.parameters.find(_._1 == "Accept") match {

                  // JSON
                  //---------------
                  case Some(Tuple2(_, v)) if (v.startsWith("application/json")) ⇒

                    var rendered = composedView.produce(application, request).toString()
                    var jsonRes = s"""{"content":"${URLEncoder.encode(rendered)}"}"""
                    response(HTTPResponse("application/json", jsonRes))

                  // Otherwise -> HTML
                  //------------
                  case _ =>
                    response(HTTPResponse("text/html", composedView.produce(application, request).toString()))
                }

              case None => throw new RuntimeException(s"Cannot compose Markdown view with $compose because the resource cannot be found")
            }
          case None =>
        }

      case None =>

      // No resource found, don't do anything
    }

  }

}