

var localWeb =  {
	
};
		
	/**
	 * Call A Remote Action
	 */
localWeb.buttonClick = function (id) {
		
		console.info("Button Clicked, sending remote request for "+id);
		
		var deffered = $.get("/action/"+id);
		deffered.done(function(data) {
			console.log("Done...");
			if (data) {
				console.log("Reloading Page")
				$("body").html(data);
			}
		});
		
	};

	
	localWeb.makeEventConnection = function() {
		
		var targetURL = "ws://"+window.location.hostname+(window.location.port ? ':'+window.location.port: '')+window.location.pathname
		console.log("Making Websocket connection to: "+targetURL);
		localWeb.wsConnection= new WebSocket(targetURL, ['soap']);

		//Log messages from the server
		localWeb.wsConnection.onmessage = function (e) {
		  console.log('Server: ' + e.data);
		  
		  // Get JSON
		  var soap = $.parseJSON("{"+e.data+"}");
		  
		  // Handle messages
		  //--------------
		  var body =  soap.Envelope.Body;
		  
		  if (body.Ack) {
			  console.log("Got Acknowledge");
		  }
		  
		  if (body.UpdateHtml) {
			  console.log("Updating HTML");
			  //$("body").html(body.UpdateHtml.HTML);
			  //$("body").html(localWeb.decodeHTML(body.UpdateHtml[0].HTML));
			  
			  $("html").html(localWeb.decodeHTML(body.UpdateHtml[0].HTML));
		  }
		  
		};
	};
	
	 /**
	 * Decodes the URL Encoded HTML back to normal html that can be loaded in the page
	 * @param content
	 */
	localWeb.decodeHTML = function (uriHTML) {
			return decodeURI(uriHTML).replace(/\++/g," ")
						.replace(/(%2F)+/g,"/")
						.replace(/(%3A)+/g,":")
						.replace(/(%3D)+/g,"=")
						.replace(/(%23)+/g,"#")
						.replace(/(%40)+/g,"@")
						.replace(/(%2C)+/g,",")
		}


	


console.info("Welcome to localweb JS..."+window.location.pathname);
localWeb.makeEventConnection();


