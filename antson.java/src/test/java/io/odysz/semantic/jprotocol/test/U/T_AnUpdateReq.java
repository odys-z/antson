package io.odysz.semantic.jprotocol.test.U;

import java.util.ArrayList;

import io.odysz.semantic.jprotocol.test.T_AnsonBody;
import io.odysz.semantic.jprotocol.test.T_AnsonHeader;
import io.odysz.semantic.jprotocol.test.T_AnsonMsg;

public class T_AnUpdateReq extends T_AnsonBody {
	String mtabl;
	public ArrayList<Object[]> nvs;
	public ArrayList<ArrayList<Object[]>> nvss;
	protected String[] cols;
	String limt;
	ArrayList<Object[]> where;

	public ArrayList<T_AnUpdateReq> postUpds;
	
	public T_AnsonHeader header;

	ArrayList<Object[]> attacheds;

	
	public T_AnUpdateReq() {
		super(null, null);
	}

	protected T_AnUpdateReq(T_AnsonMsg<? extends T_AnsonBody> parent, String uri) {
		super(parent, uri);
	}

}
