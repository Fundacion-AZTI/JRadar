/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  03 de abril de 2017
 */
package es.azti.codar.beans;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import es.azti.db.NETWORK_TB;
import ucar.nc2.NetcdfFileWriter;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es) 10 de abril de 2017
 *
 *         Codar total file bean, containing data and parameters that will be
 *         transformed in attributes and metadata for the finla NetCDF file.
 */
public class CodarTotalBean implements Serializable {

	private static final long serialVersionUID = -8419380509546501035L;

	// File Name
	private String fileName;
	private String pathToFile;

	// central node DDBB bean.
	private NETWORK_TB networkBean;

	private String CTF; // CTF
	private String fileType; // FileType
	private String LLUVSpec; // LLUVSpec
	private String UUID; // UUID
	private String manufacturer; // Manufacturer
	private String site; // Site
	private String processedTimeStamp; // ProcessedTimeStamp
	private String processingTool; // ProcessingTool
	private String timeStamp; // TimeStamp
	private String timeZone; // TimeZone
	private String timeCoverage; // TimeCoverage
	private String origin; // Origin
	private String greatCircle; // GreatCircle
	private String geodVersion; // GeodVersion
	private String LLUVTrustData; // LLUVTrustData
	private String combineMethod; // CombineMethod
	private String gridCreatedBy; // GridCreatedBy
	private String gridVersion; // GridVersion
	private String gridTimeStamp; // GridTimeStamp
	private String gridLastModified; // GridLastModified
	private String gridAxisOrientation; // GridAxisOrientation
	private String gridAxisType; // GridAxisType
	private String gridSpacing; // GridSpacing
	private String averagingRadius; // AverageRadius
	private String distanceAngularLimit; // DistanceangularLimit
	private String currentVelocityLimit; // CurrentVelocityLimit

	private String time_coverage_start;
	private String time_coverage_end;
	private String time_coverage_duration = "PT1H";
	private String time_coverage_resolution = "PT1H";

	// netCDF output dimensions
	private String xMin;
	private String xMax;
	private String yMin;
	private String yMax;

	// date formater
	private String dateCreated;
	private String dateCollected;
	private String histCreated;

	private String date_modified;
	private String date_issued;

	private String id;
	private String naming_authority = "";

	private String processing_level = "2A";
	private String license = "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)";
	private String creator_name = "";
	private String creator_email = "";
	private String creator_url = "";
	private String acknowledgment = "";
	private String comments = "";
	private String metadata_convention = "Unidata Dataset Discovery v1.0 compliant. NOAA GNOME format compliant.";
	private String platform;
	private String sensor = "";
	private String citation;
	private String operational_manager = "";
	private String operational_manager_email = "";
	private String format_version = "v1.0";
	private String data_mode = "R";
	private String update_interval = "void";
	private String site_code = "";
	private String area = "";
	private String quality_control = "Level-B: advanced QC";
	private String references = "Netcdf Java Documentation - https://www.unidata.ucar.edu/software/thredds/current/netcdf-java/";
	private String publisher_name = "European HFR node";
	private String publisher_url = "https://azti.sharepoint.com/sites/Proyectos/EUHFRNODE";
	private String publisher_email = "euhfrnode@azti.es";
	private String contributor_name = "";
	private String contributor_role = "";
	private String contributor_email = "";

	private String title = "Near Real Time Surface Ocean Total Velocity";
	private String institution;
	private String conventions = "CF-1.6, Unidata, OceanSITES, ACDD, INSPIRE";
	private String summary;
	private String source = "coastal structure";
	private String network;
	private String keywords = "OCEAN CURRENTS, SURFACE WATER, RADAR, SCR-HF";
	private String keywords_vocabulary = "GCMD Science Keywords";
	// private String history;
	private String data_language = "eng";
	private String data_char_set = "utf8";
	private String topic_cat = "oceans";
	private String reference_sys = "2EPSG:4806";
	private String metadata_language = "eng";
	private String metadata_char_set = "utf8";
	private String metadata_contact = "";
	// private String metadata_date_stamp;

	private String netcdf_version = "4.1.1";
	private String netcdf_format = NetcdfFileWriter.Version.netcdf4_classic.name();
	private String name_vocabulary = "NetCDF Climate and Forecast (CF) Metadata Convention Standard Name Table Version 1.6";

	// SiteSource # Name Lat Lon Coverage(s) RngStep(km) Pattern AntBearing(NCW)
	// SiteSource: 1 MATX 43.4558333 -2.7527167 180.00 5.100 Meas 45.0 Radial
	// SiteSource: 2 HIGE 43.3925667 -1.7957500 180.00 5.100 Meas 38.0 Radial
	private ArrayList<String> siteSource;

	// ATTRIBUTES, additional params. This are stored in a Collection, in order
	// to write in the output file, not to lost information
	private HashMap<String, String> aditionalAttributes;
	// Radial radar information, the data
	private transient CodarDataTableBean table;
	// Radial QAQC test data
	private TotalQCQATestBean totalTest;

	// default date format in CODAR files
	private SimpleDateFormat codarFormat = new SimpleDateFormat("yyyy MM dd HH mm ss");

	/**
	 * @throws ParseException
	 * 
	 */
	public CodarTotalBean() throws ParseException {
		super();
		aditionalAttributes = new HashMap<String, String>();
		siteSource = new ArrayList<String>();
	}

	/**
	 * @return the networkBean
	 */
	public NETWORK_TB getNetworkBean() {
		return networkBean;
	}

