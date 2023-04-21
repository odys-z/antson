package io.odysz.semantic.jprotocol.test;

import java.util.HashMap;

import io.odysz.common.LangExt;
import io.odysz.semantic.jprotocol.test.T_AnsonMsg.Port;

/**
 * Test Only
 * 
 * <p>Sessin Request<br>
 * a = "login" | "logout" | "heartbeat" ...</p>
 * @author odys-z@github.com
 */
public class T_AnSessionReq extends T_AnsonBody {
	public T_AnSessionReq() {
		super(null, null); // session's DB access is controlled by server
	}

	/**Session connection is ignored and controlled by server.
	 * @param parent
	 */
	public T_AnSessionReq(T_AnsonMsg<T_AnSessionReq> parent) {
		super(parent, null); // session's DB access is controlled by server
	}

	public String uid;
	String token;
	public String token() { return token; }
	String iv;
	public String iv() { return iv; }

	HashMap<String,Object> mds;
	public String md(String k) { return mds == null ? null : (String) mds.get(k); }
	public T_AnSessionReq md(String k, String md) {
		if (k == null || LangExt.isblank(md))
			return this;
		if (mds == null)
			mds = new HashMap<String, Object>();
		mds.put(k, md);
		return this;
	}

	public String uid() { return uid; }

	/**Format login request message.
	 * @param uid
	 * @param tk64
	 * @param iv64
	 * @return login request message
	 */
	public static T_AnsonMsg<T_AnSessionReq> formatLogin(String uid, String tk64, String iv64) {
		T_AnsonMsg<T_AnSessionReq> jmsg = new T_AnsonMsg<T_AnSessionReq>(Port.session);

		T_AnSessionReq itm = new T_AnSessionReq(jmsg);
		itm.uid = uid;
		itm.a("login");

		itm.setup(uid, tk64, iv64);

		jmsg.body((T_AnsonBody)itm);
		return jmsg;
	}

	private void setup(String uid, String tk64, String iv64) {
		this.uid = uid;
		this.token = tk64;
		this.iv = iv64;
	}
}

