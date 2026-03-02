package io.odysz.anson;

import java.io.IOException;

public class AnsonException extends RuntimeException {
	public final static int general = 0;
	public final static int serial_syntx = 1;
	public final static int err_AST = 91;
	
	/** * */
	private static final long serialVersionUID = 1L;

	/**
	 *  0 : general internal<br>
	 *  1 : serializing syntax check error<br>
	 *  91: AST error<br>
	 *  ...
	 */
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
	 * @return {@link #c}<br>
	 */
	public int code() { return c; }
}
