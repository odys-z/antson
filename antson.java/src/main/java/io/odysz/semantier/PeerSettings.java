package io.odysz.semantier;

import java.util.ArrayList;

import io.odysz.anson.Anson;

/**
 * @since 1.0.5
 */
public class PeerSettings extends Anson {

	/** E.g. "io.odysz.semantic.jprotocol.AnsonMsg" */
	public String ansonMsg;

	public String[] requestMsgs;
	
	/** @deprecated shouldn't be used other than in python generators. */
    public String header;

	/** @deprecated shouldn't be used other than in python generators. */
    public String json_h;

	/** @deprecated shouldn't be used other than in python generators. */
    public String py;

	/** @deprecated shouldn't be used other than in python generators. */
    public String ts;

    public ArrayList<ArrayList<String>> requests;
}
