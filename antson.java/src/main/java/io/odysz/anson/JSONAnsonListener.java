package io.odysz.anson;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import gen.antlr.json.JSONBaseListener;
import gen.antlr.json.JSONListener;
import gen.antlr.json.JSONParser.ArrayContext;
import gen.antlr.json.JSONParser.EnvelopeContext;
import gen.antlr.json.JSONParser.ObjContext;
import gen.antlr.json.JSONParser.PairContext;
import gen.antlr.json.JSONParser.Type_pairContext;
import io.odysz.anson.x.AnsonException;
import io.odysz.common.LangExt;
import io.odysz.common.Utils;

public class JSONAnsonListener extends JSONBaseListener implements JSONListener {
	/**Merge clazz's fields up to the Anson ancestor.
	 * @param clazz
	 * @param fmap
	 * @return
	 */
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

	protected List<?> parsingArr;
	protected Class<?> parsingArrElemCls; 
	protected AbstractCollection<?> collection;

	/**Parsing objects stack<br>
	 * Element: [0]: field-map, [1]: enclosing {@link Anson} object<br>
	 * Top = Current parsingVal object.<br>
	 * Currently all object must be an Anson object. */
	private ArrayList<Object[]> stack;

	protected Object parsedVal; 
	protected String parsingProp;

	/**Envelope Type Name */
 	protected String envetype; 
	
