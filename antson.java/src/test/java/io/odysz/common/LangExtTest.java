package io.odysz.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import static io.odysz.common.LangExt.*;

import org.apache.commons.text.StringEscapeUtils;
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
		assertFalse(eq((String)null, "b"));
		assertTrue(eq((String)null, null));
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
	
	@Test
	void testJoin() {
		assertEquals("a,b", join(null, new String[] { "a", "b" }));
		assertEquals("a-b", join("-",  new String[] { "a", "b" }));
		assertEquals("", join("-",  new String[] {}));
		assertEquals("", join(null, new String[] {}));
		assertEquals(null, join(null, (String[])null));

		assertEquals("a\\nx\nb", join("\n", "\\\\n", new String[] { "a\nx", "b"}));

		assertEquals("a\\nx\\ny\nb", join("\n", "\\\\n", new String[] { "a\nx\ny", "b"}));
		String c = join("\n", "\\\\n", new String[] { "a\nx\\ny", "b"});
		assertEquals("a\\nx\\ny\nb", c);
		String e = StringEscapeUtils.unescapeJava(c);
		assertEquals("a\nx\ny\nb", e);

		assertEquals("a\\nx\\ny\nb", join("\n", "\\\\n", new String[] { "a\nx\ny", "b"}));
		String hybrides = "a\nx\\ny";
		c = join("\n", "\\\\n", new String[] { StringEscapeUtils.escapeJava(hybrides), "b"});
		assertEquals("a\\nx\\\\ny\nb", c);
		e = StringEscapeUtils.unescapeJava(c);
		assertEquals(hybrides + "\nb", e);
		
		assertEquals("a\\nx\\ny\nb", joinEsc("\n", "\\\\n", new String[] { "a\nx\ny", "b"}));
		c = joinEsc("\n", "\\\\n", new String[] { hybrides, "b"});
		assertEquals("a\\nx\\\\ny\nb", c);
		e = StringEscapeUtils.unescapeJava(c);
		assertEquals(hybrides + "\nb", e);
	}
}
