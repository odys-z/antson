package io.odysz.anson;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class JsonOpt extends Anson {

	public boolean escape4DB = false;

	public JsonOpt() {}
	
	public JsonOpt(EnvelopeBeautifier beautifier) {
		this.indent = new byte[0];
		this.beautifier = beautifier;
	}

	public JsonOpt escape4DB(boolean esc) {
		escape4DB = esc;
		return this;
	}

	public String doubleFormat;

	/**
	 * Must serialize key with quotes
	 * @deprecated
	 */
	private boolean quotKey = true;


	/**Must serialize key with quotes, default true */
	public boolean quotKey() {
		return quotKey;
	}

	/**
	 * @deprecated since 0.9.116
	 * @param has
	 * @return
	 * @throws NullPointerException if has == false, since 0.9.116, illegal argument.
	 */
	public JsonOpt quotKey(boolean has) throws NullPointerException {
//		if (!has)
//			throw new NullPointerException("Key field without a quot, '\"\"', is a js object, not a valid JSON string."
//					+ "This is deprecated since 0.9.116.");
		quotKey = has;
		return this;
	}

	private boolean shortenOnAnnotation;
	public boolean shortenOnAnnotation() { return shortenOnAnnotation; }
	
	/**Tell Anson shorten printing a string value if required with type annotation, both true. 
	 * @param yes
	 * @return this
	 */
	public JsonOpt shortenOnAnnoteRequired(boolean yes) {
		this.shortenOnAnnotation = yes;
		return this;
	}
	
	/** @since 0.1.115 */
	public EnvelopeBeautifier beautifier;

	public static JsonOpt beautify() {
		return new JsonOpt(new EnvelopeBeautifier());
	}

	
	////////////////////////////// indent //////////////////////////////
	private byte[] indent;

	/**
	 * @return 
	 * @since 0.9.116
	 */
	public JsonOpt indent() {
		indent = Arrays.copyOf(indent, indent.length + 2);
		int _1 = indent.length - 1;
		indent[_1] = ' ';
		indent[_1 - 1] = ' ';
		return this;
	}
	
	public int indentWidth() {
		return indent.length;
	}
	
	public JsonOpt undent() {
		indent = Arrays.copyOfRange(indent, 0, Math.max(indent.length - 2, 0));
		return this;
	}

	public void indent(OutputStream stream) throws IOException {
		stream.write(indent);
	}
}
