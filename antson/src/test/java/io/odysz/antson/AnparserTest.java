package io.odysz.antson;


import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import gen.antlr.json.JSONLexer;
import gen.antlr.json.JSONParser;

@SuppressWarnings("deprecation")
class AnparserTest {

	@Test
	void test() {
		
		String json = "{\"a\":\"1\"}";
		ANTLRInputStream inputStream = new ANTLRInputStream(json);
        JSONLexer lex = new JSONLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lex);
        JSONParser anparser = new JSONParser(commonTokenStream);

        // Anparser classListener = new Anparser();
		Anlistner lsner = new Anlistner();
        anparser.json().enterRule(lsner);
        Object obj = lsner.getValue();
        
        Utils.logi(obj.toString());
	}

}
