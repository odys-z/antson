package io.odysz.semantic.jprotocol.test;

/**
 * Test Only
 * 
 * @author odys-z@github.com
 */
public class AnSessionResp_Test extends AnsonResp_Test {

	public SessionInf_Test ssInf;

	public AnSessionResp_Test(AnsonMsg_Test<AnsonResp_Test> parent, String ssid, String uid, String ... roleId) {
		super(parent);
		ssInf = new SessionInf_Test(ssid, uid, roleId == null || roleId.length == 0 ? null : roleId[0]);
		ssInf.ssid = ssid;
		ssInf.uid = uid;
	}

	public AnSessionResp_Test(AnsonMsg_Test<? extends AnsonResp_Test> parent, SessionInf_Test ssInf) {
		super(parent);
		this.ssInf = ssInf;
	}

	public AnSessionResp_Test() {
		super("");
	}

	public SessionInf_Test ssInf() {
		return ssInf;
	}
}
