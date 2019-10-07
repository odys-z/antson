package io.odysz.anson;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonField;

public class AnsT3son extends Anson {

	@AnsonField(ref=AnsonField.enclosing)
	AnsT3 parent;
	String gendre;
	
	public AnsT3son() { }

	public AnsT3son(AnsT3 parent) {
		this.parent = parent;
		
		gendre = "male";
		
		// expand parent
		parent.expand(this);
	}
}
