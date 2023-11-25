package io.odysz.semantic.jprotocol.test;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.odysz.anson.Anson;

/**
 * Test Only
 * 
 * @author odys-z@github.com
 */
public class T_AnsonHeader extends Anson {

	String uid;
	String ssid;
	String iv64;
	String[] usrAct;

	public T_AnsonHeader(String ssid, String uid) {
		this.uid = uid;
		this.ssid = ssid;
	}
	
	public T_AnsonHeader() { }

	public String logid() {
		return uid;
	}

	public String ssid() {
		return ssid;
	}

	/**
	 * @return js equivalent {md: ssinf.md, ssid: ssinf.ssid, uid: ssinf.uid, iv: ssinf.iv};
	 */
	public static T_AnsonHeader format(String uid, String ssid) {
		// formatLogin: {a: "login", logid: logId, pswd: tokenB64, iv: ivB64};
		return new T_AnsonHeader(ssid, uid);
	}

	public T_AnsonHeader act(String[] act) {
		usrAct = act;
		return this;
	}

	public static String[] usrAct(String funcId, String cmd, String cate, String remarks) {
		return new String[] {funcId, cate, cmd, remarks};
	}
	
	/**For test. The string can not been used for json data.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{ssid: %s, uid: %s, iv64: %s,\n\t\tuserAct: %s}",
				ssid, uid, iv64, usrAct == null ? null :
					Arrays.stream(usrAct).collect(Collectors.joining(", ", "[", "]")));
	}

}
