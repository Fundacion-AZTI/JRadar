/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  25 de jul. de 2018
 */
package es.azti.db;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es)
 * 29 de Agosto de 2018
 *
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * class to connect and retrieve information from the European radar node data
 * base.
 * 
 * @author Jose Luis Asensio (jlasensio@azti.es) 30 de oct. de 2018
 *
 */
public class DBSQLAccess {

	// logger
	private static Logger log = Logger.getLogger(DBSQLAccess.class);

	// database connection url including password and username.
	// Your IP must be registered in the data base to get access. the user and
	// password
	// does not work alone.
	private static String dburl = "jdbc:mysql://150.145.136.8:3306?user=JRadar&password=Z8JHxpRLc0wvpruh&serverTimezone=UTC";
	private static String dbName = "HFR_node_db";
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;

	/**
	 * main call to run the class and print values. just to development
	 * purpouses.
	 * 
	 * @param args
	 *            table_name, network_id station_id
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		DBSQLAccess dao = new DBSQLAccess();

		// DataBaseBean table = dao.getTable(args[0], args[1], args[2]);
		// table.printData();

		// DataBaseBean table = dao.getMockTable("network_tb", "network_id",
		// "HFR_LaMMA");
		// table.printData();

		// DataBaseBean table2 = dao.getMockTable("station_tb", "station_id",
		// "SVIN");
		// table2.printData();

		// DataBaseBean table3 = dao.getMockTable("station_tb", "station_id",
		// "LIVO");
		// table3.printData();
		//
		DataBaseBean table = dao.getTable("network_tb", "network_id", "HFR_LaMMA");
		table.printData();

		// dao.getTable("station_tb", "station_id", "SVIN");
		// dao.getTable("station_tb", "station_id", "LIVO");
	}

	/**
	 * Mock method to get values to fill the beans but not connecting to the
	 * database. It uses reflection to call fillMockBean method in the
	 * DataBaseBean extended classes.
	 * 
	 * @param tableId
	 *            Table of which we want to get values. The class that extend
	 *            the DataBaseBean must be named the same way to use reflection
	 *            properly.
	 * @return a bean of class TableId with dummy values loaded from a mock
	 *         method.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public DataBaseBean getMockTable(String tableId) throws ClassNotFoundException, SQLException {
		DataBaseBean tableBean = null;
		try {
			tableBean = (DataBaseBean) (Class.forName("es.azti.db." + tableId.toUpperCase()).getConstructor()
					.newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			log.fatal("Error mocking table: ", e);
		}
		tableBean.fillMockBean();
		return tableBean;
	}

	/**
	 * Method to retrieve the data from the database. A generic SQL query is
	 * created, to select all the values from the TableID table in the data
	 * base, using the fieldId and fieldValue as selectors. The result is
	 * returned in a generic DataBaseBean, all the data base class beans should
	 * extend this superclass For example, if tableId = "NETWORK_TB",
	 * fieldId="EDIOS_Series_id" and fieldValue="HFR_TirLig" the query is SELEC
	 * * FROM NETWORK_TB WHERE EDIOS_Series_id="HFR_TirLig"
	 * 
	 * The fieldID should be a primaryKey, we only read the first row of the
	 * response. It is working with any field id and values but only the first
	 * value is readed. TODO: modify to retrieve an array
	 * 
	 * @param tableId
	 *            The name of the table we want to run the select.
	 * @param fieldId
	 *            the name of the column to apply the where clause within the
	 *            select query
	 * @param fieldValue
	 *            the value for the where clause within the select query
	 * @return A bean containing the values.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public DataBaseBean getTable(String tableId, String fieldId, String fieldValue)
			throws ClassNotFoundException, SQLException {
		openConnection();

		String query = composeQuery(tableId, fieldId, fieldValue);
		// Result set get the result of the SQL query
		resultSet = statement.executeQuery(query);
		DataBaseBean table = null;
		try {
			table = parseResultSet(resultSet);
		} catch (Exception e) {
			log.error("generic error retrieving info from database: ", e);
		}

		closeConnection();
		return table;
	}

	/**
	 * Method to get the station with last Calibration date that belongs
	 * to a specific Id.
	 * 
	 * @param tableId
	 *            The name of the table we want to run the select.
	 * @param fieldId
	 *            the name of the column to apply the where clause within the
	 *            select query
	 * @param fieldValue
	 *            the value for the where clause within the select query
	 * @return A bean containing the values.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public DataBaseBean getCalibration(String tableId, String fieldId, String fieldValue)
			throws ClassNotFoundException, SQLException {
		openConnection();

		String query = composeCalibrationQuery(tableId, fieldId, fieldValue);
		// Result set get the result of the SQL query
		resultSet = statement.executeQuery(query);
		DataBaseBean table = null;
		try {
			table = parseResultSet(resultSet);
		} catch (Exception e) {
			log.error("generic error retrieving info from database: ", e);
		}

		closeConnection();
		return table;
	}

	/**
	 * Creates the query based on the table name, column and value we are asking
	 * for. For example, if tableId = "NETWORK_TB", fieldId="EDIOS_Series_id"
	 * and fieldValue="HFR_TirLig" the query is SELEC * FROM NETWORK_TB WHERE
	 * EDIOS_Series_id="HFR_TirLig"
	 * 
	 * @param tableId
	 *            The name of the table we want to run the select.
	 * @param fieldId
	 *            the name of the column to apply the where clause within the
	 *            select query
	 * @param fieldValue
	 *            the value for the where clause within the select query
	 * @return
	 */
	private String composeQuery(String tableId, String fieldId, String fieldValue) {
		StringBuffer sb = new StringBuffer();

		sb.append("select * from ");
		sb.append(dbName);
		sb.append(".");
		sb.append(tableId);
		sb.append(" where ");
		sb.append(fieldId);
		sb.append(" = '");
		sb.append(fieldValue);
		sb.append("';");

		return sb.toString();
	}

