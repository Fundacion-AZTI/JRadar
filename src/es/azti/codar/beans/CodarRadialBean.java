/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  18 de may. de 2016
 */
package es.azti.codar.beans;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import es.azti.db.NETWORK_TB;
import es.azti.db.STATION_TB;
import ucar.nc2.NetcdfFileWriter;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es) 18 de may. de 2016
 *
 *         Codar radial file bean, it contains data and parameters related to
 *         the radial files, that will be transformed as parameters and metadata
 *         of the final netCDF file.
 */
public class CodarRadialBean implements Serializable {

	private static final long serialVersionUID = 6199407249100077409L;

	// File Name and path
	private String fileName;
	private String pathToFile;

	// beans related to the central node DDBB
	private NETWORK_TB networkBean;
	private STATION_TB stationBean;

	// ATRIBUTES, this are mandatory, they should be in the original file or
	// should be added manualy.
	private String rangeResolutionKMeters; // RangeResolutionKMeters
	private String angularResolution; // AngularResolution
	private String origin; // Origin
	private String cTF; // CTF
	private String fileType; // FileType
	private String lLUVSpec; // LLUVSpec
	private String uUID; // UUID
	private String manufacturer; // Manufacturer
	private String site; // Site
	private String timeZone; // TimeZone
	private String timeCoverage; // TimeCoverage
	private String greatCircle; // GreatCircle
	private String geodVersion; // GeodVersion
	private String lLUVTrustData; // LLUVTrustData
	private String rangeStart; // RangeStart
	private String rangeEnd; // RangeEnd
	private String antennaBearing; // AntennaBearing
	private String minimunBearing; // minimun bearing for the netcdf grid
	private String maximunBearing; // maximun bearing for the netcdf grid
	private String minimunRange; // minimun range for the netcdf grid
	private String maximunRange; // maximun range for the netcdf grid
	private String referenceBearing; // ReferenceBearing
	private String spatialResolution; // SpatialResolution
	private String patternType; // PatternType
	private String patternDate; // PatternDate
	private String patternResolution; // PatternResolution
	private String transmitCenterFreqMHz; // TransmitCenterFreqMHz
	private String dopplerResolutionHzPerBin; // DopplerResolutionHzPerBin
	private String firstOrderMethod; // FirstOrderMethod
	private String braggSmoothingPoints; // BraggSmoothingPoints
	private String currentVelocityLimit; // CurrentVelocityLimit
	private String braggHasSecondOrder; // BraggHasSecondOrder
	private String radialBraggPeakDropOff; // RadialBraggPeakDropOff
	private String radialBraggPeakNull; // RadialBraggPeakNull
	private String radialBraggNoiseThreshold; // RadialBraggNoiseThreshold
	private String patternAmplitudeCorrections; // PatternAmplitudeCorrections
	private String patternPhaseCorrections; // PatternPhaseCorrections
	private String patternAmplitudeCalculations; // PatternAmplitudeCalculations
	private String patternPhaseCalculations; // PatternPhaseCalculations
	private String radialMusicParameters; // RadialMusicParameters
	private String mergedCount; // MergedCount
	private String radialMinimumMergePoints; // RadialMinimumMergePoints
	private String firstOrderCalc; // FirstOrderCalc
	private String mergeMethod; // MergeMethod
	private String patternMethod; // PatternMethod
	private String transmitSweepRateHz; // TransmitSweepRateHz
	private String transmitBandwidthKHz; // TransmitBandwidthKHz
	private String spectraRangeCells; // SpectraRangeCells
	private String spectraDopplerCells; // SpectraDopplerCells
	private String timeStamp; // TimeStamp
	private String radialSmoothingParameters; // RadialSmoothingParameters
	private String patternSmoothing; // PatternSmoothing
	private String patternUUID; // PatternUUID
	private String processedTimeStamp; // ProcessedTimeStamp
	private String processingTool; // ProcessingTool
	// metadata
	private String title = "Near Real Time Surface Ocean Radial Velocity";
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
	private String netcdf_version = "netCDF-4 classic model";
	private String netcdf_format = NetcdfFileWriter.Version.netcdf4_classic.name();
	private String name_vocabulary = "NetCDF Climate and Forecast (CF) Metadata Convention Standard Name Table Version 1.6";

	// date formater
	private String dateCreated;
	private String dateCollected;
	private String histCreated;

	private String id;
	private String naming_authority = "Copernicus Marine In Situ";
	private String cdm_data_type = "grid";
	private String project = "";
	private String time_coverage_start;
	private String time_coverage_end;
	private String time_coverage_duration = "PT1H";
	private String time_coverage_resolution = "PT1H";
	private String date_modified;
	private String date_issued;
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
	private String format_version = "1.4";
	private String data_mode = "R";
	private String update_interval = "void";
	private String site_code = "";
	private String area = "";
	private String quality_control = "Level-B: advanced QC";
	private String references = "Netcdf Java Documentation - https://www.unidata.ucar.edu/software/thredds/current/netcdf-java/";
	private String publisher_name = "European HFR node";
	private String publisher_url = "http://eurogoos.eu/high-frequency-radar-task-team/";
	private String publisher_email = "euhfrnode@azti.es";
	private String contributor_name = "";
	private String contributor_role = "";
	private String contributor_email = "";

