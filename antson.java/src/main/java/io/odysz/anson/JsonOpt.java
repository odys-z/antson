package io.odysz.anson;

public class JsonOpt extends Anson {

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
}
