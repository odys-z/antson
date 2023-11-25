package io.odysz.anson;

import java.util.ArrayList;
import java.util.List;

public class AnsTList extends Anson {

	List<String> lst;
	
	List<AnsT3> anss;

	List<? extends Anson> ans2;
	
	public AnsTList() {
		lst = new ArrayList<String>();
	}
}