	/**
	 * Creates the query based on the table name, column and value we are asking
	 * for but only get the one with the latest calibration. 
	 * For example, if tableId = "STATION_TB", fieldId="EDIOS_Series_id"
	 * and fieldValue="HFR_TirLig" the query is 
	 * SELEC * FROM STATION_TB WHERE 
	 * tableId = "HFR_TirLig" AND last_calibration_date= 
	 * (SELECT MAX(last_calibration_date) * FROM STATION_TB
	 * WHERE tableId="HFR_TirLig")
	 * 
	 * @param tableId
	 *            The name of the table we want to run the select.
	 * @param fieldId
	 *            the name of the column to apply the where clause within the
	 *            select query
	 * @param fieldValue
	 *            the value for the where clause within the select query
	 * @return
	 */
	private String composeCalibrationQuery(String tableId, String fieldId, String fieldValue) {
		StringBuffer sb = new StringBuffer();

		sb.append("select * from ");
		sb.append(dbName);
		sb.append(".");
		sb.append(tableId);
		sb.append(" where ");
		sb.append(fieldId);
		sb.append(" = '");
		sb.append(fieldValue);
		sb.append("' and last_calibration_date = (select max(last_calibration_date) from ");
		sb.append(dbName);
		sb.append(".");
		sb.append(tableId);
		sb.append(" where ");
		sb.append(fieldId);
		sb.append(" = '");
		sb.append(fieldValue);
		sb.append("');");
		
		return sb.toString();
	}	
	

	/**
	 * creates the connection and starts the communication with the data base.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private void openConnection() throws ClassNotFoundException, SQLException {
		// This will load the MySQL driver, each DB has its own driver
		Class.forName("com.mysql.cj.jdbc.Driver");
		// Setup the connection with the DB
		connect = DriverManager.getConnection(dburl);
		// Statements allow to issue SQL queries to the database
		statement = connect.createStatement();
	}

	/**
	 * closes the connection to the data base.
	 * 
	 * @throws SQLException
	 */
	private void closeConnection() throws SQLException {
		if (resultSet != null) {
			resultSet.close();
		}

		if (statement != null) {
			statement.close();
		}

		if (connect != null) {
			connect.close();
		}

	}

	/**
	 * translates the result set obtained after running a query. Parses the
	 * value to the correct class and fill the bean. It only takes the first
	 * value of the result set. If more, they are not used. This method and
	 * class is supposed to work only with primary keys. It uses reflection to
	 * fill the class. The class defined with the table name must be a
	 * DataBaseBean subclass. TODO: refactor to work with arrays (for stations
	 * belonging to the same network for example)
	 * 
	 * @param resultSet
	 *            response of a query with the values we want to use.
	 * @return A bean of a class extending DataBaseBean with the information
	 *         parsed.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private DataBaseBean parseResultSet(ResultSet resultSet)
			throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// Now get some metadata from the database
		// Result set get the result of the SQL query

		log.debug("The columns in the table are: ");
		log.debug("Table: " + resultSet.getMetaData().getTableName(1));

		resultSet.first();
		HashMap<String, String> data = new HashMap<String, String>();
		for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
			data.put(resultSet.getMetaData().getColumnName(i), resultSet.getString(i));
			log.debug("Column " + i + " " + resultSet.getMetaData().getColumnName(i) + " Value: "
					+ resultSet.getString(i));
		}

		// Search for the bean to fill the info using the table name and
		// reflection.
		String table = resultSet.getMetaData().getTableName(1);
		DataBaseBean tableBean = null;
		try {
			tableBean = (DataBaseBean) (Class.forName("es.azti.db." + table.toUpperCase()).getConstructor()
					.newInstance());
			tableBean.fillBean(data);
		} catch (Exception e) {
			log.fatal("Couldnt get info from data base", e);
		}
		return tableBean;
	}
}
