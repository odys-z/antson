package io.oz.jserv.docs.syn;

import io.odysz.anson.Anson;

public class T_Device extends Anson {

	final String org;
	String devname;
	String id;
	
	String tofolder;
	String synode0;
	
	public T_Device(String synconn, String org, String devid, String devname) {
		this.org = org;
		this.id = devid;
		this.devname = devname;
	}
}
