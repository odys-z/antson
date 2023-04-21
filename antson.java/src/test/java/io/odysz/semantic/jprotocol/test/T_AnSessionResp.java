package io.odysz.semantic.jprotocol.test;

/**
 * Test Only
 * 
 * @author odys-z@github.com
 */
public class T_AnSessionResp extends T_AnsonResp {

	public T_SessionInf ssInf;

	public T_AnSessionResp(T_AnsonMsg<T_AnsonResp> parent, String ssid, String uid, String ... roleId) {
		super(parent);
		ssInf = new T_SessionInf(ssid, uid, roleId == null || roleId.length == 0 ? null : roleId[0]);
		ssInf.ssid = ssid;
		ssInf.uid = uid;
	}

	public T_AnSessionResp(T_AnsonMsg<? extends T_AnsonResp> parent, T_SessionInf ssInf) {
		super(parent);
		this.ssInf = ssInf;
	}

	public T_AnSessionResp() {
		super("");
	}

	public T_SessionInf ssInf() {
		return ssInf;
	}
}
