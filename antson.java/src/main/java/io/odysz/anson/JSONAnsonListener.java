package io.odysz.anson;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
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
import io.odysz.anson.IJsonable.JsonableFactory;
import io.odysz.anson.x.AnsonException;
import io.odysz.common.LangExt;
import io.odysz.common.Utils;

public class JSONAnsonListener extends JSONBaseListener implements JSONListener {
	public static boolean verbose;

	/**<p>Parsing AST node's context, for handling the node's value,
	 * the element class of parsing stack.</p>
	 * <p>Memo: The hard lesson learned from this is if you want parse a grammar,
	 * you better follow the grammar structure.</p>
	 * @author odys-z@github.com
	 */
	public class ParsingCtx {
		/**The json prop (object key) */
		protected String parsingProp;
		/**The parsed native value */
		protected Object parsedVal;
		/**e.g. parent object reference */
		private Object enclosing;
		/**Fields' map. C# has an extra properties' map */
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
		 * @return this
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

	private static HashMap<Class<?>, JsonableFactory> factorys;

	/**Merge clazz's field meta up to the IJsonable ancestor.
	 * @param clazz
	 * @param fmap
	 * @return fmap
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

	/**<p>Parsing objects stack - top node is the currently parsing context</p>
	 * Top = Current parsingVal object.<br>
	 * Currently all object must be an IJsonable object.
	 * <p>Element: {@link ParsingCtx}</p>
	 * */
	private ArrayList<ParsingCtx> stack;

	private ParsingCtx top() { return stack.get(0); }

