package io.odysz.anson.x;

public class AnsonException extends Exception {
	/** * */
	private static final long serialVersionUID = 1L;

	private String c;
	public String code() { return c; }

	// private String ex;

	public AnsonException(String code, String template, Object... param) {
		super(template != null ? String.format(template, param) : code);
		this.c = code;
	}
}
