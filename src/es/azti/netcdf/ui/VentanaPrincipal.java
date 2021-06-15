package es.azti.netcdf.ui;

import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import es.azti.codar.beans.CodarRadialBean;
import es.azti.codar.beans.CodarTotalBean;
import es.azti.codar.utils.CodarRadialToNetCDF;
import es.azti.codar.utils.CodarTotalToNetCDF;
import es.azti.codar.utils.CodarUtils;
import es.azti.db.DBSQLAccess;
import es.azti.db.DataBaseBean;
import es.azti.db.NETWORK_TB;
import es.azti.db.STATION_TB;

/**
 * 
 * @author Jose Luis Asensio 18 de mayo de 2016
 * 
 *         Main program to run the Codar to NetCDF standarized format, using the
 *         european HF Radar standard defined in Jerico next.
 * 
 */
@SuppressWarnings("serial")
public class VentanaPrincipal extends JFrame {

	// properties
	Properties props;
	// logger
	private static Logger log;
	// panel
	private JPanel contentPane;
	// file to load
	private File fichero;
	// Radial file data
	CodarRadialBean codarRadialData;
	// Totals file data
	CodarTotalBean codarTotalData;
	// Profile default radial data
	CodarRadialBean profileCodarRadialData;
	// Profile default total data
	CodarTotalBean profileCodarTotalData;

	/**
	 * @autor Jose Luis Asensio (jlasensio@azti.es)
	 */

