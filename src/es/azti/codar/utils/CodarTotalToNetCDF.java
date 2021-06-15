/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  19 abril de 2017
 */
package es.azti.codar.utils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import es.azti.codar.beans.CodarTotalBean;
import es.azti.netcdf.ui.VentanaRunTotalQualityTests;
import es.azti.netcdf.ui.VentanaSaveFichero;
import es.azti.utils.Lldistkm;
import es.azti.utils.TableColumnNames;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingStrategy;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es) 19 abril. de 2017
 *
 *         Codar total to NetCDF transformer
 */
public class CodarTotalToNetCDF {

	private static final byte MAX_SITE = 50;
	private static final int REF_MAX = 1;
	private static final int SDN_TIME_QC_FLAG = 1;
	private static final int SDN_DEPTH_QC_FLAG = 1;

	CodarTotalBean bean;

	public CodarTotalToNetCDF(CodarTotalBean bean) {
		this.bean = bean;
	}

	public String getOutputFileName() {
		JFileChooser fileChooser = new JFileChooser();
		VentanaSaveFichero vs = new VentanaSaveFichero();
		FileNameExtensionFilter filterFileName = new FileNameExtensionFilter("netcdf", "nc");
		fileChooser.setFileFilter(filterFileName);
		fileChooser.setCurrentDirectory(new File("."));

		File saveFile = null;
		if (fileChooser.showSaveDialog(vs) == JFileChooser.APPROVE_OPTION) {
			saveFile = fileChooser.getSelectedFile();
		}
		return saveFile.getAbsolutePath().endsWith(".nc") ? saveFile.getAbsolutePath()
				: saveFile.getAbsolutePath() + ".nc";
	}

	public int saveProfile(CodarTotalBean profile) {
		// errores:
		// code = 0: ok
		// code = 1: mandatory field is missing or has an error.
		// code = 2: not mandatory field error (we can save file)
		// code = 3: radial data Table is missing
		// code = 4: Quality tests can't be run due to a missing values.
		// code = 5: data from ddbb not found and needed.
		// code = 6: Missing data but could be completed using profile

		// check mandatory values:
		int errorCode = 0;
		if (checkBeanMandatoryFields() && profile == null) {
			errorCode = 1;
		} else if (checkBeanTotalTable()) {
			errorCode = 3;
		} else if (checkQCQA() && profile == null) {
			errorCode = 4;
		} else if (profile != null) {
			errorCode = 6;
		} else {
			// save profile

		}
		return errorCode;
	}

