package io.odysz.semantic.jprotocol.test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.odysz.anson.Anson;
import io.odysz.anson.IJsonable;
import io.odysz.anson.JsonOpt;
import io.odysz.anson.x.AnsonException;

/**<p>Base class of message used by {@link io.odysz.semantic.jserv.ServHandler serv11}.</p>
 * 1. A incoming json message is parsed by *.serv into JMessage,
 * which can be used to directly to build statements;<br>
 * 2. An outgoing data object which is presented as AnsonMsg<AnsonResp>,
 * which should been directly write into output stream.
 * 
 * @author odys-z@github.com
 */
public class AnsonMsg <T extends AnsonBody> extends Anson {
	/**Port is the conceptual equivalent to the SOAP port, the service methods' group.<br>
	 * NOTE: java code shouldn't use switch-case block on enum. That cause problem with generated class.
	 * @author odys-z@github.com
	 */
	public enum Port implements IPort {  heartbeat("ping.serv11"), session("login-serv11"),
						query("r.serv11"), update("u.serv11"),
						insert("c.serv11"), delete("d.serv11"),
						echo("echo.serv11"),
						/** serv port for downloading json/xml file or uploading a file.<br>
						 * @see {@link io.odysz.semantic.jserv.file.JFileServ}. */
						file("file.serv11"),
						/**Any user defined request using message body of subclass of JBody must use this port */ 
						user("user.serv11"),
						/** semantic tree of dataset extensions<br>
						 * @see {@link io.odysz.semantic.ext.SemanticTree}. */
						stree("s-tree.serv11"),
						/** dataset extensions<br>
						 * @see {@link io.odysz.semantic.ext.Dataset}. */
						dataset("ds.serv11");
		
		private String url;
		@Override public String url() { return url; }
		Port(String url) { this.url = url; }
		@Override public IPort valof(String pname) { return valueOf(pname); }

		@Override
		public IJsonable toBlock(OutputStream stream, JsonOpt... opts) throws AnsonException, IOException {
			stream.write('\"');
			stream.write(name().getBytes());
			stream.write('\"');
			return this;
		}

		@Override
		public IJsonable toJson(StringBuffer buf) throws IOException, AnsonException {
			buf.append('\"');
			buf.append(name().getBytes());
			buf.append('\"');
			return this;
		}
	};

	public enum MsgCode {ok, exSession, exSemantic, exIo, exTransct, exDA, exGeneral, ext;
		public boolean eq(String code) {
			if (code == null) return false;
			MsgCode c = MsgCode.valueOf(MsgCode.class, code);
			return this == c;
		}
	};

	/**The default IPort implelemtation.
	 * Used for parsing port name (string) to IPort instance, like {@link #Port}.<br>
	 * */
	static IPort defaultPortImpl;

	/**Set the default IPort implelemtation, which is used for parsing port name (string)
	 * to IPort instance, like {@link AnsonMsg.Port}.<br>
	 * Because {{@link Port} only defined limited ports, user must initialize JMessage with {@link #understandPorts(IPort)}.<br>
	 * An example of how to use this is shown in jserv-sample/io.odysz.jsample.SysMenu.<br>
	 * Also check how to implement IPort extending {@link Port}, see example of jserv-sample/io.odysz.jsample.protocol.Samport.
	 * @param p extended Port
	 */
	static public void understandPorts(IPort p) {
		defaultPortImpl = p;
	}
	
	@SuppressWarnings("unused")
	private String vestion = "1.0";
	int seq;
	public int seq() { return seq; }

	Port port;
	public IPort port() { return port; }

	MsgCode code;
	public MsgCode code() { return code; }

	public void port(String pport) throws AnsonException {
		/// translate from string to enum
		if (defaultPortImpl == null)
			port = (Port) Port.echo.valof(pport);
		else
			port = (Port) defaultPortImpl.valof(pport);

		if (port == null)
			throw new AnsonException(-1, "Port can not be null. Not initialized? To use JMassage understand ports, call understandPorts(IPort) first.");
	}

	public AnsonMsg() {
		seq = (int) (Math.random() * 1000);
	}

	public AnsonMsg(Port port) {
		this.port = port;
		seq = (int) (Math.random() * 1000);
	}

	/**Typically for response
	 * @param p 
	 * @param code
	 */
	public AnsonMsg(Port p, MsgCode code) {
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
	public AnsonMsg<T> body(AnsonBody bodyItem) {
		if (body == null)
			body = new ArrayList<T>();
		body.add((T)bodyItem);
		bodyItem.parent = this;
		return this;
	}

	public AnsonMsg<T> incSeq() {
		seq++;
		return this;
	}
	
	AnsonHeader header;
	public AnsonHeader header() { return header; }
	public AnsonMsg<T> header(AnsonHeader header) {
		this.header = header;
		return this;
	}
	
	JsonOpt opts;
	public void opts(JsonOpt readOpts) { this.opts = readOpts; }
	public JsonOpt opts() {
		return opts == null ? new JsonOpt() : opts;
	}

	public AnsonMsg<T> body(List<T> bodyItems) {
		this.body = bodyItems;
		return this;
	}

	public static AnsonMsg<AnsonResp> ok(Port p, String txt) {
		AnsonResp bd = new AnsonResp(txt);
		return new AnsonMsg<AnsonResp>(p, MsgCode.ok).body(bd);
	}

	public static AnsonMsg<AnsonResp> ok(Port p, AnsonResp resp) {
		return new AnsonMsg<AnsonResp>(p, MsgCode.ok).body(resp);
	}
}
