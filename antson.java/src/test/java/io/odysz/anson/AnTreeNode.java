package io.odysz.anson;

import java.util.ArrayList;
import java.util.HashMap;

public class AnTreeNode extends Anson {
	public static class SubTree extends Anson {
		public ArrayList<AnTreeNode> children;
		
		public SubTree() {
			children = new ArrayList<AnTreeNode>();
		}
		
		public SubTree add(AnTreeNode child) {
			children.add(child);
			return this;
		}
	}

	HashMap<String, Object> node;

	public AnTreeNode() {
		node = new HashMap<String, Object>();
	}
	
	public AnTreeNode put(String k, Object v) {
		node.put(k, v);
		return this;
	}
}