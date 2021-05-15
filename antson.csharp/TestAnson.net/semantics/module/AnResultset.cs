using io.odysz.anson;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;

namespace io.odysz.module.rs
{
    public class AnResultset : Anson
    {
		private const bool debug = true;

        protected int colCnt = 0;
        /**current row index, start at 1. */
        protected int rowIdx = -1;
        protected int rowCnt = 0;

        /// <summary>
        /// Design Notes: 
        /// C# requires explicit list/array convertion, with element type checked. This is time consuming.
        /// In Antson.cs 1.0, multi-dimensional list/array deserialization is bypassed this way, an IList with valType. 
        /// </summary>
        [AnsonField(valType = "java.util.ArrayList")]
        public IList results;

        /**col-index start at 1, map: [alais(upper-case), [col-index, db-col-name(in raw case)]<br>
         * case 1<pre>
           String colName = rsMeta.getColumnLabel(i).toUpperCase();
           colnames.put(colName, new Object[] {i, rsMeta.getColumnLabel(i)});
         </pre>
         * case 2<pre>
           for (String coln : colnames.keySet()) 
             colnames.put(coln.toUpperCase(), new Object[] {colnames.get(coln), coln});
         </pre>
         * */
        [AnsonField(valType = "[Ljava.lang.Object;")]
        protected Hashtable colnames;

        /// <summary>Ignored when serializing, and no such data from java
        /// - it's type of Resultset</summary>
        [AnsonField(ignoreTo = true)]
        public DataTable rs { get; private set; }

        [AnsonField(ignoreTo = true, ignoreFrom = true)]
        protected SqlConnection conn;

        [AnsonField(ignoreTo = true, ignoreFrom = true)]
        protected SqlCommand stmt;

		/**For paged query, this the total row count*/
		protected int total = 0;

		protected Hashtable stringFormats;

		/** for deserializing */
		public AnResultset() { }

		public AnResultset(int rows, int cols, string colPrefix = "")
        {
            if (rows <= 0 || cols <= 0)
                return;
            results = new List<List<object>>(rows);

            colCnt = cols;
            colnames = new Hashtable(cols);
            for (int i = colCnt; i >= 1; i--)
            {
                string colName = (colPrefix == null || colPrefix.Length != 1)
                    ? i.ToString() : string.Format("{0}{1}", colPrefix.Trim(), i);
                colnames[colName.ToUpper()] = new object[] { i, colName };
            }
            rowIdx = 0;
            rowCnt = 0;
            for (int r = 0; r < rows; r++)
            {
                rowCnt++;
                List<object> row = new List<object>(colCnt);
                for (int j = 1; j <= colCnt; j++)
                {
                    row.Add(string.Format("{0}, {1}", r, j));
                }
                results.Add(row);
            }
        }

        public int GetRowCount()
        {
            return results == null ? rs == null ? 0 : rs.Rows.Count : results.Count;
        }

        public string GetString(int rix, string colname)
        {
            int cix = (int)((object[])colnames[colname?.ToUpper()])[0];
            return (string)((IList)results[rix])[cix];
        }
    }
}