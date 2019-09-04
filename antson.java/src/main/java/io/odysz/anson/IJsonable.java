package io.odysz.anson;

import java.io.IOException;
import java.io.OutputStream;

import io.odysz.anson.x.AnsonException;

public interface IJsonable {

	IJsonable toBlock(OutputStream stream) throws AnsonException, IOException;
	IJsonable toJson(StringBuffer buf) throws IOException, AnsonException;
}
