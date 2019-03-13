/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  30 de may. de 2016
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

import es.azti.codar.beans.CodarRadialBean;
import es.azti.netcdf.ui.VentanaRunRadialQualityTests;
import es.azti.netcdf.ui.VentanaSaveFichero;
import es.azti.utils.TableColumnNames;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingStrategy;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es) 30 de may. de 2016
 *
 *         Codar radial to NetCDF transformator
 */
public class CodarRadialToNetCDF {

	private static final int MAX_SITE = 50;
	private static final int NUM_SITES = 1;
	private static final int REF_MAX = 1;
	private static final int SDN_TIME_QC_FLAG = 1;
	private static final int SDN_DEPTH_QC_FLAG = 1;

	CodarRadialBean bean;

	public CodarRadialToNetCDF(CodarRadialBean bean) {
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

	public int saveProfile(CodarRadialBean profile) {
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
		} else if (checkBeanRadialTable()) {
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

	public int toNetCDF4(CodarRadialBean profile, String outputFileName) {
		// errores:
		// code = 0: ok
		// code = 1: mandatory field is missing or has an error.
		// code = 2: not mandatory field error (we can save file)
		// code = 3: radial data Table is missing
		// code = 4: Quality tests can't be runned due to a missing values.
		// code = 5: metadata not loaded.
		// code = 6: parameters missing but profile used instead.

		// check mandatory values:
		int errorCode = 0;
		if (checkBeanMandatoryFields()) {
			errorCode = 1;
		} else if (checkQCQA()) {
			errorCode = 4;
		} else if (bean.getStationBean() == null || bean.getNetworkBean() == null) {
			errorCode = 5;
		} else if (checkBeanRadialTable()) {
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
			// save file,
			NetcdfFileWriter dataFile = null;
			try {
				List<Integer> bearingDims = new ArrayList<Integer>();

				int minBearing = ((int) (bean.getMinimunBearingAsFloat() / bean.getAngularResolutionInteger()))
						* bean.getAngularResolutionInteger()
						+ (int) (bean.getAntennaBearingAsFloat() % bean.getAngularResolutionInteger());
				int maxBearing = ((int) (bean.getMaximunBearingAsFloat() / bean.getAngularResolutionInteger()))
						* bean.getAngularResolutionInteger();

				for (int i = minBearing; i <= maxBearing; i = i + bean.getAngularResolutionInteger()) {
					bearingDims.add(i);
				}

				List<Float> rangeDims = new ArrayList<Float>();

				float minRange = ((int) (bean.getMinimunRangeAsFloat() / bean.getRangeResolutionKMetersFloat()))
						* bean.getRangeResolutionKMetersFloat();
				float maxRange = ((int) (bean.getMaximunRangeAsFloat() / bean.getRangeResolutionKMetersFloat()))
						* bean.getRangeResolutionKMetersFloat();

				for (float i = minRange; i <= maxRange; i = (float) (Math
						.rint((i + bean.getRangeResolutionKMetersFloat()) * 10000) / 10000)) {
					rangeDims.add(i);
				}

				List<Integer> codarToNetcdfIndex = bean.getTable().getRadialTableIndexInNetCdf(bearingDims, rangeDims);
				// time coverage data

				// Current Speed - flip sign so positive velocity is away from
				// radar
				// according to CF standard name
				// 'radial_sea_water_velocity_away_from_instrument'.
				// CODAR reports positive speeds as toward the radar.
				// transform cm to m
				List<Float> veloRead = bean.getTable().getColumnElementsInOrder(TableColumnNames.VELO,
						codarToNetcdfIndex);
				List<Float> veloURead = bean.getTable().getColumnElementsInOrder(TableColumnNames.VELU,
						codarToNetcdfIndex);
				List<Float> veloVRead = bean.getTable().getColumnElementsInOrder(TableColumnNames.VELV,
						codarToNetcdfIndex);
				List<Float> velo = new ArrayList<Float>(veloRead.size());
				List<Float> velu = new ArrayList<Float>(veloURead.size());
				List<Float> velv = new ArrayList<Float>(veloVRead.size());
				for (Float velTemp : veloRead) {
					velo.add(new Float(-velTemp / 100));
				}
				for (Float velTemp : veloURead) {
					velu.add(new Float(velTemp / 100));
				}
				for (Float velTemp : veloVRead) {
					velv.add(new Float(velTemp / 100));
				}
				// processing data. changing units
				// Spatial Quality
				List<Float> spatialQTemp = bean.getTable().getColumnElementsInOrder(TableColumnNames.ESPC,
						codarToNetcdfIndex);
				List<Float> spatialQ = new ArrayList<Float>(spatialQTemp.size());
				for (Float sq : spatialQTemp) {
					float sqTemp = new Float(sq / 100);
					// Locate native bad-values for codar files.
					if (sqTemp == 9.99)
						sqTemp = Float.NaN;
					spatialQ.add(sqTemp);
				}

				// Temporal Quality
				List<Float> tempQTemp = bean.getTable().getColumnElementsInOrder(TableColumnNames.ETMP,
						codarToNetcdfIndex);
				List<Float> tempQ = new ArrayList<Float>(tempQTemp.size());
				for (Float tq : tempQTemp) {
					float tqTemp = new Float(tq / 100);
					// Locate native bad-values for codar files
					if (tqTemp == 9.99)
						tqTemp = Float.NaN;
					tempQ.add(tqTemp);
				}

				// Velocity Maximum - flip sign so positive velocity is away
				// from radar
				// according to CF standard name
				// 'radial_sea_water_velocity_away_from_instrument'.
				// CODAR reports positive speeds as toward the radar.
				List<Float> maxvTemp = bean.getTable().getColumnElementsInOrder(TableColumnNames.MAXV,
						codarToNetcdfIndex);
				List<Float> maxv = new ArrayList<Float>(maxvTemp.size());
				for (Float tq : maxvTemp) {
					float tqTemp = new Float(-tq / 100);
					// Locate native bad-values for codar files.
					if (tqTemp == 9.99)
						tqTemp = Float.NaN;
					maxv.add(tqTemp);
				}

				// Velocity Minimum - flip sign so positive velocity is away
				// from radar
				// according to CF standard name
				// 'radial_sea_water_velocity_away_from_instrument'.
				// CODAR reports positive speeds as toward the radar.
				List<Float> minvTemp = bean.getTable().getColumnElementsInOrder(TableColumnNames.MINV,
						codarToNetcdfIndex);
				List<Float> minv = new ArrayList<Float>(minvTemp.size());
				for (Float tq : minvTemp) {
					float tqTemp = new Float(-tq / 100);
					// Locate native bad-values for codar files.
					if (tqTemp == 9.99)
						tqTemp = Float.NaN;
					minv.add(tqTemp);
				}

				String site_code = bean.getNetworkBean().getNetwork_id();
				String site_id = bean.getStationBean().getStation_id();
				String platform_code = site_code + "_" + bean.getStationBean().getStation_id();
				String id = platform_code + "_" + bean.getTimeStampAsUTC();
				String TDS_catalog = bean.getNetworkBean().getMetadata_page();
				String xlink = "<sdn_reference xlink:href=\"" + TDS_catalog + "\" xlink:role=\"\" xlink:type=\"URL\"/>";

				String fileName = outputFileName;
				if (fileName == null)
					fileName = this.getOutputFileName();

				Nc4Chunking chunker = Nc4ChunkingStrategy.factory(Nc4Chunking.Strategy.standard, 6, true);
				dataFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4_classic, fileName, chunker);

				Dimension dimid_t = dataFile.addDimension(null, "TIME", 1);
				// Dimension dimid_t = dataFile.addUnlimitedDimension("TIME");
				Dimension dimid_bearing = dataFile.addDimension(null, "BEAR", bearingDims.size());
				Dimension dimid_range = dataFile.addDimension(null, "RNGE", rangeDims.size());
				Dimension dimid_depth = dataFile.addDimension(null, "DEPH", 1);
				Dimension dimid_maxsite = dataFile.addDimension(null, "MAXSITE", CodarRadialToNetCDF.MAX_SITE);
				Dimension dimid_refmax = dataFile.addDimension(null, "REFMAX", CodarRadialToNetCDF.REF_MAX);
				Dimension dimid_string_site_id = dataFile.addDimension(null, "STRING"+site_id.length(), site_id.length());
				Dimension dimid_string_site_code = dimid_string_site_id;
				if (site_code.length() != site_id.length()) {
					dimid_string_site_code = dataFile.addDimension(null, "STRING" + site_code.length(),
							site_code.length());
				}
				Dimension dimid_string_platform_code = dataFile.addDimension(null, "STRING" + platform_code.length(),
						platform_code.length());
				Dimension dimid_string_sdn_local_cdi_id = dataFile.addDimension(null, "STRING" + id.length(),
						id.length());
				Dimension dimid_string_sdn_references = dataFile.addDimension(null, "STRING" + TDS_catalog.length(),
						TDS_catalog.length());
				Dimension dimid_string_sdn_xlink = dataFile.addDimension(null, "STRING" + xlink.length(),
						xlink.length());

				List<Dimension> dimsT = new ArrayList<Dimension>();
				List<Dimension> dimsTDBR = new ArrayList<Dimension>();
				List<Dimension> dimsBR = new ArrayList<Dimension>();
				List<Dimension> dimsTS = new ArrayList<Dimension>();
				List<Dimension> dimsTP = new ArrayList<Dimension>();
				List<Dimension> dimsTId = new ArrayList<Dimension>();
				List<Dimension> dimsTSdnRef = new ArrayList<Dimension>();
				List<Dimension> dimsTRmaxXlink = new ArrayList<Dimension>();
				List<Dimension> dimsTM = new ArrayList<Dimension>();
				List<Dimension> dimsTMS4 = new ArrayList<Dimension>();

				dimsT.add(dimid_t);

				dimsTDBR.add(dimid_t);
				dimsTDBR.add(dimid_depth);
				dimsTDBR.add(dimid_bearing);
				dimsTDBR.add(dimid_range);

				dimsBR.add(dimid_bearing);
				dimsBR.add(dimid_range);

				dimsTS.add(dimid_t);
				dimsTS.add(dimid_string_site_code);

				dimsTP.add(dimid_t);
				dimsTP.add(dimid_string_platform_code);

				dimsTId.add(dimid_t);
				dimsTId.add(dimid_string_sdn_local_cdi_id);

				dimsTSdnRef.add(dimid_t);
				dimsTSdnRef.add(dimid_string_sdn_references);

				dimsTRmaxXlink.add(dimid_t);
				dimsTRmaxXlink.add(dimid_refmax);
				dimsTRmaxXlink.add(dimid_string_sdn_xlink);

				dimsTM.add(dimid_t);
				dimsTM.add(dimid_maxsite);

				dimsTMS4.add(dimid_t);
				dimsTMS4.add(dimid_maxsite);
				dimsTMS4.add(dimid_string_site_id);

				// https://www.unidata.ucar.edu/software/netcdf/docs/BestPractices.html
				// check how to add fillvalues..
				Variable varid_t = dataFile.addVariable(null, "TIME", DataType.FLOAT, "TIME");
				varid_t.addAttribute(new Attribute("long_name", "Time of Measurement UTC"));
				varid_t.addAttribute(new Attribute("standard_name", "time"));
				varid_t.addAttribute(new Attribute("units", "days since 1950-01-01T00:00:00Z"));
				varid_t.addAttribute(new Attribute("calendar", "Julian"));
				varid_t.addAttribute(new Attribute("axis", "T"));
				varid_t.addAttribute(new Attribute("sdn_parameter_name", "Elapsed time (since 1950-01-01T00:00:00Z)"));
				varid_t.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ELTJLD01"));
				varid_t.addAttribute(new Attribute("sdn_uom_name", "Days"));
				varid_t.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UTAA"));
				varid_t.addAttribute(new Attribute("ancillary_variables", "TIME_SEADATANET_QC"));

				Variable varid_bearing = dataFile.addVariable(null, "BEAR", DataType.FLOAT, "BEAR");
				varid_bearing.addAttribute(new Attribute("axis", "Y"));
				varid_bearing.addAttribute(new Attribute("long_name", "Bearing Away From Instrument"));
				varid_bearing.addAttribute(new Attribute("units", "degrees_true"));
				varid_bearing.addAttribute(new Attribute("sdn_prameter_name", "Bearing"));
				varid_bearing.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::BEARRFTR"));
				varid_bearing.addAttribute(new Attribute("sdn_uom_name", "Degrees true"));
				varid_bearing.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UABB"));
				varid_bearing.addAttribute(new Attribute("ancillary_variables", "POSITION_SEADATANET_QC"));

				Variable varid_range = dataFile.addVariable(null, "RNGE", DataType.FLOAT, "RNGE");
				varid_range.addAttribute(new Attribute("axis", "X"));
				varid_range.addAttribute(new Attribute("long_name", "Range Away From Instrument"));
				varid_range.addAttribute(new Attribute("units", "km"));
				varid_range.addAttribute(new Attribute("sdn_prameter_name",
						"Range (from fixed reference point) by unspecified GPS system"));
				varid_range.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::RIFNAX01"));
				varid_range.addAttribute(new Attribute("sdn_uom_name", "Kilometres"));
				varid_range.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::ULKM"));
				varid_range.addAttribute(new Attribute("ancillary_variables", "POSITION_SEADATANET_QC"));

				Variable varid_depth = dataFile.addVariable(null, "DEPH", DataType.FLOAT, "DEPH");
				varid_depth.addAttribute(new Attribute("long_name", "Depth of Measurement"));
				varid_depth.addAttribute(new Attribute("standard_name", "depth"));
				varid_depth.addAttribute(new Attribute("units", "m"));
				varid_depth.addAttribute(new Attribute("axis", "Z"));
				varid_depth.addAttribute(new Attribute("positive", "down"));
				varid_depth.addAttribute(new Attribute("reference", "sea_level"));
				varid_depth.addAttribute(new Attribute("sdn_parameter_name", "Depth below surface of the water body"));
				varid_depth.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ADEPZZ01"));
				varid_depth.addAttribute(new Attribute("sdn_uom_name", "Metres"));
				varid_depth.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::ULAA"));
				varid_depth.addAttribute(new Attribute("ancillary_variables", "DEPTH_SEADATANET_QC"));

				// Add auxillary coordinate variables to provide mapping from
				// range and bearing to lat, lon.
				// A minimum of 4 significant figures to the right of the
				// decimal
				// place is needed to keep resolution below 10's of meters.
				// Using
				// float data type for lat yields at least 5 significant digits
				// to
				// the right of the decimal giving ~1/2m resolution in latitude.
				// Nine significant figures (at least 7 to the right of the
				// decimal) could be achieved using int data type but need to
				// introduce a scale factor.

				// Latitude
				Variable varid_lat = dataFile.addVariable(null, "LATITUDE", DataType.FLOAT, dimsBR);
				varid_lat.addAttribute(new Attribute("standard_name", "latitude"));
				varid_lat.addAttribute(new Attribute("long_name", "Latitude"));
				varid_lat.addAttribute(new Attribute("units", "degrees_north"));
				varid_lat.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-90), new Float(90))));
				varid_lat.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_lat.addAttribute(new Attribute("sdn_parameter_name", "Latitude north"));
				varid_lat.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALATZZ01"));
				varid_lat.addAttribute(new Attribute("sdn_uom_name", "Degrees north"));
				varid_lat.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGN"));
				varid_lat.addAttribute(new Attribute("ancillary_variables", "POSITION_SEADATANET_QC"));

