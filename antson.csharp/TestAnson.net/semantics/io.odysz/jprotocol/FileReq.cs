using io.odysz.semantic.jprotocol;

namespace io.odysz.semantic.jserv.file
{
	public class FileReq : AnsonBody
	{
		internal string file { get; set; }

		internal int len;

		internal string payload;

		protected internal FileReq(AnsonMsg parent, string filename)
			: base(parent, null)
		{
			file = filename;
		}

	}
}
