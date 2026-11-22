package io.odysz.reflect;

import java.util.ArrayList;

/**
 * A semantic expression of {@link AnCtor}, the Java peer of anson.cmake's SemanExpr (reflect.h).
 * 
 * @since 1.2.0
 */
public class SemanExpr extends Semantics {
	/** "()", "=", "ini", or "" */
	public String stype;
	public ArrayList<String> args;
	public ArrayList<SemanExpr> semantics;
	public String expect_result;

	public SemanExpr() { }
}
