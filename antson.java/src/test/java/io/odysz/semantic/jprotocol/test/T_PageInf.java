package io.odysz.semantic.jprotocol.test;

import java.util.ArrayList;

import io.odysz.anson.Anson;

public class T_PageInf extends Anson {
	public long page;
	public long size;
	public ArrayList<String[]> condts;

	public T_PageInf(long page, int size, String[] whereqs) {
		this.page = page;
		this.size = size;
		this.condts = new ArrayList<String[]>() { { add(whereqs); } };
				
	}
}
