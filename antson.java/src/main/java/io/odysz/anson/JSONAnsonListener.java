package io.odysz.anson;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.TerminalNode;

import gen.antlr.json.JSONBaseListener;
import gen.antlr.json.JSONListener;
import gen.antlr.json.JSONParser.ArrayContext;
import gen.antlr.json.JSONParser.EnvelopeContext;
import gen.antlr.json.JSONParser.ObjContext;
import gen.antlr.json.JSONParser.PairContext;
import gen.antlr.json.JSONParser.Type_pairContext;
import gen.antlr.json.JSONParser.ValueContext;
import io.odysz.anson.x.AnsonException;
import io.odysz.common.LangExt;
import io.odysz.common.Utils;

public class JSONAnsonListener extends JSONBaseListener implements JSONListener {
	/**Parsing AST node's context, for handling the node's value,
	 * the element class of parsing stack.
	 * @author odys-z@github.com
	 * @param <T>
	 */
	public class ParsingCtx {
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

		/** Annotation's main types */
		private String valType;
		/** Annotation's sub types */
		private String subTypes;

		ParsingCtx(HashMap<String, Field> fmap, IJsonable enclosing) {
			this.fmap = fmap;
			this.enclosing = enclosing;
		}

		ParsingCtx(HashMap<String, Field> fmap, HashMap<String, ?> enclosing) {
			this.fmap = fmap;
			this.enclosing = enclosing;
		}

		ParsingCtx(HashMap<String, Field> fmap, List<?> enclosing) {
			this.fmap = fmap;
			this.enclosing = enclosing;
		}

		boolean isInList() {
			return enclosing instanceof List || enclosing.getClass().isArray();
		}

		boolean isInMap() {
			return enclosing instanceof HashMap;
		}

		/**Set type annotation.<br>
		 * annotation is value of {@link AnsonField#valType()}
		 * @param tn
		 * @return
		 */
		ParsingCtx elemType(String[] tn) {
			this.valType = tn == null || tn.length <= 0 ? null : tn[0];
			this.subTypes = tn == null || tn.length <= 1 ? null : tn[1];
			
			if (!LangExt.isblank(valType)) {
				// change / replace array type
				// e.g. lang.String[] to [Llang.String;
				if (valType.matches(".*\\[\\]$")) {
					valType = "[L" + valType.replaceAll("\\[\\]$", ";");
					valType = valType.replaceFirst("^\\[L\\[", "[[");
				}
			}
			return this;
		}
		
		/**Get type annotation
		 * @return {@link AnsonField#valType()} annotation
		 */
		String elemType() { return this.valType; }

		String subTypes() {
			return subTypes;
		}
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

	/**<p>Parsing objects stack</p>
	 * <p>Element: {@link ParsingCtx}</p>
	 * Top = Current parsingVal object.<br>
	 * Currently all object must be an IJsonable object. */
	private ArrayList<ParsingCtx> stack;

	private ParsingCtx top() { return stack.get(0); }

	private Object toparent(Class<?> type) {
		// no enclosing, no parent
		if (stack.size() <= 1 || LangExt.isblank(type, "null"))
			return null;

		// trace back, guess with type for children could be in array or map
		ParsingCtx p = stack.get(1);
		int i = 2;
		while (p != null) {
			if (type.equals(p.enclosing.getClass()))
				return p.enclosing;
			p = stack.get(i);
			i++;
		}
		return null;
	}

