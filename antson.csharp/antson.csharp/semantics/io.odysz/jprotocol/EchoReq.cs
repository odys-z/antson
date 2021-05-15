using io.odysz.semantic.jprotocol;

namespace io.odysz.semantic.jserv.echo
{
	public class EchoReq : AnsonBody
	{
		public EchoReq(AnsonMsg parent)
			: base(parent, null)
		{
		}
	}
}
