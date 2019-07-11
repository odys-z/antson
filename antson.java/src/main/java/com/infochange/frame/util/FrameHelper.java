package com.infochange.frame.util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

import com.infochange.frame.application.ManagedParaBean;

public class FrameHelper {
	private static int seq = 1001;

	public static int getASequenceNumber() { return ++seq; }

	/**Set value from savedInstanceState into persistedBean. Many of<br/>
	 * fld.set(persistedBean, savedInstanceState.getString(k));
	 * @param persistedBean target of which fld will be set.
	 * @param fld Field of persistedBean
	 * @param savedInstanceState source Bundle
	 * @param k name of value in savedInstanceState
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void setFieldValue(ManagedParaBean persistedBean, Field fld, Bundle savedInstanceState, String k) throws IllegalArgumentException, IllegalAccessException {
		Class<?> fcls = fld.getType();
		String fclsName = fcls.getName();
//		Log.d("te be deleted", fclsName);
		if (fclsName.equals("java.lang.String["))
			fld.set(persistedBean, savedInstanceState.getStringArray(k));
//		else if (fclsName.equals("java.lang.String"))
//			fld.set(persistedBean, savedInstanceState.getString(k));
//		else if (fclsName.equals("java.lang.Integer") || fclsName.equals("int"))
//			fld.setInt(persistedBean, savedInstanceState.getInt(k));
//		else if (fclsName.equals("java.lang.Boolean") || fclsName.equals("boolean"))
//			fld.setBoolean(persistedBean, savedInstanceState.getBoolean(k));
//		else if (fclsName.equals("java.lang.Float") || fclsName.equals("float"))
//			fld.setFloat(persistedBean, savedInstanceState.getFloat(k));
//		else if (fclsName.equals("java.lang.Double") || fclsName.equals("double"))
//			fld.setDouble(persistedBean, savedInstanceState.getDouble(k));
//		else if (fclsName.equals("java.lang.Long") || fclsName.equals("long"))
//			fld.setLong(persistedBean, savedInstanceState.getLong(k));
//		else if (fclsName.equals("java.lang.Byte") || fclsName.equals("byte"))
//			fld.setLong(persistedBean, savedInstanceState.getByte(k));
//		else if (fclsName.equals("java.lang.Char") || fclsName.equals("char"))
//			fld.setLong(persistedBean, savedInstanceState.getChar(k));
		else throw new IllegalArgumentException("unsuppored filed type: " + fclsName);
	}

	/**
	 * @param targetInt intent to be set
	 * @param srcBean source activity's managed ib
	 * @param fld Field of srcBean
	 * @param k
	 * @throws Exception 
	 */
	public static void setIntentWithField(Intent targetInt, ManagedParaBean srcBean, Field fld, String k) throws Exception {
		Class<?> fcls = fld.getType();
		String fclsName = fcls.getName();
		if (fclsName.equals("java.lang.Strng["))
			targetInt.putExtra(k, (String[]) fld.get(srcBean));
		else if (fclsName.equals("java.lang.String"));
		else if (fclsName.equals("java.lang.Integer") || fclsName.equals("int"));
		else if (fclsName.equals("java.lang.Boolean") || fclsName.equals("boolean"));
		else if (fclsName.equals("java.lang.Float") || fclsName.equals("float"));
		else if (fclsName.equals("java.lang.Double") || fclsName.equals("double"));
		else if (fclsName.equals("java.lang.Long") || fclsName.equals("long"));
		else if (fclsName.equals("java.lang.Byte") || fclsName.equals("byte"));
		else if (fclsName.equals("java.lang.Char") || fclsName.equals("char"));
		else if (fclsName.equals("java.util.Date"));
		else throw new IllegalArgumentException("unsuppored filed type: " + fclsName);
	}

