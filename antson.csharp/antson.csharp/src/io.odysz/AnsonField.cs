using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace io.odysz.anson
{
    /// <summary>
    /// member attribution, java annotation.
    /// </summary>
    public class AnsonField : Attribute
    {
        // nothing happen
        public const int undefined = -1;

        /// <summary>Must trigger reference resolving, e.g. indicating a parent field
        /// </summary>
        public const int enclosing = 1;

        public bool ignoreTo = false;

	    public bool ignoreFrom = false;

		/**
         * <p>Specifying array's element type information.</p>
         * Example:<br>
         * for Object[], use<pre>
           @AnsonField(valType="[Ljava.lang.Object;")
           Object[][] f;
           </pre>
         * for ArrayList&lt;Object[]&gt;, use <pre>
           @AnsonField(valType="java.util.ArrayList;[Ljava.lang.Object;"
           ArrayList<ArrayList<Object[]>>
           </pre>
         */
		public string valType = "";

	    public int refer = undefined;

    }
}
