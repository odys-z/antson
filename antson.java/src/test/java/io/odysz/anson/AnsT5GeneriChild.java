package io.odysz.anson;

/**Generic type of child of {@link AnsT3}
 * @author odys-z@github.com
 */
public class AnsT5GeneriChild<T extends AnsT5GeneriChild<?>> extends Anson {

	@AnsonField(ref=AnsonField.enclosing)
	AnsT3 parent;
	
	public AnsT5GeneriChild() { }
	
	public AnsT5GeneriChild(AnsT3 parent) {
		this.parent = parent;
		
		// expand parent
		if (parent != null)
			parent.expand(this);
	}
}
