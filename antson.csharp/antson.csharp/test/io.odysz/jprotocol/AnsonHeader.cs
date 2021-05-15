using io.odysz.anson;

namespace io.odysz.semantic.jprotocol
{
	public class AnsonHeader : Anson
	{
		internal string uid;

		// internal string ssid;
		public string ssid { get; }

		internal string iv64;

		internal string[] usrAct;
		public AnsonHeader UsrAct(string funcId, string cmd, string cate, string remarks = null)
		{
			usrAct = new string[] { funcId, cate, cmd, remarks };
			return this;
		}

		public AnsonHeader(string ssid, string uid)
		{
			this.uid = uid;
			this.ssid = ssid;
		}

		public AnsonHeader()
		{
		}

		public virtual string logid()
		{
			return uid;
		}

		/*
		/// <returns>js equivalent {md: ssinf.md, ssid: ssinf.ssid, uid: ssinf.uid, iv: ssinf.iv};
		///	</returns>
		public static AnsonHeader Format(string uid, string ssid)
		{
			return new AnsonHeader(ssid, uid);
		}
		*/

		public virtual AnsonHeader act(string[] act)
		{
			usrAct = act;
			return this;
		}

		/// <summary>For test.</summary>
		/// <remarks>For test. The string can not been used for json data.</remarks>
		/// <seealso cref="object.ToString()"/>
		public override string ToString()
		{
			return string.Format("{ssid: {0}, uid: {1}, iv64: {2},\n\t\tuserAct: {3}}",
					ssid, uid, iv64,
					usrAct == null ? "null" : "[" + string.Join(", ", usrAct)) + "]";
		}
	}
}
