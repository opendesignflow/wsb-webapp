package com.idyria.osi.wsb.webapp.security.providers.bearer

import com.idyria.osi.wsb.webapp.security.FederatedIdentity
import com.idyria.osi.wsb.webapp.security.AuthenticationProvider
import com.idyria.osi.wsb.webapp.security.providers.extern.FacebookFederatedIdentity
import com.idyria.osi.wsb.webapp.security.AuthenticationDatas
import com.idyria.osi.wsb.webapp.security.AuthToken
import com.idyria.osi.wsb.webapp.WebApplication
import com.idyria.osi.wsb.webapp.http.message.HTTPRequest
import uni.hd.cag.utils.security.utils.RandomID

class TokenBearerProvider extends AuthenticationProvider {

  this.requiredParameters = this.requiredParameters + ("token" -> "Authentication token to be verified")

  def authenticate(datas: AuthenticationDatas, application: WebApplication, request: HTTPRequest): AuthToken = {

    var authtoken = AuthToken()
    var identity = new TokenBearerIdentity
    //identity.accessToken = datas.datas("token")
    authtoken.federatedIdentity = identity
    authtoken.federatedIdentity.token = datas.datas("token")

    return authtoken

  }

}

class TokenBearerIdentity extends FederatedIdentity {

  this.providerId = classOf[TokenBearerProvider].getCanonicalName

}
object TokenBearerIdentity {
  
  def generate : TokenBearerIdentity = {
    
    var id = new TokenBearerIdentity
    id.token = RandomID.generate()
    
    id
  }
  
}



