package io.odysz.anson;

import static io.odysz.common.LangExt.isNull;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.odysz.common.Utils;

/**
 * @since 0.9.116
 */
public class EnvelopeBeautifier {

	private static final byte[] br_mapf = new byte[] {',', '\n'};
	private static final byte[] sep_ele = new byte[] {',', ' '};
	private static final byte[] end_env = new byte[] {'\n', '}'};
	private static final byte[] qut_comma= new byte[] {'\"', ':', ' '};
	private static final byte   quot    = '"';
	private static final byte[] env_type= "{ \"type\": \"".getBytes(StandardCharsets.UTF_8);
	private static final byte[] s_null = "null".getBytes(StandardCharsets.UTF_8);

	/**
	 * Serialize an envelope int string. 
	 * @since 0.9.116
	 * @param anson
	 * @param stream
	 * @param opts
	 * @return anson
	 * @throws IOException
	 */
	public static IJsonable toEnvelope(IJsonable anson, OutputStream stream, JsonOpt opts) throws IOException {
		opts.indent();
		
		// stream.write("{ \"type\": \"".getBytes(StandardCharsets.UTF_8));
		stream.write(env_type);
		stream.write(anson.getClass().getName().getBytes(StandardCharsets.UTF_8));
		stream.write(quot);
		
		HashMap<String, Field> fmap = new HashMap<String, Field>();
		fmap = JSONAnsonListener.mergeFields(anson.getClass(), fmap);

		for (Field f : fmap.values()) {
			// is this ignored?
			AnsonField af = f.getAnnotation(AnsonField.class);
			if (af != null && af.ignoreTo())
				continue;

			f.setAccessible(true);

			// stream.write(",\n".getBytes(StandardCharsets.UTF_8));
			stream.write(br_mapf);

			// prop
			opts.indent(stream);
			stream.write(("\"" + f.getName() + "\": ").getBytes(StandardCharsets.UTF_8));

			// value
			if (af != null && af.ref() == AnsonField.enclosing) {
				stream.write(quot);
				stream.write(f.getType().getName().getBytes(StandardCharsets.UTF_8));
				stream.write(quot);
				continue;
			}

			try {
				Object v = f.get(anson);

				AnsonField anno = f.getAnnotation(AnsonField.class);
				if (v != null && anno != null && anno.shortenString()
					&& opts != null && opts.shortenOnAnnotation())
					stream.write(("\"shortened ... \"").getBytes(StandardCharsets.UTF_8));

				else {
					if (!f.getType().isPrimitive()) {
						Class<? extends Object> vclz = v == null ? null : v.getClass();

						writeNonPrimitive(stream, vclz, v, opts);
					}
					
					if (f.getType() == char.class) {
						char c = f.getChar(anson);
						stream.write(c == 0 ? '0' : c);
					}
					else if (f.getType().isPrimitive()) {
						String str = String.valueOf(f.get(anson));
						if (!isNull(opts) && opts.escape4DB)
							str.replace("'", "''");
						stream.write(str.getBytes(StandardCharsets.UTF_8));
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				throw new AnsonException(0, e1.getMessage());
			}
		}

		opts.undent();

		if (opts.indentWidth() == 0)
			stream.write(end_env);
		else
			stream.write('}');

		// TODO don't remove this unless this is verified against sqlite
		// if (isNull(opts) || !opts.escape4DB)
		// 	stream.write('\n');

		stream.flush();

		return anson;
	}
	
	private static void toArrayBlock(OutputStream stream, Object[] v, JsonOpt opt)
			throws AnsonException, IOException {
		if (v == null) return;

		boolean the1st = true;
		stream.write('[');

		Class<?> elemtype = v.getClass().getComponentType();
		for (Object o : v) {
			if (the1st) the1st = false;
			else // stream.write(", ".getBytes(StandardCharsets.UTF_8));
				stream.write(sep_ele);

			if (o == null)
				stream.write(s_null);
			else if (IJsonable.class.isAssignableFrom(elemtype))
				((IJsonable)o).toBlock(stream, opt);
			else if (elemtype.isArray())
				toArrayBlock(stream, (Object[]) o, opt);

			else if (AbstractCollection.class.isAssignableFrom(elemtype))
				toCollectionBlock(stream, (AbstractCollection<?>) o, opt);

			else if (o instanceof String) {
				stream.write(quot);
				stream.write(Anson.escape(o.toString(), opt));
				stream.write(quot);
			}
			else stream.write(o.toString().getBytes(StandardCharsets.UTF_8));
		}
		stream.write(']');
	}

	private static void toPrimArrayBlock(OutputStream stream, Object v, JsonOpt opt) throws IOException {
		if (v == null) {
			stream.write(s_null);
			return;
		}

		boolean the1st = true;
		stream.write('[');
		int length = Array.getLength(v);
		for (int i = 0; i < length; i ++) {
			Object o = Array.get(v, i);

			if (the1st)
				the1st = false;
			else stream.write(sep_ele);

			if (o == null)
				stream.write(s_null);

			else if (o instanceof String) {
				stream.write(quot);
				stream.write(Anson.escape(o.toString(), opt));
				stream.write(quot);
			}
			else stream.write(o.toString().getBytes(StandardCharsets.UTF_8));
		}
		stream.write(']');
	}

	private static void toCollectionBlock(OutputStream stream, AbstractCollection<?> collect, JsonOpt opts)
			throws AnsonException, IOException {
		if (collect == null) return;
		
		opts.indent();

		boolean the1st = true;
		stream.write('[');
		for (Object o : collect) {
			if (the1st) the1st = false;
			else
				// stream.write(",".getBytes(StandardCharsets.UTF_8));
				stream.write(sep_ele);

			Class<?> elemtype = o.getClass();
			writeNonPrimitive(stream, elemtype, o, opts);
		}
		stream.write(']');

		opts.undent();
	}

	public static void toMapBlock(OutputStream stream, Map<?, ?> map, JsonOpt opts)
			throws AnsonException, IOException {
		if (map == null) return;

		opts.indent();

		boolean the1st = true;
		stream.write('{');
		for (Object k : map.keySet()) {

			if (the1st) the1st = false;
			else {
				// stream.write(new byte[] {',', '\n'});
				 stream.write(br_mapf);
				opts.indent(stream);
			}

			stream.write(quot);
			stream.write(Anson.escape(k.toString(), opts));
			// stream.write(new byte[] {'\"', ':', ' '});
			stream.write(qut_comma);

			Object v = map.get(k);
			if (v != null) {
				Class<?> elemtype = v.getClass();
				writeNonPrimitive(stream, elemtype, v, opts);
			}
			else 
				writeNonPrimitive(stream, null, v, opts);
		}
		stream.write('}');
		
		opts.undent();
	}

//	private static void toListBlock(OutputStream stream, AbstractCollection<?> list, JsonOpt opt)
//			throws AnsonException, IOException {
//		stream.write('[');
//		boolean is1st = true;
//		for (Object e : list) {
//			if (!is1st)
//				stream.write(new byte[] {',', ' '});
//			else
//				is1st = false;
//
//			if (e == null) {
//				stream.write("null".getBytes(StandardCharsets.UTF_8));
//				continue;
//			}
//
//			if (!e.getClass().isPrimitive())
//				writeNonPrimitive(stream, e.getClass(), e, opt);
//			else // if (f.getType().isPrimitive())
//				// must be primitive?
//				stream.write(String.valueOf(e).getBytes(StandardCharsets.UTF_8));
//
//		}
//		stream.write(']');
//	}

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
		
		opts.indent();

		Class<? extends Object> vclz = v.getClass();
		if (IJsonable.class.isAssignableFrom(vclz))
			((IJsonable)v).toBlock(stream, opts);
		else if (List.class.isAssignableFrom(v.getClass()))
			Anson.toListBlock(stream, (AbstractCollection<?>) v, opts);
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
			stream.write(quot);
			stream.write(Anson.escape(v, opts));
			stream.write(quot);
		}
		else if (fdClz.isEnum())
			stream.write(("\"" + ((Enum<?>)v).name() + "\"").getBytes(StandardCharsets.UTF_8));
		else if (v instanceof Number)
			stream.write(v.toString().getBytes());
		else {
			if (Anson.verbose)
				Utils.warn("Don't know how to serialize object.\n\ttype: %s\n\tvalue: %s", vclz.getName(), v.toString());
			try { stream.write(v.toString().getBytes(StandardCharsets.UTF_8)); }
			catch (NotSerializableException e) {
				throw new AnsonException(0, "A filed of type %s can't been serialized: %s",
						vclz.getName(), e.getMessage());
			}
		}
		
		opts.undent();
	}
}
