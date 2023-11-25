using io.odysz.transact.x;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TestAnson.net.semantics.io.odysz.x
{
	class AnsonException : TransException
	{
		public string ex { get; set; }

		public AnsonException(string format, params object[] args)
			: base(format, args)
		{
		}
		public AnsonException(string message, object arg) : base(message, arg)
		{
		}
	}
}
