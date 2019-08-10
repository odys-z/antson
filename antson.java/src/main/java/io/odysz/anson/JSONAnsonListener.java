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
import io.odysz.anson.x.AnsonException;
import io.odysz.common.Utils;

public class JSONAnsonListener extends JSONBaseListener implements JSONListener {
	/**Parsing objects stack<br>
	 * Top = Current parsing object.<br>
	 * Currently all object must be an Anson object. */
	ArrayList<Anson> parsed;

	HashMap<String, Field> fmap;

	private Object vOnExit;

	private AbstractCollection collection; 
	
	public JSONAnsonListener() {
		super();
		parsed = new ArrayList<Anson>();
	}

	@Override
	public void exitObj(ObjContext ctx) {
		// ParseTree f = ctx.getChild(0);
		// f.getText();
		
		
		// All object must an Anson object
		vOnExit = parsed.remove(parsed.size() - 1);
	}

	@Override
	public void enterType_pair(Type_pairContext ctx) {
		// Utils.logi("Type: %s", ctx.getChild(2).getText());
		String className = ctx.getChild(2).getText();
		
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> ctor = clazz.getConstructor(String.class);
			parsed.add((Anson) ctor.newInstance(new Object[0]));

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

		// create a container for Collection
		try {
			String fn = ctx.getChild(0).getText();
			Field f = fmap.get(fn);
			Class<?> ft = f.getType();
			if (ft.isAssignableFrom(AbstractCollection.class)){
				Constructor<?> ctor = ft.getConstructor(String.class);
				collection = (AbstractCollection<?>) ctor.newInstance(new Object[0]);
			}
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	public void exitPair(PairContext ctx) {
		super.exitPair(ctx);
		Utils.logi("Property-name: %s", ctx.getChild(0).getText());
		Utils.logi("Property-value: %s", ctx.getChild(2).getText());

		try {
			Anson enclosing = parsed.get(parsed.size() - 1);
			String fn = ctx.getChild(0).getText();
			Field f = fmap.get(fn);
			Class<?> ft = f.getType();
			if (ft == String.class) {
				String v = ctx.getChild(2).getText();
				f.set(enclosing, v);
			}
			else if (ft.isPrimitive()) {
				// construct primitive value
				String v = ctx.getChild(2).getText();
				set(enclosing, f, v);
			}
			else if (ft.isAssignableFrom(AbstractCollection.class)){
				f.set(enclosing, collection);
				TO BE CONTINUED
			}
			else if (ft.isAssignableFrom(Anson.class)){
				
			}
			else if (ft.isAssignableFrom(Object.class)){
				Utils.warn("Unsupported type's value of %s deserialized as Java.Lang.String", fn);
				String v = ctx.getChild(2).getText();
				f.set(enclosing, v);
			}
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	private void set(Anson obj, Field f, String v) {
		Integer i;
	}

}
