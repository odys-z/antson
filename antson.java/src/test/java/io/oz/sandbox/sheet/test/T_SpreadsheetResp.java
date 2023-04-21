package io.oz.sandbox.sheet.test;

import io.odysz.semantic.jprotocol.test.T_AnsonResp;

public class T_SpreadsheetResp<T extends T_SpreadsheetRec> extends T_AnsonResp {

	// AnResultset sheet;
	T rec;

	public T_SpreadsheetResp() {
	}

	public T_SpreadsheetResp(T rec) {
		this.rec = rec;
	}

//	@SuppressWarnings("serial")
//	public SpreadsheetResp(AnResultset sheet) {
//		this.rs = new ArrayList<AnResultset>() { {add(sheet);} };
//	}
}
