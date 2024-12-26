package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import static io.odysz.common.Regex.*;

import org.junit.jupiter.api.Test;

class RegexTest {

	@Test
	void testIsHttps() {
		assertTrue(isHttps("https://odys-z.github.io"));
		assertTrue(isHttp("http://odys-z.github.io"));
		assertFalse(isHttps("http://odys-z.github.io"));
	}

}
