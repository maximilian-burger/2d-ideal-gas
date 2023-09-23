import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.*;
public class Display extends Thread
{
	//PARAMETERS
	Panel parent;
	double radius; //[nm]
	int number; //[nm]
	double temperature; //K
	double mass; //u
	double width; //nm
	double height; //nm
	double simspeed; //ns/s
	double zoom; //px/nm
	int mode;
	double maxtemp; //K
	double avgtemp;
	double mintemp;
	
	//FRAME AND GRAPHICS
	JFrame frame;
	Graphics graphics;
    Graphics2D g2d;
    BufferedImage bi;
    BufferStrategy bufferstrgy;
	
    //RUNNING VARIABLES
	public boolean running = true;
	
	public double fps = 60; //repaint rate
	long deltaT; //time between frames in nanoseconds
	public double time = 0; //in simulation time elapsed since the start of the simulation
	int eventcounter = 0; //needed to display the events scheduled in the simulation window
	
	double wallmomentum = 0; //momentum the wall has received from the bounces
	double walllength; //circumference of the wall
	
	int chamberX = 30; //box position on frame
	int chamberY = 50; 
	
	int chamberwidthpx; //width and height of the chamber in pixels
	int chamberheightpx;
	int diameterpx; //diameter of the particles in pixels
	
	ArrayList<Particle> particles = new ArrayList<Particle>(); //ArrayList that saves all particles
	ArrayList<PastCollisions> pc = new ArrayList<PastCollisions>(); //ArrayList stores all collision of the trailing particle
	Distribution distribution; //the distribution window, only used for STANDARD MODE
	Event root;
	
	Particle trailer; //the one particle with a trailer, for DIFFUSION 2 and BROWNIAN MOTION
	
	double maxspeed; //speed of red particles
	double avgspeed; //speed of the average particle
	double minspeed; //speed of blue particles
	
	int[] distributionarray; //this array counts the particles in each speed category, this array is going to be transmitted to the distribution window
	double step; //width of the speed category
	
