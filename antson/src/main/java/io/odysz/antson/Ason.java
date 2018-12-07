package io.odysz.antson;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**Base class of antson handled class.
 * @author ody
 *
 */
public class Ason {
	static class fmt {
		// static final String pairPrmv = "\"%s\": %s";
		static final String pairPrmv = "%s: %s";
		static final String pairStrv = "\"%s\": \"%s\"";
	}

	@Override
	public String toString() { return json(); }
	
	public String json() {
		Field flist[] = this.getClass().getDeclaredFields();
		return Stream.of(flist)
			.map(m -> {
				try {
					Class<?> t = m.getType();
					if (t.isPrimitive())
//						return String.valueOf(t);
						return String.format(fmt.pairPrmv, m.getName(), t);
					else if (t == String.class)
						// return (String)m.get(this);
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
					// else if (t.isAssignableFrom(Ason.class))
					//	return String.format(fmt.pairPrmv, m.getName(), ((Ason)m.get(this)).json()); 
					else if (Ason.class.isAssignableFrom(m.getDeclaringClass()))
						return String.format(fmt.pairPrmv, m.getName(), this.json()); 
					else return m.get(this).toString();
				} catch (IllegalArgumentException | IllegalAccessException e) {
					return String.format("EXCEPTION: \"%s\"", e.getMessage());
				}
			}).collect(Collectors.joining(", ", "{", "}"));
	}
}
