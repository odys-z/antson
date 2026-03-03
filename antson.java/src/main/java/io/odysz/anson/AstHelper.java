package io.odysz.anson;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

public class AstHelper {

	public static ByteArrayOutputStream toAst(Class cls, ByteArrayOutputStream bos) {
		HashMap<String, Field> fmap = new HashMap<String, Field>();
		fmap = JSONAnsonListener.mergeFields(cls, fmap);
				
		return bos;
	}

}
