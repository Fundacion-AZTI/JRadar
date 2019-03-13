/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  16 de march. de 2017
 */
package es.azti.netcdf.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import es.azti.codar.beans.CodarRadialBean;
import es.azti.codar.utils.CodarUtils;
import es.azti.utils.Lldistkm;
import es.azti.utils.TableColumnNames;

/**
 * Cass defining the window that helps running the QAQC tests for radial files.
 * 
 * @author Jose Luis Asensio (jlasensio@azti.es) 16 de march. 2017
 *
 */
public class VentanaRunRadialQualityTests extends JFrame {

	/**
	 * This window manage the execution of necessary operations in order to run
	 * the quality control test for radials. The bean with the neccesary data to
	 * run the quality tests for radials is an attribute of the generic bean of
	 * the radial file. The tests are performed within the CodarUtil
	 * functionality the execution of the test can be manual using the user
	 * interface. this way, the users can see the results in a little display.
	 * That doesn't mean that the information about the executed test is the one
	 * stored in the final netcdf file. the info in the display is only a
	 * notification. The real test is performed when the net cdf file is saved,
	 * take in to account that the user can modify some data of the bean after
	 * running a test. Actualy, the saved data are the ones obtained when saving
	 * the netcdf.
	 * 
	 * The flaggin is described in the next table: ARGO QC flag scale The byte
	 * codes in column 1 are used only in the QC variables to describe the
	 * quality of each measurement, the string in column 2 ('meaning') are used
	 * in the attribute flag_meanings of each QC variable to describe the
	 * overall quality of the parameter. When the numeric codes are used, the
	 * flag_values and flag_meanings attributes are required and should contain
	 * lists of the codes (comma-separated) and their meanings (spaced
	 * separated, replacing spaces within each meaning by '_')
	 * 
	 * Code			Meaning 								Comment
	 * 0		unknown									No QC was performed
	 * 1		good data								All QC tests passed
	 * 2		probably good data						
	 * 3		potentially correctable bad data		These data are not to be used without scientific correction or re-calibration
	 * 4		bad data								Data have failed one or more QC tests
	 * 5		-										not used
	 * 6		-										not used
	 * 7		nominal value							Data were not observed but reported (e.g. instrument target depth)
	 * 8		interpolated value
	 * 9		missing value
	 */

	private static final long serialVersionUID = 1L;
	// logger
	private static Logger log;
	private CodarRadialBean bean;

	public VentanaRunRadialQualityTests(CodarRadialBean datos) {
		// Start logger
		log = Logger.getLogger(VentanaRunRadialQualityTests.class);
		this.bean = datos;
	}

