import java.awt.Color;


public class LocationNode {
	float centerX;
	float centerY;
	int indexX;
	int indexY;
	boolean isWalkable;
	boolean isOccupy;
	public LocationNode[] neighborNodes;
	
	public Color color;
	
	public LocationNode(){
		this.isWalkable=true;
	}
	
	public void setColor(float r, float g, float b, float alpha){
		this.color = new Color(r,g,b,alpha);
	}
	
	public String toString(){
		return "["+indexX+", "+indexY+"]";
	}
	
	public void setIndex(int x, int y){
		this.indexX=x;
		this.indexY=y;
	}
}
