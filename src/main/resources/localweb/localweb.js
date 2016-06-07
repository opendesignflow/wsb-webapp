
var localWeb = {
		
		debug: false
		
};

	/**
	 * Call A Remote Action
	 */
	localWeb.buttonClick = function(button,id) {

		console.info("Button Clicked, sending remote request for " + id);

		$(button).prop('disabled', true);
		var deffered = $.get(id+"?format=json");
		deffered.done(function(data) {
			console.log("Done...");
			$(button).prop('disabled', false);
			if (data!="OK") {
				console.log("Reloading Page")
				$("body").html(data);
			}
			
			if ($(button).attr("reload")!="") {
				location.reload();
			}
			
		});
		deffered.fail(function(data) {
			console.log("Error in action ");
			localWeb.faultFor(button,data.responseText);
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
		
		//-- Send Value
		var name = $(element).attr("name");
		
		console.info("Sending "+value+" as name "+name);
		
		var deffered = $.get(actionPath+"?format=json&"+name+"="+encodeURIComponent(value));
		deffered.done(function(data) {
			console.log("Done 2...");
			
			//-- Hide error 
			var errorNode = localWeb.errorNode(element);
			console.log("Error node: "+errorNode);
			if (errorNode) {
				$(errorNode).css("display","none");
			}
			
			//-- reRender if necessary
			console.log("Reload node: "+$(element).attr("reload"));
			if ($(element).attr("reload") && $(element).attr("reload") == true ) {
				location.reload();
			}
			else if (data!="OK") {
				console.log("Reloading Page")
				$("body").html(data);
			}
			
		});
		deffered.fail(function(data) {
			console.log("Error in value binding");
			localWeb.faultFor(element,data.responseText);
		});
		
		
		
	};

	localWeb.faultFor = function(element, faultJsonString) {
		
		//console.info("Error Reason: "+faultJsonString[0]);
		var fault = $.parseJSON("{" + faultJsonString + "}").Fault;
		
		//-- Get text
		var text = localWeb.decodeHTML(fault.Reason.Text);
		
		//console.info("Error Reason: "+fault.Reason.Text);
		
		//-- Look for error in parent neighbor 
		var errorBlockNeighbor = $(element).parent().find(".error:first");
		if (errorBlockNeighbor) {
			console.log("Found Error Container: "+errorBlockNeighbor);
			$(errorBlockNeighbor).html(text);
			$(errorBlockNeighbor).css("display","block");
			
		} else {
			console.log("Error Container Not Found");
		}
		
	};
	
	localWeb.errorNode = function(element) {
		return $(element).parent().find(".error:first");
		/*if (errorBlockNeighbor) {
			console.log("Found Error Container: "+errorBlockNeighbor);
			$(errorBlockNeighbor).html(text);
			$(errorBlockNeighbor).css("display","block");
			
		} else {
			console.log("Error Container Not Found");
		}*/
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
			
			if(localWeb.debug==true) {
				console.log('Server: ' + e.data);
			}
			

			// Get SOAP JSON
			//-------------
			var soap = $.parseJSON("{" + e.data + "}");

			
			$(localWeb.wsConnection).trigger("soap",soap);
			
			
			// Handle messages
			// --------------
			var body = soap.Envelope.Body;
			
			if(localWeb.debug==true) {
				console.log("Keys: "+Object.keys(body));
			}
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
	
	/*
	 * Function: f(payload)
	 */
	localWeb.onPushData = function( name, f ) {
		
		// Registering function
		$(localWeb.wsConnection).on("payload",function(event,pname,payload) {
			
			if(localWeb.debug==true) {
				console.log("Got payload "+pname+" filtering for "+name);
			}
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
				.replace(/(%3A)+/g, ":").replace(/(%3B)+/g, ";").replace(/(%3D)+/g, "=").replace(
						/(%23)+/g, "#").replace(/(%40)+/g, "@").replace(
						/(%2C)+/g, ",").replace(/(%2B)+/g, "+").replace(/(%28)+/g, "(").replace(/(%29)+/g, ")")
	}

	console.info("Welcome to localweb JS..." + window.location.pathname);
	localWeb.makeEventConnection();

