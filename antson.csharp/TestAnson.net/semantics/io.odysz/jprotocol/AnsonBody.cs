using io.odysz.anson;

namespace io.odysz.semantic.jprotocol
{
	public class AnsonBody : Anson
	{
		/// <summary>
		/// @AnsonField(ref= AnsonField.enclosing)
		/// </summary>
		[AnsonField(refer=AnsonField.enclosing)]
		public AnsonMsg parent { get; internal set; }

		public AnsonBody Parent(AnsonMsg p)
        {
			parent = p;
			return this;
        }

		public string conn { get; protected set; }

		/// <summary>
		/// Action: login | C | R | U | D | any serv extension
		/// </summary>
		protected string a { get; set; }

        /// <summary>
        /// @return Action: login | C | R | U | D | any serv extension
        /// </summary>
        public string A() { return a; }

        public AnsonBody A(string act)
        {
            a = act;
            return this;
        }

        public AnsonBody(AnsonMsg parent, string conn)
		{
			this.parent = parent;
			this.conn = conn;
		}
	}
}
