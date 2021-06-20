using Antlr4.Runtime;
using Antlr4.Runtime.Tree;
using System;
using System.Collections;
using System.IO;
using System.Reflection;
using static JSONParser;

namespace io.odysz.anson
{
    /// <summary>
    /// Base class for all IJsonable data object.
    /// </summary>
    public class Anson : IJsonable
    {
        public string ver { get; protected set; }

        public Anson() {
            ver = "0.9.xx";
        }

        public IJsonable ToBlock(Stream stream, JsonOpt opt = null)
        {
            bool quotK = opt == null || opt.quotKey;
			Type type = GetType();

			Utils.WriteByt(stream, '{');
			Utils.WriteStr(stream, "type", quotK);
			Utils.WriteByt(stream, ':', ' ');
			Utils.WriteStr(stream, GetType().FullName, true);

			Hashtable fmap = new Hashtable();
			Hashtable pmap = new Hashtable();
			JSONAnsonListener.MergeFields(type, fmap, pmap);

			foreach (FieldInfo f in fmap.Values)
			{
                if (f.Name.EndsWith("BackingField")) continue; // properties backing field
                AnsonField af = (AnsonField)Attribute.GetCustomAttribute(f, typeof(AnsonField));
                object v = f.GetValue(this);
                WritePair(stream, af, f.GetType(), f.FieldType, f.Name, v, opt);
            }

			foreach (PropertyInfo p in pmap.Values)
			{
                AnsonField af = (AnsonField)Attribute.GetCustomAttribute(p, typeof(AnsonField));
                object v = p.GetValue(this);
                WritePair(stream, af, p.GetType(), p.PropertyType, p.Name, v, opt);
            }

            Utils.WriteByt(stream, '}');
            stream.Flush();
            return this;
        }

        internal static void WritePair(Stream s, AnsonField af, Type ftype, Type vtype, string n, object v, JsonOpt opt = null)
        {
            // is this ignored?
            if (af != null && af.ignoreTo)
                return;

            Utils.WriteByt(s, ',', ' ');

            // prop
            Utils.WriteStr(s, n, opt == null || opt.quotKey);
            Utils.WriteByt(s, ':', ' ');

            // value
            if (af != null && af.refer == AnsonField.enclosing)
            {
                Utils.WriteStr(s, vtype.FullName, true);
                return;
            }

            try
            {
                if (!ftype.IsPrimitive)
                {
                    Type vclz = v?.GetType();
                    WriteNonPrimitive(s, vclz, v, opt);
                }
                else if (ftype.IsPrimitive)
                    Utils.WriteStr(s, (string)v);
            }
            catch (Exception x)
            {
                throw new AnsonException(0, x.Message);
            }
        }

		/**Write field (element)'s value to stream.<br>
         * The field type (fdClz) is not always the same as value's type.
         * When field is an array, collection, etc., they are different.
         * @param stream
         * @param fdName
         * @param fdClz
         * @param v
         * @throws AnsonException
         * @throws IOException
         */
		internal static void WriteNonPrimitive(Stream stream, Type fdClz, object v, JsonOpt opts = null)
		{
            if (v == null) {
				Utils.WriteStr(stream, "null");
                return;
			}

			Type vclz = v.GetType();

			if (typeof(IJsonable).IsAssignableFrom(vclz))
                ((IJsonable) v).ToBlock(stream, opts);
            else if (vclz == typeof(string) || vclz == typeof(String))
                Utils.WriteStr(stream, Escape(v), true);
            else if (fdClz.IsEnum)
                Utils.WriteStr(stream, ((Enum)v).ToString(), true);
			else if (typeof(IEnumerable).IsAssignableFrom(vclz))
				ToListBlock(stream, (IEnumerable) v, opts);
			else if (typeof(Hashtable).IsAssignableFrom(vclz))
				ToMapBlock(stream, (Hashtable) v, opts);
            // ignored java case: Collection, Array
            else
                Utils.WriteStr(stream, v.ToString());
        }

        internal static void ToListBlock(Stream stream, IEnumerable list, JsonOpt opt)
        {
            Utils.WriteByt(stream, '[');
            bool is1st = true;
            foreach (object e in list)
            {
                if (!is1st)
                    Utils.WriteByt(stream, ',', ' ');
                else
                    is1st = false;

                if (e == null)
                {
                    Utils.WriteStr(stream, "null");
                    continue;
                }

                if (!e.GetType().IsPrimitive)
                    WriteNonPrimitive(stream, e.GetType(), e, opt);
                else // if (f.getType().isPrimitive())
                     // must be primitive?
                    Utils.WriteStr(stream, e.ToString());

            }
            Utils.WriteByt(stream, ']');
        }

        internal static void ToMapBlock(Stream stream, Hashtable map, JsonOpt opts)
        {
            if (map == null) return;

            bool quote = opts == null || opts.quotKey;

            bool the1st = true;
            stream.WriteByte((byte)'{');
            foreach (object k in map.Keys)
            {
                if (the1st) the1st = false;
                else
                    Utils.WriteByt(stream, ',', ' ');

                //if (quote)
                //    stream.WriteByte((byte)'\"');
                //Utils.WriteStr(stream, k.ToString());
                //if (quote)
                //    Utils.WriteByt(stream, '\"', ':', ' ');
                //else
                //    Utils.WriteByt(stream, ':', ' ');
                Utils.WriteStr(stream, k.ToString(), quote);
                Utils.WriteByt(stream, ':', ' ');

                object v = map[k];
                Type elemtype = v.GetType();
                WriteNonPrimitive(stream, elemtype, v);
            }
            Utils.WriteByt(stream, '}');
        }

        private static string Escape(object v)
        {
            if (v == null)
                return "";
            string s = v.ToString();
            return s
                    // .replace("\n", "\\n")
                    // .replace("\/", "\\/")

                    // TODO TEST
                    // .replace("\t", "\\t")
                    // TODO TEST

                    .Replace("\r", "\\r")
                    .Replace("\b", "\\b")
                    .Replace("\\", "\\\\")
                    .Replace("\f", "\\f");
        }

        /**Parse Anson object from json string.
         * <p><b>Note: </b><br>
         * As LL(*) parsing like Antlr won't work in stream mode,
         * this method won't have a input stream version.</p>
         * @param json
         * @return
         * @throws IllegalArgumentException
         * @throws ReflectiveOperationException
         */
        public static IJsonable FromJson(string json)
        {
            return parse(new AntlrInputStream(json));
        }

        public static IJsonable FromJson(Stream ins)
        {
            return parse(new AntlrInputStream(ins));
        }

        private static IJsonable parse(ICharStream ins)
        {
            JSONLexer lexer = new JSONLexer(ins);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JSONParser parser = new JSONParser(tokens);
            JsonContext ctx = parser.json();
            ParseTreeWalker walker = new ParseTreeWalker();
            JSONAnsonListener lstner = new JSONAnsonListener();
            walker.Walk(lstner, ctx);
            return lstner.ParsedEnvelope();
        }
    }
}
