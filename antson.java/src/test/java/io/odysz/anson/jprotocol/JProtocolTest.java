package io.odysz.anson.jprotocol;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonResultset;
import io.odysz.anson.x.AnsonException;
import io.odysz.semantic.ext.test.AnDatasetResp;
import io.odysz.semantic.jprotocol.test.AnSessionReq;
import io.odysz.semantic.jprotocol.test.AnSessionResp;
import io.odysz.semantic.jprotocol.test.AnsonMsg;
import io.odysz.semantic.jprotocol.test.AnsonResp;
import io.odysz.semantic.jprotocol.test.SemanticObject;
import io.odysz.semantic.jprotocol.test.SessionInf;
import io.odysz.semantic.jprotocol.test.TransException;
import io.odysz.semantic.jprotocol.test.UserReq;
import io.odysz.semantic.jprotocol.test.AnsonMsg.MsgCode;
import io.odysz.semantic.jprotocol.test.AnsonMsg.Port;

class JProtocolTest {
	static final String iv64 = "iv: I'm base64";
	static final String tk64 = "tk: I'm base64";
	static final String uid = "test-id";
	static final String ssid = "ssid-base64";

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	@SuppressWarnings("unchecked")
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
		AnsonMsg<AnSessionReq> msg = (AnsonMsg<AnSessionReq>) Anson.fromJson(json);

		assertEquals(reqv11.code(), msg.code());
		assertEquals(reqv11.port(), msg.port());
		assertEquals(reqv11.body(0).a(), msg.body(0).a());
		assertEquals("login", msg.body(0).a());
		assertTrue(msg.body(0) instanceof AnSessionReq);
		assertEquals(reqv11.body(0).iv(), msg.body(0).iv());
		assertEquals(reqv11.body(0).token(), msg.body(0).token());
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
	void test_insertResp() throws AnsonException, IOException, TransException {
		
		HashMap<String, Object> props = new HashMap<String, Object>(2);
		props.put("resulved", new SemanticObject());
		((SemanticObject) props.get("resulved")).add("recId", "000f");
		((SemanticObject) props.get("resulved")).add("vec", "000x");
		props.put("res", new int[] { 1, 20 });

		AnsonMsg<AnsonResp> resp = new AnsonMsg<AnsonResp>(Port.insert, MsgCode.ok)
				.body(new AnsonResp().data(props));
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		resp.toBlock(bos);
		String json = bos.toString(StandardCharsets.UTF_8.name());

		@SuppressWarnings("unchecked")
		AnsonMsg<AnsonResp> msg = (AnsonMsg<AnsonResp>) Anson.fromJson(json);

		assertEquals(MsgCode.ok, msg.code());
		assertEquals(resp.port(), msg.port());
		HashMap<String, Object> props2 = msg.body(0).data();
		SemanticObject resulved = (SemanticObject) props2.get("resulved");
		assertEquals("000f", ((ArrayList<?>)resulved.get("recId")).get(0));
		assertEquals("000x", ((ArrayList<?>)resulved.get("vec")).get(0));
		
		// NOTE: for SemanticObject.props, all added element are stored as list, can't cast to int[].
		assertEquals(1, ((ArrayList<?>) props2.get("res")).get(0));
		assertEquals(20, ((ArrayList<?>)props2.get("res")).get(1));
	}