	public int toNetCDF4(CodarTotalBean profile, String outputFileName) {
		// errores:
		// code = 0: ok
		// code = 1: mandatory field is missing or has an error.
		// code = 2: not mandatory field error (we can save file)
		// code = 3: total data Table is missing
		// code = 4: Quality tests can't be run due to a missing values.
		// code = 5: data from ddbb not found and needed.
		// code = 6: parameters missing but profile used instead.

		// check mandatory values:
		int errorCode = 0;
		if (checkBeanMandatoryFields()) {
			errorCode = 1;
		} else if (checkQCQA()) {
			errorCode = 4;
		} else if (bean.getNetworkBean() == null) {
			errorCode = 5;
		} else if (checkBeanTotalTable()) {
			errorCode = 3;
		}

		boolean fixed = false;
		switch (errorCode) {
		case 1:
		case 4:
		case 5:
			// warning, profile info used to complete mandatory information
			if (profile != null) {
				fixed = true;
				bean.fixMissingValues(profile);
			} else {
				break;
			}
		case 0:
			// save file
			NetcdfFileWriter dataFile = null;
			try {
				// TODO, check lat lon and the regular grid with value
				// interpolation.
				List<Float> xAxisDims = new ArrayList<Float>();
				List<Float> yAxisDims = new ArrayList<Float>();

				// pasamos de lat long a x e y para calcular las dimensiones en km - distancia
				float siteLat = bean.getOriginElementAsFloat(0);
				float siteLon = bean.getOriginElementAsFloat(1);

				float lonMin = bean.getxMinAsFloat();
				float lonMax = bean.getxMaxAsFloat();
				float latMin = bean.getyMinAsFloat();
				float latMax = bean.getyMaxAsFloat();

				double minimunX = (Lldistkm.calculate(siteLat, siteLon, siteLat, lonMin))[0];
				if (lonMin < siteLon)
					minimunX = minimunX * -1;
				double minimunY = (Lldistkm.calculate(siteLat, siteLon, latMin, siteLon))[0];
				if (latMin < siteLat)
					minimunY = minimunY * -1;
				double maximunX = (Lldistkm.calculate(siteLat, siteLon, siteLat, lonMax))[0];
				if (lonMax < siteLon)
					maximunX = maximunX * -1;
				double maximunY = (Lldistkm.calculate(siteLat, siteLon, latMax, siteLon))[0];
				if (latMax < siteLat)
					maximunY = maximunY * -1;

				float xMax = ((int) (maximunX / bean.getGridSpacingAsFloat())) * bean.getGridSpacingAsFloat();
				float xMin = ((int) (minimunX / bean.getGridSpacingAsFloat())) * bean.getGridSpacingAsFloat();
				float yMax = ((int) (maximunY / bean.getGridSpacingAsFloat())) * bean.getGridSpacingAsFloat();
				float yMin = ((int) (minimunY / bean.getGridSpacingAsFloat())) * bean.getGridSpacingAsFloat();

				for (float i = xMin; i <= xMax; i = i + bean.getGridSpacingAsFloat()) {
					xAxisDims.add(i);
				}

				for (float i = yMin; i <= yMax; i = i + bean.getGridSpacingAsFloat()) {
					yAxisDims.add(i);
				}

				// TODO: This must be improved the values are aproximated
				List<Integer> codarToNetcdfIndex = bean.getTable().getTotalTableIndexInNetCdf(xAxisDims, yAxisDims);

				List<Float> lonCalc = new ArrayList<Float>(xAxisDims.size());
				List<Float> latCalc = new ArrayList<Float>(yAxisDims.size());
				CodarUtils.fillLatLonValuesTotals(siteLat, siteLon, latCalc, lonCalc, xAxisDims, yAxisDims);

				// time coverage data

				// Current Speed - flip sign so positive velocity is away from
				// radar
				// according to CF standard name
				// 'total_sea_water_velocity_away_from_instrument'.
				// CODAR reports positive speeds as toward the radar.
				// transform cm to m
				List<Float> veloURead = bean.getTable().getColumnElementsInOrder(TableColumnNames.VELU,
						codarToNetcdfIndex);
				List<Float> veloVRead = bean.getTable().getColumnElementsInOrder(TableColumnNames.VELV,
						codarToNetcdfIndex);
				List<Float> velo = new ArrayList<Float>(veloURead.size());
				List<Float> velu = new ArrayList<Float>(veloURead.size());
				List<Float> velv = new ArrayList<Float>(veloVRead.size());
				for (Float velTemp : veloURead) {
					velu.add(new Float(velTemp / 100));
				}
				for (Float velTemp : veloVRead) {
					velv.add(new Float(velTemp / 100));
				}
				for (int i = 0; i < veloVRead.size(); i++) {
					velo.add(new Float(Math.sqrt(Math.pow(veloURead.get(i), 2) + Math.pow(veloVRead.get(i), 2))));
				}

				String site_code = bean.getNetworkBean().getNetwork_id();
				String platform_code = site_code + "-Total";
				String dataID = site_code + "-Total_" + bean.getTimeStampAsUTC();
				String TDS_catalog = bean.getNetworkBean().getMetadata_page();
				String xlink = "<sdn_reference xlink:href=\"" + TDS_catalog + "\" xlink:role=\"\" xlink:type=\"URL\"/>";

				String fileName = outputFileName;
				if (fileName == null)
					fileName = this.getOutputFileName();

				Nc4Chunking chunker = Nc4ChunkingStrategy.factory(Nc4Chunking.Strategy.standard, 6, true);
				dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileName, chunker);

				Dimension dimid_t = dataFile.addDimension(null, "TIME", 1);
				Dimension dimid_lat = dataFile.addDimension(null, "LATITUDE", latCalc.size());
				Dimension dimid_lon = dataFile.addDimension(null, "LONGITUDE", lonCalc.size());

				Dimension dimid_depth = dataFile.addDimension(null, "DEPTH", 1);
				Dimension dimid_refmax = dataFile.addDimension(null, "REFMAX", CodarTotalToNetCDF.REF_MAX);
				Dimension dimid_maxsite = dataFile.addDimension(null, "MAXSITE", CodarTotalToNetCDF.MAX_SITE);
				Dimension dimid_maxinst = dataFile.addDimension(null, "MAXINST", CodarTotalToNetCDF.MAX_SITE);
				Dimension dimid_string50 = dataFile.addDimension(null, "STRING50", 50);
				Dimension dimid_string250 = dataFile.addDimension(null, "STRING" + xlink.length(), xlink.length());
				Dimension dimid_string15 = dataFile.addDimension(null, "STRING15", 15);

				List<Dimension> dimsTDYX = new ArrayList<Dimension>();
				List<Dimension> dimsT50 = new ArrayList<Dimension>();
				List<Dimension> dimsT250 = new ArrayList<Dimension>();
				List<Dimension> dimsTRefMax250 = new ArrayList<Dimension>();
				List<Dimension> dimsTM = new ArrayList<Dimension>();
				List<Dimension> dimsTMI = new ArrayList<Dimension>();
				List<Dimension> dimsTMS15 = new ArrayList<Dimension>();

				dimsTDYX.add(dimid_t);
				dimsTDYX.add(dimid_depth);
				dimsTDYX.add(dimid_lat);
				dimsTDYX.add(dimid_lon);

				dimsT50.add(dimid_t);
				dimsT50.add(dimid_string50);

				dimsT250.add(dimid_t);
				dimsT250.add(dimid_string250);

				dimsTRefMax250.add(dimid_t);
				dimsTRefMax250.add(dimid_refmax);
				dimsTRefMax250.add(dimid_string250);

				dimsTM.add(dimid_t);
				dimsTM.add(dimid_maxsite);

				dimsTMI.add(dimid_t);
				dimsTMI.add(dimid_maxinst);

				dimsTMS15.add(dimid_t);
				dimsTMS15.add(dimid_maxsite);
				dimsTMS15.add(dimid_string15);

				Variable varid_t = dataFile.addVariable(null, "TIME", DataType.DOUBLE, "TIME");
				varid_t.addAttribute(new Attribute("long_name", "Time"));
				varid_t.addAttribute(new Attribute("standard_name", "time"));
				varid_t.addAttribute(new Attribute("units", "days since 1950-01-01T00:00:00Z"));
				varid_t.addAttribute(new Attribute("valid_min", "-90000"));
				varid_t.addAttribute(new Attribute("valid_max", "90000"));
				varid_t.addAttribute(new Attribute("calendar", "standard"));
				varid_t.addAttribute(new Attribute("axis", "T"));
				varid_t.addAttribute(new Attribute("sdn_parameter_name", "Elapsed time (since 1950-01-01T00:00:00Z)"));
				varid_t.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ELTJLD01"));
				varid_t.addAttribute(new Attribute("sdn_uom_name", "Days"));
				varid_t.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UTAA"));
				varid_t.addAttribute(new Attribute("ancillary_variables", "TIME_QC"));

				// Latitude
				Variable varid_lat = dataFile.addVariable(null, "LATITUDE", DataType.FLOAT, "LATITUDE");
				varid_lat.addAttribute(new Attribute("long_name", "Latitude of each location"));
				varid_lat.addAttribute(new Attribute("standard_name", "latitude"));
				varid_lat.addAttribute(new Attribute("axis", "Y"));
				varid_lat.addAttribute(new Attribute("units", "degree_north"));
				varid_lat.addAttribute(new Attribute("valid_min", "-90"));
				varid_lat.addAttribute(new Attribute("valid_max", "90"));
				varid_lat.addAttribute(new Attribute("grid_mapping", "crs"));
				varid_lat.addAttribute(new Attribute("sdn_parameter_name", "Latitude north"));
				varid_lat.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALATZZ01"));
				varid_lat.addAttribute(new Attribute("sdn_uom_name", "Degrees north"));
				varid_lat.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGN"));
				varid_lat.addAttribute(new Attribute("ancillary_variables", "POSITION_QC"));

				// Longitude
				Variable varid_lon = dataFile.addVariable(null, "LONGITUDE", DataType.FLOAT, "LONGITUDE");
				varid_lon.addAttribute(new Attribute("long_name", "Longitude of each location"));
				varid_lon.addAttribute(new Attribute("standard_name", "longitude"));
				varid_lon.addAttribute(new Attribute("units", "degree_east"));
				varid_lon.addAttribute(new Attribute("valid_min", "-180"));
				varid_lon.addAttribute(new Attribute("valid_max", "180"));
				varid_lon.addAttribute(new Attribute("axis", "X"));
				varid_lon.addAttribute(new Attribute("grid_mapping", "crs"));
				varid_lon.addAttribute(new Attribute("sdn_parameter_name", "Longitude east"));
				varid_lon.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALONZZ01"));
				varid_lon.addAttribute(new Attribute("sdn_uom_name", "Degrees east"));
				varid_lon.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGE"));
				varid_lon.addAttribute(new Attribute("ancillary_variables", "POSITION_QC"));

				// crs
				Variable varid_crs = dataFile.addVariable(null, "crs", DataType.SHORT, new ArrayList<Dimension>(0));
				varid_crs.addAttribute(new Attribute("grid_mapping_name", "latitude_longitude"));
				varid_crs.addAttribute(new Attribute("epsg_code", "EPSG:4326"));
				varid_crs.addAttribute(new Attribute("semi_major_axis", 6378137.0f));
				varid_crs.addAttribute(new Attribute("inverse_flattening", 298.257223563));

				//// Add SDN namespace variables
				// To enforce homogeneity in the codes and interoperability with
				//// SDC, the site_code has to be set equal
				// to the EDIOS Series id of the HFR network and platform_code
				//// must include the EDIOS Platform id of the
				// HFR site, i.e.:
				// SDN_CRUISE=site_code=EDIOS-Series-id
				// SDN_STATION=platform_code=EDIOS-Series-id_Total (for total
				//// current data files)
				// SDN_STATION=platform_code=EDIOS-Series-id_ EDIOS-Platform-id
				//// (for radial current data files)

				// SDN_CRUISE
				Variable varid_sdncruise = dataFile.addVariable(null, "SDN_CRUISE", DataType.CHAR, dimsT50);
				varid_sdncruise.addAttribute(new Attribute("long_name", "Grid grouping label"));

				// SDN_STATION
				Variable varid_sdnstation = dataFile.addVariable(null, "SDN_STATION", DataType.CHAR, dimsT50);
				varid_sdnstation.addAttribute(new Attribute("long_name", "Grid label"));

				// SDN_LOCAL_CDI_ID
				Variable varid_sdnlocalcdiid = dataFile.addVariable(null, "SDN_LOCAL_CDI_ID", DataType.CHAR, dimsT50);
				varid_sdnlocalcdiid.addAttribute(new Attribute("long_name", "SeaDataCloud CDI identifier"));
				varid_sdnlocalcdiid.addAttribute(new Attribute("cf_role", "grid_id"));

				// SDN_EDMO_CODE
				Variable varid_sdnedmocode = dataFile.addVariable(null, "SDN_EDMO_CODE", DataType.SHORT, dimsTMI);
				varid_sdnedmocode.addAttribute(new Attribute("long_name",
						"European Directory of Marine Organisations code for the CDI partner"));
				varid_sdnedmocode.addAttribute(new Attribute("units", "1"));

				// SDN_REFERENCES
				Variable varid_sdnreferences = dataFile.addVariable(null, "SDN_REFERENCES", DataType.CHAR, dimsT250);
				varid_sdnreferences.addAttribute(new Attribute("long_name", "Usage metadata reference"));

				// SDN_XLINK
				Variable varid_sdnxlink = dataFile.addVariable(null, "SDN_XLINK", DataType.CHAR, dimsTRefMax250);
				varid_sdnxlink.addAttribute(new Attribute("long_name", "External resource linkages"));

				Variable varid_depth = dataFile.addVariable(null, "DEPH", DataType.FLOAT, "DEPTH");
				varid_depth.addAttribute(new Attribute("long_name", "Depth"));
				varid_depth.addAttribute(new Attribute("standard_name", "depth"));
				varid_depth.addAttribute(new Attribute("_FillValue", "9.96921E36"));
				varid_depth.addAttribute(new Attribute("units", "m"));
				varid_depth.addAttribute(new Attribute("valid_min", "-12000"));
				varid_depth.addAttribute(new Attribute("valid_max", "12000"));
				varid_depth.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_depth.addAttribute(new Attribute("axis", "Z"));
				varid_depth.addAttribute(new Attribute("positive", "down"));
				varid_depth.addAttribute(new Attribute("reference", "sea_level"));
				varid_depth.addAttribute(new Attribute("sdn_parameter_name", "Depth below surface of the water body"));
				varid_depth.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ADEPZZ01"));
				varid_depth.addAttribute(new Attribute("sdn_uom_name", "Metres"));
				varid_depth.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::ULAA"));
				varid_depth.addAttribute(new Attribute("ancillary_variables", "DEPH_QC"));

				// u
				Variable varid_u = dataFile.addVariable(null, "EWCT", DataType.SHORT, dimsTDYX);
				varid_u.addAttribute(new Attribute("long_name", "West-east current component"));
				varid_u.addAttribute(new Attribute("standard_name", "eastward_sea_water_velocity"));
				varid_u.addAttribute(new Attribute("units", "m s-1"));
				varid_u.addAttribute(new Attribute("valid_min", "-10000"));
				varid_u.addAttribute(new Attribute("valid_max", "10000"));
				varid_u.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_u.addAttribute(new Attribute("scale_factor", Arrays.asList(new Double(0.001))));
				varid_u.addAttribute(new Attribute("add_offset", Arrays.asList(new Double(0))));
				varid_u.addAttribute(new Attribute("ioos_category", "Currents"));
				varid_u.addAttribute(new Attribute("coordsys", "geographic"));
				varid_u.addAttribute(
						new Attribute("sdn_parameter_name", "Eastward current velocity in the water body"));
				varid_u.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::LCEWZZ01"));
				varid_u.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_u.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_u.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_u.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) -10), new Short((short) 10))));
				varid_u.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_u.addAttribute(
						new Attribute("ancillary_variables", "QCflag, VART_QC, CSPD_QC, DDNS_QC, GDOP_QC"));

				// v
				Variable varid_v = dataFile.addVariable(null, "NSCT", DataType.SHORT, dimsTDYX);
				varid_v.addAttribute(new Attribute("long_name", "South-north current component"));
				varid_v.addAttribute(new Attribute("standard_name", "northward_sea_water_velocity"));
				varid_v.addAttribute(new Attribute("units", "m s-1"));
				varid_v.addAttribute(new Attribute("valid_min", "-10000"));
				varid_v.addAttribute(new Attribute("valid_max", "10000"));
				varid_v.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_v.addAttribute(new Attribute("scale_factor", Arrays.asList(new Double(0.001))));
				varid_v.addAttribute(new Attribute("add_offset", Arrays.asList(new Double(0))));
				varid_v.addAttribute(new Attribute("ioos_category", "Currents"));
				varid_v.addAttribute(new Attribute("coordsys", "geographic"));
				varid_v.addAttribute(
						new Attribute("sdn_parameter_name", "Northward current velocity in the water body"));
				varid_v.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::LCNSZZ01"));
				varid_v.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_v.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_v.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_v.addAttribute(
						new Attribute("ancillary_variables", "QCflag, VART_QC, CSPD_QC, DDNS_QC, GDOP_QC"));
				varid_v.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) -10), new Short((short) 10))));
				varid_v.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));

				// standar deviation
				Variable varid_usd = dataFile.addVariable(null, "EWCS", DataType.SHORT, dimsTDYX);
				varid_usd.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_usd.addAttribute(
						new Attribute("long_name", "Standard deviation of surface eastward sea water velocity"));
				varid_usd.addAttribute(new Attribute("standard_name", " "));
				varid_usd.addAttribute(new Attribute("units", "m s-1"));
				varid_usd.addAttribute(new Attribute("valid_min", "-10000"));
				varid_usd.addAttribute(new Attribute("valid_max", "10000"));
				varid_usd.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_usd.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) -10), new Short((short) 10))));
				varid_usd.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_usd.addAttribute(new Attribute("scale_factor", Arrays.asList(new Double(0.001))));
				varid_usd.addAttribute(new Attribute("add_offset", Arrays.asList(new Double(0))));
				varid_usd.addAttribute(new Attribute("sdn_parameter_name",
						"Eastward current velocity standard deviation in the water body"));
				varid_usd.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::SDEWZZZZ"));
				varid_usd.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_usd.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_usd.addAttribute(new Attribute("ancillary_variables", "QCflag, VART_QC"));

				// standar deviation
				Variable varid_vsd = dataFile.addVariable(null, "NSCS", DataType.SHORT, dimsTDYX);
				varid_vsd.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_vsd.addAttribute(
						new Attribute("long_name", "Standard deviation of surface northward sea water velocity"));
				varid_vsd.addAttribute(new Attribute("standard_name", " "));
				varid_vsd.addAttribute(new Attribute("units", "m s-1"));
				varid_vsd.addAttribute(new Attribute("valid_min", "-10000"));
				varid_vsd.addAttribute(new Attribute("valid_max", "10000"));
				varid_vsd.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_vsd.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) -10), new Short((short) 10))));
				varid_vsd.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_vsd.addAttribute(new Attribute("scale_factor", Arrays.asList(new Double(0.001))));
				varid_vsd.addAttribute(new Attribute("add_offset", Arrays.asList(new Double(0))));
				varid_vsd.addAttribute(new Attribute("sdn_parameter_name",
						"Northward current velocity standard deviation in the water body"));
				varid_vsd.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::SDNSZZZZ"));
				varid_vsd.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_vsd.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_vsd.addAttribute(new Attribute("ancillary_variables", "QCflag, VART_QC"));

				// CCOV
				Variable varid_ccov = dataFile.addVariable(null, "CCOV", DataType.INT, dimsTDYX);
				varid_ccov.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_INT));
				varid_ccov.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_ccov.addAttribute(new Attribute("long_name", "Covariance of surface sea water velocity"));
				varid_ccov.addAttribute(new Attribute("standard_name", " "));
				varid_ccov.addAttribute(new Attribute("units", "m2 s-2"));
				varid_ccov.addAttribute(new Attribute("valid_min", ""));
				varid_ccov.addAttribute(new Attribute("valid_max", ""));
				varid_ccov.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Integer((int) -10), new Integer((int) 10))));
				varid_ccov.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_ccov.addAttribute(new Attribute("scale_factor", Arrays.asList(new Double(0.000001))));
				varid_ccov.addAttribute(new Attribute("add_offset", Arrays.asList(new Double(0))));
				varid_ccov.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_ccov.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_ccov.addAttribute(new Attribute("sdn_uom_name", "Square metres per second squared"));
				varid_ccov.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::SQM2"));
				varid_ccov.addAttribute(new Attribute("ancillary_variables", "QCflag"));

				// GDOP
				Variable varid_gdop = dataFile.addVariable(null, "GDOP", DataType.SHORT, dimsTDYX);
				varid_gdop.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_gdop.addAttribute(new Attribute("long_name", "Geometrical dilution of precision"));
				varid_gdop.addAttribute(new Attribute("standard_name", " "));
				varid_gdop.addAttribute(new Attribute("units", "1"));
				varid_gdop.addAttribute(new Attribute("conventions", "Copernicus Marine In Situ reference table 2"));
				varid_gdop.addAttribute(new Attribute("valid_min", "-20000"));
				varid_gdop.addAttribute(new Attribute("valid_max", "20000"));
				varid_gdop.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_gdop.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) -20), new Short((short) 20))));
				varid_gdop.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_gdop.addAttribute(new Attribute("scale_factor", Arrays.asList(new Double(0.001))));
				varid_gdop.addAttribute(new Attribute("add_offset", Arrays.asList(new Double(0))));
				varid_gdop.addAttribute(new Attribute("comment",
						"The Geometric Dilution of PRecision (GDOP) is the coefficient of the uncertainty, "
								+ "which relates the uncertainties in radial and velocity vectors. The GDOP is a unit-less coefficient, which characterizes "
								+ "the effect that radar station geometry has on the measurement and positioin determination errors. A low GDOP corresponds "
								+ "to an optimal geometric configuration of radar stations, and results in accurate surface current data. Essentially GDOP is "
								+ "a quantitative way to relate the radial an dvelocity vector uncertainties. Setting a threshold on GDOP for total combination "
								+ "avoids the combination of radials with an intersection angle below a certain value. GDOP is a useful metric for filtering errant "
								+ "velocities due to poor geometry"));
				varid_gdop.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_gdop.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_gdop.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_gdop.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));
				varid_gdop.addAttribute(new Attribute("ancillary_variables", "QCflag, GDOP_QC"));

				// Add QC variables
				// Time QC Flag
				Variable varid_tqc = dataFile.addVariable(null, "TIME_QC", DataType.BYTE, "TIME");
				varid_tqc.addAttribute(new Attribute("long_name", "Time quality flag"));
				varid_tqc.addAttribute(new Attribute("units", "1"));
				varid_tqc.addAttribute(new Attribute("conventions", "Copernicus Marine In Situ reference table 2"));
				varid_tqc.addAttribute(new Attribute("valid_min", "0"));
				varid_tqc.addAttribute(new Attribute("valid_max", "9"));
				varid_tqc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), new Byte((byte) 9))));
				varid_tqc.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 2), new Byte((byte) 3),
								new Byte((byte) 4), new Byte((byte) 5), new Byte((byte) 7), new Byte((byte) 8),
								new Byte((byte) 9))));
				varid_tqc.addAttribute(new Attribute("flag_meanings",
						"no_qc_performed good_data probably_good_data bad_data_that_are_potentially_correctable bad_data value_changed not_used nominal_value interpolated_value missing_value"));
				varid_tqc
						.addAttribute(new Attribute("comment", "OceanSITES quality flagging for temporal coordinate."));
				varid_tqc.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_tqc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Byte((byte) 1))));
				varid_tqc.addAttribute(new Attribute("add_offset", Arrays.asList(new Byte((byte) 0))));

				// Position QC Flag
				Variable varid_posqc = dataFile.addVariable(null, "POSITION_QC", DataType.BYTE, dimsTDYX);
				varid_posqc.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_posqc.addAttribute(new Attribute("long_name", "Position quality flag"));
				varid_posqc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), new Byte((byte) 9))));
				varid_posqc.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 2), new Byte((byte) 3),
								new Byte((byte) 4), new Byte((byte) 5), new Byte((byte) 7), new Byte((byte) 8),
								new Byte((byte) 9))));
				varid_posqc.addAttribute(new Attribute("flag_meanings",
						"no_qc_performed good_data probably_good_data bad_data_that_are_potentially_correctable bad_data value_changed not_used nominal_value interpolated_value missing_value"));
				varid_posqc
						.addAttribute(new Attribute("comment", "OceanSITES quality flagging for position coordinates"));
				varid_posqc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Byte((byte) 1))));
				varid_posqc.addAttribute(new Attribute("add_offset", Arrays.asList(new Byte((byte) 0))));
				varid_posqc.addAttribute(new Attribute("units", "1"));
				varid_posqc.addAttribute(new Attribute("conventions", "Copernicus Marine In Situ reference table 2"));
				varid_posqc.addAttribute(new Attribute("valid_min", "0"));
				varid_posqc.addAttribute(new Attribute("valid_max", "9"));

				// Depth QC Flag
				Variable varid_dqc = dataFile.addVariable(null, "DEPH_QC", DataType.BYTE, "TIME");
				varid_dqc.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_dqc.addAttribute(new Attribute("long_name", "Depth quality flag"));
				varid_dqc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), new Byte((byte) 9))));
				varid_dqc.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 2), new Byte((byte) 3),
								new Byte((byte) 4), new Byte((byte) 5), new Byte((byte) 7), new Byte((byte) 8),
								new Byte((byte) 9))));
				varid_dqc.addAttribute(new Attribute("flag_meanings",
						"no_qc_performed good_data probably_good_data bad_data_that_are_potentially_correctable bad_data value_changed not_used nominal_value interpolated_value missing_value"));
				varid_dqc.addAttribute(new Attribute("comment", "OceanSITES quality flagging for depth coordinate."));
				varid_dqc.addAttribute(new Attribute("units", "1"));
				varid_dqc.addAttribute(new Attribute("conventions", "Copernicus Marine In Situ reference table 2"));
				varid_dqc.addAttribute(new Attribute("valid_min", "0"));
				varid_dqc.addAttribute(new Attribute("valid_max", "9"));
				varid_dqc.addAttribute(new Attribute("add_offset", "0"));
				varid_dqc.addAttribute(new Attribute("scale_factor", "1"));

				// Vector Flag
				// Overal QC flag
				Variable varid_vflg = dataFile.addVariable(null, "QCflag", DataType.BYTE, dimsTDYX);
				varid_vflg.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_vflg.addAttribute(new Attribute("long_name", "Overall quality flag"));
				varid_vflg.addAttribute(new Attribute("units", "1"));
				varid_vflg.addAttribute(new Attribute("conventions", "Copernicus Marine In Situ reference table 2"));
				varid_vflg.addAttribute(new Attribute("valid_min", "0"));
				varid_vflg.addAttribute(new Attribute("valid_max", "9"));
				varid_vflg.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), new Byte((byte) 9))));
				varid_vflg.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 2), new Byte((byte) 3),
								new Byte((byte) 4), new Byte((byte) 5), new Byte((byte) 7), new Byte((byte) 8),
								new Byte((byte) 9))));
				varid_vflg.addAttribute(new Attribute("flag_meanings",
						"no_qc_performed good_data probably_good_data bad_data_that_are_potentially_correctable bad_data value_changed not_used nominal_value interpolated_value missing_value"));
				varid_vflg.addAttribute(new Attribute("comment", "OceanSITES quality flagging for all QC tests."));
				varid_vflg.addAttribute(new Attribute("scale_factor", Arrays.asList(new Byte((byte) 1))));
				varid_vflg.addAttribute(new Attribute("add_offset", Arrays.asList(new Byte((byte) 0))));

				// Variance Threshold QC Flag
				Variable varid_vart = dataFile.addVariable(null, "VART_QC", DataType.BYTE, dimsTDYX);
				varid_vart.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_vart.addAttribute(new Attribute("long_name", "Variance threshold quality flag"));
				varid_vart.addAttribute(new Attribute("units", "1"));
				varid_vart.addAttribute(new Attribute("conventions", "Copernicus Marine In Situ reference table 2"));
				varid_vart.addAttribute(new Attribute("valid_min", "0"));
				varid_vart.addAttribute(new Attribute("valid_max", "9"));
				varid_vart.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), new Byte((byte) 9))));
				varid_vart.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 2), new Byte((byte) 3),
								new Byte((byte) 4), new Byte((byte) 5), new Byte((byte) 7), new Byte((byte) 8),
								new Byte((byte) 9))));
				varid_vart.addAttribute(new Attribute("flag_meanings",
						"no_qc_performed good_data probably_good_data bad_data_that_are_potentially_correctable bad_data value_changed not_used nominal_value interpolated_value missing_value"));
				varid_vart.addAttribute(new Attribute("comment",
						"OceanSITES quality flagging for Variance Threshold QC test. Test not applicable "
								+ "to Direction Finding Systems. "
								+ "The Temporal Derivative test is applied. Threshold set to "
								+ bean.getTotalTest().getVarianceThreshold() + " m2/s2."));
				varid_vart.addAttribute(new Attribute("scale_factor", Arrays.asList(new Byte((byte) 1))));
				varid_vart.addAttribute(new Attribute("add_offset", Arrays.asList(new Byte((byte) 0))));

				// GDOP Quality flag density_QC
				Variable varid_gdop_qc = dataFile.addVariable(null, "GDOP_QC", DataType.BYTE, dimsTDYX);
				varid_gdop_qc.addAttribute(new Attribute("long_name", "GDOP threshold quality flag"));
				varid_gdop_qc.addAttribute(new Attribute("units", "1"));
				varid_gdop_qc.addAttribute(new Attribute("conventions", "Copernicus Marine In Situ reference table 2"));
				varid_gdop_qc.addAttribute(new Attribute("valid_min", "0"));
				varid_gdop_qc.addAttribute(new Attribute("valid_max", "9"));
				varid_gdop_qc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), new Byte((byte) 9))));
				varid_gdop_qc.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 2), new Byte((byte) 3),
								new Byte((byte) 4), new Byte((byte) 5), new Byte((byte) 7), new Byte((byte) 8),
								new Byte((byte) 9))));
				varid_gdop_qc.addAttribute(new Attribute("flag_meanings",
						"no_qc_performed good_data probably_good_data bad_data_that_are_potentially_correctable bad_data value_changed not_used nominal_value interpolated_value missing_value"));
				varid_gdop_qc.addAttribute(new Attribute("comment",
						"OceanSITES quality flagging for GDOP threshold QC test. Threshold set to "
								+ bean.getTotalTest().getGDOPThreshold()));
				varid_gdop_qc.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_gdop_qc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Byte((byte) 1))));
				varid_gdop_qc.addAttribute(new Attribute("add_offset", Arrays.asList(new Byte((byte) 0))));

				// Data Density Quality flag density_QC
				Variable varid_dd = dataFile.addVariable(null, "DDNS_QC", DataType.BYTE, dimsTDYX);
				varid_dd.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_dd.addAttribute(new Attribute("long_name", "Data density threshold quality flag"));
				varid_dd.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), new Byte((byte) 9))));
				varid_dd.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 2), new Byte((byte) 3),
								new Byte((byte) 4), new Byte((byte) 5), new Byte((byte) 7), new Byte((byte) 8),
								new Byte((byte) 9))));
				varid_dd.addAttribute(new Attribute("flag_meanings",
						"no_qc_performed good_data probably_good_data bad_data_that_are_potentially_correctable bad_data value_changed not_used nominal_value interpolated_value missing_value"));
				varid_dd.addAttribute(new Attribute("comment",
						"OceanSITES quality flagging for Data Density threshold QC test. Threshold set to contributing radials. Minimun number of contribution is set to: "
								+ bean.getTotalTest().getDataDensityThreshold()));
				varid_dd.addAttribute(new Attribute("scale_factor", Arrays.asList(new Byte((byte) 1))));
				varid_dd.addAttribute(new Attribute("add_offset", Arrays.asList(new Byte((byte) 0))));
				varid_dd.addAttribute(new Attribute("units", "1"));
				varid_dd.addAttribute(new Attribute("conventions", "Copernicus Marine In Situ reference table 2"));
				varid_dd.addAttribute(new Attribute("valid_min", "0"));
				varid_dd.addAttribute(new Attribute("valid_max", "9"));

				// Velocity Threshold QC Flag
				Variable varid_velt = dataFile.addVariable(null, "CSPD_QC", DataType.BYTE, dimsTDYX);
				varid_velt.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_velt.addAttribute(new Attribute("long_name", "Velocity threshold quality flag"));
				varid_velt.addAttribute(new Attribute("units", "1"));
				varid_velt.addAttribute(new Attribute("conventions", "Copernicus Marine In Situ reference table 2"));
				varid_velt.addAttribute(new Attribute("valid_min", "0"));
				varid_velt.addAttribute(new Attribute("valid_max", "9"));
				varid_velt.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), new Byte((byte) 9))));
				varid_velt.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 2), new Byte((byte) 3),
								new Byte((byte) 4), new Byte((byte) 5), new Byte((byte) 7), new Byte((byte) 8),
								new Byte((byte) 9))));
				varid_velt.addAttribute(new Attribute("flag_meanings",
						"no_qc_performed good_data probably_good_data bad_data_that_are_potentially_correctable bad_data value_changed not_used nominal_value interpolated_value missing_value"));
				varid_velt.addAttribute(new Attribute("comment",
						"OceanSITES quality flagging for Velocity Threshold QC test. Threshold set to "
								+ bean.getTotalTest().getVeloThreshold() + " m/s."));
				varid_velt.addAttribute(new Attribute("scale_factor", Arrays.asList(new Byte((byte) 1))));
				varid_velt.addAttribute(new Attribute("add_offset", Arrays.asList(new Byte((byte) 0))));

				// Number of received antennas
				Variable varid_narx = dataFile.addVariable(null, "NARX", DataType.BYTE, "TIME");
				varid_narx.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_narx.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_narx.addAttribute(new Attribute("long_name", "Number of receive antennas"));
				varid_narx.addAttribute(new Attribute("standard_name", " "));
				varid_narx.addAttribute(new Attribute("units", "1"));
				varid_narx.addAttribute(new Attribute("valid_min", "0"));
				varid_narx.addAttribute(new Attribute("valid_max", "127"));
				varid_narx.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), CodarTotalToNetCDF.MAX_SITE)));
				varid_narx.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_narx.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_narx.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_narx.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_narx.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_narx.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));

				Variable varid_natx = dataFile.addVariable(null, "NATX", DataType.BYTE, "TIME");
				varid_natx.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_natx.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_natx.addAttribute(new Attribute("long_name", "Number of transmit antennas"));
				varid_natx.addAttribute(new Attribute("standard_name", " "));
				varid_natx.addAttribute(new Attribute("units", "1"));
				varid_natx.addAttribute(new Attribute("valid_min", "0"));
				varid_natx.addAttribute(new Attribute("valid_max", "127"));
				varid_natx.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), CodarTotalToNetCDF.MAX_SITE)));
				varid_natx.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_natx.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_natx.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_natx.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_natx.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_natx.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));

				Variable varid_sltr = dataFile.addVariable(null, "SLTR", DataType.INT, dimsTM);
				varid_sltr.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_INT));
				varid_sltr.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_sltr.addAttribute(new Attribute("long_name", "Receive antenna latitudes"));
				varid_sltr.addAttribute(new Attribute("standard_name", "latitude"));
				varid_sltr.addAttribute(new Attribute("units", "degree_north"));
				varid_sltr.addAttribute(new Attribute("valid_min", "-90000"));
				varid_sltr.addAttribute(new Attribute("valid_max", "90000"));
				varid_sltr.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Integer((int) -90), new Integer((int) 90))));
				varid_sltr.addAttribute(new Attribute("coordinates", "TIME MAXSITE"));
				varid_sltr.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(0.001))));
				varid_sltr.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_sltr.addAttribute(new Attribute("sdn_parameter_name", "Latitude north"));
				varid_sltr.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALATZZ01"));
				varid_sltr.addAttribute(new Attribute("sdn_uom_name", "Degrees north"));
				varid_sltr.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGN"));

				Variable varid_slnr = dataFile.addVariable(null, "SLNR", DataType.INT, dimsTM);
				varid_slnr.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_INT));
				varid_slnr.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_slnr.addAttribute(new Attribute("long_name", "Receive antenna longitudes"));
				varid_slnr.addAttribute(new Attribute("standard_name", "longitude"));
				varid_slnr.addAttribute(new Attribute("units", "degree_east"));
				varid_slnr.addAttribute(new Attribute("valid_min", "-180000"));
				varid_slnr.addAttribute(new Attribute("valid_max", "180000"));
				varid_slnr.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Integer((int) -180), new Integer((int) 180))));
				varid_slnr.addAttribute(new Attribute("coordinates", "TIME MAXSITE"));
				varid_slnr.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(0.001))));
				varid_slnr.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_slnr.addAttribute(new Attribute("sdn_parameter_name", "Longitude east"));
				varid_slnr.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALONZZ01"));
				varid_slnr.addAttribute(new Attribute("sdn_uom_name", "Degrees east"));
				varid_slnr.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGE"));

				Variable varid_sltt = dataFile.addVariable(null, "SLTT", DataType.INT, dimsTM);
				varid_sltt.addAttribute(new Attribute("long_name", "Transmit antenna latitudes"));
				varid_sltt.addAttribute(new Attribute("standard_name", "latitude"));
				varid_sltt.addAttribute(new Attribute("units", "degree_north"));
				varid_sltt.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_INT));
				varid_sltt.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_sltt.addAttribute(new Attribute("valid_min", "-90000"));
				varid_sltt.addAttribute(new Attribute("valid_max", "90000"));
				varid_sltt.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Integer((int) -90), new Integer((int) 90))));
				varid_sltt.addAttribute(new Attribute("coordinates", "TIME MAXSITE"));
				varid_sltt.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(0.001))));
				varid_sltt.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_sltt.addAttribute(new Attribute("sdn_parameter_name", "Latitude north"));
				varid_sltt.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALATZZ01"));
				varid_sltt.addAttribute(new Attribute("sdn_uom_name", "Degrees north"));
				varid_sltt.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGN"));

				Variable varid_slnt = dataFile.addVariable(null, "SLNT", DataType.INT, dimsTM);
				varid_slnt.addAttribute(new Attribute("long_name", "Transmit antenna longitudes"));
				varid_slnt.addAttribute(new Attribute("standard_name", "longitude"));
				varid_slnt.addAttribute(new Attribute("units", "degree_east"));
				varid_slnt.addAttribute(new Attribute("_FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_INT));
				varid_slnt.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_slnt.addAttribute(new Attribute("valid_min", "-180000"));
				varid_slnt.addAttribute(new Attribute("valid_max", "180000"));
				varid_slnt.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Integer((int) -180), new Integer((int) 180))));
				varid_slnt.addAttribute(new Attribute("coordinates", "TIME MAXSITE"));
				varid_slnt.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(0.001))));
				varid_slnt.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_slnt.addAttribute(new Attribute("sdn_parameter_name", "Longitude east"));
				varid_slnt.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALONZZ01"));
				varid_slnt.addAttribute(new Attribute("sdn_uom_name", "Degrees east"));
				varid_slnt.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGE"));

				// Receive antenna codes
				Variable varid_scdr = dataFile.addVariable(null, "SCDR", DataType.CHAR, dimsTMS15);
				varid_scdr.addAttribute(new Attribute("long_name", "Receive antenna codes"));
				varid_scdr.addAttribute(new Attribute("standard_name", " "));
				varid_scdr.addAttribute(new Attribute("units", "1"));
				varid_scdr.addAttribute(new Attribute("_FillValue", " "));
				varid_scdr.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_scdr.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_scdr.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_scdr.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_scdr.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));

				// Transmit antenna codes
				Variable varid_scdt = dataFile.addVariable(null, "SCDT", DataType.CHAR, dimsTMS15);
				varid_scdt.addAttribute(new Attribute("long_name", "Transmit antenna codes"));
				varid_scdt.addAttribute(new Attribute("standard_name", " "));
				varid_scdt.addAttribute(new Attribute("units", "1"));
				varid_scdt.addAttribute(new Attribute("_FillValue", " "));
				varid_scdt.addAttribute(new Attribute("data_mode", bean.getData_mode()));
				varid_scdt.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_scdt.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_scdt.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_scdt.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));

				// MANDATORY ATTRIBUTES
				// Discovery and Identification
				dataFile.addGroupAttribute(null, new Attribute("site_code", site_code));
				dataFile.addGroupAttribute(null, new Attribute("platform_code", platform_code));
				dataFile.addGroupAttribute(null, new Attribute("platform_name", platform_code));
				dataFile.addGroupAttribute(null, new Attribute("data_mode", bean.getData_mode()));
				dataFile.addGroupAttribute(null,
						new Attribute("DoA_estimation_method", bean.getNetworkBean().getDoA_estimation_method()));
				dataFile.addGroupAttribute(null,
						new Attribute("calibration_type", bean.getNetworkBean().getCalibration_type()));
				dataFile.addGroupAttribute(null,
						new Attribute("last_calibration_date", bean.getNetworkBean().getLasCalibrationDateUTC()));
				dataFile.addGroupAttribute(null,
						new Attribute("calibration_link", bean.getNetworkBean().getCalibration_link()));
				dataFile.addGroupAttribute(null, new Attribute("title", bean.getNetworkBean().getTitle()));
				dataFile.addGroupAttribute(null, new Attribute("summary", bean.getNetworkBean().getSummary()));
				dataFile.addGroupAttribute(null, new Attribute("source", bean.getSource()));
				dataFile.addGroupAttribute(null, new Attribute("source_platform_category_code", "17"));
				dataFile.addGroupAttribute(null,
						new Attribute("institution", bean.getNetworkBean().getInstitution_name()));
				dataFile.addGroupAttribute(null,
						new Attribute("institution_edmo_code", bean.getNetworkBean().getEDMO_code()));
				dataFile.addGroupAttribute(null,
						new Attribute("contact", "euhfrnode@azti.es cmems-service@ifremer.fr"));
				dataFile.addGroupAttribute(null, new Attribute("references", "http://marine.copernicus.eu http://www.marineinsitu.eu http://www.marineinsitu.eu/wp-content/uploads/2018/02/HFR_Data_Model_Reference_Card_v1.pdf"));
				dataFile.addGroupAttribute(null, new Attribute("data_assembly_center", "European HFR Node"));
				dataFile.addGroupAttribute(null, new Attribute("id", dataID));

				// Geo-spatial-temporal
				dataFile.addGroupAttribute(null, new Attribute("area", bean.getNetworkBean().getArea()));
				dataFile.addGroupAttribute(null, new Attribute("data_type", "HF radar total data"));
				dataFile.addGroupAttribute(null, new Attribute("cdm_data_type", "grid"));
				dataFile.addGroupAttribute(null, new Attribute("feature_type", "surface"));
				dataFile.addGroupAttribute(null, new Attribute("geospatial_vertical_positive", "down"));
				dataFile.addGroupAttribute(null, new Attribute("reference_system", "EPSG:4806"));
				dataFile.addGroupAttribute(null,
						new Attribute("geospatial_lat_min", bean.getNetworkBean().getGeospatial_lat_min()));
				dataFile.addGroupAttribute(null,
						new Attribute("geospatial_lat_max", bean.getNetworkBean().getGeospatial_lat_max()));
				dataFile.addGroupAttribute(null,
						new Attribute("geospatial_lon_min", bean.getNetworkBean().getGeospatial_lon_min()));
				dataFile.addGroupAttribute(null,
						new Attribute("geospatial_lon_max", bean.getNetworkBean().getGeospatial_lon_max()));
				dataFile.addGroupAttribute(null, new Attribute("geospatial_vertical_max", "4"));
				dataFile.addGroupAttribute(null, new Attribute("geospatial_vertical_min", "0"));
				dataFile.addGroupAttribute(null, new Attribute("time_coverage_start", bean.getTime_coverage_start()));
				dataFile.addGroupAttribute(null, new Attribute("time_coverage_end", bean.getTime_coverage_end()));
				dataFile.addGroupAttribute(null, new Attribute("bottom_depth", ""));

				dataFile.addGroupAttribute(null, new Attribute("geospatial_lat_units", "degree_north"));
				dataFile.addGroupAttribute(null, new Attribute("geospatial_lon_units", "degree_east"));

				// Conventions used
				dataFile.addGroupAttribute(null, new Attribute("format_version", "1.4"));
				dataFile.addGroupAttribute(null, new Attribute("Conventions",
						"CF-1.6 Copernicus-InSituTAC-FormatManual-1.41 Copernicus-InSituTAC-SRD-1.5 Copernicus-InSituTAC-ParametersList-3.2.0"));

				// Publication information
				dataFile.addGroupAttribute(null, new Attribute("update_interval", "void"));
				dataFile.addGroupAttribute(null, new Attribute("citation",
						"These data were collected and made freely available by the Copernicus project and the programs that contribute to it. "
								+ bean.getNetworkBean().getCitation_statement()));
				dataFile.addGroupAttribute(null, new Attribute("distribution_statement",
						"These data follow Copernicus standards; they are public and free of charge. User assumes all risk for use of data. User must display citation in any publication or product using data. User must contact PI prior to any commercial use of data."));
				dataFile.addGroupAttribute(null, new Attribute("publisher_name", bean.getPublisher_name()));
				dataFile.addGroupAttribute(null, new Attribute("publisher_url", bean.getPublisher_url()));
				dataFile.addGroupAttribute(null, new Attribute("publisher_email", bean.getPublisher_email()));
				dataFile.addGroupAttribute(null, new Attribute("license", bean.getNetworkBean().getLicense()));
				dataFile.addGroupAttribute(null,
						new Attribute("acknowledgment", bean.getNetworkBean().getAcknowledgment()));
				dataFile.addGroupAttribute(null, new Attribute("doi", ""));
				dataFile.addGroupAttribute(null, new Attribute("pi_name", ""));
				dataFile.addGroupAttribute(null, new Attribute("qc_manual", ""));
				
				// Provenance
				dataFile.addGroupAttribute(null, new Attribute("date_created", bean.getDateCreated()));
				dataFile.addGroupAttribute(null, new Attribute("history", bean.getHistCreated()));
				dataFile.addGroupAttribute(null, new Attribute("date_modified", bean.getDate_modified()));
				dataFile.addGroupAttribute(null, new Attribute("date_update", bean.getDateCreated()));
				dataFile.addGroupAttribute(null, new Attribute("processing_level", "3B"));
				dataFile.addGroupAttribute(null, new Attribute("contributor_name", bean.getContributor_name()));
				dataFile.addGroupAttribute(null, new Attribute("contributor_role", bean.getContributor_role()));
				dataFile.addGroupAttribute(null, new Attribute("contributor_email", bean.getContributor_email()));
				dataFile.addGroupAttribute(null, new Attribute("wmo_inst_type", ""));
				
				// RECOMMENDED ATTRIBUTES
				// Discovery and Identification
				dataFile.addGroupAttribute(null, new Attribute("project", bean.getNetworkBean().getProject()));
				dataFile.addGroupAttribute(null, new Attribute("naming_authority", bean.getNaming_authority())); 
				dataFile.addGroupAttribute(null, new Attribute("wmo_platform_code", ""));
				dataFile.addGroupAttribute(null, new Attribute("ices_platform_code", ""));	
				dataFile.addGroupAttribute(null, new Attribute("keywords", bean.getKeywords()));
				dataFile.addGroupAttribute(null, new Attribute("keywords_vocabulary", bean.getKeywords_vocabulary()));
				dataFile.addGroupAttribute(null, new Attribute("comment", bean.getNetworkBean().getComment()));
				dataFile.addGroupAttribute(null, new Attribute("data_language", bean.getData_language()));
				dataFile.addGroupAttribute(null, new Attribute("data_character_set", bean.getData_char_set()));
				dataFile.addGroupAttribute(null, new Attribute("metadata_language", bean.getMetadata_language()));
				dataFile.addGroupAttribute(null, new Attribute("metadata_character_set", bean.getMetadata_char_set()));
				dataFile.addGroupAttribute(null, new Attribute("topic_category", bean.getTopic_cat()));
				dataFile.addGroupAttribute(null, new Attribute("network", bean.getNetworkBean().getNetwork_name()));

				// Geo-spatial-temporal TODO

				// Conventions used
				dataFile.addGroupAttribute(null, new Attribute("netcdf_version", bean.getNetcdf_version()));
				dataFile.addGroupAttribute(null, new Attribute("netcdf_format", bean.getNetcdf_format()));

				// OTHER ATTRIBUTES
				dataFile.addGroupAttribute(null, new Attribute("metadata_contact", bean.getMetadata_contact()));
				dataFile.addGroupAttribute(null, new Attribute("metadata_date_stamp", bean.getDateCreated()));
				dataFile.addGroupAttribute(null, new Attribute("standard_name_vocabulary", bean.getName_vocabulary()));
				dataFile.addGroupAttribute(null,
						new Attribute("institution_references", bean.getNetworkBean().getInstitution_website()));
				dataFile.addGroupAttribute(null, new Attribute("software_name", "JRadar"));
				dataFile.addGroupAttribute(null, new Attribute("software_version", "v2.1"));
				dataFile.addGroupAttribute(null, new Attribute("date_issued", bean.getDateCreated()));
				// Creator by Jose Luis Asensio
				dataFile.addGroupAttribute(null, new Attribute("software_about1",
						"This file was created using JRadar software tool, developed by Jose Luis Asensio"));
				dataFile.addGroupAttribute(null, new Attribute("software_about2",
						"To get more info about JRadar, write to txelu_ai@hotmail.com"));

				// OTHER ATTRIBUTES
				dataFile.addGroupAttribute(null, new Attribute("metadata_contact", bean.getMetadata_contact()));
				dataFile.addGroupAttribute(null, new Attribute("metadata_date_stamp", bean.getDateCreated()));
				dataFile.addGroupAttribute(null, new Attribute("standard_name_vocabulary", bean.getName_vocabulary()));
				dataFile.addGroupAttribute(null,
						new Attribute("institution_references", bean.getNetworkBean().getInstitution_website()));
				dataFile.addGroupAttribute(null, new Attribute("software_name", "JRadar"));
				dataFile.addGroupAttribute(null, new Attribute("software_version", "v2.1"));
				dataFile.addGroupAttribute(null, new Attribute("date_issued", bean.getDateCreated()));
				// Creator by Jose Luis Asensio
				dataFile.addGroupAttribute(null, new Attribute("software_about1",
						"This file was created using JRadar software tool, developed by Jose Luis Asensio"));
				dataFile.addGroupAttribute(null, new Attribute("software_about2",
						"To get more info about JRadar, write to txelu_ai@hotmail.com"));

				// End the definition mode and start writing data
				dataFile.create();

				// time dimension, days from 1950 We create a variable of that
				// date, and calculate de day number.
				Array timeData = Array.factory(DataType.DOUBLE, new int[] { 1 });
				Calendar date = bean.getTimeStampAsCalendar();

				SimpleDateFormat codarFormat = new SimpleDateFormat("yyyy MM dd HH mm ss");
				codarFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				Calendar date2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				date2.setTime(codarFormat.parse("1950 01 01 00 00 00"));
				// diference between timestamp, 1950 and divide by millisec,
				// sec, min and hours to get days sice 1950
				timeData.setDouble(0, (((date.getTimeInMillis() - date2.getTimeInMillis()) / (1000.0 * 3600 * 24))));
				dataFile.write(varid_t, timeData);
				timeData = null;
				dataFile.write(varid_depth, CodarUtils.transformCollectionInMultidimensionalFloatArray(
						Arrays.asList(new Float(0)), varid_depth.getDimensions()));

				dataFile.write(varid_lat, CodarUtils.transformCollectionInArray(latCalc));
				dataFile.write(varid_lon, CodarUtils.transformCollectionInArray(lonCalc));

				dataFile.write(varid_crs, CodarUtils.transformCollectionInMultidimensionalShortArray(
						Arrays.asList(new Float(0)), varid_crs.getDimensions(), 1));
				dataFile.write(varid_sdncruise,
						CodarUtils.transformStringIntoArrayChar(site_code, varid_sdncruise.getDimensions()));
				dataFile.write(varid_sdnstation,
						CodarUtils.transformStringIntoArrayChar(platform_code, varid_sdnstation.getDimensions()));
				dataFile.write(varid_sdnlocalcdiid,
						CodarUtils.transformStringIntoArrayChar(dataID, varid_sdnlocalcdiid.getDimensions()));
				dataFile.write(varid_sdnedmocode,
						CodarUtils.transformCollectionInMultidimensionalShortArray(
								Arrays.asList(new Float(bean.getNetworkBean().getEDMO_code_As_Short())),
								varid_sdnedmocode.getDimensions(), 1));
				dataFile.write(varid_sdnreferences,
						CodarUtils.transformStringIntoArrayChar(TDS_catalog, varid_sdnreferences.getDimensions()));
				dataFile.write(varid_sdnxlink,
						CodarUtils.transformStringIntoArrayChar(xlink, varid_sdnxlink.getDimensions()));

				// vel_u
				Array arrayVelU = CodarUtils.transformCollectionInMultidimensionalShortArray(velu,
						varid_u.getDimensions(), 1000);
				dataFile.write(varid_u, arrayVelU);

				// vel_v
				Array arrayVelV = CodarUtils.transformCollectionInMultidimensionalShortArray(velv,
						varid_v.getDimensions(), 1000);
				dataFile.write(varid_v, arrayVelV);

				// ewcs east standar deviation u StdDev
				List<Float> ewcs = bean.getTable().getColumnElementsInOrder(TableColumnNames.UQAL, codarToNetcdfIndex);
				Array ewcsData = CodarUtils.transformCollectionInMultidimensionalShortArray(ewcs,
						varid_usd.getDimensions(), 1000);
				dataFile.write(varid_usd, ewcsData);

				// nscs north standar deviation v StdDev
				List<Float> nscs = bean.getTable().getColumnElementsInOrder(TableColumnNames.VQAL, codarToNetcdfIndex);
				Array nscsData = CodarUtils.transformCollectionInMultidimensionalShortArray(nscs,
						varid_vsd.getDimensions(), 1000);
				dataFile.write(varid_vsd, nscsData);

				// covariance quality
				List<Float> ccov = bean.getTable().getColumnElementsInOrder(TableColumnNames.CQAL, codarToNetcdfIndex);
				Array ccovData = CodarUtils.transformCollectionInMultidimensionalIntArray(ccov,
						varid_ccov.getDimensions(), 1000000);
				dataFile.write(varid_ccov, ccovData);

				// GDOP
				List<Float> gdop = bean.getTable().getColumnElementsInOrder(TableColumnNames.CQAL, codarToNetcdfIndex);
				Array gdopData = CodarUtils.transformCollectionInMultidimensionalShortArray(gdop,
						varid_gdop.getDimensions(), 1000);
				dataFile.write(varid_gdop, gdopData);

				ArrayList<String> sites = bean.getSiteSource();
				List<Float> slat = new ArrayList<Float>(sites.size());
				List<Float> slon = new ArrayList<Float>(sites.size());
				List<String> scod = new ArrayList<String>(sites.size());
				for (int i = 0; i < sites.size(); i++) {
					String site = sites.get(i);
					String[] siteData = site.trim().split("\\s+");
					slat.add(i, Float.parseFloat(siteData[2]));
					slon.add(i, Float.parseFloat(siteData[3]));
					scod.add(i, siteData[1]);
				}
				ArrayChar scodData = new ArrayChar.D3(1, CodarTotalToNetCDF.MAX_SITE, 15);

				Index ima = scodData.getIndex();
				for (int j = 0; j < scod.size(); j++) {
					char[] tempSite = scod.get(j).toCharArray();

					// 15 is the max for the variable
					for (int x = 0; x < 15; x++) {
						// 4 is the length of the site name
						char aa = new Character(' ');
						if (x < 4)
							aa = tempSite[x];
						scodData.setChar(ima.set(0, j, x), aa);
					}
				}
				dataFile.write(varid_narx, CodarUtils.transformCollectionInMultidimensionalByteArray(
						Arrays.asList(new Float(slat.size())), varid_narx.getDimensions()));
				dataFile.write(varid_natx, CodarUtils.transformCollectionInMultidimensionalByteArray(
						Arrays.asList(new Float(slat.size())), varid_natx.getDimensions()));
				Array slatData = CodarUtils.transformCollectionInMultidimensionalIntArray(slat,
						varid_sltr.getDimensions(), 1000);
				Array slotData = CodarUtils.transformCollectionInMultidimensionalIntArray(slon,
						varid_slnr.getDimensions(), 1000);

				dataFile.write(varid_sltr, slatData);
				dataFile.write(varid_slnr, slotData);
				dataFile.write(varid_sltt, slatData);
				dataFile.write(varid_slnt, slotData);

				dataFile.write(varid_scdr, scodData);
				dataFile.write(varid_scdt, scodData);

				// QC flags
				dataFile.write(varid_tqc, CodarUtils.transformCollectionInMultidimensionalByteArray(
						Arrays.asList(new Float(CodarTotalToNetCDF.SDN_TIME_QC_FLAG)), varid_tqc.getDimensions()));
				List<Float> sdnPosFlag = new ArrayList<Float>(velu.size());
				for (Float vtemp : velu) {
					if (Float.isNaN(vtemp)) {
						sdnPosFlag.add(Float.NaN);
					} else {
						sdnPosFlag.add(new Float(1));
					}
				}
				dataFile.write(varid_posqc, CodarUtils.transformCollectionInMultidimensionalByteArray(sdnPosFlag,
						varid_posqc.getDimensions()));
				dataFile.write(varid_dqc, CodarUtils.transformCollectionInMultidimensionalByteArray(
						Arrays.asList(new Float(CodarTotalToNetCDF.SDN_DEPTH_QC_FLAG)), varid_dqc.getDimensions()));

				// QA - QC flags
				VentanaRunTotalQualityTests qcqaTest = new VentanaRunTotalQualityTests(bean);

				// Velocity Threshold Quality Test columna 12?
				List<Float> velocityThreshold_QCflag = qcqaTest.runThresholdTest(velo,
						bean.getTotalTest().getVeloThreshold());
				Array array_velt = CodarUtils.transformCollectionInMultidimensionalByteArray(velocityThreshold_QCflag,
						varid_velt.getDimensions());
				dataFile.write(varid_velt, array_velt);

				// DD quality test
				List<Float> ddThreshold = qcqaTest.runDataDensityTest(bean.getTotalTest().getDataDensityThreshold(),
						profile);
				Array array_dd = CodarUtils.transformCollectionInMultidimensionalByteArray(ddThreshold,
						varid_dd.getDimensions());
				dataFile.write(varid_dd, array_dd);

				// GDOP Quality Test
				List<Float> gdopThreshold_QCflag = qcqaTest.runThresholdTest(
						bean.getTable().getColumnElementsInOrder(TableColumnNames.CQAL, codarToNetcdfIndex),
						bean.getTotalTest().getGDOPThreshold());
				Array array_gdop = CodarUtils.transformCollectionInMultidimensionalByteArray(gdopThreshold_QCflag,
						varid_gdop_qc.getDimensions());
				dataFile.write(varid_gdop_qc, array_gdop);

				// Temporal derivative test
				List<Float> tderThreshold_QCFlags = qcqaTest.runTempDerivativeTest(profile);
				Array array_tder = CodarUtils.transformCollectionInMultidimensionalByteArray(tderThreshold_QCFlags,
						varid_vart.getDimensions());
				dataFile.write(varid_vart, array_tder);

				// Este QC es la suma logica del resto. Si todo está bien, este
				// está bien, si alguno esta mal, este esta mal.
				List<Float> overal_QCflag = new ArrayList<Float>(gdopThreshold_QCflag.size());
				for (int i = 0; i < gdopThreshold_QCflag.size(); i++) {
					if (gdopThreshold_QCflag.get(i).floatValue() == 1 && tderThreshold_QCFlags.get(i).floatValue() == 1
							&& velocityThreshold_QCflag.get(i).floatValue() == 1
							&& ddThreshold.get(i).floatValue() == 1) {
						overal_QCflag.add(new Float(1));
					} else if (gdopThreshold_QCflag.get(i).floatValue() == 9
							&& tderThreshold_QCFlags.get(i).floatValue() == 9
							&& velocityThreshold_QCflag.get(i).floatValue() == 9
							&& ddThreshold.get(i).floatValue() == 9) {
						overal_QCflag.add(new Float(9));
					} else if (gdopThreshold_QCflag.get(i).floatValue() == 0
							&& tderThreshold_QCFlags.get(i).floatValue() == 0
							&& velocityThreshold_QCflag.get(i).floatValue() == 0
							&& ddThreshold.get(i).floatValue() == 0) {
						overal_QCflag.add(new Float(0));
					} else if (gdopThreshold_QCflag.get(i).isNaN() && tderThreshold_QCFlags.get(i).isNaN()
							&& velocityThreshold_QCflag.get(i).isNaN() && ddThreshold.get(i).isNaN()) {
						overal_QCflag.add(Float.NaN);
					} else {
						overal_QCflag.add(new Float(4));
					}

				}
				Array array_ovqc = CodarUtils.transformCollectionInMultidimensionalByteArray(overal_QCflag,
						varid_vflg.getDimensions());
				dataFile.write(varid_vflg, array_ovqc);

				dataFile.close();

				if (fixed) {
					errorCode = 6;
				}
				// TODO: Check if the netCDF C library is properly installed
				// before creating the file..
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidRangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
				errorCode = 7;
			}
			break;
		}

		return errorCode;
	}

	public boolean checkBeanTotalTable() {
		boolean hasError = (this.bean.getTable() == null || this.bean.getTable().isEmpty());

		return hasError;
	}

	/**
	 * return if there is an error with the test parameters
	 * 
	 * @return
	 */
	public boolean checkQCQA() {
		boolean hasError = !bean.getTotalTest().readyToRunTests();

		return hasError;
	}

	public boolean checkBeanMandatoryFields() {
		boolean hasError = true;

		try {
			if (bean.getData_mode() != null && !bean.getData_mode().isEmpty() && bean.getSource() != null
					&& !bean.getSource().isEmpty() && bean.getTime_coverage_start() != null
					&& !bean.getTime_coverage_start().isEmpty() && bean.getTime_coverage_end() != null
					&& !bean.getTime_coverage_end().isEmpty() && bean.getPublisher_name() != null
					&& !bean.getPublisher_name().isEmpty() && bean.getPublisher_email() != null
					&& !bean.getPublisher_email().isEmpty() && bean.getPublisher_url() != null
					&& !bean.getPublisher_url().isEmpty() && bean.getHistCreated() != null
					&& !bean.getHistCreated().isEmpty() && bean.getContributor_name() != null
					&& !bean.getContributor_name().isEmpty() && bean.getContributor_role() != null
					&& !bean.getContributor_role().isEmpty() && bean.getContributor_email() != null
					&& !bean.getContributor_email().isEmpty() && bean.getxMax() != null && !bean.getxMax().isEmpty()
					&& bean.getxMin() != null && !bean.getxMin().isEmpty() && bean.getyMax() != null
					&& !bean.getyMax().isEmpty() && bean.getyMin() != null && !bean.getyMin().isEmpty()

			) {
				hasError = false;
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			hasError = true;
			e.printStackTrace();
		}
		return hasError;

	}

}
