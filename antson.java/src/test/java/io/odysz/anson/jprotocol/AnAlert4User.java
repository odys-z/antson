package io.odysz.anson.jprotocol;

import java.util.ArrayList;
import java.util.List;

import io.odysz.anson.Anson;
import io.odysz.anson.x.AnsonException;
import io.odysz.semantic.jprotocol.test.AnsonBody;
import io.odysz.semantic.jprotocol.test.AnsonHeader;
import io.odysz.semantic.jprotocol.test.IPort;
import io.odysz.semantic.jprotocol.test.Test_AnsonMsg.MsgCode;
import io.odysz.semantic.jprotocol.test.Test_AnsonMsg.Port;

/**<p>A mimic of AnsonMsg for testing error prone issue alarm</p>
 * 1. declare field of IJsonable instead of enum when the type is implemented with enum,
 * see {@link #port};
 * 
 * @author odys-z@github.com
 */
public class AnAlert4User <T extends AnsonBody> extends Anson {

	static IPort defaultPortImpl;

	int seq;
	public int seq() { return seq; }

	IPort port;
	public IPort port() { return port; }

	private MsgCode code;
	public MsgCode code() { return code; }

	public void port(String pport) throws AnsonException {
		if (defaultPortImpl == null)
			port = Port.echo.valof(pport);

		port = defaultPortImpl.valof(pport);

		if (port == null)
			throw new AnsonException(-1, "Port can not be null. Not initialized? To use JMassage understand ports, call understandPorts(IPort) first.");
	}

	public AnAlert4User() {
		seq = (int) (Math.random() * 1000);
	}

	public AnAlert4User(Port port) {
		this.port = port;
		seq = (int) (Math.random() * 1000);
	}

	/**Typically for response
	 * @param p 
	 * @param code
	 */
	public AnAlert4User(Port p, MsgCode code) {
		this.port = p;
		this.code = code;
	}
	
	protected List<T> body;
	public T body(int i) { return body.get(0); }
	public List<T> body() { return body; }

	/**Add a request body to the request list.
	 * @param bodyItem
	 * @return new message object
	 */
	@SuppressWarnings("unchecked")
	public AnAlert4User<T> body(AnsonBody bodyItem) {
		if (body == null)
			body = new ArrayList<T>();
		body.add((T)bodyItem);
		// bodyItem.parent = this;
		return this;
	}

	public AnAlert4User<T> incSeq() {
		seq++;
		return this;
	}
	
	AnsonHeader header;
	public AnsonHeader header() { return header; }
	public AnAlert4User<T> header(AnsonHeader header) {
		this.header = header;
		return this;
	}
	
	public AnAlert4User<T> body(List<T> bodyItems) {
		this.body = bodyItems;
		return this;
	}
}
