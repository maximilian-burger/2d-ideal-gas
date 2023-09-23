import java.awt.Color;

public class Particle
{
	public double radius, mass, x, y, vx, vy, xh, yh, th, width, height; 
	public int version; //is being incremented with every collision, prevents execution of events that where prevented earlier by other event
	public Color color;
	
	Display parent;
	
	public boolean trail = false; //does this particle have a trail
	
	Particle(double pradius, double pmass, double px, double py, double pvx, double pvy, double pwidth, double pheight, Color pcolor, Display pparent)
	{
		//receive varibales
		radius = pradius;
		mass = pmass;
		x = px;
		y = py;
		vx = pvx;
		vy = pvy;
		color = pcolor;
		parent = pparent;
		version = 0;
		width = pwidth;
		height = pheight;
		
		backtrack(parent.time); //find bounce location in a hypothetical past
	}
	public void backtrack(double time) //Where did the particle hit a wall the last time, calculate xh, yh and th
	{
		//calculate time needed to bounce off from all walls, two negative and two positive results expected
		double ttop = -y/vy;
		double tbottom = (height-y)/vy;
		double tleft = -x/vx;
		double tright = (width-x)/vx;
		
		//sort out smallest time by multiplying it with -1, negative time becomes positive
		//the new smallest time is the most recent bounce time with a wall
		double smallest = Math.min(Math.min(ttop, tbottom),Math.min(tleft, tright));
		if(smallest==ttop || Double.isInfinite(-ttop))
		{
			ttop*=-1;
		}
		else if(smallest==tbottom  || Double.isInfinite(-tbottom))
		{
			tbottom*=-1;
		}
		else if(smallest==tleft || Double.isInfinite(-tleft))
		{
			tleft*=-1;
		}
		else if(smallest==tright || Double.isInfinite(-tright))
		{
			tright*=-1;
		}
		
		//look for the new smallest time -> the most recent bounce time
		double secondsmallest = Math.min(Math.min(ttop, tbottom),Math.min(tleft, tright));
		if(secondsmallest==ttop)
		{
			th=ttop;
		}
		else if(secondsmallest==tbottom)
		{
			th=tbottom;
		}
		else if(secondsmallest==tleft)
		{
			th=tleft;
		}
		else if (secondsmallest==tright)
		{
			th=tright;
		}
		
		//calculate bounce coordinates, x-hit, y-hit and t-hit
		xh = x + vx*th;
		yh = y + vy*th;
		th+=time;
		return;
	}
	public void collide() //calculat future collisions with other particles
	{
		Particle a = this; 
		for(Particle bOG : parent.particles)
		{
			//copy bOG Data into b so bOG won't be altered in the following process
			Particle b = new Particle(bOG.radius, bOG.mass, bOG.x, bOG.y, bOG.vx, bOG.vy, width, height, bOG.color, parent); //copy particle into temporary particle
			
			//b.xh and b.yh are calculated so a.th = b.th without altering b's motion
			double deltaT = a.th-b.th;
			b.xh += b.vx*deltaT;
			b.yh += b.vy*deltaT;
			b.th = a.th;
			
			double dk = a.radius+b.radius; //calculating the distance of a and b at which a collision occurs
			
			//calculate the time of the collision by solving a big quadratic equation
			double tc =
					(-(2*(b.vy-a.vy)*(b.yh-a.yh)+2*(b.vx-a.vx)*(b.xh-a.xh))  //(-b
					-Math.sqrt(Math.pow(2*(b.vy-a.vy)*(b.yh-a.yh)+2*(b.vx-a.vx)*(b.xh-a.xh), 2) //SQRT(b^2
					-4*((b.vy-a.vy)*(b.vy-a.vy)+(b.vx-a.vx)*(b.vx-a.vx))*(-dk*dk+b.yh*b.yh-2*b.yh*a.yh+a.yh*a.yh+b.xh*b.xh-2*b.xh*a.xh+a.xh*a.xh))) //-4*a*c))
					/ //---------------
					(2*(b.vy-a.vy)*(b.vy-a.vy)+2*(b.vx-a.vx)*(b.vx-a.vx))+a.th; //2*a
			
			if(!Double.isNaN(tc) && tc>parent.time) //if equation returns actual value -> collision will happen AND collision was not in the past
			{
				Event collision = new Event(tc,true,a,a.version,bOG,bOG.version,4,parent); //create new event
				parent.addevent(collision); //add event to the event chain
			}
		}
	}
	public void bounce() //calculate future bounce with the wall
	{
		//calculate bounce time with every wall
		double ttop = th-(yh-radius)/vy;
		double tbottom = th+(height-yh-radius)/vy;
		double tleft = th-(xh-radius)/vx;
		double tright = th+(width-xh-radius)/vx;
		
		
		//sort out largest time by multiplying with -1, positive time becomes negative
		//the new smallest time is the most recent bounce with a wall
		double largest = Math.max(Math.max(ttop, tbottom),Math.max(tleft, tright));
		if(largest==ttop || Double.isInfinite(ttop))
		{
			ttop*=-1;
		}
		else if(largest==tbottom  || Double.isInfinite(tbottom))
		{
			tbottom*=-1;
		}
		else if(largest==tleft || Double.isInfinite(tleft))
		{
			tleft*=-1;
		}
		else
		{
			tright*=-1;
		}
		
		//look for the new largest time -> the next upcoming bounce
		
		//TimeBounce
		double tb;
		int wall;
		double secondlargest = Math.max(Math.max(ttop, tbottom),Math.max(tleft, tright));
		if(secondlargest==ttop)
		{
			tb=ttop;
			wall = 0;
		}
		else if(secondlargest==tbottom)
		{
			tb=tbottom;
			wall = 2;
		}
		else if(secondlargest==tleft)
		{
			tb=tleft;
			wall = 1;
		}
		else
		{
			tb=tright;
			wall = 3;
		}
		
		//create bounce event
		Particle foo3 = null;
		Event bounce = new Event(tb,false,this,this.version,foo3,0,wall,parent);
		parent.addevent(bounce);
	}
	public void updateCoordinates(double time) //update current position, needed for the rendering
	{
		x = xh-vx*(th-time); //calculating coords of the particle
    	y = yh-vy*(th-time);
    	return;
	}
	public Color updateColor() //update color according to speed
	{
		return parent.calculateColor(getSpeed(),mass);
	}
	public double getSpeed()
	{
		return Math.sqrt(vx*vx+vy*vy); //using the pythagorean theorem
	}
}
