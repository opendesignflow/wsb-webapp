

var currentUser = '${sessionScope.email}';



function persona_load () {
	
	// Prepare User
	//-----------------
	if(!currentUser) {
	    // If false set it to the literal null
	    currentUser = null;
	}
	
	// Prepare Buttons
	//------------------
	var signin = $("#sign-in")
	var signout = $("#sign-out")
	
	if (signin) {
		console.log("[Persona] sign in: "+signin)
		signin.click( function() {
			  navigator.id.request({siteName: 'St peters Music on the canal'});
			});
	}
	
	
	// Setup watch API
	//---------------

	navigator.id.watch({
	        loggedInUser: currentUser,
	        onlogin: function(assertion) {
	        	//persona_verifyAssertion(assertion);
	        },
	        onlogout: function() {
	        	persona_logoutUser();
	        }
	      });

	
	
}

// Login/Logout
//------------------


// Logout
function persona_loginUser(loggedInUser) {
  // update your UI to show that the user is logged in
  console.log("Logged in: "+loggedInUser)
}

function persona_logoutUser() {
  // update your UI to show that the user is logged out
}

// Remote Verification
//----------------------


function handleVerificationResponse(xhr) {
  // if successfully verified, log the user in
	console.log("[Persona] Ajax return: "+xhr)
}

function persona_verifyAssertion(assertion) {
	
	var request = $.ajax({
		   url: "https://verifier.login.persona.org/verify",
		   type: "POST",
		   crossDomain: true,
		   data: {assertion: assertion,audience:"https://localhost"},
		   xhrFields: {
			      withCredentials: true
			   }
		});
	
	// Done 
	request.done(function( data, textStatus, jqXHR ) {
		
		console.log("[Persona] Verification done");
		
		// Test reault
		if (data.status == "okay") {
			console.log("[Persona] Login OK");
			persona_loginUser(data.email)
		} else {
			console.log("[Persona] Login Fail");
		}
	
		
	});      
	
	// Error
	request.fail(function(jqXHR, textStatus, errorThrown ) {
		console.log("[Persona] An error occured after assertion verification: "+errorThrown);
	});
	
 /* var xhr = new XMLHttpRequest();
  xhr.open("POST", "https://verifier.login.persona.org/verify", true);
  var param = "assertion="+assertion;
  xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  xhr.setRequestHeader("Content-length", param.length);
  xhr.setRequestHeader("Connection", "close");
  xhr.send(param);
  xhr.onload = handleVerificationResponse(xhr);*/
}