using io.odysz.semantic.jprotocol;
using System.Collections.Generic;

namespace io.odysz.semantic.jserv.R
{
	/// <summary>
	/// Query Request Body Item.<br />
	/// Included are information of RDBMS query information,
	/// table, joins, conditions, groups, orders, etc.
	/// </summary>
	/// <author>odys-z@github.com</author>
	public class AnQueryReq : AnsonBody
	{
		/// <summary>Main table</summary>
		internal string mtabl;

		/// <summary>Main table alias</summary>
		internal string mAlias;

		/// <summary>
		/// <pre>joins: [join-obj],
		/// - join-obj: [{t: "j/R/l", tabl: "table-1", as: "t_alais", on: conds}]
		/// - conds: [cond-obj]
		/// cond-obj: {(main-table | alais.)left-col-val op (table-1 | alias2 .)right-col-val}
		/// - op: '=' | '&lt;=' | '&gt;=' ...</pre>
		/// </summary>
		internal List<string[]> joins;

		/// <summary>
		/// exprs: [expr-obj],
		/// expr-obj: {tabl: "b_articles/t_alais", alais: "recId", expr: "recId"}
		/// </summary>
		internal List<string[]> exprs;

		/// <summary>
		/// where: [cond-obj], see
		/// <see cref="joins"/>
		/// for cond-obj.
		/// </summary>
		internal List<string[]> where;

		/// <summary>
		/// orders: [order-obj],
		/// - order-obj: {tabl: "b_articles", field: "pubDate", asc: "true"}
		/// </summary>
		internal List<string[]> orders;

		/// <summary>
		/// group: [group-obj]
		/// - group-obj: {tabl: "b_articles/t_alais", expr: "recId" }
		/// </summary>
		internal string[] groups;

		protected internal int page { get; set; }

		protected internal int pgsize;

		internal string[] limt;

		internal List<string[]> havings;

		public AnQueryReq(AnsonMsg parent, string conn) : base(parent, conn)
		{
			a = JProtocol.CRUD.R;
		}

		public AnQueryReq()
			: base(null, null)
		{
			a = JProtocol.CRUD.R;
		}

		public AnQueryReq(AnsonMsg parent, string conn, string fromTbl, params string[] alias)
			: base(parent, conn)
		{
			a = JProtocol.CRUD.R;
			mtabl = fromTbl;
			mAlias = alias == null || alias.Length == 0 ? null : alias[0];
			Page(-1, 0);
		}

		public virtual AnQueryReq Page(int page, int size)
		{
			this.page = page;
			this.pgsize = size;
			return this;
		}

		public virtual int size()
		{
			return pgsize;
		}


		public virtual AnQueryReq j(string with, string @as, string on)
		{
			return j("j", with, @as, on);
		}

		public virtual AnQueryReq l(string with, string @as, string on)
		{
			return j("l", with, @as, on);
		}

		public virtual AnQueryReq r(string with, string @as, string on)
		{
			return j("R", with, @as, on);
		}

		public virtual AnQueryReq j(List <string[]> joins)
		{
			if (joins != null)
			{
				foreach (string[] join in joins)
				{
					j(join);
				}
			}
			return this;
		}

		public virtual AnQueryReq j(string t, string with, string @as, string on)
		{
			if (joins == null)
			{
				joins = new List<string[]>();
			}
			string[] joining = new string[Query.Ix.joinSize];
			joining[Query.Ix.joinTabl] = with;
			joining[Query.Ix.joinAlias] = @as;
			joining[Query.Ix.joinType] = t;
			joining[Query.Ix.joinOnCond] = on;
			return j(joining);
		}

		private AnQueryReq j(string[] join)
		{
			joins.Add(join);
			return this;
		}

		public virtual AnQueryReq expr(string expr, string alias, params string[] tabl)
		{
			if (exprs == null)
			{
				exprs = new List<string[]>();
			}
			string[] exp = new string[Query.Ix.exprSize];
			exp[Query.Ix.exprExpr] = expr;
			exp[Query.Ix.exprAlais] = alias;
			exp[Query.Ix.exprTabl] = tabl == null || tabl.Length == 0 ? 
				null : tabl[0];
			exprs.Add(exp);
			return this;
		}

		public virtual AnQueryReq Where(string oper, string lop, string rop)
		{
			if (where == null)
			{
				where = new List<string[]>();
			}
			string[] predicate = new string[Query.Ix.predicateSize];
			predicate[Query.Ix.predicateOper] = oper;
			predicate[Query.Ix.predicateL] = lop;
			predicate[Query.Ix.predicateR] = rop;
			where.Add(predicate);
			return this;
		}

		public virtual AnQueryReq orderby(string col, params bool[] asc)
		{
			if (orders == null)
			{
				orders = new List<string[]>();
			}
			orders.Add(new string[] { col, asc == null || asc.Length == 0 ? "asc" : asc[0] ? "asc" : "desc" });
			return this;
		}


		/// <summary>
		/// <p>Create a qeury request body item, for joining etc.</p>
		/// <p>This is a client side helper, don't confused with
		/// <see cref="Query">Query</see>
		/// .</p>
		/// </summary>
		/// <param name="conn"/>
		/// <param name="parent"/>
		/// <param name="from"></param>
		/// <param name="asTabl"></param>
		/// <returns>query request</returns>
		public static AnQueryReq formatReq(string conn, AnsonMsg parent, string from, params string[] asTabl)
		{
			AnQueryReq bdItem = new AnQueryReq
				(parent, conn, from, asTabl == null || asTabl.Length == 0 ? null : asTabl[0]);
			return bdItem;
		}


		public virtual AnQueryReq having(string oper, string lop, string rop)
		{
			if (where == null)
			{
				where = new List<string[]>();
			}
			string[] predicate = new string[Query.Ix.predicateSize];
			predicate[Query.Ix.predicateOper] = oper;
			predicate[Query.Ix.predicateL] = lop;
			predicate[Query.Ix.predicateR] = rop;
			where.Add(predicate);
			return this;
		}

	}
}
