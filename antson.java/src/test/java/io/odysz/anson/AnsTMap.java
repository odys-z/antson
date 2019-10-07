package io.odysz.anson;

import java.util.HashMap;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonField;

public class AnsTMap extends Anson {

	public HashMap<String, String> map;
	
	@AnsonField(valType="[Ljava.lang.Object;")
	public HashMap<String, Object[]> mapArr;

	public AnsTMap() {
		map = new HashMap<String, String>();
	}
}
