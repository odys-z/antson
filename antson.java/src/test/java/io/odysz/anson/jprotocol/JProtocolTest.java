package io.odysz.anson.jprotocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;
import io.odysz.anson.jprotocol.AnsonMsg;
import io.odysz.anson.x.AnsonException;

class JProtocolTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test_SessionReq() throws AnsonException, IOException {
		String iv64 = "iv: I'm base64";
		String tk64 = "tk: I'm base64";
		String uid = "test-id";
		
		// formatLogin: {a: "login", logid: logId, pswd: tokenB64, iv: ivB64};
		AnsonMsg<AnSessionReq> reqv11 = AnSessionReq.formatLogin(uid, tk64, iv64);

		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		reqv11.toBlock(bos);
		String json = bos.toString(StandardCharsets.UTF_8.name());
		
		// json:
		// {type: io.odysz.anson.jprotocol.AnsonMsg, 
		//  code: null, opts: null, 
		//  port: {type: io.odysz.anson.jprotocol.AnsonMsg$Port, "login.serv11"}, 
		//  header: null, vestion: "1.0", 
		//  body: [{type: io.odysz.anson.jprotocol.AnSessionReq, 
		//          uid: "test-id", a: "login", conn: null, 
		//          iv: "iv: I'm base64", mds: null, 
		//          token: "tk: I'm base64"}], seq: 239}
		@SuppressWarnings("unchecked")
		AnsonMsg<AnSessionReq> msg = (AnsonMsg<AnSessionReq>) Anson.fromJson(json);

		assertEquals(reqv11.code(), msg.code());
		assertEquals(reqv11.port(), msg.port());
		assertEquals(reqv11.body(0).a, msg.body(0).a);
		assertEquals(reqv11.body(0).iv, msg.body(0).iv);
		assertEquals(reqv11.body(0).token, msg.body(0).token);
		assertEquals(msg, msg.body(0).parent);
		
		// TODO test code, which is enum (port is IJsonable)
	}
}
