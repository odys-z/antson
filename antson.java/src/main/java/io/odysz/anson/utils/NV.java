package io.odysz.anson.utils;

import io.odysz.anson.Anson;

/**Type for typical {name, value}.
 * 
 * @see NVs
 * @author odys-z@github.com
 *
 */
public class NV extends Anson {

	public String name;
	public Object value;

	public NV nv(String n, Object v) {
		this.name = n;
		this.value = v;
		return this;
	}
}
