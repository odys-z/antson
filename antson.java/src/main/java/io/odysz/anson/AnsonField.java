package io.odysz.anson;

import java.lang.annotation.*;

/**<p>Controlling options of (de)serializing {@link Anson} fields.</p>
 * <p>For reference: see <a href='https://docs.oracle.com/javase/tutorial/java/annotations/index.html'>
 * Oracle Docs</a>;</p>
 * <p>For Annotation tutorial, see <a href='http://tutorials.jenkov.com/java-reflection/annotations.html'>
 * Java Reflection - Annotations</a>.</p>
 * @author odys-z@github.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AnsonField {
	boolean ignoreTo() default false;
	boolean ignoreFrom() default false;
}
