package io.odysz.anson;

import io.odysz.anson.Anson;

/**Class for testing Anson array field.
 * @author odys-z@github.com
 *
 */
public class AnsT3 extends Anson {

	/** Elements can be subclass*/
	Anson[] m;
	
	public AnsT3() {}

	public void expand(Anson child) {
		if (m != null) {
			Anson[] m1 = new Anson[m.length + 1];
			System.arraycopy(m, 0, m1, 0, m.length);
			m1[m1.length - 1] = child;
			m = m1;
		}
		else {
			m = new Anson[1];
			m[0] = child;
		}
	}
}
