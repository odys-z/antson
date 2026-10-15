package io.odysz.semantic.ext.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;

/**
 * Parsing enclosing references, {@link io.odysz.anson.AnsonField#enclosing},
 * declared with a super class type.
 * 
 * <p>Before the fix, JSONAnsonListener.toparent() matched the exact class only,
 * and walked off the parsing stack, throwing IndexOutOfBoundsException, e.g. for
 * a semantic tree (menu) response of AnTreeNode, whose parent is a TreeIndenode.</p>
 */
public class T_TreeEnclosingTest {

	/** A forest like AnDatasetResp.forest */
	public static class Forest extends Anson {
		ArrayList<T_TreeIndenode> forest;

		public Forest() {
			forest = new ArrayList<T_TreeIndenode>();
		}
	}

	@Test
	void testParseSubclassParent() throws Exception {
		/*
		 * central
		 *  |- home
		 *  L  sync
		 */
		T_AnTreeIndenode root = new T_AnTreeIndenode("central", 0);
		root.child(new T_AnTreeIndenode("home", 1, root));
		root.child(new T_AnTreeIndenode("sync", 1, root).asLastSibling());

		Forest f = new Forest();
		f.forest.add(root);

		Forest g = (Forest) Anson.fromJson(f.toBlock());

		assertEquals(1, g.forest.size());
		T_TreeIndenode r = g.forest.get(0);
		assertEquals(T_AnTreeIndenode.class, r.getClass());
		assertEquals("central", r.id());
		assertEquals(0, ((T_AnTreeIndenode) r).level);
		assertNull(r.parent, "a root has no enclosing tree node");

		assertEquals(2, r.children().size());
		for (Object c : r.children()) {
			T_TreeIndenode child = (T_TreeIndenode) c;
			assertEquals(T_AnTreeIndenode.class, child.getClass());
			assertEquals(1, ((T_AnTreeIndenode) child).level);
			assertSame(r, child.parent, "the enclosing node is resolved by instance, not exact class");
		}
		assertEquals("home", ((T_TreeIndenode) r.child(0)).id());
		assertEquals("sync", ((T_TreeIndenode) r.child(1)).id());
	}
}
