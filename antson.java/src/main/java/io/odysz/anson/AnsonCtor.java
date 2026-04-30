package io.odysz.anson;

import java.lang.annotation.*;

/**
 * E.g. if define (Design Cmake 0.1)
 * <pre>
 * @AnsonCtor(initialist="echo(string m)", base={"r/query", "uri"}),
 * 
 * // [0]: Initializer 0, ..., [-1] Base Initializer
 * AnsonAst.cotrs[i] = [["echo": "string", "m"], ["r/query", "uri"]]
 * 
 * // c++ constructor:
 * EchReq::EchoReq(string m) : AnsonBody("r/query, "uri", EchoReq::_type_), echo(m) {}
 * </pre>
 * @since 1.5.17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface AnsonCtor {

	String initialist() default "";

	/**
	 * @return
	 */
	String[] base() default "";
}
