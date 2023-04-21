package io.odysz.semantic.jprotocol.test;

/**
 * A stub for user's message body extension - subclassing {@link JBody}.
 * @author Ody Zelensky
 *
 */
public class T_UserReq extends T_AnsonBody {
	@SuppressWarnings("unused")
	private String code;
	
	public T_UserReq() {
		super(null, null);
		code = "";
	}

	private T_SemanticObject data;
	public T_UserReq data(String k, Object v) {
		if (k == null) return this;

		if (data == null)
			data = new T_SemanticObject();
		data.put(k, v);
		return this;
	}

	public Object data(String k) {
		return data == null ? null : data.get(k);
	}

	String tabl;
	public String tabl() { return tabl; }

	public T_UserReq(T_AnsonMsg<? extends T_AnsonBody> parent, String conn) {
		super(parent, conn);
	}
	
	public Object get(String prop) {
		return data == null ? null : data.get(prop);
	}
}
