package io.oz.sandbox.sheet.test;

import io.odysz.semantic.jprotocol.test.AnsonResp_Test;

public class SpreadsheetResp<T extends SpreadsheetRec> extends AnsonResp_Test {

	// AnResultset sheet;
	T rec;

	public SpreadsheetResp() {
	}

	public SpreadsheetResp(T rec) {
		this.rec = rec;
	}

//	@SuppressWarnings("serial")
//	public SpreadsheetResp(AnResultset sheet) {
//		this.rs = new ArrayList<AnResultset>() { {add(sheet);} };
//	}
}
