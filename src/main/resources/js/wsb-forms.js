/**

This file contains the utility functions for Forms

*/

/**
 * Download Extra scripts
 */
$(function() {

	//loadScripts(["/js/jquery.validationEngine-en.js","/js/jquery.validationEngine.js"])
	
	/*$.fn.validate = function() {
		
		
		
	}*/
	
	//$( document ).tooltip();
	
	
	/*$("#productsTable").attr("title","Add Products from")
	$("#productsTable").tooltip({position:{ my: "center bottom", at: "bottom" }})
	
	$( "#productsTable" ).tooltip({ content: "Awesome title!" });
	$( "#productsTable" ).tooltip( "option", "disabled", true );*/
})




function submitForm( button ) {
	
	console.log("Submiting form from button "+button)
	
	// Find Form
	//---------
	var form = $(button).closest("form")
	
	// FIXME Check that the parent is a form
	if (!form) {
		error("Cannot submit form from button if not form is surrounding the button")
	}
	
	// Validation
	//---------------

	
	// Ajax Submit
	//-----------------
	
	//-- Serialize formular
	var formSerialised = $(form).serialize()
	
	//-- If The button has a reRender attribute, then also load part at the same time
	var partLoad ="noRender=true"

	if ($(button).attr("reRender")) {
		partLoad="part="+$(button).attr("reRender")
	}
	console.log("Serialized form: "+formSerialised)
	
	//-- Ajax Post
	var deffered = $.ajax({
	  type: "POST",
	  url: "?"+partLoad,
	  data: formSerialised,
	  dataType: "json",
	  accepts: {
	      xml: "text/xml",
	      json: "application/json",
	      html: "text/html"
	  }
	});
	
	
	// On result
	//----------------------
	
	//-- Success
	deffered.done(function(data){
		
		console.log("Success AJax: ")
		
		if (data.content) {
		
			var decoded = decodeHTML(data.content)
			console.log("Decoded: "+decoded)
			setPartContent($(button).attr("reRender"),decoded)
		}
		//var content = eval(data)
		// Done: Re-Render
		//------------------------
		//reRender(button)
		
		
	})
	
	//-- Fail
	deffered.fail(function(jqXHR, textStatus, errorThrown ) {
		console.log("Errors: "+jqXHR.responseText)
		
		// Parse as JSON
		var errors = eval("("+jqXHR.responseText+")")
		
		// Look for a container
		//-------------------------
		var errorsContainer = $(form).find(".errors") 
		if (errorsContainer) {
			
			// Clear
			errorsContainer.empty()
			errorsContainer.removeClass("ui-state-error")
			
		}
		
		// Handle Errors
		//----------------------
		$(errors.errors).each(function(i,e){
			
			console.log("Error: "+e)
			
			// If There is a "source" attribute, then try to map to matching input element
			//---------------
			if (e.source) {
				
				errorsContainer.addClass("ui-state-error")
				errorsContainer.append("<div>Field "+e.source+" didn't validate: "+e.error+"</div>")
				
				
			} else if (errorsContainer) {
				errorsContainer.append("<div>"+e.error+"</div>")
			}
			
				
		})
		
	})

	
	// Close Dialog ?
	
	
	
}


// Validation
//-------------------