	public static void readSerializedPrimitive(ObjectInputStream stream,
			ManagedParaBean beanObj, Field f) throws Exception {
		Class<?> fcls = f.getType();
		if (fcls.equals(Integer.TYPE))
			f.setInt(beanObj, stream.readInt());
		else if (fcls.equals(Boolean.TYPE)) {
			f.setBoolean(beanObj, stream.readBoolean());
//			int i = stream.readInt();
//			if (i == 0)
//				f.setBoolean(beanObj, false);
//			else
//				f.setBoolean(beanObj, true);
		}
		else if (fcls.equals(Float.TYPE))
			f.setFloat(beanObj, stream.readFloat());
		else if (fcls.equals(Double.TYPE))
			f.setDouble(beanObj, stream.readFloat());
		else if (fcls.equals(Long.TYPE))
			f.setLong(beanObj, stream.readLong());
		else if (fcls.equals(Byte.TYPE))
			f.setByte(beanObj, stream.readByte());
		else if (fcls.equals(Character.class))
			f.setChar(beanObj, stream.readChar());
		else throw new IllegalArgumentException("unsuppored filed type: " + f.getName());
	}

	public static void writePrimitiveSerialized(ObjectOutputStream stream,
			ManagedParaBean beanObj, Field f) throws Exception {
		String fclsName = f.getType().getName();
		if (fclsName.equals("java.lang.Integer") || fclsName.equals("int"))
			stream.writeInt(f.getInt(beanObj));
		else if (fclsName.equals("java.lang.Boolean") || fclsName.equals("boolean")) {
			stream.writeBoolean(f.getBoolean(beanObj));
//			if (f.getBoolean(beanObj))
//				stream.writeInt(1);
//			else stream.writeInt(0);
		}
		else if (fclsName.equals("java.lang.Float") || fclsName.equals("float"))
			stream.writeFloat(f.getFloat(beanObj));
		else if (fclsName.equals("java.lang.Double") || fclsName.equals("double"))
			stream.writeDouble(f.getDouble(beanObj));
		else if (fclsName.equals("java.lang.Long") || fclsName.equals("long"))
			stream.writeLong(f.getLong(beanObj));
		else if (fclsName.equals("java.lang.Byte") || fclsName.equals("byte"))
			stream.writeByte(f.getByte(beanObj));
		else if (fclsName.equals("java.lang.Char") || fclsName.equals("char"))
			stream.writeChar(f.getChar(beanObj));
		else throw new IllegalArgumentException("unsuppored filed type: " + fclsName);
	}

	/**Shallow copy field value from srcObj to distObj.
	 * @param fd
	 * @param distBean
	 * @param srcObj
	 */
	public static void copyPrimitiveField(Field fd, ManagedParaBean distBean,
			ManagedParaBean srcObj) throws Exception {
		String fclsName = fd.getType().getName();
		if (fclsName.equals("java.lang.Integer") || fclsName.equals("int"))
			fd.setInt(distBean, fd.getInt(srcObj));
		else if (fclsName.equals("java.lang.Boolean") || fclsName.equals("boolean"))
			fd.setBoolean(distBean, fd.getBoolean(srcObj));
		else if (fclsName.equals("java.lang.Float") || fclsName.equals("float"))
			fd.setFloat(distBean, fd.getFloat(srcObj));
		else if (fclsName.equals("java.lang.Double") || fclsName.equals("double"))
			fd.setDouble(distBean, fd.getDouble(srcObj));
		else if (fclsName.equals("java.lang.Long") || fclsName.equals("long"))
			fd.setLong(distBean, fd.getLong(srcObj));
		else if (fclsName.equals("java.lang.Byte") || fclsName.equals("byte"))
			fd.setByte(distBean, fd.getByte(srcObj));
		else if (fclsName.equals("java.lang.Char") || fclsName.equals("char"))
			fd.setChar(distBean, fd.getChar(srcObj));
		else throw new IllegalArgumentException("unsuppored filed type: " + fclsName);
	}

	private static HashMap<String, Object> memory;
//	public static Class<? extends AbsFuncActivity> recall(String name) {
	public static Object recall(String objName) {
		if (memory == null) return null;
		return memory.get(objName);
	}
	
	public static void remember(String objName, Object obj) {
		if (memory == null) memory = new HashMap<String, Object>();
		memory.put(objName, obj);
	}

	public static Object take(String kObj) { return memory.remove(kObj); }
}
