using io.odysz.anson;
using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using static io.odysz.anson.jprotocol.AnsonMsg;

namespace io.odysz.anson.jprotocol
{
    public interface IPort : IJsonable 
    {
        string Url();// { return "echo.jserv"; }

        string Name();

        /// <summery>Equivalent of enum.valueOf(), except for subclass returning
        /// instance of jserv.Port.</summery>
        IPort Valof(string pname);
    }

    public class AnsonHeader : Anson
    {
        string uid;
        string ssid;
        string iv64;
        string [] usrAct;


        public AnsonHeader(string ssid, string uid)
        {
            this.uid = uid;
            this.ssid = ssid;
        }

        public AnsonHeader() { }

        public string Logid() { return uid; }

        public String Ssid() { return ssid; }

        /**
         * @return js equivalent {md: ssinf.md, ssid: ssinf.ssid, uid: ssinf.uid, iv: ssinf.iv};
         */
        public static AnsonHeader Format(String uid, String ssid)
        {
            // formatLogin: {a: "login", logid: logId, pswd: tokenB64, iv: ivB64};
            return new AnsonHeader(ssid, uid);
        }

        public AnsonHeader act(string[] act)
        {
            usrAct = act;
            return this;
        }

        public static String[] UsrAct(String funcId, String cmd, String cate, String remarks)
        {
            return new String[] { funcId, cate, cmd, remarks };
        }

    }

    public interface IAnsonBody {
        AnsonMsg Msg(); // { get; set; }
        IAnsonBody Msg(AnsonMsg p); // { get; set; }
        string conn { get; set; }
        string a { get; set; }

        IAnsonBody A(string act);
    }


    public class AnsonMsg : Anson
	{
        /// <summary> Port is the conceptual equivalent to the SOAP port, the service methods' group.
        /// In java, this is a enum type.
        /// In C#, this is IJsonable and the reverse of #ToBlock() is the construction Port(string).
        /// </summary>
        public class Port : IPort
        {
            public const int heartbeat = 1; // ("ping.serv11"),
            public const int session = 2; // ("login-serv11"),
            public const int query = 3; // ("r.serv11");
           
            private readonly string url;
            public string Url() { return url; }

            int port;
            public Port(string url) {
                this.url = url;
                port = url == "session" ? session
                     : url == "query" ? query
                     : heartbeat;
            }

            public Port(int p)
            {
                port = p;
                url = Name();
            }

            public string Name()
            {
                return port == session ? "session"
                    : port == query ? "query"
                    : "heartbeat";
            }

            public IPort Valof(string pname)
            {
                return new Port("ping.serv11");
            }

            public IJsonable ToBlock(Stream stream, JsonOpt opts = null)
            {
                Utils.WriteStr(stream, string.Format("\"{0}\"", Name()));
                return this;
            }

        }
        public class MsgCode : IJsonable
        {
            public const int ok = 0, exSession = 1, exSemantic = 2, exIo = 3,
                             exTransct = 4, exDA = 5, exGeneral = 6, ext = 7;

            protected int code = -1;

            public MsgCode(int code)
            {
                this.code = code;
            }
            public MsgCode(string name)
            {
                this.code = ValueOf(name);
            }

            public bool eq(string code)
            {
                if (code == null) return false;
                int c = MsgCode.ValueOf(code);
                return this.code == c;
            }

            public IJsonable ToBlock(Stream s, JsonOpt opt)
            {
                Utils.WriteStr(s, code.ToString());
                return this;
            }

