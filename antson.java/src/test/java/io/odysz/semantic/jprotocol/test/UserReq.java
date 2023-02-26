package io.odysz.semantic.jprotocol.test;

/**A stub for user's message body extension - subclassing {@link JBody}.
 * @author ody
 *
 */
public class UserReq extends AnsonBody {
	@SuppressWarnings("unused")
	private String code;
	
	public UserReq() {
		super(null, null);
		code = "";
	}

	private SemanticObject data;
	public UserReq data(String k, Object v) {
		if (k == null) return this;

		if (data == null)
			data = new SemanticObject();
		data.put(k, v);
		return this;
	}

	public Object data(String k) {
		return data == null ? null : data.get(k);
	}

	String tabl;
	public String tabl() { return tabl; }

	public UserReq(Test_AnsonMsg<? extends AnsonBody> parent, String conn) {
		super(parent, conn);
	}
	
	public Object get(String prop) {
		return data == null ? null : data.get(prop);
	}
}
