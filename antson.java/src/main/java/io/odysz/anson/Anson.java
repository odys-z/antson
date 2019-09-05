package io.odysz.anson;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import gen.antlr.json.JSONLexer;
import gen.antlr.json.JSONParser;
import gen.antlr.json.JSONParser.JsonContext;
import io.odysz.anson.x.AnsonException;
import io.odysz.common.Utils;

public class Anson implements IJsonable {
	private static final int bufLength = 64;

	protected String ver;
	protected int seq;
	
	public Anson() {}

	@Override
	public Anson toBlock(OutputStream stream)
			throws AnsonException, IOException {
		stream.write("{type: ".getBytes());
		stream.write((getClass().getName() + ", ").getBytes());

		HashMap<String, Field> fmap = new HashMap<String, Field>();
		fmap = JSONAnsonListener.mergeFields(this.getClass(), fmap);

		boolean moreFields = false;
		for (Field f : fmap.values()) {
			f.setAccessible(true);

			if (moreFields)
				stream.write(new byte[] {',', ' '});
			else moreFields = true;

			stream.write((f.getName() + ": ").getBytes());

			try {
				if (!f.getType().isPrimitive()) {
					Object v = f.get(this);
					Class<? extends Object> vclz = v == null ? null : v.getClass();
					if (v == null)
						stream.write(new byte[] {'n', 'u', 'l', 'l'});
					else if (IJsonable.class.isAssignableFrom(vclz))
						((IJsonable)v).toBlock(stream);
//					else if (AbstractCollection.class.isAssignableFrom(v.getClass())) {
//						toCollectionBlock(stream, (AbstractCollection<?>) v);
//					}
					else if (List.class.isAssignableFrom(v.getClass())
						|| Map.class.isAssignableFrom(vclz)
						|| AbstractCollection.class.isAssignableFrom(vclz)) {
						toCollectionBlock(stream, (AbstractCollection<?>) v);
					}
					else if (f.getType().isArray()) {
						toArrayBlock(stream, (Object[]) v);
					}
					else if (v instanceof String)
						stream.write(("\"" + v.toString() + "\"").getBytes());
					else
						try { stream.write(v.toString().getBytes()); }
						catch (NotSerializableException e) {
							Utils.warn("Filed %s of %s can't been serialized.",
									f.getName(), f.getClass().getName());
						}
				}
				else if (f.getType().isPrimitive())
					stream.write(String.valueOf(f.get(this)).getBytes());
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				throw new AnsonException(0, e1.getMessage());
			}
		}
		stream.write("}".getBytes());
		stream.flush();
		return this;
	}
	
	private void toArrayBlock(OutputStream stream, Object[] v)
			throws AnsonException, IOException {
		if (v == null) return;

		boolean the1st = true;
		stream.write('[');
		Class<?> elemtype = v.getClass().getComponentType();
		for (Object o : v) {
			if (the1st) the1st = false;
			else stream.write(new byte[] {',', ' '});

			if (IJsonable.class.isAssignableFrom(elemtype))
				((IJsonable)o).toBlock(stream);
			else if (elemtype.isArray())
				toArrayBlock(stream, (Object[]) o);

			else if (AbstractCollection.class.isAssignableFrom(elemtype))
				toCollectionBlock(stream, (AbstractCollection<?>) o);

			else if (o instanceof String) {
				stream.write('"');
				stream.write(o.toString().getBytes());
				stream.write('"');
			}
			else stream.write(o.toString().getBytes());
		}
		stream.write(']');
	}

	private void toCollectionBlock(OutputStream stream, AbstractCollection<?> collect)
			throws AnsonException, IOException {
		stream.write('[');
		boolean is1st = true;
		for (Object e : collect) {
			if (!is1st)
				stream.write(new byte[] {',', ' '});
			else 
				is1st = false;

			if (IJsonable.class.isAssignableFrom(collect.getClass()))
				((Anson)e).toBlock(stream);
			else if (e instanceof String) {
				stream.write('"');
				stream.write(e.toString().getBytes());
				stream.write('"');
			}
			else 
				stream.write(e.toString().getBytes());
		}
		stream.write(']');
	}

	@Override
	public IJsonable toJson(StringBuffer sbuf) throws IOException, AnsonException {
		sbuf.append("{");

		Field flist[] = this.getClass().getDeclaredFields();
		Class<?> parentCls = getClass().getDeclaringClass();
		for (int i = 0; i < flist.length; i++) {
			Field f = flist[i];
			f.setAccessible(true);
			try {
				appendPair(sbuf, f.getName(), f.get(this), parentCls);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new AnsonException(0, e.getMessage());
			}

			if (i < flist.length - 1)
				sbuf.append(",");
		}
		
		sbuf.append("}");
		return this;
	}

	private static void appendPair(StringBuffer sbuf, String n, Object v, Class<?> parentCls)
			throws IOException, AnsonException {
		if (v instanceof IJsonable)
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
			throws AnsonException, IOException {
		if (e instanceof Anson)
			((IJsonable)e).toJson(sbuf);
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
	public static IJsonable fromJson(String json)
			throws IllegalArgumentException, ReflectiveOperationException {
		return parse(json);
	}
	
	private static IJsonable parse(String json)
			throws IllegalArgumentException, IllegalAccessException {
		JSONLexer lexer = new JSONLexer(CharStreams.fromString(json));

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JSONParser parser = new JSONParser(tokens);
		JsonContext ctx = parser.json();
		ParseTreeWalker walker = new ParseTreeWalker();
		JSONAnsonListener lstner = new JSONAnsonListener();
		walker.walk(lstner, ctx);
		return lstner.parsedEnvelope();
	}
}
