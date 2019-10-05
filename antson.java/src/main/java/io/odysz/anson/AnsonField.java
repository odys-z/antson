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
	public int undefined = -1;
	public int enclosing = 1;

	boolean ignoreTo() default false;

	boolean ignoreFrom() default false;

	/**
	 * <p>Specifying array's element type information.</p>
	 * Example:<br>
	 * for Object[], use<pre>
	   @AnsonField(valType="[Ljava.lang.Object;")
	   Object[][] f;
	   </pre>
	 * for ArrayList&lt;Object[]&gt;, use <pre>
	   @AnsonField(valType="java.util.ArrayList;[Ljava.lang.Object;"
	   ArrayList<ArrayList<Object[]>>
	   </pre>
	 */
	String valType() default "";

	int ref() default undefined;
}
