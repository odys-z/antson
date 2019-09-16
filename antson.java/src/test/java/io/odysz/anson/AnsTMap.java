package io.odysz.anson;

import java.util.HashMap;

public class AnsTMap extends Anson {

	HashMap<String, String> map;
	
	@AnsonField(valType="[Ljava.lang.Object;")
	HashMap<String, Object[]> mapArr;

	public AnsTMap() {
		map = new HashMap<String, String>();
	}
}
