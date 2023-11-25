package io.odysz.semantic.ext.test;

import java.util.ArrayList;
import java.util.List;

import io.odysz.anson.Anson;
import io.odysz.semantic.jprotocol.test.T_AnsonMsg;
import io.odysz.semantic.jprotocol.test.T_AnsonResp;

public class T_AnDatasetResp extends T_AnsonResp {

	private List<?> forest;

	public T_AnDatasetResp(T_AnsonMsg<T_AnsonResp> parent, ArrayList<Anson> forest) {
		super(parent);
	}

	public T_AnDatasetResp(T_AnsonMsg<? extends T_AnsonResp> parent) {
		super(parent);
	}

	public T_AnDatasetResp() {
		super("");
	}

	public T_AnDatasetResp forest(List<?> lst) {
		forest = lst;
		return this;
	}

	public List<?> forest() { return forest; }
}
