package io.odysz.semantic.jprotocol.test;

public class AnSessionResp extends AnsonResp {

	public SessionInf ssInf;

	public AnSessionResp(Test_AnsonMsg<AnsonResp> parent, String ssid, String uid, String ... roleId) {
		super(parent);
		ssInf = new SessionInf(ssid, uid, roleId == null || roleId.length == 0 ? null : roleId[0]);
		ssInf.ssid = ssid;
		ssInf.uid = uid;
	}

	public AnSessionResp(Test_AnsonMsg<? extends AnsonResp> parent, SessionInf ssInf) {
		super(parent);
		this.ssInf = ssInf;
	}

	public AnSessionResp() {
		super("");
	}

	public SessionInf ssInf() {
		return ssInf;
	}
}
