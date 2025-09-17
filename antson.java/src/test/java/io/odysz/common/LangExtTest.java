package io.odysz.common;

import static io.odysz.common.LangExt.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;

class LangExtTest {

	@Test
	void testIs() {
		assertFalse(is(null));
		assertTrue (is(null, true));

		assertFalse(is(new boolean[] {false}));
		assertFalse(is(new boolean[] {false}, false));
		assertTrue (is(new boolean[] {true}));
		assertTrue (is(new boolean[] {}, true));
		assertTrue (is(new boolean[] {true}, false));
	}
	
	@Test
	void testBool() {
		assertFalse(bool(null));
		assertTrue (bool("1"));
		assertFalse (bool("0"));
		assertTrue (bool("true"));
		assertTrue (bool("True"));
		assertTrue (bool("T"));
		assertFalse (bool("False"));
		assertTrue (bool("y"));
		assertTrue (bool("1"));
		assertTrue (bool("Y"));
		assertTrue (bool("Yes"));
		assertFalse (bool("none"));
		assertFalse (bool("undefined"));
		assertFalse (bool("null"));

		assertFalse (bool(0));
		assertTrue (bool(1));
		assertTrue (bool(0.1f));
	}
	
	@Test
	void testIndexOf() {
		assertEquals(1, indexOf(new String[] {"a", "b", "c"}, "b"));
		assertEquals(-1, indexOf(new String[] {}, "b"));

		assertEquals(-1, indexOf(new Integer[] {}, 0));
		assertEquals(1, indexOf(new Integer[] {1, 0}, 0));

		assertEquals(1, indexOf(new ArrayList<String>() {{add("a");}; {add("b");}; {add("c");};}, "b"));
		assertEquals(-1, indexOf(new ArrayList<String>() {}, "b"));
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
		

		assertTrue(isNull((int[])null));
		assertTrue(isNull(new int[] {}));
		assertTrue(isNull(new int[0]));

		assertTrue(isNull(new char[] {}));
		assertTrue(isNull(new float[] {}));
		assertTrue(isNull(new short[] {}));
		assertTrue(isNull(new double[] {}));
		
		assertTrue(isNull(new String[] {}));

		assertTrue(isNull(""));
		assertFalse(isNull(0));
	}
	
