package io.odysz.anson;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

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
			if (f.getType().isArray()) {
				// iterate
				Object arr = f.get(this);
				for (int e = 0; e < Array.getLength(arr); e++) {
					Object elem = Array.get(f.get(this), e);
					append(sbuf, elem);
					if (e < Array.getLength(arr) - 1)
						sbuf.append(", ");
				}
			}
			else if (!f.getType().isPrimitive() && (parentCls == null || !parentCls.equals(f.getType())))
				sbuf.append(f.getName())
						.append(": \"")
						.append(f.get(this).toString().getBytes())
						.append("\"");
			else if (f.getType().isPrimitive())
				sbuf.append(f.getName())
					.append(": ")
					.append(String.valueOf(f.get(this)).getBytes());

			if (i < flist.length - 1)
				sbuf.append(",");
		}
		
		sbuf.append("}");
		return this;
	}

	protected Anson fromBlock(InputStream stream) throws IOException {
		byte[] buf = new byte[bufLength];
		int len = stream.read(buf);
		// ...

		return this;
	}

	protected Anson fromJson(String json)
			throws IllegalArgumentException, ReflectiveOperationException {
		Field flist[] = this.getClass().getDeclaredFields();
		Class<?> parentCls = getClass().getDeclaringClass();
		
		parse(json, this, parentCls, flist);
		return this;
	}
	
	public static void parse(String json, Object obj, Class<?> parentCls, Field[] flist)
			throws IllegalArgumentException, IllegalAccessException {
		for (Field f : flist) {
			f.setAccessible(true);
			// prevent serialize parent class instance, which is not serializable.
			if (!f.getType().isPrimitive() && (parentCls == null || !parentCls.equals(f.getType())))
				f.set(obj, f.getName());
			else if (f.getType().isPrimitive())
				f.set(obj, 1);;
		}
	}
}
