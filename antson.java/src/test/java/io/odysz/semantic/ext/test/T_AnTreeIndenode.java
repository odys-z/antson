package io.odysz.semantic.ext.test;

/**
 * Test version of semantic.DA's DatasetCfg.AnTreeNode: a subclass of the tree node,
 * whose enclosing reference field, {@link T_TreeIndenode#parent}, is declared with
 * the super class type.
 */
public class T_AnTreeIndenode extends T_TreeIndenode {
	int level;

	/** Only for Anson parser */
	public T_AnTreeIndenode() { }

	public T_AnTreeIndenode(String id, int level, T_TreeIndenode... parent) {
		super(id, parent);
		this.level = level;
	}
}