	/**Push parsing node (a envelope, map, list).
	 * @param enclosingClazz new parsing IJsonable object's class
	 * @param elemType type annotation of enclosing list/array. 0: main type, 1: sub-types<br>
	 * This parameter can't be null if is pushing a list node.
	 * @throws ReflectiveOperationException
	 * @throws SecurityException
	 * @throws AnsonException
	 */
	private void push(Class<?> enclosingClazz, String[] elemType)
			throws ReflectiveOperationException, SecurityException, AnsonException {
		if (enclosingClazz.isArray()) {
			HashMap<String, Field> fmap = new HashMap<String, Field>();
			ParsingCtx newCtx = new ParsingCtx(fmap, new ArrayList<Object>());
			stack.add(0, newCtx.elemType(elemType));
		}
		else {
			HashMap<String, Field> fmap = new HashMap<String, Field>();
			if (List.class.isAssignableFrom(enclosingClazz)) {
				List<?> enclosing = new ArrayList<Object>();
				stack.add(0, new ParsingCtx(fmap, enclosing).elemType(elemType));
			}
			else {
				Constructor<?> ctor = null;
				try { ctor = enclosingClazz.getConstructor();
				} catch (NoSuchMethodException e) {
					throw new AnsonException(0, "To make json can be parsed to %s, the class must has a default constructor(0 parameter)\n"
							+ "Also, inner class must be static."
							+ "getConstructor error: %s %s", 
							enclosingClazz.getName(), e.getClass().getName(), e.getMessage());
				}
				if (ctor != null && IJsonable.class.isAssignableFrom(enclosingClazz)) {
					fmap = mergeFields(enclosingClazz, fmap); // map merging is only needed by typed object
					try {
						IJsonable enclosing = (IJsonable) ctor.newInstance(new Object[0]);
						stack.add(0, new ParsingCtx(fmap, enclosing));
					} catch (InvocationTargetException e) {
						throw new AnsonException(0, "Failed to create instance of IJsonable with\nconstructor: %s\n"
							+ "class: %s\nerror: %s\nmessage: %s\n"
							+ "Make sure the object can be created with the constructor.", 
							ctor, enclosingClazz.getName(), e.getClass().getName(), e.getMessage());
					}
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
		try {
			HashMap<String, Field> fmap = stack.size() > 0 ?
					top.fmap : null;
			if (fmap == null || !fmap.containsKey(top.parsingProp)) {
				// In a list, found object, if not type specified with annotation, must failed.
				// But this is confusing to user. Set some report here.
				if (top.isInList() || top.isInMap())
					Utils.warn("Type in list or map is complicate, but no annotation for type info can be found. "
							+ "field type: %s\njson: %s\n"
							+ "Example: @AnsonField(valType=\"io.your.type\")\n"
							+ "Anson instances don't need annotation, but objects in json array without type-pair can also trigger this error report.",
							top.enclosing.getClass(), ctx.getText());;
				throw new AnsonException(0, "Obj type not found. property: %s", top.parsingProp);
			}

			Class<?> ft = fmap.get(top.parsingProp).getType();
			if (Map.class.isAssignableFrom(ft)) {
				// entering a map
				push(ft, null);
				// append annotation
				Field f = top.fmap.get(top.parsingProp);
				AnsonField a = f == null ? null : f.getAnnotation(AnsonField.class);
				String anno = a == null ? null : a.valType();

				if (anno != null) {
					String[] tn = parseElemType(anno);
					top().elemType(tn);
				}
			}
			else
				// entering an envelope
				// push(fmap.get(top.parsingProp).getType());
				push(ft, null);
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

		envetype = ctx.qualifiedName().getText();
		
		try {
			Class<?> clazz = Class.forName(envetype);
			push(clazz, null);
		} catch (ReflectiveOperationException | SecurityException | AnsonException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void enterPair(PairContext ctx) {
		super.enterPair(ctx);
		ParsingCtx top = top();
		top.parsingProp = getProp(ctx);
		top.parsedVal = null;
	}
	
	private static String[] parseElemType(String subTypes) {
		if (LangExt.isblank(subTypes))
			return null;
		return subTypes.split("/", 2); 
	}

	private static String[] parseListElemType(Field f) throws AnsonException {
		// for more information, see
		// https://stackoverflow.com/questions/1868333/how-can-i-determine-the-type-of-a-generic-field-in-java

		Type type = f.getGenericType();
	    if (type instanceof ParameterizedType) {
	        ParameterizedType pType = (ParameterizedType)type;

	        String[] ptypess = pType.getActualTypeArguments()[0].getTypeName().split("<", 2);
	        if (ptypess.length > 1) {
				ptypess[1] = ptypess[1].replaceFirst(">$", "");
				ptypess[1] = ptypess[1].replaceFirst("^L", "");
	        }
	        // figure out array element class 
	        else {
	        	if (!(pType.getActualTypeArguments()[0] instanceof TypeVariable)) {
					@SuppressWarnings("unchecked")
					Class<? extends Object> eleClzz =
						((Class<? extends Object>) pType.getActualTypeArguments()[0]);
					if (eleClzz.isArray()) {
						ptypess = new String[] {ptypess[0], eleClzz.getComponentType().getName()};
					}
	        	}
	        	// else nothing can do here for a type parameter, e.g. "T"
	        	else
	        		Utils.warn("Element type %s for %s is a type parameter (%s) - ignored",
	        				pType.getActualTypeArguments()[0].getTypeName(),
	        				f.getName(),
	        				pType.getActualTypeArguments()[0].getClass());
	        }
	        return ptypess;
	    }
	    else if (f.getType().isArray()) {
	    	// complex array may also has annotation
			AnsonField a = f == null ? null : f.getAnnotation(AnsonField.class);
			String tn = a == null ? null : a.valType();
			String[] valss = parseElemType(tn);
	    	
	    	String eleType = f.getType().getComponentType().getTypeName();
	    	if (valss != null && !eleType.equals(valss[0]))
	    		Utils.warn("[JSONAnsonListener#parseListElemType()]: Field %s is not annotated correctly.\n"
	    				+ "field parameter type: %s, annotated element type: %s, annotated sub-type: %s",
	    				f.getName(), eleType, valss[0], valss[1]);

	    	if (valss != null && valss.length > 1)
	    		return new String[] {eleType, valss[1]};
	    	else return new String[] {eleType};
	    }
	    else {
	    	// not a parameterized, not an array, try annotation
			AnsonField a = f == null ? null : f.getAnnotation(AnsonField.class);
			String tn = a == null ? null : a.valType();
			return parseElemType(tn);
	    }
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
	 * @return value in string
	 */
	private static String getStringVal(PairContext ctx) {
		TerminalNode str = ctx.value().STRING();
		String txt = ctx.value().getText();
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
	}

	/**
	 * grammar:<pre>value
	: STRING
	| NUMBER
	| obj		// all array's obj value can't parsed as Anson, taken as HashMap - TODO doc: known issue
	| envelope
	| array
	| 'true'
	| 'false'
	| 'null'
	;</pre>
	 * @param ctx
	 * @return simple value (STRING, NUMBER, 'true', 'false', null)
	 */
	private static Object figureJsonVal(ValueContext ctx) {
		String txt = ctx.getText();
		if (txt == null)
			return null;
		else if (ctx.NUMBER() != null)
			try { return Integer.valueOf(txt); }
			catch (Exception e) {
				try { return Float.valueOf(txt); }
				catch (Exception e1) {
					return Double.valueOf(txt);
				}
			}
		else if (ctx.STRING() != null)
			return getStringVal(ctx.STRING(), txt);
		else if (txt != null && txt.toLowerCase().equals("true"))
			return new Boolean(true);
		else if (txt != null && txt.toLowerCase().equals("flase"))
			return new Boolean(false);
		return null;
	}

	@Override
	public void enterArray(ArrayContext ctx) {
		try {
			ParsingCtx top = top();

			// if in a list or a map, parse top's sub-type as the new node's value type
			if (top.isInList() || top.isInMap()) {
				// pushing ArrayList.class because entering array, isInMap() == true means needing to figure out value type
				//
				String[] tn = parseElemType(top.subTypes());
				// ctx:		[{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]
				// subtype:	io.odysz.anson.Anson
				// tn :		[io.odysz.anson.Anson]
				push(ArrayList.class, tn);
			}
			// if field available, parse field's value type as the new node's value type
			else {
				Class<?> ft = top.fmap.get(top.parsingProp).getType();
				Field f = top.fmap.get(top.parsingProp);
				// AnsT3 { ArrayList<Anson[]> ms; }
				// ctx: [[{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]]
				// [0]: io.odysz.anson.Anson[], 
				// [1]: io.odysz.anson.Anson
				String[] tn = parseListElemType(f);
				push(ft, tn);
			}
			
			// now top is the enclosing list, it's component type is elem-type
//			if (!LangExt.isblank(tn))
//				top().elemType(tn);

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

		// figure the type if possible
		String et = top.elemType();
		if (!LangExt.isblank(et))
			try {
				Class<?> arrClzz = Class.forName(et);
				if (arrClzz.isArray())
					top.parsedVal = toPrimitiveArray(arr, arrClzz);	
			} catch (IllegalArgumentException | ClassNotFoundException e) {
				Utils.warn("Trying convert array to annotated type failed.\ntype: %s\njson: %s\nerror: %s",
						et, ctx.getText(), e.getMessage());
			}
		// No annotation, for 2d list, parsed value is still a list.
		// If enclosed element of array is also an array, it can not been handled here
		// Because there is no clue for sub array's type if annotation is empty
	}

	/**
	 * Unboxes a List in to a primitive array.
	 * reference:
	 * https://stackoverflow.com/questions/25149412/how-to-convert-listt-to-array-t-for-primitive-types-using-generic-method
	 *
	 * @param  list      the List to convert to a primitive array
	 * @param  arrType the primitive array type to convert to
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
	private static <P> P toPrimitiveArray(List<?> list, Class<P> arrType) {
	    if (!arrType.isArray()) {
	        throw new IllegalArgumentException(arrType.toString());
	    }
	    if (list == null)
	    	return null;

	    Class<?> eleType = arrType.getComponentType();

	    P array = arrType.cast(Array.newInstance(eleType, list.size()));

	    for (int i = 0; i < list.size(); i++) {
	        Array.set(array, i, list.get(i));
	    }

	    return array;
	}
	
	/**
	 * grammar:<pre>value
	: STRING
	| NUMBER
	| obj		// all array's obj value can't parsed as Anson, taken as HashMap - TODO doc: known issue
	| envelope
	| array
	| 'true'
	| 'false'
	| 'null'
	;</pre>
	 * @see gen.antlr.json.JSONBaseListener#exitValue(gen.antlr.json.JSONParser.ValueContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void exitValue(ValueContext ctx) {
		ParsingCtx top = top();
		if (top.isInList() || top.isInMap()) {
			// if in a map, parsingProp is the map key,
			// element type can only been handled with a guess,
			// or according to annotation
			// String txt = ctx.getText();
			if (top.isInList()) {
				List<?> enclosLst = (List<?>) top.enclosing;
				// for List, ft is not null
				if (top.parsedVal == null) {
					// simple value like String or number
					((List<Object>)enclosLst).add(figureJsonVal(ctx));
				}
				else {
					// try figure out is element also an array if enclosing object is an array
					// e.g. convert elements of List<String> to String[]
					// FIXME issue: if the first element is 0 length, it will failed to convert the array
					Class<?> parsedClzz = top.parsedVal.getClass();
					if (List.class.isAssignableFrom(parsedClzz)) {
						if (LangExt.isblank(top.elemType())) {
							// change list to array
							List<?> lst = (List<?>)top.parsedVal;
							if (lst != null && lst.size() > 0) {
								// search first non-null element's type
								Class<? extends Object> eleClz = null;
								int ix = 0;
								while (ix < lst.size() && lst.get(ix) == null)
									ix++;
								if (ix < lst.size())
									eleClz = lst.get(ix).getClass();

								if (eleClz != null) {
									((List<Object>)enclosLst).add(toPrimitiveArray(lst,
											Array.newInstance(eleClz, 0).getClass()));

									// remember elem type for later null element
									top.elemType(new String[] {eleClz.getName()});
								}
								// all elements are null, ignore the list is the only way
							}
							else
								// FIXME this will broken when first element's length is 0.
								((List<Object>)enclosLst).add(lst.toArray());
						}
						// branch: with annotation or type name already figured out from 1st element 
						else {
							try {
								List<?> parsedLst = (List<?>)top.parsedVal;
								String eleType = top.elemType();
								Class<?> eleClz = Class.forName(eleType);
								if (eleClz.isAssignableFrom(parsedClzz)) {
									// annotated element can be this branch
									((List<Object>)enclosLst).add(parsedLst);
								}
								else {
									// type is figured out from the previous element,
									// needing conversion to array
									// 
									// Bug: object value can't been set into string array
									// lst.getClass().getTypeName() = java.lang.ArrayList
									// ["val",88.91669145042222]
									
									
									// Test case:	AnsT3 { ArrayList<Anson[]> ms; }
									// ctx: 		[{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]
									// parsedLst:	[{type: io.odysz.anson.AnsT2, s: 4, m: null}, {type: io.odysz.anson.AnsT1, ver: "x", m: null}]
									// parsedClzz:	java.util.ArrayList
									// eleType:		[Lio.odysz.anson.Anson;
									// eleClz:		class [Lio.odysz.anson.Anson;
									// action - change parsedLst to array, add to enclosLst
									((List<Object>)enclosLst).add(toPrimitiveArray(parsedLst,
													Array.newInstance(eleClz, 0).getClass()));
								}
							} catch (Exception e) {
								Utils.warn(ctx.getText());
								e.printStackTrace();
							}
						}
					}
					else
						((List<Object>)enclosLst).add(top.parsedVal);
				}
				top.parsedVal = null;
			}
			else if (top.isInMap()) {
				// parsed Value can already got when exit array
				if (top.parsedVal == null)
					top.parsedVal = getStringVal(ctx.STRING(), ctx.getText());
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void exitPair(PairContext ctx) {
		super.exitPair(ctx);
		if (AnsonFlags.parser) {
			Utils.logi("[AnsonFlags.parser] Property-name: %s", ctx.getChild(0).getText());
			Utils.logi("[AnsonFlags.parser] Property-value: %s", ctx.getChild(2).getText());
		}

		try {
			// String fn = getProp(ctx);
			ParsingCtx top = top();
			String fn = top.parsingProp;

			// map's pairs also exits here - map helper
			if (top.isInMap()) {
				((HashMap<String, Object>)top.enclosing).put(top.parsingProp, top.parsedVal);
				top.parsedVal = null;
				top.parsingProp = null;
				return;
			}
			// not map ...

			Object enclosing = top().enclosing;
			Field f = top.fmap.get(fn);
			if (f == null)
				throw new AnsonException(0, "Field ignored: field: %s, value: %s", fn, ctx.getText());

			f.setAccessible(true);
			AnsonField af = f.getAnnotation(AnsonField.class);
			if (af != null && af.ignoreFrom()) {
				if (AnsonFlags.parser)
					Utils.logi("[AnsonFlags.parser] %s ignored", fn);
				return;
			}
			else if (af != null && af.ref() == AnsonField.enclosing) {
				Object parent = toparent(f.getType());
				if (parent == null)
					Utils.warn("parent %s is ignored: reference is null", fn);

				f.set(enclosing, parent);
				return;
			}

			Class<?> ft = f.getType();
			
			if (ft == String.class) {
				String v = getStringVal(ctx);
				f.set(enclosing, v);
			}
			else if (ft.isPrimitive()) {
				// construct primitive value
				String v = ctx.getChild(2).getText();
				setPrimitive((IJsonable) enclosing, f, v);
			}
			else if (ft.isEnum()) {
				String v = getStringVal(ctx);
				if (!LangExt.isblank(v))
					f.set(enclosing, Enum.valueOf((Class<Enum>) ft, v));
			}
			else if (ft.isArray())
				f.set(enclosing, toPrimitiveArray((List<?>)top.parsedVal, ft));
			else if (List.class.isAssignableFrom(ft)
					|| AbstractCollection.class.isAssignableFrom(ft)
					|| Map.class.isAssignableFrom(ft)) {
				f.set(enclosing, top.parsedVal);
			}
			else if (IJsonable.class.isAssignableFrom(ft)) {
				// f.set(enclosing, top.parsedVal);
				String v = ctx.getChild(2).getText();

				if (!LangExt.isblank(v, "null")) {
					IJsonable j = Anson.fromJson(v);
					f.set(enclosing, j);
				}
			}
			else if (Object.class.isAssignableFrom(ft)) {
				Utils.warn("\nDeserializing unsupported type, field: %s, type: %s, enclosing type: %s",
						fn, ft.getName(), enclosing == null ? null : enclosing.getClass().getName());
				String v = ctx.getChild(2).getText();

				if (!LangExt.isblank(v, "null"))
					f.set(enclosing, v);
			}
			else throw new AnsonException(0, "sholdn't happen");

			// not necessary, top is dropped
			top.parsedVal = null;
		} catch (ReflectiveOperationException | RuntimeException e) {
			e.printStackTrace();
		} catch (AnsonException e) {
			Utils.warn(e.getMessage());
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
