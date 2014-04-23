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
