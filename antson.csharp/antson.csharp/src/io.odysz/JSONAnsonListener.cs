using Antlr4.Runtime;
using Antlr4.Runtime.Tree;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using static JSONParser;

namespace io.odysz.anson
{
    class JSONAnsonListener : JSONBaseListener, IParseTreeListener
	{
		/**<p>Parsing AST node's context, for handling the node's value,
         * the element class of parsing stack.</p>
         * <p>Memo: The hard lesson learned from this is if you want parse a grammar,
         * you better follow the grammar structure.</p>
         * @author odys-z@github.com
         */
		public class ParsingCtx
		{
			/**The json prop (object key) */
			internal string parsingProp;
			/**The parsed native value */
			internal object parsedVal;
			/**Parent AST node */
			internal object enclosing;
			/**Fields' map<string, FieldInfo>. C# has an extra properties' map */
			internal Hashtable fmap;
			/**Properties' map<string, PropertyInfo>. Java doesn't has this map */
			internal Hashtable pmap;
			/** Annotation's main types */
			internal string valType;
			/** Annotation's sub types */
			internal string subTypes;

			internal ParsingCtx(Hashtable fmap, IJsonable enclosing)
			{
				this.fmap = fmap;
				this.enclosing = enclosing;
			}

			internal ParsingCtx(Hashtable fmap, Hashtable enclosing)
			{
				this.fmap = fmap;
				this.enclosing = enclosing;
			}

			internal ParsingCtx(Hashtable fmap, IEnumerable enclosing)
			{
				this.fmap = fmap;
				this.enclosing = enclosing;
			}

			internal bool IsInList()
			{	// return enclosing instanceof List || enclosing.getClass().isArray();
				return typeof(IEnumerable).IsAssignableFrom(enclosing.GetType());
			}

			internal bool IsInMap()
			{	// return enclosing instanceof Hashtable;
				return typeof(Hashtable).IsAssignableFrom(enclosing.GetType());
			}

			/**Set type annotation.<br>
			 * annotation is value of {@link AnsonField#valType()}
			 * @param tn
			 * @return
			 */
			internal ParsingCtx ElemType(string[] tn)
			{
				valType = tn == null || tn.Length <= 0 ? null : tn[0];
				subTypes = tn == null || tn.Length <= 1 ? null : tn[1];

				if (!string.IsNullOrEmpty(valType))
				{
					// change / replace array type
					// e.g. lang.String[] to [Llang.String;
					if (Regex.Match(valType, @".*\[\]$").Success)
					{
						// valType = "[L" + valType.replaceAll("\\[\\]$", ";");
						valType = "[L" + Regex.Replace(valType, @"\[\]$", ";");
						// valType = valType.replaceFirst(" ^\\[L\\[", "[[");
						valType = Regex.Replace(valType, @" ^\[L\[", "[[");
					}
				}
				return this;
			}

			/**Get type annotation
			 * @return {@link AnsonField#valType()} annotation
			 */
			string elemType() { return valType; }

			string SubTypes() { return subTypes; }
		}

		internal static void MergeFields(Type type, Hashtable fmap, Hashtable pmap)
        {
            FieldInfo[] flist = type.GetFields();
            foreach (FieldInfo f in flist)
			{
                if (
                    f.IsInitOnly || f.IsLiteral || f.IsStatic)
                    continue;
                // Overriden
                if (fmap.ContainsKey(f.Name))
                    continue;
                fmap[f.Name] = f;
            }

            PropertyInfo[] plist = type.GetProperties();
            foreach (PropertyInfo p in plist)
			{
                if (pmap.ContainsKey(p.Name))
                    continue;
                pmap[p.Name] = p;
            }

			Type pclz = type.BaseType;
			if (pclz != null && typeof(IJsonable).IsAssignableFrom(pclz))
				MergeFields(pclz, fmap, pmap);
        }

		/// ///////////////////////////////////////////////////////////////////////////

		private List<ParsingCtx> stack;

		private ParsingCtx Top() { return stack[0]; }

