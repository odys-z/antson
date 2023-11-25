package io.odysz.anson;

import java.util.ArrayList;

public class AnsTListPhoto extends Anson {

	ArrayList<AnsPhoto[]> ansp;
	
	public AnsTListPhoto() {
	}

	public AnsTListPhoto(String pid) {
		ansp = new ArrayList<AnsPhoto[]>();
		ansp.add(new AnsPhoto[] { new AnsPhoto(pid) });
	}
}
