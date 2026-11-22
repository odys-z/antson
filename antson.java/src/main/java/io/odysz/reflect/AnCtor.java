package io.odysz.reflect;

import java.util.ArrayList;

/**
 * A constructor's semantics, {@link AnsonAst#ctorsemantics},
 * the Java peer of anson.cmake's AnCtor (reflect.h).
 * 
 * @since 1.2.0
 */
public class AnCtor extends Semantics {
	public SemanExpr base;
	public ArrayList<SemanExpr> args;
	public ArrayList<SemanExpr> body;

	public AnCtor() { }
}
