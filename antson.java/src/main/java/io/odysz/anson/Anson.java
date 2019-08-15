package io.odysz.anson;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import gen.antlr.json.JSONLexer;
import gen.antlr.json.JSONParser;
import gen.antlr.json.JSONParser.JsonContext;
import io.odysz.common.Utils;

public class Anson {
	private static final int bufLength = 64;

	protected String ver;
	protected int seq;

	protected Anson toBlock(OutputStream stream)
			throws IllegalArgumentException, ReflectiveOperationException, IOException {
		Field flist[] = this.getClass().getDeclaredFields();
		Class<?> parentCls = getClass().getDeclaringClass();
		for (Field f : flist) {
			f.setAccessible(true);
			if (!f.getType().isPrimitive() && (parentCls == null || !parentCls.equals(f.getType())))
				try { stream.write(f.get(this).toString().getBytes()); }
				catch (NotSerializableException e) {
					Utils.warn("Filed %s of %s can't been serialized.",
							f.getName(), f.getClass().getName());
				}
			else if (f.getType().isPrimitive())
				stream.write(String.valueOf(f.get(this)).getBytes());
		}
		stream.flush();
		return this;
	}
	
	protected Anson toJson(StringBuffer sbuf)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		sbuf.append("{");

		Field flist[] = this.getClass().getDeclaredFields();
		Class<?> parentCls = getClass().getDeclaringClass();
		for (int i = 0; i < flist.length; i++) {
			Field f = flist[i];
			f.setAccessible(true);
			appendPair(sbuf, f.getName(), f.get(this), parentCls);

			if (i < flist.length - 1)
				sbuf.append(",");
		}
		
		sbuf.append("}");
		return this;
	}

	private static void appendPair(StringBuffer sbuf, String n, Object v, Class<?> parentCls)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		if (v instanceof Anson)
			((Anson)v).toJson(sbuf);
		else if (!v.getClass().isPrimitive() && (parentCls == null || !parentCls.equals(v.getClass())))
			// what's this?
			sbuf.append(n)
				.append(": \"")
				.append(v.toString())
				.append("\"");
		else if (v.getClass().isPrimitive())
			sbuf.append(n)
				.append(": ")
				.append(String.valueOf(v));
		else if (v.getClass().isArray()) {
			for (int e = 0; e < Array.getLength(v); e++) {
				Object elem = Array.get(v, e);
				sbuf.append(n)
					.append(": [");
				appendArr(sbuf, elem);
				if (e < Array.getLength(v) - 1)
					sbuf.append(", ");
			}
		}
	}

	private static void appendArr(StringBuffer sbuf, Object e) 
			throws IllegalArgumentException, IllegalAccessException, IOException {
		if (e instanceof Anson)
			((Anson)e).toJson(sbuf);
		else if (e.getClass().isPrimitive())
			sbuf.append(String.valueOf(e));
		else if (e instanceof String)
			sbuf.append("\"")
				.append((String)e)
				.append("\"");
		else // java.lang.Object
			appendObjStr(sbuf, e);
	}

	private static void appendObjStr(StringBuffer sbuf, Object e) {
		sbuf.append("{")
			.append("type: \"")
			.append(e.getClass().getName())
			.append("\", ")
			.append(String.valueOf(e))
			.append("}");
	}

	/**@deprecated After reading some docs about Kafka like <a href='https://kafka.apache.org/intro.html'>
	 * kafka.Apache.org</a>, thinking may be it's better let this been handlered
	 * by Kafka, in any style as users liked.
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected Anson fromBlock(InputStream stream) throws IOException {
		byte[] buf = new byte[bufLength];
		int len = stream.read(buf);
		while (len != -1) {
			// kafka?
		}

		return this;
	}

	/**Parse Anson object from json string.
	 * <p><b>Note: </b><br>
	 * As LL(*) parsing like Antlr won't work in stream mode,
	 * this method won't have a input stream version.</p>
	 * @param json
	 * @return
	 * @throws IllegalArgumentException
	 * @throws ReflectiveOperationException
	 */
	public static Anson fromJson(String json)
			throws IllegalArgumentException, ReflectiveOperationException {
//		Field flist[] = this.getClass().getDeclaredFields();
//		Class<?> parentCls = getClass().getDeclaringClass();
		
		return parse(json);
	}
	
	public static Anson parse(String json)
			throws IllegalArgumentException, IllegalAccessException {
//		for (Field f : flist) {
//			f.setAccessible(true);
//			// prevent serialize parent class instance, which is not serializable.
//			if (!f.getType().isPrimitive() && (parentCls == null || !parentCls.equals(f.getType())))
//				f.set(obj, f.getName());
//			else if (f.getType().isPrimitive())
//				f.set(obj, 1);;
//		}
		
		JSONLexer lexer = new JSONLexer(CharStreams.fromString(json));

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JSONParser parser = new JSONParser(tokens);
		JsonContext ctx = parser.json();
		ParseTreeWalker walker = new ParseTreeWalker();
		JSONAnsonListener lstner = new JSONAnsonListener();
		walker.walk(lstner, ctx);
		return lstner.parsed();
	}
}
