package io.odysz.semantic.ext.test;

import java.util.ArrayList;
import java.util.List;

import io.odysz.anson.Anson;
import io.odysz.semantic.jprotocol.test.AnsonMsg_Test;
import io.odysz.semantic.jprotocol.test.AnsonResp_Test;

public class AnDatasetResp_Test extends AnsonResp_Test {

	private List<?> forest;

	public AnDatasetResp_Test(AnsonMsg_Test<AnsonResp_Test> parent, ArrayList<Anson> forest) {
		super(parent);
	}

	public AnDatasetResp_Test(AnsonMsg_Test<? extends AnsonResp_Test> parent) {
		super(parent);
	}

	public AnDatasetResp_Test() {
		super("");
	}

	public AnDatasetResp_Test forest(List<?> lst) {
		forest = lst;
		return this;
	}

	public List<?> forest() { return forest; }
}
