package io.odysz.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LangExt {
	/**Split and trim elements.
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
	 */
	public static String toString(String[][] ss) {
		return Arrays.stream(ss)
				.filter(s -> s != null)
				.map(e -> toString(e))
				.collect(Collectors.joining(",", "[", "]"));
	}

	public static String toString(Map<String, ?> map) {
		if (map == null) return null;
		else return map.entrySet().stream()
				.map(e -> "{" + e.getKey() + ": " + e.getValue() + "}")
				.collect(Collectors.joining(",", "[", "]"));
	}

	public static String toString(List<Object[]> lst) {
		if (lst == null) return null;
		else return lst.stream()
				.map(e -> toString(e))
				.collect(Collectors.joining(",", "[", "]"));
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


	public static boolean is(boolean[] isAdmin, boolean... deflt) {
		if (isAdmin == null || isAdmin.length < 1)
			return (deflt == null || deflt.length < 1) ? false : is(deflt);
		else
			return isAdmin[0];
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
     * @return args == null || args.length == 0 || args.length == 1 && args[0] == null;
     */
    public static boolean isNull(final Object[] args) {
    	return args == null || args.length == 0
    		|| args.length == 1 && args[0] == null
    		|| args.length == 2 && args[0] == null && args[1] == null;
    }

    public static boolean isNull(final List<?> args) {
    	return args == null || args.isEmpty();
    }

    public static boolean isNull(final Object args) {
    	return args == null;
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
     * @param str  the String to repeat, may be null
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
	 * @return
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
	
	public static int len(Set<?> s) {
		return isNull(s) ? 0 : s.size();
	}

}

