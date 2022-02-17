package io.odysz.anson;

import java.util.HashSet;

/**This can't be serialized.
 * 
 * <p>According to <a href='https://www.json.org/json-en.html'>json.org</a>
 * there is no set type in json?
 * 
 * @author ody
 *
 */
public class AnsTSet extends Anson {

	HashSet<String> s;
	
	public AnsTSet() {}

	public AnsTSet(String e) {
		s = new HashSet<String>();
		s.add(e);
	}
}