	/**
	 * shows the main window for radial QC tests
	 * 
	 * @param profile
	 *            a profile just in case some default values are needed.
	 */
	public void mostrarTests(CodarRadialBean profile) {
		try {
			setIconImage(ImageIO.read(new File("logoVentana.png")));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		setName("HF Radar radial data tester");
		setTitle("HF Radar radial data tester");
		setBounds(400, 100, 550, 400);
		setAlwaysOnTop(true);

		JPanel tempPanel = new JPanel();

		// User interface buttons layout.
		GridLayout glayout = new GridLayout();
		glayout.setColumns(3);
		glayout.setRows(6);
		// gap between elements of the table..
		glayout.setHgap(5);
		glayout.setVgap(5);
		tempPanel.setLayout(glayout);

		// ******************************************************//
		// ******************************************************//
		// TEST 1: OVER WATER //
		// ******************************************************//
		// ******************************************************//
		JLabel labelTest1 = new JLabel();
		labelTest1.setText("Over Water test");

		JButton btnTest1 = new JButton("Run O.W. Test");
		btnTest1.setEnabled(true);
		JLabel resultsTest1 = new JLabel();
		resultsTest1.setText("Push button to run test");

		btnTest1.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					List<Float> results = runOverWaterTest(bean.getTable().getColumnElements(TableColumnNames.VFLG));
					labelTest1.setText("Test successful");
					labelTest1.setForeground(Color.blue);
					int val1 = 0;
					int val4 = 0;
					int val9 = 0;
					for (float res : results) {
						if (res == 1) {
							val1 = val1 + 1;
						} else if (res == 4) {
							val4 = val4 + 1;
						} else if (res == 9) {
							val9 = val9 + 1;
						}
					}
					resultsTest1.setText("Good data: " + val1 + " - Bad data: " + val4);

				} catch (Exception error) {
					log.error("something went wrong", error);
					labelTest1.setText("Error");
					setForeground(Color.red);
				}

			}
		});
		tempPanel.add(btnTest1);
		tempPanel.add(labelTest1);
		tempPanel.add(resultsTest1);

		// ******************************************************//
		// ******************************************************//
		// TEST 2: VELOCITY THRESHOLD //
		// ******************************************************//
		// ******************************************************//
		JLabel labelTest2 = new JLabel();
		labelTest2.setText("Velocity Threshold Test");

		JLabel resultsTest2 = new JLabel();
		resultsTest2.setText("Push button to run test");

		JButton btnTest2 = new JButton("Run V.T. Test");
		btnTest2.setEnabled(true);

		btnTest2.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// ask for a value of threshold and propose one from radials.
				JFrame frame = new JFrame("Velocity Threshold test");
				frame.setAlwaysOnTop(true);
				String limit = Float.isNaN(bean.getRadialTest().getVeloThreshold())
						? bean.getStationBean().getRadial_QC_velocity_threshold()
						: Float.toString(bean.getRadialTest().getVeloThreshold());
				if ((limit == null || limit.isEmpty() || limit.equals("NaN")) && profile != null) {
					limit = Float.toString(profile.getRadialTest().getVeloThreshold());
				}
				String veloThreshold = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the Velocity Threshold:", limit);
				if (veloThreshold != null && !veloThreshold.equals("NaN")) {
					try {
						bean.getRadialTest().setVeloThreshold(Float.parseFloat(veloThreshold));
						// be carefull with the units. The codar table in the
						// file is in cm/s and in the ddbb m/s
						List<Float> veloRead = bean.getTable().getColumnElements(TableColumnNames.VELO);
						List<Float> velo = new ArrayList<Float>(veloRead.size());
						for (Float velTemp : veloRead) {
							// translate to m/s
							velo.add(new Float(-velTemp / 100));
						}

						List<Float> pasaVeloTest = runThresholdTest(velo, bean.getRadialTest().getVeloThreshold());
						labelTest2.setText("Test successful");
						labelTest2.setForeground(Color.blue);

						int val1 = 0;
						int val4 = 0;
						int val9 = 0;
						for (float res : pasaVeloTest) {
							if (res == 1) {
								val1 = val1 + 1;
							} else if (res == 4) {
								val4 = val4 + 1;
							} else if (res == 9) {
								val9 = val9 + 1;
							}
						}
						resultsTest2.setText("Good data: " + val1 + " - Bad data: " + val4);

					} catch (NumberFormatException nfe) {
						// error, not a number
						JOptionPane.showMessageDialog(frame, "Error parsing velocity threshold. Try again please.");
						log.error("velocity threshold parsing error");
						labelTest2.setText("Error parsing threshold");
						labelTest2.setForeground(Color.red);
					} catch (Exception error) {
						log.error("error running velocity threshold test", error);
						labelTest2.setText("Error running velocity threshold test");
						labelTest2.setForeground(Color.red);
					}
				} else {
					labelTest2.setText("No valid value");
					labelTest2.setForeground(Color.red);
				}
			}
		});
		tempPanel.add(btnTest2);
		tempPanel.add(labelTest2);
		tempPanel.add(resultsTest2);

		// ******************************************************//
		// ******************************************************//
		// TEST 3: VARIANCE THRESHOLD: PARA CODAR NO VALE VER D5.14 JERICO//
		// ******************************************************//
		// ******************************************************//
		// JLabel labelTest3 = new JLabel();
		// labelTest3.setText("Variance Threshold Test");
		//
		// JLabel resultsTest3 = new JLabel();
		// resultsTest3.setText("Push button to run test");
		//
		// JButton btnTest3 = new JButton("Run Var.T. Test");
		// btnTest3.setEnabled(true);
		// btnTest3.addActionListener(new AbstractAction() {
		//
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// //ask for a value of threshold and propose one from radials.
		// //primero mostrar el límite y pedir confirmacion
		// //luego validar que es un dato valido
		// //ultimo, lanzar test
		// JFrame frame = new JFrame("Variance Threshold Test");
		// frame.setAlwaysOnTop(true);
		// String limit =
		// Float.isNaN(bean.getRadialTest().getVarianceThreshold())?bean.getStationBean().getRadial_QC_variance_threshold():Float.toString(bean.getRadialTest().getVarianceThreshold());
		// if ((limit == null || limit.isEmpty() ||
		// limit.equals("NaN"))&&profile!=null) {limit =
		// Float.toString(profile.getRadialTest().getVarianceThreshold());}
		//
		// String varThreshold = (String) JOptionPane.showInputDialog(frame,
		// "Enter a value for the Variance Threshold:",
		// limit);
		// if (varThreshold != null && !varThreshold.equals("NaN")) {
		// try {
		// bean.getRadialTest().setVarianceThreshold(Float.parseFloat(varThreshold));
		//
		// //tabla basada en posicion de indice 0, primer elemento posicion 0.
		// El 7º elemento ETMP está en la posición 6
		// //El elemento ETMP de la tabla, es la varianza temporal, Temporal
		// quality
		//
		// //en este caso no nos interesa el resultado, no lo utilizamos (se
		// podria visualizar?)
		// List<Float> pasaVarTest =
		// runThresholdTest(bean.getTable().getColumnElements(TableColumnNames.ETMP),
		// bean.getRadialTest().getVarianceThreshold());
		// labelTest3.setText("Test successful");
		// labelTest3.setForeground(Color.blue);
		//
		// int val1 = 0;
		// int val4 = 0;
		// int val9 = 0;
		// for (float res : pasaVarTest) {
		// if (res == 1) {val1=val1 +1;}
		// else if (res == 4) {val4=val4+1;}
		// else if (res == 9) {val9=val9+1;}
		// }
		// resultsTest3.setText("Good data: " + val1 + " - Bad data: " + val4);
		//
		// } catch (NumberFormatException nfe) {
		// //error, not a number
		// JOptionPane.showMessageDialog(frame, "Error parsing variance
		// threshold. Try again please.");
		// log.error("variance threshold parsing error");
		// labelTest3.setText("Error parsing threshold");
		// labelTest3.setForeground(Color.red);
		// } catch (Exception error) {
		// log.error("error running variance threshold test", error);
		// labelTest3.setText("Error running variance threshold test");
		// labelTest3.setForeground(Color.red);
		// }
		// } else {
		// labelTest3.setText("No valid value");
		// labelTest3.setForeground(Color.red);
		// }
		// }
		// });
		// tempPanel.add(btnTest3);
		// tempPanel.add(labelTest3);
		// tempPanel.add(resultsTest3);
		//
		//

		// ******************************************************//
		// ******************************************************//
		// TEST 4: AVERAGE RADIAL BEARING TEST //
		// ******************************************************//
		// ******************************************************//

		JLabel labelTest4 = new JLabel();
		labelTest4.setText("Average Radial Bearing Test");

		JLabel resultsTest4 = new JLabel();
		resultsTest4.setText("Push button to run test");

		JButton btnTest4 = new JButton("Run Av.Bearing Test");
		btnTest4.setEnabled(true);
		btnTest4.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// ask for maximun and minimun radial average bearing, the range
				// in degrees

				JFrame frame = new JFrame("Average Bearing Test");
				frame.setAlwaysOnTop(true);
				String limitMin = Float.isNaN(bean.getRadialTest().getAvRadialBearingMin())
						? bean.getStationBean().getRadial_QC_average_radial_bearing_min()
						: Float.toString(bean.getRadialTest().getAvRadialBearingMin());
				if ((limitMin == null || limitMin.isEmpty() || limitMin.equals("NaN")) && profile != null) {
					limitMin = Float.toString(profile.getRadialTest().getAvRadialBearingMin());
				}

				String limitMax = Float.isNaN(bean.getRadialTest().getAvRadialBearingMax())
						? bean.getStationBean().getRadial_QC_average_radial_bearing_max()
						: Float.toString(bean.getRadialTest().getAvRadialBearingMax());
				if ((limitMax == null || limitMax.isEmpty() || limitMax.equals("NaN")) && profile != null) {
					limitMax = Float.toString(profile.getRadialTest().getAvRadialBearingMax());
				}

				String avRadBearMin = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the Minimun value for the Average Radial Bearing in degrees:", limitMin);
				String avRadBearMax = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the Maximun value for the Average Radial Bearing in degrees:", limitMax);
				if (avRadBearMax != null && avRadBearMin != null && !avRadBearMax.equals("NaN")
						&& !avRadBearMin.equals("NaN")) {
					try {
						bean.getRadialTest().setAvRadialBearingMin(Float.parseFloat(avRadBearMin));
						bean.getRadialTest().setAvRadialBearingMax(Float.parseFloat(avRadBearMax));

						List<Float> pasaAvBearingTest = runAverageBearinTest(
								bean.getTable().getColumnElements(TableColumnNames.HEAD),
								bean.getRadialTest().getAvRadialBearingMin(),
								bean.getRadialTest().getAvRadialBearingMax());
						labelTest4.setText("Test successful");
						labelTest4.setForeground(Color.blue);
						float dato = pasaAvBearingTest.get(0);
						if (dato == 1) {
							resultsTest4.setText("Good data");
						} else if (dato == 4) {
							resultsTest4.setText("Bad data");
						}

					} catch (NumberFormatException nfe) {
						// error, not a number
						JOptionPane.showMessageDialog(frame, "Error parsing average bearing range. Try again please.");
						log.error("Average bearing range parsing error");
						labelTest4.setText("Error parsing range");
						labelTest4.setForeground(Color.red);
					} catch (Exception error) {
						log.error("error running average bearing range test", error);
						labelTest4.setText("Error running average bearing range test");
						labelTest4.setForeground(Color.red);
					}
				} else {
					labelTest4.setText("No valid value");
					labelTest4.setForeground(Color.red);
				}
			}
		});
		tempPanel.add(btnTest4);
		tempPanel.add(labelTest4);
		tempPanel.add(resultsTest4);

		// ******************************************************//
		// ******************************************************//
		// TEST 5: TEMPORAL DERIVATIVE TEST //
		// ******************************************************//
		// ******************************************************//

		JLabel labelTest5 = new JLabel();
		labelTest5.setText("Temporal derivative test");

		JLabel resultsTest5 = new JLabel();
		resultsTest5.setText("Push button to run test");

		JButton btnTest5 = new JButton("Run Temp. Derivative Test");
		btnTest5.setEnabled(true);

		btnTest5.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame("Temp derivative test");
				frame.setAlwaysOnTop(true);
				String limit = Float.isNaN(bean.getRadialTest().getTempThreshold())
						? bean.getStationBean().getRadial_QC_temporal_derivative_threshold()
						: Float.toString(bean.getRadialTest().getTempThreshold());
				if ((limit == null || limit.isEmpty() || limit.equals("NaN")) && profile != null) {
					limit = Float.toString(profile.getRadialTest().getTempThreshold());
				}
				String tempThreshold = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the Temp derivative Threshold:", limit);
				if (tempThreshold != null && !tempThreshold.equals("NaN")) {
					try {
						bean.getRadialTest().setTempThreshold(Float.parseFloat(tempThreshold));

						// Test
						List<Float> pasaTempTest = runTempDerivativeTest(profile);

						labelTest5.setText("Test successful");
						labelTest5.setForeground(Color.blue);

						int val0 = 0;
						int val1 = 0;
						int val4 = 0;
						int val9 = 0;
						for (float res : pasaTempTest) {
							if (res == 1) {
								val1 = val1 + 1;
							} else if (res == 0) {
								val0 = val0 + 1;
							} else if (res == 4) {
								val4 = val4 + 1;
							} else if (res == 9) {
								val9 = val9 + 1;
							}
						}
						resultsTest5.setText("Good: " + val1 + " - Bad: " + val4);

					} catch (NumberFormatException nfe) {
						// error, not a number
						JOptionPane.showMessageDialog(frame, "Error parsing temp derivative. Try again please.");
						log.error("temp derivative parsing error");
						labelTest5.setText("Error parsing threshold");
						labelTest5.setForeground(Color.red);
					} catch (Exception error) {
						log.error("error running temp derivative test", error);
						labelTest5.setText("Error running temp derivative test");
						labelTest5.setForeground(Color.red);
					}
				} else {
					labelTest5.setText("No valid value");
					labelTest5.setForeground(Color.red);
				}
			}
		});

		tempPanel.add(btnTest5);
		tempPanel.add(labelTest5);
		tempPanel.add(resultsTest5);

		// ******************************************************//
		// ******************************************************//
		// TEST 6: MEDIAN FILTER TEST //
		// ******************************************************//
		// ******************************************************//

		JLabel labelTest6 = new JLabel();
		labelTest6.setText("Median filter test");

		JLabel resultsTest6 = new JLabel();
		resultsTest6.setText("Push button to run test");

		JButton btnTest6 = new JButton("Run Median filter Test");
		btnTest6.setEnabled(true);

		btnTest6.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame("median filter test");
				frame.setAlwaysOnTop(true);
				String limit = Float.isNaN(bean.getRadialTest().getMedianFilter())
						? bean.getStationBean().getRadial_QC_median_filter_CurLim()
						: Float.toString(bean.getRadialTest().getMedianFilter());
				String rcLim = Float.isNaN(bean.getRadialTest().getRcLim())
						? bean.getStationBean().getRadial_QC_median_filter_RCLim()
						: Float.toString(bean.getRadialTest().getRcLim());
				String angLim = Float.isNaN(bean.getRadialTest().getAngLim())
						? bean.getStationBean().getRadial_QC_median_filter_AngLim()
						: Float.toString(bean.getRadialTest().getAngLim());
				if ((limit == null || limit.isEmpty() || limit.equals("NaN")) && profile != null) {
					limit = Float.toString(profile.getRadialTest().getMedianFilter());
				}
				if ((rcLim == null || rcLim.isEmpty() || rcLim.equals("NaN")) && profile != null) {
					rcLim = Float.toString(profile.getRadialTest().getRcLim());
				}
				if ((angLim == null || angLim.isEmpty() || angLim.equals("NaN")) && profile != null) {
					angLim = Float.toString(profile.getRadialTest().getAngLim());
				}
				String medFilterThreshold = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the median filter Threshold:", limit);

				String rcLimStr = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the RCLim radius (km):", rcLim);

				String angLimStr = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the AngLim bearing distance:", angLim);

				if (medFilterThreshold != null && !medFilterThreshold.equals("NaN") && rcLimStr != null
						&& !rcLimStr.equals("NaN") && angLimStr != null && !angLimStr.equals("NaN")) {
					try {
						bean.getRadialTest().setMedianFilter(Float.parseFloat(medFilterThreshold));
						bean.getRadialTest().setRcLim(Float.parseFloat(rcLimStr));
						bean.getRadialTest().setAngLim(Float.parseFloat(angLimStr));

						// Test
						List<Float> pasaMedianFilterTest = runMedianFilterTest(profile);

						labelTest6.setText("Test successful");
						labelTest6.setForeground(Color.blue);

						int val0 = 0;
						int val1 = 0;
						int val4 = 0;
						int val9 = 0;
						for (float res : pasaMedianFilterTest) {
							if (res == 1) {
								val1 = val1 + 1;
							} else if (res == 0) {
								val0 = val0 + 1;
							} else if (res == 4) {
								val4 = val4 + 1;
							} else if (res == 9) {
								val9 = val9 + 1;
							}
						}
						resultsTest6.setText("Good: " + val1 + " - Bad: " + val4);

					} catch (NumberFormatException nfe) {
						// error, not a number
						JOptionPane.showMessageDialog(frame, "Error parsing median filter. Try again please.");
						log.error("median filter parsing error");
						labelTest6.setText("Error parsing threshold");
						labelTest6.setForeground(Color.red);
					} catch (Exception error) {
						log.error("error running median fitler test", error);
						labelTest6.setText("Error running median filter test");
						labelTest6.setForeground(Color.red);
					}
				} else {
					labelTest6.setText("No valid value");
					labelTest6.setForeground(Color.red);
				}
			}
		});

		tempPanel.add(btnTest6);
		tempPanel.add(labelTest6);
		tempPanel.add(resultsTest6);

		// ******************************************************//
		// ******************************************************//
		// TEST 7: RADIAL COUNT TEST //
		// ******************************************************//
		// ******************************************************//

		JLabel labelTest7 = new JLabel();
		labelTest7.setText("Radial count test");

		JLabel resultsTest7 = new JLabel();
		resultsTest7.setText("Push button to run test");

		JButton btnTest7 = new JButton("Run Radial count Test");
		btnTest7.setEnabled(true);

		btnTest7.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame("radial count test");
				frame.setAlwaysOnTop(true);
				String limit = Float.isNaN(bean.getRadialTest().getRadialCount())
						? bean.getStationBean().getRadial_QC_radial_count_threshold()
						: Float.toString(bean.getRadialTest().getRadialCount());
				if ((limit == null || limit.isEmpty() || limit.equals("NaN")) && profile != null) {
					limit = Float.toString(profile.getRadialTest().getRadialCount());
				}
				String radialCountThreshold = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the Radial Count Threshold:", limit);
				if (radialCountThreshold != null && !radialCountThreshold.equals("NaN")) {
					try {
						bean.getRadialTest().setRadialCount(Float.parseFloat(radialCountThreshold));

						// Test
						List<Float> pasaRadialCountTest = runRadialCountTest(profile);

						labelTest7.setText("Test successful");
						labelTest7.setForeground(Color.blue);

						float dato = pasaRadialCountTest.get(0);
						if (dato == 1) {
							resultsTest7.setText("Good data");
						} else if (dato == 4) {
							resultsTest7.setText("Bad data");
						} else if (dato == 0) {
							resultsTest7.setText("Not evaluates");
						}

					} catch (NumberFormatException nfe) {
						// error, not a number
						JOptionPane.showMessageDialog(frame, "Error parsing radial count test. Try again please.");
						log.error("radial count parsing error");
						labelTest7.setText("Error parsing threshold");
						labelTest7.setForeground(Color.red);
					} catch (Exception error) {
						log.error("error running radial count test", error);
						labelTest7.setText("Error running radial count test");
						labelTest7.setForeground(Color.red);
					}
				} else {
					labelTest7.setText("No valid value");
					labelTest7.setForeground(Color.red);
				}
			}
		});

		tempPanel.add(btnTest7);
		tempPanel.add(labelTest7);
		tempPanel.add(resultsTest7);

		JScrollPane panel = new JScrollPane(tempPanel);
		panel.setPreferredSize(new Dimension(600, 300));
		setContentPane(panel);

		this.pack();
		this.setVisible(true);

	}

	/**
	 * Over Water test: Test labeling vectors that lie on land The result is a
	 * grid The test reads 5th column of the radial data (VFLAG) and sets: 1 if
	 * the value is 0. 4 if the value is 128. put the value as is, between these
	 * two values else (negative or over 128 or empty), NaN
	 * 
	 * The definition of the field are for VFLAG Bit0 (+1)# indicate disabled
	 * grid point and not outputted in LLUV files. Bit1 (+2)# indicates that the
	 * grid point is near the coastline. Bit2 (+4)# indicates that the grid
	 * point contains a point measurement like an ADCP. Bit4 (+16)# indicates
	 * that the current vector was the result of interpolating across a baseline
	 * area. Bit5 (+32)# indicates that the vector result exceeded the Maximum
	 * current limit. Bit7 (+128)# indicates that the vector is out of bounds.
	 * For radial vectors this means that the vector was outside the Angular
	 * filter area. Not used for Total Vectors. Bit8 (+256)# indicates that the
	 * Total vector does not have enough angular resolution from the
	 * contributing radials. Bit9 (+512) indicates that the vector is marked as
	 * hidden and should not be normally displayed. Bit11(+2048) indicates that
	 * the vector was created by interpolation. Bit12(+4096) indicates that the
	 * vectors is of dubious quality and should not normally be used or
	 * displayed.
	 * 
	 * @param datos
	 */
	public List<Float> runOverWaterTest(List<Float> vflag) {
		// Take in to account the order of the elements.
		List<Float> overWaterTest = new ArrayList<Float>(vflag.size());
		for (int i = 0; i < vflag.size(); i++) {
			float value = vflag.get(i);
			if (value == 0) {
				// good data
				overWaterTest.add(i, new Float((float) 1));
			} else if (Float.isNaN(value)) {
				// missing value, NaN
				overWaterTest.add(i, value);
			} else {
				// the rest of values, are suspicious of being bad data
				overWaterTest.add(i, new Float((float) 4));
			}
		}
		return overWaterTest;
	}

	/**
	 * generic run threshold method Takes an array of float values, and a
	 * maximun value to compare de data returns a list of the quality flags
	 * 
	 * @param data
	 *            the data to test
	 * @param limit,
	 *            over or equal to this value, the data is considered wrong data
	 * @return list of short values representinig qc test flags.
	 */
	public List<Float> runThresholdTest(List<Float> data, float limit) {
		// Take in to account the order of the elements.
		List<Float> thresholdTest = new ArrayList<Float>(data.size());
		for (int i = 0; i < data.size(); i++) {
			float value = data.get(i);
			if (Math.abs(value) <= limit) {
				// good data
				thresholdTest.add(i, new Float((float) 1));
			} else if (Float.isNaN(value)) {
				// missing value
				thresholdTest.add(i, Float.NaN);
			} else if (Float.isNaN(limit)) {
				// if the limit is NaN, test not performed
				thresholdTest.add(i, new Float((float) 0));
			} else {
				// the rest of values, are suspicious of being bad data
				thresholdTest.add(i, new Float((float) 4));
			}
		}
		return thresholdTest;
	}

	/**
	 * Test labeling radial data having a number of velocity vectors bigger than
	 * the threshold with a good_data flag and radial data having a number of
	 * velocity vectors smaller than the threshold with a bad_data flag
	 */

	public List<Float> runRadialCountTest(CodarRadialBean profile) {

		// Dependant of Velo test, check if good values are more than the
		// threshold.
		List<Float> veloRead = bean.getTable().getColumnElementsInOrder(TableColumnNames.VELO,
				CodarUtils.getRadialIndexArray(bean, bean.getTable(), profile));
		List<Float> velo = new ArrayList<Float>(veloRead.size());
		for (Float velTemp : veloRead) {
			velo.add(new Float(-velTemp / 100));
		}

		int val1 = 0; // good data if value == 1
		for (float res : velo) {
			if (!Float.isNaN(res)) {
				val1 = val1 + 1;
			}
		}

		List<Float> resultado = new ArrayList<Float>(1);
		Float radCount = Float.isNaN(bean.getRadialTest().getRadialCount())
				? bean.getStationBean().getRadial_QC_radial_count_threshold_float()
				: bean.getRadialTest().getRadialCount();
		if (radCount < val1) {
			resultado.add(0, new Float(1));
		} else {
			resultado.add(0, new Float(4));
		}

		return resultado;

	}

	/**
	 * For each source vector, the median of all velocities within a radius of
	 * <RCLim> and whose vector bearing (angle of arrival at site) is also
	 * within an angular distance of <AngLim> degres from the source vector's
	 * bearing is evaluated.
	 * 
	 * @return array with all the flags, if the difference between the vector's
	 *         velocity and the median velocity is greater than a threshold,
	 *         then the vector is labeled with "bad_data" flag, otherwise it is
	 *         labeled with a "good_data" flag.
	 */
	public List<Float> runMedianFilterTest(CodarRadialBean profile) {

		List<Float> veloRead = bean.getTable().getColumnElementsInOrder(TableColumnNames.VELO,
				CodarUtils.getRadialIndexArray(bean, bean.getTable(), profile));
		List<Float> velo = new ArrayList<Float>(veloRead.size());
		for (Float velTemp2 : veloRead) {
			velo.add(new Float(-velTemp2 / 100));
		}

		List<Float> bear = bean.getTable().getColumnElementsInOrder(TableColumnNames.BEAR,
				CodarUtils.getRadialIndexArray(bean, bean.getTable(), profile));
		List<Float> lat = bean.getTable().getColumnElementsInOrder(TableColumnNames.LATD,
				CodarUtils.getRadialIndexArray(bean, bean.getTable(), profile));
		List<Float> lon = bean.getTable().getColumnElementsInOrder(TableColumnNames.LOND,
				CodarUtils.getRadialIndexArray(bean, bean.getTable(), profile));

		List<Float> medianMatrix = new ArrayList<Float>(velo.size());
		List<Float> mfResults = new ArrayList<Float>(velo.size());

		float rcLim = Float.isNaN(this.bean.getRadialTest().getRcLim())
				? this.bean.getStationBean().getRadial_QC_median_filter_RCLim_float()
				: this.bean.getRadialTest().getRcLim();
		float angLim = Float.isNaN(this.bean.getRadialTest().getAngLim())
				? this.bean.getStationBean().getRadial_QC_median_filter_AngLim_float()
				: this.bean.getRadialTest().getAngLim();
		float mfThreshold = Float.isNaN(this.bean.getRadialTest().getMedianFilter())
				? this.bean.getStationBean().getRadial_QC_median_filter_CurLim_float()
				: this.bean.getRadialTest().getMedianFilter();

		// For each point check the distance and angle variation to calculate
		// the mean value.
		for (int i = 0; i < lat.size(); i++) {
			// Data from the current point
			float veloBase = velo.get(i);
			float bearBase = bear.get(i);
			float latBase = lat.get(i);
			float lonBase = lon.get(i);

			float tempMedian = 0;
			int counter = 0;

			for (int j = 0; j < lat.size(); j++) {
				// Checking the rest,
				float veloTemp = velo.get(j);
				float bearTemp = bear.get(j);
				float latTemp = lat.get(j);
				float lonTemp = lon.get(j);

				// Check if the iteration is over the current element, checks if
				// the bearing
				// of the temporal element is not at an angular distance greater
				// than angLim and
				// checks that the distance between the points is not greater
				// than rcLim
				if (j == i || ((Math.abs(bearTemp - bearBase) <= angLim)
						&& (Lldistkm.calculate(latBase, lonBase, latTemp, lonTemp)[0] <= rcLim))) {
					counter = counter + 1;
					tempMedian = tempMedian + veloTemp;
				}
			}
			// now, calculate de median
			float medianDiff = Math.abs(tempMedian / counter - veloBase);

			// Evaluate the median with the median threshold
			medianMatrix.add(i, medianDiff);
		}

		mfResults = runThresholdTest(medianMatrix, mfThreshold);

		return mfResults;
	}

	/**
	 * temporal derivative test It looks for files with the same name but one
	 * hour before and after the present file Then, compares the velocity
	 * vectors and checks that the difference is not bigger than the threshold
	 * 
	 * @return array with all the flags related to the velocity array.
	 */
	public List<Float> runTempDerivativeTest(CodarRadialBean profile) {

		List<Float> velo = bean.getTable().getColumnElementsInOrder(TableColumnNames.VELO,
				CodarUtils.getRadialIndexArray(bean, bean.getTable(), profile));
		float limit = Float.isNaN(bean.getRadialTest().getTempThreshold())
				? bean.getStationBean().getRadial_QC_temporal_derivative_thresholdFloat()
				: bean.getRadialTest().getTempThreshold();
		String filename = bean.getPathToFile();
		SimpleDateFormat filenameFormat = new SimpleDateFormat("yyyy_MM_dd_HHmm");

		List<Float> tdThreshold = new ArrayList<Float>(velo.size());

		filenameFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Calendar previous;
		Calendar next;
		try {
			previous = (Calendar) bean.getTimeStampAsCalendar().clone();
			next = (Calendar) bean.getTimeStampAsCalendar().clone();
			previous.add(Calendar.MINUTE, Math.negateExact(bean.getNetworkBean().getTemporal_resolution_Int()));
			next.add(Calendar.MINUTE, bean.getNetworkBean().getTemporal_resolution_Int());
			String prevFileName = filename.replace(filenameFormat.format(bean.getTimeStampAsCalendar().getTime()),
					filenameFormat.format(previous.getTime()));
			String nextFileName = filename.replace(filenameFormat.format(bean.getTimeStampAsCalendar().getTime()),
					filenameFormat.format(next.getTime()));

			// Check if files exists
			File prevFile = new File(prevFileName);
			File nextFile = new File(nextFileName);

			if (prevFile.exists() && nextFile.exists()) {
				// perform tests
				Properties props = new Properties();
				props.load(new FileInputStream("codar.properties"));

				CodarRadialBean prevBean = CodarUtils.loadCodarRadialData(prevFile, props);
				CodarRadialBean nextBean = CodarUtils.loadCodarRadialData(nextFile, props);

				List<Float> prevVelo = prevBean.getTable().getColumnElementsInOrder(TableColumnNames.VELO,
						CodarUtils.getRadialIndexArray(bean, prevBean.getTable(), profile));
				List<Float> nextVelo = nextBean.getTable().getColumnElementsInOrder(TableColumnNames.VELO,
						CodarUtils.getRadialIndexArray(bean, nextBean.getTable(), profile));

				if (prevVelo.size() == velo.size() && nextVelo.size() == velo.size()) {
					List<Float> diffPrev = new ArrayList<Float>(velo.size());
					List<Float> diffNext = new ArrayList<Float>(velo.size());

					for (int i = 0; i < velo.size(); i++) {
						if ((Float.isNaN(prevVelo.get(i)) || Float.isNaN(nextVelo.get(i)))
								&& !Float.isNaN(velo.get(i))) {
							// If the previous or next values are not present,
							// we write a -1 that means flag = 0: it cannot be
							// evaluated
							// Modified, very bad values. We left as 0, to have
							// a flag = 1, good value
							// TODO check this with the community.
							diffPrev.add(i, 0f);
							diffNext.add(i, 0f);
						} else if (Float.isNaN(velo.get(i))) {
							// if some value are bad in the previous or next
							// file, the current is taken as good one.
							diffPrev.add(i, Float.NaN);
							diffNext.add(i, Float.NaN);
						} else {
							diffPrev.add(i, (Math.abs(prevVelo.get(i) - velo.get(i))) / 100);
							diffNext.add(i, (Math.abs(nextVelo.get(i) - velo.get(i))) / 100);
						}
					}
					tdThreshold = runDoubleThresholdTest(diffPrev, diffNext, limit);
				} else {
					// tests can not be performed due to a missing files.
					tdThreshold = runThresholdTest(velo, Float.NaN);
				}

			} else {
				// tests can not be performed due to a missing files.
				tdThreshold = runThresholdTest(velo, Float.NaN);
			}
		} catch (ParseException e) {
			log.error("Error trying to perform time derivative tests", e);
		} catch (FileNotFoundException e) {
			log.error("property file not found", e);
		} catch (IOException e) {
			log.error("error al leer el fichero de propiedades", e);
		}

		return tdThreshold;
	}

	/**
	 * Average Range Bearing Test
	 * 
	 * In the radial file, we calculate the mean of the not null values of the
	 * bearing That mean values must be inside the maximum and minimun defined
	 * in the thresholds
	 * 
	 * @param data
	 *            datos de direccion del radar, columna HEAD en CODAR
	 * @param minimun
	 *            limite minimo del rango permitido
	 * @param maximun
	 *            limite maximo del rango permitido
	 * @return numero con el valor del flag, con el resultado del test
	 */
	public List<Float> runAverageBearinTest(List<Float> data, float minimum, float maximum) {
		// the result array
		List<Float> resultado = new ArrayList<Float>(1);
		// counter for not null values, only not nulls must be taken in to
		// account.
		int i = 0;
		// The addition of the values
		float suma = 0;
		for (Float valor : data) {
			if (!valor.isNaN()) {
				i++;
				suma = suma + valor.floatValue();
			}
		}
		if (suma / i >= minimum && suma / i <= maximum)
			resultado.add(0, new Float((float) 1));
		else
			resultado.add(0, new Float((float) 4));

		return resultado;
	}

	/**
	 * double threshold method Takes two array of float values, and a maximun
	 * value to compare both data Both data arrays must be same size. returns a
	 * list of the quality flags First version to check the temporal derivative
	 * 
	 * @param data1
	 *            the data to test
	 * @param data2
	 *            the data to test
	 * @param limit,
	 *            over or equal to this value, the data is considered wrong data
	 * @return list of short values representinig qc test flags.
	 */
	public List<Float> runDoubleThresholdTest(List<Float> data1, List<Float> data2, float limit) {
		List<Float> thresholdTest = new ArrayList<Float>(data1.size());
		if (data1.size() == data2.size()) {
			for (int i = 0; i < data1.size(); i++) {
				float value1 = data1.get(i);
				float value2 = data2.get(i);
				if (Float.isNaN(value1) || Float.isNaN(value2)) {
					// missing value
					thresholdTest.add(i, Float.NaN);
				} else if (value1 < 0 || value2 < 0) {
					// Test not performed, a +1h -1h is not present.
					thresholdTest.add(i, new Float((float) 0));
				} else if (value1 <= limit && value2 <= limit) {
					// good data
					thresholdTest.add(i, new Float((float) 1));
				} else if (Float.isNaN(limit)) {
					// If the limit is NaN, we cannot perform the test.
					thresholdTest.add(i, new Float((float) 0));
				} else {
					// the rest of values, are suspicious of being bad data
					thresholdTest.add(i, new Float((float) 4));
				}
			}
		} else {
			// test not performed

			for (int i = 0; i < data1.size(); i++) {
				thresholdTest.add(i, new Float((float) 0));
			}
		}
		return thresholdTest;
	}

}
