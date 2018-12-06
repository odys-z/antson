package io.odysz.antson;

import java.util.List;

import gen.antlr.json.JSONBaseListener;
import gen.antlr.json.JSONParser.ArrayContext;
import gen.antlr.json.JSONParser.JsonContext;
import gen.antlr.json.JSONParser.ObjContext;
import gen.antlr.json.JSONParser.PairContext;
import gen.antlr.json.JSONParser.ValueContext;
import io.odysz.antson.Anlistner.Axby;

/**Antlr4 Listener based json string parser<pre>
json : value ;

obj  : '{' pair (',' pair)* '}'
     | '{' '}' ;

pair : STRING ':' value ;

array: '[' value (',' value)* ']'
     | '[' ']' ;

value: STRING | NUMBER | obj | array | 'true' | 'false' | 'null' ;</pre>

 * @author ody
 *
 */
public class Anlistner extends JSONBaseListener {

	public class Axby {
		String a;
		String b;

		@Override
		public String toString() {
			return String.format("Axby: a = %s, b = %s", a, b);
		}
	}

	@Override
	public void enterJson(JsonContext ctx) {
//		super.enterJson(ctx);
		ValueContext vc = ctx.value();
		vc.enterRule(this);
	}

	private Axby javaObj;
	
	@Override
	public void enterValue(ValueContext ctx) {
//		super.enterValue(ctx);
//		int i = ctx.getChildCount();
		ObjContext ox = ctx.obj();
		ox.enterRule(this);
	}

	@Override
	public void enterObj(ObjContext ctx) {
//		super.enterObj(ctx);
		List<PairContext> ps = ctx.pair();
		System.out.println("create javaObj");
		javaObj = new Axby();
		for (PairContext p : ps) {
			String a = p.STRING().getText();
			String v = p.value().getText();
			javaObj.a = v;
		}
	}

	@Override
	public void exitArray(ArrayContext ctx) {
//		super.exitArray(ctx); 
		ctx.enterRule(this);
	}

	public Object getObj() {
		return javaObj;
	}

}
