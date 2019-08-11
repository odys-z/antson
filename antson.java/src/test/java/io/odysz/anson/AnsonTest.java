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
	}

}
