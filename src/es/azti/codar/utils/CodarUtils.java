/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  18 de may. de 2016
 */
package es.azti.codar.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import es.azti.codar.beans.CodarRadialBean;
import es.azti.codar.beans.CodarTotalBean;
import es.azti.codar.beans.RadialQCQATestBean;
import es.azti.codar.beans.TotalQCQATestBean;
import es.azti.utils.Lldistkm;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import es.azti.codar.beans.CodarDataTableBean;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es) 18 de may. de 2016
 * 
 *         Generic tools for codar data..
 */
public class CodarUtils {

	// logger
	private static Logger log = Logger.getLogger(CodarUtils.class);

	/**
	 * load a total codar file into a Bean
	 * 
	 * @param codarFile
	 *            File refering to a .tuv file.
	 * @param props
	 *            codar.properties file loaded, the attributes we are loading
	 *            are defined there.
	 * @return A compete CodarTotalBean with the required information.
	 * @throws ParseException
	 */
	public static CodarTotalBean loadCodarTotalData(File codarFile, Properties props) throws ParseException {

		CodarTotalBean data = new CodarTotalBean();
		HashMap<String, String> propiedades = new HashMap<String, String>();
		for (Object id : props.keySet()) {
			String name = (String) id;
			String value = props.getProperty(name);
			propiedades.put(value, name);
		}

		// start loading data
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(codarFile));
			data.setFileName(codarFile.getName());
			data.setPathToFile(codarFile.getAbsolutePath());
			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				// locate readed property
				String[] nameValue = currentLine.split(": ");
				if (nameValue.length > 1) {
					String name = nameValue[0];
					if (name.startsWith("%"))
						name = name.substring(1);
					if (props.containsValue(name)) {
						// locate and store using generic setters.
						try {
							String setter = "set" + propiedades.get(name).substring(21);
							Method method = CodarTotalBean.class.getMethod(setter, new Class[] { String.class });
							method.invoke(data, nameValue[1]);
						} catch (IllegalAccessException e) {
							log.error("Illegal Acces - attribute not set", e);
						} catch (IllegalArgumentException e) {
							log.error("Illegal argument - attribute not set", e);
						} catch (InvocationTargetException e) {
							log.error("Target error  - attribute not set", e);
						} catch (NoSuchMethodException e) {
							log.error("Getter not found - attribute not set", e);
						} catch (SecurityException e) {
							log.error("Security error - attribute not set", e);
						}
					} else if (name.startsWith("SiteSource")) {
						data.addSiteSource(nameValue[1]);
					} else if (name.contains("Table")) {
						// TODO improve talbe data loading section
						CodarDataTableBean rtb = new CodarDataTableBean();
						ArrayList<Float[]> datos = new ArrayList<Float[]>();
						while (currentLine != null & !currentLine.startsWith("%TableEnd:")) {
							// TODO duplicated code in radials, check if
							// refactoring is possible.
							name = nameValue[0];
							if (name.startsWith("%"))
								name = name.substring(1);
							if (name.equals("TableType") && nameValue.length > 1) {
								rtb.setType(nameValue[1]);
							} else if (name.equals("TableColumns") && nameValue.length > 1) {
								rtb.setColumns(Integer.parseInt(nameValue[1]));
							} else if (name.equals("TableColumnTypes") && nameValue.length > 1) {
								rtb.setColumnTypes((nameValue[1]).split(" "));
							} else if (name.equals("TableRows") && nameValue.length > 1) {
								rtb.setRows(Integer.parseInt(nameValue[1]));
							} else if (!name.startsWith("%") && !name.startsWith("TableStart")) {
								String[] tempData = ((name.trim()).split("\\s+"));
								// check if the data is part of the data table
								// and there are not corrupt lines with less
								// columns
								if (tempData.length == rtb.getColumns() && rtb.getType().contains("LLUV TOT")) {
									Float[] tempFloat = new Float[tempData.length];
									for (int i = 0; i < tempData.length; i++) {
										try {
											tempFloat[i] = Float.parseFloat(tempData[i]);
										} catch (NumberFormatException ex) {
											log.error("Error loading data", ex);
										}
									}
									datos.add(tempFloat);
								}
							}
							currentLine = reader.readLine();
							nameValue = currentLine.split(": ");
						}
						// for now.
						if (rtb.getType().contains("LLUV TOT")) {
							rtb.setData(datos.toArray(new Float[rtb.getRows()][rtb.getColumns()]));
							data.setTable(rtb);
						}
					}
				}
			}
			data.setTotalTest(new TotalQCQATestBean());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			if (data.getTimeStamp() != null) {
				Calendar coverageStart = data.getTimeStampAsCalendar();
				coverageStart.add(Calendar.MINUTE, -30);
				data.setTime_coverage_start(dateFormat.format(coverageStart.getTime()) + "T"
						+ hourFormat.format(coverageStart.getTime()) + "Z");
				Calendar coverageEnd = data.getTimeStampAsCalendar();
				coverageEnd.add(Calendar.MINUTE, 30);
				data.setTime_coverage_end(dateFormat.format(coverageEnd.getTime()) + "T"
						+ hourFormat.format(coverageEnd.getTime()) + "Z");
			}
			// time-data values: creation data
			data.setDateCreated(dateFormat.format(Calendar.getInstance().getTime()) + "T"
					+ hourFormat.format(Calendar.getInstance().getTime()) + "Z");
			data.setDateCollected(dateFormat.format(data.getTimeStampAsCalendar().getTime()) + "T"
					+ hourFormat.format(data.getTimeStampAsCalendar().getTime()) + "Z");
			data.setHistCreated(
					data.getDateCollected() + " data collected. " + dateFormat.format(Calendar.getInstance().getTime())
							+ " " + hourFormat.format(Calendar.getInstance().getTime())
							+ "   netCDF file created using JRADAR Software");
			data.setDate_modified(data.getDateCreated());

			String siteCode = data.getFileName().substring(5, 9);
			String fileTime = data.getFileName().substring(10, 25);
			data.setId(siteCode + '_' + fileTime.substring(0, 10).replaceAll("_", "-") + "_"
					+ fileTime.substring(11, 13) + 'Z');

			reader.close();

		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "We could not find CODAR file");
			log.error("CODAR file not found", e);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "We could not read CODAR file");
			log.error("Error trying to read CODAR file", e);
		}
		return data;
	}

	/**
	 * load a radial codar file into a Bean
	 * 
	 * @param codarFile
	 *            File refering to a .ruv file.
	 * @param props
	 *            codar.properties file loaded, the attributes we are loading
	 *            are defined there.
	 * @return A compete CodarRadialBean with the required information.
	 * @throws ParseException
	 */

	public static CodarRadialBean loadCodarRadialData(File codarFile, Properties props) throws ParseException {

		CodarRadialBean data = new CodarRadialBean();

		HashMap<String, String> propiedades = new HashMap<String, String>();
		for (Object id : props.keySet()) {
			String name = (String) id;
			String value = props.getProperty(name);

			propiedades.put(value, name);
		}

		// start loading data
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(codarFile));
			data.setFileName(codarFile.getName());
			data.setPathToFile(codarFile.getAbsolutePath());
			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				// locate readed property
				String[] nameValue = currentLine.split(": ");
				if (nameValue.length > 1) {
					String name = nameValue[0];
					if (name.startsWith("%"))
						name = name.substring(1);
					if (props.containsValue(name)) {
						// locate and store using generic setters.
						try {
							String setter = "set" + propiedades.get(name).substring(21);
							Method method = CodarRadialBean.class.getMethod(setter, new Class[] { String.class });
							method.invoke(data, nameValue[1]);
						} catch (IllegalAccessException e) {
							log.error("Illegal Acces - attribute not set", e);
						} catch (IllegalArgumentException e) {
							log.error("Illegal argument - attribute not set", e);
						} catch (InvocationTargetException e) {
							log.error("Target error  - attribute not set", e);
						} catch (NoSuchMethodException e) {
							log.error("Getter not found - attribute not set", e);
						} catch (SecurityException e) {
							log.error("Security error - attribute not set", e);
						}
					} else if (name.contains("Table")) {
						// TODO improve table data loading
						CodarDataTableBean rtb = new CodarDataTableBean();
						ArrayList<Float[]> datos = new ArrayList<Float[]>();
						while (currentLine != null & !currentLine.startsWith("%TableEnd:")) {
							// TODO duplicated code in total loading methond,
							// refactorize.
							name = nameValue[0];
							if (name.startsWith("%"))
								name = name.substring(1);
							if (name.equals("TableType") && nameValue.length > 1) {
								rtb.setType(nameValue[1]);
							} else if (name.equals("TableColumns") && nameValue.length > 1) {
								rtb.setColumns(Integer.parseInt(nameValue[1]));
							} else if (name.equals("TableColumnTypes") && nameValue.length > 1) {
								rtb.setColumnTypes((nameValue[1]).split(" "));
							} else if (name.equals("TableRows") && nameValue.length > 1) {
								rtb.setRows(Integer.parseInt(nameValue[1]));
							} else if (!name.startsWith("%") && !name.startsWith("TableStart")) {
								String[] tempData = ((name.trim()).split("\\s+"));
								// check if the table is the correct one and
								// that the lines are not corrupt
								// the column number must fit.
								if (tempData.length == rtb.getColumns() && rtb.getType().contains("LLUV RDL")) {
									Float[] tempFloat = new Float[tempData.length];
									for (int i = 0; i < tempData.length; i++) {
										try {
											tempFloat[i] = Float.parseFloat(tempData[i]);
										} catch (NumberFormatException ex) {
											log.error("Error loading data", ex);
										}
									}
									datos.add(tempFloat);
								}
							}
							currentLine = reader.readLine();
							nameValue = currentLine.split(": ");
						}
						// for now
						if (rtb.getType().contains("LLUV RDL")) {
							rtb.setData(datos.toArray(new Float[rtb.getRows()][rtb.getColumns()]));
							data.setTable(rtb);
						}
					}
				}
			}
			data.setRadialTest(new RadialQCQATestBean());

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			if (data.getTimeStamp() != null) {
				Calendar coverageStart = data.getTimeStampAsCalendar();
				coverageStart.add(Calendar.MINUTE, -30);
				data.setTime_coverage_start(dateFormat.format(coverageStart.getTime()) + "T"
						+ hourFormat.format(coverageStart.getTime()) + "Z");
				Calendar coverageEnd = data.getTimeStampAsCalendar();
				coverageEnd.add(Calendar.MINUTE, 30);
				data.setTime_coverage_end(dateFormat.format(coverageEnd.getTime()) + "T"
						+ hourFormat.format(coverageEnd.getTime()) + "Z");
			}
			// time-data values: creation data
			data.setDateCreated(dateFormat.format(Calendar.getInstance().getTime()) + "T"
					+ hourFormat.format(Calendar.getInstance().getTime()) + "Z");
			data.setDateCollected(dateFormat.format(data.getTimeStampAsCalendar().getTime()) + "T"
					+ hourFormat.format(data.getTimeStampAsCalendar().getTime()) + "Z");
			data.setHistCreated(
					data.getDateCollected() + " data collected. " + dateFormat.format(Calendar.getInstance().getTime())
							+ " " + hourFormat.format(Calendar.getInstance().getTime())
							+ "  netCDF file created using JRADAR Software");
			data.setDate_modified(data.getDateCreated());

			String siteCode = data.getFileName().substring(5, 9);
			String fileTime = data.getFileName().substring(10, 25);
			data.setId(siteCode + '_' + fileTime.substring(0, 10).replaceAll("_", "-") + "_"
					+ fileTime.substring(11, 13) + 'Z');

			reader.close();

		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "We could not find CODAR file");
			log.error("CODAR file not found", e);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "We could not read CODAR file");
			log.error("Error trying to read CODAR file", e);
		}
		return data;
	}

	/**
	 * method to transform data in a 1D array TODO check if a multidimension
	 * array generic method could be implemented.
	 * 
	 * @param data
	 *            Array 1D
	 * @return
	 */
	public static Array transformCollectionInArray(@SuppressWarnings("rawtypes") List data) {
		Array returnArray = Array.factory(DataType.FLOAT, new int[] { data.size() });
		for (int i = 0; i < data.size(); i++) {
			returnArray.setObject(i, data.get(i));
		}
		return returnArray;
	}

	/**
	 * Takes an ArrayList of float values and returns a multidimensional Float
	 * array (from 1D to 4D depending on the dimensions parameter).
	 * 
	 * @param data
	 *            the information to transform
	 * @param dimensions
	 *            variable that defines the dimensions we are needing.
	 * @return multidimensional Float array from 1D to 4D
	 */
	public static Array transformCollectionInMultidimensionalFloatArray(List<Float> data, List<Dimension> dimensions) {
		ArrayFloat A = null;
		if (dimensions.size() == 0) {
			//nada
			A = new ArrayFloat.D0();
		} else if (dimensions.size() == 1) {
			Dimension dimid_1 = dimensions.get(0);
			A = new ArrayFloat.D1(dimid_1.getLength());
			Index ima = A.getIndex();
			// 1D not tested
			for (int i = 0; i < dimid_1.getLength(); i++) {
				if (i < data.size())
					A.setFloat(ima.set(i), (float) (data.get(i)));
				else {
					A.setFloat(ima.set(i), Float.NaN);
				}
			}
		} else if (dimensions.size() == 2) {
			Dimension dimid_1 = dimensions.get(0);
			Dimension dimid_2 = dimensions.get(1);

			A = new ArrayFloat.D2(dimid_1.getLength(), dimid_2.getLength());
			Index ima = A.getIndex();
			// 2D not tested
			for (int i = 0; i < dimid_1.getLength(); i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					if (j + i * dimid_2.getLength() < data.size())
						A.setFloat(ima.set(i, j), (float) (data.get(j + i * dimid_2.getLength())));
					else {
						A.setFloat(ima.set(i, j), Float.NaN);
					}
				}
			}
		} else if (dimensions.size() == 3) {

			Dimension dimid_1 = dimensions.get(0);
			Dimension dimid_2 = dimensions.get(1);
			Dimension dimid_3 = dimensions.get(2);

			A = new ArrayFloat.D3(dimid_1.getLength(), dimid_2.getLength(), dimid_3.getLength());
			Index ima = A.getIndex();
			for (int i = 0; i < dimid_1.getLength(); i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					for (int k = 0; k < dimid_3.getLength(); k++) {
						if (k + j * dimid_3.getLength() + i * dimid_3.getLength() * dimid_2.getLength() < data.size())
							A.setFloat(ima.set(i, j, k), (float) (data
									.get(k + j * dimid_3.getLength() + i * dimid_3.getLength() * dimid_2.getLength())));
						else {
							A.setFloat(ima.set(i, j, k), Float.NaN);
						}
					}
				}
			}
		} else if (dimensions.size() == 4) {
			int dimid_1 = dimensions.get(0).getLength() == 0 ? 1 : dimensions.get(0).getLength();
			Dimension dimid_2 = dimensions.get(1);
			Dimension dimid_3 = dimensions.get(2);
			Dimension dimid_4 = dimensions.get(3);

			A = new ArrayFloat.D4(dimid_1, dimid_2.getLength(), dimid_3.getLength(), dimid_4.getLength());
			Index ima = A.getIndex();
			for (int i = 0; i < dimid_1; i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					for (int k = 0; k < dimid_3.getLength(); k++) {
						for (int l = 0; l < dimid_4.getLength(); l++) {
							if (l + k * dimid_4.getLength() + j * dimid_4.getLength() * dimid_3.getLength()
									+ i * dimid_4.getLength() * dimid_3.getLength() * dimid_2.getLength() < data.size())
								A.setFloat(ima.set(i, j, k, l), (float) (data.get(l + k * dimid_4.getLength()
										+ j * dimid_4.getLength() * dimid_3.getLength()
										+ i * dimid_4.getLength() * dimid_3.getLength() * dimid_2.getLength())));
							else {
								A.setFloat(ima.set(i, j, k, l), Float.NaN);
							}
						}
					}
				}
			}
		}
		return A;
	}

	/**
	 * Takes an ArrayList of float values and returns a multidimensional Double
	 * array (from 1D to 4D depending on the dimensions parameter).
	 * 
	 * @param data
	 *            the information to transform
	 * @param dimensions
	 *            variable that defines the dimensions we are needing.
	 * @return multidimensional Double array from 1D to 4D
	 */
	public static Array transformCollectionInMultidimensionalDoubleArray(List<Float> data, List<Dimension> dimensions) {
		ArrayDouble A = null;
		if (dimensions.size() == 0) {
			A = new ArrayDouble.D0();
		} else if (dimensions.size() == 1) {
			Dimension dimid_1 = dimensions.get(0);
			A = new ArrayDouble.D1(dimid_1.getLength());
			Index ima = A.getIndex();
			// 1D not tested
			for (int i = 0; i < dimid_1.getLength(); i++) {
				if (i < data.size())
					A.setDouble(ima.set(i), (float) (data.get(i)));
				else {
					A.setDouble(ima.set(i), Double.NaN);
				}
			}
		} else if (dimensions.size() == 2) {
			Dimension dimid_1 = dimensions.get(0);
			Dimension dimid_2 = dimensions.get(1);

			A = new ArrayDouble.D2(dimid_1.getLength(), dimid_2.getLength());
			Index ima = A.getIndex();
			// 2D not tested
			for (int i = 0; i < dimid_1.getLength(); i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					if (j + i * dimid_2.getLength() < data.size())
						A.setDouble(ima.set(i, j), (float) (data.get(j + i * dimid_2.getLength())));
					else {
						A.setDouble(ima.set(i, j), Double.NaN);
					}
				}
			}
		} else if (dimensions.size() == 3) {

			Dimension dimid_1 = dimensions.get(0);
			Dimension dimid_2 = dimensions.get(1);
			Dimension dimid_3 = dimensions.get(2);

			A = new ArrayDouble.D3(dimid_1.getLength(), dimid_2.getLength(), dimid_3.getLength());
			Index ima = A.getIndex();
			for (int i = 0; i < dimid_1.getLength(); i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					for (int k = 0; k < dimid_3.getLength(); k++) {
						if (k + j * dimid_3.getLength() + i * dimid_3.getLength() * dimid_2.getLength() < data.size())
							A.setDouble(ima.set(i, j, k), (float) (data
									.get(k + j * dimid_3.getLength() + i * dimid_3.getLength() * dimid_2.getLength())));
						else {
							A.setDouble(ima.set(i, j, k), Double.NaN);
						}
					}
				}
			}
		} else if (dimensions.size() == 4) {
			int dimid_1 = dimensions.get(0).getLength() == 0 ? 1 : dimensions.get(0).getLength();
			Dimension dimid_2 = dimensions.get(1);
			Dimension dimid_3 = dimensions.get(2);
			Dimension dimid_4 = dimensions.get(3);

			A = new ArrayDouble.D4(dimid_1, dimid_2.getLength(), dimid_3.getLength(), dimid_4.getLength());
			Index ima = A.getIndex();
			for (int i = 0; i < dimid_1; i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					for (int k = 0; k < dimid_3.getLength(); k++) {
						for (int l = 0; l < dimid_4.getLength(); l++) {
							if (l + k * dimid_4.getLength() + j * dimid_4.getLength() * dimid_3.getLength()
									+ i * dimid_4.getLength() * dimid_3.getLength() * dimid_2.getLength() < data.size())
								A.setDouble(ima.set(i, j, k, l), (float) (data.get(l + k * dimid_4.getLength()
										+ j * dimid_4.getLength() * dimid_3.getLength()
										+ i * dimid_4.getLength() * dimid_3.getLength() * dimid_2.getLength())));
							else {
								A.setDouble(ima.set(i, j, k, l), Double.NaN);
							}
						}
					}
				}
			}
		}
		return A;
	}

	/**
	 * Takes a String and returns a multidimensional char array (from 1D to 3D
	 * depending on the dimensions parameter). It writes the string in the last
	 * dimension always, the first if presents are suposed to be of length 1
	 * always.
	 * 
	 * @param input
	 *            The string to transfomr
	 * @param dimensions
	 *            variable that defines the dimensions we are needing.
	 * @return multidimensional char array from 1D to 3D
	 */
	public static Array transformStringIntoArrayChar(String input, List<Dimension> dimensions) {
		char[] scod = input.toCharArray();
		ArrayChar scodData = null;
		if (dimensions.size() == 2) {
			scodData = new ArrayChar.D2(1, scod.length);
			int i = 0;
			Index ima = scodData.getIndex();
			for (char aa : scod) {
				scodData.setChar(ima.set(0, i++), aa);
			}
		} else if (dimensions.size() == 3) {
			scodData = new ArrayChar.D3(1, 1, scod.length);
			int i = 0;
			Index ima = scodData.getIndex();
			for (char aa : scod) {
				scodData.setChar(ima.set(0, 0, i++), aa);
			}
		}
		return scodData;
	}

	/**
	 * Takes an ArrayList of float values and returns a multidimensional Short
	 * array (from 1D to 4D depending on the dimensions parameter).
	 * 
	 * @param data
	 *            the information to transform
	 * @param dimensions
	 *            variable that defines the dimensions we are needing.
	 * @return multidimensional Short array from 1D to 4D
	 */
	public static Array transformCollectionInMultidimensionalShortArray(List<Float> data, List<Dimension> dimensions, int scale) {
		ArrayShort A = null;
		if (dimensions.size() == 0) {
			//nada
			A = new ArrayShort.D0(false);
		} else if (dimensions.size() == 1) {
			Dimension dimid_1 = dimensions.get(0);
			A = new ArrayShort.D1(dimid_1.getLength(),false);
			Index ima = A.getIndex();
			// 1D not tested
			for (int i = 0; i < dimid_1.getLength(); i++) {
				if (i < data.size())
					A.setShort(ima.set(i), CodarUtils.floatToShort((float) (data.get(i) * scale)));
				else {
					A.setShort(ima.set(i), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT);
				}
			}
		} else if (dimensions.size() == 2) {
			Dimension dimid_1 = dimensions.get(0);
			Dimension dimid_2 = dimensions.get(1);

			A = new ArrayShort.D2(dimid_1.getLength(), dimid_2.getLength(),false);
			Index ima = A.getIndex();
			// 2D not tested
			for (int i = 0; i < dimid_1.getLength(); i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					if (j + i * dimid_2.getLength() < data.size())
						A.setShort(ima.set(i, j),
								CodarUtils.floatToShort((float) (data.get(j + i * dimid_2.getLength()) * scale)));
					else {
						A.setShort(ima.set(i, j), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT);
					}
				}
			}
		} else if (dimensions.size() == 3) {

			Dimension dimid_1 = dimensions.get(0);
			Dimension dimid_2 = dimensions.get(1);
			Dimension dimid_3 = dimensions.get(2);

			A = new ArrayShort.D3(dimid_1.getLength(), dimid_2.getLength(), dimid_3.getLength(),false);
			Index ima = A.getIndex();
			for (int i = 0; i < dimid_1.getLength(); i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					for (int k = 0; k < dimid_3.getLength(); k++) {
						if (k + j * dimid_3.getLength() + i * dimid_3.getLength() * dimid_2.getLength() < data.size())
							A.setShort(ima.set(i, j, k), CodarUtils.floatToShort((float) data
									.get(k + j * dimid_3.getLength() + i * dimid_3.getLength() * dimid_2.getLength()) * scale));
						else {
							A.setShort(ima.set(i, j, k), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT);
						}
					}
				}
			}
		} else if (dimensions.size() == 4) {
			int dimid_1 = dimensions.get(0).getLength() == 0 ? 1 : dimensions.get(0).getLength();
			Dimension dimid_2 = dimensions.get(1);
			Dimension dimid_3 = dimensions.get(2);
			Dimension dimid_4 = dimensions.get(3);

			A = new ArrayShort.D4(dimid_1, dimid_2.getLength(), dimid_3.getLength(), dimid_4.getLength(),false);
			Index ima = A.getIndex();
			for (int i = 0; i < dimid_1; i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					for (int k = 0; k < dimid_3.getLength(); k++) {
						for (int l = 0; l < dimid_4.getLength(); l++) {
							if (l + k * dimid_4.getLength() + j * dimid_4.getLength() * dimid_3.getLength()
									+ i * dimid_4.getLength() * dimid_3.getLength() * dimid_2.getLength() < data.size())
								A.setShort(ima.set(i, j, k, l), CodarUtils.floatToShort((float) data.get(l
										+ k * dimid_4.getLength() + j * dimid_4.getLength() * dimid_3.getLength()
										+ i * dimid_4.getLength() * dimid_3.getLength() * dimid_2.getLength()) * scale));
							else {
								A.setShort(ima.set(i, j, k, l), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT);
							}
						}
					}
				}
			}
		}
		return A;
	}

	
	/**
	 * Takes an ArrayList of float values and returns a multidimensional Int
	 * array (from 1D to 4D depending on the dimensions parameter).
	 * 
	 * @param data
	 *            the information to transform
	 * @param dimensions
	 *            variable that defines the dimensions we are needing.
	 * @return multidimensional Int array from 1D to 4D
	 */
	public static Array transformCollectionInMultidimensionalIntArray(List<Float> data, List<Dimension> dimensions, int scale) {
		ArrayInt A = null;
		if (dimensions.size() == 0) {
			//nada
			A = new ArrayInt.D0(false);
		} else if (dimensions.size() == 1) {
			Dimension dimid_1 = dimensions.get(0);
			A = new ArrayInt.D1(dimid_1.getLength(),false);
			Index ima = A.getIndex();
			// 1D not tested
			for (int i = 0; i < dimid_1.getLength(); i++) {
				if (i < data.size())
					A.setInt(ima.set(i), CodarUtils.floatToInt((float) (data.get(i) * scale)));
				else {
					A.setInt(ima.set(i), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_INT);
				}
			}
		} else if (dimensions.size() == 2) {
			Dimension dimid_1 = dimensions.get(0);
			Dimension dimid_2 = dimensions.get(1);

			A = new ArrayInt.D2(dimid_1.getLength(), dimid_2.getLength(),false);
			Index ima = A.getIndex();
			// 2D not tested
			for (int i = 0; i < dimid_1.getLength(); i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					if (j + i * dimid_2.getLength() < data.size())
						A.setInt(ima.set(i, j),
								CodarUtils.floatToInt((float) (data.get(j + i * dimid_2.getLength()) * scale)));
					else {
						A.setInt(ima.set(i, j), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_INT);
					}
				}
			}
		} else if (dimensions.size() == 3) {

			Dimension dimid_1 = dimensions.get(0);
			Dimension dimid_2 = dimensions.get(1);
			Dimension dimid_3 = dimensions.get(2);

			A = new ArrayInt.D3(dimid_1.getLength(), dimid_2.getLength(), dimid_3.getLength(),false);
			Index ima = A.getIndex();
			for (int i = 0; i < dimid_1.getLength(); i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					for (int k = 0; k < dimid_3.getLength(); k++) {
						if (k + j * dimid_3.getLength() + i * dimid_3.getLength() * dimid_2.getLength() < data.size())
							A.setInt(ima.set(i, j, k), CodarUtils.floatToInt((float) data
									.get(k + j * dimid_3.getLength() + i * dimid_3.getLength() * dimid_2.getLength()) * scale));
						else {
							A.setInt(ima.set(i, j, k), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_INT);
						}
					}
				}
			}
		} else if (dimensions.size() == 4) {
			int dimid_1 = dimensions.get(0).getLength() == 0 ? 1 : dimensions.get(0).getLength();
			Dimension dimid_2 = dimensions.get(1);
			Dimension dimid_3 = dimensions.get(2);
			Dimension dimid_4 = dimensions.get(3);

			A = new ArrayInt.D4(dimid_1, dimid_2.getLength(), dimid_3.getLength(), dimid_4.getLength(),false);
			Index ima = A.getIndex();
			for (int i = 0; i < dimid_1; i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					for (int k = 0; k < dimid_3.getLength(); k++) {
						for (int l = 0; l < dimid_4.getLength(); l++) {
							if (l + k * dimid_4.getLength() + j * dimid_4.getLength() * dimid_3.getLength()
									+ i * dimid_4.getLength() * dimid_3.getLength() * dimid_2.getLength() < data.size())
								A.setInt(ima.set(i, j, k, l), CodarUtils.floatToInt((float) data.get(l
										+ k * dimid_4.getLength() + j * dimid_4.getLength() * dimid_3.getLength()
										+ i * dimid_4.getLength() * dimid_3.getLength() * dimid_2.getLength()) * scale));
							else {
								A.setInt(ima.set(i, j, k, l), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_INT);
							}
						}
					}
				}
			}
		}
		return A;
	}

	/**
	 * Takes an ArrayList of float values and returns a multidimensional Byte
	 * array (from 1D to 4D depending on the dimensions parameter).
	 * 
	 * @param data
	 *            the information to transform
	 * @param dimensions
	 *            variable that defines the dimensions we are needing.
	 * @return multidimensional Byte array from 1D to 4D
	 */
	public static Array transformCollectionInMultidimensionalByteArray(List<Float> data, List<Dimension> dimensions) {
		ArrayByte A = null;
		if (dimensions.size() == 0) {
			//nada 
			A = new ArrayByte.D0(false);
		} else if (dimensions.size() == 1) {
			Dimension dimid_1 = dimensions.get(0);
			A = new ArrayByte.D1(dimid_1.getLength(),false);
			Index ima = A.getIndex();
			// 1D not tested
			for (int i = 0; i < dimid_1.getLength(); i++) {
				if (i < data.size())
					A.setByte(ima.set(i), CodarUtils.floatToByte((float) data.get(i)));
				else {
					A.setByte(ima.set(i), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE);
				}
			}
		} else if (dimensions.size() == 2) {
			Dimension dimid_1 = dimensions.get(0);
			Dimension dimid_2 = dimensions.get(1);

			A = new ArrayByte.D2(dimid_1.getLength(), dimid_2.getLength(),false);
			Index ima = A.getIndex();
			// 2D not tested
			for (int i = 0; i < dimid_1.getLength(); i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					if (j + i * dimid_2.getLength() < data.size())
						A.setByte(ima.set(i, j), CodarUtils.floatToByte((float) data.get(j + i * dimid_2.getLength())));
					else {
						A.setByte(ima.set(i, j), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE);
					}
				}
			}
		} else if (dimensions.size() == 3) {

			Dimension dimid_1 = dimensions.get(0);
			Dimension dimid_2 = dimensions.get(1);
			Dimension dimid_3 = dimensions.get(2);

			A = new ArrayByte.D3(dimid_1.getLength(), dimid_2.getLength(), dimid_3.getLength(),false);
			Index ima = A.getIndex();
			for (int i = 0; i < dimid_1.getLength(); i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					for (int k = 0; k < dimid_3.getLength(); k++) {
						if (k + j * dimid_3.getLength() + i * dimid_3.getLength() * dimid_2.getLength() < data.size())
							A.setByte(ima.set(i, j, k), CodarUtils.floatToByte((float) data
									.get(k + j * dimid_3.getLength() + i * dimid_3.getLength() * dimid_2.getLength())));
						else {
							A.setByte(ima.set(i, j, k), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE);
						}
					}
				}
			}
		} else if (dimensions.size() == 4) {
			int dimid_1 = dimensions.get(0).getLength() == 0 ? 1 : dimensions.get(0).getLength();
			Dimension dimid_2 = dimensions.get(1);
			Dimension dimid_3 = dimensions.get(2);
			Dimension dimid_4 = dimensions.get(3);

			A = new ArrayByte.D4(dimid_1, dimid_2.getLength(), dimid_3.getLength(), dimid_4.getLength(),false);
			Index ima = A.getIndex();
			for (int i = 0; i < dimid_1; i++) {
				for (int j = 0; j < dimid_2.getLength(); j++) {
					for (int k = 0; k < dimid_3.getLength(); k++) {
						for (int l = 0; l < dimid_4.getLength(); l++) {
							if (l + k * dimid_4.getLength() + j * dimid_4.getLength() * dimid_3.getLength()
									+ i * dimid_4.getLength() * dimid_3.getLength() * dimid_2.getLength() < data.size())
								A.setByte(ima.set(i, j, k, l), CodarUtils.floatToByte((float) data.get(l
										+ k * dimid_4.getLength() + j * dimid_4.getLength() * dimid_3.getLength()
										+ i * dimid_4.getLength() * dimid_3.getLength() * dimid_2.getLength())));
							else {
								A.setByte(ima.set(i, j, k, l), ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE);
							}
						}
					}
				}
			}
		}
		return A;
	}

	/**
	 * Simplified translation of the km2deg matlab script
	 * http://es.mathworks.com/help/map/ref/km2deg.html
	 * 
	 * @param km
	 * @return deg
	 */
	public static double km2deg(double km) {
		double deg = 0;
		double earthRad = 6.3710e+03;
		deg = (km / earthRad) * 180 / Math.PI;
		return deg;
	}

	/**
	 * Converts Float a short
	 * 
	 * @param number
	 *            float sized number
	 * @return shortnumber short sized number
	 */
	private static short floatToShort(float x) {
		short shortNumber = 0;
		if (Float.isNaN(x)) {
			shortNumber = ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT;
		} else if (x < Short.MIN_VALUE) {
			shortNumber = Short.MIN_VALUE;
		} else if (x > Short.MAX_VALUE) {
			shortNumber = Short.MAX_VALUE;
		} else {
			shortNumber = (short) Math.round(x);
		}
		return shortNumber;
	}

	/**
	 * Converts Float a integer
	 * 
	 * @param number
	 *            float sized number
	 * @return intnumber int sized number
	 */
	private static int floatToInt(float x) {
		int intNumber = 0;
		if (Float.isNaN(x)) {
			intNumber = ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_INT;
		} else if (x < Integer.MIN_VALUE) {
			intNumber = Integer.MIN_VALUE;
		} else if (x > Integer.MAX_VALUE) {
			intNumber = Integer.MAX_VALUE;
		} else {
			intNumber = Math.round(x);
		}
		return intNumber;
	}

	/**
	 * Converts Float to byte. If the value is over the maximun, it is set to
	 * the maximun. If the value is below the minimu, it is set to the minimun.
	 * 
	 * @param number
	 *            float sized number
	 * @return bytenumber byte sized number
	 */
	private static byte floatToByte(float x) {
		byte byteNumber = 0;
		if (Float.isNaN(x)) {
			byteNumber = ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE;
		} else if (x < Byte.MIN_VALUE) {
			byteNumber = Byte.MIN_VALUE;
		} else if (x > Byte.MAX_VALUE) {
			byteNumber = Byte.MAX_VALUE;
		} else {
			byteNumber = (byte) Math.round(x);
		}
		return byteNumber;
	}

	/**
	 * Takes an empty latitude - longitude array and completes the gaps using a
	 * Geodesic WGS84 projection based on the X and Y values.
	 * 
	 * TODO: check if it is valid for regular grid. It is used to create the
	 * totals lat and lon dimensions.
	 * 
	 * @param siteLat
	 *            latitude of the origin, where x and y are equal to 0
	 * @param siteLon
	 *            longitude of the origin, where x and y are equal to 0
	 * @param latd
	 *            array we want to fill with the latitude values.
	 * @param lond
	 *            array we want to fill with the longitude values.
	 * @param x
	 *            values in km to translate to latitude longitude in the x axis.
	 * @param y
	 *            values in km to translate to latitude longitude in the y axis.
	 */
	public static void fillLatLonValuesTotals(float siteLat, float siteLon, List<Float> latd, List<Float> lond,
			List<Float> x, List<Float> y) {
		int i = 0;
		for (float xInc : x) {
			GeodesicData data = Geodesic.WGS84.Direct(siteLat, siteLon, 90, xInc * 1000);
			lond.add(i, new Float(data.lon2));
			i++;
		}
		i = 0;
		for (float yInc : y) {
			GeodesicData data = Geodesic.WGS84.Direct(siteLat, siteLon, 0, yInc * 1000);
			latd.add(i, new Float(data.lat2));
			i++;
		}
	}

	/**
	 * Takes an empty latitude - longitude array and completes the gaps using a
	 * Geodesic WGS84 projection based on the range and bearing values.
	 * 
	 * @param siteLat
	 *            latitude of the origin, where range and bearing are equal to 0
	 * @param siteLon
	 *            longitude of the origin, where range and bearing are equal to
	 *            0
	 * @param latd
	 *            array we want to fill with the latitude values.
	 * @param lond
	 *            array we want to fill with the longitude values.
	 * @param rangeDims
	 *            range in km values to translate to latitude longitude.
	 * @param bearingDims
	 *            bearing values in degrees to translate to latitude longitude.
	 */
	public static void fillLatLonValues(float siteLat, float siteLon, List<Float> latd, List<Float> lond,
			List<Float> rangeDims, List<Integer> bearingDims) {
		int i = 0;
		for (float bear : bearingDims) {
			for (float range : rangeDims) {
				GeodesicData data = Geodesic.WGS84.Direct(siteLat, siteLon, bear, range * 1000);
				latd.set(i, new Float(data.lat2));
				lond.set(i, new Float(data.lon2));
				i++;
			}
		}

	}

	/**
	 * This method locates the index values of the data stored in the bean table
	 * to correctly put the values in the final netcdf Radial file. It reads the
	 * dimensions that define the NetCDF grid in radials (bearing and range),
	 * and using them, calculate all the elements that will be needed in the
	 * netCDF file. Once the elements needed are defined, it locates the index
	 * positions of the elements in the codar data table and match each table
	 * value to a position in the final netCDF grid. If the element in the
	 * netCDF is not present in the table, a -1 index value is stored in the
	 * output ArrayList as index.
	 * 
	 * @param bean
	 *            CodarRadialBean containin the metadata to calculate the grid
	 *            for the output NetCDF
	 * @param table
	 *            table containing the data.
	 * @param profile
	 *            a profile is used just in case some mandatory bean data are
	 *            empty.
	 * @return an ArrayList of index values that represent the position of each
	 *         value within the table inside the netCDF
	 */
	public static List<Integer> getRadialIndexArray(CodarRadialBean bean, CodarDataTableBean table,
			CodarRadialBean profile) {
		List<Integer> bearingDims = new ArrayList<Integer>();

		float minBear = Float.isNaN(bean.getMinimunBearingAsFloat()) ? profile.getMinimunBearingAsFloat()
				: bean.getMinimunBearingAsFloat();
		float maxBear = Float.isNaN(bean.getMaximunBearingAsFloat()) ? profile.getMaximunBearingAsFloat()
				: bean.getMaximunBearingAsFloat();
		int angRes = (bean.getAngularResolutionInteger() < 0) ? profile.getAngularResolutionInteger()
				: bean.getAngularResolutionInteger();
		float antBear = Float.isNaN(bean.getAntennaBearingAsFloat()) ? profile.getAntennaBearingAsFloat()
				: bean.getAntennaBearingAsFloat();
		float minRang = Float.isNaN(bean.getMinimunRangeAsFloat()) ? profile.getMinimunRangeAsFloat()
				: bean.getMinimunRangeAsFloat();
		float maxRang = Float.isNaN(bean.getMaximunRangeAsFloat()) ? profile.getMaximunRangeAsFloat()
				: bean.getMaximunRangeAsFloat();
		float rangResKm = Float.isNaN(bean.getRangeResolutionKMetersFloat()) ? profile.getRangeResolutionKMetersFloat()
				: bean.getRangeResolutionKMetersFloat();

		int minBearing = ((int) (minBear / angRes)) * angRes + (int) (antBear % angRes);
		int maxBearing = ((int) (maxBear / angRes)) * angRes;

		for (int i = minBearing; i <= maxBearing; i = i + angRes) {
			bearingDims.add(i);
		}

		List<Float> rangeDims = new ArrayList<Float>();
		float minRange = ((int) (minRang / rangResKm)) * rangResKm;
		float maxRange = ((int) (maxRang / rangResKm)) * rangResKm;

		for (float i = minRange; i <= maxRange; i = (float) (Math.rint((i + rangResKm) * 10000) / 10000)) {
			rangeDims.add(i);
		}

		List<Integer> codarToNetcdfIndex = table.getRadialTableIndexInNetCdf(bearingDims, rangeDims);
		return codarToNetcdfIndex;
	}

	/**
	 * This method locates the index values of the data stored in the bean table
	 * to correctly put the values in the final netcdf Total file. It reads the
	 * dimensions that define the NetCDF grid in totals (x and y), and using
	 * them, calculate all the elements that will be needed in the netCDF file.
	 * X and Y are used because we have no latitude and longitude in the
	 * original codar files, but at the end, latitude and longitude are used as
	 * final dimensions. Once the elements needed are defined, it locates the
	 * index positions of the elements in the codar data table and match each
	 * table value to a position in the final netCDF grid. If the element in the
	 * netCDF is not present in the table, a -1 index value is stored in the
	 * output ArrayList as index.
	 * 
	 * @param bean
	 *            CodarTotalBean containin the metadata to calculate the grid
	 *            for the output NetCDF
	 * @param table
	 *            table containing the data.
	 * @param profile
	 *            a profile is used just in case some mandatory bean data are
	 *            empty.
	 * @return an ArrayList of index values that represent the position of each
	 *         value within the table inside the netCDF
	 */
	public static List<Integer> getTotalIndexArray(CodarTotalBean bean, CodarDataTableBean table,
			CodarTotalBean profile) {

		List<Float> xAxisDims = new ArrayList<Float>();
		List<Float> yAxisDims = new ArrayList<Float>();
		
		//pasamos de lat long a x e y para calcular las dimensiones en km - distancia
		float siteLat = bean.getOriginElementAsFloat(0);
		float siteLon = bean.getOriginElementAsFloat(1);

		float lonMin = Float.isNaN(bean.getxMinAsFloat()) ? profile.getxMinAsFloat() : bean.getxMinAsFloat();
		float lonMax = Float.isNaN(bean.getxMaxAsFloat()) ? profile.getxMaxAsFloat() : bean.getxMaxAsFloat();
		float latMin = Float.isNaN(bean.getyMinAsFloat()) ? profile.getyMinAsFloat() : bean.getyMinAsFloat();
		float latMax = Float.isNaN(bean.getyMaxAsFloat()) ? profile.getyMaxAsFloat() : bean.getyMaxAsFloat();
		
		double minX = (Lldistkm.calculate(siteLat, siteLon, siteLat, lonMin))[0];
		if (lonMin < siteLon) minX = minX*-1;
		double minY = (Lldistkm.calculate(siteLat, siteLon, latMin, siteLon))[0];
		if (latMin < siteLat) minY = minY*-1;
		double maxX = (Lldistkm.calculate(siteLat, siteLon, siteLat, lonMax))[0];
		if (lonMax < siteLon) maxX = maxX*-1;
		double maxY = (Lldistkm.calculate(siteLat, siteLon, latMax, siteLon))[0];
		if (latMax < siteLat) maxY = maxY*-1;
		
		float gridSpacing = Float.isNaN(bean.getGridSpacingAsFloat()) ? profile.getGridSpacingAsFloat()
				: bean.getGridSpacingAsFloat();

		float xMax = ((int) (maxX / gridSpacing)) * gridSpacing;
		float xMin = ((int) (minX / gridSpacing)) * gridSpacing;
		float yMax = ((int) (maxY / gridSpacing)) * gridSpacing;
		float yMin = ((int) (minY / gridSpacing)) * gridSpacing;

		for (float i = xMin; i <= xMax; i = i + gridSpacing) {
			xAxisDims.add(i);
		}

		for (float i = yMin; i <= yMax; i = i + gridSpacing) {
			yAxisDims.add(i);
		}

		List<Integer> codarToNetcdfIndex = table.getTotalTableIndexInNetCdf(xAxisDims, yAxisDims);
		return codarToNetcdfIndex;
	}
}
