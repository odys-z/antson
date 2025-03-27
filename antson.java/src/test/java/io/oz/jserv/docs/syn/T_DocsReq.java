package io.oz.jserv.docs.syn;

import io.odysz.semantic.jprotocol.test.T_PageInf;
import io.odysz.semantic.jprotocol.test.T_UserReq;


public class T_DocsReq extends T_UserReq {
	public static class A {
		public static final String syncdocs = "r/syncs";
		public static final String orgNodes = "r/synodes";
		public static final String records = "r/list";
		public static final String mydocs = "r/my-docs";
		public static final String rec = "r/rec";
		public static final String download = "r/download";
		public static final String upload = "c";
		public static final String del = "d";
		public static final String blockStart = "c/b/start";
		public static final String blockUp = "c/b/block";
		public static final String blockEnd = "c/b/end";
		public static final String blockAbort = "c/b/abort";
		public static final String selectSyncs = "r/syncflags";
		public static final String devices = "r/devices";
		public static final String registDev = "c/device";
		public static final String checkDev = "r/check-dev";
		public static String requestSyn = "u/syn";
	}

	public String synuri;

	public String docTabl;
	public T_DocsReq docTabl(String tbl) {
		docTabl = tbl;
		return this;
	}

	public T_ExpSyncDoc doc;

	public T_PageInf pageInf;

	/**
	 * @param whereqs (n0, v0), (n1, v1), ..., must be even number of elements.
	 * @return this
	 */
	public T_DocsReq pageInf(int page, int size, String... whereqs) {
		pageInf = new T_PageInf(page, size, whereqs);
		return this;
	}

	String[] deletings;

	/**
	 * <b>Note: use {@link #DocsReq(String)}</b><br>
	 * Don't use this constructor - should only be used by json deserializer. 
	 */
	public T_DocsReq() {
		super(null, null);
		blockSeq = -1;
	}

	/**
	 * @param syncTask i.e. docTable name, could be a design problem?
	 */
	public T_DocsReq(String syncTask, String uri) {
		super(null, uri);
		blockSeq = -1;
		docTabl = syncTask;
	}

	protected String stamp;
	public String stamp() { return stamp; }

	protected T_Device device; 
	public T_Device device() { return device; }
	public T_DocsReq device(String devid) {
		device = new T_Device(devid, devid, devid, devid);
		return this;
	}

	long blockSeq;
	public long blockSeq() { return blockSeq; } 

	public T_DocsReq nextBlock;

	/**
	 * <p>Document sharing domain.</p>
	 * for album synchronizing, this is h_photos.family (not null).
	 * */
	public String org;
	public T_DocsReq org(String org) { this.org = org; return this; }

	/** If the chain already exists when starting, reset it. */
	public boolean reset;

	long limit = -1;

	public T_DocsReq doc(String device, String fullpath) {
		this.device = new T_Device(device, null, device, device);
		this.doc = new T_ExpSyncDoc().device(device).clientpath(fullpath);
		return this;
	}

	public T_DocsReq doc(T_ExpSyncDoc doc) {
		this.doc = doc;
		this.device = new T_Device(doc.device, doc.device, doc.device, doc.device);
		return this;
	}
}
