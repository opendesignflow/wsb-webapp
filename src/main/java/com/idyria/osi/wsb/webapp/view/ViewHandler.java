package com.idyria.osi.wsb.webapp.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To be set on a method to mark it as elligible for request handling
 * 
 * @author rleys
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface ViewHandler {

}
