
// This function is called when someone finishes with the Login
// Button.  See the onlogin handler attached to it in the sample
// code below.
function fb_checkLoginState() {
	FB.getLoginStatus(function(response) {

		console.log('getLoginStatus');
		console.log(response);
		// The response object is returned with a status field that lets the
		// app know the current login status of the person.
		// Full docs on the response object can be found in the documentation
		// for FB.getLoginStatus().
		if (response.status === 'connected') {
			
			// Logged into your app and Facebook.
			// Call /me to get id and email
			// --------------
			console.log('Logged in facebook, now sending auth data to server2');
			var token = response.authResponse.accessToken 
			FB.api('/me', function(response) {
				console.log('Got personal informations');
			    var id = response.id;
			    var email = response.email;
			
			    $.ajax({
				  type: "POST",
				  data: { action: "com.idyria.osi.wsb.webapp.security.identity.authenticate",
					  provider: "FacebookProvider",
					  id: id, 
					  email: email, 
					  token:token 
				}
				})
				  .done(function( msg ) {
				    console.log("[FB] Done authentication on server")
				    document.open();
				    document.write(msg);
				    document.close();
				  });
			    
			});
			

			
		} else if (response.status === 'not_authorized') {
			// The person is logged into Facebook, but not your app.
			document.getElementById('status').innerHTML = 'Please log '
					+ 'into this app.';
		} else {
			// The person is not logged into Facebook, so we're not sure if
			// they are logged into this app or not.
			/*document.getElementById('status').innerHTML = 'Please log '
					+ 'into Facebook.';*/
		}

	});
}