	@Test
	void testIsblank() {
		assertTrue(isblank("家", ".", "/"));
		assertTrue(isblank("H", ".", "/"));
		assertFalse(isblank("家", "\\.", "/"));
		assertFalse(isblank("H", "\\.", "/"));
		assertTrue(isblank(".", "\\.", "/"));
		assertTrue(isblank("/", "\\.", "/"));
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
		assertTrue(eq("A", "a", true));
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
	void testConcatArr() {
		assertArrayEquals(new String[] {"a", "b"}, concatArr(new String[] {"a"}, new String[] {"b"}));
		assertArrayEquals(new String[] {"a", "b"}, concatArr(new String[] {}, new String[] {"a", "b"}));
		assertArrayEquals(new String[] {"a", "b"}, concatArr(null, new String[] {"a", "b"}));
		assertArrayEquals(new String[] {"a"}, concatArr(new String[] {"a"}, null));
		assertArrayEquals(new String[] {}, concatArr(null, null));
		assertArrayEquals(new String[] {}, concatArr(new String[] {}, new String[] {}));
	}
	
	@Test
	void testCompact() {
		assertEquals("test.org-1", compact("test.org-", "1", -1));
		assertEquals("1", compact("test.org", "1", 1));
		assertEquals("-1", compact("test.org", "-1", 2));
		assertEquals("t-1", compact("test.org", "-1", 3));
		assertEquals("test.or-1", compact("test.org-", "-1", 9));
		assertEquals("test.org-1", compact("test.org-", "-1", 10));
		assertEquals("-", compact("", "-1", 1));
		assertEquals("-", compact(null, "-1", 1));
		assertEquals("-1", compact(null, "-1", 2));
		assertEquals("-1", compact(null, "-1", 3));
		assertEquals("test.org-1", compact("test.org", "-1", 10));
		assertEquals("test.org-1", compact("test.org", "-1", -1));
		assertEquals("", compact("test.org", "-1", 0));
		assertEquals("t", compact("test.org", "", 1));
		assertEquals("te", compact("test.org", "", 2));
	}

	@Test
	void testGtLt() {
		assertFalse(gt("1", null));
		assertFalse(lt(null, null));
		assertFalse(lt(null, ""));
		assertFalse(gt("1", "1"));
		assertFalse(gt("9", "10"));
		assertTrue(gt("11", "10"));
		assertFalse(lt("11", "10"));
		assertTrue(lt("11", "12"));
		assertTrue(lt(11, 12));
		assertTrue(lt(11.0, 12.0));
		assertTrue(gt(13.0, 12.0));
		
		assertTrue(hasGt(new Long[] {0l, 1l}, 0l));
		assertFalse(hasGt(new Long[] {0l, 0l}, 0l));
		assertFalse(hasGt(new Integer[] {0, 0}, 0));
		assertTrue(hasGt(new Integer[] {0, 0}, -1));
		
		ArrayList<Long> lst = new ArrayList<Long>();
		lst.add(0l);
		lst.add(-1l);
		assertFalse(hasGt(lst, 0l));
		assertFalse(hasGt(lst, 1l));
		assertTrue(hasGt(lst, -1l));

		lst.add(1l);
		assertTrue(hasGt(lst, 0l));
		assertFalse(hasGt(lst, 1l));

		lst.add(2l);
		assertTrue(hasGt(lst, 1l));
	}
	
	@Test
	void testFilesize2Long() {
		assertEquals((long) (6.2 * 1024), filesize("6.2 KB"));
		assertEquals((long) (6.2 * 1024), filesize("6.2 kb"));
		assertEquals((long) (6.2 * 1000), filesize("6.2 ki"));
		assertEquals((long) (6.2 * 1000), filesize("6.2 Ki"));
		assertEquals((long) (6.2 * 1024 * 1024), filesize("6.2 MB"));
		assertEquals((long) (6.2 * 1024 * 1024 * 1024), filesize("6.2 GB"));
	}
	
	@Test
	void testImagesize2int() {
		assertEquals(1024, imagesize("1024pixels"));
		assertEquals(1024, imagesize("1024 em"));
	}

	@Test
	void testJoin() {
		assertEquals("[a,b]", join(null, new Object[] { "a", "b" }));
		assertEquals("[a-b]", join("-",  new Object[] { "a", "b" }));
		assertEquals("[]", join("-",  new Object[] {}));
		assertEquals("[]", join(null, new Object[] {}));
		assertEquals(null, join(null, (Object[])null));

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

	@Test
	void testStarts() {
		assertTrue(prefixOneOf("v1234w", new String[] { "w1234", "v1234" }));
		assertFalse(prefixOneOf("v1234w", new String[] { "1v", "v1234wx" }));
		
		assertFalse(startWith("", "1"));
		assertFalse(startWith("abc", "1"));
		assertTrue(startWith("1abc", "1"));
		assertFalse(startWith(" 1abc", "1"));
		assertFalse(startWith(" 1", "1"));
		assertTrue(startWith(" ", ""));
		assertTrue(startWith(" ", " "));
	}
	
	@Test
	void testFormat() {
		assertEquals(0,  ix("%d %d %d %d %d %d", "%", 1));
		assertEquals(3,  ix("%d %d %d %d %d %d", "%", 2));
		assertEquals(6,  ix("%d %d %d %d %d %d", "%", 3));
		assertEquals(15, ix("%d %d %d %d %d %d", "%", 6));
		assertEquals(-1, ix("%d %d %d %d %d %d", "%", 7));
		assertEquals(-1, ix("%d %d %d %d %d %d", "%", 0));
		
		assertEquals("1",
				str("%d", new Integer[]
					{1}));
		assertEquals("1 2",
				str("%d %d", new Integer[]
					{1, 2}));
//		assertEquals("1 2 3",
//				str("%d %d %d", new Integer[]
//					{1, 2, 3}));
//		assertEquals("1 2 3 4",
//				str("%d %d %d %d", new Integer[]
//					{1, 2, 3, 4}));
//		assertEquals("1 2 3 4 5",
//				str("%d %d %d %d %d", new Integer[]
//					{1, 2, 3, 4, 5}));
//		assertEquals("1 2 3 4 5 6",
//				str("%d %d %d %d %d %d", new Integer[]
//					{1, 2, 3, 4, 5, 6}));
//		assertEquals("1 2 3 4 5 6 7",
//				str("%d %d %d %d %d %d %d", new Integer[]
//					{1, 2, 3, 4, 5, 6, 7}));
//		assertEquals("1 2 3 4 5 6 7 8",
//				str("%d %d %d %d %d %d %d %d", new Integer[]
//					{1, 2, 3, 4, 5, 6, 7, 8}));
//		assertEquals("1 2 3 4 5 6 7 8 9",
//				str("%d %d %d %d %d %d %d %d %d", new Integer[]
//					{1, 2, 3, 4, 5, 6, 7, 8, 9}));
//		assertEquals("1 2 3 4 5 6 7 8 9 10",
//				str("%d %d %d %d %d %d %d %d %d %d", new Integer[]
//					{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
//		assertEquals("1 2 3 4 5 6 7 8 9 10 11",
//				str("%d %d %d %d %d %d %d %d %d %d %d", new Integer[]
//					{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}));

		assertEquals("%d %d %d %d %d %d %d %d %d %d %d %d",
				str("%d %d %d %d %d %d %d %d %d %d %d %d", null));
		assertEquals("%d %d %d %d %d %d %d %d %d %d %d %d",
				str("%d %d %d %d %d %d %d %d %d %d %d %d", new Integer[0]));

		assertEquals("1 2 3 4 5 6 7 8 9 10 11 12",
				str("%d %d %d %d %d %d %d %d %d %d %d %d", new Integer[]
					{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}));
		for (int i = 3; i < 17; i++) {
			Integer[] d  = new Integer[i];
			String t = "";
			for (int k = 0; k < i; k++) {
				d[k] = k; t += "%d ";
			}

			assertEquals(
				Stream.of(d).map(x -> {
					return String.valueOf(x);
				}).collect(Collectors.joining(" ")),
				str(t.trim(), d));
		}
		
		int seq = 1, total = 7;
		assertEquals("3 / 0, 6 14.3%", str("%d / %d, %s %.1f%%", new Object[] { 3, 0, 6, (float) seq / total * 100 } ));
	}

	@Test
	void testRemovele() {
		assertEquals("v1,v3", removele("v1,v2,v3", "v2"));
		assertEquals("v1", removele("v1,v2", "v2"));
		assertEquals("v3", removele("v2,v3", "v2"));
		assertEquals("", removele("v2", "v2"));
		assertEquals("", removele("", "v2"));
		assertEquals("", removele("", ""));
		assertEquals("", removele(null, null));

		assertEquals("v1:v3", removele("v1:v2:v3", "v2", ":"));
		assertEquals("v1:v2.1,v2.2:v3", removele("v1:v2.1,v2.2:v3", "v2", ":"));
		assertEquals("v1:v2.1,v2.2", removele("v1:v2.1,v2.2:v3", "v3", ":"));
		
		String[] var = removele(new String[] {"a", "b", "c"}, 1);
		assertArrayEquals(new String[] {"a", "c"}, var);
		assertArrayEquals(new String[] {"a"}, removele(new String[] {"a", "b"}, 1));
		assertArrayEquals(new String[] {"b"}, removele(new String[] {"a", "b"}, 0));
		assertArrayEquals(new String[] {}, removele(new String[] {"a"}, 0));
		assertArrayEquals(new String[] {"a"}, removele(new String[] {"a"}, -1));
		assertArrayEquals(new String[] {}, removele(new String[] {}, 0));
		assertArrayEquals(new String[] {"a"}, removele(new String[] {"a"}, 1));
		assertArrayEquals(new String[] {"a", "b", "c"}, removele(new String[] {"a", "b", "c"}, 3));

		Integer[] foo = removele(new Integer[] {1, 2, 3}, 1, Integer.class);
		assertArrayEquals(new Integer[] {1, 3}, foo);
	}
	
	@Test
	void testIsPrimitive() {
        // primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
		assertTrue(isPrimitive(true));
        // primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        // primitiveWrapperMap.put(Character.TYPE, Character.class);
		assertTrue(isPrimitive('a'));
        // primitiveWrapperMap.put(Short.TYPE, Short.class);
		short v = 3;
		assertTrue(isPrimitive(v));
        // primitiveWrapperMap.put(Integer.TYPE, Integer.class);
		assertTrue(isPrimitive(4));
        // primitiveWrapperMap.put(Long.TYPE, Long.class);
		assertTrue(isPrimitive(5L));
        // primitiveWrapperMap.put(Double.TYPE, Double.class);
		assertTrue(isPrimitive(5d));
        // primitiveWrapperMap.put(Float.TYPE, Float.class);
		assertTrue(isPrimitive(6.0));
        // primitiveWrapperMap.put(Void.TYPE, Void.TYPE);	
		assertFalse(isPrimitive(new Object()));
		assertFalse(isPrimitive(new Anson()));
	}
	
	@Test
	void testReplacele() {
		assertTrue(ev(2, 2));
		assertTrue(ev("3", "3"));
		assertFalse(ev(5, 5.0));
		assertFalse(ev(5.1, 5.0));
		
		Object a = new Object();
		Object b = new Object();
		assertFalse(e(a, b));
		b = a;
		assertTrue(e(a, b));
		
		String[] arr = new String[] {"a", "b", "x", "d"};
		replacele(arr, "x", "y");
		
		assertTrue(e(arr[2], "y"));
	}
	
	String ignore1(String t, Object... args) {
		return f(t, args);
	}
	String ignore2(String t, Object... args) {
		return ignore1(t, args);
	}

	@Test
	void testf() {
		assertEquals(String.format("abc %s, 123", "xyz"),
					f("abc %s, 123", "xyz"));
		
		String[] tas = prefix(new String[] { "123", "abc" }, "xyz-");
		assertEquals("xyz-123", tas[0]);
		assertEquals("xyz-abc", tas[1]);
		
		assertEquals("1 2 3 4 5", f6(new String[] {"%s %s %s %s %s", "1", "2", "3", "4", "5"}));
		assertEquals("1 2 3 4", f6(new String[] {"%s %s %s %s", "1", "2", "3", "4"}));
		assertEquals("1", f6(new String[] {"%s", "1"}));
		assertEquals(null, f6(new String[] {}));
		assertEquals(null, f6(null));
		assertEquals("xyz-123", ignore1("xyz-%s", 123));
		assertEquals("xyz-123", ignore2("xyz-%s", 123));
		
		String[] args = new String[] {"123", null, "abc"};

		assertEquals("xyz-123, 123,abc", f("xyz-%s, %s", 123, str(args)));

		assertEquals("1 2 3 4 5 6 null", f6(new String[] {"%s %s %s %s %s %s %s", "1", "2", "3", "4", "5", "6", null}));
	}
	
	@Test
	void testJoinUrl() {
		assertEquals("https://127.0.0.1:8964", joinurl_(true, "127.0.0.1", 8964));
		assertEquals("https://127.0.0.1:8964/jserv_album", joinurl(true, "127.0.0.1", 8964, "jserv_album/"));
		assertEquals("https://127.0.0.1:8964/jserv_album", joinurl(true, "127.0.0.1", 8964, "/jserv_album"));
		assertEquals("https://127.0.0.1:8964/jserv_album", joinurl(true, "127.0.0.1", 8964, "/jserv_album/"));
		assertEquals("https://127.0.0.1:8964/jserv_album", joinurl(true, "127.0.0.1", 8964, "jserv_album"));

		assertEquals("https://127.0.0.1:8964/jserv/album", joinurl(true, "127.0.0.1", 8964, "jserv", "album"));
		assertEquals("https://127.0.0.1:8964/jserv/album", joinurl(true, "127.0.0.1", 8964, "jserv/", "/album"));
	}

	@Test
	void testMusts() {
		try {
			assertEquals(0, mustGe(0, 1, "0 >= 1"));
			fail("Expecting an exception.");
		} catch (NullPointerException e) {}

		assertEquals(1, mustGe(1, 1, "1 >= 1"));
		assertEquals(2, mustGe(2, 1, "2 >= 1"));

		try {
			assertEquals(-1, mustGe(-1, 0, "-1 >= 0"));
			fail("Expecting an exception.");
		} catch (NullPointerException e) {}

		assertEquals(-1, mustGe(-1, -2, "-1 >= -2"));
		
		String a = "a";
		musteq("a", a);
		try {
			musteq("ab", a);
			fail("Expecting an exception.");
		} catch (NullPointerException e) {}
	}
	
	@Test
	void test_0() {

		String[] args = new String[] {"123", null, "abc"};
		assertEquals("123", _0(args, "abc"));

		args = new String[] {};
		assertEquals("abc", _0(args, "abc"));

		args = null;
		assertEquals(null, _0(args));
		assertEquals("abc", _0(args, "abc"));

		int[] argi = new int[] {1, 23, 3};
		assertEquals(1, _0(argi, 0));
		assertEquals(1, _0(argi, -1));
		
		argi = new int[] {};
		assertEquals(0, _0(argi, 0));
		argi = null;
		assertEquals(0, _0(argi, 0));
		
		assertEquals(0.0, _0(null, 0.0));
	}
	
	@Test
	void testrof() {
		assertEquals("", strof(0, "x"));
		assertEquals("x", strof(1, "x"));
		assertEquals("**", strof(2, "*"));
		assertEquals("*.*.", strof(2, "*."));

		assertEquals("", strof("", "x"));
		assertEquals("x", strof("a", "x"));
		assertEquals("**", strof("00", "*"));
		assertEquals("", strof("  ", "*"));
		assertEquals("*.*.", strof("00", "*."));
	}
}
