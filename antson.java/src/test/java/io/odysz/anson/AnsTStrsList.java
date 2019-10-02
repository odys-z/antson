package io.odysz.anson;

import java.util.ArrayList;
import java.util.List;

public class AnsTStrsList extends Anson {

	List<String[]> lst;
	
	@AnsonField(ignoreFrom=true)
	List<ArrayList<Object>[]> lst3d;
	
	public AnsTStrsList() { }

	/**
	 * @param ele000 element[0, 0, 0], e.g "0 0 0"
	 * @param ele001 element[0, 1, 0], e.g "0 1 0"
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public AnsTStrsList(Object ele000, Object ele010) {
		lst = new ArrayList<String[]>();
		lst3d = new ArrayList<ArrayList<Object>[]>();
		
		lst3d.add(new ArrayList[] {
				new ArrayList<Object>() { {add(ele000);}; {add("");} },
				new ArrayList<Object>() { {add(ele010);} }
		});
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

	public Object cell(int i, int j, int k) {
		return lst3d.get(i)[j].get(k);
	}
}
