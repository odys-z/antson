package io.odysz.semantic.jprotocol.test;

import io.odysz.anson.AnsonException;
import io.odysz.anson.IJsonable;

 public interface T_IPort extends IJsonable {

		default public String url() { return "echo.jserv"; }

		public String name();

		/**Equivalent of enum.valueOf(), except for subclass returning instance of jserv.Port.
		 * @throws SemanticException */
		public T_IPort valof(String pname) throws AnsonException;
}
