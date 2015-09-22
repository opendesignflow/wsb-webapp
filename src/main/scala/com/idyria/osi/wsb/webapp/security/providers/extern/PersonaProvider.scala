package com.idyria.osi.wsb.webapp.security.providers.extern

import com.idyria.osi.wsb.webapp.view.WebappHTMLBuilder
import com.idyria.osi.wsb.webapp.view.WWWView

trait PersonaProviderComponents extends WebappHTMLBuilder {

  override def head(cl: => Any) = {
    super.head {
      cl

      script {
        attribute("src" -> "https://login.persona.org/include.js")
      }

    }
  }

  def personaSigninButton = {

    this.img {
      attribute("src" -> "components/persona/persona-only-signin-link.png")
      attribute("id" -> "sign-in")
    }

  }

  def personaLoad() = {
    
    // Load interface
    script {
      attribute("src" -> "components/persona/persona-interface.js")
    }
    
    // Loadpersona 
    jsOnLoad("""persona_load();""")
  }

}

object PersonaProvider {
  WWWView.addCompileTrait(classOf[PersonaProviderComponents])

  def apply(): Unit = {

  }
}