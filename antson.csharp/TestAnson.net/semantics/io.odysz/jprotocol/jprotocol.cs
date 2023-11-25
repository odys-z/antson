using io.odysz.semantics;
using static io.odysz.semantic.jprotocol.AnsonMsg;

namespace io.odysz.semantic.jprotocol
{
	public class JProtocol
	{
		/// <summary>Typical operation's common names</summary>
		public class CRUD
		{
			public const string C = "I";

			public const string R = "R";

			public const string U = "U";

			public const string D = "D";
		}

        public interface OnOk
        {
            void ok(AnsonResp resp);
        }

        /**
         * Progress notifier called by block chain.
         * Parameter blockResp provide the last uploaded block's sequence number.
         */
        public interface OnProcess
        {
            void proc(int listIndx, int totalBlocks, AnsonResp blockResp);
        }

        public interface OnError { void err(MsgCode ok, string msg, string[] args = null ); }


		public interface SCallbackV11
		{
			/// <summary>call back function called by semantic.transact</summary>
			/// <param name="msgCode">'ok' | 'ex...'</param>
			/// <param name="resp">response message</param>
			/// <exception cref="System.IO.IOException"/>
			/// <exception cref="java.sql.SQLException"/>
			/// <exception cref="SemanticException"/>
			/// <exception cref="AnException"/>
			void onCallback(MsgCode msgCode, AnsonResp resp);
		}

		public static SemanticObject err(IPort
			 port, string code, string err)
		{
			SemanticObject obj = new SemanticObject();
			obj.put("code", code);
			obj.put("error", err);
			obj.put("port", port.name);
			return obj;
		}

		public static SemanticObject ok(IPort
			 port, object data)
		{
			SemanticObject obj = new SemanticObject();
			obj.put("code", MsgCode.ok.ToString());
			obj.put("data", data);
			obj.put("port", port.name);
			return obj;
		}

		public static SemanticObject ok(IPort
			 port, string msg, params object[] msgArgs)
		{
			return ok(port, string.Format(msg, msgArgs));
		}

		//////////////////////// version 1.1 with support of Anson //////////////////////
		//public static AnsonMsg<AnsonResp> err(AnsonMsg<T>.Port port, AnsonMsg<T>.MsgCode code, string err)
		//	where T : AnsonBody
		//{
		//	AnsonResp obj = new AnsonResp
		//		(err);
		//	AnsonMsg<AnsonResp> msg = 
		//		new AnsonMsg<AnsonResp>(
		//		port, code).body(obj);
		//	return msg;
		//}
	}
}
