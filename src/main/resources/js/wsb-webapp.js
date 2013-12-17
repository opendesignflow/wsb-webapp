/**

This file initialises the scripts to be loaded for Webapp functionalities

*/
$(function() {

	var scripts = ['/js/views/views.js','/js/wsb-forms.js']
	
	$(scripts).each(function(i,e) {
		
		console.log("Trying to load: "+basePath+e)
		$.getScript(basePath+e)
		.fail(function( jqxhr, settings, exception) {
		    console.log( "Error at line: "+exception.line+ ": "+exception.stack)
		  })
	
	})
	
})

function loadScripts(scripts) {
	
	$(scripts).each(function(i,e) {
		
		$.getScript(basePath+e)
		.fail(function( jqxhr, settings, exception) {
		    console.log( exception);
		  })
	
	})
}
