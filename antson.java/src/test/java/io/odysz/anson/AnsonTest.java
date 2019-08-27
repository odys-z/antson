package io.odysz.anson;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnsonTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testToJson() {
		fail("Not yet implemented");
	}

	@Test
	void testFromJson() throws IllegalArgumentException, ReflectiveOperationException {
		AnsT1 anson = (AnsT1) Anson.fromJson("{type:io.odysz.anson.AnsT1, seq: 1, ver: \"v0.1\", m: {\"name\": \"x\"}}");
		assertEquals(1, anson.seq);
		assertEquals("x", anson.m.name);

		AnsT2 anson2 = (AnsT2) Anson.fromJson("{type:io.odysz.anson.AnsT2, seq: 2, ver: \"v0.1\", m: [\"e1\", \"e2\"]}");
		assertEquals(2, anson2.seq);
		assertEquals("e1", anson2.m[0]);
		assertEquals("e2", anson2.m[1]);

		// AnsT3.m is typeof AnsT2
		AnsT3 anson3 = (AnsT3) Anson.fromJson("{type:io.odysz.anson.AnsT3, seq: 3, ver: \"v0.1\", m: [" +
				"{seq: 4, ver: \"v0.1\"}, {seq: 5, ver: \"v0.1\"}]}");
		assertEquals(4, anson3.m[0].seq);
	}

}
