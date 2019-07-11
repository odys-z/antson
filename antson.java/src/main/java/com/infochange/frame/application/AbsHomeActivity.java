/**
 * 
 */
package com.infochange.frame.application;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.infochange.frame.util.Bundle;

/**
 * @author odysseus.edu@gmail.com
 */
public class AbsHomeActivity extends BaseActivity {
	private ManagedParaBean persistBean;
	
	/**@param pbFieldName persisted bean's field name.
	 * @throws Exception 
	 */
	protected void managePersistBean(String pbFieldName) throws Exception {
//		Field pbField = getClass().getDeclaredField(pbFieldName);
		Field pbField = findBeanField(getClass(), pbFieldName);
		
		@SuppressWarnings("unchecked")
		Class<? extends ManagedParaBean> pbClass = (Class<? extends ManagedParaBean>) pbField.getType();
		Class<?>[] parameterTypes;
		Class<?> parentCls = pbClass.getDeclaringClass();
		if (parentCls == null) {
			parameterTypes = new Class[0];
			Constructor<? extends ManagedParaBean> ct = pbClass.getDeclaredConstructor(parameterTypes);
			if (!ct.isAccessible()) ct.setAccessible(true);
			persistBean = ct.newInstance(new Object[0]);
		}
		else {
			parameterTypes = new Class[1];
			parameterTypes[0] = parentCls;
			Constructor<? extends ManagedParaBean> ct = pbClass.getDeclaredConstructor(parameterTypes);
			if (!ct.isAccessible()) ct.setAccessible(true);
			persistBean = ct.newInstance(new Object[]{this});
		}
		if (!pbField.isAccessible()) pbField.setAccessible(true);
		pbField.set(this, persistBean);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	 	Object prefed = getPreferences(MODE_PRIVATE);
	 	if (prefed != null && persistBean != null) {
	 		// String strPB = prefed.getString(extraK4bundle, null);
	 		byte[] strPB = new byte[] {'?'};
	 		if (strPB != null) {
	 			try {
//	 				ByteArrayInputStream bais = new ByteArrayInputStream(strPB.getBytes("UTF-8"));
	 				
	 				ByteArrayInputStream bais = new ByteArrayInputStream((strPB));
	 				
//	 				persistBean = new ManagedParaBean();
					persistBean.readObjectImpl(new ObjectInputStream(bais));
				} catch (Exception e) { e.printStackTrace(); }
	 		}
	 	}
		if (savedInstanceState != null)
			persistBean.copyValue((ManagedParaBean) savedInstanceState.getSerializable(extraK4bundle));
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(extraK4bundle, persistBean);
	}
	 */

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	@Override
	protected void onPause() {
		super.onPause();
		savePersistedBean();
	}

	protected void savePersistedBean() {
		SharedPreferences.Editor prefed = getPreferences(MODE_PRIVATE).edit();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			persistBean.writeObjectImpl(new ObjectOutputStream(baos));
//			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//			byte buffer[] = new byte[bais.available()];
//			bais.read(buffer);
//			String vstr = new String(buffer, "UTF-8");
//			String vstr = new String(baos.toByteArray(), "UTF-8");
			String vstr = new String(Base64Coder.encode(baos.toByteArray()));
			
			prefed.putString(extraK4bundle, vstr);
		} catch (Exception e) { e.printStackTrace(); }
		prefed.commit();
	}
	 */
}
