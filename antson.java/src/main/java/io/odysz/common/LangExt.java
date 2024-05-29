package io.odysz.common;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LangExt {
	/**
	 * Split and trim elements.
	 * <p>Empty element won't be ignored if there are 2 consequent separator. <br>
	 * That means two junctural, succeeding, cascading separators without an element in between, expect white space.
	 * Sorry for that poor English.</p>
	 * @see https://stackoverflow.com/questions/41953388/java-split-and-trim-in-one-shot
	 * @param s
	 * @param regex
	 * @param noTrim
	 * @return String[]
	 */
	public static String[] split(String s, String regex, boolean... noTrim) {
		if (s == null)
			return null;
		else {
			if (noTrim == null || noTrim.length == 0 || !noTrim[0])
				regex = "\\s*" + regex + "\\s*";
			return s.split(regex);
		}
	}

	public static String[] split(String s) {
		return split(s, ",");
	}
	
	public static String[][] split(String[] ss) {
		if (ss == null)
			return null;
		String[][] argss = new String[ss.length][];
		for (int ix = 0; ix < ss.length; ix++) {
			String[] args = LangExt.split(ss[ix], "\\s+");
			argss[ix] = args;
		}
		return argss;
	}
	
	/**
	 * Reverse of {@link #compoundVal(String...).
	 * 
	 * @since 0.9.41
	 * @param v
	 * @return deserialized compound values
	 */
	public static String[] uncombine(String v) {
		return (String[]) Stream
				.of(v.split("\n"))
				.filter(s -> !isNull(s))
				.toArray();
	}

	/**
	 * <pre>
	assertTrue(prefixWith("v1234w", new String[] { "w1234", "v1234" }));
	assertFalse(prefixWith("v1234w", new String[] { "1v", "v1234wx" }));</pre>
	 * @param s
	 * @param prefixes
	 * @return true if exist a prefix, other wise false
	 * @since 0.9.63
	 */
	public static boolean prefixWith(String s, String... prefixes) {
		for (String prefix : prefixes)
			if (s.startsWith(prefix))
				return true;
		return false;
	}

	/**
	 * See {@link #prefixWith(String, String...)}
	 * 
	 * @param s
	 * @param prefixes
	 * @return
	 * @since 0.9.39
	 */
	public static boolean startsOneOf(String s, String... prefixes) {
		return prefixWith(s, prefixes);
	}
	
	/**Get a string array that composed into string by {@link #toString(Object[])}.
	 * @param ss
	 * @return [e0, e1, ...]
	 */
	public static String toString(Object[] ss) {
		return ss == null ? null : Arrays.stream(ss)
				.filter(e -> e != null)
				.map(e -> e.toString()).collect(Collectors.joining(",", "[", "]"));
	}

	public static String toString(int[] ss) {
		return ss == null ? null : Arrays.stream(ss)
				.mapToObj(e -> String.valueOf(e)).collect(Collectors.joining(",", "[", "]"));
	}

	/** Get a string that can be parsed by {@link #toArray(String)}.<br>
	 * E.g. "[a, b]" =&gt; ["a", "b"]
	 * @param str
	 * @return string[]
	 */
	public static String[] toArray(String str) {
		return str.replaceAll("^\\[", "").replaceAll("\\]$", "").split(",");
	}	

	/**Convert 2D array to string: "[{ss[0][1]: ss[0][1]}, {ss[1][0]: ss[1][1]}, ...]"
	 * @param ss
	 * @return converted String
	 * @since 0.9.38
	*/
	public static String str(String[][] ss) {
		return Arrays.stream(ss)
				.filter(s -> s != null)
				.map(e -> toString(e))
				.collect(Collectors.joining(",", "[", "]"));
	}

	/**
	 * @param map
	 * @return map in string
	 * @since 0.9.39
	 */
	public static String str(Map<String, ?> map) {
		if (map == null) return null;
		else return map.entrySet().stream()
				.map(e -> "{" + e.getKey() + ": " + e.getValue() + "}")
				.collect(Collectors.joining(",", "[", "]"));
	}

	/**
	 * @since 0.9.72
	 * 
	 * @param lst
	 * @return string
	 */
	public static String str(List<?> lst) {
		if (lst == null) return null;
		else return lst.stream()
				.filter(e -> e != null)
				.map(e -> e.getClass().isArray() ? toString((Object[])e) : e.toString())
				.collect(Collectors.joining(",", "[", "]"));
	}
	
	/**
	 * Format text into string center.
	 * @param text e.g. "str"
	 * @param len e.g. 7
	 * @return space padded string, e.g. "  str  ".
	 */
	public static String strcenter(String text, int len){
	    String out = String.format("%" + len + "s%s%" + len + "s", "", text, "");
	    float mid = (out.length()/2);
	    float start = mid - (len/2);
	    float end = start + len; 
	    return out.substring((int)start, (int)end);
	}
	
	public static boolean bool(String v) {
		return v == null ? false
				:  v.equalsIgnoreCase("1")
				|| v.equalsIgnoreCase("true")
				|| v.equalsIgnoreCase("t")
				|| v.equalsIgnoreCase("t")
				|| v.equalsIgnoreCase("y")
				|| v.equalsIgnoreCase("yes");
	}

	public static boolean bool(int v) {
		return v != 0; 
	}

	public static boolean bool(float v) {
		return v != 0.0; 
	}

	/**Parse formatted string into hash map.
	 * @param str "k1:v1,k2:v2,..."
	 * @return hash map
	 */
	public static HashMap<String, String> parseMap(String str) {
		if (str != null && str.trim().length() > 0) {
			String[] entryss = str.trim().split(",");
			HashMap<String, String> refMap = new HashMap<String, String>(entryss.length);
			for (String entry : entryss) {
				try {
					String[] e = entry.split(":");
					refMap.put(e[0].trim(), e[1].trim());
				}
				catch (Exception ex) {
					Utils.warn("WARN: - can't parse: " + entry);
					continue;
				}
			}
			return refMap;
		}
		return null;
	}


	/**
	 * <pre>
	assertFalse(is(null));
	assertTrue (is(null, true));

	assertFalse(is(new boolean[] {false}));
	assertFalse(is(new boolean[] {false}, false));
	assertTrue (is(new boolean[] {true}));
	assertTrue (is(new boolean[] {}, true));
	assertTrue (is(new boolean[] {true}, false));
	 * </pre>
	 * @param val
	 * @param deflt
	 * @return
	 */
	public static boolean is(boolean[] val, boolean... deflt) {
		if (val == null || val.length < 1)
			return (deflt == null || deflt.length < 1) ? false : is(deflt);
		else
			return val[0];
	}

	/**
	 * Is s empty of only space - not logic meanings?
	 * If space can not be ignored, use {@link #isEmpty(CharSequence)}.
	 * 
	 * @param s
	 * @param takeAsNull regex take as null, e.g. "\\s*null\\s*" will take the string "null " as null.
	 * @return true: empty
	 */
	public static boolean isblank(String s, String... takeAsNull) {
		if (s == null || s.trim().length() == 0)
			return true;
		else if (takeAsNull == null || takeAsNull.length == 0)
			// return s.trim().length() == 0;
			return false;
		else {
			for (String asNull : takeAsNull)
				if (s.matches(asNull))
					return true;
			return false;
		}
	}

	public static boolean isblank(Object bid, String... takeAsNull) {
		return bid instanceof String ? isblank(bid.toString(), takeAsNull)
				: bid == null;
	}

    // Empty checks
    //-----------------------------------------------------------------------
    /**
     * <p>Checks if a CharSequence is empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>NOTE: This method is changed in LangExt version 2.0.
     * It's no longer trims the CharSequence.
     * That functionality is available in {@link #isblank(String, String...)}.</p>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is empty or null
     * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
    
    /**
     * @param args
     * @return args == null || args.length == 0 || args.length == 1 &amp;&amp; args[0] == null;
     */
    public static boolean isNull(final Object[] args) {
    	return args == null || args.length == 0
    		|| args.length == 1 && args[0] == null
    		|| args.length == 2 && args[0] == null && args[1] == null;
    }

    /**
     * @param args
     * @return true if the args is null or is zero length
     */
    public static boolean isNull(final List<?> args) {
    	return args == null || args.isEmpty();
    }

    public static boolean isNull(final Object arg) {
    	if (arg == null) return true;
    	
    	if (arg.getClass().isArray()) {
			return Array.getLength(arg) == 0 || Array.get(arg, 0) == null;
    	}
    	else if (arg instanceof String)
    		return isblank((String) arg);
    	else if (arg instanceof Map)
    		return ((Map<?, ?>) arg).size() == 0;
    	else if (arg instanceof Set)
    		return ((Set<?>) arg).size() == 0;
    	else return false;
    }

	public static <T> T ifnull(T op, T deflt) {
		return isblank(op) ? deflt : op;
	}
    
    /**
     * @param v
     * @return v != null &amp;&amp; v[0] == v[1] &amp;&amp; v[2] == v[3] &amp;&amp; ...
     */
    public static boolean eqs(String ... v) {
    	if (isNull(v))
    		return true;
    	else {
    		if ((v.length %2) != 0) return false;
    		for (int i = 0; i < v.length; i+=2) {
				if (!eq(v[i], v[i+1]))
					return false;
    		}
    		return true;
    	}
    }

	public static boolean eq(String v, String u, boolean ... ignoreCase) {
		return v == null && u == null || (u != null && v != null &&
				(is(ignoreCase) ? v.equalsIgnoreCase(u) : v.equals(u)));
	}

	public static boolean eq(String[] v, String u, boolean ... ignoreCase ) {
		return isNull(v) && u == null || (u != null && v != null && len(v) > 0 &&
				(is(ignoreCase) ? v[0].equalsIgnoreCase(u) : v[0].equals(u)));
	}

	public static <T> boolean gt(T a, T b) {
		return (isblank(a) || isblank(b)) ? false
				: a instanceof String ? Double.valueOf((String)a) > Double.valueOf((String)b)
				: a instanceof Integer ? (Integer)a > (Integer)b
				: a instanceof Float ? (Float)a > (Float)b
				: a instanceof Double ? (Double)a > (Double)b
				: a instanceof Long ? (Long)a > (Long)b
				: a instanceof Short ? (Short)a > (Short)b
				: ((String)a).compareTo((String) b) > 0;
	}

	public static <T> boolean lt(T a, T b) {
		return (isblank(a) || isblank(b)) ? false
				: a instanceof String ? Double.valueOf((String)a) < Double.valueOf((String)b)
				: a instanceof Integer ? (Integer)a < (Integer)b
				: a instanceof Float ? (Float)a < (Float)b
				: a instanceof Double ? (Double)a < (Double)b
				: a instanceof Long ? (Long)a < (Long)b
				: a instanceof Short ? (Short)a < (Short)b
				: ((String)a).compareTo((String) b) < 0;
	}
	
	/**
	 * Is there any element in {@code arr} is greater than {@code b}?
	 * <pre>
	 * assertTrue(hasGt(new Long[] {0l, 1l}, 0l));
	 * assertFalse(hasGt(new Long[] {0l, 0l}, 0l));
	 * assertFalse(hasGt(new Integer[] {0, 0}, 0));
	 * assertTrue(hasGt(new Integer[] {0, 0}, -1));</pre>
	 * @param arr
	 * @param b
	 * @return there is at least one
	 */
	public static <T> boolean hasGt(List<T> arr, T b) {
		if (arr == null || isblank(b)) return false;
		for (T a : arr)
			if (!isblank(a) && (
					a instanceof String && Double.valueOf((String)a) > Double.valueOf((String)b) ||
					a instanceof Number && Double.valueOf(String.valueOf(a)) > Double.valueOf(String.valueOf(b))))
				return true;  
		return false;

	}

	/**
	 * @see #hasGt(List, Object)
	 * @param arr
	 * @param b
	 * @return true if has one 
	 */
	public static <T> boolean hasGt(T[] arr, T b) {
		if (arr == null || isblank(b)) return false;
		for (T a : arr)
			if (!isblank(a) && (
					a instanceof String && Double.valueOf((String)a) > Double.valueOf((String)b) ||
					a instanceof Number && Double.valueOf(String.valueOf(a)) > Double.valueOf(String.valueOf(b))))
				return true;  
		return false;
	}
	
    public static String units = "BKMGTPEZY";

    public static long filesize(String size) {
    	if (isblank(size)) return 0;
    	size = size.toUpperCase();

        int spaceNdx = size.indexOf(" ");    
        double ret = Double.parseDouble(size.substring(0, spaceNdx));
        String unitString = size.substring(spaceNdx+1);
        int unitChar = unitString.charAt(0);
        int power = units.indexOf(unitChar);
        boolean isSi = unitString.indexOf('I')!=-1;
        int factor = 1024;
        if (isSi)
            factor = 1000;

        // return new Double(ret * Math.pow(factor, power)).longValue();
        return (long) (ret * Math.pow(factor, power));
    }
    
    public static Regex regexMysqlCol = new Regex("(\\d+)");
    public static int imagesize(String measure) {
		ArrayList<String> typeLen = regexMysqlCol.findGroups(measure);
		return isNull(typeLen) ? 0 : Integer.valueOf(typeLen.get(0));
    }

	public static String prefixIfnull(String prefix, String dest) {
		if (isblank(prefix) || dest.startsWith(prefix))
			return dest;

		return prefix + dest;
	}

	///////////////////        copyright Apache.org       ////////////////////////////////
	// https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/StringUtils.java
	/*
	 * Licensed to the Apache Software Foundation (ASF) under one or more
	 * contributor license agreements.  See the NOTICE file distributed with
	 * this work for additional information regarding copyright ownership.
	 * The ASF licenses this file to You under the Apache License, Version 2.0
	 * (the "License"); you may not use this file except in compliance with
	 * the License.  You may obtain a copy of the License at
	 *
	 *      http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 * 
	 * 
	 * package org.apache.commons.lang3;
	 */
    // Performance testing notes (JDK 1.4, Jul03, scolebourne)
    // Whitespace:
    // Character.isWhitespace() is faster than WHITESPACE.indexOf()
    // where WHITESPACE is a string of all whitespace characters
    //
    // Character access:
    // String.charAt(n) versus toCharArray(), then array[n]
    // String.charAt(n) is about 15% worse for a 10K string
    // They are about equal for a length 50 string
    // String.charAt(n) is about 4 times better for a length 3 string
    // String.charAt(n) is best bet overall
    //
    // Append:
    // String.concat about twice as fast as StringBuffer.append
    // (not sure who tested this)

    /**
     * A String for a space character.
     *
     * @since 3.2
     */
    public static final String SPACE = " ";

    /**
     * The empty String {@code ""}.
     * @since 2.0
     */
    public static final String EMPTY = "";

    /**
     * A String for linefeed LF ("\n").
     *
     * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
     *      for Character and String Literals</a>
     * @since 3.2
     */
    public static final String LF = "\n";

    /**
     * A String for carriage return CR ("\r").
     *
     * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
     *      for Character and String Literals</a>
     * @since 3.2
     */
    public static final String CR = "\r";

    /**
     * Represents a failed index search.
     * @since 2.1
     */
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * <p>The maximum size to which the padding constant(s) can expand.</p>
     */
    private static final int PAD_LIMIT = 8192;

	/**
	 * <p>Left pad a String with spaces (' ').</p>
	 *
	 * <p>The String is padded to the size of {@code size}.</p>
	 *
	 * <pre>
	 * StringUtils.leftPad(null, *)   = null
	 * StringUtils.leftPad("", 3)     = "   "
	 * StringUtils.leftPad("bat", 3)  = "bat"
	 * StringUtils.leftPad("bat", 5)  = "  bat"
	 * StringUtils.leftPad("bat", 1)  = "bat"
	 * StringUtils.leftPad("bat", -1) = "bat"
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size  the size to pad to
	 * @return left padded String or original String if no padding is necessary,
	 *  {@code null} if null String input
	 */
	public static String leftPad(final String str, final int size) {
	    return leftPad(str, size, ' ');
	}
	
	/**
	 * <p>Left pad a String with a specified character.</p>
	 *
	 * <p>Pad to a size of {@code size}.</p>
	 *
	 * <pre>
	 * StringUtils.leftPad(null, *, *)     = null
	 * StringUtils.leftPad("", 3, 'z')     = "zzz"
	 * StringUtils.leftPad("bat", 3, 'z')  = "bat"
	 * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
	 * StringUtils.leftPad("bat", 1, 'z')  = "bat"
	 * StringUtils.leftPad("bat", -1, 'z') = "bat"
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size  the size to pad to
	 * @param padChar  the character to pad with
	 * @return left padded String or original String if no padding is necessary,
	 *  {@code null} if null String input
	 * @since 2.0
	 */
	public static String leftPad(final String str, final int size, final char padChar) {
	    if (str == null) {
	        return null;
	    }
	    final int pads = size - str.length();
	    if (pads <= 0) {
	        return str; // returns original String when possible
	    }
	    if (pads > PAD_LIMIT) {
	        return leftPad(str, size, String.valueOf(padChar));
	    }
	    return repeat(padChar, pads).concat(str);
	}
	
	/**
	 * <p>Left pad a String with a specified String.</p>
	 *
	 * <p>Pad to a size of {@code size}.</p>
	 *
	 * <pre>
	 * StringUtils.leftPad(null, *, *)      = null
	 * StringUtils.leftPad("", 3, "z")      = "zzz"
	 * StringUtils.leftPad("bat", 3, "yz")  = "bat"
	 * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
	 * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
	 * StringUtils.leftPad("bat", 1, "yz")  = "bat"
	 * StringUtils.leftPad("bat", -1, "yz") = "bat"
	 * StringUtils.leftPad("bat", 5, null)  = "  bat"
	 * StringUtils.leftPad("bat", 5, "")    = "  bat"
	 * </pre>
	 *
	 * @param str  the String to pad out, may be null
	 * @param size  the size to pad to
	 * @param padStr  the String to pad with, null or empty treated as single space
	 * @return left padded String or original String if no padding is necessary,
	 *  {@code null} if null String input
	 */
	public static String leftPad(final String str, final int size, String padStr) {
	    if (str == null) {
	        return null;
	    }
	    if (isEmpty(padStr)) {
	        padStr = SPACE;
	    }
	    final int padLen = padStr.length();
	    final int strLen = str.length();
	    final int pads = size - strLen;
	    if (pads <= 0) {
	        return str; // returns original String when possible
	    }
	    if (padLen == 1 && pads <= PAD_LIMIT) {
	        return leftPad(str, size, padStr.charAt(0));
	    }
	
	    if (pads == padLen) {
	        return padStr.concat(str);
	    } else if (pads < padLen) {
	        return padStr.substring(0, pads).concat(str);
	    } else {
	        final char[] padding = new char[pads];
	        final char[] padChars = padStr.toCharArray();
	        for (int i = 0; i < pads; i++) {
	            padding[i] = padChars[i % padLen];
	        }
	        return new String(padding).concat(str);
	    }
	}
	
    /**
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     *
     * <pre>
     * StringUtils.repeat('e', 0)  = ""
     * StringUtils.repeat('e', 3)  = "eee"
     * StringUtils.repeat('e', -2) = ""
     * </pre>
     *
     * <p>Note: this method does not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of {@code char}s to be represented.
     * If you are needing to support full I18N of your applications
     * consider using {@link #repeat(String, int)} instead.
     * </p>
     *
     * @param ch  character to repeat
     * @param repeat  number of times to repeat char, negative treated as zero
     * @return String with repeated character
     * @see #repeat(String, int)
     */
    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return EMPTY;
        }
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    // Padding
    //-----------------------------------------------------------------------
    /**
     * <p>Repeat a String {@code repeat} times to form a
     * new String.</p>
     *
     * <pre>
     * StringUtils.repeat(null, 2) = null
     * StringUtils.repeat("", 0)   = ""
     * StringUtils.repeat("", 2)   = ""
     * StringUtils.repeat("a", 3)  = "aaa"
     * StringUtils.repeat("ab", 2) = "abab"
     * StringUtils.repeat("a", -2) = ""
     * </pre>
     *
     * @param str  the String to be duplicated, may be null
     * @param repeat  number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     *  {@code null} if null String input
     */
    public static String repeat(final String str, final int repeat) {
        // Performance tuned for 2.0 (JDK1.4)

        if (str == null) {
            return null;
        }
        if (repeat <= 0) {
            return EMPTY;
        }
        final int inputLength = str.length();
        if (repeat == 1 || inputLength == 0) {
            return str;
        }
        if (inputLength == 1 && repeat <= PAD_LIMIT) {
            return repeat(str.charAt(0), repeat);
        }

        final int outputLength = inputLength * repeat;
        switch (inputLength) {
            case 1 :
                return repeat(str.charAt(0), repeat);
            case 2 :
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char[] output2 = new char[outputLength];
                for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
                    output2[i] = ch0;
                    output2[i + 1] = ch1;
                }
                return new String(output2);
            default :
                final StringBuilder buf = new StringBuilder(outputLength);
                for (int i = 0; i < repeat; i++) {
                    buf.append(str);
                }
                return buf.toString();
        }
    }

	/**
	 * see {@link LangExtTest#testEndwith()}
	 * 
	 * @param uri
	 * @param postfix
	 * @return true if matched
	 */
	public static boolean endWith(String uri, String ...postfix) {
		if (!isEmpty(uri)) {
			if (isNull(postfix))
				return true;
			else {
				for (String s : postfix)
					if (uri.endsWith(s))
						return true;
				return false;
			}
		}
		else return false;
	}
	
	/**
	 * @since 0.9.41
	 * @param s
	 * @return len
	 */
	public static int len(Set<?> s) {
		return isNull(s) ? 0 : s.size();
	}

	public static int len(Map<?, ?> s) {
		return isNull(s) ? 0 : s.size();
	}

	public static int len(Object[] s) {
		return isNull(s) ? 0 : s.length;
	}

	public static int len(List<?> s) {
		return isNull(s) ? 0 : s.size();
	}

	public static int len(String s) {
		return isNull(s) ? 0 : s.length();
	}

	public static int len(char[] ch) {
		return isNull(ch) ? 0 : ch.length;
	}
	
	/**
	 * @since 0.9.33
	 * 
	 * @param arr
	 * @param e
	 * @return position
	 */
	public static int indexOf(char[] arr, char e) {
		int i = 0;
		while (i < len(arr)) {
			if (arr[i] == e)
				return i;
			i++;
		}
		return -1;
	}
	
	
	/**
	 * @param str
	 * @param s comma separated element list, without space following comma, e.g. "a-1,b-2,..."
	 * @return the index of the first occurrence of the specified substring,or -1 if there is no such occurrence.
	 */
	public static int indexOf(String str, String s) {
		return ("," + str + ",").indexOf("," + s + ",");
	}
	
    /**
     * Test is {@code target} in {@code arr}? Uses for-loop to find the index.
     * @param arr array
     * @param target
     * @return index or -1 if dosen't find
     * @param <T>
	 * @since 0.9.51
     */
    public static <T> int indexOf(T[] arr, T target) {
        for (int index = 0; index < arr.length; index++) {
            if (arr[index] == target
                    || target instanceof String && eq((String)arr[index], (String) target)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * swap array elemetns 
     * @param <T>
     * @param arr
     * @param a
     * @param b
	 * @since 0.9.51
     */
    public static <T> void swap(T[] arr, int a, int b) {
        if (arr != null && 0 <= a && a < arr.length && 0 <= b && b <= arr.length) {
            T x = arr[b];
            arr[b] = arr[a];
            arr[a] = x;
        }
    }

    /**
     * 
     * @param <T>
     * @param arr
     * @param element
     * @param position
     * @return new arr copy
	 * @since 0.9.51
     */
    public static <T> T[] insertAt(T[] arr, T element, int position) {
        List<T> list = new ArrayList<>(Arrays.asList(arr));
        list.add(position, element);
        return list.toArray(arr);
    }
    
	/**
	 * Get array item, null if not exists.
	 * @param <T>
	 * @param arr
	 * @param x
	 * @return the element
	 * @since 0.9.50
	 */
	public static <T> T ix(T[] arr, int x) {
		return (len(arr) > x) ? arr[x] : null;
	}

	/**
	 * Get array item, null if not exists.
	 * @param <T>
	 * @param arr
	 * @param x
	 * @return item
	 * @since 0.9.50
	 */
	public static <T> T ix(ArrayList<T> arr, int x) {
		return (len(arr) > x) ? arr.get(x) : null;
	}

	/**
	 * @since 0.9.33
	 * @param c
	 * @return string
	 */
	public static String str(AbstractCollection<String> c) {
		return c.stream().collect(Collectors.joining(","));
	}
	
	/**
	 * @since 0.9.33
	 * @param v
	 * @return string
	 */
	public static String str(int v) {
		return String.valueOf(v);
	}
	
	/**
	 * @since 0.9.33
	 * @param v
	 * @return string
	 */
	public static String str(int[] v) {
		return Arrays.stream(v).mapToObj(String::valueOf).collect(Collectors.joining(","));
	}

	/**
	 * @since 0.9.33
	 * @param v
	 * @return string
	 */
	public static String str(Object[] v) {
		return Arrays.stream(v).map(o -> o.toString()).collect(Collectors.joining(","));
	}

	/**
	 * Equivalent of String.format().
	 * 
	 * <p>Tests:</p>
	 * <pre>
	 * assertEquals("1", str("%d", new Integer[] {1}));
	 * ...
	 * assertEquals("1 2 3 4 5", str("%d %d %d %d %d", new Integer[] {1, 2, 3, 4, 5}));
	 * </pre>
	 * @since 0.9.46
	 * @param template
	 * @param args
	 * @return string
	 */
	public static String str(String template, Object[] args) {
		int len = len(args);
		if (len == 0) return template;

		int ix4  = ix(template, "%", 5);
		return (args == null) ? template
			 : (len == 1) ? String.format(template, args[0])
			 : (len == 2) ? String.format(template, args[0], args[1])
			 : (len == 3) ? String.format(template, args[0], args[1], args[2])
			 : (len == 4) ? String.format(template, args[0], args[1], args[2], args[3])
			 : String.format(template.substring(0, ix4), args[0], args[1], args[2], args[3])
				+ str(template.substring(ix4), Arrays.copyOfRange(args, 4, len))
			 ;
	}
	
	/**
	 * Convert to string as "v[0] zip[0], v[1] zip[1], ...".
	 * @param values
	 * @param zip
	 * @return formatted string
	 * 
	 * @since 0.9.73
	 */
	public static String str(Object[] values, String[] zip) {
		int[] i = new int[] {0};
		return Stream.of(values)
				.map(v -> {
					return String.format("%s %s", v, i[0] < zip.length ? zip[i[0]++] : "");
				}).collect(Collectors.joining(","));
	}
	
	/**
	 * 
	 * Convert to string as "v[0] zip[0], v[1] zip[1], ...".
	 * @param values
	 * @param zip
	 * @return formatted string
	 * 
	 * @since 0.9.73
	 */
	public static <T> String str(ArrayList<T> values, String[] zip) {
		int[] i = new int[] {0};
		return values.stream()
				.map(v -> {
					return String.format("%s %s", v, i[0] < zip.length ? zip[i[0]++] : "");
				}).collect(Collectors.joining(","));
	}
	
	/**
	 * Find the i-th repeat of match occurrence in f.
	 * 
	 * <p>Test:</p><pre>
	 * assertEquals(0,  ix("%d %d %d %d %d %d", "%", 1));
	 * assertEquals(3,  ix("%d %d %d %d %d %d", "%", 2));
	 * assertEquals(6,  ix("%d %d %d %d %d %d", "%", 3));
	 * assertEquals(15, ix("%d %d %d %d %d %d", "%", 6));
	 * assertEquals(-1, ix("%d %d %d %d %d %d", "%", 7));
	 * assertEquals(-1, ix("%d %d %d %d %d %d", "%", 0));
	</pre>
	 * @param f
	 * @param match
	 * @param repeat
	 * @return i-th index
	 * @since 0.9.46
	 */
	static int ix (String f, String match, int repeat) {
		if (repeat <= 0)
			return -1;
		
		int currentIndex = f.indexOf(match);
		while (currentIndex >= 0 && repeat > 1) {
			int x = f.indexOf(match, currentIndex + 1);
			if (x > currentIndex) {
				currentIndex = x;
				repeat--;
			}
			else return -1;
		}
		return repeat > 1 ? -1 : currentIndex;
	}
	
	/**
	 * @since 0.9.41
	 * @param s
	 * @return string
	 */
	public static String trim(String s) {
		if (s == null) return null;
		else return s.trim();
	}

	/**
	 * Join strings.
	 * 
	 * @since 0.9.33
	 * @param sep, null as ","
	 * @param vi "a", null, "b", ...
	 * @return "a,b,..."
	 */
	public static String join(String sep, String ... vi) {
		if (sep == null) sep = ",";
		return vi == null ? null
			: Stream.of(vi).filter(v -> v != null).collect(Collectors.joining(sep));
	}

	/**
	 * Join strings, escape sep with esc. If sep = "\n", esc = "\\n".
	 * 
	 * <p>It's the user's responsiblity to unescape the result value or distinguish what's messed up.</p>
	 * @see #joinEsc(String, String, String...)
	 * 
	 * @since 0.9.33
	 * @param sep, null as ",", e.g. "\n"
	 * @param esc, replacement, e.g. "\\n"
	 * @param vi "a\nx", null, "bb", ...
	 * @return "a\\nx\nb\n..."
	 */
	public static String join(final String sep, String esc, String ... vi) {
		final String sap = (sep == null) ? "," : sep;
		return vi == null ? null
				: Stream.of(vi)
				.filter(v -> v != null)
				.map(v -> v.replaceAll(sap, esc))
				.collect(Collectors.joining(sep));
	}

	/**
	 * Join strings, escape sep with esc. If sep = "\n", esc = "\\n",
	 * the returned string is the same as Funcall.compound(), and can
	 * be restored using StringEscapeUtils.unescapeJava(returned).
	 * 
	 * @since 0.9.33
	 * @param sep
	 * @param esc
	 * @param vi
	 * @return string
	 */
	public static String joinEsc(String sep, String esc, String... vi) {
		final String sap = (sep == null) ? "," : sep;
		return vi == null ? null
				: Stream.of(vi)
				.filter(v -> v != null)
				.map(v -> v
						.replaceAll("\\\\", "\\\\\\\\")
						.replaceAll(sap, esc))
				.collect(Collectors.joining(sep));
	}

	/**
	 * Concatenate strings into a "\n" separated string. 
	 * @since 0.9.33
	 * @param vals
	 * @return string
	 */
	public static String compoundVal(String... vals) {
		return joinEsc("\n", "\\n", vals);
	}
	
	/**
	 * <p>Remove element pattern from string of multiple elements, separated with 'sep'.</p>
	 * <pre>
	 * Test
	 * assertEquals("v1,v3", removele("v1,v2,v3", "v2"));
	 * assertEquals("v1", removele("v1,v2", "v2"));
	 * assertEquals("v3", removele("v2,v3", "v2"));
	 * assertEquals("", removele("v2", "v2"));
	 * assertEquals("", removele("", "v2"));
	 * assertEquals("", removele("", ""));
	 * assertEquals("", removele(null, null));
	 *
	 * assertEquals("v1:v2.1,v2.2:v3", removele("v1:v2.1,v2.2:v3", "v2", ":"));
	 * assertEquals("v1:v2.1,v2.2", removele("v1:v2.1,v2.2:v3", "v3", ":"));
	 * <pre>
	 * @param from e.g. "v1,v2,v3"
	 * @param p e.g. "v2"
	 * @param sep default ","
	 * @return new string, e.g. "v1,v3"
	 * @since 0.9.64
	 */
	public static String removele(String from, String p, String... sep) {
		String s = isNull(sep) ? "," : sep[0];

		return isblank(from) ? ""
			: (s + from.trim() + s).replaceAll(s + p + s, s).replaceAll("^"+s, "").replaceAll(s+"$", "");
	}
}

