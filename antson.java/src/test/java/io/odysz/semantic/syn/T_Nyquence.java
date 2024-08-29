package io.odysz.semantic.syn;

import java.sql.SQLException;
import java.util.HashMap;

import io.odysz.anson.T_AnResultset;

public class T_Nyquence {

	/**
	 * Compare a, b within modal of max long.
	 * <pre>
	 * min-long<0        0            max-long
	 *                a  b
	 *     a             b
	 *     b                    a             a - b &lt; 0
	 * </pre>
	 * @param a
	 * @param b
	 * @return 1 if a &gt; b else -1 if a &lt; b else 0
	 */
	public static int compareNyq(long a, long b) {
		long c = a - b;
		return c < 0 && c != Long.MIN_VALUE ? -1 : a == b ? 0 : 1;
	}

	public long n;

	public T_Nyquence(long n) {
		this.n = n;
	}

	public T_Nyquence(String n) {
		this(Long.valueOf(n));
	}

	/**
	 * Increase n, if less than {@code maxn}, set to {@code maxn}.
	 * 
	 * @param maxn
	 * @return this
	 */
	T_Nyquence inc(long maxn) {
		this.n = Math.max(maxn, this.n );
		this.n++;
		return this;
	}

	@Override
	public String toString() {
		return String.valueOf(n);
	}
	
	public static long maxn(long a, long b) {
		return compareNyq(a, b) < 0 ? b : a;
	}

	public static long minn(long a, long b) {
		return compareNyq(a, b) < 0 ? a : b;
	}

	public static int compareNyq(T_Nyquence a, T_Nyquence b) {
		return compareNyq(a.n, b.n);
	}

	public static HashMap<String, T_Nyquence> clone(HashMap<String, T_Nyquence> from) {
		HashMap<String, T_Nyquence> nv = new HashMap<String, T_Nyquence>(from.size());
		for (String k : from.keySet())
			nv.put(k, new T_Nyquence(from.get(k).n));
		return nv;
	}

	public static HashMap<String, T_Nyquence>[] clone(HashMap<String, T_Nyquence>[] nvs) {
		@SuppressWarnings("unchecked")
		HashMap<String, T_Nyquence>[] nv = (HashMap<String, T_Nyquence>[]) new HashMap<?, ?>[nvs.length];
		for (int ix = 0; ix < nvs.length; ix++)
			nv[ix] = clone(nvs[ix]);
		return nv;
	}

	/**
	 * Parse T_Nyquence from result set.
	 * @param chal
	 * @param nyqcol
	 * @return T_Nyquence
	 * @throws SQLException
	 */
	public static T_Nyquence getn(T_AnResultset chal, String nyqcol) throws SQLException {
		return new T_Nyquence(chal.getString(nyqcol));
	}

	/**
	 * Get absolute distance.
	 * 
	 * @param a
	 * @param b
	 * @return | a - b |
	 */
	public static long abs(T_Nyquence a, T_Nyquence b) {
		return Math.abs(Math.min(a.n - b.n, b.n - a.n));
	}

}
