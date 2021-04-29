using Antlr4.Runtime.Tree;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using System.Text.RegularExpressions;
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

			internal bool IsInList()
			{	// return enclosing instanceof List || enclosing.getClass().isArray();
				return ! typeof(Hashtable).IsAssignableFrom(enclosing.GetType())
                      && typeof(IEnumerable).IsAssignableFrom(enclosing.GetType());
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
                    }
                    return Type.GetType(tname);
                }
                return null;
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
				Hashtable pmap = new Hashtable();
				ParsingCtx newCtx = new ParsingCtx(fmap, pmap, new List<object>());
				stack.Insert(0, newCtx.ElemType(elemType));
            }
            else {
                Hashtable fmap = new Hashtable();
                Hashtable pmap = new Hashtable();
                if (typeof(IEnumerable).IsAssignableFrom(enclosingClazz)) {
                    IList enclosing = new List<object>();
					stack.Insert(0, new ParsingCtx(fmap, pmap, enclosing).ElemType(elemType));
                }
                else
                {
                    ConstructorInfo ctor = enclosingClazz.GetConstructor(Type.EmptyTypes);
                    if (ctor != null && typeof(IJsonable).IsAssignableFrom(enclosingClazz)) {
						MergeFields(enclosingClazz, fmap, pmap); // map merging is only needed by typed object

                        IJsonable enclosing = (IJsonable)Activator.CreateInstance(enclosingClazz);
                        stack.Insert(0, new ParsingCtx(fmap, pmap, enclosing));
                    }
                    else
                    {
                        Hashtable enclosing = new Hashtable();
                        ParsingCtx top = new ParsingCtx(fmap, pmap, enclosing);
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

                FieldInfo f = (FieldInfo)top.fmap[top.parsingProp];
                Type ft = f.FieldType;
                if (typeof(Hashtable).IsAssignableFrom(ft)) {
                    // entering a map
                    Push(ft, null);
                    // append annotation
                    AnsonField a = (AnsonField)((MemberInfo)f)?.GetCustomAttribute(typeof(AnsonField));
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

            try
            {
                Type clazz = ParsingCtx.CSType(envetype);
                Push(clazz, null);
            }
            catch (AnsonException e) {
                Console.Error.WriteLine(e.StackTrace);
            }
            catch (TypeLoadException e) {
                Console.Error.WriteLine(e.StackTrace);
            }
            catch (Exception e) {
                Console.Error.WriteLine(e.StackTrace);
            }
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
		private static string[] ParseListElemType(FieldInfo f) {
			// for more information, see
			// https://stackoverflow.com/questions/1868333/how-can-i-determine-the-type-of-a-generic-field-in-java

			Type ft = f.FieldType;
			if (ft.IsGenericType) {
				string[] ptypess = ft.GetGenericArguments()[0].FullName.Split('<');
                if (ptypess.Length > 1) {
                    // ptypess[1] = ptypess[1].replaceFirst(">$", "");
                    // ptypess[1] = ptypess[1].replaceFirst("^L", "");
                    ptypess[1] = Regex.Replace(ptypess[1], ">$", "");
                    ptypess[1] = Regex.Replace(ptypess[1], "^L", "");
                }
				// figure out array element class
                else {
                    Type argType = ft.GetGenericArguments()[0];
                    if (!argType.IsGenericParameter) {
                        if (argType.IsArray) {
                            ptypess = new string[] {ptypess[0], argType.GetElementType().Name};
                        }
                    }
                    // else nothing can do here for a type parameter, e.g. "T"
                    else
                        if (AnsonFlags.parser)
							Console.WriteLine(string.Format(
								"[AnsonFlags.parser] warn Element type <{0}> for {1} is a type parameter ({2}) - ignored",
                                ft.GetGenericArguments()[0],
                                f.Name,
                                ft.GetGenericArguments()[0].GetType()));
                }
                return ptypess;
            }
            else if (ft.IsArray)
            {
                // complex array may also has annotation
                AnsonField a = (AnsonField)((MemberInfo)f)?.GetCustomAttribute(typeof(AnsonField));
                string tn = a?.valType;
                string[] valss = ParseElemType(tn);

                string eleType = f.FieldType.GetElementType().FullName;
                if (valss != null && !eleType.Equals(valss[0]))
                    Console.Error.WriteLine(string.Format(
							"[JSONAnsonListener#parseListElemType()]: Field {0} is not annotated correctly.\n"
                            + "field parameter type: {1}, annotated element type: {2}, annotated sub-type: {3}",
                            f.Name, eleType, valss[0], valss[1]));

                if (valss != null && valss.Length > 1)
                    return new string[] { eleType, valss[1] };
                else return new string[] { eleType };
            }
            else
            {
                // not a parameterized, not an array, try annotation
                AnsonField a = (AnsonField)((MemberInfo)f)?.GetCustomAttribute(typeof(AnsonField));
                string tn = a?.valType;
                return ParseElemType(tn);
            }
        }

        /**Parse property name, tolerate enclosing quotes presenting or not.
         * @param ctx
         * @return
         */
        private static string GetProp(PairContext ctx)
        {
            ITerminalNode p = ctx.propname().IDENTIFIER();
            return p == null
                    ? Regex.Replace(ctx.propname().STRING().GetText(), "(^\\s*\"\\s*)|(\\s*\"\\s*$)", "")
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
                    Type ft = f.FieldType;
                    // AnsT3 { ArrayList<Anson[]> ms; }
                    // ctx: [[{type:io.odysz.anson.AnsT2,s:4},{type:io.odysz.anson.AnsT1,ver:"x"}]]
                    // [0]: io.odysz.anson.Anson[],
                    // [1]: io.odysz.anson.Anson
                    string[] tn = ParseListElemType(f);
                    Push(ft, tn);
                }
            } catch (AnsonException e) {
				Console.Error.WriteLine(e.StackTrace);
            } catch (Exception e) {
				Console.Error.WriteLine(e.StackTrace);
            }
        }

        public override void ExitArray(ArrayContext ctx)
        {
            if (!Top().IsInList())
                throw new NullReferenceException("existing not from an eclosing list. txt:\n" + ctx.GetText());

            ParsingCtx top = Pop();
            List<object> arr = (List<object>) top.enclosing;

            top = Top();
            top.parsedVal = arr;

			// figure the type if possible - convert to array
			string et = top.ElemType();
			// ?? if (!Isblank(et, "\\?.*")) // TODO debug: where did this type comes from?
			if (!string.IsNullOrEmpty(et)) // TODO debug: where did this type comes from?
				try
				{
					Type arrClzz = ParsingCtx.CSType(et);
					if (arrClzz.IsArray)
						top.parsedVal = ToPrimitiveArray(arr, arrClzz);
				}
				catch (AnsonException e)
				{
					Console.Error.WriteLine(string.Format(
							"Trying convert array to annotated type failed.\ntype: {0}\njson: {1}\nerror: {2}",
							et, ctx.GetText(), e.Message));
				}
				catch (Exception e)
				{
					Console.Error.WriteLine(string.Format(
							"Trying convert array to annotated type failed.\ntype: {0}\njson: {1}\nerror: {2}",
							et, ctx.GetText(), e.Message));
				}
            // No annotation, for 2d list, parsed value is still a list.
            // If enclosed element of array is also an array, it can not been handled here
            // Because there is no clue for sub array's type if annotation is empty
        }

        /**
         * Unboxes a List in to a primitive array.
         * reference:
         * https://stackoverflow.com/questions/25149412/how-to-convert-listt-to-array-t-for-primitive-types-using-generic-method
         *
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
        private static Array ToPrimitiveArray<P> (List<P> list, Type arrType)
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
                // String txt = ctx.getText();
                if (top.IsInList())
                {
                    List<object> enclosLst = (List <object>) top.enclosing;
                    // for List, ft is not null
                    if (top.parsedVal == null)
                    {
                        // simple value like String or number
                        ((List<object>)enclosLst).Add(FigureJsonVal(ctx));
                    }
                    else
                    {
                        // try figure out is element also an array if enclosing object is an array
                        // e.g. convert elements of List<String> to String[]
                        // FIXME issue: if the first element is 0 length, it will failed to convert the array
                        Type parsedClzz = top.parsedVal.GetType();
                        if (typeof(List<object>).IsAssignableFrom(parsedClzz)) {
                            if (string.IsNullOrEmpty(top.ElemType())) // (LangExt.isblank(top.elemType(), "\\?.*"))
                            {
                                // change list to array
                                List <object> lst = (List <object>)top.parsedVal;
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
                                            ((List<object>)enclosLst).Add(ToPrimitiveArray(lst,
                                                    Array.CreateInstance(eleClz, 0).GetType()));
                                        }
                                        catch (AnsonException e)
                                        {
                                            Console.Error.WriteLine(string.Format(
                                                "Trying convert array to annotated type failed.\nenclosing: {0}\njson: {1}\nerror: {2}",
                                                top.enclosing, ctx.GetText(), e.Message));
                                        }

                                        // remember elem type for later null element
                                        top.ElemType(new string[] { eleClz.Name });
                                    }
                                    // all elements are null, ignore the list is the only way
                                }
                                else
                                    // FIXME this will broken when first element's length is 0.
                                    ((List<object>)enclosLst).Add(lst.ToArray());
                            }
                            // branch: with annotation or type name already figured out from 1st element
                            else
                            {
                                try
                                {
                                    List <object> parsedLst = (List <object>)top.parsedVal;
                                    string eleType = top.ElemType();
                                    Type eleClz = ParsingCtx.CSType(eleType);
                                    if (eleClz.IsAssignableFrom(parsedClzz))
                                    {
                                        // annotated element can be this branch
                                        ((List<object>)enclosLst).Add(parsedLst);
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
                                        ((List<object>)enclosLst).Add(ToPrimitiveArray(parsedLst,
                                                        Array.CreateInstance(eleClz, 0).GetType()));
                                    }
                                }
                                catch (Exception e)
                                {
                                    Console.Error.WriteLine(EnvelopName());
                                    Console.Error.WriteLine(ctx.GetText());
                                    Console.Error.WriteLine(e.StackTrace);
                                }
                            }
                        }
                        else ((List<object>)enclosLst).Add(top.parsedVal);
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
        }

        public override void ExitPair(PairContext ctx)
        {
            base.ExitPair(ctx);
            if (AnsonFlags.parser)
            {
                Console.Out.WriteLine(string.Format("[AnsonFlags.parser] Property-name: {0}", ctx.GetChild(0).GetText()));
                Console.Out.WriteLine(string.Format("[AnsonFlags.parser] Property-value: {0}", ctx.GetChild(2).GetText()));
            }

            try
            {
                // String fn = getProp(ctx);
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
                FieldInfo f = (FieldInfo)top.fmap[fn];
                PropertyInfo p = (PropertyInfo)(top.pmap?[fn]);
                if (f == null && p == null)
                    throw new AnsonException(0, "Field/property ignored: field: {0}, value: {1}", fn, ctx.GetText());

                // f.setAccessible(true);
                AnsonField af = (AnsonField) (f == null ?
                                                ((MemberInfo)p)?.GetCustomAttribute(typeof(AnsonField))
                                              :((MemberInfo)f).GetCustomAttribute(typeof(AnsonField)));
                Type fptype = f == null ? p.PropertyType : f.FieldType;
                if (af != null && af.ignoreFrom)
                {
                    if (AnsonFlags.parser)
                        Console.Out.WriteLine(string.Format("[AnsonFlags.parser] {0} ignored", fn));
                    return;
                }
                else if (af != null && af.refer == AnsonField.enclosing) {
                    object parent = Toparent(fptype);
                    if (parent == null)
                        Console.Error.WriteLine(string.Format("parent {0} is ignored: reference is null", fn));

                    SetFPValue(enclosing, f, p, parent);
                    return;
                }

                // Type ft = f.GetType();

                if (fptype == typeof(String) || fptype == typeof(string)) {
                    string v = GetStringVal(ctx);
                    // f.SetValue(enclosing, v);
                    SetFPValue(enclosing, f, p, v);
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
                        SetFPValue(enclosing, f, p, v);
                }
                else if (fptype.IsArray)
                    // f.SetValue(enclosing, ToPrimitiveArray((List <object>)top.parsedVal, fptype));
                    SetFPValue(enclosing, f, p, ToPrimitiveArray((List <object>)top.parsedVal, fptype));
                else if (typeof(IList).IsAssignableFrom(fptype)
                        // || AbstractCollection.class.isAssignableFrom(ft)
                        || typeof(Hashtable).IsAssignableFrom(fptype)) {
                    // f.SetValue(enclosing, top.parsedVal);
                    SetFPValue(enclosing, f, p, top.parsedVal);
                }
                else if (typeof(IJsonable).IsAssignableFrom(fptype)) {
                    if (typeof(Anson).IsAssignableFrom(fptype))
                        // f.SetValue(enclosing, top.parsedVal);
                        SetFPValue(enclosing, f, p, top.parsedVal);
                    else
                    {
                        // Subclass of IJsonable must registered
                        //string v = GetStringVal(ctx);
                        //if (!string.IsNullOrEmpty(v))
                        //    f.SetValue(enclosing, invokeFactory(f, v));
                        throw new AnsonException("Shouldn't reach here in C#.");
                    }
                }
                else if (typeof(object).IsAssignableFrom(fptype)) {
                    Console.Out.WriteLine(string.Format(
                            "\nDeserializing unsupported type, field: {0}, type: {1}, enclosing type: {1}",
                            fn, fptype.Name, enclosing?.GetType().Name));
                    string v = ctx.GetChild(2).GetText();

                    if (!string.IsNullOrEmpty(v)) // !LangExt.isblank(v, "null")
                        // f.SetValue(enclosing, v);
                        SetFPValue(enclosing, f, p, v);
                }
			    else throw new AnsonException(0, "sholdn't happen");

                // not necessary, top is dropped
                top.parsedVal = null;
            } catch (AnsonException e) {
                Console.Error.WriteLine(string.Format(e.Message));
            } catch (Exception e) {
                Console.Error.WriteLine(string.Format(e.Message));
            }
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
                SetFPValue(obj, f, p, int.Parse(v));
            else if (ft == typeof(float) || ft == typeof(Single))
                // f.SetValue(obj, float.Parse(v));
                SetFPValue(obj, f, p, float.Parse(v));
            else if (ft == typeof(double) || ft == typeof(Double))
                // f.SetValue(obj, double.Parse(v));
                SetFPValue(obj, f, p, double.Parse(v));
            else if (ft == typeof(long) || ft == typeof(Int64))
                // f.SetValue(obj, long.Parse(v));
                SetFPValue(obj, f, p, long.Parse(v));
            else if (ft == typeof(short) || ft == typeof(Int16))
                // f.SetValue(obj, short.Parse(v));
                SetFPValue(obj, f, p, short.Parse(v));
            else if (ft == typeof(byte) || ft == typeof(Byte))
                // f.SetValue(obj, byte.Parse(v));
                SetFPValue(obj, f, p, byte.Parse(v));
            else
                // what's else?
                if (f != null)
                    throw new AnsonException(0, "Unsupported field type: {0} (field {1})",
                        f.GetType().Name, f.Name);
                else if (p != null)
                    throw new AnsonException(0, "Unsupported field type: {0} (field {1})",
                        p.GetType().Name, p?.Name);
        }

        /// <summary>
        /// Set value to field first. If it's null, try to property.
        /// </summary>
        /// <param name="enclosing"></param>
        /// <param name="f"></param>
        /// <param name="p"></param>
        /// <param name="v"></param>
        internal static void SetFPValue(object enclosing, FieldInfo f, PropertyInfo p, object v)
        {
            if (f != null)
                f.SetValue(enclosing, v);
            else if (p != null)
                p.SetValue(enclosing, v);
            else
                Console.Error.WriteLine(string.Format(
                    "Failed to set field/property value. obj: {0}, parent: {1}",
                    enclosing.ToString(), v.ToString()));
        }
    }
}
