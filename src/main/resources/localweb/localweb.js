
var localWeb = {

	};

	/**
	 * Call A Remote Action
	 */
	localWeb.buttonClick = function(id) {

		console.info("Button Clicked, sending remote request for " + id);

		var deffered = $.get(id);
		deffered.done(function(data) {
			console.log("Done...");
			if (data!="OK") {
				console.log("Reloading Page")
				//$("body").html(data);
			}
		});

	};
	
	/**
	 * Bind Value
	 */
	localWeb.bindValue = function(element,actionPath) {
		
		
		console.info("Value changed sending remote request for " + actionPath);
		
		//-- Get value 
		var elt = $(element);
		if (elt.attr("type")=="checkbox") {
			var value = elt.is(":checked");
		} else {
			var value = elt.val();
		}
		
		var name = $(element).attr("name");
		
		console.info("Sending "+value+" as name "+name);
		
		var deffered = $.get(actionPath+"?"+name+"="+value);
		deffered.done(function(data) {
			console.log("Done...");
			
		});
		
		
		
	};

	
	// Event Connection
	//---------------------------
	localWeb.makeEventConnection = function() {

		var targetURL = "ws://" + window.location.hostname
				+ (window.location.port ? ':' + window.location.port : '')
				+ window.location.pathname
		console.log("Making Websocket connection to: " + targetURL);
		localWeb.wsConnection = new WebSocket(targetURL, [ 'soap' ]);

		// Log messages from the server
		localWeb.wsConnection.onmessage = function(e) {
			console.log('Server: ' + e.data);

			// Get SOAP JSON
			//-------------
			var soap = $.parseJSON("{" + e.data + "}");

			
			$(localWeb.wsConnection).trigger("soap",soap);
			
			
			// Handle messages
			// --------------
			var body = soap.Envelope.Body;
			
			console.log("Keys: "+Object.keys(body));
			$(localWeb.wsConnection).trigger("payload",[ Object.keys(body)[0],body[Object.keys(body)[0]]]);
			
			if (body.Ack) {
				console.log("Got Acknowledge");
			}

			if (body.UpdateHtml) {
				console.log("Updating HTML");
				// $("body").html(body.UpdateHtml.HTML);
				// $("body").html(localWeb.decodeHTML(body.UpdateHtml[0].HTML));

				$("body").html($($.parseHTML(localWeb.decodeHTML(body.UpdateHtml[0].HTML))).select("body"));
			}

		};
	};
	
	localWeb.onPushData = function( name, f ) {
		
		// Registering function
		$(localWeb.wsConnection).on("payload",function(event,pname,payload) {
			
			console.log("Got payload "+pname+" filtering for "+name);
			if(pname==name) {
				f(payload[0]);
			}
			
		});
		
	};

	/**
	 * Decodes the URL Encoded HTML back to normal html that can be loaded in
	 * the page
	 * 
	 * @param content
	 */
	localWeb.decodeHTML = function(uriHTML) {
		return decodeURI(uriHTML).replace(/\++/g, " ").replace(/(%2F)+/g, "/")
				.replace(/(%3A)+/g, ":").replace(/(%3D)+/g, "=").replace(
						/(%23)+/g, "#").replace(/(%40)+/g, "@").replace(
						/(%2C)+/g, ",")
	}

	console.info("Welcome to localweb JS..." + window.location.pathname);
	localWeb.makeEventConnection();

