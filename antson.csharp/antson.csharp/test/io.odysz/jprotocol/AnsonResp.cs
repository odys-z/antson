using io.odysz.module.rs;
using System.Collections.Generic;

namespace io.odysz.semantic.jprotocol
{
	/// <summary>Anson message response body</summary>
	/// <author>odys-z@github.com</author>
	public class AnsonResp : AnsonBody
	{
		protected internal string m;

		protected List<AnResultset> rs;

		public Dictionary<string, object> map;

		public AnsonResp() : base(null, null)
		{
		}

		public AnsonResp(AnsonMsg parent) : base(parent, null)
		{
		}

		public AnsonResp(AnsonMsg parent, string txt) : base(parent, null)
		{
			this.m = txt;
		}

		public AnsonResp(string txt) : base(null, null)
		{
			this.m = txt;
		}

		public virtual string Msg()
		{
			return m;
		}

		public virtual AnsonResp Rs(AnResultset rs)
		{
			if (this.rs == null)
			{
				this.rs = new List<AnResultset>(1);
			}
			this.rs.Add(rs);
			return this;
		}

		/// <summary>Add a resultset to list.</summary>
		/// <param name="rs"/>
		/// <param name="totalRows">total row count for a paged query (only a page of rows is actually in rs).
		/// 	</param>
		/// <returns>this</returns>
		public virtual AnsonResp Rs(AnResultset rs, int totalRows)
		{
			if (rs == null)
			{
				this.rs = new List<AnResultset>();
			}

			// rs.total = totalRows;
			this.rs.Add(rs);
			return this;
		}

		public virtual AnsonBody Rs(List<AnResultset> rsLst)
		{
			rs = rsLst;
			return this;
		}

		// public virtual List<AnResultset> Rs () { return rs; }

		public virtual AnResultset Rs(int ix)
		{
			return rs == null ? null : rs[ix];
		}

		public virtual AnsonResp Data(Dictionary<string, object> props)
		{
			map = props;
			return this;
		}

		public virtual Dictionary<string, object> Data()
		{
			return map;
		}
	}
}
