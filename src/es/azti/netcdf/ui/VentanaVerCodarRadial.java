/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  18 de may. de 2016
 */
package es.azti.netcdf.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import es.azti.codar.beans.CodarRadialBean;
import es.azti.codar.beans.CodarDataTableBean;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es) 18 de may. de 2016
 *
 *         Bean Radial data visualizer in a user interface window. It helps 
 *         modifying some data too.
 */
public class VentanaVerCodarRadial extends JFrame {

	private static final long serialVersionUID = 1L;
	// logger
	private static Logger log;
	private CodarRadialBean bean;
	private CodarRadialBean profile;

	public VentanaVerCodarRadial(CodarRadialBean datos, CodarRadialBean perfil) {
		// Start logger
		log = Logger.getLogger(VentanaVerCodarRadial.class);
		this.bean = datos;
		this.profile = perfil;
	}

	/**
	 * show data in a table
	 */
	@SuppressWarnings("serial")
	public void mostrarDatos() {
		try {
			setIconImage(ImageIO.read(new File("logoVentana.png")));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		setName("CODAR metadata visualizer");
		setTitle("CODAR metadata visualizer");
		setBounds(400, 100, 550, 400);

		JPanel tempPanel = new JPanel();

		GridLayout glayout = new GridLayout();
		glayout.setColumns(3);
		glayout.setRows(25);

		glayout.setHgap(5);
		glayout.setVgap(5);
		tempPanel.setLayout(glayout);

		// showing attributes

		// minimun range: the minimun to use to create the netcdf
		this.addRowDisplay(tempPanel, "NetCDF minimun Range ( >= 0 and Km): ", this.bean.getMinimunRange(),
				this.profile == null ? "0" : this.profile.getMinimunRange(), "MinimunRange", true, true);
		// maximun range: the maximun to use to create the netcdf
		String defaultValue = null;
		if (this.bean.getStationBean() != null) {
			defaultValue = Float.toString(Float.parseFloat(this.bean.getStationBean().getNumber_of_range_cells())
					* Float.parseFloat(this.bean.getRangeResolutionKMeters()));
		}
		this.addRowDisplay(tempPanel, "NetCDF maximun Range ( >=0 and Km): ", this.bean.getMaximunRange(),
				this.profile == null ? defaultValue : this.profile.getMaximunRange(), "MaximunRange", true, true);
		// minimun bearing for the netcdf grid
		this.addRowDisplay(tempPanel, "NetCDF minimun Bearing ( >= 0 and degrees): ", this.bean.getMinimunBearing(),
				this.profile == null ? "0" : this.profile.getMinimunBearing(), "MinimunBearing", true, true);

		// Maximun bearing for the netcdf grid
		this.addRowDisplay(tempPanel, "NetCDF maximun Bearing ( >= 0 and degrees): ", this.bean.getMaximunBearing(),
				this.profile == null ? "360" : this.profile.getMaximunBearing(), "MaximunBearing", true, true);

		// data mode: mandatory: Indicates if the file contais real time
		// provisional or delayed mode data.
		this.addRowDisplay(tempPanel, "Data Mode: ", this.bean.getData_mode(),
				this.profile == null ? null : this.profile.getData_mode(), "Data_mode", true, true);
		// patternDate
		this.addRowDisplay(tempPanel, "Pattern Date: ", this.bean.getPatternDate(),
				this.profile == null ? null : this.profile.getPatternDate(), "PatternDate", true, true);
		// source: mandatory: The term "coastal structure from the SeaVoX
		// Platform Categories (L06) list must be used for HFR data
		this.addRowDisplay(tempPanel, "Source: ", this.bean.getSource(),
				this.profile == null ? null : this.profile.getSource(), "Source", true, false);
		// naming authority
		this.addRowDisplay(tempPanel, "Naming Authority: ", this.bean.getNaming_authority(),
				this.profile == null ? null : this.profile.getNaming_authority(), "Naming_authority", false, true);
		// keywords: mandatory: Provicde comma-separadet list of terms that will
		// aid in discovery of the dataset.
		this.addRowDisplay(tempPanel, "Keywords: ", this.bean.getKeywords(),
				this.profile == null ? null : this.profile.getKeywords(), "Keywords", false, true);
		// keywords_vocabulary: recomended: Plkease use one of GCMD Science
		// Keywords, SeaDataNet Parameter Discovery Vocabulary or AGU Index
		// Terms.ç
		this.addRowDisplay(tempPanel, "Keywords Vocabulary", this.bean.getKeywords_vocabulary(),
				this.profile == null ? null : this.profile.getKeywords_vocabulary(), "Keywords_vocabulary", false,
				true);
		// data language: suggested: The language in which the data elements are
		// expressed
		this.addRowDisplay(tempPanel, "Data Language", this.bean.getData_language(),
				this.profile == null ? null : this.profile.getData_language(), "Data_language", false, true);
		// data character set: suggested: the character set used for expresising
		// data
		this.addRowDisplay(tempPanel, "Data Character Set: ", this.bean.getData_char_set(),
				this.profile == null ? null : this.profile.getData_char_set(), "Data_char_set", false, true);
		// Metadata language: sugested: the language in with the metadata
		// elements are expresed
		this.addRowDisplay(tempPanel, "Metadata Laguage", this.bean.getMetadata_language(),
				this.profile == null ? null : this.profile.getMetadata_language(), "Metadata_language", false, true);
		// metadata character set: suggested: the character set used for
		// expressing metadata
		this.addRowDisplay(tempPanel, "Metadata Character Set", this.bean.getMetadata_char_set(),
				this.profile == null ? null : this.profile.getMetadata_char_set(), "Metadata_char_set", false, true);
		// topic_category: sugested: ISO 19115 topic category
		this.addRowDisplay(tempPanel, "Topic Cathegory", this.bean.getTopic_cat(),
				this.profile == null ? null : this.profile.getTopic_cat(), "Topic_cat", false, true);
		// time coverage start: mandatory: start date of the data in UTC. Time
		// must be specified as a string according to he ISO8601 standard:
		// YYYY-MM-DDThh:mm:ssZ.
		this.addRowDisplay(tempPanel, "Time Coverage Start: ", this.bean.getTime_coverage_start(),
				this.profile == null ? null : this.profile.getTime_coverage_start(), "Time_coverage_start", true,
				false);
		// time coverage end: mandatory: Final date of the data in UTC, Time
		// must be specified as a string according to hte ISO8601 standard:
		// YYYY-MM-DDThh:mm:ssZ
		this.addRowDisplay(tempPanel, "Time Coverage End: ", this.bean.getTime_coverage_end(),
				this.profile == null ? null : this.profile.getTime_coverage_end(), "Time_coverage_end", true, false);
		// Publisher Name: recomended: Name of hte person responsible for
		// metadata and formatting of the data file.
		// this.addRowDisplay(tempPanel, "Publisher Name: ",
		// this.bean.getPublisher_name(),
		// this.profile==null?null:this.profile.getPublisher_name(),
		// "Publisher_name", true, true);
		// //Publisher Email: recomended: email address of the person
		// responsible for metadata and formattin gof the data file.
		// this.addRowDisplay(tempPanel, "Publisher Email: ",
		// this.bean.getPublisher_email(),
		// this.profile==null?null:this.profile.getPublisher_email(),
		// "Publisher_email", true, true);
		// //publisher URL: recomended: web ddress of the institution or of the
		// data publisher.
		// this.addRowDisplay(tempPanel, "Publisher URL: ",
		// this.bean.getPublisher_url(),
		// this.profile==null?null:this.profile.getPublisher_url(),
		// "Publisher_url", true, true);
		// history: Provides an audit trail for modifications to the original
		// data. It should contain a separate line for each modification, with
		// each line beginning with a timestmp, and including user name,
		// modification name, and modificatiuon arguments. The time stamp must
		// be specified as a string acording to the ISO8601 standard:
		// YYYY-MM-DDThh:mm:ssZ"
		this.addRowDisplay(tempPanel, "History: ", this.bean.getHistCreated(),
				this.profile == null ? null : this.profile.getHistCreated(), "HistCreated", true, false);
		// //contributors name: recomended: A semi colon separaed list of the
		// names of any individuals or institutions that contributed to the
		// creation of this data file
		// this.addRowDisplay(tempPanel, "Contributor Names: ",
		// this.bean.getContributor_name(),
		// this.profile==null?null:this.profile.getContributor_name(),
		// "Contributor_name", true, true);
		// //contributors roles: recomended: A semi colon separated list of
		// roles of any individuals or institutions mentioned in the previous
		// attribute
		// this.addRowDisplay(tempPanel, "Contributor Roles: ",
		// this.bean.getContributor_role(),
		// this.profile==null?null:this.profile.getContributor_role(),
		// "Contributor_role", true, true);
		// //contributors email: recomended: A semi colon separated list of
		// emails fo any individuals or institutions mentioned in the previous
		// attributes
		// this.addRowDisplay(tempPanel, "Contributor Emails: ",
		// this.bean.getContributor_email(),
		// this.profile==null?null:this.profile.getContributor_email(),
		// "Contributor_email", true, true);
		// angular resolution
		this.addRowDisplay(tempPanel, "Angular Resolution: ", this.bean.getAngularResolution(),
				this.profile == null ? null : this.profile.getAngularResolution(), "AngularResolution", true, true);
		// AntennaBearing
		this.addRowDisplay(tempPanel, "Anntena Bearing: ", this.bean.getAntennaBearing(),
				this.profile == null ? null : this.profile.getAntennaBearing(), "AntennaBearing", true, true);
		// referenceBearing
		this.addRowDisplay(tempPanel, "Reference Bearing: ", this.bean.getReferenceBearing(),
				this.profile == null ? null : this.profile.getReferenceBearing(), "ReferenceBearing", true, true);
		// rangeResolutionKMeters
		this.addRowDisplay(tempPanel, "Range Resolution Kilometers: ", this.bean.getRangeResolutionKMeters(),
				this.profile == null ? null : this.profile.getRangeResolutionKMeters(), "RangeResolutionKMeters", true,
				true);
		// rangeStart
		this.addRowDisplay(tempPanel, "Range Start: ", this.bean.getRangeStart(),
				this.profile == null ? null : this.profile.getRangeStart(), "RangeStart", true, true);
		// rangeEnd
		this.addRowDisplay(tempPanel, "Range End: ", this.bean.getRangeEnd(),
				this.profile == null ? null : this.profile.getRangeEnd(), "RangeEnd", true, true);
		// //braggHasSecondOrder
		// this.addRowDisplay(tempPanel, "Bragg Has Second Order: ",
		// this.bean.getBraggHasSecondOrder(),
		// this.profile==null?null:this.profile.getBraggHasSecondOrder(),
		// "BraggHasSecondOrder", true, true);
		// //braggSmoothingPoints
		// this.addRowDisplay(tempPanel, "Bragg Smoothing Points: ",
		// this.bean.getBraggSmoothingPoints(),
		// this.profile==null?null:this.profile.getBraggSmoothingPoints(),
		// "BraggSmoothingPoints", true, true);
		// //CTF
		// this.addRowDisplay(tempPanel, "CTF: ", this.bean.getCTF(),
		// this.profile==null?null:this.profile.getCTF(), "CTF", true, true);
		// //currentVelocityLimit
		// this.addRowDisplay(tempPanel, "Current Velocity Limit: ",
		// this.bean.getCurrentVelocityLimit(),
		// this.profile==null?null:this.profile.getCurrentVelocityLimit(),
		// "CurrentVelocityLimit", true, true);
		// //dopplerResolutionHzPerBin
		// this.addRowDisplay(tempPanel, "Doppler Resolution Hz Per Bin: ",
		// this.bean.getDopplerResolutionHzPerBin(),
		// this.profile==null?null:this.profile.getDopplerResolutionHzPerBin(),
		// "DopplerResolutionHzPerBin", true, true);
		// //fileType
		// this.addRowDisplay(tempPanel, "File Type: ", this.bean.getFileType(),
		// this.profile==null?null:this.profile.getFileType(), "FileType", true,
		// true);
		// //firstOrderCalc
		// this.addRowDisplay(tempPanel, "First Order Calc: ",
		// this.bean.getFirstOrderCalc(),
		// this.profile==null?null:this.profile.getFirstOrderCalc(),
		// "FirstOrderCalc", true, true);
		// //firstOrderMethod
		// this.addRowDisplay(tempPanel, "First Order Method: ",
		// this.bean.getFirstOrderMethod(),
		// this.profile==null?null:this.profile.getFirstOrderMethod(),
		// "FirstOrderMethod", true, true);
		// //geodVersion
		// this.addRowDisplay(tempPanel, "Geod Version: ",
		// this.bean.getGeodVersion(),
		// this.profile==null?null:this.profile.getGeodVersion(), "GeodVersion",
		// true, true);
		// //greatCircle
		// this.addRowDisplay(tempPanel, "Great Circle: ",
		// this.bean.getGreatCircle(),
		// this.profile==null?null:this.profile.getGreatCircle(), "GreatCircle",
		// true, true);
		// //LLUVSpec
		// this.addRowDisplay(tempPanel, "LLUVSpec: ", this.bean.getLLUVSpec(),
		// this.profile==null?null:this.profile.getLLUVSpec(), "LLUVSpec", true,
		// true);
		// //LLUVTrustData
		// this.addRowDisplay(tempPanel, "LLUVTrustData: ",
		// this.bean.getLLUVTrustData(),
		// this.profile==null?null:this.profile.getLLUVTrustData(),
		// "LLUVTrustData", true, true);
		// //manufacturer
		// this.addRowDisplay(tempPanel, "Manufacturer: ",
		// this.bean.getManufacturer(),
		// this.profile==null?null:this.profile.getManufacturer(),
		// "Manufacturer", true, true);
		// //mergedCount
		// this.addRowDisplay(tempPanel, "Merged Count: ",
		// this.bean.getMergedCount(),
		// this.profile==null?null:this.profile.getMergedCount(), "MergedCount",
		// true, true);
		// //mergeMethod
		// this.addRowDisplay(tempPanel, "Merge Method: ",
		// this.bean.getMergeMethod(),
		// this.profile==null?null:this.profile.getMergeMethod(), "MergeMethod",
		// true, true);
		// //origin
		// this.addRowDisplay(tempPanel, "Origin: ", this.bean.getOrigin(),
		// this.profile==null?null:this.profile.getOrigin(), "Origin", true,
		// true);
		// //patternAmplitudeCalculations
		// this.addRowDisplay(tempPanel, "Pattern Amplitude Calculations: ",
		// this.bean.getPatternAmplitudeCalculations(),
		// this.profile==null?null:this.profile.getPatternAmplitudeCalculations(),
		// "PatternAmplitudeCalculations", true, true);
		// //patternAmplitudeCorrections
		// this.addRowDisplay(tempPanel, "Pattern Amplitude Corrections: ",
		// this.bean.getPatternAmplitudeCorrections(),
		// this.profile==null?null:this.profile.getPatternAmplitudeCorrections(),
		// "PatternAmplitudeCorrections", true, true);
		// //patternMethod
		// this.addRowDisplay(tempPanel, "Pattern Method: ",
		// this.bean.getPatternMethod(),
		// this.profile==null?null:this.profile.getPatternMethod(),
		// "PatternMethod", true, true);
		// //patternPhaseCalculations
		// this.addRowDisplay(tempPanel, "Pattern Phase Calculations: ",
		// this.bean.getPatternPhaseCalculations(),
		// this.profile==null?null:this.profile.getPatternPhaseCalculations(),
		// "PatternPhaseCalculations", true, true);
		// //patternPhaseCorrections
		// this.addRowDisplay(tempPanel, "Pattern Phase Corrections: ",
		// this.bean.getPatternPhaseCorrections(),
		// this.profile==null?null:this.profile.getPatternPhaseCorrections(),
		// "PatternPhaseCorrections", true, true);
		// //patternResolution
		// this.addRowDisplay(tempPanel, "Pattern Resolution: ",
		// this.bean.getPatternResolution(),
		// this.profile==null?null:this.profile.getPatternResolution(),
		// "PatternResolution", true, true);
		// //patternSmoothing
		// this.addRowDisplay(tempPanel, "Pattern Smoothing: ",
		// this.bean.getPatternSmoothing(),
		// this.profile==null?null:this.profile.getPatternSmoothing(),
		// "PatternSmoothing", true, true);
		// //patternType
		// this.addRowDisplay(tempPanel, "Pattern Type: ",
		// this.bean.getPatternType(),
		// this.profile==null?null:this.profile.getPatternType(), "PatternType",
		// true, true);
		// //patternUUID
		// this.addRowDisplay(tempPanel, "Pattern UUID: ",
		// this.bean.getPatternUUID(),
		// this.profile==null?null:this.profile.getPatternUUID(), "PatternUUID",
		// true, true);
		// //processedTimeStamp
		// this.addRowDisplay(tempPanel, "Processed Time Stamp: ",
		// this.bean.getProcessedTimeStamp(),
		// this.profile==null?null:this.profile.getProcessedTimeStamp(),
		// "ProcessedTimeStamp", true, true);
		// //processingTool
		// this.addRowDisplay(tempPanel, "Processing Tool: ",
		// this.bean.getProcessingTool(),
		// this.profile==null?null:this.profile.getProcessingTool(),
		// "ProcessingTool", true, true);
		// //radialBraggNoiseThreshold
		// this.addRowDisplay(tempPanel, "Radial Bragg Noise Threshold: ",
		// this.bean.getRadialBraggNoiseThreshold(),
		// this.profile==null?null:this.profile.getRadialBraggNoiseThreshold(),
		// "RadialBraggNoiseThreshold", true, true);
		// //radialBraggPeakDropOff
		// this.addRowDisplay(tempPanel, "Radial Bragg Peak Drop Off: ",
		// this.bean.getRadialBraggPeakDropOff(),
		// this.profile==null?null:this.profile.getRadialBraggPeakDropOff(),
		// "RadialBraggPeakDropOff", true, true);
		// //radialBraggPeakNull
		// this.addRowDisplay(tempPanel, "Radial Bragg Peak Null: ",
		// this.bean.getRadialBraggPeakNull(),
		// this.profile==null?null:this.profile.getRadialBraggPeakNull(),
		// "RadialBraggPeakNull", true, true);
		// //radialMinimumMergePoints
		// this.addRowDisplay(tempPanel, "Radial Minimum Merge Points: ",
		// this.bean.getRadialMinimumMergePoints(),
		// this.profile==null?null:this.profile.getRadialMinimumMergePoints(),
		// "RadialMinimumMergePoints", true, true);
		// //radialMusicParameters
		// this.addRowDisplay(tempPanel, "Radial Music Parameters: ",
		// this.bean.getRadialMusicParameters(),
		// this.profile==null?null:this.profile.getRadialMusicParameters(),
		// "RadialMusicParameters", true, true);
		// //radialSmoothingParameters
		//// this.addRowDisplay(tempPanel, "Radial Smoothing Parameters: ",
		// this.bean.getRadialSmoothingParameters(),
		//// this.profile==null?null:this.profile.getRadialSmoothingParameters(),
		// "RadialSmoothingParameters", true, true);
		// //site
		// this.addRowDisplay(tempPanel, "Site: ", this.bean.getSite(),
		// this.profile==null?null:this.profile.getSite(), "Site", true, true);
		// //spatialResolution
		// this.addRowDisplay(tempPanel, "Spatial Resolution: ",
		// this.bean.getSpatialResolution(),
		// this.profile==null?null:this.profile.getSpatialResolution(),
		// "SpatialResolution", true, true);
		// //spectraDopplerCells
		// this.addRowDisplay(tempPanel, "Spectra Doppler Cells: ",
		// this.bean.getSpectraDopplerCells(),
		// this.profile==null?null:this.profile.getSpectraDopplerCells(),
		// "SpectraDopplerCells", true, true);
		// //spectraRangeCells
		// this.addRowDisplay(tempPanel, "Spectra Range Cells: ",
		// this.bean.getSpectraRangeCells(),
		// this.profile==null?null:this.profile.getSpectraRangeCells(),
		// "SpectraRangeCells", true, true);
		// //timeCoverage
		// this.addRowDisplay(tempPanel, "Time Coverage: ",
		// this.bean.getTimeCoverage(),
		// this.profile==null?null:this.profile.getTimeCoverage(),
		// "TimeCoverage", true, true);
		// //timeStamp
		// this.addRowDisplay(tempPanel, "Time Stamp: ",
		// this.bean.getTimeStamp(),
		// this.profile==null?null:this.profile.getTimeStamp(), "TimeStamp",
		// true, true);
		// //timeZone
		// this.addRowDisplay(tempPanel, "Time Zone: ", this.bean.getTimeZone(),
		// this.profile==null?null:this.profile.getTimeZone(), "TimeZone", true,
		// true);
		// //transmitBandwidthKHz
		// this.addRowDisplay(tempPanel, "Transmit Bandwidth KHz: ",
		// this.bean.getTransmitBandwidthKHz(),
		// this.profile==null?null:this.profile.getTransmitBandwidthKHz(),
		// "TransmitBandwidthKHz", true, true);
		// //transmitCenterFreqMHz
		// this.addRowDisplay(tempPanel, "Transmit Center Freq MHz: ",
		// this.bean.getTransmitCenterFreqMHz(),
		// this.profile==null?null:this.profile.getTransmitCenterFreqMHz(),
		// "TransmitCenterFreqMHz", true, true);
		// //transmitSweepRateHz
		// this.addRowDisplay(tempPanel, "Transmit Sweep Rate Hz: ",
		// this.bean.getTransmitSweepRateHz(),
		// this.profile==null?null:this.profile.getTransmitSweepRateHz(),
		// "TransmitSweepRateHz", true, true);
		// //UUID
		// this.addRowDisplay(tempPanel, "UUID: ", this.bean.getUUID(),
		// this.profile==null?null:this.profile.getUUID(), "UUID", true, true);
		//
		// //title: mandatory metadata: free format text describing the dataset,
		// for use by uman readers. Use the file name if in doubt.
		// this.addRowDisplay(tempPanel, "Title: ", this.bean.getTitle(),
		// this.profile==null?null:this.profile.getTitle(), "Title", true,
		// true);
		// //institution: mandatory: Specifies institution where the original
		// data was produced.
		// this.addRowDisplay(tempPanel, "Institution: ",
		// this.bean.getInstitution(),
		// this.profile==null?null:this.profile.getInstitution(), "Institution",
		// true, true);
		// //conventions: mandatory: names of the conventions followd by the
		// dataset
		// this.addRowDisplay(tempPanel, "Conventions: ",
		// this.bean.getConventions(),
		// this.profile==null?null:this.profile.getConventions(), "Conventions",
		// true, false);
		// //summary: mandatory: Longer free format text describing the dataset.
		// This attribute should allow data discovery for a huan reader. A
		// paragraph of up to 100 words is appropiate.
		// this.addRowDisplay(tempPanel, "Summary: ", this.bean.getSummary(),
		// this.profile==null?null:this.profile.getSummary(), "Summary", true,
		// true);
		// //network: mandatory: A grouping of sites based on common shore-based
		// logistics or infraestructure
		// this.addRowDisplay(tempPanel, "Network: ", this.bean.getNetwork(),
		// this.profile==null?null:this.profile.getNetwork(), "Network", true,
		// true);
		// //reference system: sugested: ESPG coordinate reference system
		// this.addRowDisplay(tempPanel, "Reference System",
		// this.bean.getReference_sys(),
		// this.profile==null?null:this.profile.getReference_sys(),
		// "Reference_sys", false, true);
		// //metadata contact: addition:
		// this.addRowDisplay(tempPanel, "Metadata Contact: ",
		// this.bean.getMetadata_contact(),
		// this.profile==null?null:this.profile.getMetadata_contact(),
		// "Metadata_contact", false, true);
		// //netcdf format: recomended: NetCDF format used for the dataset
		// this.addRowDisplay(tempPanel, "NetCDF Format",
		// this.bean.getNetcdf_format(),
		// this.profile==null?null:this.profile.getNetcdf_format(),
		// "Netcdf_format", false, false);
		// //netcdf version: recomended: NetCDF version used for the dataset.
		// this.addRowDisplay(tempPanel, "NetCDF Version",
		// this.bean.getNetcdf_version(),
		// this.profile==null?null:this.profile.getNetcdf_version(),
		// "Netcdf_version", false, false);
		// //id and naming authority: recommended: the id and naming authority
		// (organization that manages data set names (ACDD), attributes are
		// intended to provide a globally unique identificcation for each
		// dataset. The id may be the file name without .nc suffix which is
		// designed to be unique (ACDD)
		// this.addRowDisplay(tempPanel, "Id: ", this.bean.getId(),
		// this.profile==null?null:this.profile.getId(), "Id", false, false);
		// //CDM data type: recomended: the u nidata cdm (common data model)
		// data type used by THREDDS. e.g. point, profile, section. station,
		// station_profile, trajectory, grid, radial, swath, image, use Grid for
		// gridded HRF data (ACDD)
		// this.addRowDisplay(tempPanel, "CDM Data Type: ",
		// this.bean.getCdm_data_type(),
		// this.profile==null?null:this.profile.getCdm_data_type(),
		// "Cdm_data_type", false, true);
		// //project: recommended: the scientific project that produced the data
		// this.addRowDisplay(tempPanel, "Project: ", this.bean.getProject(),
		// this.profile==null?null:this.profile.getProject(), "Project", false,
		// true);
		// //time coverage duration: recomended: duration of the data in UTC,
		// Time must be specified as a string according to hte ISO8601 standard:
		// YYYY-MM-DDThh:mm:ssZ
		// this.addRowDisplay(tempPanel, "Time Coverage duration: ",
		// this.bean.getTime_coverage_duration(),
		// this.profile==null?null:this.profile.getTime_coverage_duration(),
		// "Time_coverage_duration", false, false);
		// //time coverage resolution: recomended: resolution of the data in
		// UTC, Time must be specified as a string according to hte ISO8601
		// standard: YYYY-MM-DDThh:mm:ssZ
		// this.addRowDisplay(tempPanel, "Time Coverage resolution: ",
		// this.bean.getTime_coverage_resolution(),
		// this.profile==null?null:this.profile.getTime_coverage_resolution(),
		// "Time_coverage_resolution", false, false);
		// //date modified: recommended: the date on which the data file was
		// last modified. Time must be specified as a string according to the
		// ISO 8601 standard: YYYY-MM-DDThh:mm:ssZ
		//// this.addRowDisplay(tempPanel, "Date Modified: ",
		// this.bean.getDate_modified(), "date_modified", false, false);
		// //processing level: mandatory: procesing level of the data according
		// to the european standar. 2A
		// this.addRowDisplay(tempPanel, "Processing Level: ",
		// this.bean.getProcessing_level(),
		// this.profile==null?null:this.profile.getProcessing_level(),
		// "processing_level", true, false);
		// //Acknowledgment: recommended: a place to acknowledge various types
		// of support for the project that produced this data
		// this.addRowDisplay(tempPanel, "Acknowledment: ",
		// this.bean.getAcknowledgment(),
		// this.profile==null?null:this.profile.getAcknowledgment(),
		// "Acknowledgment", false, true);
		// //Comment: recomended: Miscellaneous informatoin about the data of
		// methods used to produce it. Any free format text is appropiate.
		// this.addRowDisplay(tempPanel, "Comment: ", this.bean.getComments(),
		// this.profile==null?null:this.profile.getComments(), "Comments",
		// false, true);
		// //metadata convention: additional:
		//// this.addRowDisplay(tempPanel, "Metadata Convention: ",
		// this.bean.getMetadata_convention(), "metadata_convention", false,
		// true);
		// //platform: mandatory: The platform code is used for indexing the
		// files, and for data synchronization between the disgribution units
		// (te regios of the insitu TAC). Therefore it has to be unique for each
		// platform, and common among the insitu TAC. For the HFR data, a
		// platfor for ech anntenna for the radial current data files an
		// danother one for the site for the total current data files are
		// required.
		// this.addRowDisplay(tempPanel, "Platform: ", this.bean.getPlatform(),
		// this.profile==null?null:this.profile.getPlatform(), "Platform", true,
		// true);
		// //sensor: mandatory: the sensor used to get hte HFR data
		// this.addRowDisplay(tempPanel, "Sensor: ", this.bean.getSensor(),
		// this.profile==null?null:this.profile.getSensor(), "Sensor", false,
		// true);
		// //citation: mandatory: the citation to be used in publications using
		// the dataset.
		// this.addRowDisplay(tempPanel, "Citation: ", this.bean.getCitation(),
		// this.profile==null?null:this.profile.getCitation(), "Citation", true,
		// true);
		// //operational manager: additional
		// this.addRowDisplay(tempPanel, "Operational Manager: ",
		// this.bean.getOperational_manager(),
		// this.profile==null?null:this.profile.getOperational_manager(),
		// "Operational_manager", false, true);
		// //operational manager email: addtional
		// this.addRowDisplay(tempPanel, "Operational Manager Email: ",
		// this.bean.getOperational_manager_email(),
		// this.profile==null?null:this.profile.getOperational_manager_email(),
		// "Operational_manager_email", false, true);
		// //format version:mandatory, verson of the data model release
		// this.addRowDisplay(tempPanel, "Format Version: ",
		// this.bean.getFormat_version(),
		// this.profile==null?null:this.profile.getFormat_version(),
		// "Format_version", true, false);
		// //update interval: mandatory: update interval for the file, in ISO
		// 8601 interval format. where elements that are 0 may be omited. Use
		// void for data that are not updated on a schedule used by inventory
		// software
		// this.addRowDisplay(tempPanel, "Update Interval: ",
		// this.bean.getUpdate_interval(),
		// this.profile==null?null:this.profile.getUpdate_interval(),
		// "Update_interval", true, true);
		// //site code: mandatory, the site code attribute is a code for each of
		// the fixed measurement sites within the oceansites project As show on
		// the map at www.oceansites.org CMEMS requires this attribute, but the
		// specific site codes for HFR sites and networks are in the process fo
		// bein generated. In the meanwhile the attrbiute has to be left empty.
		//// this.addRowDisplay(tempPanel, "Site Code: ",
		// this.bean.getSite_code(), "site_code", true, true);
		// //area, additional:
		// this.addRowDisplay(tempPanel, "Area: ", this.bean.getArea(),
		// this.profile==null?null:this.profile.getArea(), "Area", false, true);
		// //Referencces; additional
		//// this.addRowDisplay(tempPanel, "References: ",
		// this.bean.getReferences(), "references", false, true);
		//
		//
		//

		// Visualize data table.
		tempPanel.add(new JLabel("Loaded Radial Data: "));
		JButton loadFile = new JButton("View Radial data table");
		loadFile.setEnabled(true);
		loadFile.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viewTable(bean.getTable());
			}
		});
		tempPanel.add(loadFile);

		JScrollPane panel = new JScrollPane(tempPanel);
		panel.setPreferredSize(new Dimension(900, 600));
		panel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setContentPane(panel);

		this.pack();
		this.setVisible(true);

	}

	/**
	 * Visualize the data table, not editable
	 * 
	 * @param tabla
	 */
	private static void viewTable(CodarDataTableBean tabla) {
		String[] columnNames = tabla.getColumnTypes();
		Object[][] data = tabla.getData();

		@SuppressWarnings("serial")
		JTable table = new JTable(data, columnNames) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}

			// Implement table cell tool tips.
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);
				try {
					tip = getValueAt(rowIndex, colIndex).toString();
				} catch (RuntimeException e1) {
					// catch null pointer exception if mouse is over an empty
					// line
				}

				return tip;
			}
		};
		table.setFillsViewportHeight(true);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(900, 600));

		JFrame frame = new JFrame();
		try {
			frame.setIconImage(ImageIO.read(new File("logoVentana.png")));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		frame.setTitle("CODAR RADIAL DATA");
		frame.setName("CODAR RADIAL DATA");
		frame.setContentPane(scrollPane);
		frame.setBounds(400, 100, 550, 400);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * generic method to add a row to the metadata table three cells are added,
	 * one for the name, one for the value and a last button to modify if
	 * needed.
	 * 
	 * @param panel
	 * @param label
	 * @param value
	 * @param defaultValue
	 * @param id
	 * @param isMandatory
	 * @param isEditable
	 */
	@SuppressWarnings("serial")
	private void addRowDisplay(JPanel panel, String label, String value, String defaultValue, String id,
			boolean isMandatory, boolean isEditable) {

		JLabel labelText = new JLabel();
		if ((value == null || value.isEmpty()) && (defaultValue != null && !defaultValue.isEmpty())) {
			labelText.setForeground(new Color(255, 102, 0));
			value = defaultValue;
		} else if (isMandatory && (value == null || value.isEmpty())) {
			labelText.setForeground(Color.red);
		} else {
			labelText.setForeground(Color.black);
		}
		labelText.setText(label);
		panel.add(labelText);

		JLabel valueText = new JLabel();
		String valueShort = value != null && value.length() > 50 ? value.substring(0, 50) + "..." : value;
		valueText.setText(valueShort);
		valueText.setToolTipText(value);
		valueText.setForeground(labelText.getForeground());
		panel.add(valueText);

		JButton btnVer = new JButton("Edit field");
		btnVer.setEnabled(isEditable);
		btnVer.setForeground(labelText.getForeground());
		btnVer.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modifyField(id, bean, valueText, labelText, isMandatory, btnVer);
				// panel.repaint();
			}
		});
		panel.add(btnVer);

	}

	/**
	 * generi method to modify a value of a metadata.
	 * 
	 * @param id
	 * @param bean
	 * @param valueText
	 * @param labelText
	 * @param isMandatory
	 * @param btnVer
	 */
	@SuppressWarnings("serial")
	private static void modifyField(String id, CodarRadialBean bean, JLabel valueText, JLabel labelText,
			boolean isMandatory, JButton btnVer) {

		JFrame frame = new JFrame();
		try {
			frame.setIconImage(ImageIO.read(new File("logoVentana.png")));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		frame.setTitle("MODIFY FIELD");
		frame.setName("MODIFY FIELD");

		frame.setAlwaysOnTop(true);
		JPanel contentPane = new JPanel();

		GridLayout glayout = new GridLayout();
		glayout.setColumns(1);
		glayout.setRows(3);

		glayout.setHgap(5);
		glayout.setVgap(5);
		contentPane.setLayout(glayout);

		JLabel description = new JLabel();
		description.setText("Change the value of the attribute: " + id);
		contentPane.add(description);

		JTextField value = new JTextField(valueText.getText());
		contentPane.add(value);

		JButton btnChange = new JButton("Change value");
		btnChange.setEnabled(true);
		btnChange.setSize(new Dimension(10, 30));
		btnChange.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String setter = "set" + id;
				Method method;
				String actualizado = value.getText();
				try {
					method = CodarRadialBean.class.getMethod(setter, new Class[] { String.class });
					method.invoke(bean, actualizado);
					valueText.setText(actualizado);
					if (actualizado != null && !actualizado.isEmpty()) {
						// if all goes fine, check value and change foreground
						// color if necesary:
						labelText.setForeground(Color.black);
					} else if (isMandatory) {
						labelText.setForeground(Color.red);
					}
					valueText.setForeground(labelText.getForeground());
					btnVer.setForeground(labelText.getForeground());
				} catch (IllegalAccessException ex) {
					log.error("Illegal Acces - attribute not set", ex);
				} catch (IllegalArgumentException ex) {
					log.error("Illegal argument - attribute not set", ex);
				} catch (InvocationTargetException ex) {
					log.error("Target error  - attribute not set", ex);
				} catch (NoSuchMethodException ex) {
					log.error("Getter not found - attribute not set", ex);
				} catch (SecurityException ex) {
					log.error("Security error - attribute not set", ex);
				}
				frame.setVisible(false);
				frame.dispose();
			}
		});
		contentPane.add(btnChange);

		frame.setContentPane(contentPane);
		frame.setBounds(400, 100, 550, 400);
		frame.pack();
		frame.setVisible(true);

	}
}
