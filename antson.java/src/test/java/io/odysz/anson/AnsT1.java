package io.odysz.anson;

public class AnsT1 extends Anson {

	/**Inner class must be static.
	 * The parser uses reflection create instances. (TODO: docs)
	 * 
	 * @author odys-z@github.com
	 */
	public static class AnsM1 extends Anson {
		public String name;
		
		public AnsM1() {
			name = "m1 : Anson";
		}
	}

	String ver;
	
	AnsM1 m;

}
