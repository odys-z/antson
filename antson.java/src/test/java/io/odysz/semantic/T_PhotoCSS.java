package io.odysz.semantic;

import io.odysz.anson.Anson;

public class T_PhotoCSS extends Anson {
	public int size[];
	
	public T_PhotoCSS() {
		size = new int[] { 0, 0 };
	}

	public T_PhotoCSS(int w, int h) {
		size = new int[] {w, h};
	}
}
