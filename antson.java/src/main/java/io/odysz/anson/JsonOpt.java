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
}
