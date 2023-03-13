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
public class AnsonResp_Test extends AnsonBody_Test {

	protected String m;
	protected ArrayList<AnsonResultset> rs;
	protected HashMap<String, Object> map;

	public AnsonResp_Test() {
		super(null, null);
	}

	public AnsonResp_Test(AnsonMsg_Test<? extends AnsonResp_Test> parent) {
		super(parent, null);
	}

	public AnsonResp_Test(AnsonMsg_Test<? extends AnsonResp_Test> parent, String txt) {
		super(parent, null);
		this.m = txt;
	}

	public AnsonResp_Test(String txt) {
		super(null, null);
		this.m = txt;
	}

	public String msg() { return m; }

	public AnsonBody_Test rs(AnsonResultset rs) {
		if (this.rs == null)
			this.rs = new ArrayList<AnsonResultset>(1);
		this.rs.add(rs);
		return this;
	}

	public ArrayList<AnsonResultset> rs() { return this.rs; }

	public AnsonResultset rs(int ix) {
		return this.rs == null ? null : this.rs.get(ix);
	}

	public AnsonResp_Test data(HashMap<String, Object> props) {
		this.map = props;
		return this;
	}
	
	public HashMap<String, Object> data () {
		return map;
	}
}
