package io.odysz.semantic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;

class AnResultsetTest {

	@Test
	void test() throws Exception {
		T_AnResultset rs = new T_AnResultset(2, 2);
		rs.index0("1");

		rs.beforeFirst().next();
		assertEquals("0, 1", rs.getString("1"));
		assertEquals("0, 2", rs.getString("2"));
		rs.next();
		assertEquals("1, 1", rs.getString("1"));
		assertEquals("1, 2", rs.getString("2"));

		assertEquals("1, 2", rs.getStringByIndex("2", "1, 1"));

		// {"type": "io.odysz.semantic.T_AnResultset",
		//  "stringFormats": null, "total": 0, "rowCnt": 2, "colCnt": 2,
		//  "colnames": {"1": [1, "1"], "2": [2, "2"]}, "rowIdx": 2,
		//  "indices0": {"1, 1": 1, "0, 1": 0}, "flatcols": null, 
		//  "results": [["0, 1", "0, 2"], ["1, 1", "1, 2"]]}
		String msg = rs.toBlock();
		
		T_AnResultset rs2 = (T_AnResultset) Anson.fromJson(msg);

		assertEquals("1, 2", rs2.getStringByIndex("2", "1, 1"));
	}

}
