//package io.odysz.semantic.jprotocol.test;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import io.odysz.anson.IJsonable;
//import io.odysz.anson.JSONAnsonListener;
//import io.odysz.anson.JsonOpt;
//import io.odysz.anson.StringBufferOutputStream;
//import io.odysz.anson.Anson;
//import io.odysz.anson.x.AnsonException;
//
///**<p>Test case of SemanticObject, for testing (de)serializing SemanticObject map.</p>
// * 
// * @author odys-z@github.com
// */
//public class SemanticObjV11 implements IJsonable {
//
//	static {
//		JSONAnsonListener.registFactory(SemanticObjV11.class, (s) -> {
//				try {
//					return Anson.fromJson(s);
//				} catch (AnsonException e) {
//					e.printStackTrace();
//					return null;
//				}
//		});
//	}
//
//	@Override
//	public IJsonable toBlock(OutputStream stream, JsonOpt... opts) throws AnsonException, IOException {
//		stream.write("{type: ".getBytes());
//		stream.write(getClass().getName().getBytes());
//		
//		stream.write(new byte[] {',', ' '});
//		stream.write(("props: ").getBytes());
//		
//		Anson.toMapBlock(stream, props, null);
//
//		stream.write("}".getBytes());
//		stream.flush();
//		return this;
//	}
//
//	@Override
//	public IJsonable toJson(StringBuffer buf) throws IOException, AnsonException {
//		return toBlock(new StringBufferOutputStream(buf));
//	}
//
//	protected HashMap<String, Object> props;
//	public HashMap<String, Object> props() { return props; }
//
//	/**@param prop
//	 * @return null if the property doesn't exists
//	 */
//	public Class<?> getType (String prop) {
//		if (prop == null || props == null || !props.containsKey(prop))
//			return null;
//
//		Object p = props.get(prop);
//		return p == null
//				? Object.class // has key, no value
//				: p.getClass();
//	}
//
//	public boolean has(String tabl) {
//		return props != null && props.containsKey(tabl) && props.get(tabl) != null;
//	}
//
//	public Object get(String prop) {
//		return props == null ? null : props.get(prop);
//	}
//
//	public String getString(String prop) {
//		return props == null ? null : (String) props.get(prop);
//	}
//
//	public SemanticObjV11 data() {
//		return (SemanticObjV11) get("data");
//	}
//
//	public SemanticObjV11 data(SemanticObjV11 data) {
//		return put("data", data);
//	}
//	
//	public String port() {
//		return (String) get("port");
//	}
//
//	public SemanticObjV11 code(String c) {
//		return put("code", c);
//	}
//	
//	public String code() {
//		return (String) get("code");
//	}
//	
//	public SemanticObjV11 port(String port) {
//		return put("port", port);
//	}
//
//	public String msg() {
//		return (String) get("msg");
//	}
//	
//	public SemanticObjV11 msg(String msg, Object... args) {
//		if (args == null || args.length == 0)
//			return put("msg", msg);
//		else
//			return put("msg", String.format(msg, args));
//	}
//
//	/**Put resultset (SResultset) into "rs".
//	 * Useing this should be careful as the rs is a 3d array.
//	 * @param resultset
//	 * @param total 
//	 * @return this
//	 * @throws TransException
//	 */
//	public SemanticObjV11 rs(Object resultset, int total) throws AnsonException {
//		add("total", total);
//		return add("rs", resultset);
//	}
//
//	public Object rs(int i) {
//		return ((ArrayList<?>)get("rs")).get(i);
//	}
//
//	@SuppressWarnings("unchecked")
//	public int total(int i) {
//		if (get("total") == null)
//			return -1;
//		ArrayList<Object> lst = ((ArrayList<Object>)get("total"));
//		if (lst == null || lst.size() <= i)
//			return -1;
//		Object obj = lst.get(i);
//		if (obj == null)
//			return -1;
//		return (int)obj;
//	}
//	
//	public SemanticObjV11 total(int rsIdx, int total) throws AnsonException {
//		// the total(int) returned -1
//		if (total < 0) return this;
//
//		@SuppressWarnings("unchecked")
//		ArrayList<Integer> lst = (ArrayList<Integer>) get("total");
//		if (lst == null || lst.size() <= rsIdx)
//			throw new AnsonException(0, "No such index for rs; %s", rsIdx);
//		lst.set(rsIdx, total);
//		return this;
//	}
//	
//	public String error() {
//		return (String) get("error");
//	}
//	
//	public SemanticObjV11 error(String error, Object... args) {
//		if (args == null || args.length == 0)
//			return put("error", error);
//		else
//			return put("error", String.format(error, args));
//	}
//	
//	public SemanticObjV11 put(String prop, Object v) {
//		if (props == null)
//			props = new HashMap<String, Object>();
//		props.put(prop, v);
//		return this;
//	}
//
//	/**Add element 'elem' to array 'prop'.
//	 * @param prop
//	 * @param elem
//	 * @return this
//	 * @throws TransException 
//	 */
//	@SuppressWarnings("unchecked")
//	public SemanticObjV11 add(String prop, Object elem) throws AnsonException {
//		if (props == null)
//			props = new HashMap<String, Object>();
//		if (!props.containsKey(prop))
//			props.put(prop, new ArrayList<Object>());
//		if (props.get(prop) instanceof List)
//			((ArrayList<Object>) props.get(prop)).add(elem);
//		else throw new AnsonException(0, "%s seams is not an array. elem %s can't been added", prop, elem);
//		return this;
//	}
//
//	/**Add int array.
//	 * @param prop
//	 * @param ints
//	 * @return this
//	 * @throws TransException
//	 */
//	public SemanticObjV11 addInts(String prop, int[] ints) throws AnsonException {
//		for (int e : ints)
//			add(prop, e);
//		return this;
//	}
//
//	public Object remove(String prop) {
//		if (props != null && props.containsKey(prop))
//			return props.remove(prop);
//		else return null;
//	}
//
//	/**Print for reading - string can't been converted back to object
//	 * @param out
//	 */
//	public void print(PrintStream out) {
//		if (props != null)
//			for (String k : props.keySet()) {
//				out.print(k);
//				out.print(" : ");
//				Class<?> c = getType(k);
//				if (c == null)
//					continue;
//				else if (c.isAssignableFrom(SemanticObjV11.class)
//					|| SemanticObjV11.class.isAssignableFrom(c))
//					((SemanticObjV11)get(k)).print(out);
//				else if (Collection.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c)) {
//					Iterator<?> i = ((Collection<?>) get(k)).iterator(); 
//					out.println("[" + ((Collection<?>) get(k)).size() + "]");
//					while (i.hasNext()) {
//						Object ele = i.next();
//						c = ele.getClass();
//						if (c.isAssignableFrom(SemanticObjV11.class)
//								|| SemanticObjV11.class.isAssignableFrom(c))
//							((SemanticObjV11)ele).print(out);
//						else
//							out.print(get(k));
//					}
//				}
//				else out.print(get(k));
//				out.print(",\t");
//			}
//		out.println("");
//	}
//}
