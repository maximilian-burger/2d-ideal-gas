public class Event
{
	//VARIABLES
	public double time;
	boolean particle; //is this event referring to a collision or bounce
	Particle a;
	int aversion;
	Particle b; //might be null, if event is a bounce
	int bversion;
	int wall; //0,1,2,3,4 top,left, bottom, right, none
	Display parent;

	//binary tree variables
	Event root;
	Event prior;
	Event after;
	
	Event(double ptime, boolean pparticle, Particle pa, int paversion, Particle pb, int pbversion, int pwall, Display pparent) //constructor, receives all variables
	{
		time = ptime;
		particle = pparticle;
		a = pa;
		aversion = paversion;
		b = pb;
		bversion = pbversion;
		wall = pwall;
		parent = pparent;
	}
	public void execute() //time in the simulation has reached the scheduled time of this event
	{
		if(particle) //COLLISION
		{
			if(aversion == a.version && bversion == b.version) //both particles are still the same
			{
				//calcualte a and b's coordiantes
				a.updateCoordinates(time);
				b.updateCoordinates(time);
				
				//vector p points from a's position to b's position
				double px = b.x-a.x;
				double py = b.y-a.y;
				
				//Vector avp: describes the velcoity of a in the system of the impulse transmission (vector p)
				double avpx = (px/py*a.vx+a.vy)/(py/px+px/py); //a, speed, p system, x component 
				double avpy = py/px*(px/py*a.vx+a.vy)/(py/px+px/py); //a, speed, p system, y component
				
				double avqx = a.vx-avpx; //a, speed, orthogonal to p, x component
				double avqy = a.vy-avpy; //a, speed, orthogonal to p, y component
				
				
				//Vector bvp: describes the velcoity of b in the system of the impulse transmission (vector p)
				double bvpx = (px/py*b.vx+b.vy)/(py/px+px/py); //b, speed, p system, x component 
				double bvpy = py/px*(px/py*b.vx+b.vy)/(py/px+px/py); //b, speed, p system, y component
				
				double bvqx = b.vx-bvpx; //b, speed, orthogonal to p, x component
				double bvqy = b.vy-bvpy; //b, speed, orthogonal to p, y component
				
				
				double avpxnew,avpynew,bvpxnew,bvpynew; //new speed vectors' components
				
				if(a.mass == b.mass) //easy collision
				{
					double tmpx = avpx; //store avp's info in a temporary field
					double tmpy = avpy;
					
					avpxnew = bvpx; //swap a and b's impuls in the delta p (px, py) direction
					avpynew = bvpy;
					
					bvpxnew = tmpx;
					bvpynew = tmpy;
				}
				else //more complex collision, takes different masses into account
				{
					avpxnew = (a.mass-b.mass)/(a.mass+b.mass)*avpx+2*b.mass/(a.mass+b.mass)*bvpx;
					avpynew = (a.mass-b.mass)/(a.mass+b.mass)*avpy+2*b.mass/(a.mass+b.mass)*bvpy;
					
					bvpxnew = 2*a.mass/(a.mass+b.mass)*avpx+(b.mass-a.mass)/(a.mass+b.mass)*bvpx;
					bvpynew = 2*a.mass/(a.mass+b.mass)*avpy+(b.mass-a.mass)/(a.mass+b.mass)*bvpy;
				}
				
				a.vx = avqx + avpxnew; //assemble x components of speed vectors
				b.vx = bvqx + bvpxnew;
				
				a.vy = avpynew + avqy; //assemble y components of speed vectors
				b.vy = bvpynew + bvqy;
				
				a.version++; //iterate particle versions
				b.version++;
				
				a.backtrack(time); //calculate new hit location (in a hypothetical past)
				b.backtrack(time);
				
				a.bounce(); //check when they will bounce (in case they dont collide)
				b.bounce();
				
				a.collide(); //check whether they will collide
				b.collide();
				
				if(parent.mode==0 || parent.mode == 4 || parent.mode==5 || parent.mode==6) //particles get color according to speed
				{
					a.color = a.updateColor();
					b.color = b.updateColor();
				}
				
				if(a.trail) //trailing particles save collision in pastcollisions array
				{
					PastCollisions pcm = new PastCollisions();
					a.updateCoordinates(time);
					pcm.x = a.x;
					pcm.y = a.y;
					parent.pc.add(pcm);
				}
				else if(b.trail)
				{
					PastCollisions pcm = new PastCollisions();
					b.updateCoordinates(time);
					pcm.x = b.x;
					pcm.y = b.y;
					parent.pc.add(pcm);
				}
			}
			return;
		}
		else //BOUNCE
		{
			if(aversion == a.version) //particle's version is still the same
			{
				a.updateCoordinates(time);
				if(parent.mode==4) //HEAT FLOW MODE -> more complex interactions with walls
				{
					if(wall == 0) //top, set particles temperature to the one of the wall
					{
						a.vy = -a.vy;
						
						double vvectorlength = a.getSpeed();
						a.vx = a.vx/vvectorlength*parent.minspeed;
						a.vy = a.vy/vvectorlength*parent.minspeed;
						a.vx += 0.00001; //in case user enters 0K, particles get buggy at 0K
						
						parent.wallmomentum+=Math.abs(2.0*a.vy*a.mass); //keeping track of the momentum the wall receives
					}
					else if (wall == 2) //bottom,  set particles temperature to the one of the wall
					{
						a.vy = -a.vy;
						
						double vvectorlength = a.getSpeed();
						a.vx = a.vx/vvectorlength*parent.maxspeed;
						a.vy = a.vy/vvectorlength*parent.maxspeed;
						
						parent.wallmomentum+=Math.abs(2.0*a.vy*a.mass);
					}
					else if(wall == 1 || wall == 3) //left or right -> mirror horizontal speed
					{
						a.vx = -a.vx;
						parent.wallmomentum+=Math.abs(2.0*a.vx*a.mass); //keeping track of the momentum the wall receives
					}
				}
				else //normal, mirror horizontal or vertical speed
				{
					if(wall == 0 || wall == 2) //top
					{
						a.vy = -a.vy;
						parent.wallmomentum+=Math.abs(2.0*a.vy*a.mass); //keeping track of the momentum the wall receives
					}
					else if(wall == 1 || wall == 3) //left
					{
						a.vx = -a.vx;
						parent.wallmomentum+=Math.abs(2.0*a.vx*a.mass); //keeping track of the momentum the wall receives
					}
				}
				if(parent.mode == 4) //in STANDARD MODE, color does not change after bounce because the speed stays constant
				{
					a.color = a.updateColor();
				}
					
				a.backtrack(time); //set hit location to this bounce's coordinates
				
				a.version++; //iterate particle version

				a.collide(); //check whether it collides
				
				a.bounce(); //check when it bounces
				
				if(a.trail) //trailing particles save collision and bounces in pastcollisions array
				{
					PastCollisions pcm = new PastCollisions();
					a.updateCoordinates(time);
					pcm.x = a.x;
					pcm.y = a.y;
					parent.pc.add(pcm);
				}
			}
		}
	}
}
