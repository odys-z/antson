/**
 * 
 */
package com.infochange.frame.application;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import com.infochange.frame.util.Bundle;
import com.infochange.frame.util.FrameHelper;
import com.infochange.frame.util.Intent;

/**
 * @author odysseus.edu@gmail.com
 */
public class BaseActivity {
	public static final int RESULT_OK = 0;
	public static final int RESULT_CANCELED = 1;
	public static final int MODE_PRIVATE = 1;

	protected static final String extraK4bundle = "BaseActivity.k";
	protected HashMap<Class<? extends AbsFuncActivity>, Field> managedIBs;
//	public static int autoRequestCode;
//	protected int lastReqcode = 0;
//	static { autoRequestCode = FrameHelper.getASequenceNumber(); }
//	public static int getRequestCode() { return autoRequestCode; }
	
//	protected Class<? extends AbsFuncActivity> currentTargetActivityCls;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (currentTargetActivityCls != null)
			outState.putString(this.getClass().getName(), currentTargetActivityCls.getName());
	}
	 */

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			if (savedInstanceState != null) {
				String crntTrgtClsName = savedInstanceState.getString(this.getClass().getName());
				if (crntTrgtClsName != null)
					currentTargetActivityCls = (Class<? extends AbsFuncActivity>) Class.forName(crntTrgtClsName);
			}
		} catch (ClassNotFoundException e) { e.printStackTrace(); }
	} */

	/**
	 * @param targetClass target activity class, sub class of AbsFuncActivity
	 * @param ibName4target
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	protected void manageIBs(Class<? extends AbsFuncActivity> targetClass, String ibName4target) throws Exception {
		boolean isTargetAbs = Modifier.isAbstract(targetClass.getModifiers());
		if (isTargetAbs)
			throw new IllegalArgumentException("BasActivity can't support an abstract target class. And it's make no sense.");
		if (managedIBs == null)
//			managedIBs = new HashMap<Class<? extends AbsFuncActivity>, ManagedParaBean>();
			managedIBs = new HashMap<Class<? extends AbsFuncActivity>, Field>();
		
		Field fd = this.getClass().getDeclaredField(ibName4target);
		if (fd != null) {
			if (!managedIBs.containsKey(targetClass)) {
				Class<? extends ManagedParaBean> fdcls = (Class<? extends ManagedParaBean>) fd.getType();
				checkSerializable(fdcls);
				Class<?> fdclsParent = fdcls.getDeclaringClass();
				Class<?> parameterTypes[];
				ManagedParaBean ib4target;
				
				// construct ib field - declared as inner class (target class can't be abstract)
				if (fdclsParent != null) {
					boolean isParentAbs = Modifier.isAbstract(fdclsParent.getModifiers());
					parameterTypes = new Class[1];
					parameterTypes[0] = fdclsParent;
					Constructor<? extends ManagedParaBean> ct = fdcls.getDeclaredConstructor(parameterTypes);
					if (!ct.isAccessible()) ct.setAccessible(true);
					
					// construct declaring class instance
					// parent is abstract, only target class is a subclass of parent class can be handled.
					if (isParentAbs) {
						Class<?> tempClass = targetClass;
						while (tempClass.getSuperclass() != null && !tempClass.getSuperclass().equals(fdclsParent)) {
							try { tempClass = tempClass.getSuperclass(); }
							catch (Exception e) { throw new IllegalArgumentException(
								"If field to be managed is declared as an inner class of an abstarct class, target class must be a subclass of: " + fdclsParent.getName()
								+ ". Otherwise make no sense. BaseActivity can't handle such senario."); }
						}
						ib4target = ct.newInstance(new Object[]{targetClass.newInstance()});
					}
					// parent is not an abstract
					else 
						ib4target = ct.newInstance(new Object[]{fdclsParent.newInstance()});
					
					/*
					Field reqCodeField = ib4target.getClass().getDeclaredField("requestCode");
					if (!reqCodeField.isAccessible()) reqCodeField.setAccessible(true);
					reqCodeField.set(ib4target, FrameHelper.getASequenceNumber());
					
					managedIBs.put(targetClass, ib4target);
					*/
				}
				
				// construct ib field - not a inner class (deny abstract class)
				else {
					parameterTypes = new Class[0];
					Constructor<? extends ManagedParaBean> ct = fdcls.getDeclaredConstructor(parameterTypes);
					if (!ct.isAccessible()) ct.setAccessible(true);
					ib4target = ct.newInstance(new Object[0]);
					
					/*
					Field reqCodeField = ib4target.getClass().getDeclaredField("requestCode");
					if (!reqCodeField.isAccessible()) reqCodeField.setAccessible(true);
					reqCodeField.set(ib4target, FrameHelper.getASequenceNumber());
					
					managedIBs.put(targetClass, ib4target);
					*/
				}
				
				Class<?> ibSuperCls = ib4target.getClass().getSuperclass();
				while (ibSuperCls != null && ibSuperCls != ManagedParaBean.class)
					ibSuperCls = ibSuperCls.getSuperclass();
				if (ibSuperCls == null)
					throw new IllegalArgumentException("IB for targaet activity must extends ManagedParaBean.");
				
				Field reqCodeField = ibSuperCls.getDeclaredField("requestCode");
				if (!reqCodeField.isAccessible()) reqCodeField.setAccessible(true);
				reqCodeField.set(ib4target, FrameHelper.getASequenceNumber());
