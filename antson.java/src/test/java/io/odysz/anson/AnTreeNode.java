package io.odysz.anson;

import java.util.HashMap;

public class AnTreeNode extends Anson {
	HashMap<String, Object> node;

	public AnTreeNode() {
		node = new HashMap<String, Object>();
	}
	
	public AnTreeNode put(String k, Object v) {
		node.put(k, v);
		return this;
	}
}