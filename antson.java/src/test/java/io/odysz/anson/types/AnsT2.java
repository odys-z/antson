package io.odysz.anson.types;

import io.odysz.anson.Anson;

public class AnsT2 extends Anson {
	public int s;

	public String[] m;
	
	public AnsT2() {}
	
	public AnsT2(String e0, String... e1_) {
		int l = e1_ == null ? 1 : e1_.length + 1;
		m = new String[l];
		m[0] = e0;
		for (int i = 1; i < l; i++) {
			m[l] = e1_[l - 1];
		}
	}
}
