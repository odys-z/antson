package io.odysz.antson.utils;

import java.util.ArrayList;

import io.odysz.anson.Anson;

/**Type for typical {name, values}, where values is an array of String.
 * 
 * @see AnsonNv
 * @author odys-z@github.com
 */
public class AnsonNvs extends Anson {

	protected String name;
	protected ArrayList<Object> values;

	public AnsonNvs name(String n) {
		name = n;
		return this;
	}

	public AnsonNvs value(Object v) {
		if (values == null)
			values = new ArrayList<Object>();
		values.add(v);
		return this;
	}
}
