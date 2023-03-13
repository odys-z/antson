package io.odysz.semantic.jprotocol.test;

import io.odysz.anson.IJsonable;
import io.odysz.anson.x.AnsonException;

 public interface IPort_Test extends IJsonable {

		default public String url() { return "echo.jserv"; }

		public String name();

		/**Equivalent of enum.valueOf(), except for subclass returning instance of jserv.Port.
		 * @throws SemanticException */
		public IPort_Test valof(String pname) throws AnsonException;
}
