package io.odysz.common;

import static io.odysz.common.LangExt.eq;
import static io.odysz.common.LangExt.eqi;
import static io.odysz.common.LangExt.f;
import static io.odysz.common.LangExt.ifnull;
import static io.odysz.common.LangExt.insertAt;
import static io.odysz.common.LangExt.isblank;
import static io.odysz.common.LangExt.isNull;
import static io.odysz.common.LangExt.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**Regular Express helper. To match first letter 'P', set<br>
 * messages.xml/members.passport.regex = "^[pP]"<br>
 * and call:<br>
 * Regex.match(somestring);
 * See https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
 * @author ody
 *
 */
public class Regex {
	// also can be used in Instanced style
	private final Pattern regInst;
	public Regex(String pattern) {
		regInst = Pattern.compile(pattern);
	}
	
	public boolean match(String v) {
		if (v == null) return false;
		Matcher matcher = regInst.matcher(v);
		return matcher.find();
	}

	public ArrayList<String> findGroups(String v) {
		Matcher matcher = regInst.matcher(v);
        if (matcher.find()) {
        	ArrayList<String> vss = new ArrayList<String>(matcher.groupCount()) ;
        	// group(0) is the hole string itself
        	for (int i = 1; i <= matcher.groupCount(); i++)
        		vss.add(matcher.group(i));
        	return vss;
        }
        else return null;
	}
	
	public ArrayList<String[]> findGroupsRecur(String v) {
        Matcher matcher = regInst.matcher(v);
        ArrayList<String[]> res = new ArrayList<String[]>();
        int i = 0;
        while (matcher.find()) {
        	String[] grps = new String[matcher.groupCount()];
            for (int g = 0; g < matcher.groupCount(); g++) {
            	grps[g] = matcher.group(g);
            }
            res.add(grps);

            if (i++ > 20) break; // in case of wrong patter definition
            v = v.substring(matcher.end());
            matcher = regInst.matcher(v);
        }
	
        return res;
	}
	
	public int startAt(String v) {
        Matcher matcher = regInst.matcher(v);
        if (matcher.find())
        	return matcher.start();
        else return -1;
	}
	
	///////////////// utils //////////////////
	///
	static Regex httpregex;

	static Regex httpsregex;

	/** https://www.rfc-editor.org/rfc/rfc3986#appendix-B */
//	static Regex rfc3986;

	/**
	 * Is the arg an HTTPS protocol address?
	 * @param p
	 */
	public static boolean isHttps(String p) {
		if (httpsregex == null)
			httpsregex = new Regex("^https://");
		return httpsregex.match(p);
	}
	
	static Regex envlregex;
	/**
	 * Is the arg an HTTPS or HTTP protocol address?
	 * @param p
	 */
	public static boolean isHttp(String p) {
		if (httpregex == null)
			httpregex = new Regex("^https?://");
		return httpregex.match(p);
	}
	
	static Regex volumeregex;
	public static boolean startsEvelope(String envl) {
		if (envlregex == null)
			envlregex = new Regex("'?\\{\\s*[\"\']type[\"\']:");
		return envlregex.match(envl);
	}
	
	public static boolean startsVolume(String isvol) {
		if (volumeregex == null)
			volumeregex = new Regex("\\$\\w\\s*$|[\\/\\\\]+");
		return volumeregex.match(isvol);
	}
	/** Remove volume tag ($.../) from exturi */
	public static String removeVolumePrefix(String exturi, String... removePrefix) {
		String relative = exturi.replaceAll("\\$\\w+((\\s*$)|[\\/\\\\]+)", "");
		if (removePrefix != null)
			for (String prefix : removePrefix)
				if (prefix != null)
					relative = relative.replaceFirst("^" + prefix + "((\\s*$)|[\\/\\\\]*)" , "");
		return relative;
	}

	/**
	 * @since 0.9.130
	 */
	static Regex reg_isIPv6 = new Regex("^(([^:/?#]+):)?(//)?\\[(::[0-9A-Fa-f]{1,4})|([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){7})\\](:\\d{1,8})?([/?#])?");

	/**
	 * @since 0.9.130
	 */
	static Regex reg_hostportv6 = new Regex("\\[((::[0-9A-Fa-f]{1,4})|([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){7}))\\](:(\\d+))?");

	/**
	 * Is the IP include a valid IP v6 address?
	 * 
	 * @param ip
	 * @return true if a valid ip 
	 * @since 0.9.130
	 */
	public static boolean isIPv6(String ip) {
		return reg_isIPv6.match(ip);
	}

	/**
	 * groups[3]: ip[:port]
	 * groups[4]: path
	 * https://www.rfc-editor.org/rfc/rfc3986#appendix-B
	 * @since 0.9.130
	 */
	static Regex reg3986 = new Regex("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");

	/**
	 * Regex for RFC3986 Schema
	 */
	static Regex protocolPrefix = new Regex("^(\\w+:)?//");

	/**
	 * Add "http://" to url if it's not begin with, and match it with
	 * <a href='https://www.rfc-editor.org/rfc/rfc3986#appendix-B'>rfc 3986 regex</a>.
	 * @param url, e.g. 127.0.0.1/index.html
	 * @return [(String)doamin/ip, (Integer)port], e.g. 127.0.0.1, null
	 * @deprecated
	public static Object[] getHostPort(String url) {
		if (!protocolPrefix.match(url))
			url = "http://" + url;

		ArrayList<String> grps = reg3986.findGroups(url);
		if (LangExt.isblank(grps.get(3)))
			return null;
		try {
			String[] iportss = grps.get(3).split(":");
			if (LangExt.len(iportss) == 2)
				return new Object[] {iportss[0], Integer.valueOf(iportss[1])};
			else return new Object[] {grps.get(3), null};
		}
		catch (Exception e) {
			return new Object[] {url, null};
		}
	}
	 */

