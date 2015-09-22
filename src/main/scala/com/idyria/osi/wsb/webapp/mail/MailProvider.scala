

package com.idyria.osi.wsb.webapp.mail

import com.idyria.osi.wsb.webapp.db.OOXOODatabase
import com.idyria.osi.wsb.webapp.view.Inject
import com.idyria.osi.wsb.webapp.injection.Injector

class MailProvider {

  @Inject("main")
  var configDb: OOXOODatabase = null
  
  var config = new MailProviderConfig
  
  def init = {
    
    Injector.inject(this)
    
    //-- Get configuration
    configDb.container("mail").document("mailpath.xml",config)
    
  }
  
  def sendmail(address:String,subject:String,text:String) = {
    
    
    
  }
  

}