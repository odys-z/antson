package io.odysz.anson;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.tree.TerminalNode;

import gen.antlr.json.JSONBaseListener;
import gen.antlr.json.JSONListener;
import gen.antlr.json.JSONParser.ArrayContext;
import gen.antlr.json.JSONParser.EnvelopeContext;
import gen.antlr.json.JSONParser.ObjContext;
import gen.antlr.json.JSONParser.PairContext;
import gen.antlr.json.JSONParser.Type_pairContext;
import gen.antlr.json.JSONParser.ValueContext;
import io.odysz.anson.JSONAnsonListener.ParsingCtx;
import io.odysz.anson.x.AnsonException;
import io.odysz.common.LangExt;
import io.odysz.common.Utils;

public class JSONAnsonListener extends JSONBaseListener implements JSONListener {
	public static boolean verbose = true;

	/**Parsing AST node's context, for handling the node's value,
	 * the element class of parsing stack.
	 * @author odys-z@github.com
	 * @param <T>
	 */
	public class ParsingCtx {
		// primitive, list and string structure helpers
		/** Buffer for multi dimensional array. */
//		protected List<?> parsingArrs;
		protected Class<?> parsingArrElemCls; 
		
		/**The currently parsing map.
		 * <p>1. A map field is constructed when enter an object, with type of Map;<br>
		 * 2. element values are been put when exiting pair if parsingMap is not null;<br>
		 * 3. let parsedVal = parsingMap, parsingMap = null when exit object if parsingMap is not null;<br>
		 * 4. parsedVal (map value) is been set to field when exit pair if parsingMap is null</p> 
		 * Map's key is always type of string because any json object's property key must be a string.
		 * 
		 * public Map<String, Object> parsingMap;
		 * protected List<?> parsingArrs;
		 *
		 * The hard lesson learned from this is if you want parse a grammar,
		 * you better follow the grammar structure.
		 * */

		/**The json prop (object key) */
		protected String parsingProp;
		/**The parsed native value */
		protected Object parsedVal;

		private Object enclosing;
		private HashMap<String, Field> fmap;

		public ParsingCtx(HashMap<String, Field> fmap, IJsonable enclosing) {
			this.fmap = fmap;
			this.enclosing = enclosing;
		}

		public ParsingCtx(HashMap<String, Field> fmap, HashMap<String, ?> enclosing) {
			this.fmap = fmap;
			this.enclosing = enclosing;
		}

		public ParsingCtx(HashMap<String, Field> fmap, List<?> enclosing) {
			this.fmap = fmap;
			this.enclosing = enclosing;
		}

		public boolean isInList() {
			return enclosing instanceof List || enclosing.getClass().isArray();
		}

		public boolean isInMap() {
			return enclosing instanceof HashMap;
		}
		
		/*
		@SuppressWarnings("unchecked")
		public ParsingCtx pushArr(ArrayList<?> arrayList) {
			if (parsingArrs == null)
				parsingArrs = arrayList;
			else ((List<Object>)parsingArrs).add((Object)arrayList);
			
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public List<?> popArr() {
			// must not null when poping
			List<?> arr = parsingArrs;
			while (arr.size() > 0 && List.class.isAssignableFrom(arr.get(arr.size() - 1).getClass()))
				arr = (List<Object>) arr.get(arr.size() - 1);
			
			arr
			return arr;
		}

		public List<?> parsingArr() {
			return null;
		}
		*/
	}

	/**Merge clazz's field meta up to the IJsonable ancestor.
	 * @param clazz
	 * @param fmap
	 * @return
	 */
	static HashMap<String, Field> mergeFields(Class<?> clazz, HashMap<String, Field> fmap) {
		Field flist[] = clazz.getDeclaredFields();
		for (Field f : flist) {
			int mod = f.getModifiers();
			if (// Modifier.isPrivate(mod) ||
				Modifier.isAbstract(mod) || Modifier.isFinal(mod) || Modifier.isStatic(mod))
				continue;
			// Overriden
			if (fmap.containsKey(f.getName()))
				continue;
			fmap.put(f.getName(), f);
		}

		Class<?> pclz = clazz.getSuperclass();
		if (pclz != null && IJsonable.class.isAssignableFrom(pclz))
			fmap = mergeFields(pclz, fmap);
		return fmap;
	}

