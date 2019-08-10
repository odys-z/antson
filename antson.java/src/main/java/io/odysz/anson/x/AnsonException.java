package io.odysz.anson.x;

public class AnsonException extends Exception {
	/** * */
	private static final long serialVersionUID = 1L;

	private String c;

	private String ex;

	public AnsonException(String code, String template, Object... param) {
		this.c = code;
		
		if (template != null)
			this.ex = String.format(template, param);
	}
}
