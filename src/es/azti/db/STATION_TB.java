/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  29 de ago. de 2018
 */
package es.azti.db;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class to retrieve data from STATION_TB table in the european node ddbb
 * @author Jose Luis Asensio (jlasensio@azti.es) 29 de ago. de 2018
 *
 */
@SuppressWarnings("serial")
public class STATION_TB extends DataBaseBean implements Serializable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.azti.db.DataBaseBean#fillBean(java.util.HashMap)
	 */
	private String station_id;
	private String network_id;
	private String station_full_name;
	private String site_lon;
	private String site_lat;
	private String radial_QC_velocity_threshold;
	private String radial_QC_variance_threshold;
	private String radial_QC_temporal_derivative_threshold;
	private String radial_QC_median_filter_RCLim;
	private String radial_QC_median_filter_AngLim;
	private String radial_QC_median_filter_CurLim;
	private String radial_QC_average_radial_bearing_min;
	private String radial_QC_average_radial_bearing_max;
	private String radial_QC_radial_count_threshold;
	private String number_of_range_cells;
	private String radial_input_folder_path;
	private String radial_HFRnetCDF_folder_path;
	private String DoA_estimation_method;
	private String calibration_type;
	private String calibration_link;
	private String last_calibration_date;

	private String EDMO_code;
	private String institution_name;
	private String institution_website;

	
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
	 * @return the station_id
	 */
	public String getStation_id() {
		return station_id;
	}

	/**
	 * @param station_id
	 *            the station_id to set
	 */
	public void setStation_id(String station_id) {
		this.station_id = station_id;
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
	 * @return the station_full_name
	 */
	public String getStation_full_name() {
		return station_full_name;
	}

	/**
	 * @param station_full_name
	 *            the station_full_name to set
	 */
	public void setStation_full_name(String station_full_name) {
		this.station_full_name = station_full_name;
	}

	/**
	 * @return the site_lon
	 */
	public String getSite_lon() {
		return site_lon;
	}

	/**
	 * @param site_lon
	 *            the site_lon to set
	 */
	public void setSite_lon(String site_lon) {
		this.site_lon = site_lon;
	}

	/**
	 * @return the site_lat
	 */
	public String getSite_lat() {
		return site_lat;
	}

	/**
	 * @param site_lat
	 *            the site_lat to set
	 */
	public void setSite_lat(String site_lat) {
		this.site_lat = site_lat;
	}

	/**
	 * @return the radial_QC_velocity_threshold
	 */
	public String getRadial_QC_velocity_threshold() {
		return radial_QC_velocity_threshold;
	}

	/**
	 * @param radial_QC_velocity_threshold
	 *            the radial_QC_velocity_threshold to set
	 */
	public void setRadial_QC_velocity_threshold(String radial_QC_velocity_threshold) {
		this.radial_QC_velocity_threshold = radial_QC_velocity_threshold;
	}

	/**
	 * @return the radial_QC_variance_threshold
	 */
	public String getRadial_QC_variance_threshold() {
		return radial_QC_variance_threshold;
	}

	/**
	 * @param radial_QC_variance_threshold
	 *            the radial_QC_variance_threshold to set
	 */
	public void setRadial_QC_variance_threshold(String radial_QC_variance_threshold) {
		this.radial_QC_variance_threshold = radial_QC_variance_threshold;
	}

	public Float getRadial_QC_temporal_derivative_thresholdFloat() {
		return Float.parseFloat(radial_QC_temporal_derivative_threshold);
	}

	/**
	 * @return the radial_QC_temporal_derivative_threshold
	 */
	public String getRadial_QC_temporal_derivative_threshold() {
		return radial_QC_temporal_derivative_threshold;
	}

	/**
	 * @param radial_QC_temporal_derivative_threshold
	 *            the radial_QC_temporal_derivative_threshold to set
	 */
	public void setRadial_QC_temporal_derivative_threshold(String radial_QC_temporal_derivative_threshold) {
		this.radial_QC_temporal_derivative_threshold = radial_QC_temporal_derivative_threshold;
	}

	/**
	 * @return the radial_QC_median_filter_RCLim
	 */
	public String getRadial_QC_median_filter_RCLim() {
		return radial_QC_median_filter_RCLim;
	}

	/**
	 * @return the radial_QC_median_filter_RCLim
	 */
	public Float getRadial_QC_median_filter_RCLim_float() {
		return Float.parseFloat(radial_QC_median_filter_RCLim);
	}

	/**
	 * @param radial_QC_median_filter_RCLim
	 *            the radial_QC_median_filter_RCLim to set
	 */
	public void setRadial_QC_median_filter_RCLim(String radial_QC_median_filter_RCLim) {
		this.radial_QC_median_filter_RCLim = radial_QC_median_filter_RCLim;
	}

	/**
	 * @return the radial_QC_median_filter_AngLim
	 */
	public String getRadial_QC_median_filter_AngLim() {
		return radial_QC_median_filter_AngLim;
	}

	/**
	 * @return the radial_QC_median_filter_AngLim
	 */
	public Float getRadial_QC_median_filter_AngLim_float() {
		return Float.parseFloat(radial_QC_median_filter_AngLim);
	}

	/**
	 * @param radial_QC_median_filter_AngLim
	 *            the radial_QC_median_filter_AngLim to set
	 */
	public void setRadial_QC_median_filter_AngLim(String radial_QC_median_filter_AngLim) {
		this.radial_QC_median_filter_AngLim = radial_QC_median_filter_AngLim;
	}

	/**
	 * @return the radial_QC_median_filter_CurLim
	 */
	public String getRadial_QC_median_filter_CurLim() {
		return radial_QC_median_filter_CurLim;
	}

	/**
	 * @return the radial_QC_median_filter_CurLim
	 */
	public Float getRadial_QC_median_filter_CurLim_float() {
		return Float.parseFloat(radial_QC_median_filter_CurLim);
	}

	/**
	 * @param radial_QC_median_filter_CurLim
	 *            the radial_QC_median_filter_CurLim to set
	 */
	public void setRadial_QC_median_filter_CurLim(String radial_QC_median_filter_CurLim) {
		this.radial_QC_median_filter_CurLim = radial_QC_median_filter_CurLim;
	}

	/**
	 * @return the radial_QC_average_radial_bearing_min
	 */
	public String getRadial_QC_average_radial_bearing_min() {
		return radial_QC_average_radial_bearing_min;
	}

	/**
	 * @param radial_QC_average_radial_bearing_min
	 *            the radial_QC_average_radial_bearing_min to set
	 */
	public void setRadial_QC_average_radial_bearing_min(String radial_QC_average_radial_bearing_min) {
		this.radial_QC_average_radial_bearing_min = radial_QC_average_radial_bearing_min;
	}

	/**
	 * @return the radial_QC_average_radial_bearing_max
	 */
	public String getRadial_QC_average_radial_bearing_max() {
		return radial_QC_average_radial_bearing_max;
	}

	/**
	 * @param radial_QC_average_radial_bearing_max
	 *            the radial_QC_average_radial_bearing_max to set
	 */
	public void setRadial_QC_average_radial_bearing_max(String radial_QC_average_radial_bearing_max) {
		this.radial_QC_average_radial_bearing_max = radial_QC_average_radial_bearing_max;
	}

	/**
	 * @return the radial_QC_radial_count_threshold
	 */
	public String getRadial_QC_radial_count_threshold() {
		return radial_QC_radial_count_threshold;
	}

	/**
	 * @return the radial_QC_radial_count_threshold
	 */
	public Float getRadial_QC_radial_count_threshold_float() {
		return Float.parseFloat(radial_QC_radial_count_threshold);
	}

	/**
	 * @param radial_QC_radial_count_threshold
	 *            the radial_QC_radial_count_threshold to set
	 */
	public void setRadial_QC_radial_count_threshold(String radial_QC_radial_count_threshold) {
		this.radial_QC_radial_count_threshold = radial_QC_radial_count_threshold;
	}

	/**
	 * @return the number_of_range_cells
	 */
	public String getNumber_of_range_cells() {
		return number_of_range_cells;
	}

	/**
	 * @param number_of_range_cells
	 *            the number_of_range_cells to set
	 */
	public void setNumber_of_range_cells(String number_of_range_cells) {
		this.number_of_range_cells = number_of_range_cells;
	}

	/**
	 * @return the radial_input_folder_path
	 */
	public String getRadial_input_folder_path() {
		return radial_input_folder_path;
	}

	/**
	 * @param radial_input_folder_path
	 *            the radial_input_folder_path to set
	 */
	public void setRadial_input_folder_path(String radial_input_folder_path) {
		this.radial_input_folder_path = radial_input_folder_path;
	}

	/**
	 * @return the radial_HFRnetCDF_folder_path
	 */
	public String getRadial_HFRnetCDF_folder_path() {
		return radial_HFRnetCDF_folder_path;
	}

	/**
	 * @param radial_HFRnetCDF_folder_path
	 *            the radial_HFRnetCDF_folder_path to set
	 */
	public void setRadial_HFRnetCDF_folder_path(String radial_HFRnetCDF_folder_path) {
		this.radial_HFRnetCDF_folder_path = radial_HFRnetCDF_folder_path;
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
	 * @return the eDMO_code
	 */
	public String getEDMO_code() {
		return EDMO_code;
	}

	public Short getEDMO_code_As_Short() {
		return Short.parseShort(EDMO_code);
	}

	/**
	 * For developing, it prints the values in the console. 
	 */
	public void printData() {
		// print data to output only for debug purpouses
		System.out.println("This is STATION_TB: ");
		System.out.println("station_id property: " + station_id);
		System.out.println("network_id property: " + network_id);
		System.out.println("station_full_name property: " + station_full_name);
		System.out.println("site_lon property: " + site_lon);
		System.out.println("site_lat property: " + site_lat);
		System.out.println("radial_QC_velocity_threshold property: " + radial_QC_velocity_threshold);
		System.out.println("radial_QC_variance_threshold property: " + radial_QC_variance_threshold);
		System.out.println(
				"radial_QC_temporal_derivative_threshold property: " + radial_QC_temporal_derivative_threshold);
		System.out.println("radial_QC_median_filter_RCLim property: " + radial_QC_median_filter_RCLim);
		System.out.println("radial_QC_median_filter_AngLim property: " + radial_QC_median_filter_AngLim);
		System.out.println("radial_QC_median_filter_CurLim property: " + radial_QC_median_filter_CurLim);
		System.out.println("radial_QC_average_radial_bearing_min property: " + radial_QC_average_radial_bearing_min);
		System.out.println("radial_QC_average_radial_bearing_max property: " + radial_QC_average_radial_bearing_max);
		System.out.println("radial_QC_radial_count_threshold property: " + radial_QC_radial_count_threshold);
		System.out.println("number_of_range_cells property: " + number_of_range_cells);
		System.out.println("radial_input_folder_path property: " + radial_input_folder_path);
		System.out.println("radial_HFRnetCDF_folder_path property: " + radial_HFRnetCDF_folder_path);
		System.out.println("DoA_estimation_method parameter: " + DoA_estimation_method);
		System.out.println("calibration_type parameter: " + calibration_type);
		System.out.println("calibration_link parameter: " + calibration_link);
		System.out.println("last_calibration_date parameter: " + last_calibration_date);
		System.out.println("EDMO_code parameter: " + EDMO_code);
		System.out.println("institution_name parameter: " + institution_name);
		System.out.println("institution_website parameter: " + institution_website);

	}
	/**
	 * It returns a mock set of values to work with no access to the data base.
	 */
	public void fillMockBean() {

		// Only for developement purpouses due to the restriction to the data
		// base access
		this.station_id = "MONT";
		this.network_id = "HFR_TirLig";
		this.station_full_name = "MOCK Monterosso al Mare";
		this.site_lon = "9.65333";
		this.site_lat = "44.1458";
		this.radial_QC_velocity_threshold = "1.2";
		this.radial_QC_variance_threshold = "1";
		this.radial_QC_temporal_derivative_threshold = "1";
		this.radial_QC_median_filter_RCLim = "5";
		this.radial_QC_median_filter_AngLim = "30";
		this.radial_QC_median_filter_CurLim = "1";
		this.radial_QC_average_radial_bearing_min = "190";
		this.radial_QC_average_radial_bearing_max = "250";
		this.radial_QC_radial_count_threshold = "200";
		this.number_of_range_cells = "50";
		this.radial_input_folder_path = "/Users/reverendo/Documents/CNR/RADAR/DATI/Dati_HFR_LaMMA/Radials_ruv/LIVO";
		this.radial_HFRnetCDF_folder_path = "/Users/reverendo/Documents/CNR/RADAR/DATI/Dati_HFR_LaMMA/Radials_nc";
		this.DoA_estimation_method = "Direction Finding";
		this.calibration_type = "APM";
		this.calibration_link = "jlasensio@azti.es";
		this.last_calibration_date = "07/09/2016";
		this.EDMO_code = "134";
		this.institution_name = "Laboratorio di Monitoraggio e Modellistica Ambientale per lo sviluppo sostenibile";
		this.institution_website = "http://www.azti.es/";

	}

}
