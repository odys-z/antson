package com.infochange.frame.application;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import com.infochange.frame.util.FrameHelper;

import io.odysz.common.Utils;

/**Base class for persisted bean and inter activity bean.<br/>
 * Because Serializable is a mark interface,
 * every Serializable implementing class must explicitly call to serializing implementation.
 * Add this two line to every subclass of ManagedParaBean:<br/><br/><b>
 * private void readObject(ObjectInputStream stream) throws Exception { super.readObjectImpl(stream); }<br/>
 * private void writeObject(ObjectOutputStream stream) throws Exception { super.writeObjectImpl(stream); }</b>
 * 
 * @author odysseus.edu@gmail.com
 */
public class ManagedParaBean implements Serializable {
	private static final String Tag = "ManagedParaBean";
	protected static final long serialVersionUID = 1L;
	
	public int requestCode = 1001;
	/*
	private Class<? extends FrameActivity> managerCls;

	/ **@deprecated
	 * @param managerClass
	public ManagedParaBean(Class<? extends FrameActivity> managerClass) {
		super();
		managerCls = managerClass;
	}
	
	/**@deprecated
	 * @return
	public Class<? extends FrameActivity> getManagerClass() { return managerCls; }
	 */
	
	/**Copy srcObj to may field.
	 * @param srcObj
	 */
	public void copyValue(ManagedParaBean srcObj) {
		if (srcObj.getClass().equals(getClass())) {
			Field flist[] = this.getClass().getDeclaredFields();
			for (Field f : flist) {
				f.setAccessible(true);
				try {
					if (!f.getType().isPrimitive())
						f.set(this, f.get(srcObj));
					else FrameHelper.copyPrimitiveField(f, this, srcObj);
				}catch (Exception e) { e.printStackTrace(); }
			}
			
			Class<?> supercls = srcObj.getClass().getSuperclass();
			while(supercls != null && supercls != ManagedParaBean.class)
				supercls = supercls.getSuperclass();
			if (supercls != null) {
				try {
					Field reqcodFd = supercls.getDeclaredField("requestCode");
					reqcodFd.set(this, reqcodFd.getInt(srcObj));
				} catch (Exception e) { e.printStackTrace(); }
			}
			else throw new IllegalArgumentException("Object must be type of subclass of ManagedParaBean.");
		}
		else
			throw new IllegalArgumentException("srcObj must be the same type.");
	}
	
	protected void readObjectImpl(ObjectInputStream stream) throws Exception {
		Field flist[] = this.getClass().getDeclaredFields();
		requestCode = stream.readInt();
		Class<?> parentCls = getClass().getDeclaringClass();
		for (Field f : flist) {
			f.setAccessible(true);
			// prevent serialize parent class instance, which is not serializable.
			if (!f.getType().isPrimitive() && (parentCls == null || !parentCls.equals(f.getType())))
				f.set(this, stream.readObject());
			else if (f.getType().isPrimitive())
				FrameHelper.readSerializedPrimitive(stream, this, f);
		}
	}
	
	protected void writeObjectImpl(ObjectOutputStream stream) throws Exception {
		Field flist[] = this.getClass().getDeclaredFields();
		stream.writeInt(requestCode);
		Class<?> parentCls = getClass().getDeclaringClass();
		for (Field f : flist) {
			f.setAccessible(true);
			if (!f.getType().isPrimitive() && (parentCls == null || !parentCls.equals(f.getType())))
				try { stream.writeObject(f.get(this)); }
				catch (NotSerializableException e) {
					Utils.warn(Tag, String.format("Filed in bean (%s) to be managed can't be serialized. Type %s must implemente Serializable.",
							f.getName(), f.getClass().getName()));
				}
			else if (f.getType().isPrimitive())
				FrameHelper.writePrimitiveSerialized(stream, this, f);
		}
		stream.flush();
	}
}
