/**

This file initialises the scripts to be loaded for Webapp functionalities

*/
$(function() {

	var scripts = ['/js/views/views.js','/js/wsb-forms.js']
	
	$(scripts).each(function(i,e) {
		
		$.getScript(basePath+e)
		.fail(function( jqxhr, settings, exception) {
		    console.log( exception + "- "+exception);
		  })
	
	})
	
})

function loadScripts(scripts) {
	
	$(scripts).each(function(i,e) {
		
		$.getScript(basePath+e)
		.fail(function( jqxhr, settings, exception) {
		    console.log( exception + "- "+exception);
		  })
	
	})
}
