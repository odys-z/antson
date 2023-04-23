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
	
	@Test
	void testTrimStrEq() {
		assertEquals("", str(new Object[] {}));
		assertEquals("9", str(9));
		assertEquals("9,1", str(new int[] {9,1}));
		assertEquals("9,1", str(new Integer[] {9,1}));
		assertEquals("9,1", str(new String[] {"9","1"}));
		
		assertEquals("", trim(" "));
		assertEquals("9", trim(" 9"));
		assertEquals("9", trim("9 "));
		
		assertFalse(eq("a", null));
		assertFalse(eq(null, "b"));
		assertTrue(eq(null, null));
		assertTrue(eqs(null, null));
		assertTrue(eqs());
		assertTrue(eqs((String[])null));
		assertFalse(eqs("a"));
		assertFalse(eqs("a", "b"));
		assertTrue(eqs("a", "a"));
		assertFalse(eqs("a", "a", "a"));
		assertFalse(eqs("a", "a", "a", "b"));
		assertTrue(eqs("a", "a", "a", "a"));
	}
}