	/**Parsing objects stack<br>
	 * Element: [0]: field-map, [1]: enclosing {@link IJsonable} object<br>
	 * Top = Current parsingVal object.<br>
	 * Currently all object must be an IJsonable object. */
	private ArrayList<ParsingCtx> stack;

	private ParsingCtx top() { return stack.get(0); }

	/**Push parsing node (a envelope, map).
	 * @param enclosingClazz new parsing IJsonable object's class
	 * @throws ReflectiveOperationException
	 * @throws SecurityException
	 * @throws AnsonException 
	 */
	private void push(Class<?> enclosingClazz) throws ReflectiveOperationException, SecurityException, AnsonException {
		if (enclosingClazz.isArray()) {
			ParsingCtx top = new ParsingCtx(new HashMap<String, Field>(), new ArrayList<Object>());
			stack.add(0, top);
		}
		else {
			HashMap<String, Field> fmap = new HashMap<String, Field>();
			if (List.class.isAssignableFrom(enclosingClazz)) {
				List<?> enclosing = new ArrayList<Object>();
				stack.add(0, new ParsingCtx(fmap, enclosing));
			}
			else {
				Constructor<?> ctor = null;
				try { ctor = enclosingClazz.getConstructor();
				} catch (NoSuchMethodException e) {
					throw new AnsonException(0, "To make json can be parsed to %s, the class must has a default constructor(0 parameter)", enclosingClazz.getName());
				}
				if (IJsonable.class.isAssignableFrom(enclosingClazz)) {
					fmap = mergeFields(enclosingClazz, fmap); // map merging is only needed by typed object
					IJsonable enclosing = (IJsonable) ctor.newInstance(new Object[0]);
					stack.add(0, new ParsingCtx(fmap, enclosing));
				}
				else {
					HashMap<String, Object> enclosing = new HashMap<String, Object>();
					ParsingCtx top = new ParsingCtx(fmap, enclosing);
					stack.add(0, top);
				}
			}
		}
	}

	private ParsingCtx pop() {
		ParsingCtx top = stack.remove(0);
		return top;
	}

	/**Envelope Type Name */
 	protected String envetype;

	@Override
	public void exitObj(ObjContext ctx) {
		ParsingCtx top = pop();
		top().parsedVal = top.enclosing;
		top.enclosing = null;
	}

	@Override
	public void enterObj(ObjContext ctx) {
		ParsingCtx top = top();
//		if (top.parsingArr() != null) {
//			// An IJsonable Array occurred here, the element should only be an envelope.
//			// Needing type to construct object, otherwise elements can't been parsed.
//			if (Map.class.isAssignableFrom(top.parsingArrElemCls)
//				|| Set.class.isAssignableFrom(top.parsingArrElemCls)) {
//				throw new NullPointerException("TODO handling map or set: " + ctx.getText());
//			}
//			else
//				throw new NullPointerException("An IJsonable Array occurred here, the element should only be an envelope (has type-pair): "
//					+ ctx.getText());
//		}
		try {
			HashMap<String, Field> fmap = stack.size() > 0 ?
					top.fmap : null;
			if (fmap == null || !fmap.containsKey(top.parsingProp))
				throw new AnsonException(0, "Obj type not found. property: %s", top.parsingProp);
			

			Class<?> ft = fmap.get(top.parsingProp).getType();
			if (Map.class.isAssignableFrom(ft)) {
				// entering a map
//				Constructor<?> ctor = ft.getConstructor();
//				top.parsingMap = (Map<String, Object>) ctor.newInstance(new Object[0]);
				push(ft);
			}
			else
				// entering an envelope
				// push(fmap.get(top.parsingProp).getType());
				push(ft);
		} catch (SecurityException | ReflectiveOperationException | AnsonException e) {
			e.printStackTrace();
		}
	}
	