	/**Push the parsing node (a envelope, map, list) into this' {@link #stack}.
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
				try { ctor = enclosingClazz.getConstructor(new Class<?>[] {});
				} catch (NoSuchMethodException e) {
					throw new AnsonException(0,
						"To make json can be parsed to %s, the class must has a default public constructor(0 parameter)\n"
						+ "Also, inner class must be static.\n"
						+ "Class.getConstructor() error on getting: %s %s\n",
						enclosingClazz.getName(), e.getMessage(), e.getClass().getName());
				}

				if (IJsonable.class.isAssignableFrom(enclosingClazz)) {
					if (ctor == null)
						throw new AnsonException(0,
							"To make json can be parsed to %0$s, the class must has a default public constructor(0 parameter)\n",
							enclosingClazz.getName());
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

	private String envelopName() {
		if (stack != null)
			for (int i = 0; i < stack.size(); i++)
				if (stack.get(i).enclosing instanceof Anson)
					return stack.get(i).enclosing.getClass().getName();
		return null;
	}

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
					Utils.warn("Type of elements in the list or map is complicate, but no annotation for type info can be found.\n"
							+ "field type: %s\njson: %s\n"
							+ "E.g. Java field example: @AnsonField(valType=\"io.your.type\")\n"
							+ "Anson instances don't need annotation, but objects in json array without type-pair can also trigger this error report.",
							top.enclosing.getClass(), ctx.getText());;
				throw new AnsonException(0, "Obj type not found.\n\tproperty / field name: %s,\n\tenclosing type: %s",
						top.parsingProp, top.enclosing == null ? "null" : top.enclosing.getClass().getName());
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
				push(ft, null);
		} catch (SecurityException | ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	public IJsonable parsedEnvelope(boolean verbose) throws AnsonException {
		this.verbose = verbose;
		if (stack == null || stack.size() == 0)
			throw new AnsonException(0, "No envelope can be found.");
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

	/**Semantics of entering a type pair, when type is found and enclosing is an IJsonable object.<br>
	 * This is always happening on entering an object.
	 * The logic opposite is exit object. (exit pair?)
	 * @see gen.antlr.json.JSONBaseListener#enterType_pair(gen.antlr.json.JSONParser.Type_pairContext)
	 */
	@Override
	public void enterType_pair(Type_pairContext ctx) {
		if (envetype != null)
			// ignore this type specification, keep consist with java type
			return;

		// envetype = ctx.qualifiedName().getText();
		TerminalNode str = ctx.qualifiedName().STRING();
		String txt = ctx.qualifiedName().getText();
		envetype = getStringVal(str, txt);

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
	        	Type argType = pType.getActualTypeArguments()[0]; // jdk: class [Lio.odysz.anson.Photo;  
	        	if (!(argType instanceof TypeVariable) && !(argType instanceof WildcardType)) {
	        				
	        		/* change for Android compatibility
	        		 * for field : List<Photo[]> photos,
	        		 * on JDK 1.8: argType = Class<T>(io.odysz.anson.Photo[])
	        		 * on Andoid : argType = GeneralArrayTypeImpl(io.oz.album.tier.Photo[])
	        		 * 
					@SuppressWarnings("unchecked")
					Class<? extends Object> eleClzz = ((Class<? extends Object>) argType);
					if (eleClzz.isArray()) {
						ptypess = new String[] {ptypess[0], eleClzz.getComponentType().getName()};
					}
					*/
	        		String lstName = argType.getTypeName();
	        		if (lstName.matches(".*\\[\\]$")) {
						ptypess = new String[] {ptypess[0], "[L" + lstName.replaceAll("\\[\\]$", ";")};
	        		}

	        	}
	        	// else nothing can do here for a type parameter, e.g. "T"
	        	else
	        		if (AnsonFlags.parser)
	        			Utils.warn("[AnsonFlags.parser] warn Element type <%s> for %s is a type parameter (%s) - ignored",
	        				argType.getTypeName(), f.getName(), argType.getClass());
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
	 * @return the prop value in string
	 */
	private static String getProp(PairContext ctx) {
		TerminalNode p = ctx.propname().IDENTIFIER();
		/*
		return p == null
				? ctx.propname().STRING().getText().replaceAll("(^\\s*\"\\s*)|(\\s*\"\\s*$)", "")
				: p.getText();
		*/

		String prop = p == null ?
				ctx.propname().STRING() != null ?
					ctx.propname().STRING().getText() :
					ctx.propname().getText() : p.getText();
		return prop.replaceAll("(^\\s*\"\\s*)|(\\s*\"\\s*$)", "");
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
			return Boolean.valueOf(true);
		else if (txt != null && txt.toLowerCase().equals("flase"))
			return Boolean.valueOf(false);
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
				Field f = top.fmap.get(top.parsingProp);
				if (f == null)
					throw new AnsonException(1, "Field not found in %s field: %s, value: %s",
							top.enclosing.getClass().getName(), top.parsingProp, ctx.getText());
				Class<?> ft = top.fmap.get(top.parsingProp).getType();
				// AnsT3 { ArrayList<Anson[]> ms; }
				// ctx: [[{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]]
				// [0]: io.odysz.anson.Anson[],
				// [1]: io.odysz.anson.Anson
				String[] tn = parseListElemType(f);
				push(ft, tn);
			}
		} catch (ReflectiveOperationException | SecurityException | AnsonException e) {
			e.printStackTrace();
			throw new NullPointerException(e.getMessage() + "\n" + ctx.getText());
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

		// figure the type if possible - convert to array
		String et = top.elemType();
		if (!LangExt.isblank(et, "\\?.*")) // TODO debug: where did this type comes from?
			try {
				Class<?> arrClzz = Class.forName(et);
				if (arrClzz.isArray())
					top.parsedVal = toPrimitiveArray(arr, arrClzz);
			} catch (AnsonException | IllegalArgumentException | ClassNotFoundException e) {
				Utils.warn("Trying convert array to annotated type failed.\ntype: %s\njson: %s\nerror: %s\n%s",
							et, ctx.getText(), e.getMessage(),
							"If the client is js, check does every elements are the same type?");
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
	 * @throws AnsonException list element class doesn't equal array element type - not enough annotation?
	 * @throws NullPointerException
	 *         if either of the arguments are null, or if any of the elements
	 *         of the List are null
	 * @throws IllegalArgumentException
	 *         if the specified Class does not represent an array type, if
	 *         the component type of the specified Class is not a primitive
	 *         type, or if the elements of the specified List can not be
	 *         stored in an array of type P
	 */
	private static <P> P toPrimitiveArray(List<?> list, Class<P> arrType) throws AnsonException {
	    if (!arrType.isArray()) {
	        throw new IllegalArgumentException(arrType.toString());
	    }
	    if (list == null)
	    	return null;

	    Class<?> eleType = arrType.getComponentType();

	    P array = arrType.cast(Array.newInstance(eleType, list.size()));

	    for (int i = 0; i < list.size(); i++) {
	    	Object lstItem = list.get(i);
	    	if (lstItem == null)
	    		continue;

	    	/* this guess is error prone, let's tell user why. May be more annotation is needed
	    	 * Note: jserv v1.3.0
	    	 * Sometimes the eleType is figured out (only a guess) from the first element, let's try tolerate type mismatch here.
	    	 * This is correct in java, but it's ok in js for elements with different type. Just try convert it into string.
	    	 */
	    	if (eleType == String.class && !eleType.isAssignableFrom(lstItem.getClass()))
	    		lstItem = lstItem.toString();
	    	else if (!eleType.isAssignableFrom(lstItem.getClass()) && !matchPrimaryObj(eleType, lstItem.getClass()))
	    		throw new AnsonException(1, "Set element (v: %s, type %s) to array of type of \"%s[]\" failed.\n"
	    				+ "Array element's type not annotated?\n"
	    				+ "Please also note that all values in string are tolerated. (which is typical in js for array of elements in different types.)",
	    				lstItem, lstItem.getClass(), eleType);

	        Array.set(array, i, lstItem);
	    }

	    return array;
	}

	private static boolean matchPrimaryObj(Class<?> primtype, Class<? extends Object> objtype) {
		// https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html
		return primtype == int.class && objtype == Integer.class
			|| primtype == float.class && objtype == Float.class
			|| primtype == double.class && objtype == Double.class
			|| primtype == long.class && objtype == Long.class
			|| primtype == short.class && objtype == Short.class
			|| primtype == boolean.class && objtype == Boolean.class
			|| primtype == char.class && objtype == Character.class
			;
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
						if (LangExt.isblank(top.elemType(), "\\?.*")) {
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
									try {
										((List<Object>)enclosLst).add(toPrimitiveArray(lst,
												Array.newInstance(eleClz, 0).getClass()));
									} catch (AnsonException e) {
										Utils.warn("Trying convert array to annotated type failed.\nenclosing: %s\njson: %s\nerror: %s\n%s",
											top.enclosing, ctx.getText(), e.getMessage(),
											"If the client is js, check does every elements are the same type?");
									}

									// remember elem type for later null element
									// v1.3.0 This remembering wrong type. Nested array type doesn't figured out correctly - all test passed without this.
									// top.elemType(new String[] {eleClz.getName()});
								}
								// all elements are null, ignore the list is the only way
							}
							else
								// FIXME this will be broken when first element's length is 0.
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
								Utils.warn(envelopName());
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
			else //if (top.isInMap()) {
				// parsed Value can already got when exit array
				if (top.parsedVal == null)
					/** NOTE v1.3.0 25 Aug 2021 - Doc Task # 001
					 *  When client upload json, it's automatically escaped.
					 *  This makes DB (or server stored data) are mixed with escaped and un-escaped strings.
					 *  When a json string is parsed, we unescape it for the initial value (and escape it when send back - toBlock() is called)
					 *  The following is experimental to keep server side data be consists with raw data.
					 *  
					 *  befor change:
					 *  top.parsedVal = getStringVal(ctx.STRING(), ctx.getText());
					 */
					top.parsedVal = Anson.unescape(getStringVal(ctx.STRING(), ctx.getText()));
			// }
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
			if (f == null) {
				// throw new AnsonException(0, "Field ignored: field: %s, value: %s", fn, ctx.getText());
				Utils.warn("Field ignored: field: %s, value: %s", fn, ctx.getText());
				return;
			}

			f.setAccessible(true);
			AnsonField af = f.getAnnotation(AnsonField.class);
			if (af != null && af.ignoreFrom()) {
				if (AnsonFlags.parser)
					Utils.logi("[AnsonFlags.parser] %s ignored", fn);
				return;
			}
			else if (af != null && af.ref() == AnsonField.enclosing) {
				Object parent = toparent(f.getType());
				if (parent == null && verbose)
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
			// Design notes: this is broken into 2 branches in c#.
			else if (List.class.isAssignableFrom(ft)
					|| AbstractCollection.class.isAssignableFrom(ft)
					|| Map.class.isAssignableFrom(ft)) {
				f.set(enclosing, top.parsedVal);
			}
			else if (IJsonable.class.isAssignableFrom(ft)) {
                // By pass for serializing and deserializing a string value by user, e.g. Port() & Port#ToBlock()
                if (top.parsedVal != null && top.parsedVal.getClass() == String.class)
                {
                	Constructor ctor = ft.getConstructor(new Class<?>[] {String.class});
                    if (ctor == null)
                        throw new AnsonException(0,
                        		"To deserialize json to %s, the class must has a constructor(1 string parameter)\n" +
                        		"string value: %s",
                        		ft.getTypeName(), top.parsedVal);
                    f.set(enclosing, ctor.newInstance(top.parsedVal));
                }
                else if (Anson.class.isAssignableFrom(ft))
					f.set(enclosing, top.parsedVal);
				else {
					// Subclass of IJsonable must registered
					String v = getStringVal(ctx);
					if (!LangExt.isblank(v, "null"))
						f.set(enclosing, invokeFactory(f, v));
				}
			}
			else if (Object.class.isAssignableFrom(ft)) {
				Utils.warn("\nDeserializing unsupported type, field: %s, type: %s, enclosing type: %s",
						fn, ft.getName(), enclosing == null ? null : enclosing.getClass().getName());
				String v = ctx.getChild(2).getText();

				if (!LangExt.isblank(v, "null"))
					f.set(enclosing, v);
			}
			else if (top.parsedVal != null)
				new AnsonException(0, "sholdn't happen");

			// not necessary, top is dropped
			top.parsedVal = null;
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	private IJsonable invokeFactory(Field f, String v) throws AnsonException {
		if (factorys == null || !factorys.containsKey(f.getType()))
			throw new AnsonException(0,
					"Subclass of IJsonable (%s %s) must registered.\n - See javadoc of IJsonable.JsonFacotry\n"
					+ "Or don't declare the field as %1$s, use a subclass of Anson\n"
					+ "For java client, this is possible IPort implementation class not registered at client side (FIXME: Jserv should deprecate IPort)",
					f.getType(),
					f.getName());

		JsonableFactory factory = factorys.get(f.getType());
		try { return factory.fromJson(v);}
		catch (Throwable t) {
			Throwable cause = t.getCause();
			throw new AnsonException(0,
					"Invoking registered factory failed for value: %s\n" +
					"Field Type: %s,\nCause: %s\tMessage: %s\n",
					v, f.getType().getName(),
					cause == null ? "null" : cause.getClass().getName(), cause == null ? "null" : cause.getMessage());
		}
	}

	/**Set primary type values.
	 * <p>byte short int long float double boolean char</p>
	 * See <a href='https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html'>
	 * Oracle Java Documentation</a>
	 * @param obj
	 * @param f
	 * @param v
	 * @throws RuntimeException
	 * @throws ReflectiveOperationException
	 * @throws AnsonException
	 */
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
		else if (f.getType() == boolean.class || f.getType() == Boolean.class)
			f.set(obj, Boolean.valueOf(v));
		else if (f.getType() == char.class) {
			char c = v != null && v.length() > 0 ? v.charAt(0) == '"' ? v.charAt(1) : v.charAt(0) : '0';
			if (verbose)
				Utils.warn("Guessing json string (%s) as a char: %s", v, c);
			f.set(obj, c);
		}
		else
			// what's else?
			throw new AnsonException(0, "Unsupported field type: %s (field %s)",
					f.getType().getName(), f.getName());
	}

	/**Register a factory of IJsonable implementation.
	 * @param jsonable
	 * @param factory
	 */
	public static void registFactory(Class<?> jsonable, JsonableFactory factory) {
		if (factorys == null)
			factorys = new HashMap<Class<?>, JsonableFactory>();
		factorys.put(jsonable, factory);
	}
}
