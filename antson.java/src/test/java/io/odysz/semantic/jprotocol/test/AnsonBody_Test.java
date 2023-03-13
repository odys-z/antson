package io.odysz.semantic.jprotocol.test;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonField;

/**
 * Test Only
 * 
 * @author odys-z@github.com
 */
public abstract class AnsonBody_Test extends Anson {
	public static String[] jcondt(String logic, String field, String v, String tabl) {
		return new String[] {logic, field, v, tabl};
	}

	@AnsonField(ref=AnsonField.enclosing)
	public AnsonMsg_Test<? extends AnsonBody_Test> parent;

	protected String uri;
	public String uri() { return uri; }

	/** Action: login | C | R | U | D | any serv extension */
	String a;
	/** @return Action: login | C | R | U | D | any serv extension */
	public String a() { return a; }

	public AnsonBody_Test a(String act) {
		this.a = act;
		return this;
	}

	protected AnsonBody_Test(AnsonMsg_Test<? extends AnsonBody_Test> parent, String uri) {
		this.parent = parent;
		this.uri = uri;
	}
}
