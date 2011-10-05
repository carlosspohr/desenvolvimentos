package com.wp.carlos4web.vraptor;

@java.lang.annotation.Target(value={java.lang.annotation.ElementType.PARAMETER})
@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface LoadObject
{
	boolean required() default false;
	
	String redirectToWhenObjectNotFound() default "";
	
	//teste commit.
}