	@Override
	public void exitObj(ObjContext ctx) {
		// All object must an Anson object
		// enclosing = elems.remove(elems.size() - 1);
//		try {
//			Anson parsingVal = enclosing;
//			Field f = fmap.get(parsingProp);
//			f.set(enclosing, parsingVal);
			Object[] top = pop();
			parsedVal = top[1];
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		}
	}
	
	@Override
	public void enterObj(ObjContext ctx) {
		try {
			@SuppressWarnings("unchecked")
			HashMap<String, Field> fmap = stack.size() > 0 ?
					(HashMap<String, Field>)stack.get(0)[0] : null;
			if (fmap == null || !fmap.containsKey(parsingProp))
				throw new AnsonException("internal", "Obj type not found. property: %s", parsingProp);
			push(fmap.get(parsingProp).getType());
		} catch (SecurityException | ReflectiveOperationException | AnsonException e) {
			e.printStackTrace();
		}
	}

	public Anson parsed() {
		return (Anson) stack.get(0)[1];
	}

	@Override
	public void enterEnvelope(EnvelopeContext ctx) {
		stack = new ArrayList<Object[]>();
		envetype = null;
		super.enterEnvelope(ctx);
	}
	
	/**Semantics of entering a type pair is found and parsingVal an Anson object.<br>
	 * This is always happening on entering an object.
	 * The logic opposite is exit object.
	 * @see gen.antlr.json.JSONBaseListener#enterType_pair(gen.antlr.json.JSONParser.Type_pairContext)
	 */
	@Override
	public void enterType_pair(Type_pairContext ctx) {
		if (envetype != null)
			// ignore this type specification, keep consist with java type
			return;

		envetype = ctx.getChild(2).getText();
		
		try {
			Class<?> clazz = Class.forName(envetype);
//			Constructor<?> ctor = clazz.getConstructor(new Class[0]);
//
//			if (fmap == null) {
//				fmap = new HashMap<String, Field>();
//				fmap = mergeFields(clazz, fmap);
//			}
//			parsingVal =  ctor.newInstance(new Object[0]);
			push(clazz);
		} catch (ReflectiveOperationException | SecurityException e) {
			e.printStackTrace();
		}
	}

	/**Push parsingVal anson
	 * @param clazz 
	 * @param fmap
	 * @param parsingVal
	 * @throws SecurityException 
	 * @throws ReflectionOperationException 
	 */
	private void push(Class<?> clazz) throws ReflectiveOperationException, SecurityException {
		HashMap<String, Field> fmap = new HashMap<String, Field>();
		fmap = mergeFields(clazz, fmap);
		Constructor<?> ctor = clazz.getConstructor(new Class[0]);
		Anson enclosing =  (Anson) ctor.newInstance(new Object[0]);
		stack.add(0, new Object[] {fmap, enclosing});
	}

	private Object[] pop() {
		@SuppressWarnings("unused")
		Object[] top = stack.remove(0);
		// fmap = (HashMap<String, Field>) top[0];
		// enclosing = (Anson) top[1];
		return top;
	}

	@Override
	public void enterPair(PairContext ctx) {
		super.enterPair(ctx);
		// create a container for Collection
		try {
			parsingProp = getProp(ctx); //.getChild(0).getText();
			Class<?> ft = getType(parsingProp);
			if (ft.isAssignableFrom(AbstractCollection.class)){
				Constructor<?> ctor = ft.getConstructor(String.class);
				collection = (AbstractCollection<?>) ctor.newInstance(new Object[0]);
			}
		} catch (ReflectiveOperationException | AnsonException e) {
			e.printStackTrace();
		}
	}

	private static String getProp(PairContext ctx) {
		TerminalNode p = ctx.propname().IDENTIFIER();
		return p == null ?
				ctx.propname().STRING().getText().replaceAll("(^\\s*\"\\s*)|(\\s*\"\\s*$)", "")
				: p.getText();
	}

	/**Convert json value : STRING | NUMBER | 'true' | 'false' | 'null' to java.lang.String.<br>
	 * Can't handle NUMBER | obj | array.
	 * @param ctx
	 * @return
	 */
	private static String getStringVal(PairContext ctx) {
		TerminalNode str = ctx.value().STRING();
		if (str == null) {
				String s = ctx.value().getText();
				try { 
					if (LangExt.isblank(s))
						return null;
					else if ("null".equals(s))
						return null;
				} catch (Exception e) { }
				return s;
		}
		else return str.getText().replaceAll("(^\\s*\")|(\"\\s*$)", "");
	}

	/**Get prop's type from stack's top element.
	 * @param prop
	 * @return type
	 * @throws AnsonException
	 */
	private Class<?> getType(String prop) throws AnsonException {
		Object[] top = stack.get(0);
		@SuppressWarnings("unchecked")
		Field f = ((HashMap<String,Field>) top[0]).get(prop);
		if (f == null)
			throw new AnsonException("internal", "Field not found: %s", prop);
		Class<?> ft = f.getType();
		return ft;
	}

	@Override
	public void enterArray(ArrayContext ctx) {
		try {
			parsingArr = (ArrayList<?>)Class.forName("java.util.ArrayList").newInstance();
			@SuppressWarnings("unchecked")
			Field f = ((HashMap<String,Field>) stack.get(0)[0]).get(parsingProp);
			parsingArrElemCls = f.getType().getComponentType();
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void exitArray(ArrayContext ctx) {
		// list2Array(parsingArrElemCls, parsingArr);
	}

	public static <C, T extends C> C[] toArray(Class<C> componentType, List<T> list) {
	    @SuppressWarnings("unchecked")
	    C[] array = (C[])Array.newInstance(componentType, list.size());
	    return list.toArray(array);
	}	

	public static <T> T[] list2Array(Class<T[]> clazz, List<T> elements) {
	    T[] array = clazz.cast(Array.newInstance(clazz.getComponentType(), elements.size()));
	    return elements.toArray(array);
	}
//	@Override
//	public void exitValue(ValueContext ctx) {
//		if (parsingArr != null) {
//		}
//	}

	@Override
	public void exitPair(PairContext ctx) {
		super.exitPair(ctx);
		Utils.logi("Property-name: %s", ctx.getChild(0).getText());
		Utils.logi("Property-value: %s", ctx.getChild(2).getText());

		try {
			String fn = getProp(ctx);
			@SuppressWarnings("unchecked")
			Field f = ((HashMap<String,Field>) stack.get(0)[0]).get(fn);
			Anson enclosing = (Anson) stack.get(0)[1];
			if (f == null)
				throw new AnsonException("internal", "Field not found: %s", fn);
			Class<?> ft = f.getType();
			if (ft == String.class) {
				// String v = ctx.getChild(2).getText();
				String v = getStringVal(ctx);
				f.set(enclosing, v);
			}
			else if (ft.isPrimitive()) {
				// construct primitive value
				String v = ctx.getChild(2).getText();
				setPrimitive(enclosing, f, v);
			}
			else if (ft.isArray()) {
				// Class<?> cls = ft.getComponentType();
				if (parsingArr != null) {
					Object[] arr = parsingArr.toArray();
					f.set(enclosing, arr);
					parsingArr = null;
				}
			}
			else if (AbstractCollection.class.isAssignableFrom(ft)){
				f.set(enclosing, collection);
			}
			else if (Anson.class.isAssignableFrom(ft)) {
				f.set(enclosing, parsedVal);
			}
			else if (Object.class.isAssignableFrom(ft)){
				Utils.warn("Unsupported type's value of %s deserialized as Java.Lang.String", fn);
				String v = ctx.getChild(2).getText();
				f.set(enclosing, v);
			}
			else throw new AnsonException("internal", "sholdn't happen");
		} catch (ReflectiveOperationException | RuntimeException | AnsonException e) {
			e.printStackTrace();
		}
	}

	private void setPrimitive(Anson obj, Field f, String v)
			throws RuntimeException, ReflectiveOperationException, AnsonException {
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
			throw new AnsonException("internal", "Unsupported field type: %s (field %s)",
					f.getType().getName(), f.getName());
	}
}
