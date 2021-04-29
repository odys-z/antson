using System;
using System.IO;
using System.Text;

namespace io.odysz.anson
{
    public class Utils
    {
        /// <summary>
        /// Write string to stream, with or without quote mark.
        /// </summary>
        /// <param name="stream"></param>
        /// <param name="s"></param>
        /// <param name="withQuote"></param>
        internal static void WriteStr(Stream stream, string s, bool withQuote = false)
        {
            if (withQuote) stream.WriteByte((byte)'\"');
            byte[] b = Encoding.ASCII.GetBytes(s);
            stream.Write(b, 0, b.Length);
            if (withQuote) stream.WriteByte((byte)'\"');
        }

        internal static Stream WriteByt(Stream stream, params char[] chrs)
        {
            if (chrs != null)
            {
                foreach (char c in chrs)
                    stream.WriteByte((byte)c);
            }
            return stream;
        }

        public static string ToString(MemoryStream stream, bool begin = true)
        {
            if (begin) stream.Seek(0, SeekOrigin.Begin);
            StreamReader r = new StreamReader(stream);
            return r.ReadToEnd();
        }
    }
}