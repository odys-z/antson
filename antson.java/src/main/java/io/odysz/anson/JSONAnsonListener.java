package io.odysz.anson;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
	ArrayList<Anson> elems;

	HashMap<String, Field> fmap;

	private Anson vOnExit;

	private AbstractCollection collection; 
	
//	public JSONAnsonListener() {
//		super();
//		elems = new ArrayList<Anson>();
//	}

	@Override
	public void exitObj(ObjContext ctx) {
		// ParseTree f = ctx.getChild(0);
		// f.getText();
		
		
		// All object must an Anson object
		 vOnExit = elems.remove(elems.size() - 1);
	}

	public Anson parsed() {
		return vOnExit;
	}


	/**Semantics of entering a type pair is found and parsing an Anson object.<br>
	 * This is always happening on entering an object.
	 * The logic opposite is exit object.
	 * @see gen.antlr.json.JSONBaseListener#enterType_pair(gen.antlr.json.JSONParser.Type_pairContext)
	 */
	@Override
	public void enterType_pair(Type_pairContext ctx) {
		// Utils.logi("Type: %s", ctx.getChild(2).getText());
		String className = ctx.getChild(2).getText();
		
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> ctor = clazz.getConstructor(new Class[0]);
			if (elems == null)
				elems = new ArrayList<Anson>();
			elems.add((Anson) ctor.newInstance(new Object[0]));

			if (fmap == null) {
				fmap = new HashMap<String, Field>();
				fmap = mergeFields(clazz, fmap);
//				for (Field f : flist)
//					fmap.put(f.getName(), f);
			}
		} catch (ReflectiveOperationException | SecurityException e) {
			e.printStackTrace();
		}
	}

	private static HashMap<String, Field> mergeFields(Class<?> clazz, HashMap<String, Field> fmap) {
		Field flist[] = clazz.getDeclaredFields();
		for (Field f : flist) {
			int mod = f.getModifiers();
			if (Modifier.isPrivate(mod) || Modifier.isAbstract(mod) || Modifier.isFinal(mod) || Modifier.isStatic(mod))
				continue;
			// Overriden
			if (fmap.containsKey(f.getName()))
				continue;
			fmap.put(f.getName(), f);
		}

		Class<?> pclz = clazz.getSuperclass();
		if (Anson.class.isAssignableFrom(pclz))
			fmap = mergeFields(pclz, fmap);
		return fmap;
	}

	@Override
	public void enterPair(PairContext ctx) {
		super.enterPair(ctx);

		// create a container for Collection
		try {
			String fn = ctx.getChild(0).getText();
			Field f = fmap.get(fn);
			if (f == null)
				throw new AnsonException("internal", "Field not found: %s", fn);
			Class<?> ft = f.getType();
			if (ft.isAssignableFrom(AbstractCollection.class)){
				Constructor<?> ctor = ft.getConstructor(String.class);
				collection = (AbstractCollection<?>) ctor.newInstance(new Object[0]);
			}
		} catch (ReflectiveOperationException | AnsonException e) {
			e.printStackTrace();
		}
	}

	public void exitPair(PairContext ctx) {
		super.exitPair(ctx);
		Utils.logi("Property-name: %s", ctx.getChild(0).getText());
		Utils.logi("Property-value: %s", ctx.getChild(2).getText());

		try {
			Anson enclosing = elems.get(elems.size() - 1);
			String fn = ctx.getChild(0).getText();
			Field f = fmap.get(fn);
			if (f == null) throw new AnsonException("internal", "Field not found: %s", fn);
			Class<?> ft = f.getType();
			if (ft == String.class) {
				String v = ctx.getChild(2).getText();
				f.set(enclosing, v);
			}
			else if (ft.isPrimitive()) {
				// construct primitive value
				String v = ctx.getChild(2).getText();
				setPrimitive(enclosing, f, v);
			}
			else if (ft.isAssignableFrom(AbstractCollection.class)){
				f.set(enclosing, collection);
			}
			else if (ft.isAssignableFrom(Anson.class)){
				String json = ctx.getChild(2).getText();
				Anson v = Anson.fromJson(json);
				f.set(enclosing, v);
			}
			else if (ft.isAssignableFrom(Object.class)){
				Utils.warn("Unsupported type's value of %s deserialized as Java.Lang.String", fn);
				String v = ctx.getChild(2).getText();
				f.set(enclosing, v);
			}
		} catch (ReflectiveOperationException | RuntimeException | AnsonException e) {
			e.printStackTrace();
		}
	}

	private void setPrimitive(Anson obj, Field f, String v) throws RuntimeException, ReflectiveOperationException, AnsonException {
		if (f.getType() == int.class || f.getType() == Integer.class)
			f.set(obj, Integer.valueOf(v));
		else if (f.getType() == float.class || f.getType() == Float.class)
			f.set(obj, Float.valueOf(v));
		else if (f.getType() == double.class || f.getType() == Double.class)
			f.set(obj, Double.valueOf(v));
		else if (f.getType() == long.class || f.getType() == Long.class)
			f.set(obj, Long.valueOf(v));
		else if (f.getType() == short.class || f.getType() == Short.class)
			f.set(obj, Short.valueOf(v));
		else if (f.getType() == byte.class || f.getType() == Byte.class)
			f.set(obj, Byte.valueOf(v));
		else
			// what's else?
			throw new AnsonException("internal", "Unsupported field type: %s (field %s)", f.getType().getName(), f.getName());
	}
}
