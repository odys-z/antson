package io.odysz.anson;

import java.util.ArrayList;

import io.odysz.anson.utils.T_NV;

class T_ListPhoto extends Anson {

	ArrayList<AnsPhoto[]> ansp;

	public T_NV[][] checkRels;
	
	public T_ListPhoto() {
	}

	public T_ListPhoto(String pid) {
		ansp = new ArrayList<AnsPhoto[]>();
		ansp.add(new AnsPhoto[] { new AnsPhoto(pid) });
	}
}