	@Test
	void test_datasetResp() throws AnsonException, IOException, SQLException {
		AnsonResultset rs = new AnsonResultset(2, 2);
		AnDatasetResp dr = (AnDatasetResp) new AnDatasetResp().rs(rs);
		AnsonMsg<AnDatasetResp> resp = ok(Port.dataset, dr);

		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		resp.toBlock(bos);
		String json = bos.toString(StandardCharsets.UTF_8.name());

		// {"type": "io.odysz.semantic.jprotocol.test.AnsonMsg", "code": "ok", "opts": null, "port": "dataset", "header": null, "vestion": "1.0", "body": [{"type": "io.odysz.semantic.ext.test.AnDatasetResp", "rs": [{"type": "io.odysz.anson.AnsonResultset", "stringFormats": null, "total": 0, "rowCnt": 2, "colCnt": 2, "colnames": {"1": [1, "1"], "2": [2, "2"]}, "rowIdx": 0, "results": [["0, 1", "0, 2"], ["1, 1", "1, 2"]]}], "parent": "io.odysz.semantic.jprotocol.test.AnsonMsg", "a": null, "forest": null, "conn": null, "m": "", "map": null}], "seq": 0}

		@SuppressWarnings("unchecked")
		AnsonMsg<AnDatasetResp> msg = (AnsonMsg<AnDatasetResp>) Anson.fromJson(json);

		assertEquals(MsgCode.ok, msg.code());
		assertEquals(resp.port(), msg.port());
		assertEquals(msg, msg.body(0).parent);
		rs = msg.body(0).rs(0);
		rs.beforeFirst().next();
		assertEquals("0, 1", rs.getString("1"));
		assertEquals("0, 2", rs.getString("2"));
		rs.next();
		assertEquals("1, 1", rs.getString("1"));
		assertEquals("1, 2", rs.getString("2"));
	}

	@SuppressWarnings("unchecked")
	@Test
	void test_js_login() throws AnsonException {
		String req = "{\"type\":\"io.odysz.semantic.jprotocol.test.AnsonMsg\","
				+ "\"version\":\"1.1\",\"seq\":421,"
				+ "\"opts\":{\"noNull\":true,\"noBoolean\":false,\"doubleFormat\":\".2f\"},"
				+ "\"port\":\"session\",\"header\":{},"
				+ "\"body\":[{\"type\":io.odysz.semantic.jprotocol.test.AnSessionReq," // identifier as type name
					+ "\"uid\":\"admin\",\"token\":\"ZfSigOt9vrtWFWHg4c6v0A==\","
					+ "\"iv\":\"KikpUxk0GREELlU7KGJUJw==\","
					+ "\"a\":\"login\"}]}";
	
		AnsonMsg<AnSessionReq> msg = (AnsonMsg<AnSessionReq>) Anson.fromJson(req);

		assertEquals(null, msg.code());
		assertEquals(Port.session, msg.port());
		assertEquals("login", msg.body(0).a());
		assertEquals("admin", msg.body(0).uid);
		assertEquals(24, msg.body(0).iv().length());
		assertEquals(24, msg.body(0).token().length());
		
		req = "{\"type\":\"io.odysz.semantic.jprotocol.test.AnsonMsg\",\"version\":\"1.1\",\"seq\":218,\"opts\":{\"noNull\":true,\"noBoolean\":false,\"doubleFormat\":\".2f\"},\"port\":\"session\",\"header\":{},\"body\":[{\"type\":\"io.odysz.semantic.jprotocol.test.AnSessionReq\",\"uid\":\"admin\",\"token\":\"OIuFP3XNGXeLK2Yec9WVRw==\",\"iv\":\"FWMLMy5aBBk3GDEFUwMfGA==\",\"a\":\"login\"}]}";
		msg = (AnsonMsg<AnSessionReq>) Anson.fromJson(req);
		assertEquals(null, msg.code());
		assertEquals(Port.session, msg.port());
		assertEquals("login", msg.body(0).a());
		assertEquals("admin", msg.body(0).uid);
		assertEquals(24, msg.body(0).iv().length());
		assertEquals(24, msg.body(0).token().length());
	}

