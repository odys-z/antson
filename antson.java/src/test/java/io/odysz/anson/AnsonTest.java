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
		Anson anson = new Anson().fromJson("{type:\"io.odysz.anson.Anson\", seq: \"1\"}");
		assertEquals("1", anson.seq);
	}

}
