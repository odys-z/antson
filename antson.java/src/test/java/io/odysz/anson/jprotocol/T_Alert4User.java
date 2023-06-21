package io.odysz.anson.jprotocol;

import java.util.ArrayList;
import java.util.List;

import io.odysz.anson.Anson;
import io.odysz.anson.x.AnsonException;
import io.odysz.semantic.jprotocol.test.T_AnsonBody;
import io.odysz.semantic.jprotocol.test.T_AnsonHeader;
import io.odysz.semantic.jprotocol.test.T_IPort;
import io.odysz.semantic.jprotocol.test.T_AnsonMsg.MsgCode;
import io.odysz.semantic.jprotocol.test.T_AnsonMsg.T_Port;

/**<p>A mimic of AnsonMsg for testing error prone issue alarm</p>
 * 1. declare field of IJsonable instead of enum when the type is implemented with enum,
 * see {@link #port};
 * 
 * @author odys-z@github.com
 */
public class T_Alert4User <T extends T_AnsonBody> extends Anson {

	static T_IPort defaultPortImpl;

	int seq;
	public int seq() { return seq; }

	T_IPort port;
	public T_IPort port() { return port; }

	private MsgCode code;
	public MsgCode code() { return code; }

	public void port(String pport) throws AnsonException {
		if (defaultPortImpl == null)
			port = T_Port.echo.valof(pport);

		port = defaultPortImpl.valof(pport);

		if (port == null)
			throw new AnsonException(-1, "Port can not be null. Not initialized? To use JMassage understand ports, call understandPorts(IPort) first.");
	}

	public T_Alert4User() {
		seq = (int) (Math.random() * 1000);
	}

	public T_Alert4User(T_Port port) {
		this.port = port;
		seq = (int) (Math.random() * 1000);
	}

	/**Typically for response
	 * @param p 
	 * @param code
	 */
	public T_Alert4User(T_Port p, MsgCode code) {
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
	public T_Alert4User<T> body(T_AnsonBody bodyItem) {
		if (body == null)
			body = new ArrayList<T>();
		body.add((T)bodyItem);
		// bodyItem.parent = this;
		return this;
	}

	public T_Alert4User<T> incSeq() {
		seq++;
		return this;
	}
	
	T_AnsonHeader header;
	public T_AnsonHeader header() { return header; }
	public T_Alert4User<T> header(T_AnsonHeader header) {
		this.header = header;
		return this;
	}
	
	public T_Alert4User<T> body(List<T> bodyItems) {
		this.body = bodyItems;
		return this;
	}
}
