package io.odysz.antson;

import java.util.ArrayList;

import org.antlr.v4.runtime.tree.TerminalNode;

import gen.antlr.json.JSONBaseListener;
import gen.antlr.json.JSONParser.ArrayContext;
import gen.antlr.json.JSONParser.JsonContext;
import gen.antlr.json.JSONParser.ObjContext;
import gen.antlr.json.JSONParser.PairContext;
import gen.antlr.json.JSONParser.ValueContext;

/**Antlr4 Listener based json string parser.<br>
 * Handler json, value, STRING, NUMBER, 'ture', 'false', null, everyting exception obj and array.<pre>
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

	class ObjListener extends JSONBaseListener {
		private Axby javaObj;
		
		ObjListener () {
			javaObj = new Axby();
		}

		@Override
		public void enterPair(PairContext ctx) {
			// a = "a" | "b"
			String a = ctx.STRING().getText();
			
			Anlistner vl = new Anlistner();
			ctx.value().enterRule(vl);
			// String v = ctx.value().getText();
			Object v = vl.getValue();

			if ("a".equals(a))
				javaObj.a = v.toString();
			else if ("b".equals(a))
				javaObj.b = v.toString();
			else Utils.warn("no property, ignored: %s - %s", a, v);
		}
		
		public Object getObj() {return javaObj;}
	}
	
	class ArrListener extends JSONBaseListener {
		private ArrayList<Object> javaArr;
		
		ArrListener (int mx) {
			javaArr = new ArrayList<Object>(mx);
		}
		
		@Override
		public void enterArray(ArrayContext ctx) {
			ctx.value().forEach(v -> v.enterRule(this));
		}
		
		@Override
		public void enterValue(ValueContext ctx) {
			ObjContext ox = ctx.obj();
			if (ox != null) {
				ObjListener ol = new ObjListener();
				ox.enterRule(ol);
				javaArr.add(ol.getObj());
			}
			
			ArrayContext ax = ctx.array();
			if (ax != null) {
				ArrListener al = new ArrListener(ctx.getChildCount());
				ax.enterRule(al);
				javaArr.add(al.getArr());
			}
			
			TerminalNode tx = ctx.STRING();
			if (tx != null) {
				// TODO boolean
				try { javaVal = DateFormat.parse(tx.getText()); }
				catch (Exception e) { javaVal = tx.getText(); }
				javaArr.add(tx.getText());
			}
			
			tx = ctx.NUMBER();
			if (tx != null) {
				try { javaVal = Integer.parseInt(tx.getText()); }
				catch (Exception e) { javaVal = Double.parseDouble(tx.getText()); }
				javaArr.add(tx.getText());
			}
		}

		public Object[] getArr() {return javaArr.toArray();}
	}

	public class Axby {
		String a;
		String b;

		@Override
		public String toString() {
			return String.format("Axby: a = %s, b = %s", a, b);
		}
	}

	private Object javaVal;

	@Override
	public void enterJson(JsonContext ctx) {
//		super.enterJson(ctx);
		ValueContext vc = ctx.value();
		vc.enterRule(this);
	}

	
	@Override
	public void enterValue(ValueContext ctx) {
		ObjContext ox = ctx.obj();
		if (ox != null) {
			ObjListener ol = new ObjListener();
			ox.enterRule(ol);
			javaVal = ol.getObj();
		}
		
		ArrayContext ax = ctx.array();
		if (ax != null) {
			ArrListener al = new ArrListener(ax.getChildCount());
			ax.enterRule(al);
			javaVal = al.getArr();
		}
		
		TerminalNode tx = ctx.STRING();
		if (tx != null) {
			// TODO boolean
			try { javaVal = DateFormat.parse(tx.getText()); }
			catch (Exception e) { javaVal = tx.getText(); }
			javaVal = tx.getText();
		}
		
		tx = ctx.NUMBER();
		if (tx != null) {
			try { javaVal = Integer.parseInt(tx.getText()); }
			catch (Exception e) { javaVal = Double.parseDouble(tx.getText()); }
		}
	}

	@Override
	public void enterArray(ArrayContext ctx) {
		ArrListener arrListener = new ArrListener(ctx.getChildCount());
		ctx.enterRule(arrListener);
	}

//	@Override
//	public void exitArray(ArrayContext ctx) {
//		// ctx.enterRule(this);
//	}

	public Object getValue() {
		return javaVal;
	}

}
