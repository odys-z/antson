using io.odysz.anson;
using io.odysz.semantics.x;
using System;
using System.Collections.Generic;
using System.IO;

namespace io.odysz.semantic.jprotocol
{
	/// <summary>
	/// json type: io.odysz.semantic.jprotocol.AnsonMsg
	/// <p>Base class of message used by
	/// <see cref="jserv.ServPort{T}">serv11</see>
	/// .</p>
	/// 1. A incoming json message is parsed by *.serv into JMessage,
	/// which can be used to directly to build statements;<br />
	/// 2. An outgoing data object which is presented as AnsonMsg<AnsonResp>,
	/// which should been directly write into output stream.
	/// </summary>
	/// <author>odys-z@github.com</author>
	public class AnsonMsg : Anson
	{
		/// <summary>
		/// Port is the conceptual equivalent to the SOAP port, the service methods' group.<br />
		/// NOTE: java code shouldn't use switch-case block on enum.
		/// </summary>
		/// <remarks>
		/// Port is the conceptual equivalent to the SOAP port, the service methods' group.<br />
		/// NOTE: java code shouldn't use switch-case block on enum. That cause problem with generated class.
		/// </remarks>
		/// <author>odys-z@github.com</author>
		[Serializable]
		public sealed class Port : IPort
		{
			/// <summary>ping.serv11</summary>
			public const int heartbeat = 0;

			/// <summary>login.serv11</summary>
			public const int session = 1;

			/// <summary>r.serv11</summary>
			public const int query = 2;

			/// <summary>u.serv11</summary>
			public const int update = 3;

			/// <summary>c.serv11</summary>
			public const int insert = 4;

			/// <summary>d.serv11</summary>
			public const int delete = 5;

			/// <summary>echo.serv11</summary>
			public const int echo = 6;

			/// <summary>file.serv11:
			/// serv port for downloading json/xml file or uploading a file.</summary>
			/// <seealso>
			/// <see cref="jserv.file.JFileServ"/>
			/// </seealso>
			public const int file = 7;

			/// <summary>user.serv11:
			/// Any user defined request using message body of subclass of JBody must
			/// use this port</summary>
			public const int user = 8;

			/// <summary>s-tree.serv11:
			/// semantic tree of dataset extensions</summary>
			/// <seealso>
			/// <see cref="ext.SemanticTree"/>
			/// </seealso>
			public const int stree = 9;

			/// <summary>ds.serv11:
			/// dataset extensions<br /></summary>
			/// <seealso>
			/// <see cref="ext.Dataset"/>
			/// </seealso>
			public const int dataset = 10;

			public const int NA = -1;

			public readonly int port;

            class PortFactory : JsonableFactory {
                public IJsonable fromJson(string p) { return new Port(p); }
            }
			/// <summary>
			/// Setup a register for extinding new port.
			/// </summary>
            static Port()
            {
				JSONAnsonListener.registFactory(typeof(Port), new PortFactory());
			}

			// public string name { get; private set; }

			public Port(string name) { port = valof(name); }

			public Port(int port) { this.port = port; }

			public string name { get { return nameof(port); } }

			public int valof(string pname)
			{
				int p = pname == "haartbeat" ? Port.heartbeat
					: pname == "session" ? Port.session
					: pname == "query" ? Port.query
					: pname == "update" ? Port.update
					: pname == "insert" ? Port.insert
					: pname == "delete" ? Port.delete
					: pname == "echo" ? Port.echo
					: pname == "file" ? Port.file
					: pname == "user" ? Port.user
					: pname == "stree" || pname == "s-tree" ? Port.stree
					: pname == "dataset" ? Port.dataset
					: Port.NA;
				return p;
			}

			static public string nameof(int port)
			{
				return port == Port.heartbeat ? "heartbeat"
					: port == Port.session ? "session"
					: port == Port.query ? "query"
					: port == Port.update ? "update"
					: port == Port.insert ? "insert"
					: port == Port.delete ? "delete"
					: port == Port.echo ? "echo"
					: port == Port.file ? "file"
					: port == Port.user ? "user"
					: port == Port.stree ? "stree"
					: port == Port.dataset ? "dataset"
					: "NA";
			}

            public string url()
			{
				return port == heartbeat ? "ping.serv"
                    : port == session ? "login.serv11"
                    : port == query ? "r.serv11"
                    : port == update ? "u.serv11"
                    : port == insert ? "c.serv11"
                    : port == delete ? "d.serv11"
                    : port == echo ? "echo.less"
                    : port == file ? "file.serv"
                    : port == user ? "user.serv11"
                    : port == stree ? "s-tree.serv11"
                    : port == dataset ? "ds.serv11"
                    : "unknown.serv";
			}

            public IJsonable ToBlock(Stream stream, anson.JsonOpt opts = null)
            {
				Utils.WriteStr(stream, name, true);
				return this;
            }
        }

