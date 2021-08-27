package io.odysz.anson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import io.odysz.anson.x.AnsonException;

public interface IJsonable {

	/**<p>Callback to create IJsonable instance from json string.</p>
	 * <p>Any class or enum implementing IJsonable must register it's
	 * factory to JSONAsonListener through {@link JSONAnsonListener#registFactory(Class, JsonableFactory)}.</p>
	 * <p>The AnsT4Enum.Port is a tested example for registering a java enum constructor:<pre>
	 public enum Port implements IPort { 
		heartbeat("ping.serv11"), session("login.serv11"), dataset("ds.serv11");

		// using static initialized is not always correct
		// - may be there are different implementation of IPort
		static {
			JSONAnsonListener.registFactory(IPort.class, (s) -> {
				return Port.valueOf(s);
			});
		}
	 }</pre>
	 * </p>
	 * @author odys-z@github.com
	 */
	public interface JsonableFactory {
		IJsonable fromJson(String json);
	}

	/**
	 * @param stream
	 * @param opts
	 * @return this
	 * @throws AnsonException
	 * @throws IOException
	 */
	IJsonable toBlock(OutputStream stream, JsonOpt... opts) throws AnsonException, IOException;

	public default String toBlock(JsonOpt opt) throws AnsonException, IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		toBlock(bos, opt);
		return bos.toString(StandardCharsets.UTF_8.name());
	}

	/**
	 * @param buf
	 * @return this
	 * @throws IOException
	 * @throws AnsonException
	 */
	IJsonable toJson(StringBuffer buf) throws IOException, AnsonException;
}
