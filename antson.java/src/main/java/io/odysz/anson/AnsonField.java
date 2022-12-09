package io.odysz.anson;

import java.lang.annotation.*;

/**
 * <p>
 * Controlling options of (de)serializing {@link Anson} fields.
 * </p>
 * <p>
 * For reference: see <a href=
 * 'https://docs.oracle.com/javase/tutorial/java/annotations/index.html'> Oracle
 * Docs</a>;
 * </p>
 * <p>
 * For Annotation tutorial, see
 * <a href='http://tutorials.jenkov.com/java-reflection/annotations.html'> Java
 * Reflection - Annotations</a>.
 * </p>
 * 
 * @author odys-z@github.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AnsonField {
	// nothing happen
	public static final int undefined = -1;
	// must trigger reference resolving
	public static final int enclosing = 1;

	boolean ignoreTo() default false;

	boolean ignoreFrom() default false;

	/**
	 * <p>Specifying array's element type information.</p>
	 * Example:<br>
	 * for Object[], use<pre>
	   {@literal @}AnsonField(valType="[Ljava.lang.Object;")
	   Object[][] f;
	   </pre>
	 * for ArrayList&lt;Object[]&gt;, use <pre>
	   {@literal @}AnsonField(valType="java.util.ArrayList;[Ljava.lang.Object;"
	   ArrayList<ArrayList<Object[]>>
	   </pre>
	 */
	String valType() default "";
	
	/**
	 * If a sting field, e.g. base64 string come with this,
	 * {@link Anson#toBlock(JsonOpt)} (opt.shortenOnAnnotation = true) will ignore the content.
	 * <br/>- long string printed only for debugging.
	 * 
	 * @return the annotation. If true, use shorten string when possible
	 */
	boolean shortenString() default false;

	int ref() default undefined;
}