		[Serializable]
		public class MsgCode : IJsonable
		{
			public const int ok = 0;

			public const int exSession = 1;

			public const int exSemantic = 2;

			public const int exIo = 3;

			public const int exTransct = 4;

			public const int exDA = 5;

			public const int exGeneral = 6;

			public const int ext = 7;

			public int code { get; protected set; }
			public MsgCode(int code)
			{
				this.code = code;
			}

			public MsgCode(string name) : this(CodeOf(name))
			{ }

            public bool eq(string code)
			{
				if (code == null)
				{
					return false;
				}
				int c = CodeOf(code);
				return this.code == c;
			}

			private static int CodeOf(string name)
			{
				return name == "ok" ? MsgCode.ok
					: name == "exSession" ? MsgCode.exSession
					: name == "exSemantic" ? MsgCode.exSemantic
					: name == "exIo" ? MsgCode.exIo
					: name == "exTransct" ? MsgCode.exTransct
					: name == "exDA" ? MsgCode.exDA
					: name == "exGeneral" ? MsgCode.exGeneral
					: MsgCode.ext;
			}

			public string Name()
            {
				return code == MsgCode.ok ? "ok"
					: code == MsgCode.exSession ? "exSession"
					: code == MsgCode.exSemantic ? "exSemantic"
					: code == MsgCode.exIo ? "exIo"
					: code == MsgCode.exTransct ? "exTransct"
					: code == MsgCode.exDA ? "exDA"
					: code == MsgCode.exGeneral ? "exGeneral"
					: "ext";

            }

            public IJsonable ToBlock(Stream stream, anson.JsonOpt opts = null)
            {
				Utils.WriteStr(stream, Name(), false);
				return this;
            }
        }

		/// TODO we can simplify java field here
		/// <summary>The default IPort implelemtation.</summary>
		/// <remarks>
		/// The default IPort implelemtation.
		/// Used for parsing port name (string) to IPort instance, like
		/// <see cref="#Port"/>
		/// .<br />
		/// </remarks>
		/// internal static IPort defaultPortImpl;

		/// <summary>
		/// Set the default IPort implelemtation, which is used for parsing port name (string)
		/// to IPort instance, like
		/// <see cref="Port"/>.
		/// Because <see cref="Port"/> only defined limited ports, user must initialize JMessage with
		/// <see cref="AnsonMsg{T}.understandPorts(IPort)"/>.
		/// An example of how to use this is shown in jserv-sample/io.odysz.jsample.SysMenu.<br />
		/// Also check how to implement IPort extending
		/// <see cref="Port"/>, see example of jserv-sample/io.odysz.jsample.protocol.Samport.
		/// </summary>
		/// <param name="p">extended Port</param>
		//public static void understandPorts(IPort p)
		//{
		//	defaultPortImpl = p;
		//}

		string version = "1.0";

		internal int seq { get; set; }

		public Port port { get; private set; }

		public MsgCode code { get; private set; }

		public AnsonMsg()
		{
			seq = new Random().Next(1000);
		}

		public AnsonMsg(string port)
		{
			this.port = new Port(port);
			seq = new Random().Next(1000);
		}

		/// <summary>Typically for response</summary>
		/// <param name="p"></param>
		/// <param name="code"/>
		public AnsonMsg(IPort p, MsgCode code)
		{
			port = (Port)p;
			this.code = code;
		}

		protected internal IList<AnsonBody> body;

		public virtual AnsonBody Body(int i)
		{
			return body[0];
		}

		public virtual IList<AnsonBody> Body()
		{
			return body;
		}

		/// <summary>Add a request body to the request list.</summary>
		/// <param name="bodyItem"/>
		/// <returns>new message object</returns>
		public virtual AnsonMsg Body(AnsonBody bodyItem)
		{
			if (body == null)
			{
				body = new List<AnsonBody>();
			}
			body.Add(bodyItem);
			// bodyItem.parent = this;
			bodyItem.Parent(this);
			return this;
		}

		public virtual AnsonMsg incSeq()
		{
			seq = seq + 1;
			return this;
		}

		internal AnsonHeader header { get; set; }
		public AnsonMsg Header(AnsonHeader h)
		{
			header = h;
			return this;
		}

		internal JsonOpt opts { get; set; }

		public virtual AnsonMsg Body(IList<AnsonBody> bodyItems)
		{
			body = bodyItems;
			return this;
		}

		public static AnsonMsg Ok(IPort p, string txt)
		{
			AnsonResp bd = new AnsonResp(txt);
			return new AnsonMsg(p, new MsgCode(MsgCode.ok)).Body(bd);
		}

		public static AnsonMsg Ok(IPort p, AnsonBody resp)
		{
			return new AnsonMsg(p, new MsgCode(MsgCode.ok)).Body(resp);
		}
    }
}
