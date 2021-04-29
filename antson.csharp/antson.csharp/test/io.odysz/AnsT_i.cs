using System;
using System.Collections.Generic;

namespace io.odysz.anson
{
    public class AnsT1 : Anson
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

        public string version;

        public AnsM1 m;
    }

    public class AnsT2 : Anson
    {
        [AnsonField(ignoreTo = true)]
	    public int s;

        public string[] m;

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

    public class Ans2dArr : Anson
    {
        public string[][] strs;

        public Ans2dArr() { }
    }

    /**Class for testing Anson array field.
     * @author odys-z@github.com
     */
    public class AnsT3 : Anson
    {
        /** Elements can be subclass*/
        public Anson [] m;

        // annotation is not used, which can be figured out from ParameterizedType
        [AnsonField(valType="[Lio.odysz.Anson;/io.odysz.Anson")]
        List<Anson[]> ms;

        public AnsT3() { }

        public void Expand(Anson child)
        {
            if (m != null)
            {
                Anson[] m1 = new Anson[m.Length + 1];
                Array.Copy(m, 0, m1, 0, m.Length);
                m1[m1.Length - 1] = child;
                m = m1;
            }
            else
            {
                m = new Anson[1];
                m[0] = child;
            }
        }
    }

    public class AnsT3Child : Anson
    {
        [AnsonField(ignoreTo=true)]
        public AnsT3 parent;

        public AnsT3Child() { }

        public AnsT3Child(AnsT3 parent)
        {
            this.parent = parent;
            // expand parent
            parent.Expand(this);
        }
    }

    public class AnsT3son : Anson
    {
        [AnsonField(refer=AnsonField.enclosing)]
        public AnsT3 parent;
        public string gendre;


        public AnsT3son() { }

        public AnsT3son(AnsT3 parent)
        {
            this.parent = parent;
            gendre = "male";
            // expand parent
            parent.Expand(this);
        }
    }

}