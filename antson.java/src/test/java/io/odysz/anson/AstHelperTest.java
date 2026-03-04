package io.odysz.anson;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import io.odysz.semantier.PeerSettings;
import io.odysz.semantier.AstHelper;

class AstHelperTest {

	@Test
	void test() {
		PeerSettings config = new PeerSettings(); 
		config.ansonMsg = io.odysz.semantic.jprotocol.test.T_AnsonMsg.class.getName();
		config.requestMsgs = new String[] {
				io.odysz.anson.jprotocol.T_EchoReq.class.getName()};
		
		HashMap<String, String> expects = new HashMap<String, String>() {
//			{put("io.odysz.anson.jprotocol.T_AnsonBody",
//				 "{\"type\": \"io.odysz.anson.AnsonAst\", " + 
//					"\"base\": \"io.odysz.semantic.jprotocol.test\"" +
//					"\"a\": \"String\"" +
//					"\"uri\": \"String\"" +
//					"\"parent\": \"@enclosing\"}");};
	
			{put("io.odysz.anson.jprotocol.T_EchoReq",
				 "{\"type\": \"io.odysz.anson.AnsonAst\", " + 
				  "\"msg\": \"io.odysz.semantic.jprotocol.test.T_AnsonMsg\", " +
				  "\"body\": \"io.odysz.anson.jprotocol.T_EchoReq\", " +
				  "\"base\": \"io.odysz.semantic.jprotocol.test.T_AnsonBody\", " +
				  "\"a\": \"String\", " +
				  "\"uri\": \"String\", " +
				  "\"parent\": \"@enclosing\"}");}
		};
		
		for (String clzname : config.requestMsgs) {
			try {
				Class<?> cls = Class.forName(clzname);

				ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
				AstHelper.toAst(cls, bos);
				String s = bos.toString(StandardCharsets.UTF_8.name());

				assertEquals(expects.get(cls), s);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
}
