package io.odysz.semantic.jprotocol.test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.odysz.anson.Anson;

/**<p>The semantics data used internally by semantic-DA to handle semantics configuration.</p>
 * <p>SemanticObject implement methods to write itself as a json value with a writer provided by the caller.
 * This can be used to write the object into other structured object.</p>
 * <p><b>Note:</b> The equivalent of JsonObject in a request is JMessage.
 * <p>Question: If a json request object is handled by a port, e.g. SQuery,
 * is their any property name not known by the port?</p>
 * <p>If no such properties, then there shouldn't be put() and get().</p>
 * 
 * @author odys-z@github.com
 */
public class T_SemanticObject extends Anson {

	protected HashMap<String, Object> props;
	public HashMap<String, Object> props() { return props; }

	/**@param prop
	 * @return null if the property doesn't exists
	 */
	public Class<?> getType (String prop) {
		if (prop == null || props == null || !props.containsKey(prop))
			return null;

		Object p = props.get(prop);
		return p == null
				? Object.class // has key, no value
				: p.getClass();
	}

	public boolean has(String prop) {
		return props != null && props.containsKey(prop) && props.get(prop) != null;
	}

	public Object get(String prop) {
		return props == null ? null : props.get(prop);
	}

	public String getString(String prop) {
		return props == null ? null : (String) props.get(prop);
	}

	public T_SemanticObject data() {
		return (T_SemanticObject) get("data");
	}

	public T_SemanticObject data(T_SemanticObject data) {
		return put("data", data);
	}
	
	public String port() {
		return (String) get("port");
	}

	public T_SemanticObject code(String c) {
		return put("code", c);
	}
	
	public String code() {
		return (String) get("code");
	}
	
	public T_SemanticObject port(String port) {
		return put("port", port);
	}

	public String msg() {
		return (String) get("msg");
	}
	
	public T_SemanticObject msg(String msg, Object... args) {
		if (args == null || args.length == 0)
			return put("msg", msg);
		else
			return put("msg", String.format(msg, args));
	}

	/**Put resultset (SResultset) into "rs".
	 * Useing this should be careful as the rs is a 3d array.
	 * @param resultset
	 * @param total 
	 * @return this
	 * @throws T_TransException
	 */
	public T_SemanticObject rs(Object resultset, int total) throws T_TransException {
		add("total", total);
		return add("rs", resultset);
	}

	public Object rs(int i) {
		return ((ArrayList<?>)get("rs")).get(i);
	}

	@SuppressWarnings("unchecked")
	public int total(int i) {
		if (get("total") == null)
			return -1;
		ArrayList<Object> lst = ((ArrayList<Object>)get("total"));
		if (lst == null || lst.size() <= i)
			return -1;
		Object obj = lst.get(i);
		if (obj == null)
			return -1;
		return (int)obj;
	}
	
	public T_SemanticObject total(int rsIdx, int total) throws T_TransException {
		// the total(int) returned -1
		if (total < 0) return this;

		@SuppressWarnings("unchecked")
		ArrayList<Integer> lst = (ArrayList<Integer>) get("total");
		if (lst == null || lst.size() <= rsIdx)
			throw new T_TransException("No such index for rs; %s", rsIdx);
		lst.set(rsIdx, total);
		return this;
	}
	
	public String error() {
		return (String) get("error");
	}
	
	public T_SemanticObject error(String error, Object... args) {
		if (args == null || args.length == 0)
			return put("error", error);
		else
			return put("error", String.format(error, args));
	}
	
	public T_SemanticObject put(String prop, Object v) {
		if (props == null)
			props = new HashMap<String, Object>();
		props.put(prop, v);
		return this;
	}

	/**Add element 'elem' to array 'prop'.
	 * @param prop
	 * @param elem
	 * @return this
	 * @throws T_TransException 
	 */
	@SuppressWarnings("unchecked")
	public T_SemanticObject add(String prop, Object elem) throws T_TransException {
		if (props == null)
			props = new HashMap<String, Object>();
		if (!props.containsKey(prop))
			props.put(prop, new ArrayList<Object>());
		if (props.get(prop) instanceof List)
			((ArrayList<Object>) props.get(prop)).add(elem);
		else throw new T_TransException("%s seams is not an array. elem %s can't been added", prop, elem);
		return this;
	}

	/**Add int array.
	 * @param prop
	 * @param ints
	 * @return this
	 * @throws T_TransException
	 */
	public T_SemanticObject addInts(String prop, int[] ints) throws T_TransException {
		for (int e : ints)
			add(prop, e);
		return this;
	}

	public Object remove(String prop) {
		if (props != null && props.containsKey(prop))
			return props.remove(prop);
		else return null;
	}

	/**Print for reading - string can't been converted back to object
	 * @param out
	 */
	public void print(PrintStream out) {
		if (props != null)
			for (String k : props.keySet()) {
				out.print(k);
				out.print(" : ");
				Class<?> c = getType(k);
				if (c == null)
					continue;
				else if (c.isAssignableFrom(T_SemanticObject.class)
					|| T_SemanticObject.class.isAssignableFrom(c))
					((T_SemanticObject)get(k)).print(out);
				else if (Collection.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c)) {
					Iterator<?> i = ((Collection<?>) get(k)).iterator(); 
					out.println("[" + ((Collection<?>) get(k)).size() + "]");
					while (i.hasNext()) {
						Object ele = i.next();
						c = ele.getClass();
						if (c.isAssignableFrom(T_SemanticObject.class)
								|| T_SemanticObject.class.isAssignableFrom(c))
							((T_SemanticObject)ele).print(out);
						else
							out.print(get(k));
					}
				}
				else out.print(get(k));
				out.print(",\t");
			}
		out.println("");
	}
}
