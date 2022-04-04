using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace io.odysz.anson.common
{
    public class LangExt
    {
        static public bool isblank(string s, string takeAsNull, string takeAsNull2 = null)
        {
			return isblank(s, new string[] { takeAsNull, takeAsNull2 });
        }

        static public bool isblank(string s, string[] takeAsNull = null)
        {
			if (s == null || s.Trim().Length == 0)
				return true;
			else if (takeAsNull == null || takeAsNull.Length == 0)
				return false;
			else
			{
				foreach (string asNull in takeAsNull)
					if (s == asNull)
						return true;
				return false;
			}
        }
    }
}