	/**
	 * main entry point. If the first argument is "console", the software run in
	 * a non user interface way. If not, the main window appear with the
	 * different options.
	 * 
	 * The console mode needs three more arguments: input path (to a file or a
	 * folder) output path (to a file or a folder) profile path (complete path
	 * to a previously created profile).
	 * 
	 * Please, read the documentation of JRadar to lear about the usage of the
	 * profiles and console mode.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Start logger
		log = Logger.getLogger(VentanaPrincipal.class);

		VentanaPrincipal bt = new VentanaPrincipal();

		log.debug("Initializing main window");

		String id = "window";

		if (args.length > 0) {
			id = args[0];
		}

		if (id.equals("console")) {
			// TODO
			String input = args[1];
			String output = args[2];
			String profile = args[3];
			bt.loadPropertiesNoUI();
			log.debug("running with no user interface");
			bt.process(profile, input, output);
		} else {
			bt.loadProperties();
			bt.initMainWindow();
			log.debug("Init completed");
		}

	}

	private void loadPropertiesNoUI() {
		try {
			props = new Properties();
			props.load(new FileInputStream("codar.properties"));
		} catch (FileNotFoundException e) {
			log.error("property file not found", e);
		} catch (IOException e) {
			log.error("error al leer el fichero de propiedades", e);
		}
	}

	private void loadProperties() {
		try {
			props = new Properties();
			props.load(new FileInputStream("codar.properties"));

		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Error trying to load CODAR property file.");
			log.error("property file not found", e);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error trying to load CODAR property file.");
			log.error("error al leer el fichero de propiedades", e);
		}

	}

	/**
	 * background process Takes a profile and a input file or directory, and
	 * process all the information to create netcdfs+
	 * 
	 * @param profile
	 *            The profile file to use as default values.
	 * @param input
	 *            Input file or directory. If directory, all .nc files will be
	 *            processed if possible
	 * @param output
	 *            Output file or directory. if file, name will be maintain or
	 *            used as prefix. If dir, same name as origin file will be used
	 *            as outpupt.
	 */
	private void process(String profile, String input, String output) {
		// load profile
		File ficheroProfile = new File(profile);
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(new FileInputStream(ficheroProfile.getAbsoluteFile()));
			if (ficheroProfile.getName().endsWith(".radial")) {
				profileCodarRadialData = (CodarRadialBean) in.readObject();
			} else if (ficheroProfile.getName().endsWith(".total")) {
				profileCodarTotalData = (CodarTotalBean) in.readObject();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			log.error("Profile file not found", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("There was a problem trying to read the profile File", e);
		} // bytes es el byte[]
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			log.error("The profile does not fit to this version", e);
		}

		File entrada = new File(input);
		// mandatory to start with a letter, a to Z (to avoid hidden and
		// temporal files like
		// the ones present in mac OS, starting with a dot
		// TODO check
		//if (entrada.isFile() && input.substring(0, 1).matches("\\w")) {
		if (entrada.isFile()) {
			// Digest file.
			try {
				int error = -1;
				if (entrada.getName().endsWith(".ruv") && ficheroProfile.getName().endsWith(".radial")) {
					codarRadialData = CodarUtils.loadCodarRadialData(entrada, props);
					CodarRadialToNetCDF ctn = new CodarRadialToNetCDF(codarRadialData);
					error = ctn.toNetCDF4(profileCodarRadialData, output);
				} else if (entrada.getName().endsWith(".tuv") && ficheroProfile.getName().endsWith(".total")) {
					codarTotalData = CodarUtils.loadCodarTotalData(entrada, props);
					CodarTotalToNetCDF ctn = new CodarTotalToNetCDF(codarTotalData);
					error = ctn.toNetCDF4(profileCodarTotalData, output);
				}
				// code = 0: ok
				// code = 1: mandatory field is missing or has an error.
				// code = 2: not mandatory field error (we can save file)
				// code = 3: radial data Table is missing
				// code = 4: Quality tests can't be run due to a missing values.
				// code = 5: data from ddbb not found and needed.
				// code = 6: parameters missing but profile used instead.

				log.debug("********************************************");
				log.debug("input: " + entrada);
				log.debug("output: " + output);
				log.debug("profile: " + profile);

				if (error == 1) {
					log.error("There is a problem with a mandatory parameter. Check the values please:");
				} else if (error == 3) {
					log.error("There is a problem with the data. Select another file:");
				} else if (error == 4) {
					log.error("There is a problem with a Quality Test. Run all the tests manually to get more info");
				} else if (error == 5) {
					log.error("There is a problem with the data from the Data Base. Check the metadata please.");
				} else if (error == 6) {
					log.debug("File has been successfully saved using profile info to fill gaps");
				} else if (error == -1) {
					log.error("Undefined Error, check the input parameter please");
				} else {
					log.debug("File has been successfully saved");
				}
			} catch (ParseException e) {
				log.error("could not load file, check the path please", e);
			}
		} else {
			File outputFile = new File(output);
			if (outputFile.isFile()) {
				outputFile = outputFile.getParentFile();
			}
			// loop to read all the files in the entry folder.
			// only codar files accepted.
			File[] lista = entrada.listFiles();
			for (File individual : lista) {
				// Digest files. check temporally files!
				if (individual.getName().substring(0, 1).matches("\\w")) {
					try {
						log.debug("********************************************");
						log.debug("input: " + individual.getAbsolutePath());
						log.debug("profile: " + profile);

						SimpleDateFormat filenameFormat = new SimpleDateFormat("yyyy_MM_dd_HHmm");
						filenameFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
						
						int error = -1;
						if (individual.getName().endsWith(".ruv") && ficheroProfile.getName().endsWith(".radial")) {
							codarRadialData = CodarUtils.loadCodarRadialData(individual, props);
							CodarRadialToNetCDF ctn = new CodarRadialToNetCDF(codarRadialData);
							// this is working in console mode, no data base
							// information, only profiles.
							String patternType = "";
							if (codarRadialData.getPatternType().contains("Measured")) {
								patternType="m";
							} else if (codarRadialData.getPatternType().contains("Ideal")) {
								patternType="i";
							}
							
							String fileName = profileCodarRadialData.getStationBean().getNetwork_id() + "-RDL_" + patternType + "_"
									+ profileCodarRadialData.getStationBean().getStation_id() + "_" + 
									filenameFormat.format(codarRadialData.getTimeStampAsCalendar().getTime() );
							String outputFileName = outputFile.getAbsolutePath() + File.separatorChar + fileName + ".nc";
							log.debug("output: " + outputFileName);
							error = ctn.toNetCDF4(profileCodarRadialData, outputFileName);
						} else if (individual.getName().endsWith(".tuv")
								&& ficheroProfile.getName().endsWith(".total")) {
							codarTotalData = CodarUtils.loadCodarTotalData(individual, props);
							CodarTotalToNetCDF ctn = new CodarTotalToNetCDF(codarTotalData);
							String fileName = profileCodarTotalData.getNetworkBean().getNetwork_id() + "-TOTL_" +
									filenameFormat.format(codarTotalData.getTimeStampAsCalendar().getTime());
							String outputFileName = outputFile.getAbsolutePath() + File.separatorChar
									+ fileName + ".nc";
							log.debug("output: " + outputFileName);
							error = ctn.toNetCDF4(profileCodarTotalData, outputFileName);
						}
						// code = 0: ok
						// code = 1: mandatory field is missing or has an error.
						// code = 2: not mandatory field error (we can save
						// file)
						// code = 3: radial data Table is missing
						// code = 4: Quality tests can't be runned due to a
						// missing values.
						// code = 6: parameters missing but profile used
						// instead.

						if (error == 1) {
							log.error("There is a problem with a mandatory parameter. Check the values please:");
						} else if (error == 3) {
							log.error("There is a problem with the data. Select another file:");
						} else if (error == 4) {
							log.error(
									"There is a problem with a Quality Test. Run all the tests manually to get more info");
						} else if (error == 6) {
							log.debug("File has been successfully saved using profile info to fill gaps");
						} else {
							log.debug("File has been successfully saved");
						}

					} catch (ParseException e) {
						log.error("could not load file, check the path please", e);
					}
				}

			}
		}

	}

