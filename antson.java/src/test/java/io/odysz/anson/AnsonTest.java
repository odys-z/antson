package io.odysz.anson;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
		
		AnsTCollect cll = new AnsTCollect();
		cll.lst.add("A");
		cll.lst.add("B");
		bos = new ByteArrayOutputStream(); 
		cll.toBlock(bos);
		s = bos.toString(StandardCharsets.UTF_8.name());
		assertEquals("{type: io.odysz.anson.AnsTCollect, anss: null, ver: null, lst: [\"A\", \"B\"], seq: 0}", s);
	}

	@Test
	void testFromJson() throws IllegalArgumentException, ReflectiveOperationException {
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
	void testFromJson_asonArr() throws IllegalArgumentException, ReflectiveOperationException {
		// AnsT3.m is typeof AnsT2
		AnsT3 anson3 = (AnsT3) Anson.fromJson("{type: io.odysz.anson.AnsT3, seq: 3, ver: \"v0.1\", m: [" +
				"{type: io.odysz.anson.AnsT2, s: 4 }, " + 
				"{type: io.odysz.anson.AnsT1, ver: \"x\" }]}");
		assertEquals(2, anson3.m.length);
		assertEquals(4, ((AnsT2)anson3.m[0]).s);
		assertEquals("x", ((AnsT1)anson3.m[1]).ver);
	}
		
	@Test
	void testFromJson_list() throws IllegalArgumentException, ReflectiveOperationException {
		AnsTCollect cll = (AnsTCollect) Anson.fromJson("{type: io.odysz.anson.AnsTCollect, ver: null, lst: [\"A\", \"B\"], seq: 0}");
		assertEquals(2, cll.lst.size());
		assertEquals("A", cll.lst.get(0));
		assertEquals("B", cll.lst.get(1));
		
		
		cll = (AnsTCollect) Anson.fromJson("{type: io.odysz.anson.AnsTCollect, anss: ["
				+ "{type: io.odysz.anson.AnsT3, seq: 11}, "
				+ "{type: io.odysz.anson.AnsT3, seq: 12}"
				+ "], seq: 1}");
		assertEquals(2, cll.anss.size());
		assertEquals(11, cll.anss.get(0).seq);
		assertEquals(12, cll.anss.get(1).seq);

		cll = (AnsTCollect) Anson.fromJson("{type: io.odysz.anson.AnsTCollect, anss: ["
				+ "{type: io.odysz.anson.AnsT3, seq: 11 ver: \"v0.1\", m: ["
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
	void testFromJson_rs() throws IllegalArgumentException, ReflectiveOperationException {
		
	}
}
