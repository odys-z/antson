package io.odysz.anson;

public class JsonOpt extends Anson {

	public boolean escape4DB = false;
	public JsonOpt escape4DB(boolean esc) {
		escape4DB = esc;
		return this;
	}

	public String doubleFormat;
	/**Must serialize key with quotes */
	private boolean quotKey = true;


	/**Must serialize key with quotes, default true */
	public boolean quotKey() {
		return quotKey;
	}

	public JsonOpt quotKey(boolean has) {
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
	public EnvelopeBuitifier beautifier;
}
