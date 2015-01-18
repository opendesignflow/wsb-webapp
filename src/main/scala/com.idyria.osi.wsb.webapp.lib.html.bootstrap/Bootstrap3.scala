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
package com.idyria.osi.wsb.webapp.lib.html.bootstrap

import com.idyria.osi.vui.impl.html.HtmlTreeBuilder
import com.idyria.osi.vui.core.components.scenegraph.SGNode
import com.idyria.osi.wsb.webapp.view.WebappHTMLBuilder
import com.idyria.osi.vui.core.components.scenegraph.SGGroup
import com.idyria.osi.vui.impl.html.components.HTMLNode
import com.idyria.osi.vui.impl.html.components.Div
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.vui.impl.html.components.HTMLTextNode
import com.idyria.osi.vui.impl.html.components.Table
import com.idyria.osi.vui.core.components.table.TableBuilderInterface
import com.idyria.osi.vui.core.components.table.SGTable
import com.idyria.osi.wsb.webapp.navigation.GroupTrait
import com.idyria.osi.wsb.webapp.navigation.GroupTraitView

/**
 * Inject points
 */
class TopNavbar extends Div with HtmlTreeBuilder {

  type Self = TopNavbar

  def and(cl: TopNavbar ⇒ Unit): TopNavbar = {
    cl(this)
    this
  }

  def header(cl: ⇒ HTMLNode) {

    this.searchByName("header") match {
      case Some(h) ⇒ h.@->("content", cl)
      case None ⇒
    }

  }

  /**
   * Creates Navbar Menus from Application Navigation, and add them to navbar placeholder
   */
  def menusFromNavigation(app: WebApplication) = {

    // Create Menus
    //-------------------

    //-- Base recursive function
    def navElementToNode(elt: Any): HTMLNode = {

      elt match {

        //-- Group
        //----------------
        case g: GroupTrait ⇒

          // Create Group as List item
          //------------
          //var groupItem = li

          // Group View id is specified in attribute
          //--------
          var groupLink = g.view match {
            case null ⇒ "#"
            case v ⇒ g.fullPath + "/" + v
          }

          // Do Sub Content
          //---------
          var groupContent = (g.views.size + g.groups.size) match {

            case 0 ⇒ ""
            case _ ⇒

              ""
            // List(g.views.map(v => navElementToString(v)).mkString,g.groups.map(navElementToString(_)).mkString).mkString

          }
          // Gather in this group content
          //--------
          li {
            // Link
            a(g.name, groupLink)

            // Content
            (g.views.size + g.groups.size) match {

              // No content
              case 0 ⇒

              // Content -> do 
              case _ ⇒

                g.views.map(v ⇒ navElementToNode(v)).foreach {
                  elt ⇒ add(elt)
                }
                g.groups.map(v ⇒ navElementToNode(v)).foreach {
                  elt ⇒ add(elt)
                }

              // List(g.views.map(v => navElementToString(v)).mkString,g.groups.map(navElementToString(_)).mkString).mkString

            }

          }

        //-- View
        //------------------- 
        case v: GroupTraitView ⇒ li {

          a(v.name, v.fullPath)
        }

        // NO supported, just empty text then
        case _ ⇒ span("Unsupported Node")

      }

    }

    // Build Menu and add it to content
    this.searchByName("menu") match {
      case Some(m) ⇒

        var res = app.navigationConfig.views.map(v ⇒ navElementToNode(v)).toList ::: app.navigationConfig.groups.map(navElementToNode(_)).toList
        m.@->("content", res)

      //println("Adding menu content: " + res)

      case None ⇒
    }

  }

}

trait BootstrapBuilder extends HtmlTreeBuilder {

  
  
  // Init
  //---------------
  
  override def head(cl: => Any) = {
    
    super.head {
      importbs3
      cl
    }
  }
  def importbs3 = {

    meta {
      attribute(" http-equiv" -> "X-UA-Compatible")
      attribute("content" -> "IE=edge")
    }
    meta {

      attribute("name" -> "viewport")
      attribute("content" -> "width=device-width, initial-scale=1.0")
    }
    stylesheet("//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css")
    stylesheet("//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-theme.min.css")

    script {
      attribute("src" -> "//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js")
    }

  }

