using io.odysz.transact.x;
using System;

namespace io.odysz.semantics.x
{
	[Serializable]
	public class SemanticException : TransException
	{
		public SemanticObject ex { get; set; }

		public SemanticException(string format, params object[] args)
			: base(format, args)
		{
		}
		public SemanticException(string message, Exception innerException) : base(message, innerException)
		{
		}

		//protected SemanticException(SerializationInfo info, StreamingContext context)
		//	: base(info, context) { }
	}
}
