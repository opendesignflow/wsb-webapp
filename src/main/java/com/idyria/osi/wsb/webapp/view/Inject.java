/**
 * 
 */
package com.idyria.osi.wsb.webapp.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rleys
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
public @interface Inject {

	String value();

	boolean required() default false;

}