	/**
	 * @param networkBean
	 *            the networkBean to set
	 */
	public void setNetworkBean(NETWORK_TB networkBean) {
		this.networkBean = networkBean;
	}

	/**
	 * clear all additional attributes, old values are lost.
	 */
	public void clearAllAdditionalAttributes() {
		this.aditionalAttributes = new HashMap<String, String>();
	}

	/**
	 * 
	 * @param map
	 *            replace existing attributes if present
	 */
	public void setAllAdditionalAttributes(HashMap<String, String> map) {
		this.aditionalAttributes = map;
	}

	/**
	 * 
	 * @return all the aditional Attributes within a HashMap structure
	 */
	public HashMap<String, String> getAllAdditionalAttributes() {
		return aditionalAttributes;
	}

	/**
	 * @param key
	 *            of the required attribute
	 * @return the attribute value required by key
	 */
	public String getAdditionalAttribute(String key) {
		return aditionalAttributes.get(key);
	}

	/**
	 * @param key
	 *            id of the attribute
	 * @param value
	 *            string with the value of the parameter.
	 */
	public void setAdditionalAttribute(String key, String value) {
		aditionalAttributes.put(key, value);
	}

	/**
	 * @return getPathToFile {
	 */
	public String getPathToFile() {
		return pathToFile;
	}

	/**
	 * @para pathToFile the file path to set
	 */
	public void setPathToFile(String pathToFile) {
		this.pathToFile = pathToFile;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the cTF
	 */
	public String getCTF() {
		return CTF;
	}

	/**
	 * @param cTF
	 *            the cTF to set
	 */
	public void setCTF(String cTF) {
		this.CTF = cTF;
	}

	/**
	 * @return the fileType
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * @param fileType
	 *            the fileType to set
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the lLUVSpec
	 */
	public String getLLUVSpec() {
		return LLUVSpec;
	}

	/**
	 * @param lLUVSpec
	 *            the lLUVSpec to set
	 */
	public void setLLUVSpec(String lLUVSpec) {
		this.LLUVSpec = lLUVSpec;
	}

	/**
	 * @return the uUID
	 */
	public String getUUID() {
		return UUID;
	}

	/**
	 * @param uUID
	 *            the uUID to set
	 */
	public void setUUID(String uUID) {
		this.UUID = uUID;
	}

	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		return manufacturer;
	}

	/**
	 * @param manufacturer
	 *            the manufacturer to set
	 */
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	/**
	 * @return the site
	 */
	public String getSite() {
		return site;
	}

	/**
	 * @param site
	 *            the site to set
	 */
	public void setSite(String site) {
		this.site = site;
	}

	/**
	 * @return the timeStamp
	 */
	public String getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @return the timeStamp as Calendar java Class
	 * @throws ParseException
	 */
	public Calendar getTimeStampAsCalendar() throws ParseException {
		codarFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		date.setTime(codarFormat.parse(this.getTimeStamp()));

		return date;
	}

