using Antlr4.Runtime.Tree;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq.Expressions;
using System.Reflection;
using System.Text.RegularExpressions;
using static JSONParser;

namespace io.odysz.anson
{
    public class JSONAnsonListener : JSONBaseListener, IParseTreeListener
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
			internal dynamic parsedVal;
			/**Parent AST node */
			internal dynamic enclosing;
			/**Fields' map<string, FieldInfo>. C# has an extra properties' map */
			internal Hashtable fmap;
			/**Properties' map<string, PropertyInfo>. Java doesn't has this map */
			internal Hashtable pmap;
			/** Annotation's main types */
			internal string valType;
			/** Annotation's sub types */
			internal string subTypes;

            /*
			internal ParsingCtx(Hashtable fmap, Hashtable pmap, IJsonable enclosing)
			{
				this.fmap = fmap;
				this.pmap = pmap;
				this.enclosing = enclosing;
			}

			internal ParsingCtx(Hashtable fmap, Hashtable pmap, Hashtable enclosing)
			{
				this.fmap = fmap;
				this.pmap = pmap;
				this.enclosing = enclosing;
			}

			internal ParsingCtx(Hashtable fmap, Hashtable pmap, IEnumerable enclosing)
			{
				this.fmap = fmap;
				this.pmap = pmap;
				this.enclosing = enclosing;
			}
            */
			internal ParsingCtx(Hashtable fmap, Hashtable pmap, dynamic enclosing)
			{
				this.fmap = fmap;
				this.pmap = pmap;
				this.enclosing = enclosing;
			}

			internal bool IsInList()
			{
                // return enclosing instanceof List || enclosing.getClass().isArray();
                Type t = enclosing.GetType();
                return (t.Name == "IList`1" || t.GetInterface("IList`1") != null)
                        || t.IsArray;
				//return ! typeof(Hashtable).IsAssignableFrom(enclosing.GetType())
    //                  && typeof(IEnumerable).IsAssignableFrom(enclosing.GetType());
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
				// valType = tn == null || tn.Length <= 0 ? null : tn[0];
				// subTypes = tn == null || tn.Length <= 1 ? null : tn[1];
				valType = tn == null || tn.Length <= 0 ? null : tn[0];

				if (!string.IsNullOrEmpty(valType))
				{
					// change / replace array type
					// e.g. lang.String[] to [Llang.String;
					if (Regex.Match(valType, @".*\[\]$").Success)
					{
                        /* keep string[] in java style
						 * valType = "[L" + valType.replaceAll("\\[\\]$", ";");
						 * valType = valType.replaceFirst(" ^\\[L\\[", "[[");
                         */
						valType = "[L" + Regex.Replace(valType, @"\[\]$", ";");
						valType = Regex.Replace(valType, @"^\[L\[", "[[");
					}
				}
				return this;
			}

            /**Get type annotation
			 * @return {@link AnsonField#valType()} annotation
			 */
            internal string ElemType() { return valType; }

