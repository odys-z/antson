//package io.odysz.antson;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//public class Ant {
//
//	/*
//	public static <T extends Ason> String json (T[] t) {
//		// FIXME Performance problem: type() and json() should return streams.
//		// Why we can't purely depend on stream to build string?
//		// https://stackoverflow.com/questions/26158082/how-to-convert-a-tree-structure-in-a-stream-of-nodes-in-java
//		/*
//		String type = Stream.of("{type: \"[",
//						t.length > 0 ? t[0].type() : Ason.class.getName(),
//						"]\", data: [").collect(Collectors.joining(""));
//		StringBuilder b = new StringBuilder(type);
//		* /
//		StringBuilder b = new StringBuilder();
//		b.append("{type: [");
//
//		if (t != null && t.length > 0)
//			t[0].type(b);
//		else
//			// b.append(Ason.class.getName());
//			b.append(t.getClass().getComponentType().getName());
//
//		b.append("],\n data: [");
//
//		Stream<T> json = Arrays.stream(t);
//		if (t != null && t.length > 0) {
//			T first = t[0];
//			b.append(first.json());
//
//			json.skip(1).forEach(e -> {
//				b.append(", ").append(e.json());
//			});
//		}
//		return b.append("]}").toString();
//
//	}*/
//	
//	public static <T extends Ason> String json (T[] t) {
//		StringBuilder b = new StringBuilder();
//		b.append("{type: [");
//
//		b.append(t.getClass().getComponentType().getName());
//
//		b.append("],\n types: [");
//
//		HashMap<String, Object> types = new HashMap<String, Object>();
//		
////		b.append(types.entrySet().stream()
////			.map(e -> e.toString())
////			.collect(Collectors.joining(", ", "[", "]")));
//
//		types.entrySet().stream()
//			.forEach(e -> {
//				Class<?> cls = e.getClass();
//				String cn = cls.getName();
//				if (!types.containsKey(cn)) {
//					if (Ason.class.isAssignableFrom(cls)) {
//						// unknown / user type
//					}
//					else if (false)
//						;
//				}
////					e.type2(b);
//			});
//		
//		if (types.size() > 0) {
//			boolean isfirst = true;
//			for (String tp : types.keySet()) {
//				if (!isfirst)
//					b.append(", ");
//				Object typ = types.get(tp);
//				if (typ instanceof Ason)
//					((Ason)typ).type2(b);
//				else
//					// shouldn't reach here
//					b.append("{").append(typ).append(":").append(tp);
//			}
//			
//		}
//
//		b.append("],\n data: [");
//
//		// data
//		Stream<T> json = Arrays.stream(t);
//		if (t != null && t.length > 0) {
//			T first = t[0];
//			b.append(first.json());
//
//			json.skip(1).forEach(e -> {
//				b.append(", ").append(e.json());
//			});
//		}
//		return b.append("]}").toString();
//
//	}
//	
//	public static <T extends Ason> String json (T t) {
//		// FIXME Performance problem: type() and json() should return stream.
//		/*
//		return Stream.of(
//				"type: \"",
//				t.type(), "\"}, data: ",
//				t.json()).collect(Collectors.joining("", "{", "}"));
//				*/
//		StringBuilder b = new StringBuilder();
//		b.append("type: ");
//		t.type(b);
//		b.append("}, data: ");
//		b.append(t.json());
//		return b.toString();
//	}
//}