				// Longitude
				Variable varid_lon = dataFile.addVariable(null, "LONGITUDE", DataType.FLOAT, dimsBR);
				varid_lon.addAttribute(new Attribute("standard_name", "longitude"));
				varid_lon.addAttribute(new Attribute("long_name", "Longitude"));
				varid_lon.addAttribute(new Attribute("units", "degrees_east"));
				varid_lon.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-180), new Float(180))));
				varid_lon.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_lon.addAttribute(new Attribute("sdn_parameter_name", "Longitude east"));
				varid_lon.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALONZZ01"));
				varid_lon.addAttribute(new Attribute("sdn_uom_name", "Degrees east"));
				varid_lon.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGE"));
				varid_lon.addAttribute(new Attribute("ancillary_variables", "POSITION_SEADATANET_QC"));

				// crs
				Variable varid_crs = dataFile.addVariable(null, "crs", DataType.SHORT, "TIME");
				varid_crs.addAttribute(new Attribute("grid_mapping_name", "latitude_longitude"));
				varid_crs.addAttribute(new Attribute("epsg_code", "EPSG:4326"));
				varid_crs.addAttribute(new Attribute("semi_major_axis", 6378137.0f));
				varid_crs.addAttribute(new Attribute("inverse_flattening", 298.257223563f));

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
				Variable varid_sdncruise = dataFile.addVariable(null, "SDN_CRUISE", DataType.CHAR, dimsTS);
				varid_sdncruise.addAttribute(new Attribute("long_name", "Grid prouping label"));

				// SDN_STATION
				Variable varid_sdnstation = dataFile.addVariable(null, "SDN_STATION", DataType.CHAR, dimsTP);
				varid_sdnstation.addAttribute(new Attribute("long_name", "Grid label"));

				// SDN_LOCAL_CDI_ID
				Variable varid_sdnlocalcdiid = dataFile.addVariable(null, "SDN_LOCAL_CDI_ID", DataType.CHAR, dimsTId);
				varid_sdnlocalcdiid.addAttribute(new Attribute("long_name", "SeaDataCloud CDI identifier"));
				varid_sdnlocalcdiid.addAttribute(new Attribute("cf_role", "grid_id"));

				// SDN_EDMO_CODE
				Variable varid_sdnedmocode = dataFile.addVariable(null, "SDN_EDMO_CODE", DataType.SHORT, "TIME");
				varid_sdnedmocode.addAttribute(new Attribute("long_name",
						"European Directory of Marine Organisations code for the CDI partner"));

				// SDN_REFERENCES
				Variable varid_sdnreferences = dataFile.addVariable(null, "SDN_REFERENCES", DataType.CHAR, dimsTSdnRef);
				varid_sdnreferences.addAttribute(new Attribute("long_name", "Usage metadata reference"));

				// SDN_XLINK
				Variable varid_sdnxlink = dataFile.addVariable(null, "SDN_XLINK", DataType.CHAR, dimsTRmaxXlink);
				varid_sdnxlink.addAttribute(new Attribute("long_name", "External resource linkages"));

				// Add data variables
				//
				// radial_sea_water_velocity_away_from_instrument:
				// A velocity is a vector quantity. Radial velocity away from
				// instrument
				// means the component of the velocity along the line of sight
				// of the
				// instrument where positive implies movement away from the
				// instrument (i.e.
				// outward). The "instrument" (examples are radar and lidar) is
				// the device
				// used to make an observation.
				Variable varid_speed = dataFile.addVariable(null, "RDVA", DataType.FLOAT, dimsTDBR);
				varid_speed.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-10), new Float(10))));
				varid_speed
						.addAttribute(new Attribute("standard_name", "radial_sea_water_velocity_away_from_instrument"));
				varid_speed.addAttribute(new Attribute("units", "m s-1"));
				varid_speed.addAttribute(new Attribute("long_name", "Radial Sea Water Velocity Away From Instrument"));
				varid_speed.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_speed.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_speed.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_speed.addAttribute(new Attribute("sdn_parameter_name",
						"Current speed (Eulerian) in the water body by directional range-gated radar"));
				varid_speed.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::LCSAWVRD"));
				varid_speed.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_speed.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_speed.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_speed.addAttribute(
						new Attribute("ancillary_variables", "QCflag, OWTR_QC, MDFL_QC, CSPD_QC, RDCT_QC"));

				// (radial current) direction
				//
				// direction_of_radial_vector_away_from_instrument:
				// The direction_of_radial_vector_away_from_instrument is the
				// direction in
				// which the instrument itself is pointing. The direction is
				// measured
				// positive clockwise from due north. The "instrument" (examples
				// are radar
				// and lidar) is the device used to make an observation.
				// "direction_of_X"
				// means direction of a vector, a bearing.
				Variable varid_direction = dataFile.addVariable(null, "DRVA", DataType.FLOAT, dimsTDBR);
				varid_direction
						.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(0f), new Float(360f))));
				varid_direction.addAttribute(
						new Attribute("standard_name", "direction_of_radial_vector_away_from_instrument"));
				varid_direction
						.addAttribute(new Attribute("long_name", "Direction Of Radial Vector Away From Instrument"));
				varid_direction.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_direction.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_direction.addAttribute(new Attribute("units", "degrees_true"));
				varid_direction.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_direction.addAttribute(new Attribute("sdn_parameter_name",
						"Current direction (Eulerian) in the water body by directional range-gated radar"));
				varid_direction.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::LCDAWVRD"));
				varid_direction.addAttribute(new Attribute("sdn_uom_name", "Degrees True"));
				varid_direction.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UABB"));
				varid_direction.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_direction.addAttribute(
						new Attribute("ancillary_variables", "QCflag, OWTR_QC, MDFL_QC, AVRB_QC, RDCT_QC"));

				// u
				Variable varid_u = dataFile.addVariable(null, "EWCT", DataType.FLOAT, dimsTDBR);
				varid_u.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-10), new Float(10))));
				varid_u.addAttribute(new Attribute("standard_name", "surface_eastward_sea_water_velocity"));
				varid_u.addAttribute(new Attribute("long_name", "Surface Eastward Sea Water Velocity"));
				varid_u.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_u.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_u.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_u.addAttribute(new Attribute("units", "m s-1"));
				varid_u.addAttribute(
						new Attribute("sdn_parameter_name", "Eastward current velocity in the water body"));
				varid_u.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::LCEWZZ01"));
				varid_u.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_u.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_u.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_u.addAttribute(new Attribute("ancillary_variables",
						"QCflag, OWTR_QC, MDFL_QC, CSPD_QC, VART_QC, AVRB_QC, RDCT_QC"));

				// v
				Variable varid_v = dataFile.addVariable(null, "NSCT", DataType.FLOAT, dimsTDBR);
				varid_v.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-10), new Float(10))));
				varid_v.addAttribute(new Attribute("standard_name", "surface_northward_sea_water_velocity"));
				varid_v.addAttribute(new Attribute("long_name", "Surface Northward Sea Water Velocity"));
				varid_v.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_v.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_v.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_v.addAttribute(new Attribute("units", "m s-1"));
				varid_v.addAttribute(
						new Attribute("sdn_parameter_name", "Northward current velocity in the water body"));
				varid_v.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::LCNSZZ01"));
				varid_v.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_v.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_v.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_v.addAttribute(new Attribute("ancillary_variables",
						"QCflag, OWTR_QC, MDFL_QC, CSPD_QC, VART_QC, AVRB_QC, RDCT_QC"));

				// Spatial Quality
				Variable varid_espc = dataFile.addVariable(null, "ESPC", DataType.FLOAT, dimsTDBR);
				varid_espc.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-1000), new Float(1000))));
				varid_espc.addAttribute(new Attribute("long_name",
						"Radial Standard Deviation of Current Velocity over the Scatter Patch"));
				varid_espc.addAttribute(new Attribute("units", "m s-1"));
				varid_espc.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_espc.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_espc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_espc.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_espc.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_espc.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_espc.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_espc.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_espc.addAttribute(new Attribute("ancillary_variables", "QCflag, VART_QC"));

				// Temporal Quality
				Variable varid_etmp = dataFile.addVariable(null, "ETMP", DataType.FLOAT, dimsTDBR);
				varid_etmp.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-1000), new Float(1000))));
				varid_etmp.addAttribute(new Attribute("long_name",
						"Radial Standard Deviation of Current Velocity over Coverage Period"));
				varid_etmp.addAttribute(new Attribute("units", "m s-1"));
				varid_etmp.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_etmp.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_etmp.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_etmp.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_etmp.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_etmp.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_etmp.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_etmp.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_etmp.addAttribute(new Attribute("ancillary_variables", "QCflag, VART_QC"));

				// Velocity Maximum
				Variable varid_maxv = dataFile.addVariable(null, "MAXV", DataType.FLOAT, dimsTDBR);
				varid_maxv.addAttribute(
						new Attribute("long_name", "Radial Sea Water Velocity Away From Instrument Maximum"));
				varid_maxv.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-10), new Float(10))));
				varid_maxv.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_maxv.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_maxv.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_maxv.addAttribute(new Attribute("units", "m s-1"));
				varid_maxv.addAttribute(new Attribute("sdn_parameter_name",
						"Current speed (Eulerian) in the water body by directional range-gated radar"));
				varid_maxv.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::LCSAWVRD"));
				varid_maxv.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_maxv.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_maxv.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_maxv.addAttribute(new Attribute("ancillary_variables", "QCflag, MDFL_QC, CSPD_QC, VART_QC"));

				// Velocity Minimum
				Variable varid_minv = dataFile.addVariable(null, "MINV", DataType.FLOAT, dimsTDBR);
				varid_minv.addAttribute(
						new Attribute("long_name", "Radial Sea Water Velocity Away From Instrument Minimum"));
				varid_minv.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-10), new Float(10))));
				varid_minv.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_minv.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_minv.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_minv.addAttribute(new Attribute("units", "m s-1"));
				varid_minv.addAttribute(new Attribute("sdn_parameter_name",
						"Current speed (Eulerian) in the water body by directional range-gated radar"));
				varid_minv.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::LCSAWVRD"));
				varid_minv.addAttribute(new Attribute("sdn_uom_name", "Metres per second"));
				varid_minv.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UVAA"));
				varid_minv.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_minv.addAttribute(new Attribute("ancillary_variables", "QCflag, MDFL_QC, CSPD_QC, VART_QC"));

				// Spatial Count
				Variable varid_ersc = dataFile.addVariable(null, "ERSC", DataType.SHORT, dimsTDBR);
				varid_ersc.addAttribute(new Attribute("long_name", "Radial Sea Water Velocity Spatial Quality Count"));
				varid_ersc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 127))));
				varid_ersc.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_ersc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_ersc.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_ersc.addAttribute(new Attribute("units", "1"));
				varid_ersc.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_ersc.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_ersc.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_ersc.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));
				varid_ersc.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_ersc.addAttribute(new Attribute("ancillary_variables", "QCflag"));

				// Temporal Count
				Variable varid_ertc = dataFile.addVariable(null, "ERTC", DataType.SHORT, dimsTDBR);
				varid_ertc.addAttribute(new Attribute("long_name", "Radial Sea Water Velocity Temporal Quality Count"));
				varid_ertc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 127))));
				varid_ertc.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_ertc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_ertc.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_ertc.addAttribute(new Attribute("units", "1"));
				varid_ertc.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_ertc.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_ertc.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_ertc.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));
				varid_ertc.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_ertc.addAttribute(new Attribute("ancillary_variables", "QCflag"));

				// X-Distance
				Variable varid_xdst = dataFile.addVariable(null, "XDST", DataType.FLOAT, dimsBR);
				varid_xdst.addAttribute(new Attribute("long_name", "Eastward Distance From Instrument"));
				varid_xdst.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(0), new Float(1000))));
				varid_xdst.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_xdst.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_xdst.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_xdst.addAttribute(new Attribute("units", "km"));
				varid_xdst.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_xdst.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_xdst.addAttribute(new Attribute("sdn_uom_name", "Kilometres"));
				varid_xdst.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::ULKM"));
				varid_xdst.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_xdst.addAttribute(
						new Attribute("ancillary_variables", "QCflag, OWTR_QC, MDFL_QC, CSPD_QC, VART_QC"));

				// Y-Distance
				Variable varid_ydst = dataFile.addVariable(null, "YDST", DataType.FLOAT, dimsBR);
				varid_ydst.addAttribute(new Attribute("long_name", "Northward Distance From Instrument"));
				varid_ydst.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(0), new Float(1000))));
				varid_ydst.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_ydst.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_ydst.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_ydst.addAttribute(new Attribute("units", "km"));
				varid_ydst.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_ydst.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_ydst.addAttribute(new Attribute("sdn_uom_name", "Kilometres"));
				varid_ydst.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::ULKM"));
				varid_ydst.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_ydst.addAttribute(
						new Attribute("ancillary_variables", "QCflag, OWTR_QC, MDFL_QC, CSPD_QC, VART_QC"));

				// Spectra Range Cell
				Variable varid_sprc = dataFile.addVariable(null, "SPRC", DataType.BYTE, dimsTDBR);
				varid_sprc.addAttribute(
						new Attribute("long_name", "Radial Sea Water Velocity Cross Spectral Range Cell"));
				varid_sprc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Byte((byte) 0), new Byte((byte) 127))));
				varid_sprc.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_BYTE));
				varid_sprc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_sprc.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_sprc.addAttribute(new Attribute("units", "1"));
				varid_sprc.addAttribute(new Attribute("coordinates", "LONGITUDE LATITUDE"));
				varid_sprc.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_sprc.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_sprc.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_sprc.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));
				varid_sprc.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));
				varid_sprc.addAttribute(
						new Attribute("ancillary_variables", "QCflag, OWTR_QC, MDFL_QC, CSPD_QC, VART_QC"));

				// Number of receive antennas
				Variable varid_narx = dataFile.addVariable(null, "NARX", DataType.SHORT, "TIME");
				varid_narx.addAttribute(new Attribute("long_name", "Number of Receive Antennas"));
				varid_narx.addAttribute(new Attribute("valid_range",
						Arrays.asList(new Short((short) 0), new Short((short) CodarRadialToNetCDF.MAX_SITE))));
				varid_narx.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_narx.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_narx.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_narx.addAttribute(new Attribute("units", "1"));
				varid_narx.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_narx.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_narx.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_narx.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));

				// Number of transmit antennas
				Variable varid_natx = dataFile.addVariable(null, "NATX", DataType.SHORT, "TIME");
				varid_natx.addAttribute(new Attribute("long_name", "Number of Transmit Antennas"));
				varid_natx.addAttribute(new Attribute("valid_range",
						Arrays.asList(new Short((short) 0), new Short((short) CodarRadialToNetCDF.MAX_SITE))));
				varid_natx.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_natx.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_natx.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_natx.addAttribute(new Attribute("units", "1"));
				varid_natx.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_natx.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_natx.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_natx.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));

				// Receive antenna latitudes
				Variable varid_sltr = dataFile.addVariable(null, "SLTR", DataType.FLOAT, dimsTM);
				varid_sltr.addAttribute(new Attribute("long_name", "Receive Antenna Latitudes"));
				varid_sltr.addAttribute(new Attribute("standard_name", "latitude"));
				varid_sltr.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-180), new Float(180))));
				varid_sltr.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_sltr.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_sltr.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_sltr.addAttribute(new Attribute("units", "degrees_north"));
				varid_sltr.addAttribute(new Attribute("sdn_parameter_name", "Latitude north"));
				varid_sltr.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALATZZ01"));
				varid_sltr.addAttribute(new Attribute("sdn_uom_name", "Degrees north"));
				varid_sltr.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGN"));
				varid_sltr.addAttribute(new Attribute("coordinates", "TIME MAXSITE"));

				// Receive antenna longitudes
				Variable varid_slnr = dataFile.addVariable(null, "SLNR", DataType.FLOAT, dimsTM);
				varid_slnr.addAttribute(new Attribute("long_name", "Receive Antenna Longitudes"));
				varid_slnr.addAttribute(new Attribute("standard_name", "longitude"));
				varid_slnr.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-90), new Float(90))));
				varid_slnr.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_slnr.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_slnr.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_slnr.addAttribute(new Attribute("units", "degrees_east"));
				varid_slnr.addAttribute(new Attribute("sdn_parameter_name", "Longitude east"));
				varid_slnr.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALONZZ01"));
				varid_slnr.addAttribute(new Attribute("sdn_uom_name", "Degrees east"));
				varid_slnr.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGE"));
				varid_slnr.addAttribute(new Attribute("coordinates", "TIME MAXSITE"));

				// Transmit antenna latitudes
				Variable varid_sltt = dataFile.addVariable(null, "SLTT", DataType.FLOAT, dimsTM);
				varid_sltt.addAttribute(new Attribute("long_name", "Transmit Antenna Latitudes"));
				varid_sltt.addAttribute(new Attribute("standard_name", "latitude"));
				varid_sltt.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-180), new Float(180))));
				varid_sltt.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_sltt.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_sltt.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_sltt.addAttribute(new Attribute("units", "degrees_north"));
				varid_sltt.addAttribute(new Attribute("sdn_parameter_name", "Latitude north"));
				varid_sltt.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALATZZ01"));
				varid_sltt.addAttribute(new Attribute("sdn_uom_name", "Degrees north"));
				varid_sltt.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGN"));
				varid_sltt.addAttribute(new Attribute("coordinates", "TIME MAXSITE"));

				// Transmit antenna longitudes
				Variable varid_slnt = dataFile.addVariable(null, "SLNT", DataType.FLOAT, dimsTM);
				varid_slnt.addAttribute(new Attribute("long_name", "Transmit Antenna Longitudes"));
				varid_slnt.addAttribute(new Attribute("standard_name", "longitude"));
				varid_slnt.addAttribute(new Attribute("valid_range", Arrays.asList(new Float(-90), new Float(90))));
				varid_slnt.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_FLOAT));
				varid_slnt.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_slnt.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_slnt.addAttribute(new Attribute("units", "degrees_east"));
				varid_slnt.addAttribute(new Attribute("sdn_parameter_name", "Longitude east"));
				varid_slnt.addAttribute(new Attribute("sdn_parameter_urn", "SDN:P01::ALONZZ01"));
				varid_slnt.addAttribute(new Attribute("sdn_uom_name", "Degrees east"));
				varid_slnt.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::DEGE"));
				varid_slnt.addAttribute(new Attribute("coordinates", "TIME MAXSITE"));

				// Receive antenna codes
				Variable varid_scdr = dataFile.addVariable(null, "SCDR", DataType.CHAR, dimsTMS4);
				varid_scdr.addAttribute(new Attribute("long_name", "Receive Antenna Codes"));
				varid_scdr.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_scdr.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_scdr.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_scdr.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));

				// Transmit antenna codes
				Variable varid_scdt = dataFile.addVariable(null, "SCDT", DataType.CHAR, dimsTMS4);
				varid_scdt.addAttribute(new Attribute("long_name", "Transmit Antenna Codes"));
				varid_scdt.addAttribute(new Attribute("sdn_parameter_name", ""));
				varid_scdt.addAttribute(new Attribute("sdn_parameter_urn", ""));
				varid_scdt.addAttribute(new Attribute("sdn_uom_name", "Dimensionless"));
				varid_scdt.addAttribute(new Attribute("sdn_uom_urn", "SDN:P06::UUUU"));

				// Add QC variables
				// Time QC Flag
				Variable varid_tqc = dataFile.addVariable(null, "TIME_SEADATANET_QC", DataType.SHORT, "TIME");
				varid_tqc.addAttribute(new Attribute("long_name", "Time SeaDataNet quality flag"));
				varid_tqc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 9))));
				varid_tqc.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Short((short) 0), new Short((short) 1), new Short((short) 2),
								new Short((short) 3), new Short((short) 4), new Short((short) 7), new Short((short) 8),
								new Short((short) 9))));
				varid_tqc.addAttribute(new Attribute("flag_meanings",
						"unknown good_data probably_good_data potentially_correctable_bad_data bad_data nominal_value interpolated_value missing_value"));
				varid_tqc
						.addAttribute(new Attribute("comment", "OceanSITES quality flagging for temporal coordinate."));
				varid_tqc.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_tqc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_tqc.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_tqc.addAttribute(new Attribute("units", "1"));

				// Position QC Flag
				Variable varid_posqc = dataFile.addVariable(null, "POSITION_SEADATANET_QC", DataType.SHORT, dimsTDBR);
				varid_posqc.addAttribute(new Attribute("long_name", "Position SeaDataNet quality flag"));
				varid_posqc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 9))));
				varid_posqc.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Short((short) 0), new Short((short) 1), new Short((short) 2),
								new Short((short) 3), new Short((short) 4), new Short((short) 7), new Short((short) 8),
								new Short((short) 9))));
				varid_posqc.addAttribute(new Attribute("flag_meanings",
						"unknown good_data probably_good_data potentially_correctable_bad_data bad_data nominal_value interpolated_value missing_value"));
				varid_posqc
						.addAttribute(new Attribute("comment", "OceanSITES quality flagging for position coordinates"));
				varid_posqc.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_posqc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_posqc.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_posqc.addAttribute(new Attribute("units", "1"));
				varid_posqc.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));

				// Depth QC Flag
				Variable varid_dqc = dataFile.addVariable(null, "DEPTH_SEADATANET_QC", DataType.SHORT, "TIME");
				varid_dqc.addAttribute(new Attribute("long_name", "Depth SeaDataNet quality flag"));
				varid_dqc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 9))));
				varid_dqc.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Short((short) 0), new Short((short) 1), new Short((short) 2),
								new Short((short) 3), new Short((short) 4), new Short((short) 7), new Short((short) 8),
								new Short((short) 9))));
				varid_dqc.addAttribute(new Attribute("flag_meanings",
						"unknown good_data probably_good_data potentially_correctable_bad_data bad_data nominal_value interpolated_value missing_value"));
				varid_dqc.addAttribute(new Attribute("comment", "OceanSITES quality flagging for depth coordinate."));
				varid_dqc.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_dqc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_dqc.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_dqc.addAttribute(new Attribute("units", "1"));

				// Overal QC flag
				Variable varid_ovqc = dataFile.addVariable(null, "QCflag", DataType.SHORT, dimsTDBR);
				varid_ovqc.addAttribute(new Attribute("long_name", "Overall Quality Flags"));
				varid_ovqc.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 2048))));
				varid_ovqc.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Short((short) 0), new Short((short) 1), new Short((short) 2),
								new Short((short) 3), new Short((short) 4), new Short((short) 7), new Short((short) 8),
								new Short((short) 9))));
				varid_ovqc.addAttribute(new Attribute("flag_meanings",
						"unknown good_data probably_good_data potentially_correctable_bad_data bad_data nominal_value interpolated_value missing_value"));
				varid_ovqc.addAttribute(new Attribute("comment", "OceanSITES quality flagging for all QC tests."));
				varid_ovqc.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_ovqc.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_ovqc.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_ovqc.addAttribute(new Attribute("units", "1"));
				varid_ovqc.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));

				// Over-water QC Flag
				Variable varid_owtr = dataFile.addVariable(null, "OWTR_QC", DataType.SHORT, dimsTDBR);
				varid_owtr.addAttribute(new Attribute("long_name", "Over-water Quality Flags"));
				varid_owtr.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 2048))));
				varid_owtr.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Short((short) 0), new Short((short) 1), new Short((short) 2),
								new Short((short) 3), new Short((short) 4), new Short((short) 7), new Short((short) 8),
								new Short((short) 9))));
				varid_owtr.addAttribute(new Attribute("flag_meanings",
						"unknown good_data probably_good_data potentially_correctable_bad_data bad_data nominal_value interpolated_value missing_value"));
				varid_owtr
						.addAttribute(new Attribute("comment", "OceanSITES quality flagging for Over-water QC test."));
				varid_owtr.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_owtr.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_owtr.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_owtr.addAttribute(new Attribute("units", "1"));
				varid_owtr.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));

				// Median Filter QC Flag
				Variable varid_mdfl = dataFile.addVariable(null, "MDFL_QC", DataType.SHORT, dimsTDBR);
				varid_mdfl.addAttribute(new Attribute("long_name", "Median Filter Quality Flags"));
				varid_mdfl.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 2048))));
				varid_mdfl.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Short((short) 0), new Short((short) 1), new Short((short) 2),
								new Short((short) 3), new Short((short) 4), new Short((short) 7), new Short((short) 8),
								new Short((short) 9))));
				varid_mdfl.addAttribute(new Attribute("flag_meanings",
						"unknown good_data probably_good_data potentially_correctable_bad_data bad_data nominal_value interpolated_value missing_value"));
				varid_mdfl.addAttribute(new Attribute("comment",
						"OceanSITES quality flagging for Median Filter QC test. Threshold set to "
								+ bean.getRadialTest().getMedianFilter() + " m/s."));
				varid_mdfl.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_mdfl.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_mdfl.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_mdfl.addAttribute(new Attribute("units", "1"));
				varid_mdfl.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));

				// Variance Threshold QC Flag
				Variable varid_vart = dataFile.addVariable(null, "VART_QC", DataType.SHORT, dimsTDBR);
				// netcdf4 compreison netcdf.defVarDeflate(ncid, varid_vart,
				// true, true, 6);
				varid_vart.addAttribute(new Attribute("long_name", "Variance Threshold Quality Flags"));
				varid_vart.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 2048))));
				varid_vart.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Short((short) 0), new Short((short) 1), new Short((short) 2),
								new Short((short) 3), new Short((short) 4), new Short((short) 7), new Short((short) 8),
								new Short((short) 9))));
				varid_vart.addAttribute(new Attribute("flag_meanings",
						"unknown good_data probably_good_data potentially_correctable_bad_data bad_data nominal_value interpolated_value missing_value"));
				varid_vart.addAttribute(new Attribute("comment",
						"OceanSITES quality flagging for Variance Threshold QC test. Test not applicable "
						+ "to Direction Finding Systems. "
						+ "The Temporal Derivative test is applied. Threshold set to "
						+ bean.getRadialTest().getVarianceThreshold() + " m2/s2."));
				varid_vart.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_vart.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_vart.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_vart.addAttribute(new Attribute("units", "1"));
				varid_vart.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));

				// Velocity Threshold QC Flag
				Variable varid_cspd = dataFile.addVariable(null, "CSPD_QC", DataType.SHORT, dimsTDBR);
				varid_cspd.addAttribute(new Attribute("long_name", "Velocity Threshold Quality Flags"));
				varid_cspd.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 2048))));
				varid_cspd.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Short((short) 0), new Short((short) 1), new Short((short) 2),
								new Short((short) 3), new Short((short) 4), new Short((short) 7), new Short((short) 8),
								new Short((short) 9))));
				varid_cspd.addAttribute(new Attribute("flag_meanings",
						"unknown good_data probably_good_data potentially_correctable_bad_data bad_data nominal_value interpolated_value missing_value"));
				varid_cspd.addAttribute(new Attribute("comment",
						"OceanSITES quality flagging for Velocity Threshold QC test. Threshold set to "
								+ bean.getRadialTest().getVeloThreshold() + " m/s."));
				varid_cspd.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_cspd.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_cspd.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_cspd.addAttribute(new Attribute("units", "1"));
				varid_cspd.addAttribute(new Attribute("coordinates", "TIME DEPH LATITUDE LONGITUDE"));

				// Average Radial Bearing QC Flag
				Variable varid_avrb = dataFile.addVariable(null, "AVRB_QC", DataType.SHORT, "TIME");
				varid_avrb.addAttribute(new Attribute("long_name", "Average Radial Bearing Quality Flags"));
				varid_avrb.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 2048))));
				varid_avrb.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Short((short) 0), new Short((short) 1), new Short((short) 2),
								new Short((short) 3), new Short((short) 4), new Short((short) 7), new Short((short) 8),
								new Short((short) 9))));
				varid_avrb.addAttribute(new Attribute("flag_meanings",
						"unknown good_data probably_good_data potentially_correctable_bad_data bad_data nominal_value interpolated_value missing_value"));
				varid_avrb.addAttribute(new Attribute("comment",
						"OceanSITES quality flagging for Average Radial Bearing QC test. Threshold set between "
								+ bean.getRadialTest().getAvRadialBearingMin() + " and "
								+ bean.getRadialTest().getAvRadialBearingMax() + " deg."));
				varid_avrb.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_avrb.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_avrb.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_avrb.addAttribute(new Attribute("units", "1"));

				// Radial Count QC Flag
				Variable varid_rdct = dataFile.addVariable(null, "RDCT_QC", DataType.SHORT, "TIME");
				varid_rdct.addAttribute(new Attribute("long_name", "Radial Count Quality Flag"));
				varid_rdct.addAttribute(
						new Attribute("valid_range", Arrays.asList(new Short((short) 0), new Short((short) 2048))));
				varid_rdct.addAttribute(new Attribute("flag_values",
						Arrays.asList(new Short((short) 0), new Short((short) 1), new Short((short) 2),
								new Short((short) 3), new Short((short) 4), new Short((short) 7), new Short((short) 8),
								new Short((short) 9))));
				varid_rdct.addAttribute(new Attribute("flag_meanings",
						"unknown good_data probably_good_data potentially_correctable_bad_data bad_data nominal_value interpolated_value missing_value"));
				varid_rdct.addAttribute(new Attribute("comment",
						"OceanSITES quality flagging for Radial Count QC test. Thresholds set to"
								+ bean.getRadialTest().getRadialCount() + " vectors."));
				varid_rdct.addAttribute(new Attribute("FillValue", ucar.nc2.iosp.netcdf3.N3iosp.NC_FILL_SHORT));
				varid_rdct.addAttribute(new Attribute("scale_factor", Arrays.asList(new Float(1))));
				varid_rdct.addAttribute(new Attribute("add_offset", Arrays.asList(new Float(0))));
				varid_rdct.addAttribute(new Attribute("units", "1"));

				// MANDATORY ATTRIBUTES
				// Discovery and Identification
				dataFile.addGroupAttribute(null, new Attribute("site_code", site_code));
				dataFile.addGroupAttribute(null, new Attribute("platform_code", platform_code));
				dataFile.addGroupAttribute(null, new Attribute("data_mode", bean.getData_mode()));
				dataFile.addGroupAttribute(null,
						new Attribute("DoA_estimation_method", bean.getStationBean().getDoA_estimation_method()));
				dataFile.addGroupAttribute(null,
						new Attribute("calibration_type", bean.getStationBean().getCalibration_type()));
				dataFile.addGroupAttribute(null, new Attribute("last_calibration_date", bean.getPatternDateUTC()));
				dataFile.addGroupAttribute(null,
						new Attribute("calibration_link", bean.getStationBean().getCalibration_link()));
				dataFile.addGroupAttribute(null, new Attribute("title", bean.getNetworkBean().getTitle()));
				dataFile.addGroupAttribute(null, new Attribute("summary", bean.getNetworkBean().getSummary()));
				dataFile.addGroupAttribute(null, new Attribute("source", bean.getSource()));
				dataFile.addGroupAttribute(null, new Attribute("source_platform_category_code", "17"));
				dataFile.addGroupAttribute(null,
						new Attribute("institution", bean.getNetworkBean().getInstitution_name()));
				dataFile.addGroupAttribute(null,
						new Attribute("institution_edmo_code", bean.getNetworkBean().getEDMO_code()));
				dataFile.addGroupAttribute(null, new Attribute("data_assembly_center", "European HFR Node"));
				dataFile.addGroupAttribute(null, new Attribute("id", id));

				// Geo-spatial-temporal
				dataFile.addGroupAttribute(null, new Attribute("data_type", "HF radar radial data"));
				dataFile.addGroupAttribute(null, new Attribute("feature_type", "surface"));
				dataFile.addGroupAttribute(null,
						new Attribute("geospatial_lat_min", bean.getNetworkBean().getGeospatial_lat_min()));
				dataFile.addGroupAttribute(null,
						new Attribute("geospatial_lat_max", bean.getNetworkBean().getGeospatial_lat_max()));
				dataFile.addGroupAttribute(null,
						new Attribute("geospatial_lon_min", bean.getNetworkBean().getGeospatial_lon_min()));
				dataFile.addGroupAttribute(null,
						new Attribute("geospatial_lon_max", bean.getNetworkBean().getGeospatial_lon_max()));
				dataFile.addGroupAttribute(null, new Attribute("geospatial_vertical_max", "1"));
				dataFile.addGroupAttribute(null, new Attribute("geospatial_vertical_min", "0"));
				dataFile.addGroupAttribute(null, new Attribute("time_coverage_start", bean.getTime_coverage_start()));
				dataFile.addGroupAttribute(null, new Attribute("time_coverage_end", bean.getTime_coverage_end()));

				dataFile.addGroupAttribute(null,
						new Attribute("geospatial_lat_units", "degrees_north"));
				dataFile.addGroupAttribute(null,
						new Attribute("geospatial_lon_units", "degrees_east"));

				// Conventions used
				dataFile.addGroupAttribute(null, new Attribute("format_version", "v2.1"));
				dataFile.addGroupAttribute(null, new Attribute("Conventions",
						"CF-1.6, OceanSITES-Manual-1.2, Copernicus-InSituTAC-SRD-1.4, CopernicusInSituTAC-ParametersList-3.1.0, Unidata, ACDD, INSPIRE"));

				// Publication information
				dataFile.addGroupAttribute(null, new Attribute("update_interval", "void"));
				dataFile.addGroupAttribute(null,
						new Attribute("citation", bean.getNetworkBean().getCitation_statement()));
				dataFile.addGroupAttribute(null, new Attribute("distribution_statement",
						"These data follow Copernicus standards; they are public and free of charge. User assumes all risk for use of data. User must display citation in any publication or product using data. User must contact PI prior to any commercial use of data."));
				dataFile.addGroupAttribute(null, new Attribute("publisher_name", bean.getPublisher_name()));
				dataFile.addGroupAttribute(null, new Attribute("publisher_url", bean.getPublisher_url()));
				dataFile.addGroupAttribute(null, new Attribute("publisher_email", bean.getPublisher_email()));
				dataFile.addGroupAttribute(null, new Attribute("license", bean.getNetworkBean().getLicense()));
				dataFile.addGroupAttribute(null,
						new Attribute("acknowledgment", bean.getNetworkBean().getAcknowledgment()));

				// Provenance
				dataFile.addGroupAttribute(null, new Attribute("date_created", bean.getDateCreated()));
				dataFile.addGroupAttribute(null, new Attribute("history", bean.getHistCreated()));
				dataFile.addGroupAttribute(null, new Attribute("date_modified", bean.getDate_modified()));
				dataFile.addGroupAttribute(null, new Attribute("date_update", bean.getDateCreated()));
				dataFile.addGroupAttribute(null, new Attribute("processing_level", "2B"));
				dataFile.addGroupAttribute(null, new Attribute("contributor_name", bean.getContributor_name()));
				dataFile.addGroupAttribute(null, new Attribute("contributor_role", bean.getContributor_role()));
				dataFile.addGroupAttribute(null, new Attribute("contributor_email", bean.getContributor_email()));

				// RECOMMENDED ATTRIBUTES

				// Discovery and Identification
				dataFile.addGroupAttribute(null, new Attribute("project", bean.getNetworkBean().getProject()));
				dataFile.addGroupAttribute(null, new Attribute("naming_authority", bean.getNaming_authority())); // en
																													// el
																													// original
																													// it.cnr.ismar
				dataFile.addGroupAttribute(null, new Attribute("keywords", bean.getKeywords()));
				dataFile.addGroupAttribute(null, new Attribute("keywords_vocabulary", bean.getKeywords_vocabulary()));
				dataFile.addGroupAttribute(null, new Attribute("comment", bean.getNetworkBean().getComment()));
				dataFile.addGroupAttribute(null, new Attribute("data_language", bean.getData_language()));
				dataFile.addGroupAttribute(null, new Attribute("data_character_set", bean.getData_char_set()));
				dataFile.addGroupAttribute(null, new Attribute("metadata_language", bean.getMetadata_language()));
				dataFile.addGroupAttribute(null, new Attribute("metadata_character_set", bean.getMetadata_char_set()));
				dataFile.addGroupAttribute(null, new Attribute("topic_category", bean.getTopic_cat()));
				dataFile.addGroupAttribute(null, new Attribute("network", bean.getNetworkBean().getNetwork_name()));

				// Geo-spatial-temporal TODO not included

				// Conventions used
				dataFile.addGroupAttribute(null, new Attribute("netcdf_version", bean.getNetcdf_version()));
				dataFile.addGroupAttribute(null, new Attribute("netcdf_format", bean.getNetcdf_format()));

				// OTHER ATTRIBUTES
				dataFile.addGroupAttribute(null, new Attribute("metadata_contact", bean.getMetadata_contact()));
				dataFile.addGroupAttribute(null, new Attribute("metadata_date_stamp", bean.getDateCreated()));
				dataFile.addGroupAttribute(null, new Attribute("standard_name_vocabulary", bean.getName_vocabulary()));
				dataFile.addGroupAttribute(null,
						new Attribute("institution_reference", bean.getNetworkBean().getInstitution_website()));
				dataFile.addGroupAttribute(null, new Attribute("software_name", "JRadar"));
				dataFile.addGroupAttribute(null, new Attribute("software_version", "v2.1"));
				dataFile.addGroupAttribute(null, new Attribute("date_issued", bean.getDateCreated()));
				// Creator by Jose Luis Asensio
				dataFile.addGroupAttribute(null, new Attribute("software_about1",
						"This file was created using JRadar software tool, developed by Jose Luis Asensio"));
				dataFile.addGroupAttribute(null, new Attribute("software_about2",
						"To get more info about JRadar, write to txelu_ai@hotmail.com"));

				// Globals sourced from radial file metadata
				if (bean.getUUID() != null && !bean.getUUID().isEmpty() && !bean.getUUID().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("UUID", bean.getUUID()));
				if (bean.getManufacturer() != null && !bean.getManufacturer().isEmpty()
						&& !bean.getManufacturer().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("manufacturer", bean.getManufacturer()));
				if (bean.getRangeStart() != null && !bean.getRangeStart().isEmpty()
						&& !bean.getRangeStart().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("RangeStart", bean.getRangeStart()));
				if (bean.getRangeEnd() != null && !bean.getRangeEnd().isEmpty() && !bean.getRangeEnd().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("RangeEnd", bean.getRangeEnd()));
				if (bean.getRangeResolutionKMeters() != null && !bean.getRangeResolutionKMeters().isEmpty()
						&& !bean.getRangeResolutionKMeters().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("RangeResolutionKMeters", bean.getRangeResolutionKMeters()));
				if (bean.getAntennaBearing() != null && !bean.getAntennaBearing().isEmpty()
						&& !bean.getAntennaBearing().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("AntennaBearing", bean.getAntennaBearing()));
				if (bean.getReferenceBearing() != null && !bean.getReferenceBearing().isEmpty()
						&& !bean.getReferenceBearing().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("ReferenceBearing", bean.getReferenceBearing()));
				if (bean.getAngularResolution() != null && !bean.getAngularResolution().isEmpty()
						&& !bean.getAngularResolution().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("AngularResolution", bean.getAngularResolution()));
				if (bean.getSpatialResolution() != null && !bean.getSpatialResolution().isEmpty()
						&& !bean.getSpatialResolution().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("SpatialResolution", bean.getSpatialResolution()));
				if (bean.getPatternResolution() != null && !bean.getPatternResolution().isEmpty()
						&& !bean.getPatternResolution().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("PatternResolution", bean.getPatternResolution()));
				if (bean.getTransmitCenterFreqMHz() != null && !bean.getTransmitCenterFreqMHz().isEmpty()
						&& !bean.getTransmitCenterFreqMHz().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("TransmitCenterFreqMHz", bean.getTransmitCenterFreqMHz()));
				if (bean.getDopplerResolutionHzPerBin() != null && !bean.getDopplerResolutionHzPerBin().isEmpty()
						&& !bean.getDopplerResolutionHzPerBin().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("DopplerResolutionHzPerBin", bean.getDopplerResolutionHzPerBin()));
				if (bean.getFirstOrderMethod() != null && !bean.getFirstOrderMethod().isEmpty()
						&& !bean.getFirstOrderMethod().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("FirstOrderMethod", bean.getFirstOrderMethod()));
				if (bean.getBraggSmoothingPoints() != null && !bean.getBraggSmoothingPoints().isEmpty()
						&& !bean.getBraggSmoothingPoints().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("BraggSmoothingPoints", bean.getBraggSmoothingPoints()));
				if (bean.getBraggHasSecondOrder() != null && !bean.getBraggHasSecondOrder().isEmpty()
						&& !bean.getBraggHasSecondOrder().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("BraggHasSecondOrder", bean.getBraggHasSecondOrder()));
				if (bean.getRadialBraggPeakDropOff() != null && !bean.getRadialBraggPeakDropOff().isEmpty()
						&& !bean.getRadialBraggPeakDropOff().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("RadialBraggPeakDropOff", bean.getRadialBraggPeakDropOff()));
				if (bean.getRadialBraggPeakNull() != null && !bean.getRadialBraggPeakNull().isEmpty()
						&& !bean.getRadialBraggPeakNull().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("RadialBraggPeakNull", bean.getRadialBraggPeakNull()));
				if (bean.getRadialBraggNoiseThreshold() != null && !bean.getRadialBraggNoiseThreshold().isEmpty()
						&& !bean.getRadialBraggNoiseThreshold().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("RadialBraggNoiseThreshold", bean.getRadialBraggNoiseThreshold()));
				if (bean.getPatternAmplitudeCorrections() != null && !bean.getPatternAmplitudeCorrections().isEmpty()
						&& !bean.getPatternAmplitudeCorrections().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("PatternAmplitudeCorrections", bean.getPatternAmplitudeCorrections()));
				if (bean.getPatternPhaseCorrections() != null && !bean.getPatternPhaseCorrections().isEmpty()
						&& !bean.getPatternPhaseCorrections().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("PatternPhaseCorrections", bean.getPatternPhaseCorrections()));
				if (bean.getPatternAmplitudeCalculations() != null && !bean.getPatternAmplitudeCalculations().isEmpty()
						&& !bean.getPatternAmplitudeCalculations().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("PatternAmplitudeCalculations", bean.getPatternAmplitudeCalculations()));
				if (bean.getPatternPhaseCalculations() != null && !bean.getPatternPhaseCalculations().isEmpty()
						&& !bean.getPatternPhaseCalculations().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("PatternPhaseCalculations", bean.getPatternPhaseCalculations()));
				if (bean.getRadialMusicParameters() != null && !bean.getRadialMusicParameters().isEmpty()
						&& !bean.getRadialMusicParameters().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("RadialMusicParameters", bean.getRadialMusicParameters()));
				if (bean.getMergedCount() != null && !bean.getMergedCount().isEmpty()
						&& !bean.getMergedCount().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("MergedCount", bean.getMergedCount()));
				if (bean.getRadialMinimumMergePoints() != null && !bean.getRadialMinimumMergePoints().isEmpty()
						&& !bean.getRadialMinimumMergePoints().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("RadialMinimumMergePoints", bean.getRadialMinimumMergePoints()));
				if (bean.getFirstOrderCalc() != null && !bean.getFirstOrderCalc().isEmpty()
						&& !bean.getFirstOrderCalc().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("FirstOrderCalc", bean.getFirstOrderCalc()));
				if (bean.getMergeMethod() != null && !bean.getMergeMethod().isEmpty()
						&& !bean.getMergeMethod().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("MergeMethod", bean.getMergeMethod()));
				if (bean.getPatternMethod() != null && !bean.getPatternMethod().isEmpty()
						&& !bean.getPatternMethod().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("PatternMethod", bean.getPatternMethod()));
				if (bean.getTransmitSweepRateHz() != null && !bean.getTransmitSweepRateHz().isEmpty()
						&& !bean.getTransmitSweepRateHz().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("TransmitSweepRateHz", bean.getTransmitSweepRateHz()));
				if (bean.getTransmitBandwidthKHz() != null && !bean.getTransmitBandwidthKHz().isEmpty()
						&& !bean.getTransmitBandwidthKHz().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("TransmitBandwidthKHz", bean.getTransmitBandwidthKHz()));
				if (bean.getSpectraRangeCells() != null && !bean.getSpectraRangeCells().isEmpty()
						&& !bean.getSpectraRangeCells().equals("NaN"))
					dataFile.addGroupAttribute(null, new Attribute("SpectraRangeCells", bean.getSpectraRangeCells()));
				if (bean.getSpectraDopplerCells() != null && !bean.getSpectraDopplerCells().isEmpty()
						&& !bean.getSpectraDopplerCells().equals("NaN"))
					dataFile.addGroupAttribute(null,
							new Attribute("SpectraDopplerCells", bean.getSpectraDopplerCells()));

				// End the definition mode and start writing data
				dataFile.create();

				// write main dimensions bearing and range
				dataFile.write(varid_bearing, CodarUtils.transformCollectionInArray(bearingDims));
				dataFile.write(varid_range, CodarUtils.transformCollectionInArray(rangeDims));

				// time dimension, days from 1950 We create a variable of that
				// date, and calculate de day number.
				Array timeData = Array.factory(DataType.FLOAT, new int[] { 1 });
				Calendar date = bean.getTimeStampAsCalendar();

				SimpleDateFormat codarFormat = new SimpleDateFormat("yyyy MM dd HH mm ss");
				codarFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				Calendar date2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				date2.setTime(codarFormat.parse("1950 01 01 00 00 00"));
				//1950 and divide by millisec, sec, min and hours to get days sice 1950
				timeData.setFloat(0,
						((float) ((date.getTimeInMillis() - date2.getTimeInMillis()) / (1000.0 * 3600 * 24))));
				dataFile.write(varid_t, timeData);
				timeData = null; // delete value to avoid non expected values.

				// depth
				dataFile.write(varid_depth, CodarUtils.transformCollectionInMultidimensionalFloatArray(
						Arrays.asList(new Float(0)), varid_depth.getDimensions()));

				// get lat and lon from table
				List<Float> lond = bean.getTable().getColumnElementsInOrder(TableColumnNames.LOND, codarToNetcdfIndex);
				List<Float> latd = bean.getTable().getColumnElementsInOrder(TableColumnNames.LATD, codarToNetcdfIndex);
				// get origin lat and lon
				float siteLat = (bean.getOriginElementAsFloat(0) == null)
						? Float.parseFloat(bean.getStationBean().getSite_lat()) : bean.getOriginElementAsFloat(0);
				float siteLon = (bean.getOriginElementAsFloat(1) == null)
						? Float.parseFloat(bean.getStationBean().getSite_lon()) : bean.getOriginElementAsFloat(1);

				// Calculate lat and lon values.
				CodarUtils.fillLatLonValues(siteLat, siteLon, latd, lond, rangeDims, bearingDims);
				Array latArray = CodarUtils.transformCollectionInMultidimensionalFloatArray(latd, varid_lat.getDimensions());
				Array lonArray = CodarUtils.transformCollectionInMultidimensionalFloatArray(lond, varid_lon.getDimensions());

				dataFile.write(varid_lat, latArray);
				dataFile.write(varid_lon, lonArray);

				dataFile.write(varid_crs,
						CodarUtils.transformCollectionInMultidimensionalShortArray(Arrays.asList(new Float(0)), dimsT));
				dataFile.write(varid_sdncruise,
						CodarUtils.transformStringIntoArrayChar(site_code, varid_sdncruise.getDimensions()));
				dataFile.write(varid_sdnstation,
						CodarUtils.transformStringIntoArrayChar(platform_code, varid_sdnstation.getDimensions()));
				dataFile.write(varid_sdnedmocode, CodarUtils.transformCollectionInMultidimensionalShortArray(
						Arrays.asList(new Float(bean.getNetworkBean().getEDMO_code_As_Short())), dimsT));
				dataFile.write(varid_sdnlocalcdiid,
						CodarUtils.transformStringIntoArrayChar(id, varid_sdnlocalcdiid.getDimensions()));
				dataFile.write(varid_sdnreferences,
						CodarUtils.transformStringIntoArrayChar(TDS_catalog, varid_sdnreferences.getDimensions()));
				dataFile.write(varid_sdnxlink,
						CodarUtils.transformStringIntoArrayChar(xlink, varid_sdnxlink.getDimensions()));

				// velocity
				Array arrayData = CodarUtils.transformCollectionInMultidimensionalFloatArray(velo,
						varid_speed.getDimensions());
				dataFile.write(varid_speed, arrayData);

				// head
				// Bearing and heading are only reported to 10ths and can be
				// reported as short unsigned integers when scaled. However,
				// Bearing is a
				// coordinate variable and cannot be scaled by CF metadata
				// convention.
				// Also, can only use unsigned when using NetCDF-4 enhansed data
				// model,
				// otherwise w/classic data model you're limited ot signed data
				// types.
				List<Float> head = bean.getTable().getColumnElementsInOrder(TableColumnNames.HEAD, codarToNetcdfIndex);
				// multiplicar por 10 y redondear a short integer?
				// head = head.*10);

				Array arrayHead = CodarUtils.transformCollectionInMultidimensionalFloatArray(head,
						varid_direction.getDimensions());
				dataFile.write(varid_direction, arrayHead);

				// vel_u
				Array arrayVelU = CodarUtils.transformCollectionInMultidimensionalFloatArray(velu, varid_u.getDimensions());
				dataFile.write(varid_u, arrayVelU);

				// vel_v
				Array arrayVelV = CodarUtils.transformCollectionInMultidimensionalFloatArray(velv, varid_v.getDimensions());
				dataFile.write(varid_v, arrayVelV);

				// temporal and spatial quality
				Array spatialQData = CodarUtils.transformCollectionInMultidimensionalFloatArray(spatialQ,
						varid_espc.getDimensions());
				dataFile.write(varid_espc, spatialQData);
				Array temporalQData = CodarUtils.transformCollectionInMultidimensionalFloatArray(tempQ,
						varid_etmp.getDimensions());
				dataFile.write(varid_etmp, temporalQData);

				// temporal and spatial quality
				Array maxVData = CodarUtils.transformCollectionInMultidimensionalFloatArray(maxv,
						varid_maxv.getDimensions());
				dataFile.write(varid_maxv, maxVData);
				Array minVData = CodarUtils.transformCollectionInMultidimensionalFloatArray(minv,
						varid_minv.getDimensions());
				dataFile.write(varid_minv, minVData);

				// ersc-edvc Spatial quality count
				List<Float> erscTemp = bean.getTable().getColumnElementsInOrder(TableColumnNames.ERSC,
						codarToNetcdfIndex);
				List<Float> ersc = new ArrayList<Float>(erscTemp.size());
				if (erscTemp.size() == 0) {
					erscTemp = bean.getTable().getColumnElementsInOrder(TableColumnNames.EDVC, codarToNetcdfIndex);
				}
				// Locate native bad-values
				for (Float er : erscTemp) {
					float erTemp = er;
					if (erTemp == 999)
						erTemp = Float.NaN;
					else if (erTemp > 127)
						erTemp = 127;
					ersc.add(erTemp);
				}
				Array erscData = CodarUtils.transformCollectionInMultidimensionalShortArray(ersc,
						varid_ersc.getDimensions());
				dataFile.write(varid_ersc, erscData);

				// ertc temporal quality count
				List<Float> ertcTemp = bean.getTable().getColumnElementsInOrder(TableColumnNames.ERTC,
						codarToNetcdfIndex);
				List<Float> ertc = new ArrayList<Float>(ertcTemp.size());
				// Locate native bad-values
				for (Float er : ertcTemp) {
					float erTemp = er;
					if (erTemp == 999)
						erTemp = Float.NaN;
					else if (erTemp > 127)
						erTemp = 127;
					ertc.add(erTemp);
				}
				Array ertcData = CodarUtils.transformCollectionInMultidimensionalShortArray(ertc,
						varid_ertc.getDimensions());
				dataFile.write(varid_ertc, ertcData);

				// Eastware distance from instrument
				List<Float> xdst = bean.getTable().getColumnElementsInOrder(TableColumnNames.XDST, codarToNetcdfIndex);
				Array xdstData = CodarUtils.transformCollectionInMultidimensionalFloatArray(xdst,
						varid_xdst.getDimensions());
				dataFile.write(varid_xdst, xdstData);

				// Northward distance from instrument
				List<Float> ydst = bean.getTable().getColumnElementsInOrder(TableColumnNames.YDST, codarToNetcdfIndex);
				Array ydstData = CodarUtils.transformCollectionInMultidimensionalFloatArray(ydst,
						varid_ydst.getDimensions());
				dataFile.write(varid_ydst, ydstData);

				// Cross Spectal Range Cell
				List<Float> sprc = bean.getTable().getColumnElementsInOrder(TableColumnNames.SPRC, codarToNetcdfIndex);
				Array sprcData = CodarUtils.transformCollectionInMultidimensionalByteArray(sprc,
						varid_sprc.getDimensions());
				dataFile.write(varid_sprc, sprcData);

				dataFile.write(varid_narx, CodarUtils.transformCollectionInMultidimensionalShortArray(
						Arrays.asList(new Float(CodarRadialToNetCDF.NUM_SITES)), dimsT));
				dataFile.write(varid_natx, CodarUtils.transformCollectionInMultidimensionalShortArray(
						Arrays.asList(new Float(CodarRadialToNetCDF.NUM_SITES)), dimsT));
				dataFile.write(varid_sltr, CodarUtils
						.transformCollectionInMultidimensionalFloatArray(Arrays.asList(new Float(siteLat)), dimsTM));
				dataFile.write(varid_slnr, CodarUtils
						.transformCollectionInMultidimensionalFloatArray(Arrays.asList(new Float(siteLon)), dimsTM));
				dataFile.write(varid_sltt, CodarUtils
						.transformCollectionInMultidimensionalFloatArray(Arrays.asList(new Float(siteLat)), dimsTM));
				dataFile.write(varid_slnt, CodarUtils
						.transformCollectionInMultidimensionalFloatArray(Arrays.asList(new Float(siteLon)), dimsTM));

				dataFile.write(varid_scdr, CodarUtils.transformStringIntoArrayChar(
						bean.getStationBean().getStation_id(), varid_scdr.getDimensions()));
				dataFile.write(varid_scdt, CodarUtils.transformStringIntoArrayChar(
						bean.getStationBean().getStation_id(), varid_scdt.getDimensions()));

				// QC flags
				dataFile.write(varid_tqc, CodarUtils.transformCollectionInMultidimensionalShortArray(
						Arrays.asList(new Float(CodarRadialToNetCDF.SDN_TIME_QC_FLAG)), dimsT));

				List<Float> sdnPosFlag = new ArrayList<Float>(velu.size());
				for (Float vtemp : velu) {
					if (Float.isNaN(vtemp)) {
						sdnPosFlag.add(Float.NaN);
					} else {
						sdnPosFlag.add(new Float(1));
					}
				}

				dataFile.write(varid_posqc,
						CodarUtils.transformCollectionInMultidimensionalShortArray(sdnPosFlag, dimsTDBR));

				dataFile.write(varid_dqc, CodarUtils.transformCollectionInMultidimensionalShortArray(
						Arrays.asList(new Float(CodarRadialToNetCDF.SDN_DEPTH_QC_FLAG)), dimsT));

				// QA - QC flags
				VentanaRunRadialQualityTests qcqaTest = new VentanaRunRadialQualityTests(bean);

				// OverWater Quality test
				List<Float> overWater_QCflag = qcqaTest.runOverWaterTest(
						bean.getTable().getColumnElementsInOrder(TableColumnNames.VFLG, codarToNetcdfIndex));
				Array array_owtr = CodarUtils.transformCollectionInMultidimensionalShortArray(overWater_QCflag,
						varid_owtr.getDimensions());
				dataFile.write(varid_owtr, array_owtr);

				// medianFilter Quality test
				List<Float> medianFilter_QCflag = qcqaTest.runMedianFilterTest(profile);
				Array array_medianFilter = CodarUtils.transformCollectionInMultidimensionalShortArray(
						medianFilter_QCflag, varid_mdfl.getDimensions());
				dataFile.write(varid_mdfl, array_medianFilter);

				// temporal derivative
				List<Float> tempDerFilter_QCflag = qcqaTest.runTempDerivativeTest(profile);
				Array array_TempDerFilter = CodarUtils.transformCollectionInMultidimensionalShortArray(
						tempDerFilter_QCflag, varid_vart.getDimensions());
				dataFile.write(varid_vart, array_TempDerFilter);

				// Velocity Threshold Quality Test
				List<Float> velocityThreshold_QCflag = qcqaTest.runThresholdTest(velo,
						bean.getRadialTest().getVeloThreshold());
				Array array_velt = CodarUtils.transformCollectionInMultidimensionalShortArray(velocityThreshold_QCflag,
						varid_cspd.getDimensions());
				dataFile.write(varid_cspd, array_velt);

				// Average Radial Bearing Quality Test
				List<Float> averageRadialBearing_QC_flag = bean.getTable()
						.getColumnElementsInOrder(TableColumnNames.HEAD, codarToNetcdfIndex);
				List<Float> avRadialB_QC_flag = qcqaTest.runAverageBearinTest(averageRadialBearing_QC_flag,
						bean.getRadialTest().getAvRadialBearingMin(), bean.getRadialTest().getAvRadialBearingMax());
				Array array_avrb = CodarUtils.transformCollectionInMultidimensionalShortArray(avRadialB_QC_flag,
						varid_avrb.getDimensions());
				dataFile.write(varid_avrb, array_avrb);

				// radial count Quality test
				List<Float> radialCountFilter_QCflag = qcqaTest.runRadialCountTest(profile);
				Array array_radCountFilter = CodarUtils.transformCollectionInMultidimensionalShortArray(
						radialCountFilter_QCflag, varid_rdct.getDimensions());
				dataFile.write(varid_rdct, array_radCountFilter);

				// OverAll QC checks all the QC tests.
				List<Float> overal_QCflag = new ArrayList<Float>(overWater_QCflag.size());
				// warning: avRadialB_QC_flag and radialCountFilter_QCflag, are
				// scalar values not gridded.

				for (int i = 0; i < overWater_QCflag.size(); i++) {
					if (overWater_QCflag.get(i).floatValue() == 1 && radialCountFilter_QCflag.get(0).floatValue() == 1
							&& velocityThreshold_QCflag.get(i).floatValue() == 1
							&& medianFilter_QCflag.get(i).floatValue() == 1
							&& tempDerFilter_QCflag.get(i).floatValue() == 1
							&& avRadialB_QC_flag.get(0).floatValue() == 1) {
						overal_QCflag.add(new Float(1));
					} else if (overWater_QCflag.get(i).floatValue() == 9
							&& radialCountFilter_QCflag.get(0).floatValue() == 9
							&& velocityThreshold_QCflag.get(i).floatValue() == 9
							&& medianFilter_QCflag.get(i).floatValue() == 9
							&& tempDerFilter_QCflag.get(i).floatValue() == 9
							&& avRadialB_QC_flag.get(0).floatValue() == 9) {
						overal_QCflag.add(new Float(9));
					} else if (overWater_QCflag.get(i).floatValue() == 0
							&& radialCountFilter_QCflag.get(0).floatValue() == 0
							&& velocityThreshold_QCflag.get(i).floatValue() == 0
							&& medianFilter_QCflag.get(i).floatValue() == 0
							&& tempDerFilter_QCflag.get(i).floatValue() == 0
							&& avRadialB_QC_flag.get(0).floatValue() == 0) {
						overal_QCflag.add(new Float(0));
					} else if (overWater_QCflag.get(i).isNaN() && velocityThreshold_QCflag.get(i).isNaN()
							&& medianFilter_QCflag.get(i).isNaN() && tempDerFilter_QCflag.get(i).isNaN()) {
						overal_QCflag.add(Float.NaN);
					} else {
						overal_QCflag.add(new Float(4));
					}

				}
				Array array_ovqc = CodarUtils.transformCollectionInMultidimensionalShortArray(overal_QCflag,
						varid_ovqc.getDimensions());
				dataFile.write(varid_ovqc, array_ovqc);

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

	public boolean checkBeanRadialTable() {
		boolean hasError = (this.bean.getTable() == null || this.bean.getTable().isEmpty());

		return hasError;
	}

	/**
	 * return if there is an error with the test parameters
	 * 
	 * @return
	 */
	public boolean checkQCQA() {
		boolean hasError = !bean.getRadialTest().readyToRunTests();

		return hasError;
	}

	public boolean checkBeanMandatoryFields() {
		boolean hasError = true;

		try {
			if (bean.getData_mode() != null && !bean.getData_mode().isEmpty() && bean.getPatternDate() != null
					&& !bean.getPatternDate().isEmpty() && bean.getSource() != null && !bean.getSource().isEmpty()
					&& bean.getTime_coverage_start() != null && !bean.getTime_coverage_start().isEmpty()
					&& bean.getTime_coverage_end() != null && !bean.getTime_coverage_end().isEmpty()
					&& bean.getPublisher_name() != null && !bean.getPublisher_name().isEmpty()
					&& bean.getPublisher_email() != null && !bean.getPublisher_email().isEmpty()
					&& bean.getPublisher_url() != null && !bean.getPublisher_url().isEmpty()
					&& bean.getHistCreated() != null && !bean.getHistCreated().isEmpty()
					&& bean.getContributor_name() != null && !bean.getContributor_name().isEmpty()
					&& bean.getContributor_role() != null && !bean.getContributor_role().isEmpty()
					&& bean.getContributor_email() != null && !bean.getContributor_email().isEmpty()
					&& bean.getAngularResolution() != null && !bean.getAngularResolution().isEmpty()
					&& bean.getAntennaBearing() != null && !bean.getAntennaBearing().isEmpty()
					&& bean.getReferenceBearing() != null && !bean.getReferenceBearing().isEmpty()
					&& bean.getRangeResolutionKMeters() != null && !bean.getRangeResolutionKMeters().isEmpty()
					&& bean.getRangeStart() != null && !bean.getRangeStart().isEmpty() && bean.getRangeEnd() != null
					&& !bean.getRangeEnd().isEmpty() && bean.getMinimunRange() != null
					&& !bean.getMinimunRange().isEmpty() && bean.getMaximunRange() != null
					&& !bean.getMaximunRange().isEmpty() && bean.getMinimunBearing() != null
					&& !bean.getMinimunBearing().isEmpty() && bean.getMaximunBearing() != null
					&& !bean.getMaximunBearing().isEmpty()) {
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
