package io.odysz.anson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.odysz.anson.types.AnTreeNode;
import io.odysz.anson.types.Ans2dArr;
import io.odysz.anson.types.AnsT1;
import io.odysz.anson.types.AnsT2;
import io.odysz.anson.types.AnsT3;
import io.odysz.anson.types.AnsT3Child;
import io.odysz.anson.types.AnsT3son;
import io.odysz.anson.types.AnsT4Enum;
import io.odysz.anson.types.AnsTList;
import io.odysz.anson.types.AnsTMap;
import io.odysz.anson.types.AnsTRs;
import io.odysz.anson.types.AnsTStrsList;
import io.odysz.anson.types.AnTreeNode.SubTree;
import io.odysz.anson.types.AnsT4Enum.MsgCode;
import io.odysz.anson.types.AnsT4Enum.Port;
import io.odysz.anson.x.AnsonException;
import io.odysz.common.Utils;

class AnsonTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testToJson() throws Exception {
		AnsT1 anson = new AnsT1();
//		anson.seq = 1;
		anson.ver = "v0.1";
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		anson.toBlock(bos);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsT1, ver: \"v0.1\", m: null}", s);

		AnsT2 a2 = new AnsT2();
		a2.m = new String[] {"e0", "e1"};
//		a2.seq = 2;
		bos = new ByteArrayOutputStream(); 
		a2.toBlock(bos);
		s = bos.toString(StandardCharsets.UTF_8.name());
		// assertEquals("{type: io.odysz.anson.AnsT2, ver: null, s: 0, m: [\"e0\", \"e1\"], seq: 2}", s);
		assertEquals("{type: io.odysz.anson.AnsT2, s: 0, m: [\"e0\", \"e1\"]}", s);
		
		AnsTList cll = new AnsTList();
		cll.lst.add("A");
		cll.lst.add("B");
		bos = new ByteArrayOutputStream(); 
		cll.toBlock(bos);
		s = bos.toString(StandardCharsets.UTF_8.name());
		// assertEquals("{type: io.odysz.anson.AnsTList, anss: null, ver: null, lst: [\"A\", \"B\"], seq: 0}", s);
		assertEquals("{type: io.odysz.anson.AnsTList, anss: null, lst: [\"A\", \"B\"]}", s);

		AnsTRs anrs = new AnsTRs();
		bos = new ByteArrayOutputStream(); 
		anrs.toBlock(bos);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsTRs, rs: "
				+ "{type: io.odysz.anson.AnsonResultset, stringFormats: null, total: 0, rowCnt: 3, colCnt: 4,"
				+ " colnames: {\"1\": [1, \"1\"], \"2\": [2, \"2\"], \"3\": [3, \"3\"], \"4\": [4, \"4\"]},"
				+ " rowIdx: 0, results: [[\"0, 1\", \"0, 2\", \"0, 3\", \"0, 4\"], [\"1, 1\", \"1, 2\", \"1, 3\", \"1, 4\"], [\"2, 1\", \"2, 2\", \"2, 3\", \"2, 4\"]]"
				+ "}}", s);
	}
	
	@Test
	void test2dArr() throws AnsonException, IOException {
		Ans2dArr a2d = new Ans2dArr();
		a2d.strs = new String[][] {
			new String[] {"0.0", "0.1"},
			new String[] {"1.0", "1.1", "1.2"},
			new String[] {"2.0"},
			new String[] {"3.0", "3.1"},
			new String[] {} };
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		a2d.toBlock(bos);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		String expect = "{type: io.odysz.anson.Ans2dArr, strs: [[\"0.0\", \"0.1\"], [\"1.0\", \"1.1\", \"1.2\"], [\"2.0\"], [\"3.0\", \"3.1\"], []]}";
		assertEquals(expect, s);
		
		a2d = (Ans2dArr) Anson.fromJson(expect);
		assertEquals("0.0", a2d.strs[0][0]);
		assertEquals("0.1", a2d.strs[0][1]);
		assertEquals("1.1", a2d.strs[1][1]);
		assertEquals("2.0", a2d.strs[2][0]);
		assertEquals("3.1", a2d.strs[3][1]);
	}

	@SuppressWarnings("unused")
	@Test
	void test2Json_PC() throws Exception {
		AnsT3 parent = new AnsT3();
		
		AnsT3Child c = new AnsT3Child(parent);
		AnsT3son son = new AnsT3son(parent);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		parent.toBlock(bos);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		String expect = "{type: io.odysz.anson.AnsT3, "
						+ "ms: null, m: [{type: io.odysz.anson.AnsT3Child}, "
							+ "{type: io.odysz.anson.AnsT3son, gendre: \"male\"}]}";
		assertEquals(expect, s);
		
		AnsT3 p = (AnsT3) Anson.fromJson(s);
		assertEquals(((AnsT3son)p.m[1]).gendre, "male");
	}
	
	@Test
	void test2Json4Enum() throws AnsonException, IOException {
		AnsT4Enum en = new AnsT4Enum();
		en.p = Port.heartbeat;
		en.c = MsgCode.ok;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		en.toBlock(bos);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		String expect = "{type: io.odysz.anson.AnsT4Enum, p: \"heartbeat\", c: \"ok\"}";
		assertEquals(expect, s);
		
		AnsT4Enum denum = (AnsT4Enum) Anson.fromJson(expect);
		assertEquals(denum.c, MsgCode.ok);
		assertEquals(denum.p, Port.heartbeat);
	}
	
	@SuppressWarnings("serial")
	@Test
	void test2Json4StrsList() throws AnsonException, IOException {
		AnsTStrsList lst = new AnsTStrsList("0-0-0", "0-1-0");
		lst.add0row()
			.add("0,0", "0,1", "0,2")
			.add("1,0", "1,1", "1,2")
			.addnull()
			.add0row()
			.add3Drow(new ArrayList<Object[]>() {
				{ add(new Object[] {"1-0-0", 1.5}); }
				{ add(new Object[] {}); }
			})
			.set4dcell(0, 0, 0, 0, new AnsT2("0 0 0 0"))
			.set4dcell(0, 0, 0, 1, new AnsT2("0 0 0 1"))
			.set4dcell(1, 1, 1, 1, new AnsT2("1 1 1 1"));
	
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		lst.toBlock(bos);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		String expect = "{type: io.odysz.anson.AnsTStrsList, "
				+ "dim4: [[[[{type: io.odysz.anson.AnsT2, s: 0, m: [\"0 0 0 0\"]}, "
						  + "{type: io.odysz.anson.AnsT2, s: 0, m: [\"0 0 0 1\"]}], "
						 + "[null, null]], [[null, null], [null, null]]], [[[null, null], [null, null]], [[null, null], "
						 + "[null, {type: io.odysz.anson.AnsT2, s: 0, m: [\"1 1 1 1\"]}]]]], "
				+ "lst3d: [[[\"0-0-0\", \"\"], [\"0-1-0\"]], [[\"1-0-0\", 1.5], []]], "
				+ "lst: [[], [\"0,0\", \"0,1\", \"0,2\"], [\"1,0\", \"1,1\", \"1,2\"], null, []]}";
		assertEquals(expect, s);
	
		AnsTStrsList l = (AnsTStrsList) Anson.fromJson(s);
		try { assertEquals(0, l.row(0).length);
		} catch (ClassCastException e) {
			// issue:
			// If first element is null, can't figure out what's the component type.
			Utils.warn("Testing 0 length array as first sub-array, should get type conversion error message...");
			Utils.warn(e.getMessage());
		}
		assertEquals("0,0", l.cell(1, 0));	
		assertEquals("1,1", l.cell(2, 1));
		assertEquals(null, l.row(3));
		assertEquals(0, l.row(4).length);

		assertEquals("0-0-0", l.cell(0, 0, 0));
		assertEquals("0-1-0", l.cell(0, 1, 0));
		assertEquals("1-0-0", l.cell(1, 0, 0));
		assertEquals(1.5f, l.cell(1, 0, 1));
	
		assertEquals("0 0 0 0", ((AnsT2)l.cell(0, 0, 0, 0)).m[0]);
		assertEquals("0 0 0 1", ((AnsT2)l.cell(0, 0, 0, 1)).m[0]);
		assertEquals("1 1 1 1", ((AnsT2)l.cell(1, 1, 1, 1)).m[0]);
	}

	@Test
	void testFromJson() throws IllegalArgumentException, ReflectiveOperationException, AnsonException {
		AnsT1 anson = (AnsT1) Anson.fromJson("{type:io.odysz.anson.AnsT1, ver: \"v0.1\", m: {\"name\": \"x\"}}");
		assertEquals("x", anson.m.name);

		anson = (AnsT1) Anson.fromJson("{type: io.odysz.anson.AnsT1, ver: \"v0.1\"}");
		assertEquals("v0.1", anson.ver);
		assertEquals(null, anson.m);

		anson = (AnsT1) Anson.fromJson("{type: io.odysz.anson.AnsT1, ver: \"v0.1\", m: null}");
		assertEquals("v0.1", anson.ver);
		assertEquals(null, anson.m);

		AnsT2 anson2 = (AnsT2) Anson.fromJson("{type:io.odysz.anson.AnsT2, m: [\"e1\", \"e2\"]}");
		assertEquals("e1", anson2.m[0]);
		assertEquals("e2", anson2.m[1]);
	}
	
	@Test
	void testFromJson_asonArr() throws IllegalArgumentException, ReflectiveOperationException, AnsonException {
		// AnsT3.m is typeof AnsT2
		AnsT3 anson3 = (AnsT3) Anson.fromJson("{type: io.odysz.anson.AnsT3, m: [" +
				"{type: io.odysz.anson.AnsT2, s: 4 }, " + 
				"{type: io.odysz.anson.AnsT1, ver: \"x\" }]}");
		assertEquals(2, anson3.m.length);
		assertEquals(4, ((AnsT2)anson3.m[0]).s);
		assertEquals("x", ((AnsT1)anson3.m[1]).ver);

		AnsT3 anson4 = (AnsT3) Anson.fromJson("{type: io.odysz.anson.AnsT3, ms: [[" +
				"{type: io.odysz.anson.AnsT2, s: 4 }, " + 
				"{type: io.odysz.anson.AnsT1, ver: \"x\" }]]}");
		assertEquals(1, anson4.ms.size());
		assertEquals(2, anson4.ms.get(0).length);
		assertEquals(4, ((AnsT2)anson4.ms.get(0)[0]).s);
		assertEquals("x", ((AnsT1)anson4.ms.get(0)[1]).ver);
	}
		
	@Test
	void testFromJson_list() throws IllegalArgumentException, ReflectiveOperationException, AnsonException {
		AnsTList cll = (AnsTList) Anson.fromJson("{type: io.odysz.anson.AnsTList, lst: [\"A\", \"B\"]}");
		assertEquals(2, cll.lst.size());
		assertEquals("A", cll.lst.get(0));
		assertEquals("B", cll.lst.get(1));
		
		
		cll = (AnsTList) Anson.fromJson("{type: io.odysz.anson.AnsTList, anss: ["
				+ "{type: io.odysz.anson.AnsT3}, "
				+ "{type: io.odysz.anson.AnsT3}"
				+ "]}");
		assertEquals(2, cll.anss.size());

		cll = (AnsTList) Anson.fromJson("{type: io.odysz.anson.AnsTList, anss: ["
				+ "{type: io.odysz.anson.AnsT3, m: ["
					+ "{type: io.odysz.anson.AnsT2, s: 4 }, "
					+ "{type: io.odysz.anson.AnsT1, ver: \"x\" }]}, "
				+ "{type: io.odysz.anson.AnsT3}"
				+ "]}");
		assertEquals(2, cll.anss.size());
		assertEquals(4, ((AnsT2)cll.anss.get(0).m[0]).s);
		assertEquals("x", ((AnsT1)cll.anss.get(0).m[1]).ver);
	}

	@Test
	void testFromJson_map() throws IllegalArgumentException, ReflectiveOperationException, AnsonException {
		AnsTMap m = (AnsTMap) Anson.fromJson("{type: io.odysz.anson.AnsTMap, ver: null, map: {\"A\": \"B\"}}");
//		assertEquals(null, m.ver);
		assertEquals("B", m.map.get("A"));

		m = (AnsTMap) Anson.fromJson("{type: io.odysz.anson.AnsTMap, map: {\"A\": \"B\"}, mapArr: {a: [1, \"s\"]}}");
		assertEquals("B", m.map.get("A"));
		assertEquals(2, m.mapArr.get("a").length);
		assertEquals(1, m.mapArr.get("a")[0]);
		assertEquals("s", m.mapArr.get("a")[1]);
	}
		
	@Test
	void testFromJson_rs() throws IllegalArgumentException, ReflectiveOperationException, SQLException, AnsonException {
		AnsTRs rs = (AnsTRs) Anson.fromJson("{type: io.odysz.anson.AnsTRs, rs: "
				+ "{type: io.odysz.anson.AnsonResultset, stringFormats: null, total: 0, rowCnt: 3, colCnt: 4,"
				+ " colnames: {\"1\": [1, \"1\"], \"2\": [2, \"2\"], \"3\": [3, \"3\"], \"4\": [4, \"4\"]},"
				+ " rowIdx: 0, results: [[\"0 1\", \"0 2\", \"0 3\", \"0 4\"], [\"1 1\", \"1 2\", \"1 3\", \"1 4\"], [\"2 1\", \"2 2\", \"2 3\", \"2 4\"]]"
				+ "}}");
		
		assertEquals(3, rs.rs.getRowCount());
		rs.rs.beforeFirst().next();
		assertEquals("0 1", rs.rs.getString("1"));

		rs = (AnsTRs) Anson.fromJson("{type: io.odysz.anson.AnsTRs, rs: "
				+ "{type: io.odysz.anson.AnsonResultset, stringFormats: null, total: 0, rowCnt: 3, colCnt: 4,"
				+ " colnames: {\"1\": [1, \"1\"], \"2\": [2, \"2\"], \"3\": [3, \"3\"], \"4\": [4, \"4\"]},"
				+ " rowIdx: 0, results: [[\"0, 1\", \"0, 2\", \"0, 3\", \"0, 4\"], [\"1, 1\", \"1, 2\", \"1, 3\", \"1, 4\"], [\"2, 1\", \"2, 2\", \"2, 3\", \"2, 4\"]]"
				+ "}}");
		
		assertEquals(3, rs.rs.getRowCount());
		rs.rs.beforeFirst().next();
		assertEquals("0, 1", rs.rs.getString("1"));
	}
	
	@Test
	void testFromJson_tree() throws AnsonException {
		AnTreeNode tr = (AnTreeNode) Anson.fromJson("{type:io.odysz.anson.AnTreeNode,"
				+ "node:{\"fullpath\":\"1 sys.1 domain\","
					  + "\"id\":\"sys-domain\",\"text\":\"Domain Settings\","
					  + "\"sort\":\"1\",\"parentId\":\"sys\",\"url\":\"views/sys/domain/domain.html\""
					 + "}},"
			+ "{type:io.odysz.anson.AnTreeNode,"
				+ "node:{\"fullpath\":\"1 sys.2 role\","
					  + "\"id\":\"sys-role\",\"text\":\"Role Manage\","
					  + "\"sort\":\"2\",\"parentId\":\"sys\",\"url\":\"views/sys/role/roles.html\""
					 + "}},"
			+ "{type:io.odysz.anson.AnTreeNode,"
				+ "node:{\"fullpath\":\"1 sys.3 org\","
					  + "\"id\":\"sys-org\",\"text\":\"Orgnization Manage\","
					  + "\"sort\":\"3\",\"parentId\":\"sys\",\"url\":\"views/sys/org/orgs.html\""
					 + "}},"
			+ "{type:io.odysz.anson.AnTreeNode,"
				+ "node:{\"fullpath\":\"1 sys.4 user\","
					  + "\"id\":\"sys-uesr\",\"text\":\"Uesr Manage\","
					  + "\"sort\":\"4\",\"parentId\":\"sys\",\"url\":\"views/sys/user/users.html\""
					 + "}},"
			+ "{type:io.odysz.anson.AnTreeNode,"
				+ "node:{\"fullpath\":\"1 sys.5 wf\","
					  + "\"id\":\"sys-wf\",\"text\":\"Workflow Settings\","
					  + "\"sort\":\"5\",\"parentId\":\"sys\",\"url\":\"views/sys/workflow/workflows.html\""
					 + "}}" 
				);

		// only last node here?
		assertEquals(6, tr.node.size());
	}
	
	@Test
	void test_innerClass() throws AnsonException, IOException {
		String tst = "{type: io.odysz.anson.AnTreeNode$SubTree, "
				+ "children: [{type: io.odysz.anson.AnTreeNode, "
						   + "node: {\"fullpath\": \"1 sys.1 domain\", "
						   + "\"id\": \"sys-domain\", \"text\": \"Domain Settings\", "
						   + "\"sort\": \"1\", \"parentId\": \"sys\", \"url\": \"views/sys/domain/domain.html\""
						   + "}}]}";
		SubTree tr = (SubTree) Anson.fromJson(tst);
	
		assertEquals(1, tr.children.size());
		assertEquals(6, tr.children.get(0).node.size());
		assertEquals("sys-domain", tr.children.get(0).node.get("id"));
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		tr.toBlock(bos);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals(tst, s);
	}
}
