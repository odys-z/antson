package io.odysz.anson.types;

import java.util.ArrayList;
import java.util.List;

import io.odysz.anson.Anson;

public class AnsTList extends Anson {

	public List<String> lst;
	
	public List<AnsT3> anss;
	
	public AnsTList() {
		lst = new ArrayList<String>();
	}
}