            public static int ValueOf(string code)
            {
                if (string.IsNullOrEmpty(code)) return -1;
                else if ("ok" == code) return ok;
                else if ("exSession" == code) return exSession;
                else if ("exSemantic" == code) return exSemantic;
                else if ("exIo" == code) return exIo;
                else if ("exTransct" == code) return exTransct;
                else if ("exDA" == code) return exDA;
                else if ("exGeneral" == code) return exGeneral;
                else return ext;
            }
            public static string NameOf(int code)
            {
                if (code < 0) return "NA";
                else if (ok == code) return "ok";
                else if (exSession == code) return "exSession";
                else if (exSemantic == code) return "exSemantic";
                else if (exIo == code) return "exIo";
                else if (exTransct == code) return "exTransct";
                else if (exDA == code) return "exDA";
                else if (exGeneral == code) return "exGeneral";
                else return "ext";
            }

        }
        static IPort defaultPortImpl;

        static public void understandPorts(IPort p)
        {
            defaultPortImpl = p;
        }

        public int seq { get; private set; }

        public Port port;
        // public IPort Port() { return port; }

        private MsgCode code;
        public MsgCode Code() { return code; }

        public void SetPort(string pport)
        {
            throw new AnsonException(-1,
                "Port can not be null. Not initialized? To use JMassage understand ports, call understandPorts(IPort) first.");
        }

        public AnsonMsg() { }

        public AnsonMsg(int code)
        {
            this.code = new MsgCode(code);
            seq = 1245;
        }

        public AnsonMsg(Port port)
        {
            this.port = port;
            seq = 1112;
        }

        /**Typically for response
         * @param p 
         * @param code
         */
        public AnsonMsg(Port p, MsgCode code)
        {
            this.port = p;
            this.code = code;
        }

        protected List<IAnsonBody> body;
        public IAnsonBody Body(int i) { return body[0]; }
        public IList Body() { return body; }

        /// <summary>Add a request body to the request list.</summary>
        /// <paramInfo cref='bodyItem'></paramInfo> 
        /// <return>new message object</return> 
        public AnsonMsg Body(IAnsonBody bodyItem)
        {
            if (body == null)
                body = new List<IAnsonBody>();
            body.Add(bodyItem);
            // bodyItem.Msg1(); // Why?
            // (bodyItem as AnSessionReq).Msg();
            return this;
        }

        public AnsonMsg IncSeq()
        {
            seq++;
            return this;
        }

        AnsonHeader header;
        public AnsonHeader Header() { return header; }
        public AnsonMsg Header(AnsonHeader header)
        {
            this.header = header;
            return this;
        }

        JsonOpt opts;
        public void Opts(JsonOpt readOpts) { this.opts = readOpts; }
        public JsonOpt Opts()
        {
            return opts ?? new JsonOpt();
        }

        public AnsonMsg Body(List<IAnsonBody> bodyItems)
        {
            body = bodyItems;
            return this;
        }

    }

    public class AnSessionReq : Anson, IAnsonBody
    {
        [AnsonField(refer=AnsonField.enclosing)]
        public AnsonMsg parent;
        public AnSessionReq()
        {
        }
        public AnSessionReq(AnsonMsg parent) { 
            this.parent = parent;
        }

        public string uid;
        string token;
        public string Token() { return token; }
        string iv;
        public string Iv() { return iv; }

        Hashtable mds;

        public string conn { get; set; }
        public string a { get; set; }

        public string md(string k) { return mds == null ? null : (string)mds[k]; }

        public AnsonMsg Msg()
        {
            return parent;
        }

        public IAnsonBody Msg(AnsonMsg p)
        {
            this.parent = p;
            return this;
        }

        public static AnsonMsg FormatLogin(string uid, string tk64, string iv64)
        {
            AnsonMsg jmsg = new AnsonMsg(new Port(Port.session));

            AnSessionReq itm = new AnSessionReq(parent: jmsg);
            itm.uid = uid;
            itm.A("login");

            itm.Setup(uid, tk64, iv64);

            jmsg.Body(itm);
            return jmsg;
        }

        private void Setup(string uid, string tk64, string iv64)
        {
            this.uid = uid;
            this.token = tk64;
            this.iv = iv64;
        }

        public IAnsonBody A(string act) {
            this.a = act;
            return this;
        }
    }
}
