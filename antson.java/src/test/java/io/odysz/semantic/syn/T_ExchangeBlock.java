package io.odysz.semantic.syn;

import java.util.HashMap;

import io.odysz.anson.AnsonField;
import io.odysz.semantic.T_AnResultset;
import io.odysz.semantic.jprotocol.test.T_AnsonBody;
import io.odysz.semantic.jprotocol.test.T_AnsonMsg;

public class T_ExchangeBlock extends T_AnsonBody {

	String session;
	String peer;
	String srcnode;

	T_AnResultset synodes;
	T_AnResultset chpage;
	int chpagesize;
	int challengeSeq;

	T_AnResultset anspage;
	int answerSeq;
	int totalChallenges;
	
	int act;
	
	T_AnResultset entities;
	
	@AnsonField(valType = "io.odysz.semantic.syn.T_Nyquence")
	public HashMap<String, T_Nyquence> nv;

	public T_ExchangeBlock() {
		this(null, null);
	}

	public T_ExchangeBlock(T_AnsonMsg<? extends T_AnsonBody> parent, String uri) {
		super(parent, uri);
	}

}
