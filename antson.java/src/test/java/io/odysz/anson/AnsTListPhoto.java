package io.odysz.anson;

import java.util.ArrayList;

public class AnsTListPhoto extends Anson {

	ArrayList<Photo[]> ansp;
	
	public AnsTListPhoto() {
	}

	public AnsTListPhoto(String pid) {
		ansp = new ArrayList<Photo[]>();
		ansp.add(new Photo[] { new Photo(pid) });
	}
}
