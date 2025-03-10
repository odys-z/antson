package io.odysz.anson;

import static io.odysz.common.LangExt._0;
import static io.odysz.common.LangExt.isNull;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import gen.antlr.json.JSONLexer;
import gen.antlr.json.JSONParser;
import gen.antlr.json.JSONParser.JsonContext;
import io.odysz.anson.x.AnsonException;
import io.odysz.common.Utils;

/**
 * Base class for any user's type that can be transmitted over JServ protocol to AnClient.
 * 
 * @author odys-z@github.com
 */
public class Anson implements IJsonable {
	public static boolean verbose;
	
	/**For debug, print, etc. The string can not been used for json data.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			toBlock(bos, new JsonOpt().shortenOnAnnoteRequired(true));
			return bos.toString(StandardCharsets.UTF_8.name());
		} catch (AnsonException | IOException e) {
			e.printStackTrace();
			return super.toString();
		}
	}

	public Anson() {}

	/**
	 * Serialize an envelope int string. 
	 * @since 0.9.41
	 * @param anson
	 * @param stream
	 * @param opts
	 * @return anson
	 * @throws IOException
	 */
	public static IJsonable toEnvelope(IJsonable anson, OutputStream stream, JsonOpt... opts) throws IOException {
		boolean quotK = opts == null || opts.length == 0 || opts[0] == null || opts[0].quotKey();
		if (quotK) {
			stream.write("{\"type\": \"".getBytes(StandardCharsets.UTF_8));
			stream.write(anson.getClass().getName().getBytes(StandardCharsets.UTF_8));
			stream.write('\"');
		}
		else {
			stream.write("{type: ".getBytes(StandardCharsets.UTF_8));
			stream.write(anson.getClass().getName().getBytes(StandardCharsets.UTF_8));
		}

		HashMap<String, Field> fmap = new HashMap<String, Field>();
		fmap = JSONAnsonListener.mergeFields(anson.getClass(), fmap);

		for (Field f : fmap.values()) {
			// is this ignored?
			AnsonField af = f.getAnnotation(AnsonField.class);
			if (af != null && af.ignoreTo())
				continue;

			f.setAccessible(true);

			stream.write(", ".getBytes(StandardCharsets.UTF_8));

			// prop
			if (quotK)
				stream.write(("\"" + f.getName() + "\": ").getBytes(StandardCharsets.UTF_8));
			else
				stream.write((f.getName() + ": ").getBytes(StandardCharsets.UTF_8));

			// value
			if (af != null && af.ref() == AnsonField.enclosing) {
				stream.write('\"');
				stream.write(f.getType().getName().getBytes(StandardCharsets.UTF_8));
				stream.write('\"');
				continue;
			}

			try {
				Object v = f.get(anson);
				AnsonField anno = f.getAnnotation(AnsonField.class);
				if (v != null && anno != null && anno.shortenString()
					&& opts != null && opts.length > 0 && opts[0] != null && opts[0].shortenOnAnnotation())
					stream.write(("\"Asnon field shortened ... \"").getBytes(StandardCharsets.UTF_8));
				else {
					if (!f.getType().isPrimitive()) {
						Class<? extends Object> vclz = v == null ? null : v.getClass();

						writeNonPrimitive(stream, vclz, v, isNull(opts) ? null : opts[0]);
					}
					if (f.getType() == char.class) {
						char c = f.getChar(anson);
						stream.write(c == 0 ? '0' : c);
					}
					else if (f.getType().isPrimitive()) {
						String str = String.valueOf(f.get(anson));
						if (!isNull(opts) && opts[0].escape4DB)
							str.replace("'", "''");
						stream.write(str.getBytes(StandardCharsets.UTF_8));
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				throw new AnsonException(0, e1.getMessage());
			}
		}
		stream.write('}');
		if (isNull(opts) || !opts[0].escape4DB)
			stream.write('\n');
		stream.flush();

		return anson;
	}

	/**
	 * Serialize Anson object.
	 * 
	 * <p>Debug Note, 1 Dec. 2021:</p>
	 * javadoc of byte[] java.lang.String.getBytes(StandardCharsets.UTF_8)():<br>
	 * Encodes this String into a sequence of bytes using the platform's default charset,
	 * storing the result into a new byte array.<br>
	 * So Windows change UTF8 to whatever it likes.
	 */
	@Override
	public Anson toBlock(OutputStream stream, JsonOpt... opts)
			throws AnsonException, IOException {
		return !isNull(opts) && _0(opts).beautifier != null
				? (Anson) EnvelopeBeautifier.toEnvelope(this, stream, _0(opts))
				: (Anson) toEnvelope(this, stream, opts);
	}
	
	public Anson toFile(String path, JsonOpt... opts) throws IOException {
		try (FileOutputStream os = new FileOutputStream(path)) {
			return toBlock(os, opts);
		}
	}

	private static void toArrayBlock(OutputStream stream, Object[] v, JsonOpt opt)
			throws AnsonException, IOException {
		if (v == null) return;

		boolean the1st = true;
		stream.write('[');
		Class<?> elemtype = v.getClass().getComponentType();
		for (Object o : v) {
			if (the1st) the1st = false;
			else stream.write(", ".getBytes(StandardCharsets.UTF_8));

			if (o == null)
				stream.write("null".getBytes(StandardCharsets.UTF_8));
			else if (IJsonable.class.isAssignableFrom(elemtype))
				((IJsonable)o).toBlock(stream, opt);
			else if (elemtype.isArray())
				toArrayBlock(stream, (Object[]) o, opt);

			else if (AbstractCollection.class.isAssignableFrom(elemtype))
				toCollectionBlock(stream, (AbstractCollection<?>) o, opt);

			else if (o instanceof String) {
				stream.write('"');
				stream.write(escape(o.toString(), opt));
				stream.write('"');
			}
			else stream.write(o.toString().getBytes(StandardCharsets.UTF_8));
		}
		stream.write(']');
	}

	private static void toPrimArrayBlock(OutputStream stream, Object v, JsonOpt opt) throws IOException {
		if (v == null) {
			stream.write("null".getBytes(StandardCharsets.UTF_8));
			return;
		}

		boolean the1st = true;
		stream.write('[');
		int length = Array.getLength(v);
		for (int i = 0; i < length; i ++) {
			Object o = Array.get(v, i);

			if (the1st)
				the1st = false;
			else stream.write(new byte[] {',', ' '});

			if (o == null)
				stream.write("null".getBytes(StandardCharsets.UTF_8));

			else if (o instanceof String) {
				stream.write('"');
				stream.write(escape(o.toString(), opt));
				stream.write('"');
			}
			else stream.write(o.toString().getBytes(StandardCharsets.UTF_8));
		}
		stream.write(']');
	}

	private static void toCollectionBlock(OutputStream stream, AbstractCollection<?> collect, JsonOpt opts)
			throws AnsonException, IOException {
		if (collect == null) return;

		boolean the1st = true;
		stream.write('[');
		for (Object o : collect) {
			if (the1st) the1st = false;
			else
				stream.write(", ".getBytes(StandardCharsets.UTF_8));

			Class<?> elemtype = o.getClass();
			writeNonPrimitive(stream, elemtype, o, opts);
		}
		stream.write(']');
	}

	public static void toMapBlock(OutputStream stream, Map<?, ?> map, JsonOpt opts)
			throws AnsonException, IOException {
		if (map == null) return;

		boolean quote = opts == null || opts.quotKey();

		boolean the1st = true;
		stream.write('{');
		for (Object k : map.keySet()) {
			if (the1st) the1st = false;
			else stream.write(new byte[] {',', ' '});

			if (quote)
				stream.write('\"');
			stream.write(escape(k.toString(), opts));
			if (quote)
				stream.write(new byte[] {'\"', ':', ' '});
			else
				stream.write(new byte[] {':', ' '});

			Object v = map.get(k);
			if (v != null) {
				Class<?> elemtype = v.getClass();
				writeNonPrimitive(stream, elemtype, v, opts);
			}
			else 
				writeNonPrimitive(stream, null, v, opts);
		}
		stream.write('}');
	}

	static void toListBlock(OutputStream stream, AbstractCollection<?> list, JsonOpt opt)
			throws AnsonException, IOException {
		stream.write('[');
		boolean is1st = true;
		for (Object e : list) {
			if (!is1st)
				stream.write(new byte[] {',', ' '});
			else
				is1st = false;

			if (e == null) {
				stream.write("null".getBytes(StandardCharsets.UTF_8));
				continue;
			}

			if (!e.getClass().isPrimitive())
				writeNonPrimitive(stream, e.getClass(), e, opt);
			else // if (f.getType().isPrimitive())
				// must be primitive?
				stream.write(String.valueOf(e).getBytes(StandardCharsets.UTF_8));

		}
		stream.write(']');
	}

	@Override
	public IJsonable toJson(StringBuffer sbuf) throws IOException, AnsonException {
		return toJson(sbuf, this);
	}
	
	/**
	 * @since 0.9.41
	 * @param sbuf
	 * @param anson
	 * @return JSON string
	 * @throws AnsonException
	 * @throws IOException
	 */
	public static IJsonable toJson(StringBuffer sbuf, IJsonable anson) throws AnsonException, IOException {
		sbuf.append("{");

		Field flist[] = anson.getClass().getDeclaredFields();
		Class<?> parentCls = anson.getClass().getDeclaringClass();
		for (int i = 0; i < flist.length; i++) {
			Field f = flist[i];
			f.setAccessible(true);
			try {
				appendPair(sbuf, f.getName(), f.get(anson), parentCls, null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new AnsonException(0, e.getMessage());
			}

			if (i < flist.length - 1)
				sbuf.append(",");
		}

		sbuf.append("}");
		
		return anson;
	}

	/**
	 * Write field (element)'s value to stream.<br>
	 * The field type (fdClz) is not always the same as value's type.
	 * When field is an array, collection, etc., they are different.
	 * @param stream
	 * @param fdName
	 * @param fdClz
	 * @param v
	 * @throws AnsonException
	 * @throws IOException
	 */
	private static void writeNonPrimitive(OutputStream stream,
			Class<? extends Object> fdClz, Object v, JsonOpt opts)
			throws AnsonException, IOException {
		if (v == null) {
			stream.write("null".getBytes(StandardCharsets.UTF_8));
			return;
		}

		Class<? extends Object> vclz = v.getClass();
		if (IJsonable.class.isAssignableFrom(vclz)) {
			((IJsonable)v).toBlock(stream, opts);
		}
		else if (List.class.isAssignableFrom(v.getClass()))
			toListBlock(stream, (AbstractCollection<?>) v, opts);
		else if ( Map.class.isAssignableFrom(vclz))
			toMapBlock(stream, (Map<?, ?>) v, opts);
		else if (AbstractCollection.class.isAssignableFrom(vclz))
			toCollectionBlock(stream, (AbstractCollection<?>) v, opts);
		else if (fdClz.isArray()) {
			if (v != null && v.getClass().getComponentType() != null
				&& v.getClass().getComponentType().isPrimitive() == true)
				toPrimArrayBlock(stream, v, opts);
			else
				toArrayBlock(stream, (Object[]) v, opts);
		}
		else if (v instanceof String) {
			stream.write('\"');
			stream.write(escape(v, opts));
			stream.write('\"');
		}
		else if (fdClz.isEnum())
			stream.write(("\"" + ((Enum<?>)v).name() + "\"").getBytes(StandardCharsets.UTF_8));
		else if (v instanceof Number)
			stream.write(v.toString().getBytes());
		else
		{
			if (verbose)
				Utils.warn("Don't know how to serialize object.\n\ttype: %s\n\tvalue: %s", vclz.getName(), v.toString());
			try { stream.write(v.toString().getBytes(StandardCharsets.UTF_8)); }
			catch (NotSerializableException e) {
				throw new AnsonException(0, "A filed of type %s can't been serialized: %s",
						vclz.getName(), e.getMessage());
			}
		}
	}

	/**
	 * If opts.escape4DB = true, scape "'" as "''" first, then
	 * escape according to JSON escape:
	 * <pre>fragment ESC
     : '\\' (["\\/bfnrt] | UNICODE) ;</pre>
     * 
     * See <a href='https://www.json.org/json-en.html'>JSON Introduction</a>
     * 
     * TODO using stream as output
     * 
	 * @param v
	 * @param opts use opts.escape_singlquot = true for db writing.
	 * @return escaped bytes
	 * @since 0.9.43
	 * @since 0.9.55 This method scan through the string an will create a new for return 
	 */
	public static byte[] escape(Object v, JsonOpt... opts) {
		if (v == null)
			return new byte[0];
		String s = v.toString();

		/*
		 * NOTE v1.3.0 25 Aug 2021 - Doc Task # 001
		 * v1.3.0 NOTE 19 Aug 2021
		 * - why escape is disabled?
		 * What's needed currently:
		 * A DB varchar field is stored with value "{\"msg\": \"hello\"}",
		 * which is a field in an Anson object. 
		
		 * NOTE 24 Aug 2021
		 * The problem is DB saved both escaped and non-escaped string, e.g. v =
		 * that.alert(L(\"Quiz saved!\\n\\nQuestions number: {questions}\", {questions}));
		 * where '\' and '"' are separate characters.
		 * because "{\"msg\": \"hello\"}" is generated by code,
		 * but this is uploaded via json requests:
		 * that.alert(L(\"Quiz saved!\\n\\nQuestions number: {questions}\", {questions}));
		 *
		return s
				.replace("\n", "\\n")  // '?' -> '\', 'n'
				.replace("\r", "\\r")  // '?' -> '\', 'r'
				.replace("\t", "\\t")  // '?' -> '\', 't'
				.replace("\"", "\\\"") // '?' -> '\', '"'
				.getBytes(StandardCharsets.UTF_8);
		 */
		StringBuffer b = new StringBuffer();
		s.chars()
		 .forEachOrdered(c -> {
			if      (c == '\n')
				b.append("\\n");

			else if (c == '\t')
				b.append("\\t");

			else if (c == '\r')
				b.append("\\r");

			else if (c == '\"')
				b.append("\\\"");

			else if (c == '\\')
				b.append("\\\\");
			else if ( !isNull(opts) && opts[0].escape4DB && c == '\'')
				b.append("\'\'");
			else b.append((char)c);
		});
		return b.toString().getBytes();
	}
	
	/**
	 * NOTE v1.3.0 25 Aug 2021 - Doc Task # 001
	 * When client upload json, it's automatically escaped.
	 * This makes DB (or server stored data) are mixed with escaped and un-escaped strings.
	 * When a json string is parsed, we unescape it for the initial value (and escape it when send back - toBlock() is called)
	 * The following is experimental to keep server side data be consists with raw data.
	 *  
	 * befor change:
	 * top.parsedVal = getStringVal(ctx.STRING(), ctx.getText());
	 * @since 0.9.55 This method scan through the string an will create a new for return 
	 * @param v
	 * @return unescaped string
	 */
	public static String unescape(String v) {
		/*
		return v == null ? null
		 : v.replace("\\\\", "\\")
		 	.replace("\\n", "\n")
			.replace("\\r", "\r")
			.replace("\\t", "\t")
			.replace("\\\"", "\"")
			;
		*/
		if (v == null) return null;
		StringBuffer b = new StringBuffer();
		boolean[] prebacklash = new boolean[] {false};
		v.chars()
		 .forEachOrdered(c -> {
			if (prebacklash[0]) {
				b.append( (char)c == 't' ? '\t'
						: (char)c == 'n' ? '\n'
						: (char)c == 'r' ? '\r'
						: (char)c );
				prebacklash[0] = false;
			}
			else {
				if (c == '\\')
					prebacklash[0] = true;
				else b.append((char)c);
			}
		 });
		if (prebacklash[0])
			Utils.warn("There are unpaired '\\' in string:\n%s", v);
		return b.toString();
	}

	public static void appendPair(StringBuffer sbuf, String n, Object v, Class<?> parentCls, JsonOpt opt)
			throws IOException, AnsonException {
		boolean forceQuote = opt == null || opt.quotKey();
		if (v instanceof IJsonable)
			((Anson)v).toJson(sbuf);
		else if (!v.getClass().isPrimitive() && (parentCls == null || !parentCls.equals(v.getClass())))
			sbuf.append( forceQuote ? "\"" + n + "\"" : n)
				.append(": \"")
				.append(v.toString())
				.append("\"");
		else if (v.getClass().isPrimitive())
			sbuf.append( forceQuote ? "\"" + n + "\"" : n)
				.append(": ")
				.append(String.valueOf(v));
		else if (v.getClass().isArray()) {
			for (int e = 0; e < Array.getLength(v); e++) {
				Object elem = Array.get(v, e);
				sbuf.append( forceQuote ? "\"" + n + "\"" : n)
					.append(": [");
				appendArr(sbuf, elem, opt);
				if (e < Array.getLength(v) - 1)
					sbuf.append(", ");
			}
		}
	}

	public static void appendArr(StringBuffer sbuf, Object e, JsonOpt opts)
			throws AnsonException, IOException {
		if (e instanceof Anson)
			((IJsonable)e).toJson(sbuf);
		else if (e.getClass().isPrimitive())
			sbuf.append(String.valueOf(e));
		else if (e instanceof String)
			sbuf.append("\"")
				.append(escape((String)e, opts))
				.append("\"");
		else
			appendObjStr(sbuf, e);
	}

	public static void appendObjStr(StringBuffer sbuf, Object e) {
		sbuf.append("{")
			.append("type: \"")
			.append(e.getClass().getName())
			.append("\", ")
			.append(String.valueOf(e))
			.append("}");
	}

	/**
	 * @deprecated After reading some docs about Kafka like <a href='https://kafka.apache.org/intro.html'>
	 * kafka.Apache.org</a>, thinking may be it's better let this been handlered
	 * by Kafka, in any style as users liked.
	 * @param stream
	 * @return this
	 * @throws IOException
	 */
	protected Anson fromBlock(InputStream stream) throws IOException {
		byte[] buf = new byte[2048];
		int len = stream.read(buf);
		while (len != -1) {
			// kafka?
		}

		return this;
	}

	/**
	 * Parse Anson object from json string.
	 * <p><b>Note: </b><br>
	 * As LL(*) parsing like Antlr won't work in stream mode,
	 * this method won't have a input stream version.</p>
	 * @param json
	 * @return new instance
	 * @throws IllegalArgumentException
	 * @throws ReflectiveOperationException
	 */
	public static IJsonable fromJson(String json)
			throws AnsonException {
		return parse(CharStreams.fromString(json));
	}

	public static IJsonable fromJson(InputStream is)
			throws IOException, AnsonException {
		return parse(CharStreams.fromStream(is));
	}

	private static IJsonable parse(CharStream ins)
			throws AnsonException {

		JSONLexer lexer = new JSONLexer(ins);

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JSONParser parser = new JSONParser(tokens);
		JsonContext ctx = parser.json();
		ParseTreeWalker walker = new ParseTreeWalker();
		JSONAnsonListener lstner = new JSONAnsonListener();

		if (verbose) {
			Utils.logi("Anson.verbose - ctx.getText():");
			Utils.logi(ctx.getText());
		}

		walker.walk(lstner, ctx);
		return lstner.parsedEnvelope(verbose);
	}
}
