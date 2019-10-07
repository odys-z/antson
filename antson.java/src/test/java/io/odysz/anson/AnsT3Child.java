package io.odysz.anson;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonField;

public class AnsT3Child extends Anson {

	@AnsonField(ignoreTo=true)
	AnsT3 parent;
	
	public AnsT3Child() { }
	
	public AnsT3Child(AnsT3 parent) {
		this.parent = parent;
		
		// expand parent
		parent.expand(this);
	}
}
