package io.odysz.semantic.jprotocol.test;

/**
 * A stub for user's message body extension - subclassing {@link JBody}.
 * @author Ody Zelensky
 *
 */
public class UserReq_T extends AnsonBody_Test {
	@SuppressWarnings("unused")
	private String code;
	
	public UserReq_T() {
		super(null, null);
		code = "";
	}

	private SemanticObject_Test data;
	public UserReq_T data(String k, Object v) {
		if (k == null) return this;

		if (data == null)
			data = new SemanticObject_Test();
		data.put(k, v);
		return this;
	}

	public Object data(String k) {
		return data == null ? null : data.get(k);
	}

	String tabl;
	public String tabl() { return tabl; }

	public UserReq_T(AnsonMsg_Test<? extends AnsonBody_Test> parent, String conn) {
		super(parent, conn);
	}
	
	public Object get(String prop) {
		return data == null ? null : data.get(prop);
	}
}
