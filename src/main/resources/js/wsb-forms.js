/*
 * #%L
 * WSB Webapp
 * %%
 * Copyright (C) 2013 - 2014 OSI / Computer Architecture Group @ Uni. Heidelberg
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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
	
	var errorsContainer = $(form).find(".errors") 
	
	// Validation
	//---------------
	form.validate()
	if (!form.valid()) {
		error("Cannot submit form from button if it is not valid")
	}
	
	// Ajax Submit
	//-----------------
	
	//-- Serialize formular
	var formSerialised = $(form).serialize()
	
	//-- If The button has a reRender attribute, then also load part at the same time
	var partLoad ="noRender=true"

	if ($(button).attr("reRender")) {
		partLoad="part="+$(button).attr("reRender")
	}
	if ($(form).attr("reRender")) {
		partLoad="part="+$(form).attr("reRender")
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
		
		console.log("Success AJax: "+data)
		
		if (data.content) {
		
			var decoded = decodeHTML(data.content)
			console.log("Decoded: "+decoded)
			if ($(form).attr("reRender")) {
				setPartContent($(form).attr("reRender"),decoded)
			}
			
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
		
		console.log("Errors container "+errorsContainer)
		if (errorsContainer) {
			
			// Clear
			errorsContainer.empty()
			errorsContainer.removeClass("ui-state-error")
			
		}
		
		// Handle Errors
		//----------------------
		$(errors.errors).each(function(i,e){
			
			console.log("Error: "+e.error)
			
			form.addClass("has-error")
			
			// If There is a "source" attribute, then try to map to matching input element
			//---------------
			if (e.source) {
				
				errorsContainer.addClass("ui-state-error")
				errorsContainer.append("<div>Field "+e.source+" didn't validate: "+e.error+"</div>")
				
				
			} else if (errorsContainer) {
				console.log("Adding: "+e.error)
				errorsContainer.append("<p class=\"bg-danger help-block has-error\">"+e.error+"</p>")
				console.log("Errors is now: "+errorsContainer)
			}
			
				
		})
		
	})

	
	// Close Dialog ?
	
	
	
}


// Validation
//-------------------

