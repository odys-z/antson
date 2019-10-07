package io.odysz.anson;

import java.util.ArrayList;
import java.util.List;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonField;

public class AnsTStrsList extends Anson {

	List<String[]> lst;
	
	List<ArrayList<Object[]>> lst3d;
	
	@AnsonField(valType="java.util.ArrayList/java.util.ArrayList/[Lio.odysz.anson.Anson;")
	ArrayList<ArrayList<Anson[]>>[] dim4;
	
	public AnsTStrsList() { }

	/**
	 * @param ele000 element[0, 0, 0], e.g "0 0 0"
	 * @param ele001 element[0, 1, 0], e.g "0 1 0"
	 */
	@SuppressWarnings({ "serial", "unchecked" })
	public AnsTStrsList(Object ele000, Object ele010) {
		lst = new ArrayList<String[]>();
		lst3d = new ArrayList<ArrayList<Object[]>>();
		
		lst3d.add(new ArrayList<Object[]>() {
				{ add(new Object[] {ele000, ""}); };
				{ add(new Object[] {ele010 }); }
		});
		
		dim4 = (ArrayList<ArrayList<Anson[]>>[]) new ArrayList[2];
		dim4[0] = new ArrayList<ArrayList<Anson[]>>();
		dim4[0].add(new ArrayList<Anson[]>());
		dim4[0].get(0).add(new Anson[2]);
		dim4[0].get(0).add(new Anson[2]);
		dim4[0].add(new ArrayList<Anson[]>());
		dim4[0].get(1).add(new Anson[2]);
		dim4[0].get(1).add(new Anson[2]);

		dim4[1] = new ArrayList<ArrayList<Anson[]>>();
		dim4[1].add(new ArrayList<Anson[]>());
		dim4[1].get(0).add(new Anson[2]);
		dim4[1].get(0).add(new Anson[2]);
		dim4[1].add(new ArrayList<Anson[]>());
		dim4[1].get(1).add(new Anson[2]);
		dim4[1].get(1).add(new Anson[2]);
	}
	
	public AnsTStrsList add(String e0, String e1, String... e2) {
		lst.add(new String[] {e0, e1, e2 == null || e2.length == 0 ? null : e2[0]});
		return this;
	}

	public AnsTStrsList add3Drow(ArrayList<Object[]> row) { 
		lst3d.add(row);
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
		return lst3d.get(i).get(j)[k];
	}

	public Object cell(int i, int j, int k, int h) {
		return dim4[i].get(j).get(k)[h];
	}
	
//	public AnsTStrsList addDim4(int rx, ArrayList<Anson[]> cell2d) {
//		if (dim4[rx] == null)
//			dim4[rx] = new ArrayList<ArrayList<Anson[]>>();
//		dim4[rx].add(cell2d);
//		return this;
//	}
	
	public AnsTStrsList set4dcell(int x, int y, int z, int w, Anson c) {
		dim4[x].get(y).get(z)[w] = c;
		return this;
	}
}
