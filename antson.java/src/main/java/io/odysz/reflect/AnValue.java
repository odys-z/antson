package io.odysz.reflect;

import java.util.ArrayList;

import io.odysz.anson.Anson;

/**
 * A value specified in an annotation, {@link AnAnnotation#elements}.
 * 
 * <p>Name-value pairs instead of a map, as the names, e.g. "type", can be Anson's envelope key;
 * values are strings with the declared type in {@link #valType}, as Anson can't deserialize
 * an array into an Object field.</p>
 * 
 * E.g. for {@code @TAnno(value="pkg", kinds={Kind.a, Kind.b}, type=AnsT3.class, nums={1, 2})}:
 * <pre>
 * name   valType                   value / array
 * value  java.lang.String          "pkg"
 * kinds  io.x.TArrays.Kind[]       ["io.x.TArrays.Kind.a", "io.x.TArrays.Kind.b"]
 * type   java.lang.Class           "io.odysz.anson.AnsT3"
 * nums   int[]                     ["1", "2"]
 * </pre>
 * 
 * @since 1.2.0
 */
public class AnValue extends Anson {
	public String name;

	/**
	 * boolean, int, long, ..., java.lang.String, java.lang.Class,
	 * the enum type or the annotation type; with "[]" for arrays.
	 */
	public String valType;

	/**
	 * A scalar value: primitives and strings as is;
	 * class literals and enum constants as qualified names, e.g. io.odysz.anson.AnsT3, io.x.Port.heartbeat.
	 */
	public String value;

	/** Elements of an array of scalars, as {@link #value}. */
	public ArrayList<String> array;

	/** Nested annotation(s): one for an annotation value, the elements for an array of annotations. */
	public ArrayList<AnAnnotation> annotations;

	public AnValue() { }

	/**
	 * A scalar.
	 * @param name element name
	 * @param valType e.g. boolean, java.lang.String, java.lang.Class, io.x.TArrays.Kind
	 * @param value e.g. true, io.odysz.anson.AnsT3, io.x.TArrays.Kind.a
	 */
	public AnValue(String name, String valType, String value) {
		this.name = name;
		this.valType = valType;
		this.value = value;
	}

	/**
	 * An array, of which the elements are added by {@link #add(String, String)}, or to {@link #annotations}.
	 * @param name element name
	 * @return the value, with valType "[]" until an element is added
	 */
	public static AnValue array(String name) {
		AnValue v = new AnValue(name, "[]", null);
		v.array = new ArrayList<String>();
		return v;
	}

	/**
	 * A nested annotation, or an array of them.
	 * @param name element name
	 * @param annotation
	 * @return the value
	 */
	public static AnValue annotation(String name, AnAnnotation annotation) {
		AnValue v = new AnValue(name, annotation.annotation, null);
		v.annotations = new ArrayList<AnAnnotation>();
		v.annotations.add(annotation);
		return v;
	}

	/**
	 * Add an element of an array.
	 * @param elemType the element's type, which sets {@link #valType} on the first element
	 * @param elem
	 * @return this
	 */
	public AnValue add(String elemType, String elem) {
		if ("[]".equals(valType))
			valType = elemType + "[]";
		array.add(elem);
		return this;
	}
}