	public String getTimeStampAsUTC() {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date coverageStart = Calendar.getInstance().getTime();
		try {
			coverageStart = codarFormat.parse(getTimeStamp());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dateFormat.format(coverageStart.getTime()) + "T" + hourFormat.format(coverageStart.getTime()) + "Z";

	}

	/**
	 * @param timeStamp
	 *            the timeStamp to set
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * @param timeZone
	 *            the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * @return the timeCoverage
	 */
	public String getTimeCoverage() {
		return timeCoverage;
	}

	/**
	 * @param timeCoverage
	 *            the timeCoverage to set
	 */
	public void setTimeCoverage(String timeCoverage) {
		this.timeCoverage = timeCoverage;
	}

	/**
	 * @return the origin element requested.
	 */
	public String getOriginElement(int pos) {
		String[] elements = origin.trim().split("\\s+");
		if (pos < elements.length) {
			return elements[pos];
		} else {
			return null;
		}
	}

	/**
	 * @return the origin element requested as a double value.
	 */
	public Float getOriginElementAsFloat(int pos) throws NumberFormatException {
		String value = this.getOriginElement(pos);

		if (value != null) {
			return Float.parseFloat(value);
		} else {
			return null;
		}

	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}

	/**
	 * @return the greatCircle
	 */
	public String getGreatCircle() {
		return greatCircle;
	}

	/**
	 * @param greatCircle
	 *            the greatCircle to set
	 */
	public void setGreatCircle(String greatCircle) {
		this.greatCircle = greatCircle;
	}

	/**
	 * @return the geodVersion
	 */
	public String getGeodVersion() {
		return geodVersion;
	}

	/**
	 * @param geodVersion
	 *            the geodVersion to set
	 */
	public void setGeodVersion(String geodVersion) {
		this.geodVersion = geodVersion;
	}

	/**
	 * @return the lLUVTrustData
	 */
	public String getLLUVTrustData() {
		return LLUVTrustData;
	}

	/**
	 * @param lLUVTrustData
	 *            the lLUVTrustData to set
	 */
	public void setLLUVTrustData(String lLUVTrustData) {
		this.LLUVTrustData = lLUVTrustData;
	}

	/**
	 * @return the currentVelocityLimit
	 */
	public String getCurrentVelocityLimitData() {
		String data = this.currentVelocityLimit;
		if (currentVelocityLimit.trim().split("\\s+").length > 1) {
			data = currentVelocityLimit.trim().split("\\s+")[0];
		}

		return data;
	}

	/**
	 * @return the value with no unit, is supposed to be cm/s
	 */
	public String getCurrentVelocityLimit() {
		return currentVelocityLimit;
	}

	/**
	 * @param currentVelocityLimit
	 *            the currentVelocityLimit to set
	 */
	public void setCurrentVelocityLimit(String currentVelocityLimit) {
		this.currentVelocityLimit = currentVelocityLimit;
	}

	/**
	 * @return the table
	 */
	public CodarDataTableBean getTable() {
		return table;
	}

	/**
	 * @param table
	 *            the table to set
	 */
	public void setTable(CodarDataTableBean table) {
		this.table = table;
	}

	/**
	 * @return the totalTest
	 */
	public TotalQCQATestBean getTotalTest() {
		return totalTest;
	}

	/**
	 * @param totalTest
	 *            the totalTest to set
	 */
	public void setTotalTest(TotalQCQATestBean totalTest) {
		this.totalTest = totalTest;
	}

	/**
	 * @return the codarFormat
	 */
	public SimpleDateFormat getCodarFormat() {
		return codarFormat;
	}

	/**
	 * @param codarFormat
	 *            the codarFormat to set
	 */
	public void setCodarFormat(SimpleDateFormat codarFormat) {
		this.codarFormat = codarFormat;
	}

	/**
	 * @return the combineMethod
	 */
	public String getCombineMethod() {
		return combineMethod;
	}

	/**
	 * @param combineMethod
	 *            the combineMethod to set
	 */
	public void setCombineMethod(String combineMethod) {
		this.combineMethod = combineMethod;
	}

	/**
	 * @return the gridCreatedBy
	 */
	public String getGridCreatedBy() {
		return gridCreatedBy;
	}

	/**
	 * @param gridCreatedBy
	 *            the gridCreatedBy to set
	 */
	public void setGridCreatedBy(String gridCreatedBy) {
		this.gridCreatedBy = gridCreatedBy;
	}

	/**
	 * @return the gridVersion
	 */
	public String getGridVersion() {
		return gridVersion;
	}

	/**
	 * @param gridVersion
	 *            the gridVersion to set
	 */
	public void setGridVersion(String gridVersion) {
		this.gridVersion = gridVersion;
	}

	/**
	 * @return the gridTimeStamp
	 */
	public String getGridTimeStamp() {
		return gridTimeStamp;
	}

	/**
	 * @param gridTimeStamp
	 *            the gridTimeStamp to set
	 */
	public void setGridTimeStamp(String gridTimeStamp) {
		this.gridTimeStamp = gridTimeStamp;
	}

	/**
	 * @return the gridLastModified
	 */
	public String getGridLastModified() {
		return gridLastModified;
	}

	/**
	 * @param gridLastModified
	 *            the gridLastModified to set
	 */
	public void setGridLastModified(String gridLastModified) {
		this.gridLastModified = gridLastModified;
	}

	/**
	 * @return the gridAxisOrientation
	 */
	public String getGridAxisOrientation() {
		return gridAxisOrientation;
	}

	/**
	 * @param gridAxisOrientation
	 *            the gridAxisOrientation to set
	 */
	public void setGridAxisOrientation(String gridAxisOrientation) {
		this.gridAxisOrientation = gridAxisOrientation;
	}

	/**
	 * @return the gridAxisType
	 */
	public String getGridAxisType() {
		return gridAxisType;
	}

	/**
	 * @param gridAxisType
	 *            the gridAxisType to set
	 */
	public void setGridAxisType(String gridAxisType) {
		this.gridAxisType = gridAxisType;
	}

	/**
	 * @return the gridSpacing
	 */
	public String getGridSpacing() {
		return gridSpacing;
	}

	/**
	 * @return float the gridSpacing as a float
	 * 
	 */
	public float getGridSpacingAsFloat() throws NumberFormatException {
		Float vuelta = Float.NaN;
		if (this.gridSpacing != null) {
			String gspac = this.gridSpacing;
			if (gridSpacing.trim().split("\\s+").length > 1) {
				gspac = gridSpacing.trim().split("\\s+")[0];
			}
			vuelta = Float.parseFloat(gspac);
		}
		return vuelta;
	}

	/**
	 * @param gridSpacing
	 *            the gridSpacing to set
	 */
	public void setGridSpacing(String gridSpacing) {
		this.gridSpacing = gridSpacing;
	}

	/**
	 * @return the averagingRadius
	 */
	public String getAveragingRadius() {
		return averagingRadius;
	}

	/**
	 * @param averagingRadius
	 *            the averagingRadius to set
	 */
	public void setAveragingRadius(String averagingRadius) {
		this.averagingRadius = averagingRadius;
	}

	/**
	 * @return the distanceAngularLimit
	 */
	public String getDistanceAngularLimit() {
		return distanceAngularLimit;
	}

	/**
	 * @param distanceAngularLimit
	 *            the distanceAngularLimit to set
	 */
	public void setDistanceAngularLimit(String distanceAngularLimit) {
		this.distanceAngularLimit = distanceAngularLimit;
	}

	/**
	 * @return the time_coverage_start
	 */
	public String getTime_coverage_start() {
		return time_coverage_start;
	}

	/**
	 * @param time_coverage_start
	 *            the time_coverage_start to set
	 */
	public void setTime_coverage_start(String time_coverage_start) {
		this.time_coverage_start = time_coverage_start;
	}

	/**
	 * @return the time_coverage_end
	 */
	public String getTime_coverage_end() {
		return time_coverage_end;
	}

	/**
	 * @param time_coverage_end
	 *            the time_coverage_end to set
	 */
	public void setTime_coverage_end(String time_coverage_end) {
		this.time_coverage_end = time_coverage_end;
	}

	/**
	 * @return the time_coverage_duration
	 */
	public String getTime_coverage_duration() {
		return time_coverage_duration;
	}

	/**
	 * @param time_coverage_duration
	 *            the time_coverage_duration to set
	 */
	public void setTime_coverage_duration(String time_coverage_duration) {
		this.time_coverage_duration = time_coverage_duration;
	}

	/**
	 * @return the time_coverage_resolution
	 */
	public String getTime_coverage_resolution() {
		return time_coverage_resolution;
	}

	/**
	 * @param time_coverage_resolution
	 *            the time_coverage_resolution to set
	 */
	public void setTime_coverage_resolution(String time_coverage_resolution) {
		this.time_coverage_resolution = time_coverage_resolution;
	}

	/**
	 * @return the dateCreated
	 */
	public String getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated
	 *            the dateCreated to set
	 */
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the dateCollected
	 */
	public String getDateCollected() {
		return dateCollected;
	}

	/**
	 * @param dateCollected
	 *            the dateCollected to set
	 */
	public void setDateCollected(String dateCollected) {
		this.dateCollected = dateCollected;
	}

	/**
	 * @return the histCreated
	 */
	public String getHistCreated() {
		return histCreated;
	}

	/**
	 * @param histCreated
	 *            the histCreated to set
	 */
	public void setHistCreated(String histCreated) {
		this.histCreated = histCreated;
	}

	/**
	 * @return the date_modified
	 */
	public String getDate_modified() {
		return date_modified;
	}

	/**
	 * @param date_modified
	 *            the date_modified to set
	 */
	public void setDate_modified(String date_modified) {
		this.date_modified = date_modified;
	}

	/**
	 * @return the date_issued
	 */
	public String getDate_issued() {
		return date_issued;
	}

	/**
	 * @param date_issued
	 *            the date_issued to set
	 */
	public void setDate_issued(String date_issued) {
		this.date_issued = date_issued;
	}

	/**
	 * @return the creator_name
	 */
	public String getCreator_name() {
		return creator_name;
	}

	/**
	 * @param creator_name
	 *            the creator_name to set
	 */
	public void setCreator_name(String creator_name) {
		this.creator_name = creator_name;
	}

	/**
	 * @return the creator_email
	 */
	public String getCreator_email() {
		return creator_email;
	}

	/**
	 * @param creator_email
	 *            the creator_email to set
	 */
	public void setCreator_email(String creator_email) {
		this.creator_email = creator_email;
	}

	/**
	 * @return the creator_url
	 */
	public String getCreator_url() {
		return creator_url;
	}

	/**
	 * @param creator_url
	 *            the creator_url to set
	 */
	public void setCreator_url(String creator_url) {
		this.creator_url = creator_url;
	}

	/**
	 * @return the acknowledgment
	 */
	public String getAcknowledgment() {
		return acknowledgment;
	}

	/**
	 * @param acknowledgment
	 *            the acknowledgment to set
	 */
	public void setAcknowledgment(String acknowledgment) {
		this.acknowledgment = acknowledgment;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the metadata_convention
	 */
	public String getMetadata_convention() {
		return metadata_convention;
	}

	/**
	 * @param metadata_convention
	 *            the metadata_convention to set
	 */
	public void setMetadata_convention(String metadata_convention) {
		this.metadata_convention = metadata_convention;
	}

	/**
	 * @return the platform
	 */
	public String getPlatform() {
		return platform;
	}

	/**
	 * @param platform
	 *            the platform to set
	 */
	public void setPlatform(String platform) {
		this.platform = platform;
	}

	/**
	 * @return the sensor
	 */
	public String getSensor() {
		return sensor;
	}

	/**
	 * @param sensor
	 *            the sensor to set
	 */
	public void setSensor(String sensor) {
		this.sensor = sensor;
	}

	/**
	 * @return the citation
	 */
	public String getCitation() {
		return citation;
	}

	/**
	 * @param citation
	 *            the citation to set
	 */
	public void setCitation(String citation) {
		this.citation = citation;
	}

	/**
	 * @return the operational_manager
	 */
	public String getOperational_manager() {
		return operational_manager;
	}

	/**
	 * @param operational_manager
	 *            the operational_manager to set
	 */
	public void setOperational_manager(String operational_manager) {
		this.operational_manager = operational_manager;
	}

	/**
	 * @return the operational_manager_email
	 */
	public String getOperational_manager_email() {
		return operational_manager_email;
	}

	/**
	 * @param operational_manager_email
	 *            the operational_manager_email to set
	 */
	public void setOperational_manager_email(String operational_manager_email) {
		this.operational_manager_email = operational_manager_email;
	}

	/**
	 * @return the format_version
	 */
	public String getFormat_version() {
		return format_version;
	}

	/**
	 * @param format_version
	 *            the format_version to set
	 */
	public void setFormat_version(String format_version) {
		this.format_version = format_version;
	}

	/**
	 * @return the data_mode
	 */
	public String getData_mode() {
		return data_mode;
	}

	/**
	 * @param data_mode
	 *            the data_mode to set
	 */
	public void setData_mode(String data_mode) {
		this.data_mode = data_mode;
	}

	/**
	 * @return the processing_level
	 */
	public String getProcessing_level() {
		return processing_level;
	}

	/**
	 * @param processing_level
	 *            the processing_level to set
	 */
	public void setProcessing_level(String processing_level) {
		this.processing_level = processing_level;
	}

	/**
	 * @return the license
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * @param license
	 *            the license to set
	 */
	public void setLicense(String license) {
		this.license = license;
	}

	/**
	 * @return the update_interval
	 */
	public String getUpdate_interval() {
		return update_interval;
	}

	/**
	 * @param update_interval
	 *            the update_interval to set
	 */
	public void setUpdate_interval(String update_interval) {
		this.update_interval = update_interval;
	}

	/**
	 * @return the site_code
	 */
	public String getSite_code() {
		return site_code;
	}

	/**
	 * @param site_code
	 *            the site_code to set
	 */
	public void setSite_code(String site_code) {
		this.site_code = site_code;
	}

	/**
	 * @return the area
	 */
	public String getArea() {
		return area;
	}

	/**
	 * @param area
	 *            the area to set
	 */
	public void setArea(String area) {
		this.area = area;
	}

	/**
	 * @return the quality_control
	 */
	public String getQuality_control() {
		return quality_control;
	}

	/**
	 * @param quality_control
	 *            the quality_control to set
	 */
	public void setQuality_control(String quality_control) {
		this.quality_control = quality_control;
	}

	/**
	 * @return the references
	 */
	public String getReferences() {
		return references;
	}

	/**
	 * @param references
	 *            the references to set
	 */
	public void setReferences(String references) {
		this.references = references;
	}

	/**
	 * @return the publisher_name
	 */
	public String getPublisher_name() {
		return publisher_name;
	}

	/**
	 * @param publisher_name
	 *            the publisher_name to set
	 */
	public void setPublisher_name(String publisher_name) {
		this.publisher_name = publisher_name;
	}

	/**
	 * @return the publisher_url
	 */
	public String getPublisher_url() {
		return publisher_url;
	}

	/**
	 * @param publisher_url
	 *            the publisher_url to set
	 */
	public void setPublisher_url(String publisher_url) {
		this.publisher_url = publisher_url;
	}

	/**
	 * @return the publisher_email
	 */
	public String getPublisher_email() {
		return publisher_email;
	}

	/**
	 * @param publisher_email
	 *            the publisher_email to set
	 */
	public void setPublisher_email(String publisher_email) {
		this.publisher_email = publisher_email;
	}

	/**
	 * @return the contributor_name
	 */
	public String getContributor_name() {
		return contributor_name;
	}

	/**
	 * @param contributor_name
	 *            the contributor_name to set
	 */
	public void setContributor_name(String contributor_name) {
		this.contributor_name = contributor_name;
	}

	/**
	 * @return the contributor_role
	 */
	public String getContributor_role() {
		return contributor_role;
	}

	/**
	 * @param contributor_role
	 *            the contributor_role to set
	 */
	public void setContributor_role(String contributor_role) {
		this.contributor_role = contributor_role;
	}

	/**
	 * @return the contributor_email
	 */
	public String getContributor_email() {
		return contributor_email;
	}

	/**
	 * @param contributor_email
	 *            the contributor_email to set
	 */
	public void setContributor_email(String contributor_email) {
		this.contributor_email = contributor_email;
	}

	/**
	 * @return the siteSource
	 */
	public ArrayList<String> getSiteSource() {
		return siteSource;
	}

	/**
	 * @param siteSource
	 *            the siteSource to set
	 */
	public void setSiteSource(ArrayList<String> siteSource) {
		this.siteSource = siteSource;
	}

	/**
	 * @param String
	 *            siteSource adds one sitesource info to the array
	 */
	public void addSiteSource(String siteSourceElement) {
		this.siteSource.add(siteSourceElement);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the processedTimeStamp
	 */
	public String getProcessedTimeStamp() {
		return processedTimeStamp;
	}

	/**
	 * @param processedTimeStamp
	 *            the processedTimeStamp to set
	 */
	public void setProcessedTimeStamp(String processedTimeStamp) {
		this.processedTimeStamp = processedTimeStamp;
	}

	/**
	 * @return the processingTool
	 */
	public String getProcessingTool() {
		return processingTool;
	}

	/**
	 * @param processingTool
	 *            the processingTool to set
	 */
	public void setProcessingTool(String processingTool) {
		this.processingTool = processingTool;
	}

	/**
	 * @return the netcdf_version
	 */
	public String getNetcdf_version() {
		return netcdf_version;
	}

	/**
	 * @return the netcdf_format
	 */
	public String getNetcdf_format() {
		return netcdf_format;
	}

	/**
	 * @return the name_vocabulary
	 */
	public String getName_vocabulary() {
		return name_vocabulary;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the institution
	 */
	public String getInstitution() {
		return institution;
	}

	/**
	 * @param institution
	 *            the institution to set
	 */
	public void setInstitution(String institution) {
		this.institution = institution;
	}

	/**
	 * @return the conventions
	 */
	public String getConventions() {
		return conventions;
	}

	/**
	 * @param conventions
	 *            the conventions to set
	 */
	public void setConventions(String conventions) {
		this.conventions = conventions;
	}

	/**
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * @param summary
	 *            the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the network
	 */
	public String getNetwork() {
		return network;
	}

	/**
	 * @param network
	 *            the network to set
	 */
	public void setNetwork(String network) {
		this.network = network;
	}

	/**
	 * @return the keywords
	 */
	public String getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords
	 *            the keywords to set
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return the keywords_vocabulary
	 */
	public String getKeywords_vocabulary() {
		return keywords_vocabulary;
	}

	/**
	 * @param keywords_vocabulary
	 *            the keywords_vocabulary to set
	 */
	public void setKeywords_vocabulary(String keywords_vocabulary) {
		this.keywords_vocabulary = keywords_vocabulary;
	}

	/**
	 * @return the data_language
	 */
	public String getData_language() {
		return data_language;
	}

	/**
	 * @param data_language
	 *            the data_language to set
	 */
	public void setData_language(String data_language) {
		this.data_language = data_language;
	}

	/**
	 * @return the data_char_set
	 */
	public String getData_char_set() {
		return data_char_set;
	}

	/**
	 * @param data_char_set
	 *            the data_char_set to set
	 */
	public void setData_char_set(String data_char_set) {
		this.data_char_set = data_char_set;
	}

	/**
	 * @return the topic_cat
	 */
	public String getTopic_cat() {
		return topic_cat;
	}

	/**
	 * @param topic_cat
	 *            the topic_cat to set
	 */
	public void setTopic_cat(String topic_cat) {
		this.topic_cat = topic_cat;
	}

	/**
	 * @return the reference_sys
	 */
	public String getReference_sys() {
		return reference_sys;
	}

	/**
	 * @param reference_sys
	 *            the reference_sys to set
	 */
	public void setReference_sys(String reference_sys) {
		this.reference_sys = reference_sys;
	}

	/**
	 * @return the metadata_language
	 */
	public String getMetadata_language() {
		return metadata_language;
	}

	/**
	 * @param metadata_language
	 *            the metadata_language to set
	 */
	public void setMetadata_language(String metadata_language) {
		this.metadata_language = metadata_language;
	}

	/**
	 * @return the metadata_char_set
	 */
	public String getMetadata_char_set() {
		return metadata_char_set;
	}

	/**
	 * @param metadata_char_set
	 *            the metadata_char_set to set
	 */
	public void setMetadata_char_set(String metadata_char_set) {
		this.metadata_char_set = metadata_char_set;
	}

	/**
	 * @return the metadata_contact
	 */
	public String getMetadata_contact() {
		return metadata_contact;
	}

	/**
	 * @param metadata_contact
	 *            the metadata_contact to set
	 */
	public void setMetadata_contact(String metadata_contact) {
		this.metadata_contact = metadata_contact;
	}

	/**
	 * @param netcdf_version
	 *            the netcdf_version to set
	 */
	public void setNetcdf_version(String netcdf_version) {
		this.netcdf_version = netcdf_version;
	}

	/**
	 * @param netcdf_format
	 *            the netcdf_format to set
	 */
	public void setNetcdf_format(String netcdf_format) {
		this.netcdf_format = netcdf_format;
	}

	/**
	 * @param name_vocabulary
	 *            the name_vocabulary to set
	 */
	public void setName_vocabulary(String name_vocabulary) {
		this.name_vocabulary = name_vocabulary;
	}

	/**
	 * @return the naming_authority
	 */
	public String getNaming_authority() {
		return naming_authority;
	}

	/**
	 * @param naming_authority
	 *            the naming_authority to set
	 */
	public void setNaming_authority(String naming_authority) {
		this.naming_authority = naming_authority;
	}

	/**
	 * @return the xMin
	 */
	public String getxMin() {
		return xMin;
	}

	/**
	 * @return the xMin as float
	 */
	public Float getxMinAsFloat() throws NumberFormatException {
		Float vuelta = Float.NaN;
		if (this.xMin != null) {
			String bb = this.xMin;
			if (xMin.trim().split("\\s+").length > 1) {
				bb = xMin.trim().split("\\s+")[0];
			}
			vuelta = Float.parseFloat(bb);
		}
		return vuelta;
	}

	/**
	 * @param xMin
	 *            the xMin to set
	 */
	public void setxMin(String xMin) {
		this.xMin = xMin;
	}

	/**
	 * @return the xMax
	 */
	public String getxMax() {
		return xMax;
	}

	/**
	 * @return the xMax as float
	 */
	public Float getxMaxAsFloat() throws NumberFormatException {
		Float vuelta = Float.NaN;
		if (this.xMax != null) {
			String bb = this.xMax;
			if (xMax.trim().split("\\s+").length > 1) {
				bb = xMax.trim().split("\\s+")[0];
			}
			vuelta = Float.parseFloat(bb);
		}
		return vuelta;
	}

	/**
	 * @param xMax
	 *            the xMax to set
	 */
	public void setxMax(String xMax) {
		this.xMax = xMax;
	}

	/**
	 * @return the yMin
	 */
	public String getyMin() {
		return yMin;
	}

	/**
	 * @return the yMin as float
	 */
	public Float getyMinAsFloat() throws NumberFormatException {
		Float vuelta = Float.NaN;
		if (this.yMin != null) {
			String bb = this.yMin;
			if (yMin.trim().split("\\s+").length > 1) {
				bb = yMin.trim().split("\\s+")[0];
			}
			vuelta = Float.parseFloat(bb);
		}
		return vuelta;
	}

	/**
	 * @param yMin
	 *            the yMin to set
	 */
	public void setyMin(String yMin) {
		this.yMin = yMin;
	}

	/**
	 * @return the yMax
	 */
	public String getyMax() {
		return yMax;
	}

	/**
	 * @return the yMax as float
	 */
	public Float getyMaxAsFloat() throws NumberFormatException {
		Float vuelta = Float.NaN;
		if (this.yMax != null) {
			String bb = this.yMax;
			if (yMax.trim().split("\\s+").length > 1) {
				bb = yMax.trim().split("\\s+")[0];
			}
			vuelta = Float.parseFloat(bb);
		}
		return vuelta;
	}

	/**
	 * @param yMax
	 *            the yMax to set
	 */
	public void setyMax(String yMax) {
		this.yMax = yMax;
	}

	/**
	 * Completes missing values using a profile info. Just the missing, empty or
	 * null
	 * 
	 * @param profile
	 */
	public boolean fixMissingValues(CodarTotalBean profile) {
		boolean todoOK = false;
		if (profile == null) {
			// cuidado, pasamos un nulo! no vale para nada!
		} else {

			if (getCombineMethod() == null || getCombineMethod().isEmpty() || getCombineMethod().equals("NaN"))
				setCombineMethod(profile.getCombineMethod());
			if (getGridAxisOrientation() == null || getGridAxisOrientation().isEmpty()
					|| getGridAxisOrientation().equals("NaN"))
				setGridAxisOrientation(profile.getGridAxisOrientation());
			if (getGridCreatedBy() == null || getGridCreatedBy().isEmpty() || getGridCreatedBy().equals("NaN"))
				setGridCreatedBy(profile.getGridCreatedBy());
			if (getGridVersion() == null || getGridVersion().isEmpty() || getGridVersion().equals("NaN"))
				setGridVersion(profile.getGridVersion());
			if (getGridTimeStamp() == null || getGridTimeStamp().isEmpty() || getGridTimeStamp().equals("NaN"))
				setGridTimeStamp(profile.getGridTimeStamp());
			if (getGridLastModified() == null || getGridLastModified().isEmpty() || getGridLastModified().equals("NaN"))
				setGridLastModified(profile.getGridLastModified());
			if (getGridAxisType() == null || getGridAxisType().isEmpty() || getGridAxisType().equals("NaN"))
				setGridAxisType(profile.getGridAxisType());
			if (getGridSpacing() == null || getGridSpacing().isEmpty() || getGridSpacing().equals("NaN"))
				setGridSpacing(profile.getGridSpacing());
			if (getAveragingRadius() == null || getAveragingRadius().isEmpty() || getAveragingRadius().equals("NaN"))
				setAveragingRadius(profile.getAveragingRadius());
			if (getDistanceAngularLimit() == null || getDistanceAngularLimit().isEmpty()
					|| getDistanceAngularLimit().equals("NaN"))
				setDistanceAngularLimit(profile.getDistanceAngularLimit());

			if (getOrigin() == null || getOrigin().isEmpty() || getOrigin().equals("NaN"))
				setOrigin(profile.getOrigin());
			if (getCTF() == null || getCTF().isEmpty() || getCTF().equals("NaN"))
				setCTF(profile.getCTF());
			if (getFileType() == null || getFileType().isEmpty() || getFileType().equals("NaN"))
				setFileType(profile.getFileType());
			if (getLLUVSpec() == null || getLLUVSpec().isEmpty() || getLLUVSpec().equals("NaN"))
				setLLUVSpec(profile.getLLUVSpec());
			if (getUUID() == null || getUUID().isEmpty() || getUUID().equals("NaN"))
				setUUID(profile.getUUID());
			if (getManufacturer() == null || getManufacturer().isEmpty() || getManufacturer().equals("NaN"))
				setManufacturer(profile.getManufacturer());
			if (getSite() == null || getSite().isEmpty() || getSite().equals("NaN"))
				setSite(profile.getSite());
			if (getTimeZone() == null || getTimeZone().isEmpty() || getTimeZone().equals("NaN"))
				setTimeZone(profile.getTimeZone());
			if (getTimeCoverage() == null || getTimeCoverage().isEmpty() || getTimeCoverage().equals("NaN"))
				setTimeCoverage(profile.getTimeCoverage());
			if (getGreatCircle() == null || getGreatCircle().isEmpty() || getGreatCircle().equals("NaN"))
				setGreatCircle(profile.getGreatCircle());

			if (getxMin() == null || getxMin().isEmpty() || getxMin().equals("NaN"))
				setxMin(profile.getxMin());
			if (getxMax() == null || getxMax().isEmpty() || getxMax().equals("NaN"))
				setxMax(profile.getxMax());
			if (getyMin() == null || getyMin().isEmpty() || getyMin().equals("NaN"))
				setyMin(profile.getyMin());
			if (getyMax() == null || getyMax().isEmpty() || getyMax().equals("NaN"))
				setyMax(profile.getyMax());

			if (getGeodVersion() == null || getGeodVersion().isEmpty() || getGeodVersion().equals("NaN"))
				setGeodVersion(profile.getGeodVersion());
			if (getLLUVTrustData() == null || getLLUVTrustData().isEmpty() || getLLUVTrustData().equals("NaN"))
				setLLUVTrustData(profile.getLLUVTrustData());

			if (getCurrentVelocityLimit() == null || getCurrentVelocityLimit().isEmpty()
					|| getCurrentVelocityLimit().equals("NaN"))
				setCurrentVelocityLimit(profile.getCurrentVelocityLimit());
			if (getTimeStamp() == null || getTimeStamp().isEmpty() || getTimeStamp().equals("NaN"))
				setTimeStamp(profile.getTimeStamp());
			if (getProcessedTimeStamp() == null || getProcessedTimeStamp().isEmpty()
					|| getProcessedTimeStamp().equals("NaN"))
				setProcessedTimeStamp(profile.getProcessedTimeStamp());
			if (getProcessingTool() == null || getProcessingTool().isEmpty() || getProcessingTool().equals("NaN"))
				setProcessingTool(profile.getProcessingTool());

			if (getInstitution() == null || getInstitution().isEmpty() || getInstitution().equals("NaN"))
				setInstitution(profile.getInstitution());
			if (getSummary() == null || getSummary().isEmpty() || getSummary().equals("NaN"))
				setSummary(profile.getSummary());
			if (getNetwork() == null || getNetwork().isEmpty() || getNetwork().equals("NaN"))
				setNetwork(profile.getNetwork());
			if (getMetadata_contact() == null || getMetadata_contact().isEmpty() || getMetadata_contact().equals("NaN"))
				setMetadata_contact(profile.getMetadata_contact());

			if (getId() == null || getId().isEmpty() || getId().equals("NaN"))
				setId(profile.getId());
			if (getNaming_authority() == null || getNaming_authority().isEmpty() || getNaming_authority().equals("NaN"))
				setNaming_authority(profile.getNaming_authority());
			// cdm_data_type
			if (getTime_coverage_start() == null || getTime_coverage_start().isEmpty()
					|| getTime_coverage_start().equals("NaN"))
				setTime_coverage_start(profile.getTime_coverage_start());
			if (getTime_coverage_end() == null || getTime_coverage_end().isEmpty()
					|| getTime_coverage_end().equals("NaN"))
				setTime_coverage_end(profile.getTime_coverage_end());

			if (getDate_issued() == null || getDate_issued().isEmpty() || getDate_issued().equals("NaN"))
				setDate_issued(profile.getDate_issued());
			if (getLicense() == null || getLicense().isEmpty() || getLicense().equals("NaN"))
				setLicense(profile.getLicense());
			if (getCreator_name() == null || getCreator_name().isEmpty() || getCreator_name().equals("NaN"))
				setCreator_name(profile.getCreator_name());
			if (getCreator_email() == null || getCreator_email().isEmpty() || getCreator_email().equals("NaN"))
				setCreator_email(profile.getCreator_email());
			if (getCreator_url() == null || getCreator_url().isEmpty() || getCreator_url().equals("NaN"))
				setCreator_url(profile.getCreator_url());
			if (getAcknowledgment() == null || getAcknowledgment().isEmpty() || getAcknowledgment().equals("NaN"))
				setAcknowledgment(profile.getAcknowledgment());
			if (getComments() == null || getComments().isEmpty() || getComments().equals("NaN"))
				setComments(profile.getComments());
			if (getPlatform() == null || getPlatform().isEmpty() || getPlatform().equals("NaN"))
				setPlatform(profile.getPlatform());

			if (getSensor() == null || getSensor().isEmpty() || getSensor().equals("NaN"))
				setSensor(profile.getSensor());
			if (getCitation() == null || getCitation().isEmpty() || getCitation().equals("NaN"))
				setCitation(profile.getCitation());
			if (getOperational_manager() == null || getOperational_manager().isEmpty()
					|| getOperational_manager().equals("NaN"))
				setOperational_manager(profile.getOperational_manager());
			if (getOperational_manager_email() == null || getOperational_manager_email().isEmpty()
					|| getOperational_manager_email().equals("NaN"))
				setOperational_manager_email(profile.getOperational_manager_email());
			// format_version
			// data mode
			if (getUpdate_interval() == null || getUpdate_interval().isEmpty() || getUpdate_interval().equals("NaN"))
				setUpdate_interval(profile.getUpdate_interval());
			if (getSite_code() == null || getSite_code().isEmpty() || getSite_code().equals("NaN"))
				setSite_code(profile.getSite_code());
			if (getArea() == null || getArea().isEmpty() || getArea().equals("NaN"))
				setArea(profile.getArea());
			// quality_control
			if (getReferences() == null || getReferences().isEmpty() || getReferences().equals("NaN"))
				setReferences(profile.getReferences());
			if (getPublisher_name() == null || getPublisher_name().isEmpty() || getPublisher_name().equals("NaN"))
				setPublisher_name(profile.getPublisher_name());
			if (getPublisher_url() == null || getPublisher_url().isEmpty() || getPublisher_url().equals("NaN"))
				setPublisher_url(profile.getPublisher_url());
			if (getPublisher_email() == null || getPublisher_email().isEmpty() || getPublisher_email().equals("NaN"))
				setPublisher_email(profile.getPublisher_email());
			if (getContributor_name() == null || getContributor_name().isEmpty() || getContributor_name().equals("NaN"))
				setContributor_name(profile.getContributor_name());
			if (getContributor_role() == null || getContributor_role().isEmpty() || getContributor_role().equals("NaN"))
				setContributor_role(profile.getContributor_role());
			if (getContributor_email() == null || getContributor_email().isEmpty()
					|| getContributor_email().equals("NaN"))
				setContributor_email(profile.getContributor_email());

			// additionalAttributes

			// Radial QAQC test data
			if (Float.isNaN(totalTest.getVeloThreshold()))
				totalTest.setVeloThreshold(profile.getTotalTest().getVeloThreshold());
			if (Float.isNaN(totalTest.getVarianceThreshold()))
				totalTest.setVarianceThreshold(profile.getTotalTest().getVarianceThreshold());
			if (Float.isNaN(totalTest.getDataDensityThreshold()))
				totalTest.setDataDensityThreshold(profile.getTotalTest().getDataDensityThreshold());
			if (Float.isNaN(totalTest.getGDOPThreshold()))
				totalTest.setGDOPThreshold(profile.getTotalTest().getGDOPThreshold());
			if (Float.isNaN(totalTest.getTempThreshold()))
				totalTest.setTempThreshold(profile.getTotalTest().getTempThreshold());

			if (networkBean == null)
				setNetworkBean(profile.getNetworkBean());

			todoOK = true;
		}
		return todoOK;
	}

}