	Display(Panel parent, double pradius, int pnumber, double ptemperature, double pmass, double pwidth, double pheight, double psimspeed, double pzoom, int pmode, double pmintemp, double pmaxtemp) //contructor, is called when simulation launches
	{
		//EVENT TREE ANCHOR POINTS
		Particle foo1 = null;
		Particle foo2 = null;
		root = new Event(9999999, false, foo1, 0, foo2, 0, 4,this);
		
		//RECEIVING PARAMETERS
		radius = pradius;
		number = pnumber;
		temperature = ptemperature;
		mass = pmass;
		width = pwidth;
		height = pheight;
		simspeed = psimspeed;
		zoom = pzoom;
		mode = pmode;
		mintemp = pmintemp;
		maxtemp = pmaxtemp;
		avgtemp = temperature;
		
		//CREATING FRAME
		frame = new JFrame("2D ideales Gas Simulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(360,0);
		frame.setSize(1920-790,800);
		frame.setResizable(false);
		
		//SETTING UP GRAPHICS
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
		deltaT = (long)(1.0/fps*1000000000);
		
		chamberwidthpx = (int)(width*zoom);
		chamberheightpx = (int)(height*zoom);
		walllength = width*2.0+height*2.0; //chamber circumference is needed to calculate the pressure afterwards
		
		maxspeed = tempToV(maxtemp);
		avgspeed = tempToV(avgtemp);
		minspeed = tempToV(mintemp);
		
		createParticles(radius, number, temperature); //spawn particles and save them in the arraylist
		updateAllParticles(); //calculate all events and queue them in even tree
		
		if(mode==0) //calculate speed category width, create frame to display distribution
		{
			step = maxspeed/40.0;
			distribution = new Distribution(this, this.parent);
		}
	}
	private void createParticles(double radius, int number, double temperature) //create particles
	{
		//SCENARIOS
		if(mode==0 || mode == 4) //STANDARD
		{
			for(int i = 0;i<number;i++) //create random particles
			{
				double direction = Math.random()*2.0*Math.PI;
				double vx = Math.cos(direction)*avgspeed;
				double vy = Math.sin(direction)*avgspeed;
				double x = Math.random()*(width-radius*3.0)+radius*1.5;
				double y = Math.random()*(height-radius*3.0)+radius*1.5;
				Color color = new Color(0,0,255);
				
				particles.add(new Particle(radius,mass,x,y,vx,vy,width,height, color, this));
			}
		}
		else if(mode==1) //TWO DIFFERENT COLORS
		{
			int i = 0;
			for(i = 0;i<number/2;i++) //create random particles, group 1, left half
			{
				double direction = Math.random()*2.0*Math.PI;
				double vx = Math.cos(direction)*avgspeed;
				double vy = Math.sin(direction)*avgspeed;
				double x = Math.random()*(width/2.0-radius*3.0)+radius*1.5;
				double y = Math.random()*(height-radius*3.0)+radius*1.5;
				Color color = new Color(255,0,255);
				
				particles.add(new Particle(radius,mass,x,y,vx,vy,width,height, color, this));
			}
			for(;i<number;i++) //create random particles, group 2, right half
			{
				double direction = Math.random()*2.0*Math.PI;
				double speed = Math.sqrt(3.0*temperature/mass*8314.463);
				double vx = Math.cos(direction)*speed;
				double vy = Math.sin(direction)*speed;
				double x = Math.random()*(width/2.0-radius*3.0)+width/2+radius*1.5;
				double y = Math.random()*(height-radius*3.0)+radius*1.5;
				Color color = new Color(0,204,0);
				
				particles.add(new Particle(radius,mass,x,y,vx,vy,width,height, color, this));
			}
		}
		else if(mode==2) //ONE TRAILER
		{
			for(int i = 0;i<number;i++) //create random particles
			{
				double direction = Math.random()*2.0*Math.PI;
				double speed = Math.sqrt(3.0*temperature/mass*8314.463);
				double vx = Math.cos(direction)*speed;
				double vy = Math.sin(direction)*speed;
				double x = Math.random()*(width-radius*3.0)+radius*1.5;
				double y = Math.random()*(height-radius*3.0)+radius*1.5;
				Color color = new Color(0,0,255);
				
				Particle particle = new Particle(radius,mass,x,y,vx,vy,width,height, color, this);
				
				if(i == number-1) //last particle gets trailer
				{
					trailer = particle;
					particle.trail = true;
					particle.color = new Color(255,0,190);
				}
				
				particles.add(particle);
			}
		}
		else if(mode==3) //ONE BIG MOLECULE WITH TRAILER
		{
			for(int i = 0;i<number;i++) //create random particles
			{
				double direction = Math.random()*2.0*Math.PI;
				double speed = Math.sqrt(3*temperature/mass*8314.463);
				double vx = Math.cos(direction)*speed;
				double vy = Math.sin(direction)*speed;
				double x = Math.random()*(width-radius*3.0)+radius*1.5;
				double y = Math.random()*(height-radius*3.0)+radius*1.5;
				Color color = new Color(0,0,255);
				
				Particle particle = new Particle(radius,mass,x,y,vx,vy,width,height, color, this);
				
				if(i == number-1) //that is going to be the one with a trailer
				{
					trailer = particle;
					particle.trail = true;
					particle.color = new Color(0,255,255);
					particle.mass = mass*100;
					particle.radius = radius*10;
					particle.x = width/2.0;
					particle.y = height/2.0;
					particle.vx = 0.001;
					particle.vy = 0.001;
					particle.backtrack(time);
				}
				
				particles.add(particle);
			}
		}
		else if(mode==5) //SMALL HOT GROUP
		{
			int i = 0;
			for(i = 0;i<number/30;i++) //create random particles group 1, HOT
			{
				double direction = Math.random()*2.0*Math.PI;
				double speed = Math.sqrt(3.0*temperature*30.0/mass*8314.463); //1/30th of the particles get all the energy
				double vx = Math.cos(direction)*speed;
				double vy = Math.sin(direction)*speed;
				double x = Math.random()*(width/10-radius*3)+radius*1.5;
				double y = Math.random()*(height-radius*3)+radius*1.5;
				Color color = new Color(255,0,0);
				
				particles.add(new Particle(radius,mass,x,y,vx,vy,width,height, color, this));
			}
			for(;i<number;i++) //create random particles group 2, VERY COLD
			{
				double direction = Math.random()*2*Math.PI;
				double speed = Math.sqrt(3.0*temperature/1000.0/mass*8314.463); //29/30th get almost no energy
				double vx = Math.cos(direction)*speed;
				double vy = Math.sin(direction)*speed;
				double x = Math.random()*(width*0.9-radius*3)+width/10+radius*1.5;
				double y = Math.random()*(height-radius*3)+radius*1.5;
				Color color = new Color(0,0,255);
				
				particles.add(new Particle(radius,mass,x,y,vx,vy,width,height, color, this));
			}
		}
		else if(mode==6) //ONLY FILL ONE HALF
		{
			for(int i = 0;i<number;i++) //create random particles
			{
				double direction = Math.random()*2.0*Math.PI;
				double speed = Math.sqrt(3.0*temperature/mass*8314.463);
				double vx = Math.cos(direction)*speed;
				double vy = Math.sin(direction)*speed;
				double x = Math.random()*(width/2.0-radius*3.0)+radius*1.5;
				double y = Math.random()*(height-radius*3.0)+radius*1.5;
				Color color = new Color(0,0,255);
				
				particles.add(new Particle(radius,mass,x,y,vx,vy,width,height, color, this));
			}
		}
		return;
	}
	private void updateAllParticles()
	//after spawning the particles they have to check for the first time where they will bounce and collide,
	//after that they will check with every bounce and collision
	{
		for(Particle a : particles)
		{
			a.collide();
			a.bounce();
		}
		return;
	}
	public void addevent(Event event) //new event has to be saved in the event tree
	{
		Event pointer = root;
		eventcounter++;
		while(true) 
		{
			if(event.time<pointer.time) //descend into left sub-tree
			{
				if(pointer.prior!=null) //is the sub-tree not empty?
				{
					pointer = pointer.prior; //continue looking in the sub-tree
				}
				else
				{
					pointer.prior = event; //become member of the tree
					event.root = pointer;
					break; //done saving the event in the tree
				}
			}
			else //descend into right-sub tree
			{
				if(pointer.after!=null) //is the sub-tree not empty?
				{
					pointer = pointer.after; //continue looking in the sub-tree
				}
				else
				{
					pointer.after = event; //become member of the tree
					event.root = pointer;
					break; //done saving the event in the tree
				}
			}
		}
		return;
	}
	public void run() //this is where the new thread starts
	{
		long told = 0; 
		double deltaFrames = 1.0/fps*simspeed; //time increment in the simulation
		
		Event pointer; //needed to detect the event that has to be executed
		
		Font small = new Font("Calibri",0,15); //font for the stats
		Font big = new Font("Calibri",0,25); //font for the writing on the warm and cold block of HEAT FLOW MODE
		
		while(running)
		{
			time+=deltaFrames; //new time in the simualtion
			
			//EXECUTE ALL EVENTS DUE BEFORE THE NEXT FRAME
			while(true)
			{
				pointer = root;
				while(pointer.prior!=null) //find earliest event in tree
				{
					pointer = pointer.prior;
				}
				if(pointer.time<=time) //does the earliest event have to be executed
				{
					pointer.execute(); //execute the event
					eventcounter--;
					pointer.root.prior = pointer.after; //reconnect event's root to event's right children, could be null
					if(pointer.after!=null) //if event's right children not null, connect them back to event's root
					{
						pointer.after.root = pointer.root;
					}
				}
				else
				{
					break; //no events scheduled before event
				}
			}
			
			//WAIT UNTIL FRAME HAS TO BE PAINTED
			while(!(System.nanoTime()-told>deltaT))
			{
				
			}
			
			//PAINT FRAME
			double deltaTtrue = System.nanoTime()-told; //calculate true time between frames
			told = System.nanoTime();
			try
			{
                //refresh, create white background
                g2d = bi.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, frame.getWidth(),frame.getHeight());
                
                //draw chamber
                g2d.setColor(Color.BLACK);
                g2d.drawRect(chamberX, chamberY, chamberwidthpx, chamberheightpx);        
                
                if(mode == 4) //HEAT FLOW MODE, draw blue and red bar and write the temperatures on top of them
                {
	                g2d.setColor(Color.BLUE);      
	                g2d.fillRect(chamberX,chamberY-29,chamberwidthpx,30);
	                
	                g2d.setColor(Color.RED);      
	                g2d.fillRect(chamberX,chamberY+chamberheightpx,chamberwidthpx,30);
	                
	                g2d.setColor(Color.WHITE);
	                g2d.setFont(big);
	                g2d.drawString(mintemp+"K",chamberwidthpx/2+chamberX-50,45);
	                g2d.drawString(maxtemp+"K",chamberwidthpx/2+chamberX-50,chamberheightpx+chamberY+25);
                }
	                
                //render the particles
                if(mode==0) //for STANDARD MODE
                {
                	distributionarray = new int[80];
                	
	                for(Particle particle:particles)
	                {
	                	//draw particle
	                	particle.updateCoordinates(time);
	                	
	                	diameterpx = (int)(2*particle.radius*zoom);
	                	g2d.setColor(particle.color);
	                    g2d.fillOval((int)((particle.x-particle.radius)*zoom)+chamberX, (int)((particle.y-particle.radius)*zoom)+chamberY, diameterpx, diameterpx);
	                    
	                    int category = (int)Math.floor(particle.getSpeed()/step); //calculate speed category the particle belongs to
	                    try
	                    {
		                    distributionarray[category]++; //add one to bar height
	                    }
	                    catch(IndexOutOfBoundsException e)
	                    {
	                    	//speed is so high that it cannot be represented in a bar in the distribution window
	                    }
	                }
	                distribution.draw(distributionarray);
                }
                else //without distribution frame
                {
                	for(Particle particle:particles)
	                {
                		//draw particle
	                	particle.updateCoordinates(time);
	                	
	                	diameterpx = (int)(2*particle.radius*zoom);
	                	g2d.setColor(particle.color);
	                    g2d.fillOval((int)((particle.x-particle.radius)*zoom)+chamberX, (int)((particle.y-particle.radius)*zoom)+chamberY, diameterpx, diameterpx);
	                }
                }
                if((mode == 2 || mode==3) && pc.size()>0) //draw trail, DIFFUSION 2 AND BROWNIAN MODEL MODE
                {
                	int i = 0;
	                for(;i<pc.size()-1;i++) //connect all past collision with each other by a line
	                {
	                	g2d.setColor(new Color((int)(127*Math.sin(i/80.0))+127,(int)(127*Math.sin(i/60.0+1.5))+127,0)); //use slowly changing colors
	                	g2d.setStroke(new BasicStroke(4));
	                	g2d.drawLine((int)(pc.get(i).x*zoom+chamberX), (int)(pc.get(i).y*zoom+chamberY), (int)(pc.get(i+1).x*zoom+chamberX), (int)(pc.get(i+1).y*zoom+chamberY));
	                }
	                //connect the most recent past collision with the current position by a line
	                g2d.setColor(new Color((int)(127*Math.sin((i)/80.0))+127,(int)(127*Math.sin((i)/60.0+1.5))+127,0));
	                g2d.drawLine((int)(pc.get(i).x*zoom+chamberX), (int)(pc.get(i).y*zoom+chamberY), (int)((trailer.x)*zoom)+chamberX, (int)((trailer.y)*zoom)+chamberY);
                }
                
                //display statistics in the top
                double msperframe =  deltaTtrue/1000000.0;
                g2d.setColor(Color.BLACK);
                g2d.setFont(small);
                g2d.drawString(Math.round(time*1000000.0)/1000.0+" ps",10,12); //sim time
                g2d.drawString(Math.round(1/msperframe*10000.0)/10.0+" FPS",90,12); //fps
                g2d.drawString(eventcounter+" Events scheduled",170,12); //number of events planned
                g2d.drawString((int)Math.round(wallmomentum/(time*walllength)*1.660/1000000.0)+" mN/m",340,12); //"pressure"
                
                //paint everything
                graphics = bufferstrgy.getDrawGraphics();
                graphics.drawImage( bi, 0, 0, null);
                if(!bufferstrgy.contentsLost())
                {
                    bufferstrgy.show();
                }
            }
            finally //when done
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
			time+=deltaFrames; //update time of the simualtion
		}
		//WHEN RUNNING=FALSE (STOP BUTTON RPESSED)
		try
		{
			frame.dispose(); //get rid of the frame
			distribution.frame.dispose();
		}
		catch(NullPointerException e)
		{
			//if distribution frame does not exist
		}
	}
	public Color calculateColor(double speed, double pmass) //return color according to speed of the particle and its mass
	{
		Color calculatedcolor;
		double temp = speed*speed*pmass/24942.0; //calculate temperature according to speed
		if(temp>maxtemp)
		{
			calculatedcolor = new Color(255,0,0);
		}
		else if(temp<mintemp)
		{
			calculatedcolor = new Color(0,0,255);
		}
		else
		{
			calculatedcolor = new Color((int)((temp-mintemp)*255.0/(maxtemp-mintemp)),0,(int)((temp-maxtemp)*255.0/(mintemp-maxtemp)));
		}
		return calculatedcolor;
	}
	public double tempToV(double temp) //input: temperature in kelvin, output: speed
	{
		return Math.sqrt(temp/mass*24943.389);
	}
}
