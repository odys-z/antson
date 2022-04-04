using io.odysz.anson;

namespace io.odysz.semantic.jprotocol
{
    public class PortCode
    {
        public const int NA = -1;       // error
        public const int heartbeat = 0; // ("ping.serv11"),
        public const int session = 1;   // ("login.serv11"),
        public const int query = 2;     // ("r.serv11"),
        public const int update = 3;    // ("u.serv11"),
        public const int insert = 4;    // ("c.serv11");
        public const int delete = 5;    // ("d.serv11"),
        public const int echo = 6;      // ("echo.serv11"),
        /// <summary> serv port for downloading json/xml file or uploading a file.</summary>
        /// <see cref="jserv.file.JFileServ"/>
        public const int file = 7;      // ("file.serv11"),
        /// <summary>Any user defined request using message body of subclass of JBody must use this port </summary>
        public const int user = 8;      // ("user.serv11"),
        /// <summary>semantic tree of dataset extensions</summary>
        /// <see cref="ext.SemanticTree"/>
        public const int stree = 9;     // ("s-tree.serv11"),
        /// <summary>dataset extensions<br> </summary> 
        /// <see cref="ext.Dataset" />
        public const int dataset = 10;  // ("ds.serv11");
    }

	public interface IPort : IJsonable
	{
        string url();
        string name { get; }

		/// <summary>Equivalent of enum.valueOf(), except for subclass returning instance of jserv.Port.
		/// 	</summary>
		/// <exception cref="SemanticException"></exception>
		int valof(string pname);
	}
}
