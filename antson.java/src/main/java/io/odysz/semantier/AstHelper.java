package io.odysz.semantier;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

import io.odysz.anson.JSONAnsonListener;

/**
 * @since 1.0.5
 */
public class AstHelper {

	public static ByteArrayOutputStream toAst(Class cls, ByteArrayOutputStream bos) {
		HashMap<String, Field> fmap = new HashMap<String, Field>();
		fmap = JSONAnsonListener.mergeFields(cls, fmap);
				
		return bos;
	}

}
