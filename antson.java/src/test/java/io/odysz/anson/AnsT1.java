package io.odysz.anson;

public class AnsT1 extends Anson {

	/**Inner class must be static.
	 * The parser uses reflection create instances. (TODO: docs)
	 * 
	 * @author odys-z@github.com
	 */
	public static class AnsM1 extends Anson {
		String name;
		
		public AnsM1() {
			name = "m1 : Anson";
		}
	}

	protected String ver;
	
	public AnsM1 m;

}
