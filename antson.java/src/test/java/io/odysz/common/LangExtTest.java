package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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
		assertTrue(isNull((Object[])null));
		assertFalse(isNull(new Object[] {""}));
		assertFalse(isNull(new Object[] {null, ""}));

		assertTrue(isNull((List<?>)null));
	}

	/**
	 *<pre>
	assertFalse(endWith(null, new String[] {}));
	assertFalse(endWith("", new String[] {}));
	assertTrue(endWith(" "));
	assertTrue(endWith(" ", " "));
	assertTrue(endWith("anything"));
	assertTrue(endWith("anything", ""));
	assertTrue(endWith("anything", "g", "ing"));
	assertFalse(endWith("anythin-", "g", "ing"));
	assertTrue(endWith("anythin-", "g", "ing", "-"));
	</pre>
	 */
	@Test
	void testEndwith() {
		assertFalse(endWith(null, new String[] {}));
		assertFalse(endWith("", new String[] {}));
		assertTrue(endWith(" "));
		assertTrue(endWith(" ", " "));
		assertTrue(endWith("anything"));
		assertTrue(endWith("anything", ""));
		assertTrue(endWith("anything", "g", "ing"));
		assertFalse(endWith("anythin-", "g", "ing"));
		assertTrue(endWith("anythin-", "g", "ing", "-"));
	}
}
