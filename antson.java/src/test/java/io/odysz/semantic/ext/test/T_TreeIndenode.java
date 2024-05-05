package io.odysz.semantic.ext.test;

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
 * @author Ody Z
 *
 */
public class T_TreeIndenode extends Anson {
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
	T_TreeIndenode parent;

	ArrayList<T_IndentFlag> indents;

	// Only for Anson parser
	public T_TreeIndenode() { }

	public T_TreeIndenode(String id, T_TreeIndenode... parent) {
		this.id = id;
		this.parentId = len(parent) > 0 ? parent[0].id : null;
		this.parent   = len(parent) > 0 ? parent[0] : null;
		node = new HashMap<String, Object>();
	}
	
	public ArrayList<T_IndentFlag> getChildIndents() {
		ArrayList<T_IndentFlag> indents = indents();
		ArrayList<T_IndentFlag> ret = new ArrayList<T_IndentFlag>(indents);

		if (len(ret) > 0) {
			ret.remove(ret.size() - 1);
			if (lastSibling)
				ret.add(T_IndentFlag.space);
			else
				ret.add(T_IndentFlag.vlink);
		}
		return ret;
	}

	public ArrayList<T_IndentFlag> indents() {
		if (indents == null && parent != null) {
			indents = parent.getChildIndents();

			if (lastSibling) 
				indents.add(T_IndentFlag.childx);
			else
				indents.add(T_IndentFlag.childi);
		}
		if (indents == null)
			indents = new ArrayList<T_IndentFlag>();
		return indents;
	}

	public T_TreeIndenode put(String k, Object v) {
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

	public T_TreeIndenode child(T_TreeIndenode ch) {
		@SuppressWarnings("unchecked")
		List<T_TreeIndenode> children = (List<T_TreeIndenode>) get("children");
		if (children == null) {
			children = new ArrayList<T_TreeIndenode>();
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

	public T_TreeIndenode children_(List<? extends T_TreeIndenode> childrenArray) {
		put("children", childrenArray);
		return this;
	}
	
	/**
	 * Set last child as the last sibling.
	 * @return this
	 */
	public T_TreeIndenode tagLast() {
		@SuppressWarnings("unchecked")
		ArrayList<T_TreeIndenode> children = (ArrayList<T_TreeIndenode>) get("children");
		if (!isNull(children))
			children.get(children.size() - 1).asLastSibling();
		return this;
	}

	boolean lastSibling;
	public T_TreeIndenode asLastSibling() {
		lastSibling = true;
		return this;
	}
}
