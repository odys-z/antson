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
	
}
