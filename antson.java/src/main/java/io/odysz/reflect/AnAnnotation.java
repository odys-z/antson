package io.odysz.reflect;

import java.util.ArrayList;

import io.odysz.anson.Anson;

/**
 * An annotation on a field, {@link AnsonField#annotations}, e.g. {@code @AnsonField(ignoreFrom=true, ref=AnsonField.enclosing)}.
 * 
 * <p>Only the values explicitly specified are in the class file, so defaults are not listed.</p>
 * 
 * @since 1.2.0
 */
public class AnAnnotation extends Anson {
	/** Qualified name of the annotation type, e.g. io.odysz.anson.AnsonField */
	public String annotation;

	/** RetentionPolicy.RUNTIME, otherwise CLASS */
	public boolean runtime;

	/** Element values specified, in order. Not named "values", which is a method of Python dict. */
	public ArrayList<AnValue> elements;

	public AnAnnotation() { }

	/**
	 * @param annotation qualified name of the annotation type
	 * @param runtime RetentionPolicy.RUNTIME
	 */
	public AnAnnotation(String annotation, boolean runtime) {
		this.annotation = annotation;
		this.runtime = runtime;
		this.elements = new ArrayList<AnValue>();
	}

	/**
	 * @param name
	 * @return the element's value specified, null if not specified (default)
	 */
	public AnValue element(String name) {
		if (elements != null)
			for (AnValue v : elements)
				if (name.equals(v.name)) return v;
		return null;
	}
}
