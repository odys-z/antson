package io.odysz.semantic.jprotocol.test;

import io.odysz.common.LangExt;

public class T_TransException extends Exception {
	private static final long serialVersionUID = 1L;

	public T_TransException(String format, Object... args) {
		super(LangExt.isblank(format) ? null
			: args != null && args.length > 0 ?
					String.format(format, args) : format);
	}
}
