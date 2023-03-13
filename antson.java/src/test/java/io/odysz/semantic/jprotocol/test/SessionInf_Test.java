package io.odysz.semantic.jprotocol.test;

import io.odysz.anson.Anson;

public class SessionInf_Test extends Anson {
	public String ssid;
	public String uid;
	public String roleId; 

	public SessionInf_Test () {
	}
	
	public SessionInf_Test (String ssid, String uid, String... roleId) {
		this.ssid = ssid;
		this.uid = uid;
		this.roleId = roleId == null || roleId.length == 0 ? null : roleId[0];
	}
	
	public String ssid() { return ssid; }
	public String uid() { return uid; }
}
