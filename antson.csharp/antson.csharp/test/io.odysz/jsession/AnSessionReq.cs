using io.odysz.semantic.jprotocol;
using System.Collections.Generic;
using static io.odysz.semantic.jprotocol.AnsonMsg;

namespace io.odysz.semantic.jsession
{
	/// <summary>
	/// <p>Sessin Request<br />
	/// a: see
	/// <see cref="AnSession"/>
	/// </p>
	/// </summary>
	/// <author>odys-z@github.com</author>
	public class AnSessionReq : AnsonBody
	{
		string type;
		public AnSessionReq() : base(null, null)
		{
			type = "io.odysz.semantic.jsession.AnSessionReq";
		}

		/// <summary>Session connection is ignored and controlled by server.</summary>
		/// <param name="parent"/>
		public AnSessionReq(AnsonMsg parent) : base(parent, null)
		{
			type = "io.odysz.semantic.jsession.AnSessionReq";
		}

		internal string uid;

		public string token { get; protected set; }

		// session's DB access is controlled by server
		// internal virtual string token() { return token; }

		public string iv { get; protected set; }

		// internal virtual string iv() { return iv; }

		internal Dictionary<string, object> mds;

		public virtual string md(string k)
		{
			return mds == null ? null : (string)mds[k];
		}

		public virtual AnSessionReq md(string k, string md)
		{
			if (k == null || string.IsNullOrEmpty(md))
			{
				return this;
			}
			if (mds == null)
			{
				mds = new Dictionary<string, object>();
			}
			mds[k] = md;
			return this;
		}

		/// <summary>Format login request message.</summary>
		/// <param name="uid"/>
		/// <param name="tk64"/>
		/// <param name="iv64"/>
		/// <returns>login request message</returns>
		public static AnsonMsg formatLogin(string uid, string tk64, string iv64)
		{
			// AnSessionReq : AnsonBody
			AnsonMsg jmsg = new AnsonMsg(new Port(Port.session), null);
			AnSessionReq itm = new AnSessionReq(jmsg);
			itm.setup(uid, tk64, iv64);
			itm.A("login");

			jmsg.Body((AnsonBody)itm);
			return jmsg;
		}

		private void setup(string logid, string tk64, string iv64)
		{
			uid = logid;
			token = tk64;
			iv = iv64;
		}
	}
}
