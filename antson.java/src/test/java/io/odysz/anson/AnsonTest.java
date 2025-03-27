package io.odysz.anson;

import static io.odysz.common.LangExt.len;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.odysz.anson.AnsT4Enum.MsgCode;
import io.odysz.anson.AnsT4Enum.T4_Port;
import io.odysz.anson.T_AnTreeNode.SubTree;
import io.odysz.anson.x.AnsonException;
import io.odysz.common.Utils;
import io.odysz.semantic.ext.test.T_IndentFlag;
import io.odysz.semantic.ext.test.T_TreeIndenode;
import io.oz.jserv.docs.syn.T_DocsReq;
import io.oz.jserv.docs.syn.T_ExpSyncDoc;

class AnsonTest {

	private JsonOpt opt = new JsonOpt().quotKey(false);

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testEscape() throws AnsonException {
		String value = "1\t 2\n 3\" 4\\";
		// 1\\t 2\\n 3\" 4\\
		// Utils.logi(new String(Anson.escape(value)));
		assertEquals("1\\t 2\\n 3\\\" 4\\\\", new String(Anson.escape(value)));

		value = "1\\t 2\\n3";
		// 1	.2
		// 3"
		// Utils.logi(Anson.unescape(value));
		assertEquals("1\t 2\n3", Anson.unescape(value));

		Anson.unescape("1\\t 2\\n 3\\\" 4\\"); // warn in console

		value = "1\\t 2\\n 3\\\" 4\\\\";
		// 1	 2
		//  3" 4\
		assertEquals("1\t 2\n 3\" 4\\", Anson.unescape(value));
	}
	
	@Test
	void testToJson() throws Exception {
		AnsT1 anson = new AnsT1();
		anson.ver = "v0.1";
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		anson.toBlock(bos, opt);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsT1, ver: \"v0.1\", m: null}\n", s);
 
		// ESC
		AnsT2 a2 = new AnsT2();
		a2.m = new String[] {"e\n0", "e1\r\nvalue", "{\"msg\": \"george\"}"};
		bos = new ByteArrayOutputStream(); 
		a2.toBlock(bos, opt);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsT2, b: false, s: 0, c: 0, m: [\"e\\n0\", \"e1\\r\\nvalue\", \"{\\\"msg\\\": \\\"george\\\"}\"]}\n", s);

		AnsTList cll = new AnsTList();
		cll.lst.add("A");
		cll.lst.add("B");
		bos = new ByteArrayOutputStream(); 
		cll.toBlock(bos, opt);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsTList, anss: null, ans2: null, lst: [\"A\", \"B\"]}\n", s);

