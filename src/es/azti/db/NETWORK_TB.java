/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  30 de ago. de 2018
 */
package es.azti.db;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class to retrieve data from NETWORK_TB table in the european node ddbb
 * @author Jose Luis Asensio (jlasensio@azti.es) 30 de ago. de 2018
 *
 */
@SuppressWarnings("serial")
public class NETWORK_TB extends DataBaseBean implements Serializable {

	private String network_id;
	private String EDIOS_Series_id;
	private String EDMO_code;
	private String metadata_page;
	private String DoA_estimation_method;
	private String calibration_type;
	private String calibration_link;
	private String last_calibration_date;
	private String title;
	private String summary;
	private String institution_name;
	private String citation_statement;
	private String license;
	private String acknowledgment;
	private String total_QC_velocity_threshold;
	private String total_QC_GDOP_threshold;
	private String total_QC_variance_threshold;
	private String total_QC_temporal_derivative_threshold;
	private String total_QC_data_density_threshold;
	private String project;
	private String institution_website;
	private String comment;
	private String network_name;
	private String area;
	private String geospatial_lon_min;
	private String geospatial_lon_max;
	private String geospatial_lat_min;
	private String geospatial_lat_max;
	private String grid_resolution;
	private String temporal_resolution;
	private String region_bigram;
	private String combination_search_radius;
	private String total_input_folder_path;
	private String total_HFRnetCDF_folder_path;
	private String total_mat_folder_path;
	private String contributor_name;
	private String contributor_role;
	private String contributor_email;

	/**
	 * @return the contributor_name
	 */
	public String getContributor_name() {
		return contributor_name;
	}

	/**
	 * @param contributor_name the contributor_name to set
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
	 * @param contributor_role the contributor_role to set
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
	 * @param contributor_email the contributor_email to set
	 */
	public void setContributor_email(String contributor_email) {
		this.contributor_email = contributor_email;
	}

	/**
	 * @return the network_id
	 */
	public String getNetwork_id() {
		return network_id;
	}

	/**
	 * @param network_id
	 *            the network_id to set
	 */
	public void setNetwork_id(String network_id) {
		this.network_id = network_id;
	}

	/**
	 * @return the eDIOS_Series_id
	 */
	public String getEDIOS_Series_id() {
		return EDIOS_Series_id;
	}

	/**
	 * @param eDIOS_Series_id
	 *            the eDIOS_Series_id to set
	 */
	public void setEDIOS_Series_id(String eDIOS_Series_id) {
		EDIOS_Series_id = eDIOS_Series_id;
	}

	/**
	 * @return the eDMO_code
	 */
	public String getEDMO_code() {
		return EDMO_code;
	}

	public Short getEDMO_code_As_Short() {
		return Short.parseShort(EDMO_code);
	}

	/**
	 * @param eDMO_code
	 *            the eDMO_code to set
	 */
	public void setEDMO_code(String eDMO_code) {
		EDMO_code = eDMO_code;
	}

	/**
	 * @return the metadata_page
	 */
	public String getMetadata_page() {
		return metadata_page.trim();
	}

	/**
	 * @param metadata_page
	 *            the metadata_page to set
	 */
	public void setMetadata_page(String metadata_page) {
		this.metadata_page = metadata_page;
	}

	/**
	 * @return the doA_estimation_method
	 */
	public String getDoA_estimation_method() {
		return DoA_estimation_method;
	}

	/**
	 * @param doA_estimation_method
	 *            the doA_estimation_method to set
	 */
	public void setDoA_estimation_method(String doA_estimation_method) {
		DoA_estimation_method = doA_estimation_method;
	}

	/**
	 * @return the calibration_type
	 */
	public String getCalibration_type() {
		return calibration_type;
	}

	/**
	 * @param calibration_type
	 *            the calibration_type to set
	 */
	public void setCalibration_type(String calibration_type) {
		this.calibration_type = calibration_type;
	}

	/**
	 * @return the calibration_link
	 */
	public String getCalibration_link() {
		return calibration_link;
	}

	/**
	 * @param calibration_link
	 *            the calibration_link to set
	 */
	public void setCalibration_link(String calibration_link) {
		this.calibration_link = calibration_link;
	}

	/**
	 * @return the last_calibration_date
	 */
	public String getLast_calibration_date() {
		return last_calibration_date;
	}

	/**
	 * @param last_calibration_date
	 *            the last_calibration_date to set
	 */
	public void setLast_calibration_date(String last_calibration_date) {
		this.last_calibration_date = last_calibration_date;
	}