	// ATTRIBUTES, additional params. This are stored in a Collection, in order
	// to write in the output file, not to lost information
	// not used yet.
	private HashMap<String, String> aditionalAttributes;
	// Radial radar information, the data
	private transient CodarDataTableBean table;
	// Radial QAQC test data
	private RadialQCQATestBean radialTest;

	// default date format in CODAR files
	private SimpleDateFormat codarFormat = new SimpleDateFormat("yyyy MM dd HH mm ss");

	/**
	 * @throws ParseException
	 * 
	 */
	public CodarRadialBean() throws ParseException {
		super();
		aditionalAttributes = new HashMap<String, String>();
	}

	/**
	 * @return the stationBean
	 */
	public STATION_TB getStationBean() {
		return stationBean;
	}

	/**
	 * @param stationBean
	 *            the stationBean to set
	 */
	public void setStationBean(STATION_TB stationBean) {
		this.stationBean = stationBean;
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
	 * @return the rangeResolutionKMeters
	 */
	public String getRangeResolutionKMeters() {
		return rangeResolutionKMeters;
	}

	/**
	 * @return number representin the angular resolution
	 */
	public float getRangeResolutionKMetersFloat() throws NumberFormatException {
		Float vuelta = Float.NaN;
		if (rangeResolutionKMeters != null) {
			vuelta = Float.parseFloat(rangeResolutionKMeters);
		} else {

		}
		return vuelta;
	}

	/**
	 * @param rangeResolutionKMeters
	 *            the rangeResolutionKMeters to set
	 */
	public void setRangeResolutionKMeters(String rangeResolutionKMeters) {
		this.rangeResolutionKMeters = rangeResolutionKMeters;
	}

	/**
	 * @return the angularResolution
	 */
	public String getAngularResolution() {
		return angularResolution;
	}

	/**
	 * @return number representing the angular resolution
	 */
	public int getAngularResolutionInteger() throws NumberFormatException {
		Integer vuelta = new Integer(-1);
		if (this.angularResolution != null) {
			String angRes = this.angularResolution;
			if (angularResolution.trim().split("\\s+").length > 1) {
				angRes = angularResolution.trim().split("\\s+")[0];
			}
			vuelta = Integer.parseInt(angRes);
		}
		return vuelta;
	}

	/**
	 * @param angularResolution
	 *            the angularResolution to set
	 */
	public void setAngularResolution(String angularResolution) {
		this.angularResolution = angularResolution;
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
	 * @return the cTF
	 */
	public String getCTF() {
		return cTF;
	}

	/**
	 * @param cTF
	 *            the cTF to set
	 */
	public void setCTF(String cTF) {
		this.cTF = cTF;
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
		return lLUVSpec;
	}

	/**
	 * @param lLUVSpec
	 *            the lLUVSpec to set
	 */
	public void setLLUVSpec(String lLUVSpec) {
		this.lLUVSpec = lLUVSpec;
	}

	/**
	 * @return the uUID
	 */
	public String getUUID() {
		return uUID;
	}

	/**
	 * @param uUID
	 *            the uUID to set
	 */
	public void setUUID(String uUID) {
		this.uUID = uUID;
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
		return lLUVTrustData;
	}

	/**
	 * @param lLUVTrustData
	 *            the lLUVTrustData to set
	 */
	public void setLLUVTrustData(String lLUVTrustData) {
		this.lLUVTrustData = lLUVTrustData;
	}

	/**
	 * @return the rangeStart
	 */
	public String getRangeStart() {
		return rangeStart;
	}

	/**
	 * @param rangeStart
	 *            the rangeStart to set
	 */
	public void setRangeStart(String rangeStart) {
		this.rangeStart = rangeStart;
	}

	/**
	 * @return the rangeEnd
	 */
	public String getRangeEnd() {
		return rangeEnd;
	}

	/**
	 * @param rangeEnd
	 *            the rangeEnd to set
	 */
	public void setRangeEnd(String rangeEnd) {
		this.rangeEnd = rangeEnd;
	}

	/**
	 * @return the antennaBearing
	 */
	public String getAntennaBearing() {
		return antennaBearing;
	}

	/**
	 * @return the antennaBearing as float
	 */
	public Float getAntennaBearingAsFloat() throws NumberFormatException {
		Float vuelta = Float.NaN;
		if (this.antennaBearing != null) {
			String antBear = this.antennaBearing;
			if (antennaBearing.trim().split("\\s+").length > 1) {
				antBear = antennaBearing.trim().split("\\s+")[0];
			}
			vuelta = Float.parseFloat(antBear);
		}
		return vuelta;
	}

	/**
	 * @param antennaBearing
	 *            the antennaBearing to set
	 */
	public void setAntennaBearing(String antennaBearing) {
		this.antennaBearing = antennaBearing;
	}

	/**
	 * @return the referenceBearing
	 */
	public String getReferenceBearing() {
		return referenceBearing;
	}

	/**
	 * @param referenceBearing
	 *            the referenceBearing to set
	 */
	public void setReferenceBearing(String referenceBearing) {
		this.referenceBearing = referenceBearing;
	}

	/**
	 * @return the spatialResolution
	 */
	public String getSpatialResolution() {
		return spatialResolution;
	}

	/**
	 * @param spatialResolution
	 *            the spatialResolution to set
	 */
	public void setSpatialResolution(String spatialResolution) {
		this.spatialResolution = spatialResolution;
	}

	/**
	 * @return the patternType
	 */
	public String getPatternType() {
		return patternType;
	}

	/**
	 * @param patternType
	 *            the patternType to set
	 */
	public void setPatternType(String patternType) {
		this.patternType = patternType;
	}

	/**
	 * @return the patternDate
	 */
	public String getPatternDate() {
		return patternDate;
	}

	public String getPatternDateUTC() {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date coverageStart = Calendar.getInstance().getTime();
		try {
			coverageStart = codarFormat.parse(patternDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dateFormat.format(coverageStart.getTime()) + "T" + hourFormat.format(coverageStart.getTime()) + "Z";
	}

	/**
	 * @param patternDate
	 *            the patternDate to set
	 */
	public void setPatternDate(String patternDate) {
		this.patternDate = patternDate;
	}

	/**
	 * @return the patternResolution
	 */
	public String getPatternResolution() {
		return patternResolution;
	}

	/**
	 * @param patternResolution
	 *            the patternResolution to set
	 */
	public void setPatternResolution(String patternResolution) {
		this.patternResolution = patternResolution;
	}

	/**
	 * @return the transmitCenterFreqMHz
	 */
	public String getTransmitCenterFreqMHz() {
		return transmitCenterFreqMHz;
	}

	/**
	 * @param transmitCenterFreqMHz
	 *            the transmitCenterFreqMHz to set
	 */
	public void setTransmitCenterFreqMHz(String transmitCenterFreqMHz) {
		this.transmitCenterFreqMHz = transmitCenterFreqMHz;
	}

	/**
	 * @return the dopplerResolutionHzPerBin
	 */
	public String getDopplerResolutionHzPerBin() {
		return dopplerResolutionHzPerBin;
	}

	/**
	 * @param dopplerResolutionHzPerBin
	 *            the dopplerResolutionHzPerBin to set
	 */
	public void setDopplerResolutionHzPerBin(String dopplerResolutionHzPerBin) {
		this.dopplerResolutionHzPerBin = dopplerResolutionHzPerBin;
	}

	/**
	 * @return the firstOrderMethod
	 */
	public String getFirstOrderMethod() {
		return firstOrderMethod;
	}

	/**
	 * @param firstOrderMethod
	 *            the firstOrderMethod to set
	 */
	public void setFirstOrderMethod(String firstOrderMethod) {
		this.firstOrderMethod = firstOrderMethod;
	}

	/**
	 * @return the braggSmoothingPoints
	 */
	public String getBraggSmoothingPoints() {
		return braggSmoothingPoints;
	}

	/**
	 * @param braggSmoothingPoints
	 *            the braggSmoothingPoints to set
	 */
	public void setBraggSmoothingPoints(String braggSmoothingPoints) {
		this.braggSmoothingPoints = braggSmoothingPoints;
	}

	/**
	 * @return the currentVelocityLimit
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
	 * @return the braggHasSecondOrder
	 */
	public String getBraggHasSecondOrder() {
		return braggHasSecondOrder;
	}

	/**
	 * @param braggHasSecondOrder
	 *            the braggHasSecondOrder to set
	 */
	public void setBraggHasSecondOrder(String braggHasSecondOrder) {
		this.braggHasSecondOrder = braggHasSecondOrder;
	}

	/**
	 * @return the radialBraggPeakDropOff
	 */
	public String getRadialBraggPeakDropOff() {
		return radialBraggPeakDropOff;
	}

	/**
	 * @param radialBraggPeakDropOff
	 *            the radialBraggPeakDropOff to set
	 */
	public void setRadialBraggPeakDropOff(String radialBraggPeakDropOff) {
		this.radialBraggPeakDropOff = radialBraggPeakDropOff;
	}

	/**
	 * @return the radialBraggPeakNull
	 */
	public String getRadialBraggPeakNull() {
		return radialBraggPeakNull;
	}

	/**
	 * @param radialBraggPeakNull
	 *            the radialBraggPeakNull to set
	 */
	public void setRadialBraggPeakNull(String radialBraggPeakNull) {
		this.radialBraggPeakNull = radialBraggPeakNull;
	}

	/**
	 * @return the radialBraggNoiseThreshold
	 */
	public String getRadialBraggNoiseThreshold() {
		return radialBraggNoiseThreshold;
	}

	/**
	 * @param radialBraggNoiseThreshold
	 *            the radialBraggNoiseThreshold to set
	 */
	public void setRadialBraggNoiseThreshold(String radialBraggNoiseThreshold) {
		this.radialBraggNoiseThreshold = radialBraggNoiseThreshold;
	}

	/**
	 * @return the patternAmplitudeCorrections
	 */
	public String getPatternAmplitudeCorrections() {
		return patternAmplitudeCorrections;
	}

	/**
	 * @param patternAmplitudeCorrections
	 *            the patternAmplitudeCorrections to set
	 */
	public void setPatternAmplitudeCorrections(String patternAmplitudeCorrections) {
		this.patternAmplitudeCorrections = patternAmplitudeCorrections;
	}

	/**
	 * @return the patternPhaseCorrections
	 */
	public String getPatternPhaseCorrections() {
		return patternPhaseCorrections;
	}

	/**
	 * @param patternPhaseCorrections
	 *            the patternPhaseCorrections to set
	 */
	public void setPatternPhaseCorrections(String patternPhaseCorrections) {
		this.patternPhaseCorrections = patternPhaseCorrections;
	}

	/**
	 * @return the patternAmplitudeCalculations
	 */
	public String getPatternAmplitudeCalculations() {
		return patternAmplitudeCalculations;
	}

	/**
	 * @param patternAmplitudeCalculations
	 *            the patternAmplitudeCalculations to set
	 */
	public void setPatternAmplitudeCalculations(String patternAmplitudeCalculations) {
		this.patternAmplitudeCalculations = patternAmplitudeCalculations;
	}

	/**
	 * @return the patternPhaseCalculations
	 */
	public String getPatternPhaseCalculations() {
		return patternPhaseCalculations;
	}

	/**
	 * @param patternPhaseCalculations
	 *            the patternPhaseCalculations to set
	 */
	public void setPatternPhaseCalculations(String patternPhaseCalculations) {
		this.patternPhaseCalculations = patternPhaseCalculations;
	}

	/**
	 * @return the radialMusicParameters
	 */
	public String getRadialMusicParameters() {
		return radialMusicParameters;
	}

	/**
	 * @param radialMusicParameters
	 *            the radialMusicParameters to set
	 */
	public void setRadialMusicParameters(String radialMusicParameters) {
		this.radialMusicParameters = radialMusicParameters;
	}

	/**
	 * @return the mergedCount
	 */
	public String getMergedCount() {
		return mergedCount;
	}

	/**
	 * @param mergedCount
	 *            the mergedCount to set
	 */
	public void setMergedCount(String mergedCount) {
		this.mergedCount = mergedCount;
	}

	/**
	 * @return the radialMinimumMergePoints
	 */
	public String getRadialMinimumMergePoints() {
		return radialMinimumMergePoints;
	}

	/**
	 * @param radialMinimumMergePoints
	 *            the radialMinimumMergePoints to set
	 */
	public void setRadialMinimumMergePoints(String radialMinimumMergePoints) {
		this.radialMinimumMergePoints = radialMinimumMergePoints;
	}

	/**
	 * @return the firstOrderCalc
	 */
	public String getFirstOrderCalc() {
		return firstOrderCalc;
	}

	/**
	 * @param firstOrderCalc
	 *            the firstOrderCalc to set
	 */
	public void setFirstOrderCalc(String firstOrderCalc) {
		this.firstOrderCalc = firstOrderCalc;
	}

	/**
	 * @return the mergeMethod
	 */
	public String getMergeMethod() {
		return mergeMethod;
	}

	/**
	 * @param mergeMethod
	 *            the mergeMethod to set
	 */
	public void setMergeMethod(String mergeMethod) {
		this.mergeMethod = mergeMethod;
	}

	/**
	 * @return the patternMethod
	 */
	public String getPatternMethod() {
		return patternMethod;
	}

	/**
	 * @param patternMethod
	 *            the patternMethod to set
	 */
	public void setPatternMethod(String patternMethod) {
		this.patternMethod = patternMethod;
	}

	/**
	 * @return the transmitSweepRateHz
	 */
	public String getTransmitSweepRateHz() {
		return transmitSweepRateHz;
	}

	/**
	 * @param transmitSweepRateHz
	 *            the transmitSweepRateHz to set
	 */
	public void setTransmitSweepRateHz(String transmitSweepRateHz) {
		this.transmitSweepRateHz = transmitSweepRateHz;
	}

	/**
	 * @return the transmitBandwidthKHz
	 */
	public String getTransmitBandwidthKHz() {
		return transmitBandwidthKHz;
	}

	/**
	 * @param transmitBandwidthKHz
	 *            the transmitBandwidthKHz to set
	 */
	public void setTransmitBandwidthKHz(String transmitBandwidthKHz) {
		this.transmitBandwidthKHz = transmitBandwidthKHz;
	}

	/**
	 * @return the spectraRangeCells
	 */
	public String getSpectraRangeCells() {
		return spectraRangeCells;
	}

	/**
	 * @param spectraRangeCells
	 *            the spectraRangeCells to set
	 */
	public void setSpectraRangeCells(String spectraRangeCells) {
		this.spectraRangeCells = spectraRangeCells;
	}

	/**
	 * @return the spectraDopplerCells
	 */
	public String getSpectraDopplerCells() {
		return spectraDopplerCells;
	}

	/**
	 * @param spectraDopplerCells
	 *            the spectraDopplerCells to set
	 */
	public void setSpectraDopplerCells(String spectraDopplerCells) {
		this.spectraDopplerCells = spectraDopplerCells;
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
	 * @return the radialSmoothingParameters
	 */
	public String getRadialSmoothingParameters() {
		return radialSmoothingParameters;
	}

	/**
	 * @param radialSmoothingParameters
	 *            the radialSmoothingParameters to set
	 */
	public void setRadialSmoothingParameters(String radialSmoothingParameters) {
		this.radialSmoothingParameters = radialSmoothingParameters;
	}

	/**
	 * @return the patternSmoothing
	 */
	public String getPatternSmoothing() {
		return patternSmoothing;
	}

	/**
	 * @param patternSmoothing
	 *            the patternSmoothing to set
	 */
	public void setPatternSmoothing(String patternSmoothing) {
		this.patternSmoothing = patternSmoothing;
	}

	/**
	 * @return the patternUUID
	 */
	public String getPatternUUID() {
		return patternUUID;
	}

	/**
	 * @param patternUUID
	 *            the patternUUID to set
	 */
	public void setPatternUUID(String patternUUID) {
		this.patternUUID = patternUUID;
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
	 * @return the radialTest
	 */
	public RadialQCQATestBean getRadialTest() {
		return radialTest;
	}

	/**
	 * @param radialTest
	 *            the radialTest to set
	 */
	public void setRadialTest(RadialQCQATestBean radialTest) {
		this.radialTest = radialTest;
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
	 * @return the lLUVSpec
	 */
	public String getlLUVSpec() {
		return lLUVSpec;
	}

	/**
	 * @param lLUVSpec
	 *            the lLUVSpec to set
	 */
	public void setlLUVSpec(String lLUVSpec) {
		this.lLUVSpec = lLUVSpec;
	}

	/**
	 * @return the uUID
	 */
	public String getuUID() {
		return uUID;
	}

	/**
	 * @param uUID
	 *            the uUID to set
	 */
	public void setuUID(String uUID) {
		this.uUID = uUID;
	}

	/**
	 * @return the lLUVTrustData
	 */
	public String getlLUVTrustData() {
		return lLUVTrustData;
	}

	/**
	 * @param lLUVTrustData
	 *            the lLUVTrustData to set
	 */
	public void setlLUVTrustData(String lLUVTrustData) {
		this.lLUVTrustData = lLUVTrustData;
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
	 * @return the netcdf_version
	 */
	public String getNetcdf_version() {
		return netcdf_version;
	}

	/**
	 * @param netcdf_version
	 *            the netcdf_version to set
	 */
	public void setNetcdf_version(String netcdf_version) {
		this.netcdf_version = netcdf_version;
	}

	/**
	 * @return the netcdf_format
	 */
	public String getNetcdf_format() {
		return netcdf_format;
	}

	/**
	 * @param netcdf_format
	 *            the netcdf_format to set
	 */
	public void setNetcdf_format(String netcdf_format) {
		this.netcdf_format = netcdf_format;
	}

	/**
	 * @return the name_vocabulary
	 */
	public String getName_vocabulary() {
		return name_vocabulary;
	}

	/**
	 * @param name_vocabulary
	 *            the name_vocabulary to set
	 */
	public void setName_vocabulary(String name_vocabulary) {
		this.name_vocabulary = name_vocabulary;
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
	 * @return the cdm_data_type
	 */
	public String getCdm_data_type() {
		return cdm_data_type;
	}

	/**
	 * @param cdm_data_type
	 *            the cdm_data_type to set
	 */
	public void setCdm_data_type(String cdm_data_type) {
		this.cdm_data_type = cdm_data_type;
	}

	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}

	/**
	 * @param project
	 *            the project to set
	 */
	public void setProject(String project) {
		this.project = project;
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
	 * @return the contributor role
	 */
	public String getContributor_role() {
		return contributor_role;
	}

	/**
	 * @param contributor_email
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
	 * @return the aditionalAttributes
	 */
	public HashMap<String, String> getAditionalAttributes() {
		return aditionalAttributes;
	}

	/**
	 * @param aditionalAttributes
	 *            the aditionalAttributes to set
	 */
	public void setAditionalAttributes(HashMap<String, String> aditionalAttributes) {
		this.aditionalAttributes = aditionalAttributes;
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
	 * Completes missing values using a profile info. Just the missing, empty or
	 * null
	 * 
	 * @param profile
	 */
	public boolean fixMissingValues(CodarRadialBean profile) {
		boolean todoOK = false;
		if (profile == null) {
			// cuidado, pasamos un nulo! no vale para nada!
		} else {
			if (getRangeResolutionKMeters() == null || getRangeResolutionKMeters().isEmpty()
					|| getRangeResolutionKMeters().equals("NaN"))
				setRangeResolutionKMeters(profile.getRangeResolutionKMeters());
			if (getAngularResolution() == null || getAngularResolution().isEmpty()
					|| getAngularResolution().equals("NaN"))
				setAngularResolution(profile.getAngularResolution());
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

			if (getGeodVersion() == null || getGeodVersion().isEmpty() || getGeodVersion().equals("NaN"))
				setGeodVersion(profile.getGeodVersion());
			if (getLLUVTrustData() == null || getLLUVTrustData().isEmpty() || getLLUVTrustData().equals("NaN"))
				setLLUVTrustData(profile.getLLUVTrustData());
			if (getRangeStart() == null || getRangeStart().isEmpty() || getRangeStart().equals("NaN"))
				setRangeStart(profile.getRangeStart());
			if (getRangeEnd() == null || getRangeEnd().isEmpty() || getRangeEnd().equals("NaN"))
				setRangeEnd(profile.getRangeEnd());
			if (getAntennaBearing() == null || getAntennaBearing().isEmpty() || getAntennaBearing().equals("NaN"))
				setAntennaBearing(profile.getAntennaBearing());
			if (getReferenceBearing() == null || getReferenceBearing().isEmpty() || getReferenceBearing().equals("NaN"))
				setReferenceBearing(profile.getReferenceBearing());
			if (getSpatialResolution() == null || getSpatialResolution().isEmpty()
					|| getSpatialResolution().equals("NaN"))
				setSpatialResolution(profile.getSpatialResolution());
			if (getPatternType() == null || getPatternType().isEmpty() || getPatternType().equals("NaN"))
				setPatternType(profile.getPatternType());
			if (getPatternDate() == null || getPatternDate().isEmpty() || getPatternDate().equals("NaN"))
				setPatternDate(profile.getPatternDate());
			if (getPatternResolution() == null || getPatternResolution().isEmpty()
					|| getPatternResolution().equals("NaN"))
				setPatternResolution(profile.getPatternResolution());

			if (getMinimunBearing() == null || getMinimunBearing().isEmpty() || getMinimunBearing().equals("NaN"))
				setMinimunBearing(profile.getMinimunBearing());
			if (getMaximunBearing() == null || getMaximunBearing().isEmpty() || getMaximunBearing().equals("NaN"))
				setMaximunBearing(profile.getMaximunBearing());
			if (getMinimunRange() == null || getMinimunRange().isEmpty() || getMinimunRange().equals("NaN"))
				setMinimunRange(profile.getMinimunRange());
			if (getMaximunRange() == null || getMaximunRange().isEmpty() || getMaximunRange().equals("NaN"))
				setMaximunRange(profile.getMaximunRange());

			if (getTransmitCenterFreqMHz() == null || getTransmitCenterFreqMHz().isEmpty()
					|| getTransmitCenterFreqMHz().equals("NaN"))
				setTransmitCenterFreqMHz(profile.getTransmitCenterFreqMHz());
			if (getDopplerResolutionHzPerBin() == null || getDopplerResolutionHzPerBin().isEmpty()
					|| getDopplerResolutionHzPerBin().equals("NaN"))
				setDopplerResolutionHzPerBin(profile.getDopplerResolutionHzPerBin());
			if (getFirstOrderMethod() == null || getFirstOrderMethod().isEmpty() || getFirstOrderMethod().equals("NaN"))
				setFirstOrderMethod(profile.getFirstOrderMethod());
			if (getBraggSmoothingPoints() == null || getBraggSmoothingPoints().isEmpty()
					|| getBraggSmoothingPoints().equals("NaN"))
				setBraggSmoothingPoints(profile.getBraggSmoothingPoints());
			if (getCurrentVelocityLimit() == null || getCurrentVelocityLimit().isEmpty()
					|| getCurrentVelocityLimit().equals("NaN"))
				setCurrentVelocityLimit(profile.getCurrentVelocityLimit());
			if (getBraggHasSecondOrder() == null || getBraggHasSecondOrder().isEmpty()
					|| getBraggHasSecondOrder().equals("NaN"))
				setBraggHasSecondOrder(profile.getBraggHasSecondOrder());
			if (getRadialBraggPeakDropOff() == null || getRadialBraggPeakDropOff().isEmpty()
					|| getRadialBraggPeakDropOff().equals("NaN"))
				setRadialBraggPeakDropOff(profile.getRadialBraggPeakDropOff());
			if (getRadialBraggPeakNull() == null || getRadialBraggPeakNull().isEmpty()
					|| getRadialBraggPeakNull().equals("NaN"))
				setRadialBraggPeakNull(profile.getRadialBraggPeakNull());
			if (getRadialBraggNoiseThreshold() == null || getRadialBraggNoiseThreshold().isEmpty()
					|| getRadialBraggNoiseThreshold().equals("NaN"))
				setRadialBraggNoiseThreshold(profile.getRadialBraggNoiseThreshold());
			if (getPatternAmplitudeCorrections() == null || getPatternAmplitudeCorrections().isEmpty()
					|| getPatternAmplitudeCorrections().equals("NaN"))
				setPatternAmplitudeCorrections(profile.getPatternAmplitudeCorrections());

			if (getPatternPhaseCorrections() == null || getPatternPhaseCorrections().isEmpty()
					|| getPatternPhaseCorrections().equals("NaN"))
				setPatternPhaseCorrections(profile.getPatternPhaseCorrections());
			if (getPatternAmplitudeCalculations() == null || getPatternAmplitudeCalculations().isEmpty()
					|| getPatternAmplitudeCalculations().equals("NaN"))
				setPatternAmplitudeCalculations(profile.getPatternAmplitudeCalculations());
			if (getPatternPhaseCalculations() == null || getPatternPhaseCalculations().isEmpty()
					|| getPatternPhaseCalculations().equals("NaN"))
				setPatternPhaseCalculations(profile.getPatternPhaseCalculations());
			if (getRadialMusicParameters() == null || getRadialMusicParameters().isEmpty()
					|| getRadialMusicParameters().equals("NaN"))
				setRadialMusicParameters(profile.getRadialMusicParameters());
			if (getMergedCount() == null || getMergedCount().isEmpty() || getMergedCount().equals("NaN"))
				setMergedCount(profile.getMergedCount());
			if (getRadialMinimumMergePoints() == null || getRadialMinimumMergePoints().isEmpty()
					|| getRadialMinimumMergePoints().equals("NaN"))
				setRadialMinimumMergePoints(profile.getRadialMinimumMergePoints());
			if (getFirstOrderCalc() == null || getFirstOrderCalc().isEmpty() || getFirstOrderCalc().equals("NaN"))
				setFirstOrderCalc(profile.getFirstOrderCalc());
			if (getMergeMethod() == null || getMergeMethod().isEmpty() || getMergeMethod().equals("NaN"))
				setMergeMethod(profile.getMergeMethod());
			if (getPatternMethod() == null || getPatternMethod().isEmpty() || getPatternMethod().equals("NaN"))
				setPatternMethod(profile.getPatternMethod());
			if (getTransmitSweepRateHz() == null || getTransmitSweepRateHz().isEmpty()
					|| getTransmitSweepRateHz().equals("NaN"))
				setTransmitSweepRateHz(profile.getTransmitSweepRateHz());

			if (getTransmitBandwidthKHz() == null || getTransmitBandwidthKHz().isEmpty()
					|| getTransmitBandwidthKHz().equals("NaN"))
				setTransmitBandwidthKHz(profile.getTransmitBandwidthKHz());
			if (getSpectraRangeCells() == null || getSpectraRangeCells().isEmpty()
					|| getSpectraRangeCells().equals("NaN"))
				setSpectraRangeCells(profile.getSpectraRangeCells());
			if (getSpectraDopplerCells() == null || getSpectraDopplerCells().isEmpty()
					|| getSpectraDopplerCells().equals("NaN"))
				setSpectraDopplerCells(profile.getSpectraDopplerCells());
			if (getTimeStamp() == null || getTimeStamp().isEmpty() || getTimeStamp().equals("NaN"))
				setTimeStamp(profile.getTimeStamp());
			if (getRadialSmoothingParameters() == null || getRadialSmoothingParameters().isEmpty()
					|| getRadialSmoothingParameters().equals("NaN"))
				setRadialSmoothingParameters(profile.getRadialSmoothingParameters());
			if (getPatternSmoothing() == null || getPatternSmoothing().isEmpty() || getPatternSmoothing().equals("NaN"))
				setPatternSmoothing(profile.getPatternSmoothing());
			if (getPatternUUID() == null || getPatternUUID().isEmpty() || getPatternUUID().equals("NaN"))
				setPatternUUID(profile.getPatternUUID());
			if (getProcessedTimeStamp() == null || getProcessedTimeStamp().isEmpty()
					|| getProcessedTimeStamp().equals("NaN"))
				setProcessedTimeStamp(profile.getProcessedTimeStamp());
			if (getProcessingTool() == null || getProcessingTool().isEmpty() || getProcessingTool().equals("NaN"))
				setProcessingTool(profile.getProcessingTool());
			// if(getTitle()== null || getTitle().isEmpty() ||
			// getTitle().equals("NaN")) setTitle(profile.getTitle());

			if (getInstitution() == null || getInstitution().isEmpty() || getInstitution().equals("NaN"))
				setInstitution(profile.getInstitution());
					
			// if(getConventions()== null || getConventions().isEmpty() ||
			// getConventions().equals("NaN"))
			// setConventions(profile.getConventions());
			if (getSummary() == null || getSummary().isEmpty() || getSummary().equals("NaN"))
				setSummary(profile.getSummary());
			// if(getSource()== null || getSource().isEmpty() ||
			// getSource().equals("NaN")) setSource(profile.getSource());
			if (getNetwork() == null || getNetwork().isEmpty() || getNetwork().equals("NaN"))
				setNetwork(profile.getNetwork());
			// if(getKeywords()== null || getKeywords().isEmpty() ||
			// getKeywords().equals("NaN")) setKeywords(profile.getKeywords());
			// if(getKeywords_vocabulary()== null ||
			// getKeywords_vocabulary().isEmpty() ||
			// getKeywords_vocabulary().equals("NaN"))
			// setKeywords_vocabulary(profile.getKeywords_vocabulary());
			// data_language="eng";
			// data_char_set="utf8";
			// topic_cat="oceans";
			// reference_sys="2EPSG:4806";
			// metadata_language="eng";
			// metadata_char_set="utf8";
			if (getMetadata_contact() == null || getMetadata_contact().isEmpty() || getMetadata_contact().equals("NaN"))
				setMetadata_contact(profile.getMetadata_contact());
			// netcdf_version="4.1.1";
			// netcdf_format=NetcdfFileWriter.Version.netcdf4_classic.name();
			// name_vocabulary="NetCDF Climate and Forecast (CF) Metadata
			// Convention Standard Name Table Version 1.6";
			// dateCreated;
			// dateCollected;
			// histCreated;

			if (getId() == null || getId().isEmpty() || getId().equals("NaN"))
				setId(profile.getId());
			if (getNaming_authority() == null || getNaming_authority().isEmpty() || getNaming_authority().equals("NaN"))
				setNaming_authority(profile.getNaming_authority());
			// cdm_data_type
			if (getProject() == null || getProject().isEmpty() || getProject().equals("NaN"))
				setProject(profile.getProject());
			if (getTime_coverage_start() == null || getTime_coverage_start().isEmpty()
					|| getTime_coverage_start().equals("NaN"))
				setTime_coverage_start(profile.getTime_coverage_start());
			if (getTime_coverage_end() == null || getTime_coverage_end().isEmpty()
					|| getTime_coverage_end().equals("NaN"))
				setTime_coverage_end(profile.getTime_coverage_end());
			// time_coverage_duration
			// time_coverage_resolution

			// date_modified
			if (getDate_issued() == null || getDate_issued().isEmpty() || getDate_issued().equals("NaN"))
				setDate_issued(profile.getDate_issued());
			// processing_level
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
			// metadata_convention
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
			if (Float.isNaN(radialTest.getVeloThreshold()))
				radialTest.setVeloThreshold(profile.getRadialTest().getVeloThreshold());
			if (Float.isNaN(radialTest.getVarianceThreshold()))
				radialTest.setVarianceThreshold(profile.getRadialTest().getVarianceThreshold());
			if (Float.isNaN(radialTest.getAvRadialBearingMin()))
				radialTest.setAvRadialBearingMin(profile.getRadialTest().getAvRadialBearingMin());
			if (Float.isNaN(radialTest.getAvRadialBearingMax()))
				radialTest.setAvRadialBearingMax(profile.getRadialTest().getAvRadialBearingMax());
			if (Float.isNaN(radialTest.getMedianFilter()))
				radialTest.setMedianFilter(profile.getRadialTest().getMedianFilter());
			if (Float.isNaN(radialTest.getTempThreshold()))
				radialTest.setTempThreshold(profile.getRadialTest().getTempThreshold());
			if (Float.isNaN(radialTest.getRadialCount()))
				radialTest.setRadialCount(profile.getRadialTest().getRadialCount());
			if (Float.isNaN(radialTest.getRcLim()))
				radialTest.setRcLim(profile.getRadialTest().getRcLim());
			if (Float.isNaN(radialTest.getAngLim()))
				radialTest.setAngLim(profile.getRadialTest().getAngLim());

			if (networkBean == null)
				setNetworkBean(profile.getNetworkBean());
			if (stationBean == null)
				setStationBean(profile.getStationBean());

			todoOK = true;
		}
		return todoOK;
	}

	/**
	 * @return the minimunBearing
	 */
	public String getMinimunBearing() {
		return minimunBearing;
	}

	/**
	 * @return the minimunBearing as float
	 */
	public Float getMinimunBearingAsFloat() throws NumberFormatException {
		Float vuelta = Float.NaN;
		if (this.minimunBearing != null) {
			String bb = this.minimunBearing;
			if (minimunBearing.trim().split("\\s+").length > 1) {
				bb = minimunBearing.trim().split("\\s+")[0];
			}
			vuelta = Float.parseFloat(bb);
		}
		return vuelta;
	}

	/**
	 * @param minimunBearing
	 *            the minimunBearing to set
	 */
	public void setMinimunBearing(String minimunBearing) {
		this.minimunBearing = minimunBearing;
	}

	/**
	 * @return the maximunBearing
	 */
	public String getMaximunBearing() {
		return maximunBearing;
	}

	/**
	 * @return the maximunBearing As float
	 */
	public Float getMaximunBearingAsFloat() {
		Float vuelta = Float.NaN;
		if (this.maximunBearing != null) {
			String bb = this.maximunBearing;
			if (maximunBearing.trim().split("\\s+").length > 1) {
				bb = maximunBearing.trim().split("\\s+")[0];
			}
			vuelta = Float.parseFloat(bb);
		}
		return vuelta;
	}

	/**
	 * @param maximunBearing
	 *            the maximunBearing to set
	 */
	public void setMaximunBearing(String maximunBearing) {
		this.maximunBearing = maximunBearing;
	}

	/**
	 * @return the minimunRange
	 */
	public String getMinimunRange() {
		return minimunRange;
	}

	/**
	 * @return the minimunRange As Float
	 */
	public Float getMinimunRangeAsFloat() {
		Float vuelta = Float.NaN;
		if (this.minimunRange != null) {
			String bb = this.minimunRange;
			if (minimunRange.trim().split("\\s+").length > 1) {
				bb = minimunRange.trim().split("\\s+")[0];
			}
			vuelta = Float.parseFloat(bb);
		}
		return vuelta;
	}

	/**
	 * @param minimunRange
	 *            the minimunRange to set
	 */
	public void setMinimunRange(String minimunRange) {
		this.minimunRange = minimunRange;
	}

	/**
	 * @return the maximunRange
	 */
	public String getMaximunRange() {
		return maximunRange;
	}

	/**
	 * @return the maximunRange As Float
	 */
	public Float getMaximunRangeAsFloat() {
		Float vuelta = Float.NaN;
		if (this.maximunRange != null) {
			String bb = this.maximunRange;
			if (maximunRange.trim().split("\\s+").length > 1) {
				bb = maximunRange.trim().split("\\s+")[0];
			}
			vuelta = Float.parseFloat(bb);
		}
		return vuelta;
	}

	/**
	 * @param maximunRange
	 *            the maximunRange to set
	 */
	public void setMaximunRange(String maximunRange) {
		this.maximunRange = maximunRange;
	}
}