	/**
	 * @since 0.9.130
	 * @param semiJserv
	 * @return nomalized jserv url (all url parts, in RFC3986, are present)
	 */
	public static String asJserv(String semiJserv) {
		Object[] parts = getHttpParts(semiJserv);
		return f("%s://%s%s%s%s%s", // schema, host, :port, /sub-paths, ?query, #fragment
				(boolean)parts[1] ? "https" : "http",
				parts[2],
				eqi(80, (int)parts[3]) || eqi(443, (int)parts[3]) ? "" : ":" + (int)parts[3],
				isNull((String[])parts[4]) ? "" : "/" + join("/", "", "", (Object[])parts[4]),
				isblank(parts[5]) ? "" : "?" + parts[5],
				isblank(parts[6]) ? "" : "#" + parts[6]);
	}
	
	/**
	 * 
	 * @param url
	 * @return<pre>
	 * [0] is ip v6 address
	 * [1] scheme true: https, false: possibly http
	 * [2] authority host
	 * [3] authority port
	 * [4] sub-paths, String[]
	 * [5] query
	 * [6] fragment</pre>
	 * @since 0.9.130
	 */
	public static Object[] getHttpParts(String url) {
		return isIPv6(url)
					? insertAt(getHttpsPartsv6(url), true,  0)
					: insertAt(getHttpsPartsv4(url), false, 0);
	}

	/**
	 * Parse parts in a URl with valid IP v6 address.
	 * Return of invalid IP v6 address is unspecified.
	 * @param url
	 * @return<pre>
	 * [0] scheme true: https, false: possibly http
	 * [1] authority host
	 * [2] authority port
	 * [3] sub-paths, String[]
	 * [4] query
	 * [5] fragment</pre>
	 * @since 0.9.130
	 */
	public static Object[] getHttpsPartsv6(String url) {
		if (!protocolPrefix.match(url))
			url = "http://" + url;

		ArrayList<String> grps = reg3986.findGroups(url);
		if (LangExt.isblank(grps.get(3)))
			return null;
		try {
			boolean https = eq("https", grps.get(1));
			boolean http  = eq("http", ifnull(grps.get(1), "http"));
			int port = https ? 443 : http? 80 : 0; 

			String host = grps.get(3);
			ArrayList<String> iportss = reg_hostportv6.findGroups(host);

			if (LangExt.len(iportss) == 6) {
				host = iportss.get(0);
				try { port = Integer.valueOf(iportss.get(5)); }
				catch (Exception e) {}
			}
			return new Object[] { https, "[" + host.replaceAll("(^\\[)|(\\]$)", "") + "]", port,
					isblank(grps.get(4), "/+") ? null : grps.get(4).replaceAll("^/*", "").split("/"),
					grps.get(6), grps.get(8)};
		}
		catch (Exception e) {
			return new Object[] {url, null};
		}
	}

	/**
	 * Get URL parts.
	 * See <a href='https://www.rfc-editor.org/rfc/rfc3986#section-3'>Section 3, RFC 3986</a>.
	 * <pre>
	 * foo://example.com:8042/over/there?name=ferret#nose
	 * \_/   \______________/\_________/ \_________/ \__/
	 * |           |            |            |        |
	 * scheme     authority       path        query   fragment
	 * </pre>
	 * @param url
	 * @return<pre>
	 * [0] scheme true: https, false: possibly http
	 * [1] authority host
	 * [2] authority port
	 * [3] sub-paths, String[]
	 * [4] query
	 * [5] fragment</pre>
	 */
	public static Object[] getHttpsPartsv4(String url) {
		if (!protocolPrefix.match(url))
			url = "http://" + url;

		ArrayList<String> grps = reg3986.findGroups(url);
		if (LangExt.isblank(grps.get(3)))
			return null;
		try {
			boolean https = eq("https", grps.get(1));
			boolean http  = eq("http", ifnull(grps.get(1), "http"));
			int port = https ? 443 : http? 80 : 0; 
			String host = grps.get(3);
			String[] iportss = host.split(":");
			if (LangExt.len(iportss) == 2) {
				host = iportss[0];
				try { port = Integer.valueOf(iportss[1]); }
				catch (Exception e) {}
			}
			return new Object[] { https, host, port,
					isblank(grps.get(4), "/+") ? null : grps.get(4).replaceAll("^/*", "").split("/"),
					grps.get(6), grps.get(8)};
		}
		catch (Exception e) {
			return new Object[] {url, null};
		}
	}

	/**
	 * 
	 * @param p
	 * @param range
	 * @return valid or not
	 * @since 0.9.130
	 */
	public static boolean validUrlPort(String p, int... range) {
		try {
			int port = Integer.valueOf(p);
			return validUrlPort(port, range);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @param port
	 * @param range
	 * @return valid or not
	 * @since 0.9.130
	 */
	public static boolean validUrlPort(int port, int... range) {
		if (isNull(range)) return port > 0;
		else {
			return (range[0] < 0 || port >= range[0])
				&& (range.length < 2 || range[1] < 0 || port <= range[1]);
		}
	}
	
	/**
	 * 
	 * @param expects
	 * @param subs
	 * @return valid or not
	 * @since 0.9.130
	 */
	public static boolean validPaths(String[] expects, String[] subs) {
		return isNull(expects) && isNull(subs) || Arrays.equals(expects, subs);
	}

}