	/**
	 * parses the calibration date stored in the data base and transforms to the UTC ISO 8601
	 * @return
	 */
	public String getLasCalibrationDateUTC() {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		hourFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date coverageStart = Calendar.getInstance().getTime();
		try {
			coverageStart = dateFormat.parse(last_calibration_date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dateFormat.format(coverageStart.getTime()) + "T" + hourFormat.format(coverageStart.getTime()) + "Z";
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
	 * @return the institution_name
	 */
	public String getInstitution_name() {
		return institution_name;
	}

	/**
	 * @param institution_name
	 *            the institution_name to set
	 */
	public void setInstitution_name(String institution_name) {
		this.institution_name = institution_name;
	}

	/**
	 * @return the citation_statement
	 */
	public String getCitation_statement() {
		return citation_statement;
	}

	/**
	 * @param citation_statement
	 *            the citation_statement to set
	 */
	public void setCitation_statement(String citation_statement) {
		this.citation_statement = citation_statement;
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
	 * @return the total_QC_velocity_threshold
	 */
	public String getTotal_QC_velocity_threshold() {
		return total_QC_velocity_threshold;
	}

	/**
	 * @param total_QC_velocity_threshold
	 *            the total_QC_velocity_threshold to set
	 */
	public void setTotal_QC_velocity_threshold(String total_QC_velocity_threshold) {
		this.total_QC_velocity_threshold = total_QC_velocity_threshold;
	}

	/**
	 * @return the total_QC_GDOP_threshold
	 */
	public String getTotal_QC_GDOP_threshold() {
		return total_QC_GDOP_threshold;
	}

	/**
	 * @param total_QC_GDOP_threshold
	 *            the total_QC_GDOP_threshold to set
	 */
	public void setTotal_QC_GDOP_threshold(String total_QC_GDOP_threshold) {
		this.total_QC_GDOP_threshold = total_QC_GDOP_threshold;
	}

	/**
	 * @return the total_QC_variance_threshold
	 */
	public String getTotal_QC_variance_threshold() {
		return total_QC_variance_threshold;
	}

	/**
	 * @param total_QC_variance_threshold
	 *            the total_QC_variance_threshold to set
	 */
	public void setTotal_QC_variance_threshold(String total_QC_variance_threshold) {
		this.total_QC_variance_threshold = total_QC_variance_threshold;
	}

	/**
	 * @return the total_QC_temporal_derivative_threshold
	 */
	public String getTotal_QC_temporal_derivative_threshold() {
		return total_QC_temporal_derivative_threshold;
	}

	/**
	 * @return the total_QC_temporal_derivative_threshold
	 */
	public Float getTotal_QC_temporal_derivative_threshold_float() {
		return Float.parseFloat(total_QC_temporal_derivative_threshold);
	}

	/**
	 * @param total_QC_temporal_derivative_threshold
	 *            the total_QC_temporal_derivative_threshold to set
	 */
	public void setTotal_QC_temporal_derivative_threshold(String total_QC_temporal_derivative_threshold) {
		this.total_QC_temporal_derivative_threshold = total_QC_temporal_derivative_threshold;
	}

	/**
	 * @return the total_QC_data_density_threshold
	 */
	public String getTotal_QC_data_density_threshold() {
		return total_QC_data_density_threshold;
	}

	/**
	 * @param total_QC_data_density_threshold
	 *            the total_QC_data_density_threshold to set
	 */
	public void setTotal_QC_data_density_threshold(String total_QC_data_density_threshold) {
		this.total_QC_data_density_threshold = total_QC_data_density_threshold;
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
	 * @return the institution_website
	 */
	public String getInstitution_website() {
		return institution_website;
	}

	/**
	 * @param institution_website
	 *            the institution_website to set
	 */
	public void setInstitution_website(String institution_website) {
		this.institution_website = institution_website;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the network_name
	 */
	public String getNetwork_name() {
		return network_name;
	}

	/**
	 * @param network_name
	 *            the network_name to set
	 */
	public void setNetwork_name(String network_name) {
		this.network_name = network_name;
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
	 * @return the geospatial_lon_min
	 */
	public String getGeospatial_lon_min() {
		return geospatial_lon_min;
	}

	/**
	 * @param geospatial_lon_min
	 *            the geospatial_lon_min to set
	 */
	public void setGeospatial_lon_min(String geospatial_lon_min) {
		this.geospatial_lon_min = geospatial_lon_min;
	}

	/**
	 * @return the geospatial_lon_max
	 */
	public String getGeospatial_lon_max() {
		return geospatial_lon_max;
	}

	/**
	 * @param geospatial_lon_max
	 *            the geospatial_lon_max to set
	 */
	public void setGeospatial_lon_max(String geospatial_lon_max) {
		this.geospatial_lon_max = geospatial_lon_max;
	}

	/**
	 * @return the geospatial_lat_min
	 */
	public String getGeospatial_lat_min() {
		return geospatial_lat_min;
	}

	/**
	 * @param geospatial_lat_min
	 *            the geospatial_lat_min to set
	 */
	public void setGeospatial_lat_min(String geospatial_lat_min) {
		this.geospatial_lat_min = geospatial_lat_min;
	}

	/**
	 * @return the geospatial_lat_max
	 */
	public String getGeospatial_lat_max() {
		return geospatial_lat_max;
	}

	/**
	 * @param geospatial_lat_max
	 *            the geospatial_lat_max to set
	 */
	public void setGeospatial_lat_max(String geospatial_lat_max) {
		this.geospatial_lat_max = geospatial_lat_max;
	}

	/**
	 * @return the grid_resolution
	 */
	public String getGrid_resolution() {
		return grid_resolution;
	}

	/**
	 * @param grid_resolution
	 *            the grid_resolution to set
	 */
	public void setGrid_resolution(String grid_resolution) {
		this.grid_resolution = grid_resolution;
	}

	/**
	 * @return the region_bigram
	 */
	public String getRegion_bigram() {
		return region_bigram;
	}

	/**
	 * @param region_bigram
	 *            the region_bigram to set
	 */
	public void setRegion_bigram(String region_bigram) {
		this.region_bigram = region_bigram;
	}

	/**
	 * @return the combination_search_radius
	 */
	public String getCombination_search_radius() {
		return combination_search_radius;
	}

	/**
	 * @param combination_search_radius
	 *            the combination_search_radius to set
	 */
	public void setCombination_search_radius(String combination_search_radius) {
		this.combination_search_radius = combination_search_radius;
	}

	/**
	 * @return the total_input_folder_path
	 */
	public String getTotal_input_folder_path() {
		return total_input_folder_path;
	}

	/**
	 * @param total_input_folder_path
	 *            the total_input_folder_path to set
	 */
	public void setTotal_input_folder_path(String total_input_folder_path) {
		this.total_input_folder_path = total_input_folder_path;
	}

	/**
	 * @return the total_HFRnetCDF_folder_path
	 */
	public String getTotal_HFRnetCDF_folder_path() {
		return total_HFRnetCDF_folder_path;
	}

	/**
	 * @param total_HFRnetCDF_folder_path
	 *            the total_HFRnetCDF_folder_path to set
	 */
	public void setTotal_HFRnetCDF_folder_path(String total_HFRnetCDF_folder_path) {
		this.total_HFRnetCDF_folder_path = total_HFRnetCDF_folder_path;
	}

	/**
	 * @return the total_mat_folder_path
	 */
	public String getTotal_mat_folder_path() {
		return total_mat_folder_path;
	}

	/**
	 * @param total_mat_folder_path
	 *            the total_mat_folder_path to set
	 */
	public void setTotal_mat_folder_path(String total_mat_folder_path) {
		this.total_mat_folder_path = total_mat_folder_path;
	}

	/**
	 * For developing, it prints the values in the console. 
	 */
	public void printData() {
		// print data to output only for debug purpouses
		System.out.println("This is NETWORK_TB: ");
		System.out.println("network_id parameter: " + network_id);
		System.out.println("EDIOS_Series_id parameter: " + EDIOS_Series_id);
		System.out.println("EDMO_code parameter: " + EDMO_code);
		System.out.println("metadata_page parameter: " + metadata_page);
//		System.out.println("DoA_estimation_method parameter: " + DoA_estimation_method);
//		System.out.println("calibration_type parameter: " + calibration_type);
//		System.out.println("calibration_link parameter: " + calibration_link);
//		System.out.println("last_calibration_date parameter: " + last_calibration_date);
		System.out.println("title parameter: " + title);
		System.out.println("summary parameter: " + summary);
		System.out.println("institution_name parameter: " + institution_name);
		System.out.println("citation_statement parameter: " + citation_statement);
		System.out.println("license parameter: " + license);
		System.out.println("acknowledgment parameter: " + acknowledgment);
		System.out.println("total_QC_velocity_threshold  parameter: " + total_QC_velocity_threshold);
		System.out.println("total_QC_GDOP_threshold parameter: " + total_QC_GDOP_threshold);
		System.out.println("total_QC_variance_threshold   parameter: " + total_QC_variance_threshold);
		System.out
				.println("total_QC_temporal_derivative_threshold parameter: " + total_QC_temporal_derivative_threshold);
		System.out.println("total_QC_data_density_threshold  parameter: " + total_QC_data_density_threshold);
		System.out.println("project parameter: " + project);
		System.out.println("institution_website parameter: " + institution_website);
		System.out.println("comment parameter: " + comment);
		System.out.println("network_name parameter: " + network_name);
		System.out.println("area parameter: " + area);
		System.out.println("geospatial_lon_min parameter: " + geospatial_lon_min);
		System.out.println("geospatial_lon_max parameter: " + geospatial_lon_max);
		System.out.println("geospatial_lat_min parameter: " + geospatial_lat_min);
		System.out.println("geospatial_lat_max parameter: " + geospatial_lat_max);
		System.out.println("grid_resolution parameter: " + grid_resolution);
		System.out.println("region_bigram parameter: " + region_bigram);
		System.out.println("combination_search_radius parameter: " + combination_search_radius);
		System.out.println("total_input_folder_path parameter: " + total_input_folder_path);
		System.out.println("total_HFRnetCDF_folder_path  parameter: " + total_HFRnetCDF_folder_path);
		System.out.println("total_mat_folder_path parameter: " + total_mat_folder_path);
		System.out.println("contributor_name: " + contributor_name);
		System.out.println("contributor_role: " + contributor_role);
		System.out.println("contributor_email: " + contributor_email);
		
	}

	/**
	 * It returns a mock set of values to work with no access to the data base.
	 */
	public void fillMockBean() {

		// Only for developement purpouses due to the restriction to the data
		// base access
		this.network_id = "HFR_TirLig";
		this.EDIOS_Series_id = "HFR_TirLig";
		this.EDMO_code = "134";
		this.metadata_page = "http://150.145.136.27:8080/thredds/HF_RADAR/TirLig/TirLig_catalog.html";
//		this.DoA_estimation_method = "Direction Finding";
//		this.calibration_type = "APM";
//		this.calibration_link = "carlo.mantovani@cnr.it";
//		this.last_calibration_date = "07/09/2016";
		this.title = "MOCK NETWORK Data, not real, only for development purpouses";
		this.summary = "The data set consists of maps of total velocity of the surface current in the North-Western Tyrrhenian Sea and Ligurian Sea averaged over a time interval of 1 hour around the cardinal hour. Surface ocean velocities estimated by HF Radar are representative of the upper 0.3-2.5 meters of the ocean.	";
		this.institution_name = "Laboratorio di Monitoraggio e Modellistica Ambientale per lo sviluppo sostenibile";
		this.citation_statement = "These data were collected and made freely available by the Copernicus project and the programs that contribute to it. Data collected and processed by CNR-ISMAR within Co.Co.Net, SSD-Pesca and RITMARE projects -  Year 2015";
		this.license = "HF radar sea surface current velocity dataset by LaMMA is licensed under a Creative Commons Attribution 4.0 International License. You should have received a copy of the license along with this work. If not, see http://creativecommons.org/licenses/by/4.0/.";
		this.acknowledgment = "The network has been funded by Regione Toscana and designed, implemented and managed by Consorzio LaMMA.";
		this.total_QC_velocity_threshold = "1.2";
		this.total_QC_GDOP_threshold = "2";
		this.total_QC_variance_threshold = "1";
		this.total_QC_temporal_derivative_threshold = "1.2";
		this.total_QC_data_density_threshold = "3";
		this.project = "EUSKALMET, Jerico-Next, IMPACT and SICOMAR Plus";
		this.institution_website = "http://www.azti.es/";
		this.comment = "Total velocities are derived using least square fit that maps radial velocities measured from individual sites onto a cartesian grid. The final product is a map of the horizontal components of the ocean currents on a regular grid in the area of overlap of two or more radar stations.";
		this.network_name = "LaMMA_HFR";
		this.area = "Mediterranean Sea";
		this.geospatial_lon_min = "9.2";
		this.geospatial_lon_max = "10.1";
		this.geospatial_lat_min = "43.68";
		this.geospatial_lat_max = "44.23";
		this.grid_resolution = "2";
		this.region_bigram = "MO";
		this.combination_search_radius = "3";
		this.total_input_folder_path = "/Users/reverendo/Documents/CNR/RADAR/DATI/Dati_HFR_LaMMA/Totals_tuv";
		this.total_HFRnetCDF_folder_path = "/Users/reverendo/Documents/CNR/RADAR/DATI/Dati_HFR_LaMMA/Totals_nc";
		this.total_mat_folder_path = "/Users/reverendo/Documents/CNR/RADAR/DATI/Dati_HFR_LaMMA/Totals_mat";
		this.temporal_resolution = "60";
		this.contributor_name = "Julien Mader; Anna Rubio; Jose Luis Asensio";
		this.contributor_role = "Data Centre expert, HFR expert; metadata expert";
		this.contributor_email = "jmader@azti.es; arubio@azti.es; jlasensio@azti.es";
	}

	/**
	 * @return the temporal_resolution
	 */
	public String getTemporal_resolution() {
		return temporal_resolution;
	}
	
	/**
	 * @return the int value of temporal_resolution
	 */
	public int getTemporal_resolution_Int(){
		double data = Double.parseDouble(temporal_resolution);
		return (int)data;
	}

	/**
	 * @param temporal_resolution the temporal_resolution to set
	 */
	public void setTemporal_resolution(String temporal_resolution) {
		this.temporal_resolution = temporal_resolution;
	}

}
