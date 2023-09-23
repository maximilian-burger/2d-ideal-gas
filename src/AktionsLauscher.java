import java.awt.event.*;
public class AktionsLauscher implements ActionListener
{
	Panel parent;
	AktionsLauscher(Panel pparent) //Constructor, called when added to UI elements
	{
		parent = pparent; //get connected to control panel
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(parent.button1)) //start button was pressed
		{
			parent.start();
			parent.button1.setEnabled(false);
			parent.button2.setEnabled(true);
		}
		else if(e.getSource().equals(parent.button2)) //stop button was pressed
		{
			parent.display.running = false;
			parent.button1.setEnabled(true);
			parent.button2.setEnabled(false);
		}
		else if(e.getSource().equals(parent.button3)) //stop button was pressed
		{
			if(parent.button3.getText().equals("EN")) //currently set to german
			{
				parent.button1.setText("start");
				parent.button2.setText("stop");
				parent.button3.setText("DE");
				
				parent.frame.setTitle("2D ideal gas simulator");
				try
				{
					parent.display.frame.setTitle("2D ideal gas simulator");
					parent.display.distribution.frame.setTitle("speed distribution");
				}
				catch(NullPointerException q)
				{
					
				}
				
				parent.label1.setText("particle radius [pm]");
				parent.label2.setText("particle count []");
				parent.label3.setText("temperature [K]");
				parent.label4.setText("particle mass [u]");
				parent.label5.setText("chamber width [nm]");
				parent.label6.setText("chamber height [nm]");
				parent.label7.setText("simulation speed [ps/s]");
				parent.label8.setText("zoom [px/nm]");
				parent.label9.setText("scenario");
				parent.label10.setText("color scale [K]");
				
				parent.settings.removeAllItems();
				String[] modes = {"standard","diffusion 1","diffusion 2","Brownian motion","thermal conduction","entropy","half vacuum"};
				for(int i =0;i<modes.length;i++)
				{
					parent.settings.addItem(modes[i]);
				}
			}
			else //currently set to english
			{
				parent.button1.setText("Start");
				parent.button2.setText("Stop");
				parent.button3.setText("EN");
				
				parent.frame.setTitle("2D ideales Gas Simulator");
				try
				{
					parent.display.frame.setTitle("2D ideales Gas Simulator");
					parent.display.distribution.frame.setTitle("Geschwindigkeitsverteilung");
				}
				catch(NullPointerException q)
				{
					
				}
				
				parent.label1.setText("Teilchenradius [pm]");
				parent.label2.setText("Teilchenanzahl []");
				parent.label3.setText("Temperatur [K]");
				parent.label4.setText("Teilchenmasse [u]");
				parent.label5.setText("Kammerbreite [nm]");
				parent.label6.setText("Kammerhöhe [nm]");
				parent.label7.setText("Simulationsgeschwindigkeit [ps/s]");
				parent.label8.setText("Zoom [px/nm]");
				parent.label9.setText("Szenario");
				parent.label10.setText("Farbskala [K]");
				
				parent.settings.removeAllItems();
				String[] modes = {"Standard","Diffusion 1","Diffusion 2","Brown'sche Molekularbewegung","Wärmeleitung","Entropie","Halbes Vakuum"};
				for(int i =0;i<modes.length;i++)
				{
					parent.settings.addItem(modes[i]);
				}
			}
		}
		else
		{
			//not possible
		}
	}
}
