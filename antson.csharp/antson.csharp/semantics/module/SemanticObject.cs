using io.odysz.anson;
using io.odysz.transact.x;
using System;
using System.Collections.Generic;

namespace io.odysz.semantics
{
	/// <summary>
	/// <p>The semantics data used internally by semantic-DA to handle semantics configuration.</p>
	/// <p>SemanticObject implement methods to write itself as a json value with a writer provided by the caller.
	/// </summary>
	/// <remarks>
	/// <p>The semantics data used internally by semantic-DA to handle semantics configuration.</p>
	/// <p>SemanticObject implement methods to write itself as a json value with a writer provided by the caller.
	/// This can be used to write the object into other structured object.</p>
	/// <p><b>Note:</b> The equivalent of JsonObject in a request is JMessage.
	/// <p>Question: If a json request object is handled by a port, e.g. SQuery,
	/// is their any property name not known by the port?</p>
	/// <p>If no such properties, then there shouldn't be put() and get().</p>
	/// </remarks>
	/// <author>odys-z@github.com</author>
	public class SemanticObject : Anson
	{
		public Dictionary<string, object> props { get; set; }

		/// <param name="prop"/>
		/// <returns>null if the property doesn't exists</returns>
		public virtual Type getType(string prop)
		{
			if (prop == null || props == null || !props.ContainsKey(prop))
			{
				return null;
			}
			object p = props[prop];
			return p == null ? null : p.GetType();
		}

		// has key, no value
		public virtual bool has(string prop)
		{
			return props != null && props.ContainsKey(prop) && props[prop] != null;
		}

		public virtual object get(string prop)
		{
			return props == null ? null : props[prop];
		}

		public virtual string getString(string prop)
		{
			return props == null ? null : (string)props[prop];
		}

		public virtual SemanticObject data()
		{
			return (SemanticObject)get("data");
		}

		public virtual SemanticObject data(SemanticObject data)
		{
			return put("data", data);
		}

		public virtual string port()
		{
			return (string)get("port");
		}

		public virtual SemanticObject code(string c)
		{
			return put("code", c);
		}

		public virtual string code()
		{
			return (string)get("code");
		}

		public virtual SemanticObject port(string port)
		{
			return put("port", port);
		}

		public virtual string msg()
		{
			return (string)get("msg");
		}

		public virtual SemanticObject msg(string msg, params object[] args)
		{
			if (args == null || args.Length == 0)
			{
				return put("msg", msg);
			}
			else
			{
				return put("msg", string.Format(msg, args));
			}
		}

		/// <summary>Put resultset (SResultset) into "rs".</summary>
		/// <remarks>
		/// Put resultset (SResultset) into "rs".
		/// Useing this should be careful as the rs is a 3d array.
		/// </remarks>
		/// <param name="resultset"/>
		/// <param name="total"></param>
		/// <returns>this</returns>
		/// <exception cref="TransException"/>
		public virtual SemanticObject rs(object resultset, int total)
		{
			add("total", total);
			return add("rs", resultset);
		}

		public virtual object rs(int i)
		{
			return ((List<object>)get("rs"))[i];
		}

		public virtual int total(int i)
		{
			if (get("total") == null)
			{
				return -1;
			}
			List<object> lst = ((List<object
				>)get("total"));
			if (lst == null || lst.Count <= i)
			{
				return -1;
			}
			object obj = lst[i];
			if (obj == null)
			{
				return -1;
			}
			return (int)obj;
		}

		/// <exception cref="TransException"/>
		public virtual SemanticObject total(int rsIdx, int total)
		{
			// the total(int) returned -1
			if (total < 0)
			{
				return this;
			}
			List<int> lst = (List<int>)
				get("total");
			if (lst == null || lst.Count <= rsIdx)
			{
				throw new TransException("No such index for rs; %s", rsIdx);
			}
			lst.Insert(rsIdx, total);
			return this;
		}

		public virtual string error()
		{
			return (string)get("error");
		}

		public virtual SemanticObject error(string error, params object[] args)
		{
			if (args == null || args.Length == 0)
			{
				return put("error", error);
			}
			else
			{
				return put("error", string.Format(error, args));
			}
		}

		public virtual SemanticObject put(string prop, object v)
		{
			if (props == null)
			{
				props = new Dictionary<string, object>();
			}
			props[prop] = v;
			return this;
		}

		/// <summary>Add element 'elem' to array 'prop'.</summary>
		/// <param name="prop"/>
		/// <param name="elem"/>
		/// <returns>this</returns>
		/// <exception cref="TransException"></exception>
		public virtual SemanticObject add(string prop, object elem)
		{
			if (props == null)
			{
				props = new Dictionary<string, object>();
			}
			if (!props.ContainsKey(prop))
			{
				props[prop] = new List<object>();
			}
			if (props[prop] is System.Collections.IList)
			{
				((List<object>)props[prop]).Add(elem);
			}
			else
			{
				throw new TransException("%s seams is not an array. elem %s can't been added",
										prop, elem);
			}
			return this;
		}

		/// <summary>Add int array.</summary>
		/// <param name="prop"/>
		/// <param name="ints"/>
		/// <returns>this</returns>
		/// <exception cref="TransException"/>
		public virtual SemanticObject addInts(string prop, int[] ints)
		{
			foreach (int e in ints)
			{
				add(prop, e);
			}
			return this;
		}

		public virtual object remove(string prop)
		{
			if (props != null && props.ContainsKey(prop))
			{
				return props.Remove(prop);
			}
			else
			{
				return null;
			}
		}

		/// <summary>Print for reading - string can't been converted back to object</summary>
		/// <param name="out"/>
		//public virtual void print(System.IO.TextWriter @out)
		//{
		//	if (props != null)
		//	{
		//		foreach (string k in props.Keys)
		//		{
		//			@out.Write(k);
		//			@out.Write(" : ");
		//			Type c = getType(k);
		//			if (c == null)
		//			{
		//				continue;
		//			}
		//			else
		//			{
		//				if (c.isAssignableFrom(Sharpen.Runtime.getClassForType(typeof(SemanticObject
		//					))) || Sharpen.Runtime.getClassForType(typeof(SemanticObject)
		//					).isAssignableFrom(c))
		//				{
		//					((SemanticObject)get(k)).print(@out);
		//				}
		//				else
		//				{
		//					if (Sharpen.Runtime.getClassForType(typeof(System.Collections.ICollection)).isAssignableFrom
		//						(c) || Sharpen.Runtime.getClassForType(typeof(System.Collections.IDictionary)).isAssignableFrom
		//						(c))
		//					{
		//						IEnumerator<object> i = ((ICollection
		//							<object>)get(k)).GetEnumerator();
		//						@out.WriteLine("[" + ((ICollection<object>)get(k)).Count
		//							 + "]");
		//						while (i.MoveNext())
		//						{
		//							object ele = i.Current;
		//							c = Sharpen.Runtime.getClassForObject(ele);
		//							if (c.isAssignableFrom(Sharpen.Runtime.getClassForType(typeof(SemanticObject
		//								))) || Sharpen.Runtime.getClassForType(typeof(SemanticObject)
		//								).isAssignableFrom(c))
		//							{
		//								((SemanticObject)ele).print(@out);
		//							}
		//							else
		//							{
		//								@out.Write(get(k));
		//							}
		//						}
		//					}
		//					else
		//					{
		//						@out.Write(get(k));
		//					}
		//				}
		//			}
		//			@out.Write(",\t");
		//		}
		//	}
		//	@out.WriteLine(string.Empty);
		//}
	}
}