	/**
	 * @autor Jose Luis Asensio (jlasensio@azti.es)
	 * 
	 *        It runs the main window, with buttons, listeners eventdispatcher
	 *        etc.
	 */

	public void initMainWindow() {

		setAlwaysOnTop(true);
		//JOptionPane.showMessageDialog(this, "Software in development, not final version");

		try {
			setIconImage(ImageIO.read(new File("logoVentana.png")));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		setName("Transform CODAR to NetCDF");
		setTitle("Trasnform CODAR to NetCDF");

		// window parameters
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 550, 400);
		setAlwaysOnTop(true);
		contentPane = new JPanel();
		setContentPane(contentPane);

		// Table layout, with fixed width
		GridLayout glayout = new GridLayout();
		glayout.setColumns(1);
		glayout.setRows(9);
		// gap between elements.
		glayout.setHgap(5);
		glayout.setVgap(5);

		contentPane.setLayout(glayout);

		JLabel infoLabel = new JLabel();
		infoLabel.setText("Load a file to start working");

		JButton btnSave = new JButton("Save as NetCDF");
		btnSave.setEnabled(false);
		btnSave.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int error = saveData();
				if (error == 0 || error == 6) {
					infoLabel.setText("NetCDF saved");
				} else {
					infoLabel.setText("Try again");
				}
			}
		});

		JButton btnTest = new JButton("Run Quality Tests");
		btnTest.setEnabled(false);
		btnTest.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runTest();
				infoLabel.setText("Quality tests menu");
			}
		});

		JButton btnVer = new JButton("View CODAR Data");
		btnVer.setEnabled(false);
		btnVer.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viewData();
				infoLabel.setText("Visualize Data");
			}
		});

		// Now, we load metadata from the DDBB of the node.
		JButton btnLoadMeta = new JButton("Load and display metadata");
		btnLoadMeta.setEnabled(false);
		btnLoadMeta.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int error = loadMetaData();
				if (error == 0 || error == 6) {
					infoLabel.setText("Metadata loaded");
				} else {
					infoLabel.setText("Try again");
				}
			}
		});

		JButton btnSaveProf = new JButton("Save current info as a profile");
		btnSaveProf.setEnabled(false);
		btnSaveProf.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int error = saveProfile();
				if (error == 0 || error == 6) {
					infoLabel.setText("Profile saved");
				} else {
					infoLabel.setText("Try again");
				}
			}
		});

		JButton btnLoadProf = new JButton("Load a profile");
		btnLoadProf.setEnabled(true);
		btnLoadProf.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (loadProfile()) {
					infoLabel.setText("Profile loaded");
				} else {
					infoLabel.setText("Try again");
				}
			}
		});

		JButton loadFile = new JButton("Select CODAR file");
		loadFile.setEnabled(true);
		loadFile.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean todoOk = loadFile();
				if (todoOk) {
					infoLabel.setText("File loaded, select an option");
					btnVer.setEnabled(true);
					btnTest.setEnabled(true);
					btnSave.setEnabled(true);
					btnLoadMeta.setEnabled(true);
					btnSaveProf.setEnabled(true);
				} else {
					infoLabel.setText("File not loaded, try again");
				}

			}
		});

		JButton btnAbout = new JButton("About this software");
		btnAbout.setEnabled(true);
		btnAbout.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lauchAboutWindow();
			}
		});

		contentPane.add(loadFile);

		contentPane.add(btnVer);

		contentPane.add(btnTest);

		contentPane.add(btnLoadMeta);

		contentPane.add(btnLoadProf);

		contentPane.add(btnSaveProf);

		contentPane.add(btnSave);

		contentPane.add(btnAbout);

		contentPane.add(infoLabel);

		// TODO: show file name somewhere in the window once it is loaded
		// TODO: add new parameters and attributes
		this.pack();
		this.setVisible(true);

	}

	/**
	 * about window, it shows a message with developer name and opens the pdf
	 * documentation.
	 */
	private void lauchAboutWindow() {
		// write credits, explanation about the software and a description of
		// the functionality.
		try {
			Desktop.getDesktop().open(new File("README.pdf"));
			JOptionPane.showMessageDialog(this,
					"Software developed by Jose Luis Asensio (txelu_ai@hotmail.com) under the license: (CC BY-NC-SA 4.0) ");
		} catch (IOException e) {
			// if pdf not present...
			log.error("Error trying to load software information", e);
		}
	}

	/**
	 * loads a previously creadt profile.
	 * 
	 * @return
	 */
	private boolean loadProfile() {
		boolean todoOk = false;
		try {
			VentanaSelectFichero ventana = new VentanaSelectFichero();
			ventana.setAlwaysOnTop(true);
			JFileChooser fc = new JFileChooser();
			// only codar files!
			FileNameExtensionFilter filterFileName = new FileNameExtensionFilter("PROFILE", "radial", "total");
			fc.setFileFilter(filterFileName);
			fc.setCurrentDirectory(new File("."));
			int returnVal = fc.showOpenDialog(ventana);
			// If the file was chossen properly...
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File ficheroProfile = fc.getSelectedFile();
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(ficheroProfile.getAbsoluteFile())); // bytes
																														// es
																														// el
																														// byte[]
				if (ficheroProfile.getName().endsWith("radial")) {
					profileCodarRadialData = (CodarRadialBean) in.readObject();
					profileCodarTotalData = null;
					JOptionPane.showMessageDialog(this, "Profile has been successfully loaded");
				} else if (ficheroProfile.getName().endsWith("total")) {
					profileCodarTotalData = (CodarTotalBean) in.readObject();
					profileCodarRadialData = null;
					JOptionPane.showMessageDialog(this, "Profile has been successfully loaded");
				} else {
					JOptionPane.showMessageDialog(this,
							"Profile has not been loaded, there is a problem with the file extension");
				}
				in.close();
				todoOk = true;
			} else {
				log.debug("File access cancelled by user.");
			}
		} catch (IOException ioe) {
			log.error("Error trying to load profile", ioe);
		} catch (ClassNotFoundException e) {
			log.error("error trying to load profile", e);
			JOptionPane.showMessageDialog(this, "Error trying to load profile");
		}
		return todoOk;
	}

	/**
	 * Saves the information on the bean inside a profile
	 * 
	 * @return
	 */
	private int saveProfile() {
		int error = -1;
		if (codarRadialData != null) {
			CodarRadialToNetCDF ctn = new CodarRadialToNetCDF(codarRadialData);
			error = ctn.saveProfile(profileCodarRadialData);
			if (error == 1) {
				JOptionPane.showMessageDialog(this,
						"There is a problem with a mandatory parameter. Check the values please:");
				VentanaVerCodarRadial vCodar = new VentanaVerCodarRadial(codarRadialData, profileCodarRadialData);
				vCodar.mostrarDatos();
			} else if (error == 3) {
				JOptionPane.showMessageDialog(this, "There is a problem with the data. Select another file:");
			} else if (error == 4) {
				JOptionPane.showMessageDialog(this,
						"There is a problem with a Quality Test. Run all the tests manually to get more info:");
				runTest();
			} else if (error == 6) {
				JOptionPane.showMessageDialog(this,
						"The profile will be saved using loaded profile info to fill gaps present in the file");
				codarRadialData.fixMissingValues(profileCodarRadialData);
			}

			if (error == 0 || error == 6) {
				// It is running properly.
				try {
					// serialize information in a binary file
					// TODO save as xml or similar, not dependant of the version
					// of the bean.
					JFileChooser fileChooser = new JFileChooser();
					VentanaSaveFichero vs = new VentanaSaveFichero();
					vs.setAlwaysOnTop(true);
					FileNameExtensionFilter filterFileName = new FileNameExtensionFilter("PROFILE", "radial");
					fileChooser.setFileFilter(filterFileName);
					fileChooser.setCurrentDirectory(new File("."));

					File saveFile = null;
					if (fileChooser.showSaveDialog(vs) == JFileChooser.APPROVE_OPTION) {
						saveFile = fileChooser.getSelectedFile();
					}
					String fileName = saveFile.getAbsolutePath().endsWith(".radial") ? saveFile.getAbsolutePath()
							: saveFile.getAbsolutePath() + ".radial";

					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
					out.writeObject(codarRadialData);
					out.close();
					JOptionPane.showMessageDialog(this, "Profile has been successfully saved");
				} catch (IOException ioe) {
					log.error("Error trying to save data profile", ioe);
					JOptionPane.showMessageDialog(this, "Error trying to save data profile");
					error = 7;
				}
			}
		} else if (codarTotalData != null) {
			// TODO duplicated code for total and radial, improve refactoring
			CodarTotalToNetCDF ctn = new CodarTotalToNetCDF(codarTotalData);
			error = ctn.saveProfile(profileCodarTotalData);
			if (error == 1) {
				JOptionPane.showMessageDialog(this,
						"There is a problem with a mandatory parameter. Check the values please:");
				VentanaVerCodarTotal vCodar = new VentanaVerCodarTotal(codarTotalData, profileCodarTotalData);
				vCodar.mostrarDatos();
			} else if (error == 3) {
				JOptionPane.showMessageDialog(this, "There is a problem with the data. Select another file:");
			} else if (error == 4) {
				JOptionPane.showMessageDialog(this,
						"There is a problem with a Quality Test. Run all the tests manually to get more info:");
				runTest();
			} else if (error == 6) {
				JOptionPane.showMessageDialog(this,
						"The profile will be saved using loaded profile info to fill gaps present in the file");
				codarTotalData.fixMissingValues(profileCodarTotalData);
			}

			if (error == 0 || error == 6) {
				// running prety well.
				try {
					// Serialize the info in a binary file
					// TODO save as xml or simmilar
					JFileChooser fileChooser = new JFileChooser();
					VentanaSaveFichero vs = new VentanaSaveFichero();
					vs.setAlwaysOnTop(true);
					FileNameExtensionFilter filterFileName = new FileNameExtensionFilter("PROFILE", "total");
					fileChooser.setFileFilter(filterFileName);
					fileChooser.setCurrentDirectory(new File("."));

					File saveFile = null;
					if (fileChooser.showSaveDialog(vs) == JFileChooser.APPROVE_OPTION) {
						saveFile = fileChooser.getSelectedFile();
					}
					String fileName = saveFile.getAbsolutePath().endsWith(".total") ? saveFile.getAbsolutePath()
							: saveFile.getAbsolutePath() + ".total";

					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
					out.writeObject(codarTotalData);
					out.close();
					JOptionPane.showMessageDialog(this, "Profile has been successfully saved");
				} catch (IOException ioe) {
					log.error("Error trying to save data profile", ioe);
					JOptionPane.showMessageDialog(this, "Error trying to save data profile");
					error = 7;
				}
			}
		}
		return error;
	}

	/**
	 * Save the netCDF file
	 * 
	 * @return
	 */
	private int saveData() {
		//JOptionPane.showMessageDialog(this, "In progress");
		int error = 1;
		if (codarRadialData == null && codarTotalData == null) {
			JOptionPane.showMessageDialog(this, "No CODAR data loaded, try loading a file first");
		} else if (codarRadialData != null) {
			CodarRadialToNetCDF ctn = new CodarRadialToNetCDF(codarRadialData);
			error = ctn.toNetCDF4(profileCodarRadialData, null);
			if (error == 1) {
				JOptionPane.showMessageDialog(this,
						"There is a problem with a mandatory parameter. Check the values please:");
				VentanaVerCodarRadial vCodar = new VentanaVerCodarRadial(codarRadialData, profileCodarRadialData);
				vCodar.mostrarDatos();
			} else if (error == 3) {
				JOptionPane.showMessageDialog(this, "There is a problem with the data. Select another file:");
			} else if (error == 4) {
				JOptionPane.showMessageDialog(this,
						"There is a problem with a Quality Test. Run all the tests manually to get more info:");
				VentanaRunRadialQualityTests vTests = new VentanaRunRadialQualityTests(codarRadialData);
				vTests.mostrarTests(profileCodarRadialData);
			} else if (error == 6) {
				JOptionPane.showMessageDialog(this, "File has been successfully saved using profile info to fill gaps");
			} else if (error == 7) {
				JOptionPane.showMessageDialog(this,
						"Error writing file, please check that NetCDF-C libraries are installed in your system");
				JOptionPane.showMessageDialog(this,
						"Visit unidata page for more info: http://www.unidata.ucar.edu/software/netcdf/docs/winbin.html");
			} else {
				// todo va bien.
				JOptionPane.showMessageDialog(this, "File has been successfully saved");
			}
		} else if (codarTotalData != null) {
			CodarTotalToNetCDF ctn = new CodarTotalToNetCDF(codarTotalData);
			error = ctn.toNetCDF4(profileCodarTotalData, null);
			if (error == 1) {
				JOptionPane.showMessageDialog(this,
						"There is a problem with a mandatory parameter. Check the values please:");
				VentanaVerCodarTotal vCodar = new VentanaVerCodarTotal(codarTotalData, profileCodarTotalData);
				vCodar.mostrarDatos();
			} else if (error == 3) {
				JOptionPane.showMessageDialog(this, "There is a problem with the data. Select another file:");
			} else if (error == 4) {
				JOptionPane.showMessageDialog(this,
						"There is a problem with a Quality Test. Run all the tests manually to get more info:");
				VentanaRunTotalQualityTests vTests = new VentanaRunTotalQualityTests(codarTotalData);
				vTests.mostrarTests(profileCodarTotalData);
			} else if (error == 6) {
				JOptionPane.showMessageDialog(this, "File has been successfully saved using profile info to fill gaps");
			} else if (error == 7) {
				JOptionPane.showMessageDialog(this,
						"Error writing file, please check that NetCDF-C libraries are installed in your system");
				JOptionPane.showMessageDialog(this,
						"Visit unidata page for more info: http://www.unidata.ucar.edu/software/netcdf/docs/winbin.html");
			} else if (error == 0) {
				// todo va bien.
				JOptionPane.showMessageDialog(this, "File has been successfully saved");
			} else {
				JOptionPane.showMessageDialog(this, "Something very bad has just happend... please, try not to cry");
			}
		}
		return error;
	}

	/**
	 * opens a new window showing the data of the bean.
	 */
	private void viewData() {
		if (codarRadialData == null && codarTotalData == null) {
			JOptionPane.showMessageDialog(this, "No CODAR data loaded, try loading a file first");
		} else if (codarRadialData != null) {
			VentanaVerCodarRadial vCodar = new VentanaVerCodarRadial(codarRadialData, profileCodarRadialData);
			vCodar.mostrarDatos();
		} else if (codarTotalData != null) {
			VentanaVerCodarTotal vCodar = new VentanaVerCodarTotal(codarTotalData, profileCodarTotalData);
			vCodar.mostrarDatos();
		}
	}

	/**
	 * Open the QAQC test main window.
	 */
	private void runTest() {
		if (codarRadialData == null && codarTotalData == null) {
			JOptionPane.showMessageDialog(this, "No CODAR Radial data loaded, try loading a file first.");
		} else if (codarRadialData != null) {
			if (profileCodarRadialData != null) {
				codarRadialData.fixMissingValues(profileCodarRadialData);
				JOptionPane.showMessageDialog(this, "Profile info used to fill gaps");
			}
			CodarRadialToNetCDF ctn = new CodarRadialToNetCDF(codarRadialData);
			if (!ctn.checkBeanMandatoryFields() && codarRadialData.getStationBean() != null) {
				VentanaRunRadialQualityTests vTests = new VentanaRunRadialQualityTests(codarRadialData);
				vTests.mostrarTests(profileCodarRadialData);
			} else {
				JOptionPane.showMessageDialog(this,
						"Missing information. Please, fill the mandatory parameters, load station and network information from DDBB, or load a profile.");
			}
		} else if (codarTotalData != null) {
			if (profileCodarTotalData != null) {
				codarTotalData.fixMissingValues(profileCodarTotalData);
				JOptionPane.showMessageDialog(this, "Profile info used to fill gaps");
			}
			CodarTotalToNetCDF ctn = new CodarTotalToNetCDF(codarTotalData);
			if (!ctn.checkBeanMandatoryFields() && codarTotalData.getNetworkBean() != null) {
				VentanaRunTotalQualityTests vTests = new VentanaRunTotalQualityTests(codarTotalData);
				vTests.mostrarTests(profileCodarTotalData);
			} else {
				JOptionPane.showMessageDialog(this,
						"Missing information. Please, fill the mandatory parameters, load station and network information from DDBB, or load a profile.");
			}
		}
	}

	/**
	 * loads codar file, radial or total.
	 * 
	 * @return
	 */
	private boolean loadFile() {
		boolean returnValue = false;
		VentanaSelectFichero ventana = new VentanaSelectFichero();
		ventana.setAlwaysOnTop(true);
		JFileChooser fc = new JFileChooser();
		// solo aceptamos archivos CODAR, ver como hacer
		FileNameExtensionFilter filterFileName = new FileNameExtensionFilter("CODAR FILES", "ruv", "tuv");
		fc.setFileFilter(filterFileName);
		fc.setCurrentDirectory(new File("."));
		int returnVal = fc.showOpenDialog(ventana);
		// Si hemos seleccionado un archivo correctamente...
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fichero = fc.getSelectedFile();
			try {
				// Digest file.
				if (fichero.getName().endsWith("ruv")) {
					codarRadialData = CodarUtils.loadCodarRadialData(fichero, props);
					codarTotalData = null;
				} else if (fichero.getName().endsWith("tuv")) {
					codarTotalData = CodarUtils.loadCodarTotalData(fichero, props);
					codarRadialData = null;
				}
				returnValue = true;
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Error trying to load CODAR file.");
				log.error("problem accessing file" + fichero.getAbsolutePath(), ex);
			}
		} else {
			log.debug("File access cancelled by user.");
		}
		return returnValue;
	}

	/**
	 * makes a query to the European data base to retrieve the metadata related
	 * to the Network and site.
	 * 
	 * @return
	 */
	private int loadMetaData() {
		int error = -1;
		// we must read the file site and network id, and use them to load the
		// tables from the node ddbb and display them.
		// a jframe here isn't strictly necessary, but it makes the example a
		// little more real
		JFrame frame = new JFrame("Select Site and Network:");
		// prompt the user to enter their name
		String currentNwk = codarTotalData == null ? codarRadialData.getNetwork() : codarTotalData.getNetwork();
		String currentSt = codarTotalData == null ? codarRadialData.getSite() : codarTotalData.getSite();
		String networkId = JOptionPane.showInputDialog(frame, "Type Network id", currentNwk);
		String siteId = null;
		if (codarTotalData == null) {
			siteId = JOptionPane.showInputDialog(frame, "Type Site id", currentSt);
		}

		DataBaseBean networkData = null;
		DataBaseBean stationData = null;
		Object[] stationDataTemp = null;
		
		DBSQLAccess dbAccess = new DBSQLAccess();
		try {

			networkData = dbAccess.getTable("network_tb", "network_id", networkId);
			// JOptionPane.showMessageDialog(this, "Mocking data from DDBB:
			// VentanaPrincipal.java line 741");
			// networkData = dbAccess.getMockTable("network_tb");
			if (codarTotalData == null) {
				// Station information only loaded when working with radials.
				// stationData = dbAccess.getMockTable("station_tb",
				// "station_id", siteId);
				stationData = dbAccess.getTable("station_tb", "station_id", siteId);
			} else {
				
				stationDataTemp = dbAccess.getCalibration("station_tb", "network_id", networkId);
				if (stationDataTemp == null) {
					JOptionPane.showMessageDialog(this,
							"It was not possible to retrieve the calibration metadata for the network.");
				} else {
					//iterate over all possiblities
					String stimationMethod = "";
					String calType = "";
					String calLink = "";
					String calDate = "";
					for(Object obj:stationDataTemp) {
						if (!stimationMethod.isEmpty())
						{
							stimationMethod = stimationMethod + "; ";
						}
						stimationMethod = stimationMethod + ((STATION_TB) obj).getStation_id() + ": " +((STATION_TB) obj).getDoA_estimation_method();
						if (!calType.isEmpty())
						{
							calType = calType + "; ";
						}
						calType = calType + ((STATION_TB) obj).getStation_id() + ": " + ((STATION_TB) obj).getCalibration_type();
						calLink = calLink + ((STATION_TB) obj).getStation_id() + ":" + ((STATION_TB) obj).getCalibration_link() + ";";
						calDate = calDate + ((STATION_TB) obj).getStation_id() + ":" + ((STATION_TB) obj).getLast_calibration_date() + ";";
					}
					((NETWORK_TB) networkData).setDoA_estimation_method(stimationMethod);
					((NETWORK_TB) networkData).setCalibration_type(calType);
					((NETWORK_TB) networkData).setCalibration_link(calLink);
					((NETWORK_TB) networkData).setLast_calibration_date(calDate);
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			log.error("error retrieving network and station data from DDBB:", e);
			JOptionPane.showMessageDialog(this, "Error retrieving network and station data from DDBB");
		}
		VentanaVerDBData verDDBB = new VentanaVerDBData((NETWORK_TB) networkData, (STATION_TB) stationData);
		if (networkData == null || (stationData == null && codarRadialData != null)) {
			JOptionPane.showMessageDialog(this,
					"It was not possible to retrieve the complete metadata from the data base. Check the station and network ids and your ddbb access privileges and connectivity.");
		}
		if (codarRadialData != null) {
			codarRadialData.setNetworkBean((NETWORK_TB) networkData);
			codarRadialData.setStationBean((STATION_TB) stationData);
		} else if (codarTotalData != null) {
			codarTotalData.setNetworkBean((NETWORK_TB) networkData);
			
		}
		verDDBB.mostrarDatos();

		return error;
	}

}