		/**Push parsing node (a envelope, map, list) into this' {@link #stack}.
         * @param enclosingClazz new parsing IJsonable object's class
         * @param elemType type annotation of enclosing list/array. 0: main type, 1: sub-types<br>
         * This parameter can't be null if is pushing a list node.
         * @throws ReflectiveOperationException
         * @throws SecurityException
         * @throws AnsonException
         */
		private void Push(Type enclosingClazz, string[] elemType)
		{
            if (enclosingClazz.IsArray) {
				Hashtable fmap = new Hashtable();
				ParsingCtx newCtx = new ParsingCtx(fmap, new List<object>());
				stack.Insert(0, newCtx.ElemType(elemType));
            }
            else {
                Hashtable fmap = new Hashtable();
                if (typeof(IEnumerable).IsAssignableFrom(enclosingClazz)) {
                    IList enclosing = new List<object>();
					stack.Insert(0, new ParsingCtx(fmap, enclosing).ElemType(elemType));
                }
                else
                {
                    ConstructorInfo ctor = enclosingClazz.GetConstructor(Type.EmptyTypes);
                    //try
                    //{
                    //    ctor = enclosingClazz.GetConstructor(Type.EmptyTypes);
                    //}
                    //catch (NoSuchMethodException e)
                    //{
                    //    throw new AnsonException(0, "To make json can be parsed to %s, the class must has a default public constructor(0 parameter)\n"
                    //            + "Also, inner class must be static.\n"
                    //            + "Class.getConstructor() error on getting: %s %s",
                    //            enclosingClazz.getName(), e.getMessage(), e.getClass().getName());
                    //}
                    if (ctor != null && typeof(IJsonable).IsAssignableFrom(enclosingClazz)) {
						MergeFields(enclosingClazz, fmap, null); // map merging is only needed by typed object

                        IJsonable enclosing = (IJsonable)Activator.CreateInstance(enclosingClazz);
                        stack.Insert(0, new ParsingCtx(fmap, enclosing));
                    //try
                    //{
                    //    IJsonable enclosing = (IJsonable)Activator.CreateInstance(enclosingClazz);
                    //    stack.Insert(0, new ParsingCtx(fmap, enclosing));
                    //}
                    //catch (InvocationTargetException e)
                    //{
                    //    throw new AnsonException(0, "Failed to create instance of IJsonable with\nconstructor: %s\n"
                    //        + "class: %s\nerror: %s\nmessage: %s\n"
                    //        + "Make sure the object can be created with the constructor.",
                    //        ctor, enclosingClazz.getName(), e.getClass().getName(), e.getMessage());
                    //}
                    }
                    else
                    {
                        Hashtable enclosing = new Hashtable();
                        ParsingCtx top = new ParsingCtx(fmap, enclosing);
                        stack.Insert(0, top);
                    }
                }
            }
        }

        private ParsingCtx Pop()
        {
			ParsingCtx top = stack[0];
			stack.RemoveAt(0);
            return top;
        }

        public override void ExitObj(ObjContext ctx)
        {
            ParsingCtx top = Pop();
            Top().parsedVal = top.enclosing;
            top.enclosing = null;
        }

        public override void EnterObj(ObjContext ctx)
        {
            ParsingCtx top = Top();
            try
            {
                Hashtable fmap = stack.Count > 0 ? top.fmap : null;

                if (fmap == null || !fmap.ContainsKey(top.parsingProp))
                {
                    // In a list, found object, if not type specified with annotation, must failed.
                    // But this is confusing to user. Set some report here.
                    if (top.IsInList() || top.IsInMap())
                        Console.Error.WriteLine(string.Format(
                            "Type of elements in list or map is complicate, but no annotation for type info can be found.\n"
                            + "field type: {0}\njson: {1}\n"
                            + "E.g. Java field example: @AnsonField(valType=\"io.your.type\")\n"
                            + "Anson instances don't need annotation, but objects in json array without type-pair can also trigger this error report.",
                            top.enclosing.GetType(), ctx.GetText()));
                    throw new AnsonException(0, "Obj type not found. property: {0}", top.parsingProp);
                }

                Type ft = fmap[top.parsingProp].GetType();
                if (typeof(Hashtable).IsAssignableFrom(ft)) {
                    // entering a map
                    Push(ft, null);
                    // append annotation
                    FieldInfo f = (FieldInfo)top.fmap[top.parsingProp];
                    AnsonField a = (AnsonField)(f?.GetCustomAttribute(typeof(AnsonField)));
                    string anno = a?.valType;

                    if (anno != null) {
                        string[] tn = ParseElemType(anno);
                        Top().ElemType(tn);
                    }
                }
                else
                    // entering an envelope
                    // push(fmap.get(top.parsingProp).getType());
                    Push(ft, null);
            } catch (AnsonException e) {
                Console.WriteLine(e.StackTrace);
            } catch (Exception e) {
                Console.WriteLine(e.StackTrace);
            }
        }

        internal IJsonable ParsedEnvelope()
        {
			if (stack == null || stack.Count == 0)
				throw new AnsonException(0, "No envelope is avaliable.");
			return stack[0].enclosing as IJsonable;
		}

		/**Envelope Type Name */
		protected string envetype;

		public override void EnterEnvelope(EnvelopeContext ctx)
		{
			if (stack == null)
			{
				stack = new List<ParsingCtx>();
			}
			envetype = null;
		}

		public override void ExitEnvelope(EnvelopeContext ctx)
		{
			base.ExitEnvelope(ctx);
			if (stack.Count > 1)
			{
				ParsingCtx tp = Pop();
				Top().parsedVal = tp.enclosing;
			}
			// else keep last one (root) as return value
		}

        private static string[] ParseElemType(String subTypes)
        {
            if (string.IsNullOrEmpty(subTypes))
                return null;
            return subTypes.Split('/');
        }


    }
}
