package io.odysz.anson.utils;

import java.util.ArrayList;

import io.odysz.anson.Anson;

/**
 * Type for typical [{name, values}], where values is an array of String.
 * 
 * @see T_NV
 * @author odys-z@github.com
 */
public class T_NVs extends Anson {

	protected String name;
	protected ArrayList<Object> values;

	public T_NVs name(String n) {
		name = n;
		return this;
	}

	public T_NVs value(Object v) {
		if (values == null)
			values = new ArrayList<Object>();
		values.add(v);
		return this;
	}
}
