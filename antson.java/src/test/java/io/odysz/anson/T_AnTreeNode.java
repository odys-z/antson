package io.odysz.anson;

import java.util.ArrayList;
import java.util.HashMap;

public class T_AnTreeNode extends Anson {
	public static class SubTree extends Anson {
		public ArrayList<T_AnTreeNode> children;
		
		public SubTree() {
			children = new ArrayList<T_AnTreeNode>();
		}
		
		public SubTree add(T_AnTreeNode child) {
			children.add(child);
			return this;
		}
	}

	HashMap<String, Object> node;

	public T_AnTreeNode() {
		node = new HashMap<String, Object>();
	}
	
	public T_AnTreeNode put(String k, Object v) {
		node.put(k, v);
		return this;
	}
}