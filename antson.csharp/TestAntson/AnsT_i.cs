namespace io.odysz.anson
{
    internal class AnsT1
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

        string ver;

        AnsM1 m;
    }
}