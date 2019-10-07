package io.odysz.anson.jprotocol;

import java.util.ArrayList;
import java.util.HashMap;

import io.odysz.anson.AnsonResultset;

/**Anson message response body
 * @author odys-z@github.com
 */
public class AnsonResp extends AnsonBody {

	private String m;
	private ArrayList<AnsonResultset> rs;
	private HashMap<String, Object> map;

	public AnsonResp() {
		super(null, null);
	}

	public AnsonResp(AnsonMsg<? extends AnsonResp> parent) {
		super(parent, null);
	}

	public AnsonResp(AnsonMsg<? extends AnsonResp> parent, String txt) {
		super(parent, null);
		this.m = txt;
	}

	public AnsonResp(String txt) {
		super(null, null);
		this.m = txt;
	}

	public String msg() { return m; }

	public AnsonBody rs(AnsonResultset rs) {
		if (this.rs == null)
			this.rs = new ArrayList<AnsonResultset>(1);
		this.rs.add(rs);
		return this;
	}

	public ArrayList<AnsonResultset> rs() { return this.rs; }

	public AnsonResultset rs(int ix) {
		return this.rs == null ? null : this.rs.get(ix);
	}

	public AnsonResp data(HashMap<String, Object> props) {
		this.map = props;
		return this;
	}
	
	public HashMap<String, Object> data () {
		return map;
	}
}
