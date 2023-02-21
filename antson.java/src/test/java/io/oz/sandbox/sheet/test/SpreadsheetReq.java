package io.oz.sandbox.sheet.test;

import io.odysz.semantic.jprotocol.test.AnsonBody;
import io.odysz.semantic.jprotocol.test.Test_AnsonMsg;
import io.odysz.semantic.jprotocol.test.PageInf;

public class SpreadsheetReq<T extends SpreadsheetRec> extends AnsonBody {
	static class A {
		public static final String records = "r";
		public static final String insert = "c";
		public static final String update = "u";
		public static final String delete = "d";
	}

	T rec;
	PageInf page;

	public SpreadsheetReq() {
		super(null, null);
	}

	protected SpreadsheetReq(Test_AnsonMsg<? extends AnsonBody> parent, String uri) {
		super(parent, uri);
	}

}
