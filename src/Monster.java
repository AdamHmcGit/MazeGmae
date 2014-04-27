import java.util.Queue;
import org.lwjgl.opengl.GL11;


public class Monster extends Entity{

	int speed=1;
	boolean isReadyForNextMove=true;
	LocationNode currentTarget;
	PathFinder pf;
	
	public Monster(float x, float y, Queue<LocationNode> newRoute) {
		this.x=x;
		this.y=y;
		shouldRender = true;
		setRoute(newRoute);
		pf = new PathFinder(DisplayExample.mapNodes);
	}

	@Override
	public void render() {
		if(isReadyForNextMove)
			currentTarget = this.route.poll();
		if(currentTarget!=null){
			isReadyForNextMove=false;
			moveTo(currentTarget.centerX, currentTarget.centerY);	
		}
		else{
			shouldRender=false;
		}
		if(shouldRender){
			LocationNode tempNode = getCurrentLocationNode();
//			GL11.glColor4f(0.8f, 1.0f, 1.0f, 0.5f);
//			GL11.glBegin(GL11.GL_QUADS);
//			GL11.glVertex2f(tempNode.centerX-DisplayExample.defaultCellWidth/2, tempNode.centerY-DisplayExample.defaultCellWidth/2);
//			GL11.glVertex2f(tempNode.centerX+DisplayExample.defaultCellWidth/2 ,tempNode.centerY-DisplayExample.defaultCellWidth/2);
//			GL11.glVertex2f(tempNode.centerX+DisplayExample.defaultCellWidth/2, tempNode.centerY+DisplayExample.defaultCellWidth/2);
//			GL11.glVertex2f(tempNode.centerX-DisplayExample.defaultCellWidth/2 , tempNode.centerY+DisplayExample.defaultCellWidth/2);
//			GL11.glEnd();
			GL11.glColor3f(1f, 1f, 0f);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(x - 3, y - 3);
			GL11.glVertex2f(x + 3, y - 3);
			GL11.glVertex2f(x + 3, y + 3);
			GL11.glVertex2f(x - 3, y + 3);
			GL11.glEnd();
		}
	}

	@Override
	public void moveTo(float targetX, float targetY) {
		if(Math.abs(targetX-x)<speed){
			x=targetX;
		}
		else{
			if(targetX>x){
				x+=speed;
			}
			else
				x-=speed;
		}
		if(Math.abs(targetY-y)<speed){
			y=targetY;
		}
		else{
			if(targetY>y){
				y+=speed;
			}
			else
				y-=speed;
		}
		getCurrentLocationNode().isOccupy=true;
		if(x==targetX&&y==targetY){
			isReadyForNextMove=true;
		}
	}
	
	public void updateRoute(LocationNode newNode){
		if(routeNodes.contains(newNode)){
			route.clear();
			route.addAll(pf.getShortestPath(getCurrentLocationNode(), DisplayExample.destinationNode));
			routeNodes.clear();
			routeNodes.addAll(route);
			isReadyForNextMove=true;
		}
	}
	
	public LocationNode getCurrentLocationNode(){
		int indexX = (int) (this.x/DisplayExample.defaultCellWidth);
		int indexY =(int) (this.y/DisplayExample.defaultCellHeight);
		return DisplayExample.mapNodes[indexY][indexX];
	}
	
}
