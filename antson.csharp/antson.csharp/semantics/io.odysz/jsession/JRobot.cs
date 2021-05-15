//namespace io.odysz.semantic.jserv.jsession
//{
//	/// <summary>This robot is only used for test.</summary>
//	/// <remarks>
//	/// This robot is only used for test.
//	/// If you are implementin a servlet without login, subclassing a
//	/// <see cref="jserv.jsession.JUser">JUser</see>
//	/// instead.
//	/// </remarks>
//	/// <author>odys-z@github.com</author>
//	public class JRobot : IUser
//	{
//		internal long touched;

//		public virtual System.Collections.Generic.List<string> dbLog(System.Collections.Generic.List
//			<string> sqls)
//		{
//			return null;
//		}

//		/// <exception cref="io.odysz.transact.x.TransException"/>
//		public virtual bool login(object request)
//		{
//			return true;
//		}

//		public virtual void touch()
//		{
//			touched = Sharpen.Runtime.currentTimeMillis();
//		}

//		public virtual long touchedMs()
//		{
//			return touched;
//		}

//		public virtual string uid()
//		{
//			return "jrobot";
//		}

//		public virtual io.odysz.semantics.SemanticObject logout()
//		{
//			return null;
//		}

//		/// <exception cref="System.IO.IOException"/>
//		public virtual void writeJsonRespValue(object writer)
//		{
//		}

//		public virtual io.odysz.semantics.IUser logAct(string funcName, string funcId)
//		{
//			return this;
//		}

//		public virtual string sessionId()
//		{
//			return null;
//		}

//		public virtual io.odysz.semantics.IUser sessionId(string skey)
//		{
//			return null;
//		}

//		/// <exception cref="io.odysz.transact.x.TransException"/>
//		public virtual io.odysz.semantics.IUser notify(object note)
//		{
//			return null;
//		}

//		public virtual System.Collections.Generic.IList<object> notifies()
//		{
//			return null;
//		}

//		public virtual io.odysz.semantics.meta.TableMeta meta()
//		{
//			return null;
//		}
//	}
//}
