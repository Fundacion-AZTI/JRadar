/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  29 de ago. de 2018
 */
package es.azti.db;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Interface to retrieve data from the european central node data base.
 * 
 * @author Jose Luis Asensio (jlasensio@azti.es) 29 de ago. de 2018
 *
 */
public interface DataBaseInterface {

	/**
	 * The only way to fill the beans from the data base at the moment. We must
	 * pass a hashmap and the beans will use reflection to set the values.
	 * 
	 * @param data
	 *            HashMap containing the name of the attributes and the values
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public abstract void fillBean(HashMap<String, String> data)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	// simulate a query and return a Object of the corresponding sublass with
	// fake values (just for testing
	/**
	 * Only for developing purpouse The data base access is restricted by ip so,
	 * to allow working in different places this method helps having some
	 * response This method cannot be used in prod.
	 */
	public abstract void fillMockBean();

	public abstract void printData();
}
