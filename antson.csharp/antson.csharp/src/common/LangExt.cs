using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace io.odysz.anson.common
{
    public class LangExt
    {
        static public bool isblank(string s, string nul, string nul2, string nul3, string nul4 = null)
        {
			return isblank(s, new string[] { nul, nul2, nul3, nul4 });
        }

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

		static public T[] Fill<T>(T[] arr, T value)
		{
			for (int i = 0; i < arr.Length; i++)
				arr[i] = value;
			return arr;
		}
	}
}
