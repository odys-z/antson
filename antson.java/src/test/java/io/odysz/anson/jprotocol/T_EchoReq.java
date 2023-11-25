package io.odysz.anson.jprotocol;

import io.odysz.semantic.jprotocol.test.T_AnsonBody;
import io.odysz.semantic.jprotocol.test.T_AnsonMsg;

public class T_EchoReq extends T_AnsonBody {

	public static class A {
		public static final String echo = "echo";
		/** query interfaces, only response to localhost */
		public static final String inet = "inet";
	}

	public T_EchoReq() {
		super(null, null);
	}

	public T_EchoReq(T_AnsonMsg<? extends T_AnsonBody> parent) {
		super(parent, null);
	}
}
