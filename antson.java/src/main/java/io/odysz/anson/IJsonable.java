package io.odysz.anson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public interface IJsonable {

	/**
	 * <h4>This interface is planned to be deprecated in the future.</h4>
	 * <p>Callback to create IJsonable instance from json string.</p>
	 * <p>Any class or enum implementing IJsonable must register it's
	 * factory to JSONAsonListener through {@link JSONAnsonListener#registFactory(Class, JsonableFactory)}.</p>
	 * <p>The AnsT4Enum.Port is a tested example for registering a java enum constructor:<pre>
	 public enum Port implements IPort { 
		heartbeat("ping.serv"), session("login.serv"), dataset("ds.serv");

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
	 * <p>TODO to be renamed as toEnvelope().</p>
	 * 
	 * {@link Anson} implemented this for almost all the case, user shouldn't care about this.
	 * 
	 * But typically, IPort implementation should handle this specially like
	 * <pre>
	 	stream.write('\"');
		stream.write(name().getBytes());
		stream.write('\"');
		return this;
	 </pre> 
	 * @param stream
	 * @param opts
	 * @return this
	 * @throws AnsonException
	 * @throws IOException
	 */
	IJsonable toBlock(OutputStream stream, JsonOpt... opts) throws AnsonException, IOException;

	/** @see #toBlock(OutputStream, JsonOpt...) */
	public default String toBlock(JsonOpt... opt) throws AnsonException, IOException {
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
