package io.odysz.semantic.ext.test;

import java.util.ArrayList;
import java.util.List;

import io.odysz.anson.Anson;
import io.odysz.semantic.jprotocol.test.AnsonMsg;
import io.odysz.semantic.jprotocol.test.AnsonResp;

public class AnDatasetResp extends AnsonResp {

	private List<?> forest;

	public AnDatasetResp(AnsonMsg<AnsonResp> parent, ArrayList<Anson> forest) {
		super(parent);
	}

	public AnDatasetResp(AnsonMsg<? extends AnsonResp> parent) {
		super(parent);
	}

	public AnDatasetResp() {
		super("");
	}

	public AnDatasetResp forest(List<?> lst) {
		forest = lst;
		return this;
	}

	public List<?> forest() { return forest; }
}
