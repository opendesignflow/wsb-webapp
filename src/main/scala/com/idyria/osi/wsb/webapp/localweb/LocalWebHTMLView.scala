package com.idyria.osi.wsb.webapp.localweb

import java.io.File
import org.w3c.dom.html.HTMLElement
import com.idyria.osi.vui.core.view.AView
import com.idyria.osi.vui.core.view.AViewCompiler
import com.idyria.osi.vui.html.HTMLNode
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.vui.html.basic.DefaultBasicHTMLBuilder._
import java.net.URI
import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import com.idyria.osi.wsb.core.broker.tree.single.SingleMessageIntermediary
import com.idyria.osi.wsb.core.message.Message
import scala.reflect.ClassTag
import java.util.prefs.Preferences
import java.util.prefs.PreferenceChangeListener
import java.util.prefs.PreferenceChangeEvent
import com.idyria.osi.wsb.core.broker.tree.single.SingleMessage

// with StandaloneHTMLUIBuilder
class LocalWebHTMLVIew extends AView[HTMLElement, HTMLNode[HTMLElement, HTMLNode[HTMLElement, _]]] with DefaultLocalWebHTMLBuilder {

  this.currentView = this

  override def clone = {
    getClass.newInstance()
  }

  // Send backend Message
  //------------------------
  def sendBackendMessage(elt: ElementBuffer) = {
    getTopParentView.@->("soap.send", elt)
  }
  def broadCastBackendMessage(elt: ElementBuffer) = {
    getTopParentView.@->("soap.broadcast", elt)
  }

  def getViewResourcePath(str: String): URI = {
    new URI((getTopParentView.asInstanceOf[LocalWebHTMLVIew].viewPath + s"/resources/${viewPath}/resources/$str").noDoubleSlash)
  }

  // Receive message
  def singleMessageUpdateText[MT <: SingleMessage](id: MT => String, text: MT => String, onlyLast: Boolean = false, delayMS: Long = 100)(implicit tag: ClassTag[MT]) = {

    LocalWebEngine.broker <= new SingleMessageIntermediary[MT] {
      this.onDownMessage {
        update =>

          LocalWebHTMLVIew.this.request match {
            case Some(_) if (onlyLast == false || (onlyLast && update.lastInTrain)) =>

              //println(s"Sending  update from " + this)
              var updateText = new UpdateText
              updateText.id = id(update)
              updateText.text = text(update)
              sendBackendMessage(updateText)

              Thread.sleep(delayMS)

            case None =>
          }

      }
    }
  }
  def singleMessageReceive[MT <: SingleMessage](onlyLast: Boolean = false, delayMS: Long = 100)(cl: (MT => Unit))(implicit tag: ClassTag[MT]) = {
    LocalWebEngine.broker <= new SingleMessageIntermediary[MT] {
      this.onDownMessage {
        message =>

          LocalWebHTMLVIew.this.request match {
            case Some(_) if (onlyLast == false || (onlyLast && message.lastInTrain)) =>

              cl(message)

              Thread.sleep(delayMS)

            case None =>
          }

      }
    }
  }

  override def bindToPreference[V](p: Preferences, key: String, default: V)(cl: V => Unit)(implicit tag: ClassTag[V]): Unit = {

    var idC = (p.absolutePath() + "/" + key).replace("/", "_").replace(".", "_")
    super.bindToPreference[V](p, key, default) {
      v =>
        id(idC)
        cl(v)
    }

    //-- Listen on bound value
    p.addPreferenceChangeListener(new PreferenceChangeListener {
      def preferenceChange(evt: PreferenceChangeEvent) = {

        if (evt.getKey == key) {

          //println(s"Updating Bound value of " + idC)

          //-- Update
          var updateText = new UpdateText
          updateText.id = idC
          updateText.text = evt.getNewValue
          sendBackendMessage(updateText)

        }
      }
    })

  }

}

object LocalWebHTMLVIewCompiler extends AViewCompiler[HTMLElement, LocalWebHTMLVIew] {

  /*val eout = new File("target/web-classes")
  eout.mkdirs()
  compiler.settings2.outputDirs.setSingleOutput(eout.getAbsolutePath)*/

  this.tempSourceFolder = new File("target/localweb-sources")
  this.outputClassesFolder = new File("target/localweb-classes")

  this.initCompiler

}