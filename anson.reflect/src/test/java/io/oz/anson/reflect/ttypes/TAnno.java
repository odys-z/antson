package io.oz.anson.reflect.ttypes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Test type for TypeScanner, an annotation of CLASS retention, with enum, class and array values.
 */
@Retention(RetentionPolicy.CLASS)
public @interface TAnno {
	String value() default "";
	TArrays.Kind[] kinds() default {};
	Class<?> type() default Object.class;
	int[] nums() default {};
}
