package com.example.springBootDemo.core.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SecureCaseAction {
	String value() default "[unassigned]";
}
