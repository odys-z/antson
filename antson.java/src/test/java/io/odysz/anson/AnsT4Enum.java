package io.odysz.anson;

import java.io.IOException;
import java.io.OutputStream;

import io.odysz.anson.x.AnsonException;

public class AnsT4Enum extends Anson {
	public interface IPort extends IJsonable {
			default public String url() { return "echo.jserv"; }

			public String name();

			/**Equivalent of enum.valueOf(), except for subclass returning instance of jserv.Port.
			 * @throws SemanticException */
			public IPort valof(String pname) throws AnsonException;
	}

	public enum Port implements IPort { 
		heartbeat("ping.serv"), session("login.serv11"), dataset("ds.serv11");

		static {
			JSONAnsonListener.registFactory(IPort.class, (s) -> {
					return Port.valueOf(s);
			});
		}

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

	MsgCode c;
	Port p;

	IPort problem;

	public AnsT4Enum() { }
}
