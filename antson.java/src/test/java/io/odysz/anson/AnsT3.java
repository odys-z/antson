package io.odysz.anson;

import java.util.ArrayList;

/**Class for testing Anson array field.
 * @author odys-z@github.com
 *
 */
public class AnsT3 extends Anson {

	/** Elements can be subclass*/
	Anson[] m;
	
	// annotation is not used, which can be figured out from ParameterizedType
	@AnsonField(valType="[Lio.odysz.Anson;/io.odysz.Anson")
	ArrayList<Anson[]> ms;

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
