package io.oz.sandbox.sheet.test;

import io.odysz.semantic.jprotocol.test.T_AnsonBody;
import io.odysz.semantic.jprotocol.test.T_AnsonMsg;
import io.odysz.semantic.jprotocol.test.T_PageInf;

public class T_SpreadsheetReq<T extends T_SpreadsheetRec> extends T_AnsonBody {
	static class A {
		public static final String records = "r";
		public static final String insert = "c";
		public static final String update = "u";
		public static final String delete = "d";
	}

	T rec;
	T_PageInf page;

	public T_SpreadsheetReq() {
		super(null, null);
	}

	protected T_SpreadsheetReq(T_AnsonMsg<? extends T_AnsonBody> parent, String uri) {
		super(parent, uri);
	}

}