	@Test
	void test_js_logout() throws AnsonException {
		// NOTES: an envelope must has a type property
		// A real case debugging
		// json = "{\"type\":\"io.odysz.semantic.jprotocol.AnsonMsg\",\"version\":\"0.9\",\"seq\":256,\"port\":\"session\",\"opts\":{},\"header\":{\"type\":\"io.odysz.semantic.jprotocol.AnsonHeader\",\"ssid\":\"0014eKTs\",\"uid\":\"admin\"},\"body\":[{\"a\":\"logout\",\"parent\":\"io.odysz.semantic.jprotocol.AnsonMsg\"}]}";
		String json = "{\"type\":\"io.odysz.semantic.jprotocol.test.AnsonMsg\","
			+   "\"version\":\"0.9\",\"seq\":256,\"port\":\"session\",\"opts\":{},"
			+   "\"header\":{\"type\":\"io.odysz.semantic.jprotocol.test.AnsonHeader\",\"ssid\":\"0014eKTs\",\"uid\":\"admin\"},"
			+   "\"body\":[{\"type\":io.odysz.semantic.jprotocol.test.AnSessionReq,"
			+              "\"a\":\"logout\",\"parent\":\"io.odysz.semantic.jprotocol.AnsonMsg\"}]}";
			
		@SuppressWarnings("unchecked")
		AnsonMsg<AnSessionReq> msg = (AnsonMsg<AnSessionReq>) AnsonMsg.fromJson(json);
		assertEquals(null, msg.code());
		assertEquals(Port.session, msg.port());
		assertEquals("0014eKTs", msg.header().ssid());
		assertEquals("admin", msg.header().logid());
		assertTrue(msg.body(0) instanceof AnSessionReq);
		assertEquals("logout", msg.body(0).a());
		assertEquals(msg, msg.body(0).parent);
	}
	
    String userReqJsonReduced =  
		"{ \"type\": \"io.odysz.semantic.jprotocol.test.AnsonMsg\", \"version\": \"0.9\", \"seq\": 238, \"port\": \"quiz\", \"opts\": {}," + 
          "\"header\": { \"type\": \"io.odysz.semantic.jprotocol.test.AnsonHeader\", \"ssid\": \"001ETPAo\", \"uid\": \"becky\" }," + 
          "\"body\": [" + 
            "{ \"type\": \"io.odysz.semantic.jprotocol.test.UserReq\", \"a\": \"update\"," + 
              "\"parent\": \"io.odysz.semantic.jprotocol.test.AnsonMsg\"," + 
              "\"uri\": \"/n/quizzes\", \"tabl\": \"quizzes\"," + 
              "\"data\": {" + 
                "\"props\": {" + 
                  "\"quizId\": \"000003\", \"qtitle\": \"Emotion Poll (Type A)\", \"quizinfo\": null," + 
                  "\"questions\": [" + 
                    "[ [ \"question\", \"Test 1\" ]], " + 
                    "[ [ \"question\", \"学习压力\" ]], " + 
                    "[ [ \"question\", \"父母关系\" ]]" + 
                  "]" + 
                "}" + 
              "}" + 
            "}" + 
          "]" + 
        "}";	
 
    String userReqJson =  
		"{ \"type\": \"io.odysz.semantic.jprotocol.test.AnsonMsg\", \"version\": \"0.9\", \"seq\": 238, \"port\": \"quiz\", \"opts\": {}," + 
          "\"header\": { \"type\": \"io.odysz.semantic.jprotocol.test.AnsonHeader\", \"ssid\": \"001ETPAo\", \"uid\": \"becky\" }," + 
          "\"body\": [" + 
            "{ \"type\": \"io.odysz.semantic.jprotocol.test.UserReq\", \"a\": \"update\"," + 
              "\"parent\": \"io.odysz.semantic.jprotocol.test.AnsonMsg\"," + 
              "\"uri\": \"/n/quizzes\", \"tabl\": \"quizzes\"," + 
              "\"data\": {" + 
                "\"props\": {" + 
                  "\"quizId\": \"000003\", \"qtitle\": \"Emotion Poll (Type A)\", \"quizinfo\": null," + 
                  "\"questions\": [" + 
                    "[ [ \"question\", \"Test 1\" ], [ \"answers\", \"\" ], [ \"qtype\", \"\" ], [ \"answer\", null ], [ \"quizId\", null ], [ \"qorder\", 0 ], [ \"shortDesc\", \"Test 1\" ], [ \"hints\", null ], [ \"extra\", null ]], " + 
                    "[ [ \"question\", \"学习压力\" ], [ \"answers\", \"\" ], [ \"qtype\", \"s\" ], [ \"answer\", null ], [ \"quizId\", null ], [ \"qorder\", 1 ], [ \"shortDesc\", \"学习压力\" ], [ \"hints\", null ], [ \"extra\", null ]], " + 
                    "[ [ \"question\", \"父母/家庭关系父母/家庭关系父母/家庭关系父母/家庭关系\" ], [ \"answers\", \"vv\" ], [ \"qtype\", \"1\" ], [ \"answer\", \"\" ], [ \"quizId\", null ], [ \"qorder\", 1 ], [ \"shortDesc\", \"父母/家庭关系\" ], [ \"hints\", null ], [ \"extra\", null ] ]" + 
                  "]" + 
                "}" + 
              "}" + 
            "}" + 
          "]" + 
        "}";	
    
