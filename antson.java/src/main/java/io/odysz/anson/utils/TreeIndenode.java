package io.odysz.anson.utils;

import static io.odysz.common.LangExt.len;
import static io.odysz.common.LangExt.isNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonField;
import io.odysz.anson.JsonOpt;
import io.odysz.anson.x.AnsonException;

/**
 * Tree node supporting indent for rendering tree structure.
 * 
 * TODO to be moved to Semantic.DA
 * 
 * @author Ody Z
 *
 */
public class TreeIndenode extends Anson {
	@Override
	public Anson toBlock(OutputStream stream, JsonOpt... opts)
			throws AnsonException, IOException {
		indents();
		return super.toBlock(stream, opts);
	}

	HashMap<String, Object> node;
	String id;
	String parentId;
	
	@AnsonField(ref=AnsonField.enclosing)
	TreeIndenode parent;

	ArrayList<IndentFlag> indents;

	// Only for Anson parser
	public TreeIndenode() { }

	public TreeIndenode(String id, TreeIndenode... parent) {
		this.id = id;
		this.parentId = len(parent) > 0 ? parent[0].id : null;
		this.parent   = len(parent) > 0 ? parent[0] : null;
		node = new HashMap<String, Object>();
	}
	
	public ArrayList<IndentFlag> getChildIndents() {
		ArrayList<IndentFlag> indents = indents();
		ArrayList<IndentFlag> ret = new ArrayList<IndentFlag>(indents);

		if (len(ret) > 0) {
			ret.remove(ret.size() - 1);
			if (lastSibling)
				ret.add(IndentFlag.space);
			else
				ret.add(IndentFlag.vlink);
		}
		return ret;
	}

	public ArrayList<IndentFlag> indents() {
		if (indents == null && parent != null) {
			indents = parent.getChildIndents();

			if (lastSibling) 
				indents.add(IndentFlag.childx);
			else
				indents.add(IndentFlag.childi);
		}
		if (indents == null)
			indents = new ArrayList<IndentFlag>();
		return indents;
	}

	public TreeIndenode put(String k, Object v) {
		node.put(k, v);
		return this;
	}

	public Object get(String k) {
		return node == null ? null : node.get(k);
	}

	public String id() { return id; }
	public String parent() { return parentId; }
	public String fullpath() { 
		return node == null ? null : (String) node.get("fullpath");
	}

	public List<?> children() {
		return node == null ? null : (List<?>) node.get("children");
	}

	public Object child(int cx) {
		return node == null ? null : ((List<?>) node.get("children")).get(cx);
	}

	public TreeIndenode child(TreeIndenode ch) {
		@SuppressWarnings("unchecked")
		List<TreeIndenode> children = (List<TreeIndenode>) get("children");
		if (children == null) {
			children = new ArrayList<TreeIndenode>();
			children_(children);
		}
		children.add(ch);
		return this;
	}

	/**
	 * node: { children: arrChildren&lt;List&gt; }
	 * @param arrChildren
	 */
	public void children(List<Object> arrChildren) {
		put("children", arrChildren);
	}

	public TreeIndenode children_(List<? extends TreeIndenode> childrenArray) {
		put("children", childrenArray);
		return this;
	}
	
	/**
	 * Set last child a last-sibling.
	 * @return this
	 */
	public TreeIndenode tagLast() {
		@SuppressWarnings("unchecked")
		ArrayList<TreeIndenode> children = (ArrayList<TreeIndenode>) get("children");
		if (!isNull(children))
			children.get(children.size() - 1).asLastSibling();
		return this;
	}

	boolean lastSibling;
	public TreeIndenode asLastSibling() {
		lastSibling = true;
		return this;
	}
}
