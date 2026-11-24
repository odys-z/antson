package io.odysz.anson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import io.odysz.anson.AnsT7Envelope.T7_Other;
import io.odysz.anson.AnsT7Envelope.T7_Ref;
import io.odysz.anson.AnsT7Envelope.T7_SubRef;

/**
 * A field of an IJsonable type which is not an Anson, written as an envelope by its own
 * toBlock() ({@link Anson#toEnvelope(IJsonable, java.io.OutputStream, JsonOpt...)}, as
 * semantic.transact's AnDbField), is read back the way it was written, like an Anson,
 * without a registered factory (JSONAnsonListener, since 1.2.1).
 */
class EnvelopeJsonableTest {

	@Test
	void testEnvelopeField() throws Exception {
		AnsT7Envelope t = new AnsT7Envelope();
		t.ref  = new T7_Ref("X29,00A3F1", "/vol/album/IMG_0412.jpg");
		t.sub  = new T7_SubRef("X29,00A3F2", null, "Spring Trip");
		t.none = null;
		t.refs = new ArrayList<T7_Ref>();
		t.refs.add(new T7_Ref("X29,00A3F7", null));
		t.refs.add(new T7_SubRef("X29,00A3F8", "/vol/album/tea.mp4", "Tea"));

		String json = t.toBlock();
		// written as envelopes, by the fields' own toBlock()
		assertTrue(json.contains("\"type\": \"io.odysz.anson.AnsT7Envelope$T7_Ref\""), json);
		assertTrue(json.contains("\"type\": \"io.odysz.anson.AnsT7Envelope$T7_SubRef\""), json);

		AnsT7Envelope back = (AnsT7Envelope) Anson.fromJson(json);

		// the field's declared type
		assertInstanceOf(T7_Ref.class, back.ref);
		assertEquals("X29,00A3F1", back.ref.uids);
		assertEquals("/vol/album/IMG_0412.jpg", back.ref.uri64);

		// a subclass, its own fields kept, declared as the base type (as SDocRef of DocRef)
		assertInstanceOf(T7_SubRef.class, back.sub);
		assertEquals("X29,00A3F2", back.sub.uids);
		assertNull(back.sub.uri64);
		assertEquals("Spring Trip", ((T7_SubRef) back.sub).title);

		// null stays null
		assertNull(back.none);

		// the list's elements, as before
		assertEquals(2, back.refs.size());
		assertEquals("X29,00A3F7", back.refs.get(0).uids);
		assertNull(back.refs.get(0).uri64);
		assertInstanceOf(T7_SubRef.class, back.refs.get(1));
		assertEquals("Tea", ((T7_SubRef) back.refs.get(1)).title);

		// the same json again
		assertEquals(json, back.toBlock());
	}

	/**
	 * An envelope of another type, not an instance of the field's type, is not assigned: as before,
	 * such a field needs a registered factory.
	 */
	@Test
	void testOtherType() throws Exception {
		T7_Other o = new T7_Other();
		o.other = "not a T7_Ref";
		String json = "{\"type\": \"io.odysz.anson.AnsT7Envelope\", \"ref\": " + o.toBlock() + "}";

		AnsonException e = assertThrows(AnsonException.class, () -> Anson.fromJson(json));
		assertTrue(e.getMessage().contains("must registered"), e.getMessage());
	}
}
