package io.odysz.anson;

import java.io.IOException;
import java.io.OutputStream;

public class AnsT6Bypass extends Anson {
	static {
		JSONAnsonListener.registFactory(T6_Port.class, (s) -> {
				return new T6_Port(s);
		});
	}

	public static class T6_Port implements IJsonable { 
		static final int heartbeat = 1; // ("ping.serv"),
		static final int session = 2;   // ("login.serv11"),
		static final int dataset = 3;   // ("ds.serv11");

		private int port;
		public int port() { return port; }

		private String url;
		public String url() { return url; }

		T6_Port(String url) {
			this.url = url;
			port = valof(url);
		}

		public static int valof(String pname) {
			return "heartbeat".equals(pname) ? heartbeat
					: "session".equals(pname) ? session
					: dataset;
		}

		@Override
		public IJsonable toBlock(OutputStream stream, JsonOpt... opts) throws AnsonException, IOException {
			stream.write('\"');
			stream.write(url().getBytes());
			stream.write('\"');
			return this;
		}

		@Override
		public IJsonable toJson(StringBuffer buf) throws IOException, AnsonException {
			throw new AnsonException(0, "To keep consists with c#, this interface method will be replaced with constructor?");
		}
	};

	public enum MsgCode {ok, exSession, exSemantic, exIo, exTransct, exDA, exGeneral, ext;
		public boolean eq(String code) {
			if (code == null) return false;
			MsgCode c = MsgCode.valueOf(MsgCode.class, code);
			return this == c;
		}
	};

	T6_Port p;

	public AnsT6Bypass() {
		p = new T6_Port("session");
	}
}
