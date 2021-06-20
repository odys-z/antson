namespace io.odysz.semantic.jserv.U
{
    public class Query
    {
		/**String Array Index definition. Not using java field for compliance with JS (without user type).
		 * @author odys-z@github.com
		 */
		public class Ix
		{
			/**String[0] = join.j | join.l | join.r */
			public const int joinType = 0;
			/**String[1] = join-with-tabl */
			public const int joinTabl = 1;
			/**String[2] = alias */
			public const int joinAlias = 2;
			/**String[3] = on-condition-string */
			public const int joinOnCond = 3;
			public const int joinSize = 4;

			public const int exprExpr = 0;
			public const int exprAlais = 1;
			public const int exprTabl = 2;
			public const int exprSize = 3;

			public const int nvn = 0;
			public const int nvv = 1;

			public const int predicateOper = 0;
			public const int predicateL = 1;
			public const int predicateR = 2;
			public const int predicateSize = 3;

			public const int orderExpr = 0;
			public const int orderAsc = 1;
		}

	}
}