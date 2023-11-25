using System;
using System.Runtime.Serialization;

namespace io.odysz.anson
{
    [Serializable]
    internal class AnsonException : Exception
    {
        private int v;
        private string message;

        public AnsonException()
        {
        }

        public AnsonException(string message) : base(message)
        {
        }

        public AnsonException(int v, string message)
        {
            this.v = v;
            this.message = message;
        }

        public AnsonException(string message, Exception innerException)
            : base(message, innerException)
        {
        }

        public AnsonException(int c, string template, params string[] args)
            : this(c, string.Format(template, args))
        {
        }

        protected AnsonException(SerializationInfo info, StreamingContext context)
            : base(info, context)
        {
        }
    }
}