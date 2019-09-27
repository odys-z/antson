package io.odysz.anson;

public class AnsT3son extends Anson {

	@AnsonField(ref=AnsonField.enclosing)
	AnsT3 parent;
	public String gendre;
	
	public AnsT3son() { }

	public AnsT3son(AnsT3 parent) {
		this.parent = parent;
		
		gendre = "male";
		
		// expand parent
		parent.expand(this);
	}
}
