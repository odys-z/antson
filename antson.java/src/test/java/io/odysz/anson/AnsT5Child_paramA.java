package io.odysz.anson;

/**Concrete type of child of {@link AnsT3}
 * @author odys-z@github.com
 */
public class AnsT5Child_paramA extends AnsT5GeneriChild<AnsT5Child_paramA> {

	String name;
	
	public AnsT5Child_paramA() {
		super(null);
		name = "param A";
	}

	public AnsT5Child_paramA(AnsT3 parent) {
		super(parent);
		name = "param A";
	}
}
