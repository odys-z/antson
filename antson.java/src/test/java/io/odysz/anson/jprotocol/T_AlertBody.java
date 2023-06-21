package io.odysz.anson.jprotocol;

import io.odysz.anson.AnsonField;
import io.odysz.semantic.jprotocol.test.T_AnsonBody;
import io.odysz.semantic.jprotocol.test.T_AnsonMsg.T_Port;

/**<p>A mimic of AnSessionReq for testing error prone issue's alarm</p>
 * @author odys-z@github.com
 */
public class T_AlertBody extends T_AnsonBody {
	@AnsonField(ref=AnsonField.enclosing)
	private T_Alert4User<T_AlertBody> parent_;

	public T_AlertBody() {
		super(null, null); // session's DB access is controlled by server
	}

	/**Session connection is ignored and controlled by server.
	 * @param jmsg
	 */
	public T_AlertBody(T_Alert4User<T_AlertBody> jmsg) {
		super(null, null); // session's DB access is controlled by server
		parent_ = jmsg;
	}

	/**Format login request message.
	 * @param uid
	 * @param tk64
	 * @param iv64
	 * @return login request message
	 */
	public static T_Alert4User<T_AlertBody> formatMsg(String uid, String tk64, String iv64) {
		T_Alert4User<T_AlertBody> jmsg = new T_Alert4User<T_AlertBody>(T_Port.heartbeat);

		T_AlertBody itm = new T_AlertBody(jmsg);
		itm.a("login");

		jmsg.body((T_AnsonBody)itm);
		return jmsg;
	}
}

