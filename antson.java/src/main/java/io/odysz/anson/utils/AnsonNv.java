package io.odysz.anson.utils;

import io.odysz.anson.Anson;

/**Type for typical {name, value}.
 * 
 * @see AnsonNvs
 * @author odys-z@github.com
 *
 */
public class AnsonNv extends Anson {

	protected String name;
	protected Object value;

	public void nv(String n, Object v) {
		this.name = n;
		this.value = v;
	}
}
