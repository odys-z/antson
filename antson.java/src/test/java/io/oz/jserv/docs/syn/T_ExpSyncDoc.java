package io.oz.jserv.docs.syn;

import java.io.IOException;
import java.util.Date;

import io.odysz.anson.Anson;
import io.odysz.anson.AnsonField;

/**
 * A sync object, server side and jprotocol oriented data record,
 * used for docsync.jserv. 
 * 
 * @author ody
 */
public class T_ExpSyncDoc extends Anson {
	protected static String[] synpageCols;

	public String recId;
	public String recId() { return recId; }
	public T_ExpSyncDoc recId(String did) {
		recId = did;
		return this;
	}

	public String pname;
	public String clientname() { return pname; }
	public T_ExpSyncDoc clientname(String clientname) {
		pname = clientname;
		return this;
	}

	public String clientpath;
	public String fullpath() { return clientpath; }

	public String mime() { return mime; }

	/** Non-public: doc' device id is managed by session. */
	protected String device;
	public String device() { return device; }
	public T_ExpSyncDoc device(String device) {
		this.device = device;
		return this;
	}
	
	public final String org = "";

	public String shareflag;
	public String shareflag() { return shareflag; }

	/** usally reported by client file system, overriden by exif date, if exits */
	public String createDate;
	public String cdate() { return createDate; }
	public T_ExpSyncDoc cdate(String cdate) {
		createDate = cdate;
		return this;
	}

	@AnsonField(shortenString=true)
	public String uri64;
	public String uri() { return uri64; }
	public T_ExpSyncDoc uri(String synuri) {
		this.uri64 = synuri;
		return this;
	}

	public String shareby;
	public String sharedate;
	
//	public String syncFlag;

	/** usually ignored when sending request */
	public long size;

	public T_ExpSyncDoc shareby(String share) {
		this.shareby = share;
		return this;
	}

	public T_ExpSyncDoc sharedate(String format) {
		sharedate = format;
		return this;
	}

	public T_ExpSyncDoc share(String shareby, String flag, String sharedate) {
		this.shareflag = flag;
		this.shareby = shareby;
		sharedate(sharedate);
		return this;
	}

	public T_ExpSyncDoc share(String shareby, String s, Date sharedate) {
		this.shareflag = s;
		this.shareby = shareby;
		sharedate(sharedate.toString());
		return this;
	}

	public String mime;
	
	public T_ExpSyncDoc fullpath(String clientpath) throws IOException {
		this.clientpath = clientpath;

		return this;
	}


	protected String folder;
	public String folder() { return folder; }
	public T_ExpSyncDoc folder(String v) {
		this.folder = v;
		return this;
	}

	/**
	 * Parse {@link PathsPage#clientPaths}.
	 * 
	 * @param flags
	 * @return this
	public T_ExpSyncDoc parseFlags(String[] flags) {
		if (!isNull(flags)) {
			syncFlag = flags[0];
			shareflag = flags[1];
			shareby = flags[2];
			sharedate(flags[3]);
		}
		return this;
	}
	 */

	public T_ExpSyncDoc clientpath(String fullpath) {
		this.clientpath = fullpath;
		return this;
	}

	public T_ExpSyncDoc size(int s) {
		this.size = s;
		return this;
	}

	public T_ExpSyncDoc mime(String mime) {
		this.mime = mime;
		return this;
	}

	public T_ExpSyncDoc createDate(String date) {
		this.createDate = date;
		return this;
	}
}