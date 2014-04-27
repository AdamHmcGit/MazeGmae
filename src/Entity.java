import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;


public abstract class Entity {
	public float x;
	public float y;
	public boolean shouldRender;
	Queue<LocationNode> route;
	Set<LocationNode> routeNodes;
	
	abstract public void render();
	abstract public void moveTo(float x, float y);
	
	public void setRoute(Queue<LocationNode> route){
		this.route = new ArrayDeque<LocationNode>(route);
		routeNodes = new HashSet<LocationNode>(route);
	}
}
