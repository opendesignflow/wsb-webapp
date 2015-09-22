package com.idyria.osi.wsb.webapp.security.providers.bearer

import com.idyria.osi.wsb.webapp.security.FederatedIdentity
import com.idyria.osi.wsb.webapp.security.AuthenticationProvider
import com.idyria.osi.wsb.webapp.security.providers.extern.FacebookFederatedIdentity
import com.idyria.osi.wsb.webapp.security.AuthenticationDatas
import com.idyria.osi.wsb.webapp.security.AuthToken
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import uni.hd.cag.utils.security.utils.RandomID
import com.idyria.osi.wsb.webapp.navigation.NavigationRule
import com.idyria.osi.wsb.webapp.security.User

class TokenBearerProvider extends AuthenticationProvider {

  //this.requiredParameters = this.requiredParameters + ("token" -> "Authentication token to be verified")

  def authenticate(datas: AuthenticationDatas, application: WebApplication, request: HTTPRequest): AuthToken = {

    request.getURLParameter("token") match {
      case Some(token) =>

        var authtoken = AuthToken()
        var identity = new TokenBearerIdentity
        //identity.accessToken = datas.datas("token")
        authtoken.federatedIdentity = identity
        authtoken.federatedIdentity.token = token
        
        authtoken
        
      case None => null
    }

   /* var authtoken = AuthToken()
    var identity = new TokenBearerIdentity
    //identity.accessToken = datas.datas("token")
    authtoken.federatedIdentity = identity
    authtoken.federatedIdentity.token = datas.datas("token")

    return authtoken*/

  }

}

class TokenBearerTryAuthRule extends NavigationRule {
  def evaluate(application: WebApplication, request: HTTPRequest): Boolean = {

    false

  }
}

class TokenBearerIdentity extends FederatedIdentity {

  this.providerId = classOf[TokenBearerProvider].getCanonicalName

}
object TokenBearerIdentity {

  def generateTo(user:User,compare:Iterable[User]) : TokenBearerIdentity = {
    
    var continue = true
    var id = new TokenBearerIdentity
    while(continue) {
      
      //-- Generate
       id.token = RandomID.generateSmall()
       
       //-- Check
       compare.find { user => user.identities.find{identity =>identity.token != null && identity.token.toString()==id.token.toString}!=None } match {
         case Some(user) => 
         case None => 
           user.identities += id
           continue = false
       }
      
    }
    
    id
    
  } 
  
  def generate: TokenBearerIdentity = {

    var id = new TokenBearerIdentity
    id.token = RandomID.generateSmall()

    id
  }

}



