package io.odysz.anson;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;

import gen.antlr.json.JSONBaseListener;
import gen.antlr.json.JSONListener;
import gen.antlr.json.JSONParser.ObjContext;
import gen.antlr.json.JSONParser.PairContext;
import gen.antlr.json.JSONParser.Type_pairContext;
import io.odysz.common.Utils;

public class JSONAnsonListener extends JSONBaseListener implements JSONListener {
	/**Current parsing object */
	Anson anson;
	HashMap<String, Field> fmap;

	private ArrayList<Object> pairStack; 
	
	public JSONAnsonListener() {
		super();
		pairStack = new ArrayList<Object>();
	}

//	@Override
//	public void enterObj(ObjContext ctx) {
//		ParseTree f = ctx.getChild(0);
//		f.getText();
//	}

	@Override
	public void exitObj(ObjContext ctx) {
	}

	@Override
	public void enterType_pair(Type_pairContext ctx) {
		// Utils.logi("Type: %s", ctx.getChild(2).getText());
		String className = ctx.getChild(2).getText();
		
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> ctor = clazz.getConstructor(String.class);
			anson = (Anson) ctor.newInstance(new Object[0]);

			Field flist[] = this.getClass().getDeclaredFields();
			fmap = new HashMap<String, Field>(flist.length);
			for (Field f : flist)
				fmap.put(f.getName(), f);
		} catch (ReflectiveOperationException | SecurityException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void enterPair(PairContext ctx) {
		super.enterPair(ctx);
	}

	public void exitPair(PairContext ctx) {
		super.exitPair(ctx);
		Utils.logi("Property-name: %s", ctx.getChild(0).getText());
		Utils.logi("Property-value: %s", ctx.getChild(2).getText());
		String fn = ctx.getChild(0).getText();

		try {
			Field f = fmap.get(fn);
			Class<?> ft = f.getType();
			if (ft == String.class) {
				String vStr = ctx.getChild(2).getText();
				f.set(anson, vStr);
			}
			else if (ft.isPrimitive()) {
				// construct primitive value
			}
			else if (ft.isAssignableFrom(AbstractCollection.class)){

			}
			else if (ft.isAssignableFrom(Object.class)){
				
			}
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}


}
