package io.odysz.antson;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**@deprecated
 * Base class of antson handled class.
 * @author ody
 *
 */
public class Ason {
	static class fmt {
		static final String pairPrmv = "%s: %s";
		static final String pairStrv = "\"%s\": \"%s\"";
	}

	@Override
	public String toString() {
		return json();
	}
	
	public HashMap<String, Object> types(HashMap<String, Object> protos) {
		
		return protos;
	}

	public String type2(StringBuilder b) {
		return null;
	}

	
	public Ason type(StringBuilder b) {
		// { com.foo.typ1: { "f1": Object, "f2": [String], "f3": {com.foo.typ2: {...}}}}
		b.append("{");
		b.append(getClass().getName());
		b.append(": {");

		Field flist[] = this.getClass().getDeclaredFields();
		if (flist != null && flist.length > 0) {
			appendField(b, flist[0], true);
			Stream.of(flist).skip(1).forEach(f -> {
				appendField(b, f, false);
			});
			b.append("}");
		}
		return this;
	}
	
	private StringBuilder appendField(StringBuilder b, Field f, boolean isFirst) {
		
		if (f.getName().startsWith("this$"))
			return b;

		if (!isFirst) b.append(", ");

		b.append("\"")
		 .append(f.getName())
		 .append("\": ");
		
		// [String] | {com.foo.typ2: {...}} | Object
		Class<?> tp = f.getType();
		boolean isArray = false;
		if (tp.isArray()) {
			isArray = true;
			b.append("[");
			tp = tp.getComponentType();
		}
		if (Ason.class.isAssignableFrom(tp))
			// Ason | {com.foo.typ3: {...}}
			try {
				Ason obj;
				if (isArray) {
					Ason[] arr = (Ason[])f.get(this);
					if (arr == null || arr.length == 0)
						obj = null;
					else
						obj = arr[0];
				}
				else
					obj = (Ason)f.get(this);

				if (obj == null)
					b.append(tp.getName());
				else
					obj.type(b);
			}
			catch (IllegalArgumentException | IllegalAccessException e) { e.printStackTrace(); }
		else
			b.append(tp.getName().replaceFirst("^java\\.lang\\.", "").replaceFirst("java.\\.util\\.", ""));
		if (isArray)
			b.append("]");
		return b;
	}

	public String json() {
		Field flist[] = this.getClass().getDeclaredFields();
		return Stream.of(flist)
			.filter(m -> !m.getName().startsWith("this$"))
			.map(m -> {
				try {
					Class<?> t = m.getType();
					if (t.isPrimitive())
						return String.format(fmt.pairPrmv, m.getName(), t);
					else if (t == String.class)
						return String.format(fmt.pairPrmv, m.getName(), m.get(this));
					else if (t.isArray()) {
						return String.format(fmt.pairPrmv, m.getName(), 
							m == null || m.get(this) == null ? "" :
								Arrays.stream((Ason[])m.get(this))
									.map(e -> {
										try {
											return e == null ? "" : e.json();
										} catch (IllegalArgumentException e1) {
											e1.printStackTrace();
											return String.format("EXCEPTION:\"%\"", e1.getMessage());
										}
							}).collect(Collectors.joining(", ", "[", "]")));
					}
					else if (Ason.class.isAssignableFrom(m.getDeclaringClass())) {
						if (!m.getName().startsWith("this$"))
							return String.format(fmt.pairPrmv, m.getName(), ((Ason)m.get(this)).json());
//							return String.format(fmt.pairPrmv, m.getName());
						else return "";
//						if (!m.getName().equals("this$0"))
//							 return String.format(fmt.pairPrmv, m.getName(), this.jvalue()); 
////							return Stream.concat( Stream.of( m.getName(), ": "), this.json())); 
//						else return "";

//						return "";
					}
					else return m.get(this).toString();
				} catch (IllegalArgumentException | IllegalAccessException e) {
					return String.format("EXCEPTION: \"%s\"", e.getMessage());
				}
			}).collect(Collectors.joining(", ", "{", "}"));
	}

}
