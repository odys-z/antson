package io.oz.sandbox.sheet.test;

import io.odysz.semantic.jprotocol.test.AnsonBody_Test;
import io.odysz.semantic.jprotocol.test.AnsonMsg_Test;
import io.odysz.semantic.jprotocol.test.PageInf_Test;

public class SpreadsheetReq_Test<T extends SpreadsheetRec> extends AnsonBody_Test {
	static class A {
		public static final String records = "r";
		public static final String insert = "c";
		public static final String update = "u";
		public static final String delete = "d";
	}

	T rec;
	PageInf_Test page;

	public SpreadsheetReq_Test() {
		super(null, null);
	}

	protected SpreadsheetReq_Test(AnsonMsg_Test<? extends AnsonBody_Test> parent, String uri) {
		super(parent, uri);
	}

}
