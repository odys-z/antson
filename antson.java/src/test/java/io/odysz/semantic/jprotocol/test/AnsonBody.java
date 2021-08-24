package io.odysz.semantic.jprotocol.test;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonField;

public abstract class AnsonBody extends Anson {
	public static String[] jcondt(String logic, String field, String v, String tabl) {
		return new String[] {logic, field, v, tabl};
	}

	@AnsonField(ref=AnsonField.enclosing)
	public AnsonMsg<? extends AnsonBody> parent;

	protected String uri;
	public String uri() { return uri; }

	/** Action: login | C | R | U | D | any serv extension */
	String a;
	/** @return Action: login | C | R | U | D | any serv extension */
	public String a() { return a; }

	public AnsonBody a(String act) {
		this.a = act;
		return this;
	}

	protected AnsonBody(AnsonMsg<? extends AnsonBody> parent, String uri) {
		this.parent = parent;
		this.uri = uri;
	}
}
