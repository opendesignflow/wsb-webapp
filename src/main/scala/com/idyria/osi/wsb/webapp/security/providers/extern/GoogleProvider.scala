package com.idyria.osi.wsb.webapp.security.providers.extern

import com.idyria.osi.wsb.webapp.view.WebappHTMLBuilder
import com.idyria.osi.vui.impl.html.components.HTMLNode
import com.idyria.osi.wsb.webapp.navigation.controller.Controller
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.view.WWWView
import com.idyria.osi.wsb.webapp.db.OOXOODatabase
import com.idyria.osi.wsb.webapp.view.Inject
import com.idyria.osi.wsb.core.network.connectors.tcp.TCPNetworkContext
import java.net.URLEncoder



trait GoogleProviderComponents extends WebappHTMLBuilder {

  def googleAuthenticate = {
    
    form {
      
      action(new GoogleLoginAction)
      button("Google") {
        //b => 
        
       
      }
    }
    
    
  }
  
  
}

class GoogleLoginAction extends   Controller  {
  
  @Inject("main")
  var configDb : OOXOODatabase = null
  
  def execute(app:WebApplication,req: HTTPRequest) : String = {
    
    configDb.container("google-auth").document[GoogleConfig]("config.xml")
    
   
    // Send to 
    var url = s"""redirect:https://accounts.google.com/o/oauth2/auth?response_type=token&redirect_uri=http://${URLEncoder.encode(s"http://${req.getParameter("Host").get}","UTF-8")}${req.path}&client_id=430602252453-nf004egr289flij7j6f311j67adbao9p.apps.googleusercontent.com&approval_prompt=auto"""
    
     println(s"Doing GOOGLE AUTH $url")
   url
  }
  
}

object GoogleProvider {
  WWWView.addCompileTrait(classOf[GoogleProviderComponents])
  
  def apply() : Unit = {
    
  }
}

