package io.odysz.anson;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Fields of IJsonable types which are not Anson, but written as envelopes, by
 * {@link Anson#toEnvelope(IJsonable, OutputStream, JsonOpt...)}, like semantic.transact's AnDbField
 * (semantic.DA's DocRef and its subclasses, e.g. semantic-doc's SDocRef). No factory is registered
 * for them: they are read back from their envelopes, as Anson objects are. Since 1.2.1.
 */
public class AnsT7Envelope extends Anson {

	/**
	 * Not an Anson, written as an envelope, as AnDbField: the base type of the fields.
	 */
	public static class T7_Ref implements IJsonable {
		public String uids;
		public String uri64;

		public T7_Ref() { }

		public T7_Ref(String uids, String uri64) {
			this.uids = uids;
			this.uri64 = uri64;
		}

		@Override
		public IJsonable toBlock(OutputStream stream, JsonOpt... opts) throws AnsonException, IOException {
			return Anson.toEnvelope(this, stream, opts);
		}

		@Override
		public IJsonable toJson(StringBuffer buf) throws IOException, AnsonException {
			return Anson.fromJson(buf.toString());
		}
	}

	/** A subclass, as SDocRef of DocRef. */
	public static class T7_SubRef extends T7_Ref {
		public String title;

		public T7_SubRef() { }

		public T7_SubRef(String uids, String uri64, String title) {
			super(uids, uri64);
			this.title = title;
		}
	}

	/** Another IJsonable written as an envelope, not a T7_Ref. */
	public static class T7_Other implements IJsonable {
		public String other;

		public T7_Other() { }

		@Override
		public IJsonable toBlock(OutputStream stream, JsonOpt... opts) throws AnsonException, IOException {
			return Anson.toEnvelope(this, stream, opts);
		}

		@Override
		public IJsonable toJson(StringBuffer buf) throws IOException, AnsonException {
			return Anson.fromJson(buf.toString());
		}
	}

	public T7_Ref ref;
	public T7_Ref sub;
	public T7_Ref none;

	@AnsonField(valType="io.odysz.anson.AnsT7Envelope$T7_Ref")
	public ArrayList<T7_Ref> refs;

	public AnsT7Envelope() { }
}
