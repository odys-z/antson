package io.odysz.semantic.jprotocol.test;

import java.util.ArrayList;
import java.util.HashMap;

import io.odysz.anson.AnsonResultset;

/**
 * Test Only -
 * Anson message response body
 * 
 * @author odys-z@github.com
 */
public class T_AnsonResp extends T_AnsonBody {

	protected String m;
	protected ArrayList<AnsonResultset> rs;
	protected HashMap<String, Object> map;

	public T_AnsonResp() {
		super(null, null);
	}

	public T_AnsonResp(T_AnsonMsg<? extends T_AnsonResp> parent) {
		super(parent, null);
	}

	public T_AnsonResp(T_AnsonMsg<? extends T_AnsonResp> parent, String txt) {
		super(parent, null);
		this.m = txt;
	}

	public T_AnsonResp(String txt) {
		super(null, null);
		this.m = txt;
	}

	public String msg() { return m; }

	public T_AnsonBody rs(AnsonResultset rs) {
		if (this.rs == null)
			this.rs = new ArrayList<AnsonResultset>(1);
		this.rs.add(rs);
		return this;
	}

	public ArrayList<AnsonResultset> rs() { return this.rs; }

	public AnsonResultset rs(int ix) {
		return this.rs == null ? null : this.rs.get(ix);
	}

	public T_AnsonResp data(HashMap<String, Object> props) {
		this.map = props;
		return this;
	}
	
	public HashMap<String, Object> data () {
		return map;
	}
}