	public IJsonable parsedEnvelope() {
		return (IJsonable) stack.get(0).enclosing;
	}

	@Override
	public void enterEnvelope(EnvelopeContext ctx) {
		if (stack == null) {
			stack = new ArrayList<ParsingCtx>();
		}
		else {
			// push and parse sub envelope
			// handled in enterType_pair
		}
		envetype = null;
	}
	
	@Override
	public void exitEnvelope(EnvelopeContext ctx) {
		super.exitEnvelope(ctx);
		if (stack.size() > 1) {
			ParsingCtx top = pop();
			top().parsedVal = top.enclosing;
		}
		// else keep last one (root) as return value
	}
	
	/**Semantics of entering a type pair is found and parsingVal an IJsonable object.<br>
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
			push(clazz);
		} catch (ReflectiveOperationException | SecurityException | AnsonException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void enterPair(PairContext ctx) {
		super.enterPair(ctx);
		ParsingCtx top = top();
//		if (top.isInMap()) {
//		//	 we are parsing map, prop can't be a field name
			top.parsingProp = getProp(ctx);
			top.parsedVal = null;
//		}
		// handled when entering array
//		else {
//			// create a container for Collection
//			try {
//				top.parsingProp = getProp(ctx);
//				Class<?> ft = getType(top.parsingProp);
//				if (List.class.isAssignableFrom(ft)){
//					// Because of Java type erasing, list should be safely changed into ArrayList<Object>
//					top.parsingArr = new ArrayList<Object>();
//				}
//			} catch (AnsonException e) {
//				e.printStackTrace();
//			}
//		}
	}
	
	/**Parse property name, tolerate enclosing quotes presenting or not. 
	 * @param ctx
	 * @return
	 */
	private static String getProp(PairContext ctx) {
		TerminalNode p = ctx.propname().IDENTIFIER();
		return p == null
				? ctx.propname().STRING().getText().replaceAll("(^\\s*\"\\s*)|(\\s*\"\\s*$)", "")
				: p.getText();
	}
	
	/**Convert json value : STRING | NUMBER | 'true' | 'false' | 'null' to java.lang.String.<br>
	 * Can't handle NUMBER | obj | array.
	 * @param ctx
	 * @return
	 */
	private static String getStringVal(PairContext ctx) {
		TerminalNode str = ctx.value().STRING();
		String txt = ctx.value().getText();
		return getStringVal(str, txt);
	}

	private static String getStringVal(ValueContext ctx) {
		TerminalNode str = ctx.STRING();
		String txt = ctx.getText();
		return getStringVal(str, txt);
	}
	
	private static String getStringVal(TerminalNode str, String rawTxt) {
		if (str == null) {
				try { 
					if (LangExt.isblank(rawTxt))
						return null;
					else if ("null".equals(rawTxt))
						return null;
				} catch (Exception e) { }
				return rawTxt;
		}
		 else return str.getText().replaceAll("(^\\s*\")|(\"\\s*$)", "");
//		else return rawTxt == null ? null : rawTxt.trim();
	}
	
	/**Get prop's type from stack's top element.
	 * @param prop
	 * @return type
	 * @throws AnsonException
	 */
	private Class<?> getType(String prop) throws AnsonException {
		Field f = top().fmap.get(prop);
		if (f == null)
			throw new AnsonException(0, "Field not found: %s", prop);
		Class<?> ft = f.getType();
		return ft;
	}

