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

}
