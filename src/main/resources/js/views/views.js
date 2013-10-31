/**
 * This file contains client-side utilites to interact with views and view parts
 * 
 *  For example opening a view part on request as a main dialog.
 *  
 */

/**
 * Reload the actual view, but only updates a part matching the element with
 * provided id
 * 
 */
function reloadPart(id) {

}

/**
 * Loads a part for this view
 * 
 * @param remoteId
 */
function loadPart(partId) {

	console.log("loading part: " + partId)

	var deferred = $.post("?part=" + partId, {
		part : partId
	})
	
	/*.fail(function(data) {

		console.log("Failed load part : ");

		

	}).done(function(data) {

		console.log("Done part done: "+data);

		
	})*/
	
	return deferred

}

function openDialogPart(partId) {
	
	//-- Load Data
	var partLoad = loadPart(partId)
	partLoad.done(function(partData) {

		//console.log("Done part done: "+data);
		
		console.log("Got data: "+partData)
		
		//-- Create Dialog Pane
		var html = $.parseHTML(partData.trim())
		
		console.log("Parsed: "+html)
		
		//$("#page").append( html );
		//-- Width : 50 % of available area
		var width = $( document ).width() * 60/100
		
		console.log("Expected html width: "+$(html).width())
		
		$(html).dialog({
			resizable: true,
			width: 'auto',
			height: "auto",
			modal:true,
			open: function( event, ui ) {
				
			},
			resizeStop: function(event,ui) {
	
				console.log("Dialog resized: "+$(event.target).width())
				console.log("Dialog resized: "+$(html).width())
			}
		})
		$(html).dialog().hide().fadeIn();
		
		
		
		
	}).fail(function(jqXHR, textStatus, errorThrown ) {

		console.log("Failed load part : "+textStatus+" // "+jqXHR.responseText);

		//-- Create Dialog Pane
		var html = $.parseHTML("<div title='Error Message'>"+jqXHR.responseText.trim()+"</div>")
		
		console.log("Parsed: "+html)
		
		//$("#page").append( html );
		
		//-- Width : 50 % of available area
		//var width = window.width() * 60/100
		
		$(html).dialog({
			resizable: false,
			width: 'auto',
		})

	})
	
	
	
	
}

/**
 * Tries to reRender the page part based on source information
 * 
 * <button reRender="page">
 * 
 * means:
 * 
 *  - Load a part called page
 *  - Look for an element with an id #page
 *  - Update
 * 
 * @param source
 */
function reRender(source) {
	
	//-- Get ID
	var partId = $(source).attr("reRender")
	if(partId=="") {
		
		return
	}
	
	//-- Get target
	var targetElement = $("#"+partId)
	if (!targetElement) {
		console.error("Could not reRender part: "+partId+" because no target element with id #"+partId+" has been found")
		return
	}
	
	// Load
	//------------------
	var partLoad = loadPart(partId)
	
	// Show temporary informations
	//-------------------
	
	// Empty target
	targetElement.empty()
	
	targetElement.append($("<div>Reloading...<div>"))
	
	// Update ok
	//-----------------
	partLoad.done(function(partData) {

		// Empty target
		targetElement.empty()
		
		
		// Parse
		var html = $.parseHTML(partData.trim())
		
		// Append
		targetElement.append(html)
		
		console.log("updated part "+partId)

	})
	// Update with an error
	//-----------------------------
	.fail(function(jqXHR, textStatus, errorThrown ) {

		// Empty target
		targetElement.empty()
		
		
		// Parse
		var html = $.parseHTML("<div title='Error Message'>"+jqXHR.responseText.trim()+"</div>")
		
		// Append
		targetElement.append(html)
		
	})
}