			internal string SubTypes() { return subTypes; }
		}

		internal static void MergeFields(Type type, Hashtable fmap, Hashtable pmap)
        {
            FieldInfo[] flist = type.GetFields(BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance);
            foreach (FieldInfo f in flist)
			{
                if (f.IsInitOnly || f.IsLiteral || f.IsStatic)
                    continue;

                // Overriden
                if (fmap.ContainsKey(f.Name))
                    continue;
                fmap[f.Name] = f;
            }

            PropertyInfo[] plist = type.GetProperties(BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance);
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
        /// <summary>Assembly name that additional types provided, e.g. for AnsonMsg</summary>
        private static string assmName = "";
        public static void setAssembly(string assName)
        {
            assmName = assName;
        }

		private List<ParsingCtx> stack;

		private ParsingCtx Top() { return stack[0]; }

		/// <summary>Push parsing node (a envelope, map, list) into this' {@link #stack}.
        /// </summary> 
        /// @param enclosingClazz new parsing IJsonable object's class
        /// @param elemType type annotation of enclosing list/array. 0: main type, 1: sub-types<br>
        /// This parameter can't be null if is pushing a list node.
        /// @throws ReflectiveOperationException
        /// @throws SecurityException
        /// @throws AnsonException
		private void Push(Type enclosingClazz, string[] elemType)
		{
            if (enclosingClazz.IsArray) {
				Hashtable fmap = new Hashtable();
				Hashtable pmap = new Hashtable();
				ParsingCtx newCtx = new ParsingCtx(fmap, pmap, new List<object>());
				stack.Insert(0, newCtx.ElemType(elemType));
            }
            else {
                Hashtable fmap = new Hashtable();
                Hashtable pmap = new Hashtable();

                // TODO refactor java's branch sequence
                if ( !typeof(Hashtable).IsAssignableFrom(enclosingClazz)
                    && !(enclosingClazz.Name == "Dictionary`2" || enclosingClazz.GetInterface("Dictionary`2") != null)
                    && typeof(IEnumerable).IsAssignableFrom(enclosingClazz))
                {   // branch: List, Array
                    // IList enclosing = new List<object>();
                    IList enclosing = null;
                    if (elemType == null || elemType.Length == 0)
                        enclosing = new List<object>();
                    else
                    {
                        Type cstype = CSharp.CSType(elemType[0]);
                        Type lstype = typeof(List<>).MakeGenericType(cstype);
                        enclosing = (IList)Activator.CreateInstance(lstype);
                    }
                    stack.Insert(0, new ParsingCtx(fmap, pmap, enclosing).ElemType(elemType));
                }
                else
                {   // branch: Map, Anston
                    ConstructorInfo ctor = enclosingClazz.GetConstructor(Type.EmptyTypes);
                    if (!typeof(Hashtable).IsAssignableFrom(enclosingClazz))
                    {
                        if (ctor == null)
                            throw new AnsonException(0, "To make json can be parsed to {0}, the class must has a default public constructor(0 parameter)\n",
                                            enclosingClazz.FullName);

                        if (!enclosingClazz.IsGenericType)
                            MergeFields(enclosingClazz, fmap, pmap); // map merging is only needed by typed object

                        // IJsonable enclosing = (IJsonable)Activator.CreateInstance(enclosingClazz);
                        dynamic enclosing = Activator.CreateInstance(enclosingClazz);
                        stack.Insert(0, new ParsingCtx(fmap, pmap, enclosing).ElemType(elemType));
                    }
                    else // if (!typeof(Hashtable).IsAssignableFrom(enclosingClazz))
                    {
                        Hashtable enclosing = new Hashtable();
                        ParsingCtx top = new ParsingCtx(fmap, pmap, enclosing).ElemType(elemType);
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
                Hashtable pmap = stack.Count > 0 ? top.pmap : null;

                if (  (fmap == null || !fmap.ContainsKey(top.parsingProp))
                    &&(pmap == null || !pmap.ContainsKey(top.parsingProp)) )
                {
                    // In a list, found object, if not type specified with annotation, must failed.
                    // But this is confusing to user. Set some report here.
                    if (top.IsInList() || top.IsInMap())
                        Debug.WriteLine(string.Format(
                            "Type of elements in list or map is complicate, but no annotation for type info can be found.\n"
                            + "field type: {0}\njson: {1}\n"
                            + "E.g. Java field example: @AnsonField(valType=\"io.your.type\")\n"
                            + "Anson instances don't need annotation, but objects in json array without type-pair can also trigger this error report.",
                            top.enclosing.GetType(), ctx.GetText()));
                    throw new AnsonException(0, "Obj type not found. property: {0}", top.parsingProp);
                }

                string fpn = top.parsingProp;
                FieldInfo f = (FieldInfo)top.fmap?[fpn];
                PropertyInfo p = (PropertyInfo)(top.pmap?[fpn]);
                Type fpt = f != null ? f.FieldType : p?.PropertyType;

                if (typeof(Hashtable).IsAssignableFrom(fpt) ||
                   (fpt.Name == "Dictionary`2" || fpt.GetInterface("Dictionary`2") != null) ) {
                    // entering a map
                    // append annotation
                    AnsonField a = (AnsonField)((MemberInfo)f)?.GetCustomAttribute(typeof(AnsonField));
                    string anno = a?.valType;

                    if (anno != null) {
                        string[] tn = ParseElemType(anno);
                        Top().ElemType(tn);
                        Push(fpt, tn);
                    }
                    else
                        Push(fpt, null);
                }
                else
                    // entering an envelope
                    Push(fpt, null);
            } catch (AnsonException e) {
                Debug.WriteLine(e.StackTrace);
            } catch (Exception e) {
                Debug.WriteLine(e.StackTrace);
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

        private string EnvelopName()
        {
            if (stack != null)
                for (int i = 0; i < stack.Count; i++)
                    if (stack[i].GetType().IsAssignableFrom(typeof(Anson)))
                        return stack[i].enclosing.GetType().Name;
            return null;
        }

        private object Toparent(Type type)
        {
            // no enclosing, no parent
            if (stack.Count <= 1 || string.IsNullOrEmpty(type.Name))
                return null;

            // trace back, guess with type for children could be in array or map
            ParsingCtx p = stack[1];
            int i = 2;
            while (p != null)
            {
                if (type == p.enclosing.GetType())
                    return p.enclosing;
                p = stack[i];
                i++;
            }
            return null;
        }


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

		/**Semantics of entering a type pair is found and parsingVal an IJsonable object.<br>
         * This is always happening on entering an object.
         * The logic opposite is exit object.
         * @see gen.antlr.json.JSONBaseListener#enterType_pair(gen.antlr.json.JSONParser.Type_pairContext)
         */
        public override void EnterType_pair(Type_pairContext ctx)
        {
            if (envetype != null)
                // ignore this type specification, keep consist with java type
                return;

            // envetype = ctx.qualifiedName().getText();
            ITerminalNode str = ctx.qualifiedName().STRING();
            string txt = ctx.qualifiedName().GetText();
            envetype = GetStringVal(str, txt);
            Type clazz = CSharp.CSType(envetype);
            Push(clazz, null);
        }

        public override void EnterPair(PairContext ctx)
        {
            base.EnterPair(ctx);
            ParsingCtx top = Top();
            top.parsingProp = GetProp(ctx);
            top.parsedVal = null;
        }
 
        private static string[] ParseElemType(string subTypes)
        {
            if (string.IsNullOrEmpty(subTypes))
                return null;
            return subTypes.Split('/');
        }

        /// <summary>
        /// Parse list/array field's element type.
        /// </summary>
        /// <param name="f"></param>
        /// <returns>Type name and type parameters</returns>
		private static string[] ParseListElemType(MemberInfo f, Type fpt) {
			// for more information, see
			// https://stackoverflow.com/questions/1868333/how-can-i-determine-the-type-of-a-generic-field-in-java

			// Type ft = f.FieldType;
            AnsonField a = (AnsonField)((MemberInfo)f)?.GetCustomAttribute(typeof(AnsonField));
            string tn = a?.valType;
            if (tn != null)
            {
                // try annotation first - override default types
                return ParseElemType(tn);
            }
            else if (fpt.IsGenericType)
            {
                string[] ptypess = fpt.GetGenericArguments()[0].FullName.Split('<');
                if (ptypess.Length > 1)
                {
                    // ptypess[1] = ptypess[1].replaceFirst(">$", "");
                    // ptypess[1] = ptypess[1].replaceFirst("^L", "");
                    ptypess[1] = Regex.Replace(ptypess[1], ">$", "");
                    ptypess[1] = Regex.Replace(ptypess[1], "^L", "");
                }
                // figure out array element class
                else
                {
                    Type argType = fpt.GetGenericArguments()[0];
                    if (!argType.IsGenericParameter)
                    {
                        if (argType.IsArray)
                        {
                            ptypess = new string[] { ptypess[0], argType.GetElementType().Name };
                        }
                    }
                    // else nothing can do here for a type parameter, e.g. "T"
                    else
                        if (AnsonFlags.parser)
                        Debug.WriteLine(string.Format(
                                "[AnsonFlags.parser] warn Element type <{0}> for {1} is a type parameter ({2}) - ignored",
                                fpt.GetGenericArguments()[0],
                                f.Name,
                                fpt.GetGenericArguments()[0].GetType()));
                }
                return ptypess;
            }
            else if (fpt.IsArray)
            {
                // complex array may also has annotation
                string[] valss = ParseElemType(tn);

                string eleType = fpt.GetElementType().FullName;
                if (valss != null && !eleType.Equals(valss[0]))
                    Debug.WriteLine(string.Format(
                            "[JSONAnsonListener#parseListElemType()]: Field {0} is not annotated correctly.\n"
                            + "field parameter type: {1}, annotated element type: {2}, annotated sub-type: {3}",
                            f.Name, eleType, valss[0], valss[1]));

                if (valss != null && valss.Length > 1)
                    return new string[] { eleType, valss[1] };
                else return new string[] { eleType };
            }
            else return null;
        }

        /**Parse property name, tolerate enclosing quotes presenting or not.
         * @param ctx
         * @return
         */
        private static string GetProp(PairContext ctx)
        {
            ITerminalNode p = ctx.propname().IDENTIFIER();
            return p == null ?
                    ctx.propname().STRING() != null ? 
                    Regex.Replace(ctx.propname().STRING().GetText(), "(^\\s*\"\\s*)|(\\s*\"\\s*$)", "")
                    : Regex.Replace(ctx.propname().GetText(), "(^\\s*\"\\s*)|(\\s*\"\\s*$)", "")
                    : p.GetText();
        }

        /**Convert json value : STRING | NUMBER | 'true' | 'false' | 'null' to java.lang.String.<br>
         * Can't handle NUMBER | obj | array.
         * @param ctx
         * @return value in string
         */
        private static string GetStringVal(PairContext ctx)
        {
            ITerminalNode str = ctx.value().STRING();
            string txt = ctx.value().GetText();
            return GetStringVal(str, txt);
        }

        private static string GetStringVal(ITerminalNode str, string rawTxt)
        {
            if (str == null)
            {
                try
                {
                    if (string.IsNullOrEmpty(rawTxt))
                        return null;
                    else if ("null" == rawTxt)
                        return null;
                }
                catch (Exception e) { }
                return rawTxt;
            }
            else return Regex.Replace(str.GetText(), "(^\\s*\")|(\"\\s*$)", "");
        }

        /**
         * grammar:<pre>value
        : STRING
        | NUMBER
        | obj		// all array's obj value can't parsed as Anson, taken as HashMap - TODO doc: known issue
        | envelope
        | array
        | 'true'
        | 'false'
        | 'null'
        ;</pre>
         * @param ctx
         * @return simple value (STRING, NUMBER, 'true', 'false', null)
         */
        private static object FigureJsonVal(ValueContext ctx)
        {
            string txt = ctx.GetText();
            if (txt == null)
                return null;
            else if (ctx.NUMBER() != null)
                try { return int.Parse(txt); }
                catch (Exception e)
                {
                    try { return float.Parse(txt); }
                    catch (Exception e1)
                    {
                        return double.Parse(txt);
                    }
                }
            else if (ctx.STRING() != null)
                return GetStringVal(ctx.STRING(), txt);
            else if (txt != null && txt.ToLower() == "true")
                return true;
            else if (txt != null && txt.ToLower() == "flase")
                return false;
            return null;
        }

        public override void EnterArray(ArrayContext ctx)
        {
            try
            {
                ParsingCtx top = Top();

                // if in a list or a map, parse top's sub-type as the new node's value type
                if (top.IsInList() || top.IsInMap())
                {
                    // pushing ArrayList.class because entering array, isInMap() == true means needing to figure out value type
                    //
                    string[] tn = ParseElemType(top.subTypes);
                    // ctx:		[{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]
                    // subtype:	io.odysz.anson.Anson
                    // tn :		[io.odysz.anson.Anson]
                    Push(typeof(IList), tn);
                }
                // if field available, parse field's value type as the new node's value type
                else
                {
                    FieldInfo f = (FieldInfo)top.fmap[top.parsingProp];
                    PropertyInfo p = (PropertyInfo)top.pmap[top.parsingProp];
                    Type ft = f?.FieldType;
                    ft = ft ?? p?.PropertyType;
                    // AnsT3 { ArrayList<Anson[]> ms; }
                    // ctx: [[{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]]
                    // [0]: io.odysz.anson.Anson[],
                    // [1]: io.odysz.anson.Anson
                    if (f != null || p != null)
                    {
                        string[] tn = ParseListElemType((MemberInfo)f ?? p, ft);
                        Push(ft, tn);
                    }
                    else 
                        Push(ft??typeof(IList), null);
                }
            } catch (AnsonException e) {
                Debug.WriteLine(e.StackTrace);
            } catch (Exception e) {
                Debug.WriteLine(e.StackTrace);
            }
        }

        public override void ExitArray(ArrayContext ctx)  /// <summary>quit list/array enclosing</summary>
        {
            if (!Top().IsInList())
                throw new NullReferenceException("existing not from an eclosing list. txt:\n" + ctx.GetText());

            ParsingCtx top = Pop();
            IList arr = (IList)top.enclosing;

            //dynamic arr;
            //if (top.valType != null)
            //{
            //    // arr = Convert.ChangeType(top.enclosing, lstType);
            //    Type lstType = typeof(List<>).MakeGenericType(new Type[] { CSType(top.valType) });
            //    arr = (IList)Activator.CreateInstance(lstType);

            //    // arr = CastList(lstType, top.enclosing);
            //    foreach (dynamic e in top.enclosing) {
            //        dynamic arrEle = CastList(CSType(top.valType), e);
            //        arr.Add(arrEle);
            //    }
            //}
            //else
            //{
            //    arr = (IList)top.enclosing;
            //}

            top = Top();
            top.parsedVal = arr;

			// figure the type if possible - convert to array
			string et = top.ElemType();
            /* As we are using cast-list in c#, can this convertion correct?
             */
            if (!string.IsNullOrEmpty(et))
                try
                {
                    Type arrClzz = CSharp.CSType(et);
                    if (arrClzz.IsArray)
                        top.parsedVal = ToPrimitiveArray(arr, arrClzz);
                }
                catch (AnsonException e)
                {
                    Debug.WriteLine(string.Format(
                            "Error: Trying convert array to annotated type failed.\ntype: {0}\njson: {1}\nerror: {2}",
                            et, ctx.GetText(), e.Message));
                }
                catch (Exception e)
                {
                    Debug.WriteLine(string.Format(
                            "Error: Trying convert array to annotated type failed.\ntype: {0}\njson: {1}\nerror: {2}",
                            et, ctx.GetText(), e.Message));
                }
            // No annotation, for 2d list, parsed value is still a list.
            // If enclosed element of array is also an array, it can not been handled here
            // Because there is no clue for sub array's type if annotation is empty
        }

        /// <summary>
        /**
         * @param  list      the List to convert to a primitive array
         * @param  arrType the primitive array type to convert to
         * @param  <P>       the primitive array type to convert to
         * @return an array of P with the elements of the specified List
         * @throws AnsonException list element class doesn't equal array element type - not enough annotation?
         * @throws NullPointerException
         *         if either of the arguments are null, or if any of the elements
         *         of the List are null
         * @throws IllegalArgumentException
         *         if the specified Class does not represent an array type, if
         *         the component type of the specified Class is not a primitive
         *         type, or if the elements of the specified List can not be
         *         stored in an array of type P
         */
        /// Unboxes a List in to a primitive array.
        /// reference:
        /// https://stackoverflow.com/questions/25149412/how-to-convert-listt-to-array-t-for-primitive-types-using-generic-method
        /// </summary>
        private static Array ToPrimitiveArray (IList list, Type arrType)
        {
            if (!arrType.IsArray) {
                throw new AnsonException("Illegal type argument: " + arrType.ToString());
            }
            if (list == null)
                return null;

            Type eleType = arrType.GetElementType();

            Array array = Array.CreateInstance(eleType, list.Count);

            for (int i = 0; i < list.Count; i++) {
                object lstItem = list[i];
                if (lstItem == null)
                    continue;

                // this guess is error prone, let's tell user why. May be more annotation is needed
                if (!eleType.IsAssignableFrom(lstItem.GetType()))
                    throw new AnsonException(1, "Set element (v: {0}, type {1}) to array of type of \"{2}[]\" failed.\n"
                            + "Array element's type not annotated?",
                            lstItem.ToString(), lstItem.GetType().FullName, eleType.FullName);

                array.SetValue(list[i], i);
            }

            return array;
        }

        /**
         * grammar:<pre>value
        : STRING
        | NUMBER
        | obj		// all array's obj value can't parsed as Anson, taken as HashMap - TODO doc: known issue
        | envelope
        | array
        | 'true'
        | 'false'
        | 'null'
        ;</pre>
         * @see gen.antlr.json.JSONBaseListener#exitValue(gen.antlr.json.JSONParser.ValueContext)
         */
        public override void ExitValue(ValueContext ctx)
        {
            ParsingCtx top = Top();
            if (top.IsInList() || top.IsInMap())
            {
                // if in a map, parsingProp is the map key,
                // element type can only been handled with a guess,
                // or according to annotation
                if (top.IsInList())
                {
                    IList enclosLst = (IList)top.enclosing;
                    // for List, ft is not null
                    if (top.parsedVal == null)
                    {
                        // simple value like String or number
                        enclosLst.Add(FigureJsonVal(ctx));
                    }
                    else
                    {
                        // try figure out is element also an array if enclosing object is an array
                        // e.g. convert elements of List<String> to String[]
                        // FIXME issue: if the first element is 0 length, it will failed to convert the array
                        Type parsedClzz = top.parsedVal.GetType();
                        if (typeof(IList).IsAssignableFrom(parsedClzz))
                        // if (parsedClzz.Name == "IList`1" ||parsedClzz.GetInterface("IList`1") != null)
                        {
                            if (string.IsNullOrEmpty(top.ElemType())) // (LangExt.isblank(top.elemType(), "\\?.*"))
                            {
                                // change list to array
                                IList lst = (IList)top.parsedVal;
                                if (lst != null && lst.Count > 0)
                                {
                                    // search first non-null element's type
                                    Type eleClz = null;
                                    int ix = 0;
                                    while (ix < lst.Count && lst[ix] == null)
                                        ix++;
                                    if (ix < lst.Count)
                                        eleClz = lst[ix].GetType();

                                    if (eleClz != null)
                                    {
                                        try
                                        {
                                            enclosLst.Add(ToPrimitiveArray(lst,
                                                Array.CreateInstance(eleClz, 0).GetType()));
                                        }
                                        catch (AnsonException e)
                                        {
                                            Debug.WriteLine(string.Format(
                                                "Trying convert array to annotated type failed.\nenclosing: {0}\njson: {1}\nerror: {2}",
                                                top.enclosing, ctx.GetText(), e.Message));
                                        }

                                        // remember elem type for later null element
                                        top.ElemType(new string[] { eleClz.Name });
                                    }
                                    // all elements are null, ignore the list is the only way
                                }
                                else
                                    enclosLst.Add(lst);
                            }
                            // branch: with annotation or type name already figured out from 1st element
                            else
                            {
                                try
                                {
                                    IList parsedLst = (IList)top.parsedVal;
                                    string eleType = top.ElemType();
                                    Type eleClz = CSharp.CSType(eleType);
                                    if (eleClz.IsAssignableFrom(parsedClzz))
                                    {
                                        // annotated element can be this branch
                                        enclosLst.Add(parsedLst);
                                    }
                                    else
                                    {
                                        // type is figured out from the previous element,
                                        // needing conversion to array
                                        //
                                        // Bug: object value can't been set into string array
                                        // lst.getClass().getTypeName() = java.lang.ArrayList
                                        // ["val",88.91669145042222]

                                        // Test case:	AnsT3 { ArrayList<Anson[]> ms; }
                                        // ctx: 		[{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]
                                        // parsedLst:	[{type: io.odysz.anson.AnsT2, s: 4, m: null}, {type: io.odysz.anson.AnsT1, ver: "x", m: null}]
                                        // parsedClzz:	java.util.ArrayList
                                        // eleType:		[Lio.odysz.anson.Anson;
                                        // eleClz:		class [Lio.odysz.anson.Anson;
                                        // action - change parsedLst to array, add to enclosLst
                                        enclosLst.Add(ToPrimitiveArray(parsedLst,
                                                      Array.CreateInstance(eleClz, 0).GetType()));
                                    }
                                }
                                catch (Exception e)
                                {
                                    Debug.WriteLine(EnvelopName());
                                    Debug.WriteLine(ctx.GetText());
                                    Debug.WriteLine(e.StackTrace);
                                }
                            }
                        }
                        else enclosLst.Add(top.parsedVal);
                    }
                    top.parsedVal = null;
                }
                else if (top.IsInMap())
                {
                    // parsed Value can already got when exit array
                    if (top.parsedVal == null)
                        top.parsedVal = GetStringVal(ctx.STRING(), ctx.GetText());
                }
            }
            else if (top.parsedVal == null)
                top.parsedVal = GetStringVal(ctx.STRING(), ctx.GetText());
        }

        public override void ExitPair(PairContext ctx)
        {
            base.ExitPair(ctx);
            if (AnsonFlags.parser)
            {
                Debug.WriteLine(string.Format("[AnsonFlags.parser] Property-name: {0}", ctx.GetChild(0).GetText()));
                Debug.WriteLine(string.Format("[AnsonFlags.parser] Property-value: {0}", ctx.GetChild(2).GetText()));
            }

            ParsingCtx top = Top();
            string fn = top.parsingProp;

            // map's pairs also exits here - map helper
            if (top.IsInMap())
            {
                ((Hashtable)top.enclosing)[top.parsingProp] = top.parsedVal;
                top.parsedVal = null;
                top.parsingProp = null;
                return;
            }
            // not map ...

            object enclosing = Top().enclosing;
            Type et = enclosing?.GetType();
            FieldInfo f = (FieldInfo)top.fmap[fn];
            PropertyInfo p = (PropertyInfo)(top.pmap?[fn]);
            Type fptype = f == null ? p?.PropertyType : f.FieldType;

            if ( f == null && p == null &&
               !(typeof(Hashtable).IsAssignableFrom(et) ||
                 // et.Name == "Dictionary`2" || et.GetInterface("Dictionary`2") != null) )
                 CSharp.IsDict(et)) )
                throw new AnsonException(0, "Field/property ignored: field: {0}, value: {1}", fn, ctx.GetText());

            AnsonField af = (AnsonField) (f == null ?
                               ((MemberInfo)p)?.GetCustomAttribute(typeof(AnsonField))
                              :((MemberInfo)f).GetCustomAttribute(typeof(AnsonField)));
            if (af != null && af.ignoreFrom)
            {
                if (AnsonFlags.parser)
                    Debug.WriteLine(string.Format("[AnsonFlags.parser] {0} ignored", fn));
                return;
            }
            else if (af != null && af.refer == AnsonField.enclosing) {
                object parent = Toparent(fptype);
                if (parent == null)
                    Debug.WriteLine(string.Format("parent {0} is ignored: reference is null", fn));

                CSharp.SetFPValue(enclosing, f, p, parent);
                return;
            }

            if (fptype == null && et.IsGenericType) {
                // design notes:
                // -------------
                //     As json is datetype ignoring data, we only try to figure out
                //     Map/Dictionary, List/Array, Simple type and so on,
                //     so here first try map/dict, then handle the rest later.
                CSharp.SetFPValueKv(enclosing, et, top.parsingProp, top.parsedVal);
            }

            else if (fptype == typeof(String) || fptype == typeof(string))
            {
                string v = GetStringVal(ctx);
                CSharp.SetFPValue(enclosing, f, p, v);
            }
            else if (fptype.IsPrimitive)
            {
                // construct primitive value
                string v = ctx.GetChild(2).GetText();
                SetPrimitive((IJsonable)enclosing, f, p, v);
            }
            else if (fptype.IsEnum)
            {
                string v = GetStringVal(ctx);
                if (!string.IsNullOrEmpty(v))
                    // f.SetValue(enclosing, Enum.Parse(fptype, v));
                    CSharp.SetFPValue(enclosing, f, p, v);
            }
            else if (fptype.IsArray)
                // FIXME wast of time?
                CSharp.SetFPValueList(enclosing, f, p, ToPrimitiveArray((IList)top.parsedVal, fptype));
            else if (typeof(Hashtable).IsAssignableFrom(fptype))
            {
                CSharp.SetFPValue(enclosing, f, p, (Hashtable)top.parsedVal);
            }
            else if (CSharp.IsList(fptype))
            {
                // SetFPValueList(enclosing, f, p, top.parsedVal);
                CSharp.SetFPValue(enclosing, f, p, top.parsedVal);
            }
            // else if (fptype.Name == "Dictionary`2" || fptype.GetInterface("Dictionary`2") != null)
            else if (CSharp.IsDict(fptype))
            {
                CSharp.SetFPValueDict(enclosing, f, p, top.parsedVal);
            }
            else if (typeof(IJsonable).IsAssignableFrom(fptype))
            {
                // By pass for serializing and deserializing a string value by user, e.g. Port() & Port#ToBlock()
                if (top.parsedVal != null && top.parsedVal.GetType() == typeof(string))
                {
                    ConstructorInfo ctor = fptype.GetConstructor(new Type[] { typeof(string) });
                    if (ctor == null)
                        throw new AnsonException(0, "To deserialize json to {0}, the class must has a constructor(1 string parameter)\n"
                                        + "string value: {1}",
                                        fptype.FullName, top.parsedVal);
                    CSharp.SetFPValue(enclosing, f, p, ctor.Invoke(new string[] { top.parsedVal }));
                }
                else if (typeof(Anson).IsAssignableFrom(fptype))
                    // f.SetValue(enclosing, top.parsedVal);
                    CSharp.SetFPValue(enclosing, f, p, (Anson)top.parsedVal);
                // ignore null value
                else if (top.parsedVal != null)
                {
                    // Subclass of IJsonable must registered
                    //string v = GetStringVal(ctx);
                    //if (!string.IsNullOrEmpty(v))
                    //    f.SetValue(enclosing, invokeFactory(f, v));
                    throw new AnsonException("Shouldn't reach here in C#.");
                }
            }
            else if (typeof(object).IsAssignableFrom(fptype))
            {
                Debug.WriteLine(string.Format(
                        "\nDeserializing unsupported type, field: {0}, type: {1}, enclosing type: {1}",
                        fn, fptype.Name, enclosing?.GetType().Name));
                string v = ctx.GetChild(2).GetText(); // null -> "null"

                if (!string.IsNullOrEmpty(v) && "null" != v.Trim())
                    CSharp.SetFPValue(enclosing, f, p, v);
            }
            else throw new AnsonException(0, "sholdn't happen");

            // not necessary, top is dropped
            top.parsedVal = null;
        }

        //private IJsonable invokeFactory(FieldInfo f, string v)
        //{
        //    if (factorys == null || !factorys.containsKey(f.GetType()))
        //        throw new AnsonException(0,
        //                "Subclass of IJsonable ({0}) must registered.\n - See javadoc of IJsonable.JsonFacotry\n"
        //                + "Or don't declare the field as {0:S}, use a subclass of Anson",
        //                f.GetType().FullName);

        //    JsonableFactory factory = factorys.get(f.GetType());
        //    try { return factory.fromJson(v); }
        //    catch (Exception t)
        //    {
        //        throw new AnsonException(0,
        //                "Subclass of IJsonable ({0}) must registered.\n - See javadoc of IJsonable.JsonFacotry\n"
        //                + "Or don't declare the field as {0:S}, use a subclass of Anson",
        //                f.GetType().FullName);
        //    }
        //}

        internal static void SetPrimitive(IJsonable obj, FieldInfo f, PropertyInfo p, string v)
        {
            /* c# type reference: 
             * https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/builtin-types/floating-point-numeric-types
             * https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/builtin-types/integral-numeric-types
             */
            Type ft = f == null ? p?.PropertyType : f.FieldType;
            if (ft == typeof(int) || ft == typeof(Int32))
                // f.SetValue(obj, int.Parse(v));
                CSharp.SetFPValue(obj, f, p, int.Parse(v));
            else if (ft == typeof(float) || ft == typeof(Single))
                // f.SetValue(obj, float.Parse(v));
                CSharp.SetFPValue(obj, f, p, float.Parse(v));
            else if (ft == typeof(double) || ft == typeof(Double))
                // f.SetValue(obj, double.Parse(v));
                CSharp.SetFPValue(obj, f, p, double.Parse(v));
            else if (ft == typeof(long) || ft == typeof(Int64))
                // f.SetValue(obj, long.Parse(v));
                CSharp.SetFPValue(obj, f, p, long.Parse(v));
            else if (ft == typeof(short) || ft == typeof(Int16))
                // f.SetValue(obj, short.Parse(v));
                CSharp.SetFPValue(obj, f, p, short.Parse(v));
            else if (ft == typeof(byte) || ft == typeof(Byte))
                // f.SetValue(obj, byte.Parse(v));
                CSharp.SetFPValue(obj, f, p, byte.Parse(v));
            else
                // what's else?
                if (f != null)
                    throw new AnsonException(0, "Unsupported field type: {0} (field {1})",
                        f.GetType().Name, f.Name);
                else if (p != null)
                    throw new AnsonException(0, "Unsupported field type: {0} (field {1})",
                        p.GetType().Name, p?.Name);
        }

        /// <summary>C# Specific reflection helper</summary>
        class CSharp
        {

            /// <summary>
            /// Set value to field first. If it's null, try to property.
            /// </summary>
            /// <param name="enclosing"></param>
            /// <param name="f"></param>
            /// <param name="p"></param>
            /// <param name="v"></param>
            internal static void SetFPValue(object enclosing, FieldInfo f, PropertyInfo p, object v)
            {
                if (v == null)
                    return;

                if (f != null)
                    f.SetValue(enclosing, v);
                    // f.SetValue(enclosing, IsList(f.FieldType) ? ToList((Array)v) : v);
                else if (p != null)
                    p.SetValue(enclosing, v);
                    // p.SetValue(enclosing, IsList(p.PropertyType) ? ToList((Array)v) : v);
                else
                    Debug.WriteLine(string.Format(
                        "Failed to set field/property value. obj: {0}, parent: {1}",
                        enclosing.ToString(), v.ToString()));
            }

            //private static IList ToList(Array v)
            //{
            //    if (v == null) return null;
            //    IList lst = new List<object>(v.Length);
            //    foreach (object e in v)
            //        lst.Add(e);
            //    return lst;
            //}

            internal static void SetFPValueList(object enclosing, FieldInfo f, PropertyInfo p, IList v)
            {
                if (v == null)
                    return;

                if (f != null)
                    f.SetValue(enclosing, CastList(f.FieldType, v));
                else if (p != null)
                    // p.SetValue(enclosing, v);
                    p.SetValue(enclosing, CastList(p.PropertyType, v));
                else
                    Debug.WriteLine(string.Format(
                        "Failed to set field/property value. obj: {0}, parent: {1}",
                        enclosing.ToString(), v.ToString()));
            }
            /// <summary>
            /// Set value to a generic map property/field
            /// </summary>
            /// <param name="enclosing"></param>
            /// <param name="f"></param>
            /// <param name="p"></param>
            /// <param name="v"></param>
            internal static void SetFPValueDict(object enclosing, FieldInfo f, PropertyInfo p, object v)
            {
                if (v == null)
                    return;

                if (f != null)
                    f.SetValue(enclosing, v);
                else if (p != null)
                    // p.SetValue(enclosing, v);
                    p.SetValue(enclosing, v);
                else
                    Debug.WriteLine(string.Format(
                        "Failed to set field/property value. obj: {0}, parent: {1}",
                        enclosing.ToString(), v.ToString()));
            }

            /// <summary>
            /// Set inner k:v of generic map
            /// </summary>
            /// <param name="enclosing"></param>
            /// <param name="mapt"></param>
            /// <param name="n"></param>
            /// <param name="v"></param>
            internal static void SetFPValueKv(object enclosing, Type mapt, object n, dynamic v)
            {
                if (v == null)
                    return;

                if (typeof(Hashtable).IsAssignableFrom(mapt))
                    ((Hashtable)enclosing)[n] = v;
                else if (mapt.Name == "Dictionary`2" || mapt.GetInterface("Dictionary`2") != null)
                {
                    ((Dictionary<string, object>)enclosing)[(string)n] = v;
                }
                else
                    ; // so what?
            }

            /// <summary>Handling type conversion in c# which doesn't allow setting List[object] to nested List type.
            /// Reference:
            /// https://stackoverflow.com/a/27584212/7362888
            /// </summary>
            /// <param name="Type"></param>
            /// <param name="data"></param>
            /// <returns></returns>
            internal static object CastList(Type type, object data)
            {
                var DataParam = Expression.Parameter(typeof(object), "data");
                var Body = Expression.Block(Expression.Convert(Expression.Convert(DataParam, data.GetType()), type));

                var Run = Expression.Lambda(Body, DataParam).Compile();
                var ret = Run.DynamicInvoke(data);
                return ret;
            }

            /// <summary>
            /// create c# type, "$" -> "+"
            /// </summary>
            /// <param name="tname"></param>
            /// <returns></returns>
            internal static Type CSType(string tname)
            {
                if (!string.IsNullOrEmpty(tname))
                {
                    tname = Regex.Replace(tname, @"\$", "+");

                    if (Regex.Match(tname, @"^\[L").Success)
                    {
                        // keep string[] in java style
                        tname = Regex.Replace(tname, @"^\[\[", "[L[");
                        tname = Regex.Replace(tname, @";$", "[]");
                        tname = Regex.Replace(tname, @"\[L", "");

                        tname = Regex.Replace(tname, @"java\.lang", "System");
                    }

                    // CS problem: https://stackoverflow.com/a/3512351/7362888
                    Type t = Type.GetType(Java2cs(tname) + ", " + assmName);
                    return t == null ?
                        Type.GetType(Java2cs(tname)) : t;
                }
                return null;
            }

            public static string Java2cs(string tname)
                => "java.util.ArrayList" == tname ?
                    "System.Collections.Generic.List`1[[System.Object]]" : tname;

            internal static bool IsDict(Type fptype)
                => fptype.Name == "Dictionary`2" || fptype.GetInterface("Dictionary`2") != null;

            internal static bool IsList(Type fptype)
                => fptype.IsInterface && fptype.Name == "IList"
                    || fptype.Name == "IList`1" || fptype.GetInterface("IList`1") != null;
        }
    }
}