  // Form
  //----------------
  def bs3Form(cl: ⇒ Any) = {

    form {
      attribute("role" -> "form")
      attribute("method" -> "POST")
      cl
    }
  }

  def bs3FormGroup(cl: ⇒ Any) = {

    div {
      classes("form-group")
      cl
    }
  }

  def bs3PlaceHolder(text:String) = {
    attribute("placeHolder"->text)
  }
  
  // Grid
  //----------------

  def bs3Row(cl: ⇒ Any) = {

    div {
      classes("row")
      cl
    }

  }

  def bs3Col1(cl: ⇒ Any) = {

    div {
      classes("col-md-1")
      cl
    }

  }
  def bs3Col4(cl: ⇒ Any) = {

    div {
      classes("col-md-4")
      cl
    }

  }
  def bs3Col6(cl: ⇒ Any) = {
    div {
      classes("col-md-6")
      cl
    }
  }

  def bs3Col8(cl: ⇒ Any) = {
    div {
      classes("col-md-8")
      cl
    }
  }

  def bs3Col9(cl: ⇒ Any) = {
    div {
      classes("col-md-9")
      cl
    }
  }

  def bs3Col12(cl: ⇒ Any) = {
    div {
      classes("col-md-12")
      cl
    }
  }

  // Tables
  //-----------
  def bs3Table[OT](cl: BS3Table[OT] => Any): BS3Table[OT] = {

    var r = switchToNode(new BS3Table[OT], {})
    // r("class" -> "table")

    cl(r)
    r
  }

  // Button Groups
  //-------------------

  def bs3SingleDropdownButton(name: String, btnType: String = "default")(cl: => Any) = {

    div {
      attribute("class" -> "btn-group")

      //-- Add Button
      button(name) {
        //b =>
          attribute("class" -> s"btn btn-${btnType} dropdown-toggle")
          attribute("data-toggle" -> "dropdown")

          // Add Caret
          span {
            classes("caret")
          }
      }

      //-- Add Menu
      ul {
        classes("dropdown-menu")
        attribute("role" -> "menu")

        cl

      }
    }

  }

  def bs3Action(name: String, action: String = "#") = {

    li {
      a(name, action)
    }
  }
  
  
  // Styled elements
  //-----------------------
  def bs3Header(cl: => Any) = {
    div {
      classes("page-header")
      cl
    }
  }

}

class BS3Table[OT] extends Table[OT] {

  this("class" -> "table")

  def condensed = {
    this.attributeAppend("class", "table-condensed")
    this
  }
  def bordered = {
    this.attributeAppend("class", "table-bordered")
    this
  }

  def hover = {
    this.attributeAppend("class", "table-hover")
    this
  }
}

/*
trait BS3TableBuilder extends TableBuilderInterface[Any] {

  def table[OT]: SGTable[OT, Any] = {

    return new BS3Table[OT] {

    }

  }

}*/

object Bootstrap3 extends HtmlTreeBuilder {

  def stylesheets(nd: SGGroup[Any]): Unit = {

    nd <= meta {
      attribute(" http-equiv" -> "X-UA-Compatible")
      attribute("content" -> "IE=edge")
    }
    nd <= meta {

      attribute("name" -> "viewport")
      attribute("content" -> "width=device-width, initial-scale=1.0")
    }
    nd <= stylesheet("http://netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css")
    nd <= stylesheet("http://netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-theme.min.css")

  }

  def scripts(nd: SGGroup[Any]): Unit = {

    nd <= script {
      attribute("src" -> "//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js")
    }

  }

  /**
   * Inject points
   */
  def topNavBar: TopNavbar = {

    // Top navbar
    var top = new TopNavbar
    this.switchToNode(top, {
      classes("navbar navbar-inverse navbar-fixed-top")

      // Container
      div {
        classes("container")

        // Navbar Header
        //---------------------
        div {
          classes("navbar-header")

          currentNode.setName("header")
          currentNode.waitFor("content")

        }

        // Menus
        //-------------------
        div {
          classes("collapse navbar-collapse")
          ul {
            classes("nav navbar-nav")
            currentNode.setName("menu")
            currentNode.waitFor("content")
          }

        }

        // Right part
        //------------------------
        ul {
          classes("nav navbar-nav navbar-right")
        }
      }

    })

    top

  }

}