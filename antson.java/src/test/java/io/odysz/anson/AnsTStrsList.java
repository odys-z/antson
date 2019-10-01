package io.odysz.anson;

import java.util.ArrayList;
import java.util.List;

public class AnsTStrsList extends Anson {

	List<String[]> lst;
	
	public AnsTStrsList() {
		lst = new ArrayList<String[]>();
	}
	
	public AnsTStrsList add(String e0, String e1, String... e2) {
		lst.add(new String[] {e0, e1, e2 == null || e2.length == 0 ? null : e2[0]});
		return this;
	}

	public AnsTStrsList addnull() { 
		lst.add(null);
		return this;
	}

	public AnsTStrsList add0row() { 
		lst.add(new String[] {});
		return this;
	}
	
	public String cell(int x, int y) {
		return lst.get(x)[y];
	}
	
	public String[] row(int r) {
		return lst.get(r);
	}
}
