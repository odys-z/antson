package io.odysz.semantic.jprotocol.test;

import io.odysz.anson.Anson;

public class T_SessionInf extends Anson {
	public String ssid;
	public String uid;
	public String roleId; 

	public T_SessionInf () {
	}
	
	public T_SessionInf (String ssid, String uid, String... roleId) {
		this.ssid = ssid;
		this.uid = uid;
		this.roleId = roleId == null || roleId.length == 0 ? null : roleId[0];
	}
	
	public String ssid() { return ssid; }
	public String uid() { return uid; }
}
