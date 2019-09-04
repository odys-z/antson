package io.odysz.anson.x;

public class AnsonException extends Exception {
	/** * */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private int c;
//	public String code() { return c; }

	public AnsonException(int code, String template, Object... param) {
		super(template != null ? String.format(template, param) : "");
		this.c = code;
	}
}