//				managedIBs.put(targetClass, ib4target);
				managedIBs.put(targetClass, fd);
					
				if (!fd.isAccessible()) fd.setAccessible(true);
				fd.set(this, ib4target);
			}
			else {
				// changing orientation will reach here
				/*
				ManagedParaBean ib = managedIBs.get(targetClass);
				if (!fd.isAccessible()) fd.setAccessible(true);
				fd.set(this, ib);
				*/
			}
		}
	}
	
	/**Hash method readObject(...) ?
	 * @param fdcls
	 */
	protected void checkSerializable(Class<? extends ManagedParaBean> fdcls) {
		try {
			Class<ObjectInputStream> instrCls = ObjectInputStream.class;
			Method mread = fdcls.getDeclaredMethod("readObject", instrCls);
			if (!Modifier.isPrivate(mread.getModifiers()))
				throw new Exception();
		} catch (Exception e) {
			throw new IllegalArgumentException("Managed Bean must implement method: private void readObject(ObjectInputStream stream) in class: " + fdcls.getName() + ". See details of ManagedParaBean.");
		}
		try {
			Class<ObjectOutputStream> outstrCls = ObjectOutputStream.class;
			Method mread = fdcls.getDeclaredMethod("writeObject", outstrCls);
			if (!Modifier.isPrivate(mread.getModifiers()))
				throw new Exception();
		} catch (Exception e) {
			throw new IllegalArgumentException("Managed Bean must implement method: private void writeObject(ObjectOutputStream stream) in class: " + fdcls.getName() + ". See details of ManagedParaBean.");
		}
	}
	
	protected Field findBeanField(Class<?> targetClass, String fdname) throws NoSuchFieldException {
		try { return targetClass.getDeclaredField(fdname); }
		catch (NoSuchFieldException e) {
			Class<?> superCls = targetClass.getSuperclass();
			if (superCls == null) throw e;
			else return findBeanField(superCls, fdname);
		}
	}
	
	protected void startForResults(Class<? extends AbsFuncActivity> targetClass) {
		Intent in = new Intent();
		in.setClass(getApplicationContext(), targetClass);
		
		int reqCode = 1001;
		
		if (managedIBs != null) {
			Field ibFd = managedIBs.get(targetClass);
			if (ibFd != null) {
				try {
					ManagedParaBean ib = (ManagedParaBean) ibFd.get(this);
					Bundle b = new Bundle();
					b.putSerializable(extraK4bundle, ib);
					in.putExtra(targetClass.getName(), b);
				
					Field reqF = ib.getClass().getField("requestCode");
					reqCode = reqF.getInt(ib);
					FrameHelper.remember(this.getClass().getName(), targetClass);
					// startActivityForResult(in, reqCode);
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
	}

	/**Fake stub
	 * @return
	 */
	private Object getApplicationContext() {
		return null;
	}

	/**@Remarks Target class must been remembered by the activity, which can be re-onCreated for orientation, etc.<br/>
	 * If it's remembered in startForResults() as a member field, can be null now.<br/>
	 * Unlike IBs, current target class are unknown to onCreate, so has to be remembered in other ways.
	 * It's really weird.
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		
		Bundle b = data.getBundleExtra(extraK4bundle);
		ManagedParaBean returnedIB = (ManagedParaBean) b.getSerializable(extraK4bundle);
		
		if (returnedIB.requestCode != requestCode) return;
		else {
//			managedIBs.get(currentTargetActivityCls).copyValue(returnedIB);
			@SuppressWarnings("unchecked")
			Class<? extends AbsFuncActivity> currentTargetActivityCls = (Class<? extends AbsFuncActivity>) FrameHelper.recall(this.getClass().getName());
//			managedIBs.get(currentTargetActivityCls).copyValue(returnedIB);

			try {
				// fd may be null when the App is interrupted,
				// FIXME why?
				Field fd = managedIBs.get(currentTargetActivityCls);
				if (!fd.isAccessible()) fd.setAccessible(true);
				fd.set(this, returnedIB);
			} catch (Exception e) { e.printStackTrace(); }
		}
	}

	protected Intent getIntent() { return null; }

	public void finish() { }
		
	protected void setResult(int resultOk, Intent intent) { }

	public void onCreate(Bundle savedInstanceState) { }

	public Object getPreferences(int modePrivate) { return null; }
}
