using io.odysz.semantic.jprotocol;
using io.odysz.semantic.jserv.R;
using io.odysz.semantics.x;
using System.Collections.Generic;

namespace io.odysz.semantic.jserv.U
{
	/// <summary>
	/// <p>Insert Request Message</p>
	/// <b>Note:</b>
	/// <p>InsertReq is a subclass of UpdateReq, and have no
	/// <see cref="#toJson(com.google.gson.stream.JsonWriter,jprotocol.JOpts)
	/// 	">toJson()</see>
	/// and
	/// <see cref="#fromJsonName(String,com.google.gson.stream.JsonReader)">fromJson()</see>
	/// implementation.
	/// Otherwise any post updating list in request won't work.</p>
	/// Because all request element is deserialized a UpdateReq, so this can only work for Update/Insert request.</p>
	/// <p>Design Memo<br />
	/// This is a strong evidence showing that we need anson.</p>
	/// see
	/// <see cref="UpdateReq#fromJsonName(String,com.google.gson.stream.JsonReader)">super.fromJsonName()
	/// 	</see>
	/// <br />
	/// and
	/// <see cref="jprotocol.JHelper#readLstUpdateReq(com.google.gson.stream.JsonReader)
	/// 	">JHelper.readListUpdateReq()</see>
	/// </summary>
	/// <author>odys-z@github.com</author>
	public class AnUpdateReq : AnsonBody
	{
		/// <summary>Format an update request.</summary>
		/// <param name="conn"/>
		/// <param name="parent"/>
		/// <param name="tabl"/>
		/// <param name="cmd">
		/// <see cref="JProtocol.CRUD"/>.C R U D
		/// </param>
		/// <returns>a new update request</returns>
		public static AnUpdateReq FormatUpdateReq(string conn, AnsonMsg parent, string tabl)
		{
			AnUpdateReq bdItem = ((AnUpdateReq)new AnUpdateReq(parent, conn)
				.A(JProtocol.CRUD.U))
				.Mtabl(tabl);
			return bdItem;
		}

		/// <summary>Format a delete request.</summary>
		/// <param name="conn"/>
		/// <param name="parent"/>
		/// <param name="tabl"/>
		/// <returns>a new deleting request</returns>
		public static AnUpdateReq formatDelReq(string conn, AnsonMsg parent, string tabl)
		{
			AnUpdateReq bdItem = ((AnUpdateReq)new AnUpdateReq(parent, conn)
				.A(JProtocol.CRUD.D))
				.Mtabl(tabl);
			return bdItem;
		}

		/// <summary>Main table</summary>
		internal string mtabl { get; set; }

        public virtual AnUpdateReq Mtabl(string mtbl)
        {
            mtabl = mtbl;
            return this;
        }

        /// <summary>
        /// nvs: [nv-obj],
        /// nv-obj: {n: "roleName", v: "admin"}
        /// </summary>
        internal List<object[]> nvs;

		/// <summary>inserting values, used for "I".</summary>
		/// <remarks>inserting values, used for "I". 3d array [[[n, v], ...]]</remarks>
		protected internal List<List<object[]>> nvss;

		/// <summary>inserting columns, used for "I".</summary>
		/// <remarks>
		/// inserting columns, used for "I".
		/// Here a col shouldn't be an expression - so not Object[], unlike that of query.
		/// </remarks>

		/// <summary>get columns for sql's insert into COLs.</summary>
		protected internal string[] cols { get; set; }

		/// <summary>get columns for sql's insert into COLs.</summary>
		/// <returns>columns</returns>
		// public virtual string[] cols() { return cols; }

		/// <summary>
		/// where: [cond-obj], see
		/// <see cref="#joins"/>
		/// for cond-obj.
		/// </summary>
		internal List<object[]> where;

		internal string limt;

		internal List<AnUpdateReq> postUpds;

		public jprotocol.AnsonHeader header;

		internal List<object[]> attacheds;

		public AnUpdateReq() : base(null, null)
		{
		}

		/// <summary>
		/// Don't call new InsertReq(), call
		/// <see cref="#formatReq(String,JMessage,String)"/>
		/// .
		/// This constructor is declared publicly for JHelper.
		/// </summary>
		/// <param name="parent"/>
		/// <param name="conn"/>
		public AnUpdateReq(AnsonMsg parent, string conn)
			: base(parent, conn)
		{
		}

		public virtual AnUpdateReq Nv(string n, object v)
		{
			if (nvs == null)
			{
				nvs = new List<object[]>();
			}
			object[] nv = new object[2];
			nv[Query.Ix.nvn] = n;
			nv[Query.Ix.nvv] = v;
			nvs.Add(nv);
			return this;
		}

		/// <exception cref=".x.SemanticException"/>
		public virtual void Valus(List<object[]> row)
		{
			if (nvs != null && nvs.Count > 0)
			{
				throw new SemanticException("InsertReq don't support both nv() and values() been called for the same request object. User only one of them." );
			}
			if (nvss == null)
			{
				nvss = new List<List<object[]>>();
			}
			nvss.Add(row);
		}

		/// <summary>get values in VALUE-CLAUSE for sql insert into (...) values VALUE-CLAUSE
		/// 	</summary>
		/// <returns>[[[n, v], ...]]</returns>
		public virtual List<List<object[]>> Values()
		{
			if (nvs != null && nvs.Count > 0)
			{
				if (nvss == null)
				{
					nvss = new List<List<object
						[]>>();
				}
				nvss.Add(nvs);
				nvs = null;
			}
			return nvss;
		}

		/// <summary>FIXME wrong?</summary>
		/// <param name="file"/>
		/// <param name="b64"/>
		/// <returns/>
		public virtual AnUpdateReq Attach(string file, string b64)
		{
			if (attacheds == null)
			{
				attacheds = new List<object[]>();
			}
			attacheds.Add(new string[] { file, b64 });
			return this;
		}

		public virtual AnUpdateReq Where(string oper, string lop, string rop)
		{
			if (where == null)
			{
				where = new List<object[]>();
			}
			string[] predicate = new string[Query.Ix.predicateSize];
			predicate[Query.Ix.predicateOper] = oper;
			predicate[Query.Ix.predicateL] = lop;
			predicate[Query.Ix.predicateR] = rop;
			where.Add(predicate);
			return this;
		}

		/// <summary>calling where("=", lop, "'" + rconst + "'")</summary>
		/// <param name="lop"/>
		/// <param name="rconst"/>
		/// <returns/>
		public virtual AnUpdateReq WhereEq(string lop, string rconst)
		{
			return Where("=", lop, "'" + rconst + "'");
		}

		public virtual AnUpdateReq Post(AnUpdateReq pst)
		{
			if (postUpds == null)
			{
				postUpds = new List<AnUpdateReq>();
			}
			postUpds.Add(pst);
			return this;
		}

		/// <exception cref="SemanticException"/>
		public virtual void Validate()
		{
			if (!JProtocol.CRUD.D.Equals(a)
				&& (nvs == null || nvs.Count <= 0) && (nvss == null || nvss.Count <= 0))
			{
				throw new SemanticException("Updating/inserting denied for empty column values");
			}
			if ((JProtocol.CRUD.U.Equals(a) || JProtocol.CRUD.D.Equals(a)) && (where == null || where.Count == 0))
			{
				throw new SemanticException("Updatin/deleting  denied for empty conditions"
					);
			}
			if (!JProtocol.CRUD.R.Equals(a) && string.IsNullOrEmpty(mtabl))
			{
				throw new SemanticException("Updating/inserting/deleting denied for empty main table");
			}
		}
	}
}
