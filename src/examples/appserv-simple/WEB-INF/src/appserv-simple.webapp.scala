package wsb.webapp.examples.simple

import com.idyria.osi.wsb.webapp.appserv._

import com.idyria.osi.wsb.webapp.view.WWWView
import com.idyria.osi.wsb.webapp.lib.html.bootstrap.BootstrapBuilder

class SimpleWebapp extends AIBWebapp {
  
  
  onInit {
    
    println(s"******************* INIT *********************")
    WWWView.addCompileTrait(classOf[BootstrapBuilder])
    
  }
  
}
