import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

public class Distribution
{
	Display parent;
	Panel parentparent;
	
	//FRAME AND GRAPHICS (exactly the same as in Display.java)
	JFrame frame;
	Graphics graphics;
    Graphics2D g2d;
    BufferedImage bi;
    BufferStrategy bufferstrgy;
    
    double ystretch; //height of one particle in a bar
    
    //x-coordinates of temperature and speed marks in the diagram
    int mincoords; 
    int avgcoords;
    int maxcoords;
    
    //color of the temperature and speed marks
    Color mincolor;
    Color avgcolor;
    Color maxcolor;
	
	Distribution(Display pparent, Panel pparentparent) //constructor
	{
		//receive parent references
		parent = pparent;
		parentparent = pparentparent;
		
		//JFRAME
		frame = new JFrame("Geschwindigkeitsverteilung");
		frame.setSize(370,375);
		frame.setLocation(0,425);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		//SETTING UP GRAPHICS (exactly the same as in Display.java)
		Canvas canvas = new Canvas();
        canvas.setIgnoreRepaint(true);
        canvas.setSize(frame.getWidth(),frame.getHeight());
        frame.add(canvas);
        
        frame.setVisible(true);
        canvas.createBufferStrategy(2);
        bufferstrgy = canvas.getBufferStrategy();
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        bi = gc.createCompatibleImage(frame.getWidth(),frame.getHeight());
        
        graphics = null;
        g2d = null;
        
        //CALCULATING VARIABLES USED WHILE RUNNING
        ystretch = 2500.0/(double)parent.number;
        
        mincoords = (int)(parent.minspeed/parent.step*4)+19;
        avgcoords = (int)(parent.avgspeed/parent.step*4)+19;
        maxcoords = (int)(parent.maxspeed/parent.step*4)+19;
        
        mincolor = parent.calculateColor(parent.minspeed,parent.mass);
        avgcolor = parent.calculateColor(parent.avgspeed,parent.mass);
        maxcolor = parent.calculateColor(parent.maxspeed,parent.mass);
	}
	public void draw(int[] distribution) //is being called with every new frame in Display
	{
		try
		{
            //REFRESH, WHITE BACKGROUND
            g2d = bi.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0,0,frame.getWidth(),frame.getHeight());
         
            //DRAW TEMPERATURE AND SPEED MARKS
            g2d.setColor(Color.BLACK);
            
            g2d.drawLine(20, 320, 340, 320); //X-AXIS
            g2d.fillPolygon(new int[] {340,340,350},new int[] {315,325,320},3);
            g2d.drawString("v", 320, 335);
            
            g2d.drawLine(20, 20, 20, 320); //Y-AXIS
            g2d.fillPolygon(new int[] {15,20,25},new int[] {20,10,20},3);
            g2d.drawString("p(v)", 28, 20);
            
            g2d.setColor(mincolor);
            g2d.drawString("| "+(int)parent.minspeed+" m/s",mincoords, 35);
            g2d.drawString("| "+(int)parent.mintemp+" K",mincoords, 80);
            g2d.setColor(avgcolor);
            g2d.drawString("| "+(int)parent.avgspeed+" m/s",avgcoords, 50);
            g2d.drawString("| "+(int)parent.avgtemp+" K",avgcoords, 95);
            g2d.setColor(maxcolor);
            g2d.drawString("| "+(int)parent.maxspeed+" m/s", maxcoords, 65);
            g2d.drawString("| "+(int)parent.maxtemp+" K",maxcoords, 110);
            
            //DRAW BARS
            for(int i = 0;i<80;i++)
            {
            	g2d.setColor(parent.calculateColor((double)i/40.0*parent.maxspeed,parent.mass));
            	g2d.fillRect(21+i*4,320-(int)(distribution[i]*ystretch),4,(int)(distribution[i]*ystretch));
            }
            
            //DRAW EVERYTHING
            graphics = bufferstrgy.getDrawGraphics();
            graphics.drawImage( bi, 0, 0, null);
            if(!bufferstrgy.contentsLost())
            {
                bufferstrgy.show();
            }
        }
        finally //dispose when done
        {
            if( graphics != null ) 
            {
                graphics.dispose();
            }
            if( g2d != null ) 
            {
                g2d.dispose();
            }
        }
	}
}
