/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  1 de oct. de 2018
 */
package es.azti.netcdf.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import es.azti.db.NETWORK_TB;
import es.azti.db.STATION_TB;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es) 1 de oct. de 2018
 *
 *         Visualize the metadata retrieved from the database
 */
@SuppressWarnings("serial")
public class VentanaVerDBData extends JFrame {

	// Bean
	private NETWORK_TB networkBean;
	private STATION_TB stationBean;

	public VentanaVerDBData(NETWORK_TB ntw, STATION_TB sttn) {
		this.networkBean = ntw;
		this.stationBean = sttn;
	}

	/**
	 * Shows the metadata retrieved from the ddbb in a single table Both beans
	 * retrieved from the ddbb (network and station) are shown in the very same
	 * table.
	 */
	public void mostrarDatos() {
		try {
			setIconImage(ImageIO.read(new File("logoVentana.png")));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		setName("DDBB metadata visualizer");
		setTitle("DDBB metadata visualizer");
		setBounds(400, 100, 550, 400);

		JPanel tempPanel = new JPanel();

		// layout as a scroll table view,
		GridLayout glayout = new GridLayout();
		glayout.setColumns(2);
		// 2 rows for the titles, 1 station & 1 network
		// 17 values for the station
		// 34 values for the network
		glayout.setRows(63);

		// gap between elements.
		glayout.setHgap(5);
		glayout.setVgap(5);
		tempPanel.setLayout(glayout);

		// NETWORK DDBB info
		this.addRowDisplay(tempPanel, "This is the data from: ", "NETWORK_TB DB TABLE");
		if (this.networkBean != null) {
			this.addRowDisplay(tempPanel, "network_id", this.networkBean.getNetwork_id());
			this.addRowDisplay(tempPanel, "EDIOS_Series_id", this.networkBean.getEDIOS_Series_id());
			this.addRowDisplay(tempPanel, "EDMO_code", this.networkBean.getEDMO_code());
			this.addRowDisplay(tempPanel, "metadata_page", this.networkBean.getMetadata_page());
			this.addRowDisplay(tempPanel, "title", this.networkBean.getTitle());
			this.addRowDisplay(tempPanel, "summary", this.networkBean.getSummary());
			this.addRowDisplay(tempPanel, "institution_name", this.networkBean.getInstitution_name());
			this.addRowDisplay(tempPanel, "citation_statement", this.networkBean.getCitation_statement());
			this.addRowDisplay(tempPanel, "license", this.networkBean.getLicense());
			this.addRowDisplay(tempPanel, "acknowledgment", this.networkBean.getAcknowledgment());
			this.addRowDisplay(tempPanel, "total_QC_velocity_threshold",
					this.networkBean.getTotal_QC_velocity_threshold());
			this.addRowDisplay(tempPanel, "total_QC_GDOP_threshold", this.networkBean.getTotal_QC_GDOP_threshold());
			this.addRowDisplay(tempPanel, "total_QC_variance_threshold",
					this.networkBean.getTotal_QC_variance_threshold());
			this.addRowDisplay(tempPanel, "total_QC_temporal_derivative_threshold",
					this.networkBean.getTotal_QC_temporal_derivative_threshold());
			this.addRowDisplay(tempPanel, "total_QC_data_density_threshold",
					this.networkBean.getTotal_QC_data_density_threshold());
			this.addRowDisplay(tempPanel, "project", this.networkBean.getProject());
			this.addRowDisplay(tempPanel, "institution_website", this.networkBean.getInstitution_website());
			this.addRowDisplay(tempPanel, "comment", this.networkBean.getComment());
			this.addRowDisplay(tempPanel, "network_name", this.networkBean.getNetwork_name());
			this.addRowDisplay(tempPanel, "area", this.networkBean.getArea());
			this.addRowDisplay(tempPanel, "geospatial_lon_min", this.networkBean.getGeospatial_lon_min());
			this.addRowDisplay(tempPanel, "geospatial_lon_max", this.networkBean.getGeospatial_lon_max());
			this.addRowDisplay(tempPanel, "geospatial_lat_min", this.networkBean.getGeospatial_lat_min());
			this.addRowDisplay(tempPanel, "geospatial_lat_max", this.networkBean.getGeospatial_lat_max());
			this.addRowDisplay(tempPanel, "grid_resolution", this.networkBean.getGrid_resolution());
			this.addRowDisplay(tempPanel, "temporal_resolution", this.networkBean.getTemporal_resolution());
			
			this.addRowDisplay(tempPanel, "region_bigram", this.networkBean.getRegion_bigram());
			this.addRowDisplay(tempPanel, "combination_search_radius", this.networkBean.getCombination_search_radius());
			this.addRowDisplay(tempPanel, "total_input_folder_path", this.networkBean.getTotal_input_folder_path());
			this.addRowDisplay(tempPanel, "total_HFRnetCDF_folder_path",
					this.networkBean.getTotal_HFRnetCDF_folder_path());
			this.addRowDisplay(tempPanel, "total_mat_folder_path", this.networkBean.getTotal_mat_folder_path());
			this.addRowDisplay(tempPanel,  "contributor_name", this.networkBean.getContributor_name());
			this.addRowDisplay(tempPanel,  "contributor_role", this.networkBean.getContributor_role());
			this.addRowDisplay(tempPanel,  "contributor_email", this.networkBean.getContributor_email());
		} else {
			// if there is no bean loaded
			this.addRowDisplay(tempPanel, "NO DATA TO DISPLAY", "NO DATA TO DISPLAY");
		}
		// STATION DDBB info
		this.addRowDisplay(tempPanel, "This is the data from: ", "STATION_TB DB TABLE");
		if (this.stationBean != null) {
			this.addRowDisplay(tempPanel, "station_id", this.stationBean.getStation_id());
			this.addRowDisplay(tempPanel, "network_id", this.stationBean.getNetwork_id());
			this.addRowDisplay(tempPanel, "station_full_name", this.stationBean.getStation_full_name());
			this.addRowDisplay(tempPanel, "site_lon", this.stationBean.getSite_lon());
			this.addRowDisplay(tempPanel, "site_lat", this.stationBean.getSite_lat());
			this.addRowDisplay(tempPanel, "radial_QC_velocity_threshold",
					this.stationBean.getRadial_QC_velocity_threshold());
			this.addRowDisplay(tempPanel, "radial_QC_variance_threshold",
					this.stationBean.getRadial_QC_variance_threshold());
			this.addRowDisplay(tempPanel, "radial_QC_temporal_derivative_threshold",
					this.stationBean.getRadial_QC_temporal_derivative_threshold());
			this.addRowDisplay(tempPanel, "radial_QC_median_filter_RCLim",
					this.stationBean.getRadial_QC_median_filter_RCLim());
			this.addRowDisplay(tempPanel, "radial_QC_median_filter_AngLim",
					this.stationBean.getRadial_QC_median_filter_AngLim());
			this.addRowDisplay(tempPanel, "radial_QC_median_filter_CurLim",
					this.stationBean.getRadial_QC_median_filter_CurLim());
			this.addRowDisplay(tempPanel, "radial_QC_average_radial_bearing_min",
					this.stationBean.getRadial_QC_average_radial_bearing_min());
			this.addRowDisplay(tempPanel, "radial_QC_average_radial_bearing_max",
					this.stationBean.getRadial_QC_average_radial_bearing_max());
			this.addRowDisplay(tempPanel, "radial_QC_radial_count_threshold",
					this.stationBean.getRadial_QC_radial_count_threshold());
			this.addRowDisplay(tempPanel, "number_of_range_cells", this.stationBean.getNumber_of_range_cells());
			this.addRowDisplay(tempPanel, "radial_input_folder_path", this.stationBean.getRadial_input_folder_path());
			this.addRowDisplay(tempPanel, "radial_HFRnetCDF_folder_path",
					this.stationBean.getRadial_HFRnetCDF_folder_path());
			this.addRowDisplay(tempPanel, "DoA_estimation_method", this.stationBean.getDoA_estimation_method());
			this.addRowDisplay(tempPanel, "calibration_type", this.stationBean.getCalibration_type());
			this.addRowDisplay(tempPanel, "calibration_link", this.stationBean.getCalibration_link());
			this.addRowDisplay(tempPanel, "last_calibration_date", this.stationBean.getLast_calibration_date());

		} else {
			// if there is no bean loaded.
			this.addRowDisplay(tempPanel, "NO DATA TO DISPLAY", "NO DATA TO DISPLAY");
		}

		JScrollPane panel = new JScrollPane(tempPanel);
		panel.setPreferredSize(new Dimension(900, 400));
		panel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setContentPane(panel);

		this.pack();
		this.setVisible(true);

	}

	// method that helps adding a row to the table that displays the metadata
	private void addRowDisplay(JPanel panel, String label, String value) {

		JLabel labelText = new JLabel();
		labelText.setText(label);
		panel.add(labelText);

		JTextField valueText = new JTextField();
		String valueShort = value.length() > 50 ? value.substring(0, 50) + "..." : value;
		valueText.setText(valueShort);
		valueText.setToolTipText(value);
		valueText.setEditable(false);
		panel.add(valueText);

	}
}
