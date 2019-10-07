package io.odysz.anson;

import io.odysz.anson.Anson;
import io.odysz.anson.x.AnsonException;

public class AnsT4Enum extends Anson {
	public interface IPort {
			default public String url() { return "echo.jserv"; }

			public String name();

			/**Equivalent of enum.valueOf(), except for subclass returning instance of jserv.Port.
			 * @throws SemanticException */
			public IPort valof(String pname) throws AnsonException;
	}

	public enum Port implements IPort { 
		heartbeat("ping.serv11"), session("login.serv11"), dataset("ds.serv11");
		private String url;
		@Override public String url() { return url; }
		Port(String url) { this.url = url; }
		@Override public IPort valof(String pname) { return valueOf(pname); }
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

	public AnsT4Enum() { }
}
