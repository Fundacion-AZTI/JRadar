/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  18 abril. de 2017
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

import es.azti.codar.beans.CodarTotalBean;
import es.azti.codar.utils.CodarUtils;
import es.azti.utils.TableColumnNames;

/**
 * Cass defining the window that helps running the QAQC tests for total files.
 * 
 * @author Jose Luis Asensio (jlasensio@azti.es) 18 abril. 2017
 *
 */
public class VentanaRunTotalQualityTests extends JFrame {

	/**
	 * This window manage the execution of necessary operations in order to run
	 * the quality control test for totals. The bean with the neccesary data to
	 * run the quality tests for totals is an attribute of the generic bean of
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
	private CodarTotalBean bean;

	public VentanaRunTotalQualityTests(CodarTotalBean datos) {
		// Start logger
		log = Logger.getLogger(VentanaRunTotalQualityTests.class);
		this.bean = datos;
	}

	/**
	 * shows the main window for total QC tests
	 * 
	 * @param profile
	 *            a profile just in case some default values are needed.
	 */
	public void mostrarTests(CodarTotalBean profile) {
		try {
			setIconImage(ImageIO.read(new File("logoVentana.png")));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		setName("HF Radar total data tester");
		setTitle("HF Radar total data tester");
		setBounds(400, 100, 550, 400);
		setAlwaysOnTop(true);

		JPanel tempPanel = new JPanel();

		// User interface buttons layout.
		GridLayout glayout = new GridLayout();
		glayout.setColumns(3);
		glayout.setRows(4);
		// gap between elements of the table..
		glayout.setHgap(5);
		glayout.setVgap(5);
		tempPanel.setLayout(glayout);

		// ******************************************************//
		// ******************************************************//
		// TEST 1: DATA DENSITY THRESHOLD //
		// ******************************************************//
		// ******************************************************//
		JLabel labelTest1 = new JLabel();
		labelTest1.setText("Data density threshold test");

		JLabel resultsTest1 = new JLabel();
		resultsTest1.setText("Push button to run test");

		JButton btnTest1 = new JButton("Run data density Test");
		btnTest1.setEnabled(true);

		btnTest1.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// ask for a value of threshold and propose one from totals.
				JFrame frame = new JFrame("Data Density Threshold test");
				frame.setAlwaysOnTop(true);
				String limit = null;
				if (Float.isNaN(bean.getTotalTest().getDataDensityThreshold())) {
					if (bean.getNetworkBean() != null)
						limit = bean.getNetworkBean().getTotal_QC_data_density_threshold();
				} else {
					limit = Float.toString(bean.getTotalTest().getDataDensityThreshold());
				}
				if ((limit == null || limit.isEmpty() || limit.equals("NaN")) && profile != null) {
					limit = Float.toString(profile.getTotalTest().getDataDensityThreshold());
				}
				String ddThreshold = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the Data Density Threshold:", limit);
				if (ddThreshold != null && !ddThreshold.equals("NaN")) {
					try {
						bean.getTotalTest().setDataDensityThreshold(Float.parseFloat(ddThreshold));

						List<Float> pasaDDTest = runDataDensityTest(bean.getTotalTest().getDataDensityThreshold(),
								profile);
						labelTest1.setText("Test successful");
						labelTest1.setForeground(Color.blue);

						int val1 = 0;
						int val4 = 0;
						int val9 = 0;
						for (float res : pasaDDTest) {
							if (res == 1) {
								val1 = val1 + 1;
							} else if (res == 4) {
								val4 = val4 + 1;
							} else if (res == 9) {
								val9 = val9 + 1;
							}
						}
						resultsTest1.setText("Good: " + val1 + " - Bad: " + val4);

					} catch (NumberFormatException nfe) {
						// error, not a number
						JOptionPane.showMessageDialog(frame, "Error parsing data density threshold. Try again please.");
						log.error("Data density threshold parsing error");
						labelTest1.setText("Error parsing threshold");
						labelTest1.setForeground(Color.red);
					} catch (Exception error) {
						log.error("error running data density threshold test", error);
						labelTest1.setText("Error running data density threshold test");
						labelTest1.setForeground(Color.red);
					}
				} else {
					labelTest1.setText("No valid value");
					labelTest1.setForeground(Color.red);
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
				// ask for a value of threshold and propose one from totals.
				JFrame frame = new JFrame("Velocity Threshold test");
				frame.setAlwaysOnTop(true);
				String limit = Float.isNaN(bean.getTotalTest().getVeloThreshold())
						? bean.getNetworkBean().getTotal_QC_velocity_threshold()
						: Float.toString(bean.getTotalTest().getVeloThreshold());
				if ((limit == null || limit.isEmpty() || limit.equals("NaN")) && profile != null) {
					limit = Float.toString(profile.getTotalTest().getVeloThreshold());
				}
				String veloThreshold = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the Velocity Threshold:", limit);
				if (veloThreshold != null && !veloThreshold.equals("NaN")) {
					try {
						bean.getTotalTest().setVeloThreshold(Float.parseFloat(veloThreshold));
						List<Float> veloURead = bean.getTable().getColumnElements(TableColumnNames.VELU);
						List<Float> veloVRead = bean.getTable().getColumnElements(TableColumnNames.VELV);
						List<Float> velo = new ArrayList<Float>(veloURead.size());
						for (int i = 0; i < veloVRead.size(); i++) {
							velo.add(new Float(Math
									.sqrt(Math.pow(veloURead.get(i) / 100, 2) + Math.pow(veloVRead.get(i) / 100, 2))));
						}

						List<Float> pasaVeloTest = runThresholdTest(velo, bean.getTotalTest().getVeloThreshold());
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
						resultsTest2.setText("Good: " + val1 + " - Bad: " + val4);

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

		// //******************************************************//
		// //******************************************************//
		// // TEST 3: VARIANCE THRESHOLD //
		// //******************************************************//
		// //******************************************************//
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
		// //ask for a value of threshold and propose one from totals.
		// //primero mostrar el límite y pedir confirmacion
		// //luego validar que es un dato valido
		// //ultimo, lanzar test
		// JFrame frame = new JFrame("Variance Threshold Test");
		// frame.setAlwaysOnTop(true);
		// String limit =
		// Float.isNaN(bean.getTotalTest().getVarianceThreshold())?bean.getNetworkBean().getTotal_QC_variance_threshold():Float.toString(bean.getTotalTest().getVarianceThreshold());
		// if ((limit == null || limit.isEmpty() ||
		// limit.equals("NaN"))&&profile!=null) {limit =
		// Float.toString(profile.getTotalTest().getVarianceThreshold());}
		//
		// String varThreshold = (String) JOptionPane.showInputDialog(frame,
		// "Enter a value for the Variance Threshold:",
		// limit);
		// if (varThreshold != null && !varThreshold.equals("NaN")) {
		// try {
		// bean.getTotalTest().setVarianceThreshold(Float.parseFloat(varThreshold));
		//
		// //tabla basada en posicion de indice 0, primer elemento posicion 0.
		// El 7º elemento ETMP está en la posición 6
		// //El elemento ETMP de la tabla, es la varianza temporal, Temporal
		// quality
		//
		// //en este caso no nos interesa el resultado, no lo utilizamos (se
		// podria visualizar?)
		// //TODO hacer media del 5 y 6
		//
		// List<Float> pasaVarTest =
		// runVarianceThresholdTest(bean.getTotalTest().getVarianceThreshold());
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
		// resultsTest3.setText("Good: " + val1 + " - Bad: " + val4);
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

		// ******************************************************//
		// ******************************************************//
		// TEST 4: Temporal THRESHOLD //
		// ******************************************************//
		// ******************************************************//

		JLabel labelTest4 = new JLabel();
		labelTest4.setText("Temporal derivative test");

		JLabel resultsTest4 = new JLabel();
		resultsTest4.setText("Push button to run test");

		JButton btnTest4 = new JButton("Run Temp. Derivative Test");
		btnTest4.setEnabled(true);

		btnTest4.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// ask for a value of temporal derivative
				JFrame frame = new JFrame("Temp derivative test");
				frame.setAlwaysOnTop(true);
				String limit = Float.isNaN(bean.getTotalTest().getTempThreshold())
						? bean.getNetworkBean().getTotal_QC_temporal_derivative_threshold()
						: Float.toString(bean.getTotalTest().getTempThreshold());
				if ((limit == null || limit.isEmpty() || limit.equals("NaN")) && profile != null) {
					limit = Float.toString(profile.getTotalTest().getTempThreshold());
				}
				String tempThreshold = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the Temp derivative Threshold:", limit);
				if (tempThreshold != null && !tempThreshold.equals("NaN")) {
					try {
						bean.getTotalTest().setTempThreshold(Float.parseFloat(tempThreshold));

						// Test
						List<Float> pasaTempTest = runTempDerivativeTest(profile);

						labelTest4.setText("Test successful");
						labelTest4.setForeground(Color.blue);

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
						resultsTest4.setText("Good: " + val1 + " - Bad: " + val4);

					} catch (NumberFormatException nfe) {
						// error, not a number
						JOptionPane.showMessageDialog(frame, "Error parsing temp derivative. Try again please.");
						log.error("temp derivative parsing error");
						labelTest4.setText("Error parsing threshold");
						labelTest4.setForeground(Color.red);
					} catch (Exception error) {
						log.error("error running temp derivative test", error);
						labelTest4.setText("Error running temp derivative test");
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
		// TEST 5: GDOP THRESHOLD //
		// ******************************************************//
		// ******************************************************//

		JLabel labelTest5 = new JLabel();
		labelTest5.setText("GDOP threshold test");

		JLabel resultsTest5 = new JLabel();
		resultsTest5.setText("Push button to run test");

		JButton btnTest5 = new JButton("Run GDOP Test");
		btnTest5.setEnabled(true);

		btnTest5.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame("GDOP Threshold test");
				frame.setAlwaysOnTop(true);
				String limit = Float.isNaN(bean.getTotalTest().getGDOPThreshold())
						? bean.getNetworkBean().getTotal_QC_GDOP_threshold()
						: Float.toString(bean.getTotalTest().getGDOPThreshold());
				if ((limit == null || limit.isEmpty() || limit.equals("NaN")) && profile != null) {
					limit = Float.toString(profile.getTotalTest().getGDOPThreshold());
				}
				String gdopThreshold = (String) JOptionPane.showInputDialog(frame,
						"Enter a value for the GDOP Threshold:", limit);
				if (gdopThreshold != null && !gdopThreshold.equals("NaN")) {
					try {
						bean.getTotalTest().setGDOPThreshold(Float.parseFloat(gdopThreshold));
						List<Float> pasaGDOPTest = runThresholdTest(
								bean.getTable().getColumnElements(TableColumnNames.CQAL),
								bean.getTotalTest().getGDOPThreshold());
						labelTest5.setText("Test successful");
						labelTest5.setForeground(Color.blue);

						int val1 = 0;
						int val4 = 0;
						int val9 = 0;
						for (float res : pasaGDOPTest) {
							if (res == 1) {
								val1 = val1 + 1;
							} else if (res == 4) {
								val4 = val4 + 1;
							} else if (res == 9) {
								val9 = val9 + 1;
							}
						}
						resultsTest5.setText("Good: " + val1 + " - Bad: " + val4);

					} catch (NumberFormatException nfe) {
						// error, not a number
						JOptionPane.showMessageDialog(frame, "Error parsing GDOP threshold. Try again please.");
						log.error("GDOP threshold parsing error");
						labelTest5.setText("Error parsing threshold");
						labelTest5.setForeground(Color.red);
					} catch (Exception error) {
						log.error("error running GDOP threshold test", error);
						labelTest5.setText("Error running GDOP threshold test");
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

		JScrollPane panel = new JScrollPane(tempPanel);
		panel.setPreferredSize(new Dimension(600, 300));
		setContentPane(panel);

		this.pack();
		this.setVisible(true);
	}

	/**
	 * temporal derivative test It looks for files with the same name but one
	 * hour before and after the present file Then, compares the velocity
	 * vectors and checks that the difference is not bigger than the threshold
	 * 
	 * @return array with all the flags related to the velocity array.
	 */

	public List<Float> runTempDerivativeTest(CodarTotalBean profile) {

		List<Integer> codarToNetcdfIndex = CodarUtils.getTotalIndexArray(bean, bean.getTable(), profile);
		List<Float> veloURead = bean.getTable().getColumnElementsInOrder(TableColumnNames.VELU, codarToNetcdfIndex);
		List<Float> veloVRead = bean.getTable().getColumnElementsInOrder(TableColumnNames.VELV, codarToNetcdfIndex);
		List<Float> velo = new ArrayList<Float>(veloURead.size());
		for (int i = 0; i < veloVRead.size(); i++) {
			velo.add(new Float(Math.sqrt(Math.pow(veloURead.get(i), 2) + Math.pow(veloVRead.get(i), 2))));
		}

		float limit = Float.isNaN(bean.getTotalTest().getTempThreshold())
				? bean.getNetworkBean().getTotal_QC_temporal_derivative_threshold_float()
				: bean.getTotalTest().getTempThreshold();
		String filename = bean.getPathToFile();
		SimpleDateFormat filenameFormat = new SimpleDateFormat("yyyy_MM_dd_HHmm");

		List<Float> tdThreshold = new ArrayList<Float>(velo.size());

		filenameFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Calendar previous;
		Calendar next;
		try {
			previous = (Calendar) bean.getTimeStampAsCalendar().clone();
			next = (Calendar) bean.getTimeStampAsCalendar().clone();
			previous.add(Calendar.HOUR, -1);
			next.add(Calendar.HOUR, 1);
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

				CodarTotalBean prevBean = CodarUtils.loadCodarTotalData(prevFile, props);
				CodarTotalBean nextBean = CodarUtils.loadCodarTotalData(nextFile, props);

				// temp
				List<Integer> codarToNetcdfIndex1 = CodarUtils.getTotalIndexArray(bean, prevBean.getTable(), profile);
				List<Integer> codarToNetcdfIndex2 = CodarUtils.getTotalIndexArray(bean, nextBean.getTable(), profile);

				List<Float> prevVelo = prevBean.getTable().getColumnElementsInOrder(TableColumnNames.VELO,
						codarToNetcdfIndex1);
				List<Float> nextVelo = nextBean.getTable().getColumnElementsInOrder(TableColumnNames.VELO,
						codarToNetcdfIndex2);

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
			// TODO Auto-generated catch block
			log.error("Error trying to perform time derivative tests", e);
		} catch (FileNotFoundException e) {
			log.error("property file not found", e);
		} catch (IOException e) {
			log.error("error al leer el fichero de propiedades", e);
		}

		return tdThreshold;
	}

	public List<Float> runDataDensityTest(float limit, CodarTotalBean profile) {

		List<Integer> codarToNetcdfIndex = CodarUtils.getTotalIndexArray(bean, bean.getTable(), profile);
		List<Float> site1 = bean.getTable().getColumnElementsInOrder(TableColumnNames.S1CN, codarToNetcdfIndex);
		List<Float> site2 = bean.getTable().getColumnElementsInOrder(TableColumnNames.S2CN, codarToNetcdfIndex);
		List<Float> site3 = bean.getTable().getColumnElementsInOrder(TableColumnNames.S3CN, codarToNetcdfIndex);
		List<Float> site4 = bean.getTable().getColumnElementsInOrder(TableColumnNames.S4CN, codarToNetcdfIndex);
		List<Float> site5 = bean.getTable().getColumnElementsInOrder(TableColumnNames.S5CN, codarToNetcdfIndex);
		List<Float> site6 = bean.getTable().getColumnElementsInOrder(TableColumnNames.S6CN, codarToNetcdfIndex);
		List<Float> ddThreshold = new ArrayList<Float>(site1.size());
		for (int i = 0; i < site1.size(); i++) {
			float total = 0;
			if (!site1.isEmpty())
				total = total + site1.get(i);
			if (!site2.isEmpty())
				total = total + site2.get(i);
			if (!site3.isEmpty())
				total = total + site3.get(i);
			if (!site4.isEmpty())
				total = total + site4.get(i);
			if (!site5.isEmpty())
				total = total + site5.get(i);
			if (!site6.isEmpty())
				total = total + site6.get(i);

			ddThreshold.add(i, total);
		}
		List<Float> vuelta = runInverseThresholdTest(ddThreshold, limit);
		return vuelta;
	}

	public List<Float> runVarianceThresholdTest(float limit, CodarTotalBean profile) {
		// TODO calculate properly, Variance Threshold Quality
		List<Integer> codarToNetcdfIndex = CodarUtils.getTotalIndexArray(bean, bean.getTable(), profile);
		List<Float> ustdevThreshold_QCflag = bean.getTable().getColumnElementsInOrder(TableColumnNames.UQAL,
				codarToNetcdfIndex);
		List<Float> vstdevThreshold_QCflag = bean.getTable().getColumnElementsInOrder(TableColumnNames.VQAL,
				codarToNetcdfIndex);
		List<Float> varianceThreshold_QCflag = new ArrayList<Float>(ustdevThreshold_QCflag.size());
		for (int i = 0; i < ustdevThreshold_QCflag.size(); i++) {
			float value = (float) Math
					.sqrt((Math.pow(vstdevThreshold_QCflag.get(i), 2) + Math.pow(ustdevThreshold_QCflag.get(i), 2)));
			varianceThreshold_QCflag.add(i, value);
		}
		List<Float> vuelta = runThresholdTest(varianceThreshold_QCflag, limit);
		return vuelta;

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
				// If the limit is NaN, we cannot perform the test.
				thresholdTest.add(i, new Float((float) 0));
			} else {
				// the rest of values, are suspicious of being bad data
				thresholdTest.add(i, new Float((float) 4));
			}
		}
		return thresholdTest;
	}

	/**
	 * Inverse threshold method Takes an array of float values, and a minimun
	 * value to compare the data (values must be higher that the limit to mark
	 * as good values) returns a list of the quality flags
	 * 
	 * @param data
	 *            the data to test
	 * @param limit,
	 *            over or equal to this value, the data is considered wrong data
	 * @return list of short values representinig qc test flags.
	 */
	public List<Float> runInverseThresholdTest(List<Float> data, float limit) {
		List<Float> thresholdTest = new ArrayList<Float>(data.size());
		for (int i = 0; i < data.size(); i++) {
			float value = data.get(i);
			if (Math.abs(value) >= limit) {
				// good data
				thresholdTest.add(i, new Float((float) 1));
			} else if (Float.isNaN(value)) {
				// missing value
				thresholdTest.add(i, Float.NaN);
			} else if (Float.isNaN(limit)) {
				// If the limit is NaN, we cannot perform the test.
				thresholdTest.add(i, new Float((float) 0));
			} else {
				// the rest of values, are suspicious of being bad data
				thresholdTest.add(i, new Float((float) 4));
			}
		}
		return thresholdTest;
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
