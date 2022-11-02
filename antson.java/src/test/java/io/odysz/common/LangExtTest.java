package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;
import static io.odysz.common.LangExt.*;

import org.junit.jupiter.api.Test;

class LangExtTest {

	@Test
	void testIs() {
		assertFalse(is(null));
		assertTrue(is(null, true));

		assertFalse(is(new boolean[] {false}));
		assertFalse(is(new boolean[] {false}, false));
		assertTrue(is(new boolean[] {true}));
		assertTrue(is(new boolean[] {}, true));
		assertTrue(is(new boolean[] {true}, false));
	}

	@Test
	void testIsNull() {
		assertTrue(isNull(new Object[] {}));
		assertTrue(isNull(new Object[] {null}));
		assertTrue(isNull(new Object[0]));
		assertTrue(isNull(null));
		assertFalse(isNull(new Object[] {""}));
		assertFalse(isNull(new Object[] {null, ""}));
	}
}
