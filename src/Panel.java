import java.awt.Color;

import javax.swing.*;
public class Panel
{
	//declares all UI objects
	JFrame frame;
	JPanel panel;
	
	//LABELS
	JLabel label1; //particle radius
	JLabel label2; //particle count
	JLabel label3; //average temperature
	JLabel label4; //particle mass
	JLabel label5; //chamber width
	JLabel label6; //chamber height
	JLabel label7; //sim speed
	JLabel label8; //zoom
	JLabel label9; //scenario
	JLabel label10; //color scale
	
	//TEXTFIELDS
	JTextField field1; //particle radius
	JTextField field2; //particle count
	JTextField field3; //average temperature
	JTextField field4; //particle mass
	JTextField field5; //chamber width
	JTextField field6; //chamber height
	JTextField field7; //sim speed
	JTextField field8; //zoom
	JTextField field9; //maxtemp
	JTextField field10; //mintemp
	
	//VARIABLES
	double radius;
	int number;
	double temperature;
	double mass;
	double width;
	double height;
	double simspeed;
	double zoom;
	double mintemp;
	double maxtemp;
	
	//COMBOBOX
	JComboBox settings;
	
	//BUTTONS
	JButton button1; //Start
	JButton button2; //Stop
	JButton button3; //language
	
	Display display;
	
	Panel() //Constructor, runs when program launches
	{
		//initialize all UI Object, give position on screen and pre-fill fields with values
		frame = new JFrame("2D ideales Gas Simulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(370,430);
		frame.setLocation(0,0);
		frame.setResizable(false);
		
		panel = new JPanel();
		panel.setLayout(null);
		
		panel.setBackground(Color.WHITE);
		
		label1 = new JLabel("Teilchenradius [pm]"); //particle radius
		label1.setBounds(10,10,200,30);
		panel.add(label1);
		
		label2 = new JLabel("Teilchenanzahl []"); //particle count
		label2.setBounds(10,40,200,30);
		panel.add(label2);
		
		label3 = new JLabel("Temperatur [K]"); //average temperature
		label3.setBounds(10,70,200,30);
		panel.add(label3);
		
		label4 = new JLabel("Teilchemasse [u]"); //particle mass
		label4.setBounds(10,100,200,30);
		panel.add(label4);
		
		label5 = new JLabel("Kammerbreite [nm]"); //chamber width
		label5.setBounds(10,130,200,30);
		panel.add(label5);
		
		label6 = new JLabel("Kammerhöhe [nm]"); //chamber height
		label6.setBounds(10,160,200,30);
		panel.add(label6);
		
		label7 = new JLabel("Simulationsgeschwindigkeit [ps/s]"); //simualtion speed
		label7.setBounds(10,190,200,30);
		panel.add(label7);
		
		label8 = new JLabel("Zoom [px/nm]"); //zoom
		label8.setBounds(10,220,200,30);
		panel.add(label8);
		
		field1 = new JTextField(); //radius
		field1.setBounds(230,10,115,30);
		field1.setText("50");
		panel.add(field1);
		
		field2 = new JTextField(); //number
		field2.setBounds(230,40,115,30);
		field2.setText("1000");
		panel.add(field2);
		
		field3 = new JTextField(); //temperature
		field3.setBounds(230,70,115,30);
		field3.setText("298");
		panel.add(field3);
		
		field4 = new JTextField(); //mass
		field4.setBounds(230,100,115,30);
		field4.setText("1");
		panel.add(field4);
		
		field5 = new JTextField(); //width
		field5.setBounds(230,130,115,30);
		field5.setText("14");
		panel.add(field5);
		
		field6 = new JTextField(); //height
		field6.setBounds(230,160,115,30);
		field6.setText("9");
		panel.add(field6);
		
		field7 = new JTextField(); //simspeed
		field7.setBounds(230,190,115,30);
		field7.setText("0.1");
		panel.add(field7);
		
		field8 = new JTextField(); //zoom
		field8.setBounds(230,220,115,30);
		field8.setText("75");
		panel.add(field8);
		
	    label9 = new JLabel("Szenario"); //scenarios
		label9.setBounds(10,270,200,30);
		panel.add(label9);
		
		String[] modes = {"Standard","Diffusion 1","Diffusion 2","Brown'sche Molekularbewegung","Wärmeleitung","Entropie","Halbes Vakuum"};
		settings = new JComboBox(modes);
		settings.setBounds(130,270,214,30);
		settings.setBackground(Color.WHITE);
		panel.add(settings);
		
		label10 = new JLabel("Farbskala Blau-Rot [K]"); //color scale red-blue
		label10.setBounds(10,310,200,30);
		panel.add(label10);
		
		field9 = new JTextField(); //mintemp
		field9.setBounds(180,310,78,30);
		field9.setText("10");
		panel.add(field9);
		
		field10 = new JTextField(); //maxtemp
		field10.setBounds(268,310,77,30);
		field10.setText("1000");
		panel.add(field10);
		
		
		button1 = new JButton("Start"); //start button
		button1.setBounds(10,350,125,30);
		button1.addActionListener(new AktionsLauscher(this)); //adds ActionListener
		button1.setBackground(Color.WHITE);
		panel.add(button1);
		
		button2 = new JButton("Stop"); //stop button
		button2.setBounds(145,350,125,30);
		button2.addActionListener(new AktionsLauscher(this)); //adds ActionListener
		button2.setBackground(Color.WHITE);
		button2.setEnabled(false);
		panel.add(button2);
		
		button3 = new JButton("EN"); //language button
		button3.setBounds(280,350,65,30);
		button3.addActionListener(new AktionsLauscher(this)); //adds ActionListener
		button3.setBackground(Color.WHITE);
		panel.add(button3);
		
		frame.add(panel);
		frame.setVisible(true);
	}
	public void start()
	{
		try //get values and parse them to doubles
		{
			radius = Double.parseDouble(field1.getText())/1000.0;
			number = Integer.parseInt(field2.getText());
			temperature = Double.parseDouble(field3.getText());
			mass = Double.parseDouble(field4.getText());
			width = Double.parseDouble(field5.getText());
			height = Double.parseDouble(field6.getText());
			simspeed = Double.parseDouble(field7.getText())/1000;
			zoom = Double.parseDouble(field8.getText());
			mintemp = Double.parseDouble(field9.getText());
			maxtemp = Double.parseDouble(field10.getText());
			int mode = settings.getSelectedIndex();
			
			//start simulation as its own thread
			display = new Display(this, radius, number, temperature, mass, width, height, simspeed, zoom, mode, mintemp, maxtemp);
			display.start();
		}
		catch(Exception e) //the user has failed to enter numbers in the right format
		{
			JOptionPane.showInternalMessageDialog(null, "Bitte geben Sie gültige Zahlen ein! Komma bitte als Punkt schreiben.\n\nPlease enter valid numbers!\n\n"+e.getMessage());
			return;
		}
	}
}
