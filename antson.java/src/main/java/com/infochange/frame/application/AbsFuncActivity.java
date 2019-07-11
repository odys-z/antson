/**
 * 
 */
package com.infochange.frame.application;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.infochange.frame.util.Bundle;
import com.infochange.frame.util.Intent;

/**Base activity for function activities.
 * @author odysseus.edu@gmail.com
 */
public class AbsFuncActivity extends BaseActivity {
	protected ManagedParaBean parentIB;
	protected Field pb4pField;

	protected void manageIb4Parent(String ib4parent) throws Exception {
//		pb4pField = getClass().getDeclaredField(ib4parent);
		pb4pField = findBeanField(getClass(), ib4parent);
		
		@SuppressWarnings("unchecked")
		Class<? extends ManagedParaBean> pbClass = (Class<? extends ManagedParaBean>) pb4pField.getType();
		Class<?>[] parameterTypes;
		Class<?> parentCls = pbClass.getDeclaringClass();
		if (parentCls == null) {
			parameterTypes = new Class[0];
			Constructor<? extends ManagedParaBean> ct = pbClass.getDeclaredConstructor(parameterTypes);
			if (!ct.isAccessible()) ct.setAccessible(true);
			parentIB = ct.newInstance(new Object[0]);
		}
		else {
			parameterTypes = new Class[1];
			parameterTypes[0] = parentCls;
			Constructor<? extends ManagedParaBean> ct = pbClass.getDeclaredConstructor(parameterTypes);
			if (!ct.isAccessible()) ct.setAccessible(true);
			parentIB = ct.newInstance(new Object[]{this});
		}
		if (!pb4pField.isAccessible()) pb4pField.setAccessible(true);
		pb4pField.set(this, parentIB); 
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent pintent = getIntent();
		try {
			if (pintent != null) {
				Bundle b = pintent.getBundleExtra(getClass().getName());
//				Class<? extends ManagedParaBean> v = (Class<? extends ManagedParaBean>) b.getSerializable(extraK4bundle);
				ManagedParaBean v = (ManagedParaBean) b.getSerializable(extraK4bundle);
				if (v != null)
					parentIB.copyValue(v);
				else parentIB = null;
				if (pb4pField != null)
					pb4pField.set(this, parentIB);
			}
			else {} // won't reach here.
		}catch (Exception e) { e.printStackTrace(); }
	}

	public void finish() {
		Intent intent = new Intent();
		if (parentIB != null) {
			Bundle b = new Bundle();
			b.putSerializable(extraK4bundle, parentIB);
			intent.putExtra(extraK4bundle, b);
		}
		setResult(RESULT_OK, intent);
		super.finish();
	}
	

	public void cancle() {
		setResult(RESULT_CANCELED);
		super.finish();
	}

	private void setResult(int resultCanceled) {
		// TODO Auto-generated method stub
		
	}
}
