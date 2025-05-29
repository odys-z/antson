package io.odysz.common;

import java.util.ArrayList;
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
	static Regex rfc3986;

	static Regex envlregex;

	/**
	 * Is the arg an HTTPS protocol address?
	 * @param p
	 */
	public static boolean isHttps(String p) {
		if (httpsregex == null)
			httpsregex = new Regex("^https://");
		return httpsregex.match(p);
	}
	
	/**
	 * Is the arg an HTTPS or HTTP protocol address?
	 * @param p
	 */
	public static boolean isHttp(String p) {
		if (httpregex == null)
			httpregex = new Regex("^https?://");
		return httpregex.match(p);
	}
	
	public static boolean startsEvelope(String envl) {
		if (envlregex == null)
			envlregex = new Regex("'?\\{\\s*[\"\']type[\"\']:");
		return envlregex.match(envl);
	}
	
	/**
	 * groups[3]: ip[:port]
	 * groups[4]: path
	 * https://www.rfc-editor.org/rfc/rfc3986#appendix-B
	 * */
	static Regex reg3986 = new Regex("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");
	
	static Regex protocolPrefix = new Regex("^(\\w+:)?//");

	/**
	 * Add "http://" to url if it's not begin with, and match it with
	 * <a href='https://www.rfc-editor.org/rfc/rfc3986#appendix-B'>rfc 3986 regex</a>.
	 * @param url, e.g. 127.0.0.1/index.html
	 * @return [(String)doamin/ip, (Integer)port], e.g. 127.0.0.1, null
	 */
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
}