    String jsonPollsUsers = "{ \"type\": \"io.odysz.semantic.jprotocol.test.AnsonMsg\"," +
    	    "\"version\": \"0.9\", \"seq\": 14, \"port\": \"quiz\", \"opts\": {}," +
    	    "\"header\": { \"type\": \"io.odysz.semantic.jprotocol.test.AnsonHeader\", \"ssid\": \"001AzjA9\", \"uid\": \"becky\" }," +
    	    "\"body\": [ {" +
    	            "\"type\": \"io.odysz.semantic.jprotocol.test.UserReq\"," +
    	            "\"a\": \"polls-users\"," +
    	            "\"parent\": \"io.odysz.semantic.jprotocol.test.AnsonMsg\"," +
    	            "\"data\": { \"props\": {} }" +
    	        "}" +
    	    "]" +
    	"}";
    /**TODO TODO TODO TODO TODO the c# version could also need this test case.
     * @throws AnsonException
     */
	@SuppressWarnings("unchecked")
    @Test
	void test_js_userReq() throws AnsonException {
    	AnsonMsg<UserReq> pollsUsers = (AnsonMsg<UserReq>) AnsonMsg.fromJson(jsonPollsUsers);
		assertEquals("polls-users", pollsUsers.body(0).a());

		AnsonMsg<UserReq> reduced = (AnsonMsg<UserReq>) AnsonMsg.fromJson(userReqJsonReduced);
		assertEquals(null, reduced.code());

		ArrayList<?> rques = (ArrayList<?>) reduced.body(0).data("questions");
		assertEquals(3, rques.size());
		Object r0 = rques.get(0);
		assertEquals("question", ((String[][]) r0)[0][0].toString());
		assertEquals("Test 1", ((String[][]) r0)[0][1].toString());
		Object r1 = rques.get(1);
		assertEquals("question", ((String[][]) r1)[0][0].toString());
		assertEquals("学习压力", ((String[][]) r1)[0][1].toString());

		AnsonMsg<UserReq> msg = (AnsonMsg<UserReq>) AnsonMsg.fromJson(userReqJson);
		assertEquals(null, msg.code());
		assertEquals("Emotion Poll (Type A)", msg.body(0).data("qtitle"));
		
		ArrayList<?> ques = (ArrayList<?>) msg.body(0).data("questions");
		assertEquals(3, ques.size());

		// [[question, Test 1], [answers, ], [qtype, ], [answer, null], [quizId, null], [qorder, 0], [shortDesc, Test 1], [hints, null], [extra, null]]
		Object q0 = ques.get(0);
		assertEquals("question", ((String[][]) q0)[0][0].toString());
		assertEquals("Test 1", ((String[][]) q0)[0][1].toString());

		// [[question, Test 1], [answers, ], [qtype, ], [answer, null], [quizId, null], [qorder, 0], [shortDesc, Test 1], [hints, null], [extra, null]]
		Object q1 = ques.get(1);
		assertEquals("question", ((String[][]) q1)[0][0].toString());
		assertEquals("学习压力", ((String[][]) q1)[0][1].toString());
	}

	static <U extends AnsonResp> AnsonMsg<U> ok(Port p, U body) {
		AnsonMsg<U> msg = new AnsonMsg<U>(p, MsgCode.ok);
		msg.body(body);
		return msg;
	}
}