		AnsTRs anrs = new AnsTRs();
		bos = new ByteArrayOutputStream(); 
		anrs.toBlock(bos, opt);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsTRs, rs: "
				+ "{type: io.odysz.semantic.T_AnResultset, stringFormats: null, total: 0, rowCnt: 3, colCnt: 4,"
				+ " colnames: {1: [1, \"1\"], 2: [2, \"2\"], 3: [3, \"3\"], 4: [4, \"4\"]},"
				+ " rowIdx: 0, indices0: null, flatcols: null, results: [[\"0, 1\", \"0, 2\", \"0, 3\", \"0, 4\"], [\"1, 1\", \"1, 2\", \"1, 3\", \"1, 4\"], [\"2, 1\", \"2, 2\", \"2, 3\", \"2, 4\"]]"
				+ "}\n}\n", s);
		
		
		
		// {"type": "io.odysz.semantic.tier.docs.DocsReq", "tabl": null, "a": "c/b/start", "parent": "io.odysz.semantic.jprotocol.AnsonMsg", "data": null, "org": null, "pageInf": null, "stamp": null, "deletings": null, "nextBlock": null, "uri": "/album/syn", "blockSeq": 0, "syncingPage": null, "limit": -1, "syncQueries": null, "doc": {"type": "io.odysz.semantic.tier.docs.ExpSyncDoc", "synoder": null, "org": "", "subs": null, "pname": "成都市社会保险个人参保证明20240828210710.pdf", "shareby": "ody", "mime": "application/pdf", "shareMsg": null, "uri64": null, "clientpath": "/storage/emulated/0/Download/成都市社会保险个人参保证明20240828210710.pdf", "synode": null, "folder": "2025-03", "size": 42177, "uids": null, "sharedate": "2025-03-26", "shareflag": "prv", "device": "0003", "nyquence": null, "recId": null, "createDate": "2024-08-28"} , "reset": true, "docTabl": "h_photos", "device": {"type": "io.odysz.semantic.tier.docs.Device", "tofolder": null, "synode0": null, "devname": null, "id": "0003"} , "synuri": null}
		T_DocsReq req = (T_DocsReq) new T_DocsReq()
				.doc(new T_ExpSyncDoc()
						.device("0003")
						.fullpath("/storage/emulated/0/Download/成都市社会保险个人参保证明20240828210710.pdf")
						.clientname("成都市社会保险个人参保证明20240828210710.pdf")
						.folder("2025-03")
						.size(42177)
						.mime("application/pdf")
						.createDate("2024-08-28")
						.share("ody", "prv", "2025-03-26"))
				.docTabl("h_photos")
				.uri("/album/syn")
				.a(T_DocsReq.A.blockStart);
		
		opt = new JsonOpt().quotKey(true);
		bos = new ByteArrayOutputStream(); 
		req.toBlock(bos, opt);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{\"type\": \"io.oz.jserv.docs.syn.T_DocsReq\", \"tabl\": null, "
				+ "\"parent\": \"io.odysz.semantic.jprotocol.test.T_AnsonMsg\", "
				+ "\"a\": \"c/b/start\", "
				+ "\"code\": null, "
				+ "\"data\": null, "
				+ "\"org\": null, "
				+ "\"pageInf\": null, \"stamp\": null, \"deletings\": null, \"nextBlock\": null, "
				+ "\"uri\": \"/album/syn\", "
				+ "\"blockSeq\": -1, "
				// + "\"syncingPage\": null, "
				+ "\"limit\": -1, "
				// + "\"syncQueries\": null, "
				+ "\"doc\": {\"type\": \"io.oz.jserv.docs.syn.T_ExpSyncDoc\", "
					+ "\"clientpath\": \"/storage/emulated/0/Download/成都市社会保险个人参保证明20240828210710.pdf\", "
					+ "\"folder\": \"2025-03\", "
					+ "\"size\": 42177, "
					+ "\"pname\": \"成都市社会保险个人参保证明20240828210710.pdf\", "
					+ "\"shareby\": \"ody\", "
					+ "\"sharedate\": \"2025-03-26\", "
					+ "\"shareflag\": \"prv\", "
					+ "\"mime\": \"application/pdf\", "
					+ "\"uri64\": null, "
					+ "\"device\": \"0003\", "
					// + "\"nyquence\": null,"
					+ "\"recId\": null, "
					// + "\"shareMsg\": null, "
					// + "\"synoder\": null, "
					// + "\"org\": \"\", \"subs\": null, "
					// + "\"synode\": null, "
					// + "\"uids\": null, "
					+ "\"createDate\": \"2024-08-28\"}\n, "
				// "reset": false, "docTabl": null, "device": {"type": "io.oz.jserv.docs.syn.T_Device"} , "synuri": null}
				+ "\"reset\": false, \"docTabl\": \"h_photos\", "
				+ "\"device\": {\"type\": \"io.oz.jserv.docs.syn.T_Device\", \"tofolder\": null, \"synode0\": null, \"devname\": \"0003\", \"id\": \"0003\"}\n, "
				+ "\"synuri\": null}\n",
				s);
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
		a2d.toBlock(bos, opt);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		String expect = "{type: io.odysz.anson.Ans2dArr, strs: [[\"0.0\", \"0.1\"], [\"1.0\", \"1.1\", \"1.2\"], [\"2.0\"], [\"3.0\", \"3.1\"], []]}\n";
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
		// should trigger parent: io.odysz.anson.AnsT3
		AnsT3son son = new AnsT3son(parent);
	
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			parent.toBlock(bos, opt);
			String s = bos.toString(StandardCharsets.UTF_8.name());
			String expect = "{type: io.odysz.anson.AnsT3, ms: null, "
						+ "m: [{type: io.odysz.anson.AnsT3Child}\n"
						+ ", {type: io.odysz.anson.AnsT3son, parent: \"io.odysz.anson.AnsT3\", gendre: \"male\"}\n]}\n";
			assertEquals(expect, s);
		
			// should resolve parent ref with a type guess
			AnsT3 p = (AnsT3) Anson.fromJson(s);
			assertEquals(((AnsT3son)p.m[1]).gendre, "male");
			assertEquals(null, ((AnsT3Child)p.m[0]).parent);
			assertEquals(p, ((AnsT3son)p.m[1]).parent);
		}
		
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			parent.toBlock(bos, JsonOpt.beautify());
			String s = bos.toString(StandardCharsets.UTF_8.name());
			String expect = "{ \"type\": \"io.odysz.anson.AnsT3\",\n"
					+ "  \"ms\": null,\n"
					+ "  \"m\": [{ \"type\": \"io.odysz.anson.AnsT3Child\"}, { \"type\": \"io.odysz.anson.AnsT3son\",\n"
					+ "      \"parent\": \"io.odysz.anson.AnsT3\",\n"
					+ "      \"gendre\": \"male\"}]\n"
					+ "}";
			assertEquals(expect, s);
		}
	}
	
	@Test
	void test_generic_pc() throws AnsonException, IOException {
		AnsT3 enclosing = new AnsT3();
		
		AnsT5Child_paramA c0 = new AnsT5Child_paramA(enclosing);
		AnsT5Child_paramA c1 = new AnsT5Child_paramA(enclosing);
		c1.name = "B";
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		enclosing.toBlock(bos, opt);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		String expect = "{type: io.odysz.anson.AnsT3, ms: null, "
				+ "m: [{type: io.odysz.anson.AnsT5Child_paramA, parent: \"io.odysz.anson.AnsT3\", name: \"param A\"}\n"
				+ ", {type: io.odysz.anson.AnsT5Child_paramA, parent: \"io.odysz.anson.AnsT3\", name: \"B\"}\n]}\n";
		assertEquals(expect, s);
		
		AnsT3 clone = (AnsT3) Anson.fromJson(s);
		assertEquals(enclosing.m.length, clone.m.length);
		assertEquals(c0.name, ((AnsT5Child_paramA)clone.m[0]).name);
		assertEquals(((AnsT5Child_paramA)enclosing.m[1]).name, ((AnsT5Child_paramA)clone.m[1]).name);
		assertEquals(clone, ((AnsT5Child_paramA)clone.m[0]).parent);
		assertEquals(clone, ((AnsT5GeneriChild<?>)clone.m[1]).parent);
	}
	
	@Test
	void test2Json4Enum() throws AnsonException, IOException {
		AnsT4Enum en = new AnsT4Enum();
		en.p = T4_Port.heartbeat;
		en.c = MsgCode.ok;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		en.toBlock(bos, opt);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		bos.close();
		String expect = "{type: io.odysz.anson.AnsT4Enum, p: \"heartbeat\", c: \"ok\", problem: null}\n";
		assertEquals(expect, s);
		
		AnsT4Enum denum = (AnsT4Enum) Anson.fromJson(expect);
		assertEquals(denum.c, MsgCode.ok);
		assertEquals(denum.p, T4_Port.heartbeat);
		
		en.problem = T4_Port.dataset;
		bos = new ByteArrayOutputStream(); 
		en.toBlock(bos, opt);
		s = bos.toString(StandardCharsets.UTF_8.name());
		bos.close();
		expect = "{type: io.odysz.anson.AnsT4Enum, p: \"heartbeat\", c: \"ok\", problem: \"dataset\"}\n";
		assertEquals(expect, s);

		AnsT4Enum problem = (AnsT4Enum) Anson.fromJson(expect);
		assertEquals(MsgCode.ok, problem.c);
		assertEquals(T4_Port.heartbeat, problem.p);
		assertEquals(T4_Port.dataset, problem.problem);
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
	
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			lst.toBlock(bos, opt);
			String s = bos.toString(StandardCharsets.UTF_8.name());
			String expect = "{type: io.odysz.anson.AnsTStrsList, "
					+ "dim4: [[[[{type: io.odysz.anson.AnsT2, b: false, s: 0, c: 0, m: [\"0 0 0 0\"]}\n"
					+ ", {type: io.odysz.anson.AnsT2, b: false, s: 0, c: 0, m: [\"0 0 0 1\"]}\n], "
					+ "[null, null]], [[null, null], [null, null]]], [[[null, null], [null, null]], [[null, null], "
					+ "[null, {type: io.odysz.anson.AnsT2, b: false, s: 0, c: 0, m: [\"1 1 1 1\"]}\n"
					+ "]]]], lst3d: [[[\"0-0-0\", \"\"], [\"0-1-0\"]], [[\"1-0-0\", 1.5], []]], "
					+ "lst: [[], [\"0,0\", \"0,1\", \"0,2\"], [\"1,0\", \"1,1\", \"1,2\"], null, []]}\n";
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
		
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			lst.toBlock(bos, JsonOpt.beautify());
			String s = bos.toString(StandardCharsets.UTF_8.name());
			String expect =
					  "{ \"type\": \"io.odysz.anson.AnsTStrsList\",\n"
					+ "  \"dim4\": [[[[{ \"type\": \"io.odysz.anson.AnsT2\",\n"
					+ "          \"b\": false,\n"
					+ "          \"s\": 0,\n"
					+ "          \"c\": 0,\n"
					+ "          \"m\": [\"0 0 0 0\"]}, { \"type\": \"io.odysz.anson.AnsT2\",\n"
					+ "          \"b\": false,\n"
					+ "          \"s\": 0,\n"
					+ "          \"c\": 0,\n"
					+ "          \"m\": [\"0 0 0 1\"]}], [null, null]], [[null, null], [null, null]]], [[[null, null], [null, null]], [[null, null], [null, { \"type\": \"io.odysz.anson.AnsT2\",\n"
					+ "          \"b\": false,\n"
					+ "          \"s\": 0,\n"
					+ "          \"c\": 0,\n"
					+ "          \"m\": [\"1 1 1 1\"]}]]]],\n"
					+ "  \"lst3d\": [[[\"0-0-0\", \"\"], [\"0-1-0\"]], [[\"1-0-0\", 1.5], []]],\n"
					+ "  \"lst\": [[], [\"0,0\", \"0,1\", \"0,2\"], [\"1,0\", \"1,1\", \"1,2\"], null, []]\n"
					+ "}";

			assertEquals(expect, s);
		}
	}

	@Test
	void testFromJson() throws IllegalArgumentException, ReflectiveOperationException, AnsonException {
		AnsT1 anson = (AnsT1) Anson.fromJson("{type:io.odysz.anson.AnsT1, ver: \"v0.1\", "
				+ "m: {type:io.odysz.anson.AnsT1$AnsM1, \"name\": \"x\"}}");
		assertEquals("x", anson.m.name);

		anson = (AnsT1) Anson.fromJson("{type: io.odysz.anson.AnsT1, ver: \"v0.1\"}");
		assertEquals("v0.1", anson.ver);
		assertEquals(null, anson.m);

		anson = (AnsT1) Anson.fromJson("{type: io.odysz.anson.AnsT1, ver: \"v0\\n.\\r\\n1\", m: null}");
		assertEquals("v0\n.\r\n1", anson.ver);
		assertEquals(null, anson.m);

		AnsT2 anson2 = (AnsT2) Anson.fromJson("{type:io.odysz.anson.AnsT2, b: true, c: \"c\", m: [\"e1\\nvalue\", \"e2\"]}");
		assertEquals(true, anson2.b);
		assertEquals('c', anson2.c);
		assertEquals("e1\nvalue", anson2.m[0]);
		assertEquals("e2", anson2.m[1]);

		anson2 = (AnsT2) Anson.fromJson("{type:io.odysz.anson.AnsT2, m: ["
				+ "\"Cannot create PoolableConnectionFactory (ORA-28001: xxx\\n)\", "
				+ "\"Cannot create PoolableConnectionFactory (ORA-28001: xxx\\\\n)\"]}");
		assertEquals("Cannot create PoolableConnectionFactory (ORA-28001: xxx\n)", anson2.m[0]);
		assertEquals("Cannot create PoolableConnectionFactory (ORA-28001: xxx\\n)", anson2.m[1]);
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

		cll = (AnsTList) Anson.fromJson("{type: io.odysz.anson.AnsTList, ans2: ["
				+ "{type: io.odysz.anson.AnsT2, s: 4 }, "
				+ "{type: io.odysz.anson.AnsT1, ver: \"z\" }]}");
		assertEquals(2, cll.ans2.size());
		assertEquals(4, ((AnsT2)cll.ans2.get(0)).s);
		assertEquals("z", ((AnsT1)cll.ans2.get(1)).ver);
	}
	
	/**
	 * @since 0.9.30 also test windows path escape
	 * @throws AnsonException
	 */
	@Test
	void testFromJson_list2d() throws AnsonException {
		T_ListPhoto cll = (T_ListPhoto) Anson.fromJson("{type: io.odysz.anson.T_ListPhoto, ansp: [["
				+ "{type: io.odysz.anson.AnsPhoto, pid: \"1\", clientpath: \"raw\\\\res\\\\my.jpg\" },"
				+ "{type: io.odysz.anson.AnsPhoto, pid: \"2\", clientpath: \"raw/res/my.jpg\"} ]],"
				+ "\"checkRels\":[[{\"name\":\"oid\",\"value\":\"ap01\"},{\"name\":\"pid\",\"value\":\"2023_12\"}]]}");
		assertEquals(1, cll.ansp.size());
		assertEquals(2, cll.ansp.get(0).length);
		assertEquals("2", cll.ansp.get(0)[1].pid);
		assertEquals("raw\\res\\my.jpg", cll.ansp.get(0)[0].clientpath);
		assertEquals("raw/res/my.jpg", cll.ansp.get(0)[1].clientpath);
		assertEquals("oid", cll.checkRels[0][0].name);
		assertEquals("ap01", cll.checkRels[0][0].value);
		assertEquals("pid", cll.checkRels[0][1].name);
		assertEquals("2023_12", cll.checkRels[0][1].value);
		
		/* FIXME error report: line 1:47 extraneous input '<EOF>' expecting {',', '}'}
		 * This should be a grammar error.
		 * g4:
			array
			: '[' value (',' value)* ']'
			| '[' ']'
			;
		*/
		cll = (T_ListPhoto) Anson.fromJson("{type: io.odysz.anson.T_ListPhoto, ansp: [[]]");
		assertEquals(1, cll.ansp.size());
		assertEquals(0, cll.ansp.get(0).length);

	}

	@Test
	void testFromJson_map() throws ReflectiveOperationException, AnsonException, IOException {
		AnsTMap m = (AnsTMap) Anson.fromJson("{type: io.odysz.anson.AnsTMap, ver: null, map: {\"A\": \"B\"}}");
		assertEquals("B", m.map.get("A"));

		m = (AnsTMap) Anson.fromJson("{type: io.odysz.anson.AnsTMap, map: {\"A\": \"B\"}, mapArr: {a: [1, \"s\"]}}\n");
		assertEquals("B", m.map.get("A"));
		assertEquals(2, m.mapArr.get("a").length);
		assertEquals(1, m.mapArr.get("a")[0]);
		assertEquals("s", m.mapArr.get("a")[1]);
		
		m.map.put("v-null", null);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		opt.quotKey(true);
		m.toBlock(bos, opt);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{\"type\": \"io.odysz.anson.AnsTMap\", \"map\": {\"A\": \"B\", \"v-null\": null}, \"mapArr\": {\"a\": [1, \"s\"]}}\n", s);
		bos.close();

		bos = new ByteArrayOutputStream(); 
		opt.quotKey(false);
		m.toBlock(bos, opt);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsTMap, map: {A: \"B\", v-null: null}, mapArr: {a: [1, \"s\"]}}\n", s);
	}
		
	@Test
	void testFromJson_rs() throws IllegalArgumentException, ReflectiveOperationException, SQLException, AnsonException {
		AnsTRs rs = (AnsTRs) Anson.fromJson("{type: io.odysz.anson.AnsTRs, rs: "
				+ "{type: io.odysz.semantic.T_AnResultset, stringFormats: null, total: 0, rowCnt: 3, colCnt: 4,"
				+ " colnames: {\"1\": [1, \"1\"], \"2\": [2, \"2\"], \"3\": [3, \"3\"], \"4\": [4, \"4\"]},"
				+ " rowIdx: 0, results: [[\"0 1\", \"0 2\", \"0 3\", \"0 4\"], [\"1 1\", \"1 2\", \"1 3\", \"1 4\"], [\"2 1\", \"2 2\", \"2 3\", \"2 4\"]]"
				+ "}}");
		
		assertEquals(3, rs.rs.getRowCount());
		rs.rs.beforeFirst().next();
		assertEquals("0 1", rs.rs.getString("1"));

		rs = (AnsTRs) Anson.fromJson("{type: io.odysz.anson.AnsTRs, rs: "
				+ "{type: io.odysz.semantic.T_AnResultset, stringFormats: null, total: 0, rowCnt: 3, colCnt: 4,"
				+ " colnames: {\"1\": [1, \"1\"], \"2\": [2, \"2\"], \"3\": [3, \"3\"], \"4\": [4, \"4\"]},"
				+ " rowIdx: 0, results: [[\"0, 1\", \"0, 2\", \"0, 3\", \"0, 4\"], [\"1, 1\", \"1, 2\", \"1, 3\", \"1, 4\"], [\"2, 1\", \"2, 2\", \"2, 3\", \"2, 4\"]]"
				+ "}}");
		
		assertEquals(3, rs.rs.getRowCount());
		rs.rs.beforeFirst().next();
		assertEquals("0, 1", rs.rs.getString("1"));
	}
	
	@Test
	void testFromJson_tree() throws AnsonException, IOException {
		T_AnTreeNode tr = (T_AnTreeNode) Anson.fromJson(
			  "{type:io.odysz.anson.T_AnTreeNode,"
				+ "node:{\"fullpath\":\"1 sys.1 domain\","
					  + "\"id\":\"sys-domain\",\"text\":\"Domain Settings\","
					  + "\"sort\":\"1\",\"parentId\":\"sys\",\"url\":\"views/sys/domain/domain.html\""
					 + "}},"
			+ "{type:io.odysz.anson.T_AnTreeNode,"
				+ "node:{\"fullpath\":\"1 sys.2 role\","
					  + "\"id\":\"sys-role\",\"text\":\"Role Manage\","
					  + "\"sort\":\"2\",\"parentId\":\"sys\",\"url\":\"views/sys/role/roles.html\""
					 + "}},"
			+ "{type:io.odysz.anson.T_AnTreeNode,"
				+ "node:{\"fullpath\":\"1 sys.3 org\","
					  + "\"id\":\"sys-org\",\"text\":\"Orgnization Manage\","
					  + "\"sort\":\"3\",\"parentId\":\"sys\",\"url\":\"views/sys/org/orgs.html\""
					 + "}},"
			+ "{type:io.odysz.anson.T_AnTreeNode,"
				+ "node:{\"fullpath\":\"1 sys.4 user\","
					  + "\"id\":\"sys-uesr\",\"text\":\"Uesr Manage\","
					  + "\"sort\":\"4\",\"parentId\":\"sys\",\"url\":\"views/sys/user/users.html\""
					 + "}},"
			+ "{type:io.odysz.anson.T_AnTreeNode,"
				+ "node:{\"fullpath\":\"1 sys.5 wf\","
					  + "\"id\":\"sys-wf\",\"text\":\"Workflow Settings\","
					  + "\"sort\":\"5\",\"parentId\":\"sys\",\"url\":\"views/sys/workflow/workflows.html\""
					 + "}}" 
				);

		// only last node here?
		assertEquals(6, tr.node.size());
		
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			tr.toBlock(bos, JsonOpt.beautify());
			String s = bos.toString(StandardCharsets.UTF_8.name());
			String expect =
					    "{ \"type\": \"io.odysz.anson.T_AnTreeNode\",\n"
					  + "  \"node\": {\"fullpath\": \"1 sys.1 domain\",\n"
					  + "      \"id\": \"sys-domain\",\n"
					  + "      \"text\": \"Domain Settings\",\n"
					  + "      \"sort\": \"1\",\n"
					  + "      \"parentId\": \"sys\",\n"
					  + "      \"url\": \"views/sys/domain/domain.html\"}\n"
					  + "}";

			assertEquals(expect, s);
		}
	}
	
	@Test
	void testSTree2block() throws AnsonException, IOException {
		T_TreeIndenode n0  = new T_TreeIndenode("0");
		n0 .child(new T_TreeIndenode("0.1", n0 ));
		n0 .child(new T_TreeIndenode("0.2", n0 ).asLastSibling());

		n0.toBlock(); // test case for multiple DP visiting

		/*
		 * (+)0
		 *  |- 0.1
		 *  L 0.2
		 */
		ArrayList<T_IndentFlag> ind01 = ((T_TreeIndenode)n0 .child(0))
				.indents();
		assertEquals(1, len(ind01));
		assertEquals(T_IndentFlag.childi, ind01.get(0));

		ArrayList<T_IndentFlag> ind0_2 = ((T_TreeIndenode)n0 .child(1))
				.indents();
		assertEquals(1, len(ind0_2));
		assertEquals(T_IndentFlag.childx, ind0_2.get(0));

		n0  = new T_TreeIndenode("1");
		T_TreeIndenode n11 = new T_TreeIndenode("1.1", n0 ).asLastSibling();
		T_TreeIndenode n111 = new T_TreeIndenode("1.1.1", n11).asLastSibling();
		n0 .child(n11);
		n11.child(n111);
		
		n111.toBlock();
		/**
		 * (+)1
		 *  L  1.1
		 *    L 1.1.1
		 * */
		ArrayList<T_IndentFlag> ind1_1_1 = n111.indents();
		assertEquals(2, len(ind1_1_1));
		assertEquals(T_IndentFlag.space, ind1_1_1.get(0));
		assertEquals(T_IndentFlag.childx, ind1_1_1.get(1));

		T_TreeIndenode n2    = new T_TreeIndenode("2");
		T_TreeIndenode n21   = new T_TreeIndenode("2.1", n2 );
		T_TreeIndenode n211  = new T_TreeIndenode("2.1.1", n21).asLastSibling();
		T_TreeIndenode n2111 = new T_TreeIndenode("2.1.1.1", n211).asLastSibling();

		n2  .child(n21);
		n21 .child(n211);
		n211.child(n2111);

		T_TreeIndenode n22   = new T_TreeIndenode("2.2", n2 ).asLastSibling();
		n2.child(n22);
		
		assertEquals("{\"type\": \"io.odysz.semantic.ext.test.T_TreeIndenode\", "
				+ "\"node\": {}, \"parent\": \"io.odysz.semantic.ext.test.T_TreeIndenode\", "
				+ "\"indents\": [\"vlink\", \"space\", \"childx\"], "
				+ "\"lastSibling\": true, \"id\": \"2.1.1.1\", \"parentId\": \"2.1.1\"}\n",
				n2111.toBlock());
		/**
		 * (+)2 or
		 *  2
		 *  |-2.1
		 *  | L 2.1.1
		 *  |   L 2.1.1.1 
		 *  L 2.2
		 * */
		ArrayList<T_IndentFlag> ind21 = n21.indents();
		assertEquals(1, len(ind21));
		assertEquals(T_IndentFlag.childi, ind21.get(0));

		ArrayList<T_IndentFlag> ind211 = n211.indents();
		assertEquals(2, len(ind211));
		assertEquals(T_IndentFlag.vlink, ind211.get(0));
		assertEquals(T_IndentFlag.childx, ind211.get(1));

		ArrayList<T_IndentFlag> ind2111 = n2111.indents();
		assertEquals(3, len(ind2111));
		assertEquals(T_IndentFlag.vlink, ind2111.get(0));
		assertEquals(T_IndentFlag.space, ind2111.get(1));
		assertEquals(T_IndentFlag.childx, ind2111.get(2));

		ArrayList<T_IndentFlag> ind22 = n22.indents();
		assertEquals(1, len(ind22));
		assertEquals(T_IndentFlag.childx, ind22.get(0));
		
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			n0.toBlock(bos, JsonOpt.beautify());
			String s = bos.toString(StandardCharsets.UTF_8.name());
			String expect =
					      "{ \"type\": \"io.odysz.semantic.ext.test.T_TreeIndenode\",\n"
					    + "  \"node\": {\"children\": [{ \"type\": \"io.odysz.semantic.ext.test.T_TreeIndenode\",\n"
					    + "          \"node\": {\"children\": [{ \"type\": \"io.odysz.semantic.ext.test.T_TreeIndenode\",\n"
					    + "                  \"node\": {},\n"
					    + "                  \"parent\": \"io.odysz.semantic.ext.test.T_TreeIndenode\",\n"
					    + "                  \"indents\": [\"space\", \"childx\"],\n"
					    + "                  \"lastSibling\": true,\n"
					    + "                  \"id\": \"1.1.1\",\n"
					    + "                  \"parentId\": \"1.1\"}]},\n"
					    + "          \"parent\": \"io.odysz.semantic.ext.test.T_TreeIndenode\",\n"
					    + "          \"indents\": [\"childx\"],\n"
					    + "          \"lastSibling\": true,\n"
					    + "          \"id\": \"1.1\",\n"
					    + "          \"parentId\": \"1\"}]},\n"
					    + "  \"parent\": \"io.odysz.semantic.ext.test.T_TreeIndenode\",\n"
					    + "  \"indents\": [],\n"
					    + "  \"lastSibling\": false,\n"
					    + "  \"id\": \"1\",\n"
					    + "  \"parentId\": null\n"
					    + "}";

			assertEquals(expect, s);
		}
	}
	
	@Test
	void test_innerClass() throws AnsonException, IOException {
		String tst = "{type: io.odysz.anson.T_AnTreeNode$SubTree, "
		+ "children: [{type: io.odysz.anson.T_AnTreeNode, "
				   + "node: {fullpath: \"1 sys.1 domain\", "
				   + "id: \"sys-domain\", text: \"Domain Settings\", "
				   + "sort: \"1\", parentId: \"sys\", url: \"views/sys/domain/domain.html\""
				   + "}}\n]}\n";
		SubTree tr = (SubTree) Anson.fromJson(tst);
	
		assertEquals(1, tr.children.size());
		assertEquals(6, tr.children.get(0).node.size());
		assertEquals("sys-domain", tr.children.get(0).node.get("id"));
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		tr.toBlock(bos, opt);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals(tst, s);
	}

	/**
	 * <p>Note about v0.9.14</p>
	 * I'm not sure what's bypass test want to do here - too long ago idea.
	 * This test result in error since v0.9.14 (change AnsonException as subclass of RuntimeException).
	 * It's fixed with register a factory to {@link AnsT6Bypass.T6_Port}, which is probable not
	 * initial intention of this test.
	 * 
	 * @throws AnsonException
	 * @throws IOException
	 */
	@Test
	void testBypass() throws AnsonException, IOException {
		AnsT6Bypass t6 = new AnsT6Bypass();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		t6.toBlock(bos, opt);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		
		assertEquals("{type: io.odysz.anson.AnsT6Bypass, p: \"session\"}\n", s);
		
		AnsT6Bypass bypass = (AnsT6Bypass) Anson.fromJson(s);
		
		assertEquals(AnsT6Bypass.T6_Port.session, t6.p.port());
		assertEquals(bypass.p.port(), t6.p.port());
	}
}
