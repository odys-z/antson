package io.odysz.anson.utils;

import io.odysz.anson.Anson;

/**
 * Type for typical {name, value}.
 * 
 * @see T_NVs
 * @author odys-z@github.com
 *
 */
public class T_NV extends Anson {

	public String name;
	public Object value;

	public T_NV nv(String n, Object v) {
		this.name = n;
		this.value = v;
		return this;
	}
}