	@Override
	public void enterArray(ArrayContext ctx) {
		try {
			ParsingCtx top = top();

//			top.parsingArr = (ArrayList<?>)Class.forName("java.util.ArrayList").newInstance();
//
//			// top.parsingProp can be a map key or an Anson field 
//			if (!(top.isInMap())) {
//				Field f = top.fmap.get(top.parsingProp);
//				top.parsingArrElemCls = f.getType().getComponentType();
//			}
//			else // it's HashMap, let the top.parsingArrElemCls unresolved
//				top.parsingArrElemCls = Object.class;
			
			if (top.isInList())
				push(ArrayList.class);
			else if (top.isInMap())
				push(HashMap.class);
			else {
				Class<?> ft = top.fmap.get(top.parsingProp).getType();
				push(ft);
			}
		} catch (ReflectiveOperationException | SecurityException | AnsonException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void exitArray(ArrayContext ctx) {
		if (!top().isInList())
			throw new NullPointerException("existing not from an eclosing list. txt:\n" + ctx.getText());

		ParsingCtx top = pop();
		List<?> arr = (List<?>) top.enclosing;

		top = top();
		top.parsedVal = arr;

//		Field f = top.fmap.get(top.parsingProp);
//		Class<?> ft = f.getType();
//		try {
//			if (ft.isArray())
//				f.set(top.enclosing, toPrimitiveArray(arr, ft));	
//			else
//				f.set(top.enclosing, arr);
//		} catch (IllegalArgumentException | IllegalAccessException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Unboxes a List in to a primitive array.
	 * reference:
	 * https://stackoverflow.com/questions/25149412/how-to-convert-listt-to-array-t-for-primitive-types-using-generic-method
	 *
	 * @param  list      the List to convert to a primitive array
	 * @param  arrayType the primitive array type to convert to
	 * @param  <P>       the primitive array type to convert to
	 * @return an array of P with the elements of the specified List
	 * @throws NullPointerException
	 *         if either of the arguments are null, or if any of the elements
	 *         of the List are null
	 * @throws IllegalArgumentException
	 *         if the specified Class does not represent an array type, if
	 *         the component type of the specified Class is not a primitive
	 *         type, or if the elements of the specified List can not be
	 *         stored in an array of type P
	 */
	private static <P> P toPrimitiveArray(List<?> list, Class<P> arrayType) {
	    if (!arrayType.isArray()) {
	        throw new IllegalArgumentException(arrayType.toString());
	    }
	    Class<?> primitiveType = arrayType.getComponentType();

	    P array = arrayType.cast(Array.newInstance(primitiveType, list.size()));

	    for (int i = 0; i < list.size(); i++) {
	        Array.set(array, i, list.get(i));
	    }

	    return array;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void exitValue(ValueContext ctx) {
		ParsingCtx top = top();
		if (top.isInList() || top.isInMap()) {
			// if in a map, parsingProp is the map key, element type can only handled with a guess
//			if (top.isInMap()) {
//				// other values are handled when exiting pair
//				// only list (array) needing a help here
//				List<Object> arr = (List<Object>) top.parsingArr;
//				if (arr != null)
//					arr.add(top.parsedVal);
//			}
//			else {
				// NOTE: ft can only work for Array, collection's ft is null
				Field f = top().fmap.get(top.parsingProp);
				Class<?> elemtype = null;

				// if in a map, parsingProp is the map key, element type can only handled with a guess
				if (!(top.isInMap())) {
					elemtype = f == null ? null	// in a list or array, f can be null
							:f.getType().getComponentType();
				}
				String txt = ctx.getText();
				List<?> arr = (List<?>) top.enclosing;
				if (top.isInList()) {
					// for List, ft is not null
					if (top.parsedVal == null) {
						// simple value like String FIXME shouldn't happen
						((List<Object>)arr).add(getStringVal(ctx.STRING(), txt));
						System.err.print("Why parsed value is null?  " + ctx.getText());
					}
					else {
						((List<Object>)arr).add(top.parsedVal);
						top.parsedVal = null;
					}

					else if (elemtype == int.class || elemtype == Integer.class)
						((List<Integer>)arr).add(Integer.valueOf(getStringVal(ctx.NUMBER(), txt)));
					else if (elemtype == float.class || elemtype == Float.class)
						((List<Float>)arr).add(Float.valueOf(getStringVal(ctx.NUMBER(), txt)));
					else if (elemtype == double.class || elemtype == Double.class)
						((List<Double>)arr).add(Double.valueOf(getStringVal(ctx.NUMBER(), txt)));
					else if (elemtype == long.class || elemtype == Long.class)
						((List<Long>)arr).add(Long.valueOf(getStringVal(ctx.NUMBER(), txt)));
					else if (elemtype == short.class || elemtype == Short.class)
						((List<Short>)arr).add(Short.valueOf(getStringVal(ctx.NUMBER(), txt)));
					else if (elemtype == byte.class || elemtype == Byte.class)
						((List<Byte>)arr).add(Byte.valueOf(getStringVal(ctx.NUMBER(), txt)));
					else if (elemtype == String.class) 
						((List<String>)arr).add(getStringVal(ctx));
					else if (elemtype != null && IJsonable.class.isAssignableFrom(elemtype))
						((List<IJsonable>)arr).add((IJsonable) top.parsedVal);
					else if (elemtype != null && Object.class.isAssignableFrom(elemtype))
						((List<Object>)arr).add(top.parsedVal);
					else
						// what's else?
						throw new NullPointerException(String.format("internal", "Unsupported array for type: %s (field %s)",
								elemtype.getName(), top.parsingProp));


				}
				else if (top.isInMap()) {
					// complex value is helped deserialized in parsedVal
					if (top.parsedVal != null) {
						((List<Object>)arr).add(top.parsedVal);
						top.parsedVal = null;
					}
					else
						((List<Object>)arr).add(getStringVal(ctx.STRING(), txt));
				}
				top.parsedVal = null;
//			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void exitPair(PairContext ctx) {
		super.exitPair(ctx);
		Utils.logi("Property-name: %s", ctx.getChild(0).getText());
		Utils.logi("Property-value: %s", ctx.getChild(2).getText());

		try {
			// String fn = getProp(ctx);
			ParsingCtx top = top();
			String fn = top.parsingProp;
//			if (f == null)
//				throw new AnsonException(0, "Field not found: %s", fn);
			// map's pairs also exits here - map helper
			if (top.isInMap()) {
				((HashMap<String, Object>)top.enclosing).put(top.parsingProp, top.parsedVal);
				top.parsedVal = null;
				top.parsingProp = null;
				return;
			}
			// not map ...
//			else

			Field f = top.fmap.get(fn);
			AnsonField af = f.getAnnotation(AnsonField.class);
			if (af != null && af.ignoreFrom()) {
				if (verbose)
					Utils.logi("%s ignored", fn);
				return;
			}

			Object enclosing = top().enclosing;
			Class<?> ft = f.getType();
			f.setAccessible(true);
			
			if (ft == String.class) {
				String v = getStringVal(ctx);
				f.set(enclosing, v);
			}
			else if (ft.isPrimitive()) {
				// construct primitive value
				String v = ctx.getChild(2).getText();
				setPrimitive((IJsonable) enclosing, f, v);
			}
			else if (ft.isArray())
				f.set(enclosing, toPrimitiveArray((List<?>)top.parsedVal, ft));
			else if (List.class.isAssignableFrom(ft)
					|| AbstractCollection.class.isAssignableFrom(ft)
					|| Map.class.isAssignableFrom(ft)) {
				f.set(enclosing, top.parsedVal);
			}
			else if (IJsonable.class.isAssignableFrom(ft)) {
				// pushed by enterObject()
				f.set(enclosing, top.parsedVal);
			}
			else if (Object.class.isAssignableFrom(ft)){
				Utils.warn("Unsupported type's value of %s deserialized as %s", fn, ft.getName());
				String v = ctx.getChild(2).getText();
				f.set(enclosing, v);
			}
			else throw new AnsonException(0, "sholdn't happen");

			// not necessary, top is dropped
			top.parsedVal = null;
		} catch (ReflectiveOperationException | RuntimeException | AnsonException e) {
			e.printStackTrace();
		}
	}

	private static void setPrimitive(IJsonable obj, Field f, String v)
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
			throw new AnsonException(0, "Unsupported field type: %s (field %s)",
					f.getType().getName(), f.getName());
	}
}
