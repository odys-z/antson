using io.odysz.semantic.jprotocol;
using io.odysz.semantics.x;

namespace io.odysz.semantic.jserv.U
{
	/// <summary>
	/// <p>Insert Request helper</p>
	/// <b>Note:</b>
	/// <p>AnInsertReq is a subclass of
	/// <see cref="AnUpdateReq"/>
	/// .</p>
	/// <p>Because all request element is deserialized as an AnUpdateReq, so this can only work for Update/Insert request.</p>
	/// </summary>
	/// <author>odys-z@github.com</author>
	public class AnInsertReq : AnUpdateReq
	{
		public AnInsertReq()
		{
		}

		/// <summary>
		/// Don't call new InsertReq(), call
		/// <see cref="#formatReq(String,JMessage,String)"/>.
		/// This constructor is declared publicly for JHelper.
		/// </summary>
		/// <param name="parent"/>
		/// <param name="conn"/>
		public AnInsertReq(AnsonMsg parent, string conn)
			: base(parent, conn)
		{
			a = JProtocol.CRUD.C;
		}

		public virtual AnInsertReq Cols(string[] cols)
		{
			base.cols = cols;
			a = JProtocol.CRUD.C;
			return this;
		}

		/// <summary>Format an insert request.</summary>
		/// <param name="conn"/>
		/// <param name="parent"/>
		/// <param name="tabl"/>
		/// <param name="cmd">
		/// 
		/// <see cref="JProtocol.CRUD"/>
		/// .C R U D
		/// </param>
		/// <returns>a new update request</returns>
		public static AnInsertReq formatInsertReq(string conn, AnsonMsg parent, string tabl)
		{
			AnInsertReq bdItem = (AnInsertReq) new AnInsertReq(parent, conn).A(JProtocol.CRUD.C);
			bdItem.mtabl = tabl;
			return bdItem;
		}

		public override void Valus(System.Collections.Generic.List<object[]> row)
		{
			if (nvs != null && nvs.Count > 0)
			{
				throw new SemanticException("InsertReq don't support both nv() and values() been called for the same request object. User only one of them.");
			}
			if (nvss == null)
			{
				nvss = new System.Collections.Generic.List<System.Collections.Generic.List<object[]>>();
			}
			nvss.Add(row);
		}

		public virtual AnUpdateReq Cols(string c, params string[] ci)
		{
			if (cols == null)
			{
				cols = new string[ci == null ? 1 : ci.Length + 1];
			}
			cols[0] = c;
			for (int ix = 0; ci != null && ix < ci.Length; ix++)
			{
				cols[ix + 1] = ci[ix];
			}
			return this;
		}
	}
}
