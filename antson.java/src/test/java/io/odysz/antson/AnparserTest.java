package io.odysz.antson;


import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Before;
import org.junit.Test;

import gen.antlr.json.JSONLexer;
import gen.antlr.json.JSONParser;

@SuppressWarnings("deprecation")
class AnparserTest {
	@Before
	static void init() {
		Utils.printCaller(false);
	}

	@Test
	void test() {
		
		String json = "{\"a\":\"1\", \"b\":[{\"a\":\"x\"}]}";
		ANTLRInputStream inputStream = new ANTLRInputStream(json);
        JSONLexer lex = new JSONLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lex);
        JSONParser anparser = new JSONParser(commonTokenStream);

        // Anparser classListener = new Anparser();
		Anlistner lsner = new Anlistner();
        anparser.json().enterRule(lsner);
        Object obj = lsner.getValue();
        
        Utils.logi(json);
        Utils.logi(obj.toString());
        
        Utils.logi(((Ason)obj).json());
	}
	
	@Test
	void tryJson() {
		Ason1 e1 = new Ason1();
		e1.a = "e1-prop";
		e1.b = new Ason2[] { };
		Ason1 e2 = new Ason1();
		e2.a = "e2-prop";
		Ason1 e3 = new Ason1();
		e3.a = "e3-prop";
		Ason2 asn2 = new Ason2();
		e3.b = new Ason2[] {null, asn2};

		Ason[] a = new Ason[] { e1, e2, e3 };
		
		String s = Arrays.stream((Ason[])a).map(e -> e.json()).collect(Collectors.joining(";\n"));
		Utils.logi(s);
	}
	
	@Test
	void testTj() {
		Ason1 ason1 = new Ason1();
		ason1.a = "A";
		String s = Ant.json(ason1);
		Utils.logi(s);

		s = Ant.json(new Ason[] {});
		Utils.logi(s);
		
		s = Ant.json(new Ason1[] {});
		Utils.logi(s);

		Ason2 anson2 = new Ason2();
		s = Ant.json(new Ason[] {anson2});
		Utils.logi(s);

		ason1.b = new Ason2[] {anson2};
		s = Ant.json(new Ason[] {ason1, ason1});
		Utils.logi(s);
		
		// { 
		//   types: [
		//		{io.odysz.antson.AnparserTest$Ason3: { addr: Address, b: [io.odysz.antson.AnparserTest$Ason2] }},
		//		{io.odysz.antson.AnparserTest$Ason3$Address: { country: String, province: String, zip: String }},
		//		{io.odysz.antson.AnparserTest$Ason2: { a: int, name: String} }
		//   ],
		//   data: {type: io.odysz.antson.AnparserTest$Ason3,
		//          addr: {type: io.odysz.antson.AnparserTest$Ason3$Address,
		//                 contry:, province:, zip:},
		//          orders: {type: java.util.Map<String, Order>,  // we can get it from type signature
		//                   array: []},
		//          this$0: {type: io.odysz.antson.AnparserTest$Ason2,
		//                   c: 0, name: String
		//                   this$0: {type: io.odysz.antson.AnparserTest$Ason,
		//                            a: , b: {type: [ASON],
		//                                     array:[]}}
		//                  }
		//         }
		// }
		Ason3 ason3 = new Ason3();
		
		// {...
		// data: [
		//  { this$type: io.odysz.antson.AnparserTest$Ason2, c, name, this$0: {...}},
		//  { this$type: io.odysz.antson.AnparserTest$Ason3, addr, ..., this$0: {c, name, this$0: ...}}}
		// ] }
		Ason[] a3 = new Ason[] { anson2, ason3 };
	}

	/**types: [
	 *  {io.odysz.antson.AnparserTest$Ason1: { a: int, b: [io.odysz.antson.AnparserTest$Ason2]}},
	 *  {io.odysz.antson.AnparserTest$Ason2: { a: int, name: String}}
	 * ]
	 * 
	 * @author ody
	 *
	 */
	class Ason1 extends Ason {
		String a;
		Ason2[] b;
	}
	
	/**types: [{io.odysz.antson.AnparserTest$Ason2: { a: int, name: String}}]
	 * @author ody
	 *
	 */
	class Ason2 extends Ason {
		int c;
		String name;
	}
	
	/**types: [
	 *  {io.odysz.antson.AnparserTest$Ason3: { addr: Address, b: [io.odysz.antson.AnparserTest$Ason2] }},
	 *  {io.odysz.antson.AnparserTest$Ason3$Address: { country: String, province: String, zip: String }},
	 *  {io.odysz.antson.AnparserTest$Ason2: { a: int, name: String} }
	 * ]
	 * @author ody
	 *
	 */
	class Ason3 <U extends Ason> extends Ason2 {
		Address addr;
		HashMap<String, Order> orders = new LinkedHashMap<String, Order>();
		ArrayList<Ason1> lst;
		U extAson;
	
		class Address {
			String contry;
			String province; // spell check
			String zip;
		}
		
		class Order {
			String orderId;
			float amount;
		}
	}
	
	/**Find out various information that can get with reflection.<pre>
addr, io.odysz.antson.AnparserTest$Ason3, io.odysz.antson.AnparserTest$Ason3$Address, 	| null
	java.lang.Object
orders, io.odysz.antson.AnparserTest$Ason3, java.util.HashMap, 	| Ljava/util/HashMap<Ljava/lang/String;*>;
	java.util.AbstractMap
lst, io.odysz.antson.AnparserTest$Ason3, java.util.ArrayList, 	| Ljava/util/ArrayList<Lio/odysz/antson/AnparserTest$Ason1;>;
	java.util.AbstractList
this$0, io.odysz.antson.AnparserTest$Ason3, io.odysz.antson.AnparserTest, 	| null
	java.lang.Object
c, io.odysz.antson.AnparserTest$Ason2, int, 	| null
	null
name, io.odysz.antson.AnparserTest$Ason2, java.lang.String, 	| null
	java.lang.Object
this$0, io.odysz.antson.AnparserTest$Ason2, io.odysz.antson.AnparserTest, 	| null
	java.lang.Object</pre>
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	@Test
	void temp () throws NoSuchFieldException, SecurityException {
		Ason3<Ason1> a3 = new Ason3<Ason1>();

		Field sf = Field.class.getDeclaredField("signature");
		sf.setAccessible(true);
		Field [] flist = a3.getClass().getDeclaredFields();

		for (Field f : flist) {
			String n = f.getName();
			String ofCls = f.getDeclaringClass().getName();
			String ft = f.getType().getName();
			String sper = f.getType().getSuperclass().getName();
			String s = null;
			String gt = null;
			try {
				s = (String) sf.get(f);
				if (s != null && s.startsWith("T")) {
					// gt = ((f.getGenericType()).getClass()).getTypeName();
					gt = getDeclaration(f.getType());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			Utils.logi("%s, %s,\n\tfield type: %s\t%s,\n\tgeneric type: %s\n\tsuper type: %s", n, ofCls,
					ft, s,
					gt,
					sper);
		}
		
		
		Class<?> scls = a3.getClass().getSuperclass();
		flist = a3.getClass().getSuperclass().getDeclaredFields();

		for (Field f : flist) {
			String n = f.getName();
			String ofCls = f.getDeclaringClass().getName();
			String ft = f.getType().getName();

			String sper = null;
			String s = null;
			String gt = null;
			try {
				if (f.getType().getSuperclass() != null)
					sper = f.getType().getSuperclass().getName();
				s = (String) sf.get(f);
				if (s != null && s.startsWith("T"))
					gt = getDeclaration(f.getType());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			Utils.logi("%s, %s,\n\tfield type: %s\t%s,\n\tgeneric type: %s\n\tsuper type: %s", n, ofCls,
					ft, s,
					gt,
					sper);
		}
	
	}
	

	private static String getDeclaration(Type genericType) {
		/*
		if (genericType instanceof ParameterizedType) {
		    ParameterizedType ptype = (ParameterizedType) genericType;
		    ptype.getRawType();
		    return String.format("-raw type: %s, -type arg: ", ptype.getRawType(), ptype.getActualTypeArguments()[0]);
		}
		else return null;
	    */
		if(genericType instanceof ParameterizedType) {
			// types with parameters
			ParameterizedType parameterizedType = (ParameterizedType) genericType;
			String declaration = parameterizedType.getRawType().getTypeName();
			declaration += "<";
			
			Type[] typeArgs = parameterizedType.getActualTypeArguments();
			for(int i = 0; i < typeArgs.length; i++) {
				Type typeArg = typeArgs[i];
				if(i > 0) {
					declaration += ", ";
				}
				// note: recursive call
				declaration += getDeclaration(typeArg);
			}
			
			declaration += ">";
			declaration = declaration.replace('$', '.');
			return declaration;
		}
		else if(genericType instanceof Class<?>) {
			Class<?> clazz = (Class<?>) genericType;
			
			if(clazz.isArray()) {
				// arrays
				return clazz.getComponentType().getCanonicalName() + "[]";
			}
			else {
				// primitive and types without parameters (normal/standard types)
				// return clazz.getCanonicalName();
				return String.format("-type name: %s, -type arg: ", clazz.getTypeName()/*,  clazz.getTypeParameters()[0]*/);
			}
		}
		else {
			// e.g. WildcardTypeImpl (Class<? extends Integer>)
			return genericType.getTypeName();
		}
	}
}
