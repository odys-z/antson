package io.odysz.antson;


import java.util.Arrays;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gen.antlr.json.JSONLexer;
import gen.antlr.json.JSONParser;

@SuppressWarnings("deprecation")
class AnparserTest {
	@BeforeAll
	static void init() {
		Utils.printCaller(false);
	}

	@Test
	void test() {
		
		String json = "{\"a\":\"1\", \"b\":[{\"a\":\"x\"}]}";
		ANTLRInputStream inputStream = new ANTLRInputStream(json);
        JSONLexer lex = new JSONLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lex);
        JSONParser anparser = new JSONParser(commonTokenStream);

        // Anparser classListener = new Anparser();
		Anlistner lsner = new Anlistner();
        anparser.json().enterRule(lsner);
        Object obj = lsner.getValue();
        
        Utils.logi(json);
        Utils.logi(obj.toString());
        
        Utils.logi(((Ason)obj).json());
	}
	
	@Test
	void tryJson() {
		Ason1 e1 = new Ason1();
		e1.a = "e1-prop";
		e1.b = new Ason[] { };
		Ason1 e2 = new Ason1();
		e2.a = "e2-prop";

		Ason[] a = new Ason[] { e1, e2 };
		
		String s = Arrays.stream((Ason[])a).map(e -> e.json()).collect(Collectors.joining(";\n"));
		Utils.logi(s);
	}

	class Ason1 extends Ason {
		String a;
		Ason[] b;
	}
}
