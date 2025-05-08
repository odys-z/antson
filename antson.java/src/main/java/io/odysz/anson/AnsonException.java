package io.odysz.anson;

public class AnsonException extends RuntimeException {
	/** * */
	private static final long serialVersionUID = 1L;

	private int c;

	/**
	 * @param code see {@link #code()}
	 * @param template
	 * @param param
	 */
	public AnsonException(int code, String template, Object... param) {
		super(template != null ? String.format(template, param) : "");
		this.c = code;
	}

	/**
	 * @return code<br>
	 *  0: general internal<br>
	 *  1: serializing syntax check error<br>
	 */
	public int code() { return c; }
}
