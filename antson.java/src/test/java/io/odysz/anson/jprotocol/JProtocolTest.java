package io.odysz.anson.jprotocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;
import io.odysz.anson.jprotocol.AnsonMsg;
import io.odysz.anson.x.AnsonException;
import io.odysz.anson.jprotocol.AnsonResp;
import io.odysz.anson.jprotocol.AnsonMsg.MsgCode;
import io.odysz.anson.jprotocol.AnsonMsg.Port;

class JProtocolTest {
	static final String iv64 = "iv: I'm base64";
	static final String tk64 = "tk: I'm base64";
	static final String uid = "test-id";
	static final String ssid = "ssid-base64";

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test_SessionReq() throws AnsonException, IOException {
		// formatLogin: {a: "login", logid: logId, pswd: tokenB64, iv: ivB64};
		AnsonMsg<AnSessionReq> reqv11 = AnSessionReq.formatLogin(uid, tk64, iv64);

		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		reqv11.toBlock(bos);
		String json = bos.toString(StandardCharsets.UTF_8.name());
		
		// json:
		// {type: io.odysz.anson.jprotocol.AnsonMsg,
		//  code: null, opts: null,
		//  port: "session",
		//  header: null, vestion: "1.0", 
		//  body: [{type: io.odysz.anson.jprotocol.AnSessionReq,
		//          uid: "test-id", 
		//          parent: "io.odysz.anson.jprotocol.AnsonMsg",
		//          a: "login", conn: null,
		//          iv: "iv: I'm base64", mds: null,
		//          token: "tk: I'm base64"}], seq: 909}
		@SuppressWarnings("unchecked")
		AnsonMsg<AnSessionReq> msg = (AnsonMsg<AnSessionReq>) Anson.fromJson(json);

		assertEquals(reqv11.code(), msg.code());
		assertEquals(reqv11.port(), msg.port());
		assertEquals(reqv11.body(0).a, msg.body(0).a);
		assertEquals("login", msg.body(0).a);
		assertEquals(reqv11.body(0).iv, msg.body(0).iv);
		assertEquals(reqv11.body(0).token, msg.body(0).token);
		assertEquals(msg, msg.body(0).parent);
	}
	
	@Test
	void test_SessionResp() throws AnsonException, IOException {
		SessionInf ssinf = new SessionInf(ssid, uid);
		AnSessionResp bd = new AnSessionResp(null, ssinf);
		AnsonMsg<AnSessionResp> resp = ok(Port.session, bd);

		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		resp.toBlock(bos);
		String json = bos.toString(StandardCharsets.UTF_8.name());

		// {type: io.odysz.anson.jprotocol.AnsonMsg,
		//  code: "ok", opts: null,
		//  port: "session", header: null, vestion: "1.0", 
		//  body: [{type: io.odysz.anson.jprotocol.AnSessionResp, 
		//          rs: null, 
		//          parent: "io.odysz.anson.jprotocol.AnsonMsg",
		//          a: null, conn: null, 
		//          ssInf: {type: io.odysz.anson.jprotocol.SessionInf, 
		//                  uid: "test-id", roleId: null, 
		//                  ssid: "ssid-base64"}, m: null, map: null}], seq: 0}
		@SuppressWarnings("unchecked")
		AnsonMsg<AnSessionResp> msg = (AnsonMsg<AnSessionResp>) Anson.fromJson(json);

		assertEquals(MsgCode.ok, msg.code());
		assertEquals(resp.port(), msg.port());
		assertEquals(ssid, msg.body(0).ssInf.ssid);
		assertEquals(uid, msg.body(0).ssInf.uid);
		assertEquals(resp.body(0).ssInf.ssid, msg.body(0).ssInf.ssid);
		assertEquals(resp.body(0).ssInf.uid, msg.body(0).ssInf.uid);
		assertEquals(msg, msg.body(0).parent);
	}
	
	@Test
	void test_insertResp() throws AnsonException, IOException {
		
		HashMap<String, Object> props = new HashMap<String, Object>(2);
		props.put("resulved", new SemanticObjV11());
		((SemanticObjV11) props.get("resulved")).add("recId", "000f");
		((SemanticObjV11) props.get("resulved")).add("vec", "000x");
		props.put("res", new int[] { 1, 20 });

		AnsonMsg<AnsonResp> resp = new AnsonMsg<AnsonResp>(Port.insert, MsgCode.ok)
				.body(new AnsonResp().data(props));
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		resp.toBlock(bos);
		String json = bos.toString(StandardCharsets.UTF_8.name());

		@SuppressWarnings("unchecked")
		AnsonMsg<AnSessionResp> msg = (AnsonMsg<AnSessionResp>) Anson.fromJson(json);

		assertEquals(MsgCode.ok, msg.code());
		assertEquals(resp.port(), msg.port());
		assertEquals("000f", ((SemanticObjV11) msg.body(0).data().get("resulved")).get("recId"));
		assertEquals("000x", ((SemanticObjV11) msg.body(0).data().get("resulved")).get("vec"));
		assertEquals(1, ((int[]) msg.body(0).data().get("res"))[0]);
		assertEquals(20, ((int[]) msg.body(0).data().get("res"))[1]);
	}

	static <U extends AnsonResp> AnsonMsg<U> ok(Port p, U body) {
		AnsonMsg<U> msg = new AnsonMsg<U>(p, MsgCode.ok);
		msg.body(body);
		return msg;
	}
}
