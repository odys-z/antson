namespace io.odysz.anson
{
    internal class AnsT1 : Anson
    {
		/**Inner class must be static.
         * The parser uses reflection create instances. (TODO: docs)
         * 
         * @author odys-z@github.com
         */
		public class AnsM1 : Anson
		{
            public string name;

            public AnsM1()
            {
                name = "m1 : Anson";
            }
        }

        internal string version;

        internal AnsM1 m;
    }

    internal class AnsT2 : Anson
    {
	    internal int s;

        internal string[] m;

        public AnsT2() { }

        public AnsT2(string e0, string e1_ = null)
        {
            int l = e1_ == null ? 1 : e1_.Length + 1;
            m = new string[l];
            m[0] = e0;
            for (int i = 1; i < l; i++)
                m[l] = e1_;
        }
    }

}