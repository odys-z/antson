package io.odysz.anson;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.odysz.anson.AnsT4Enum.MsgCode;
import io.odysz.anson.AnsT4Enum.Port;
import io.odysz.anson.x.AnsonException;

class AnsonTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testToJson() throws Exception {
		AnsT1 anson = new AnsT1();
		anson.seq = 1;
		anson.ver = "v0.1";
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		anson.toBlock(bos);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsT1, ver: \"v0.1\", m: null, seq: 1}", s);

		AnsT2 a2 = new AnsT2();
		a2.m = new String[] {"e0", "e1"};
		a2.seq = 2;
		bos = new ByteArrayOutputStream(); 
		a2.toBlock(bos);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsT2, ver: null, s: 0, m: [\"e0\", \"e1\"], seq: 2}", s);
		
		Ans2dArr a2d = new Ans2dArr();
		a2d.strs = new String[][] {
			new String[] {"0.0", "0.1"},
			new String[] {"1.0", "1.1", "1.2"},
			new String[] {"2.0"},
			new String[] {"3.0", "3.1"},
			new String[] {} };
		bos = new ByteArrayOutputStream(); 
		a2d.toBlock(bos);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.Ans2dArr, ver: null, strs: [[\"0.0\", \"0.1\"], [\"1.0\", \"1.1\", \"1.2\"], [\"2.0\"], [\"3.0\", \"3.1\"], []], seq: 0}", s);
		
		AnsTList cll = new AnsTList();
		cll.lst.add("A");
		cll.lst.add("B");
		bos = new ByteArrayOutputStream(); 
		cll.toBlock(bos);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsTList, anss: null, ver: null, lst: [\"A\", \"B\"], seq: 0}", s);

		AnsTRs anrs = new AnsTRs();
		bos = new ByteArrayOutputStream(); 
		anrs.toBlock(bos);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsTRs, rs: "
				+ "{type: io.odysz.anson.AnsonResultset, stringFormats: null, total: 0, ver: null, rowCnt: 3, colCnt: 4,"
				+ " colnames: {\"1\": [1, \"1\"], \"2\": [2, \"2\"], \"3\": [3, \"3\"], \"4\": [4, \"4\"]},"
				+ " rowIdx: 0, results: [[\"0, 1\", \"0, 2\", \"0, 3\", \"0, 4\"], [\"1, 1\", \"1, 2\", \"1, 3\", \"1, 4\"], [\"2, 1\", \"2, 2\", \"2, 3\", \"2, 4\"]],"
				+ " seq: 0}, ver: null, seq: 0}", s);
	}

	@SuppressWarnings("unused")
	@Test
	void test2Json_PC() throws Exception {
		AnsT3 parent = new AnsT3();
		parent.seq = 1;
		parent.ver = "v0.1";
		
		AnsT3Child c = new AnsT3Child(parent);
		AnsT3son son = new AnsT3son(parent);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		parent.toBlock(bos);
		String s = bos.toString(StandardCharsets.UTF_8.name());
		String expect = "{type: io.odysz.anson.AnsT3, ver: \"v0.1\", "
						+ "m: [{type: io.odysz.anson.AnsT3Child, ver: null, seq: 0}, "
							+ "{type: io.odysz.anson.AnsT3son, gendre: \"male\", ver: null, seq: 0}], seq: 1}";
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
		String expect = "{type: io.odysz.anson.AnsT4Enum, p: \"heartbeat\", ver: null, c: \"ok\", seq: 0}";
		assertEquals(expect, s);
		
		AnsT4Enum denum = (AnsT4Enum) Anson.fromJson(expect);
		assertEquals(denum.c, MsgCode.ok);
		assertEquals(denum.p, Port.heartbeat);
	}

	@Test
	void testFromJson() throws IllegalArgumentException, ReflectiveOperationException, AnsonException {
		AnsT1 anson = (AnsT1) Anson.fromJson("{type:io.odysz.anson.AnsT1, seq: 1, ver: \"v0.1\", m: {\"name\": \"x\"}}");
		assertEquals(1, anson.seq);
		assertEquals("x", anson.m.name);

		anson = (AnsT1) Anson.fromJson("{type: io.odysz.anson.AnsT1, ver: \"v0.1\"}");
		assertEquals("v0.1", anson.ver);
		assertEquals(null, anson.m);

		anson = (AnsT1) Anson.fromJson("{type: io.odysz.anson.AnsT1, ver: \"v0.1\", m: null}");
		assertEquals("v0.1", anson.ver);
		assertEquals(null, anson.m);

		AnsT2 anson2 = (AnsT2) Anson.fromJson("{type:io.odysz.anson.AnsT2, seq: 2, ver: \"v0.1\", m: [\"e1\", \"e2\"]}");
		assertEquals(2, anson2.seq);
		assertEquals("e1", anson2.m[0]);
		assertEquals("e2", anson2.m[1]);
	}
	
	@Test
	void testFromJson_asonArr() throws IllegalArgumentException, ReflectiveOperationException, AnsonException {
		// AnsT3.m is typeof AnsT2
		AnsT3 anson3 = (AnsT3) Anson.fromJson("{type: io.odysz.anson.AnsT3, seq: 3, ver: \"v0.1\", m: [" +
				"{type: io.odysz.anson.AnsT2, s: 4 }, " + 
				"{type: io.odysz.anson.AnsT1, ver: \"x\" }]}");
		assertEquals(2, anson3.m.length);
		assertEquals(4, ((AnsT2)anson3.m[0]).s);
		assertEquals("x", ((AnsT1)anson3.m[1]).ver);
	}
		
	@Test
	void testFromJson_list() throws IllegalArgumentException, ReflectiveOperationException, AnsonException {
		AnsTList cll = (AnsTList) Anson.fromJson("{type: io.odysz.anson.AnsTList, ver: null, lst: [\"A\", \"B\"], seq: 0}");
		assertEquals(2, cll.lst.size());
		assertEquals("A", cll.lst.get(0));
		assertEquals("B", cll.lst.get(1));
		
		
		cll = (AnsTList) Anson.fromJson("{type: io.odysz.anson.AnsTList, anss: ["
				+ "{type: io.odysz.anson.AnsT3, seq: 11}, "
				+ "{type: io.odysz.anson.AnsT3, seq: 12}"
				+ "], seq: 1}");
		assertEquals(2, cll.anss.size());
		assertEquals(11, cll.anss.get(0).seq);
		assertEquals(12, cll.anss.get(1).seq);

		cll = (AnsTList) Anson.fromJson("{type: io.odysz.anson.AnsTList, anss: ["
				+ "{type: io.odysz.anson.AnsT3, seq: 11, ver: \"v0.1\", m: ["
					+ "{type: io.odysz.anson.AnsT2, s: 4 }, "
					+ "{type: io.odysz.anson.AnsT1, ver: \"x\" }]}, "
				+ "{type: io.odysz.anson.AnsT3, seq: 12}"
				+ "], seq: 1}");
		assertEquals(2, cll.anss.size());
		assertEquals(11, cll.anss.get(0).seq);
		assertEquals(4, ((AnsT2)cll.anss.get(0).m[0]).s);
		assertEquals("x", ((AnsT1)cll.anss.get(0).m[1]).ver);
		assertEquals(12, cll.anss.get(1).seq);
	}

	@Test
	void testFromJson_map() throws IllegalArgumentException, ReflectiveOperationException, AnsonException {
		AnsTMap m = (AnsTMap) Anson.fromJson("{type: io.odysz.anson.AnsTMap, ver: null, map: {\"A\": \"B\"}}");
		assertEquals(null, m.ver);
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
				+ "{type: io.odysz.anson.AnsonResultset, stringFormats: null, total: 0, ver: null, rowCnt: 3, colCnt: 4,"
				+ " colnames: {\"1\": [1, \"1\"], \"2\": [2, \"2\"], \"3\": [3, \"3\"], \"4\": [4, \"4\"]},"
				+ " rowIdx: 0, results: [[\"0 1\", \"0 2\", \"0 3\", \"0 4\"], [\"1 1\", \"1 2\", \"1 3\", \"1 4\"], [\"2 1\", \"2 2\", \"2 3\", \"2 4\"]],"
				+ " seq: 0}, ver: null, seq: 0}");
		
		assertEquals(3, rs.rs.getRowCount());
		rs.rs.beforeFirst().next();
		assertEquals("0 1", rs.rs.getString("1"));

		rs = (AnsTRs) Anson.fromJson("{type: io.odysz.anson.AnsTRs, rs: "
				+ "{type: io.odysz.anson.AnsonResultset, stringFormats: null, total: 0, ver: null, rowCnt: 3, colCnt: 4,"
				+ " colnames: {\"1\": [1, \"1\"], \"2\": [2, \"2\"], \"3\": [3, \"3\"], \"4\": [4, \"4\"]},"
				+ " rowIdx: 0, results: [[\"0, 1\", \"0, 2\", \"0, 3\", \"0, 4\"], [\"1, 1\", \"1, 2\", \"1, 3\", \"1, 4\"], [\"2, 1\", \"2, 2\", \"2, 3\", \"2, 4\"]],"
				+ " seq: 0}, ver: null, seq: 0}");
		
		assertEquals(3, rs.rs.getRowCount());
		rs.rs.beforeFirst().next();
		assertEquals("0, 1", rs.rs.getString("1"));
	}
	
}
