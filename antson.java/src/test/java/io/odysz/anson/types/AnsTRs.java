package io.odysz.anson.types;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonResultset;

public class AnsTRs extends Anson {

	public AnsonResultset rs;
	
	public AnsTRs() {
		rs = new AnsonResultset(3, 4);
	}
}
