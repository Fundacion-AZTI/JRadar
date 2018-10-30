/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  30 de oct. de 2018
 */
package es.azti.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of the DataBaseInterface with the fillBean generic method
 * ready to use. The extended clases must implement the mock method, to run the
 * software with no access to the database just for development and testing
 * purpouses.
 * 
 * @author Jose Luis Asensio (jlasensio@azti.es) 30 de oct. de 2018
 *
 */
public abstract class DataBaseBean implements DataBaseInterface {

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.azti.db.DataBaseInterface#fillBean(java.util.HashMap)
	 */
	/**
	 * Uses de reflection to fill the data in the bean. The class must have the
	 * same name the ddbb table, and the attributes must have the same name the
	 * table columns have.
	 */
	@Override
	public void fillBean(HashMap<String, String> data)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Iterator<Entry<String, String>> it = data.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();

			Method[] metodos = this.getClass().getMethods();
			for (Method met : metodos) {
				if (met.getName().toUpperCase().contains(pair.getKey().toUpperCase())
						&& met.getName().startsWith("set")) {
					met.invoke(this, (String) pair.getValue());
					break;
				}
			}

		}
	}
}
