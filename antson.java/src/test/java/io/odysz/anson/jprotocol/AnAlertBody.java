package io.odysz.anson.jprotocol;

import io.odysz.anson.AnsonField;
import io.odysz.semantic.jprotocol.test.AnsonBody_Test;
import io.odysz.semantic.jprotocol.test.AnsonMsg_Test.Port;

/**<p>A mimic of AnSessionReq for testing error prone issue's alarm</p>
 * @author odys-z@github.com
 */
public class AnAlertBody extends AnsonBody_Test {
	@AnsonField(ref=AnsonField.enclosing)
	private AnAlert4User<AnAlertBody> parent_;

	public AnAlertBody() {
		super(null, null); // session's DB access is controlled by server
	}

	/**Session connection is ignored and controlled by server.
	 * @param jmsg
	 */
	public AnAlertBody(AnAlert4User<AnAlertBody> jmsg) {
		super(null, null); // session's DB access is controlled by server
		parent_ = jmsg;
	}

	/**Format login request message.
	 * @param uid
	 * @param tk64
	 * @param iv64
	 * @return login request message
	 */
	public static AnAlert4User<AnAlertBody> formatMsg(String uid, String tk64, String iv64) {
		AnAlert4User<AnAlertBody> jmsg = new AnAlert4User<AnAlertBody>(Port.heartbeat);

		AnAlertBody itm = new AnAlertBody(jmsg);
		itm.a("login");

		jmsg.body((AnsonBody_Test)itm);
		return jmsg;
	}
}

