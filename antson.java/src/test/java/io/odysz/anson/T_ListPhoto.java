package io.odysz.anson;

import java.util.ArrayList;

import io.odysz.anson.utils.NV;

class T_ListPhoto extends Anson {

	ArrayList<AnsPhoto[]> ansp;

	public NV[][] checkRels;
	
	public T_ListPhoto() {
	}

	public T_ListPhoto(String pid) {
		ansp = new ArrayList<AnsPhoto[]>();
		ansp.add(new AnsPhoto[] { new AnsPhoto(pid) });
	}
}
