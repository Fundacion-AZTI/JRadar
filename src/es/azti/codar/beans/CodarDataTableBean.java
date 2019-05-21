/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  18 de may. de 2016
 */
package es.azti.codar.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.azti.utils.TableColumnNames;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es) 18 de may. de 2016
 *
 *         Bean used to store the data of codar file tables and their
 *         transformations. Includes an utility to read just a selection and not
 *         all the table.
 */
public class CodarDataTableBean {

	private String type;
	private int columns;
	private int rows;
	private String[] columnTypes;
	private Float[][] data;

	/**
	 * Checi if the table is empty or not checking the number of rows.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return (this.getColumns() == 0 && this.getRows() == 0);
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the columns
	 */
	public int getColumns() {
		return columns;
	}

	/**
	 * @param columns
	 *            the columns to set
	 */
	public void setColumns(int columns) {
		this.columns = columns;
	}

	/**
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * @param rows
	 *            the rows to set
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * @return the columnTypes
	 */
	public String[] getColumnTypes() {
		return columnTypes;
	}

	/**
	 * returns the columns names as a Java List<String>
	 * 
	 * @return
	 */
	public List<String> getColumnTypesAsList() {
		return Arrays.asList(columnTypes);
	}

	/**
	 * @return the columnTypes
	 */
	public String getColumnTypesAsString() {
		StringBuffer sb = new StringBuffer();
		for (String elem : columnTypes) {
			sb.append(elem + " ");
		}
		return sb.toString();
	}

	/**
	 * @param columnTypes
	 *            the columnTypes to set
	 */
	public void setColumnTypes(String[] columnTypes) {
		this.columnTypes = columnTypes;
	}

	/**
	 * @return the data
	 */
	public Float[][] getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(Float[][] data) {
		this.data = data;
	}

	/**
	 * Mapping between codar radial data table and netcdf data table
	 * 
	 * @param bearingData
	 *            the bearing data array used as netcdf index
	 * @param rangeData
	 *            the range data array used as netcdf index
	 * @return the mapping of the codar table. The returned array, is a netcdf
	 *         sized array, and each element, is stored de index of the codar
	 *         table from witch we need to read the data to fill the elemente of
	 *         the netcdf. for example: in a 2x2 netcdf array: [1,3,2,5] means,
	 *         that the netcdf, must be filled with the elementes of the rows
	 *         1,3,2,5 from codar text file.
	 */
	public List<Integer> getRadialTableIndexInNetCdf(List<Integer> bearingData, List<Float> rangeData) {

		List<Integer> returnTable = new ArrayList<Integer>(bearingData.size() * rangeData.size());
		List<Float> bearingCodarData = getColumnElements(TableColumnNames.BEAR);
		List<Float> rangeCodarData = getColumnElements(TableColumnNames.RNGE);
		for (int indexb = 0; indexb < bearingData.size(); indexb++) {
			for (int indexr = 0; indexr < rangeData.size(); indexr++) {
				// read values
				double range = rangeData.get(indexr);
				int bearing = bearingData.get(indexb);
				// search for the correct index
				int position = -1;
				for (int innerIndex = 0; innerIndex < bearingCodarData.size(); innerIndex++) {
					if (range == rangeCodarData.get(innerIndex)
							&& bearing == ((int) bearingCodarData.get(innerIndex).doubleValue())) {
						position = innerIndex;
						break;
					}
				}
				if (position != -1) {
					// For debugging purpouses
					// System.out.println("found range: "+ range + " not found
					// bearing: " + bearing + "pos b: " + indexb + " pos r: "+
					// indexr + "mascara: " + ((indexb*rangeData.size() +
					// indexr) + " : pos: " + position));
				}
				returnTable.add(indexb * rangeData.size() + indexr, new Integer(position));
			}
		}
		return returnTable;
	}

	/**
	 * Mapping between codar total data table and netcdf data table
	 * 
	 * @param xData
	 *            the x axis data array used as netcdf index
	 * @param yData
	 *            the y axis data array used as netcdf index
	 * @return the mapping of the codar total table. The returned array, is a
	 *         netcdf sized array, and each element, is stored de index of the
	 *         codar table from witch we need to read the data to fill the
	 *         elemente of the netcdf. for example: in a 2x2 netcdf array:
	 *         [1,3,2,5] means, that the netcdf, must be filled with the
	 *         elementes of the rows 1,3,2,5 from codar text file.
	 */
	public List<Integer> getTotalTableIndexInNetCdf(List<Float> xData, List<Float> yData) {
		List<Integer> returnTable = new ArrayList<Integer>(xData.size() * yData.size()) ;
			for(int i=0;i<xData.size()*yData.size();i++){
				returnTable.add(new Integer(-1));
			}
		
		List<Float> xCodarData = getColumnElements(TableColumnNames.XDST);
		List<Float> yCodarData = getColumnElements(TableColumnNames.YDST);
		for (int indexb = 0; indexb < xData.size(); indexb++) {
			for (int indexr = 0; indexr < yData.size(); indexr++) {
				// read values
				float y = yData.get(indexr);
				float x = xData.get(indexb);
				// search for codar index
				int position = -1;
				for (int innerIndex = 0; innerIndex < xCodarData.size(); innerIndex++) {
					if (y == yCodarData.get(innerIndex) && x == (xCodarData.get(innerIndex))) {
						position = innerIndex;
						break;
					}
				}
				if (position != -1) {
					// For debugging purpouses
//					 System.out.println("found y: "+ y + " not found x: " + x
//					 + "pos x: " + indexb + " pos y: "+ indexr + "mascara: " +
//					 ((indexb*yData.size() + indexr) + " : pos: " +
//					 position));
				}
				//returnTable.add(indexb * yData.size() + indexr, new Integer(position));
				returnTable.remove(indexr * xData.size() + indexb);
				returnTable.add(indexr * xData.size() + indexb, new Integer(position));
			}
		}

		return returnTable;
	}

	/**
	 * reads the information in the table and get an orderer matrix related to
	 * the index information
	 * 
	 * @param col:
	 *            column you want to request the info from
	 * @param codarToNetcdfIndex:
	 *            index position of each data related to the dimensions you want
	 *            to use to retrieve the data.
	 * @return ArrayList of the data in float format.
	 */
	public List<Float> getColumnElementsInOrder(Enum<TableColumnNames> col, List<Integer> codarToNetcdfIndex) {
		int index = this.getColumnTypesAsList().indexOf(col.toString());
		List<Float> datosColumna = new ArrayList<Float>();
		if (index >= 0) {
			for (int i = 0; i < codarToNetcdfIndex.size(); i++) {
				if (codarToNetcdfIndex.get(i) != -1) {
					datosColumna.add(this.data[codarToNetcdfIndex.get(i)][index]);
				} else {
					// if the info requested is out of bounds, we put a NaN
					// value for Float data format.
					datosColumna.add(Float.NaN);
				}
			}
		}
		return datosColumna;
	}

	/**
	 * Request information of a column as it is. No care about dimensions and
	 * position.
	 * 
	 * @return data from a column in a Float ArrayList.
	 */
	public List<Float> getColumnElements(Enum<TableColumnNames> col) {
		List<Float> datosColumna = new ArrayList<Float>();
		int index = this.getColumnTypesAsList().indexOf(col.toString());
		for (int i = 0; i < this.data.length; i++) {
			datosColumna.add(this.data[i][index]);
		}
		return datosColumna;
	}

}
