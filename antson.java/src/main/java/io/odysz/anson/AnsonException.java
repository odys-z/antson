package io.odysz.anson;

import java.io.IOException;

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

	public AnsonException(IOException e) {
		this(0, e == null ? null : e.getMessage());
		setStackTrace(e.getStackTrace());
	}

	/**
	 * @return code<br>
	 *  0: general internal<br>
	 *  1: serializing syntax check error<br>
	 */
	public int code() { return c; }
}
