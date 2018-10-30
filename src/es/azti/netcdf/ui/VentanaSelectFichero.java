package es.azti.netcdf.ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 
 * @author Jose Luis Asensio (jlasensio@azti.es)
 * 8 de mar. de 2016
 * 
 * Frame to load a file from the file system.
 *
 */
public class VentanaSelectFichero extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JTextArea textArea;

	/**
	 * @author Jose Luis Asensio (jlasensio@azti.es)
	 * 
	 * Init Frame
	 */
	public VentanaSelectFichero() {
		
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setLayout(null);
		setContentPane(contentPane);

		textField = new JTextField();
		textField.setToolTipText("Type the path to the file");
		textField.setBounds(52, 26, 209, 20);
		contentPane.add(textField);
		textField.setColumns(10);

		JButton btnSeleccionar = new JButton("Select...");
		btnSeleccionar.setBounds(288, 25, 109, 23);
		contentPane.add(btnSeleccionar);

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBounds(52, 76, 360, 156);

		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setBounds(52, 76, 360, 156);
		contentPane.add(scroll);

	}
}
