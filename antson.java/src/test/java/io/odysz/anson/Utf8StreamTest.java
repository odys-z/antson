package io.odysz.anson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Streams must always be UTF-8, whatever the platform charset is.
 * Run also with -Dfile.encoding=GBK (default on JRE 17 Chinese Windows),
 * where {@link Anson#escape(Object, JsonOpt...)} used to emit GBK bytes,
 * which the peer parses as UTF-8 into U+FFFD.
 */
public class Utf8StreamTest {

	@Test
	void testNonLatinStream() throws Exception {
		String pth = "E:/PM/PM葵花宝典.docx";

		AnsT1 t = new AnsT1();
		t.ver = pth;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		t.toBlock(bos);
		byte[] bytes = bos.toByteArray();

		// bytes on the wire are UTF-8
		String json = new String(bytes, StandardCharsets.UTF_8);
		assertEquals(true, json.contains(pth), json);

		// and round trip through the stream parser
		AnsT1 back = (AnsT1) Anson.fromJson(new ByteArrayInputStream(bytes));
		assertEquals(pth, back.ver);

		// escape() itself
		assertEquals(pth, new String(Anson.escape(pth), StandardCharsets.UTF_8));
	}
}
