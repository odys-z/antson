package io.oz.sandbox.sheet.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;
import io.odysz.anson.x.AnsonException;

@SuppressWarnings("unused")
class SpreadsheetTest {

	@Test
	void testToJson() throws Exception {
	}

	@Test
	void testFromJson() throws AnsonException {
		SpreadsheetReq_Test<MyCurriculum> anson;

		/* FIXME let's finish this
		anson = (SpreadsheetReq<MyCurriculum>) Anson.fromJson(
				"{type: \"io.oz.sandbox.sheet.test.SpreadsheetReq<T extends MyCurriculum>\", "
				+ "rec: {type: io.oz.sandbox.sheet.test.MyCurriculum, \"cid\": \"00001\"}}");
		assertEquals("00001", anson.rec.cid);

		anson = (SpreadsheetReq<MyCurriculum>) Anson.fromJson(
				"{type: io.oz.sandbox.sheet.test.SpreadsheetReq<T extends MyCurriculum>, "
				+ "rec: {type: io.oz.sandbox.sheet.test.MyCurriculum, \"cid\": \"00002\"}}");
		assertEquals("00002", anson.rec.cid);
		*/
	}

}
