import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class PathFinder {
	
	private LocationNode [][] mapNodes;
	private SearchNode [][] mapSearchNodes;

	boolean isNeedToUpdateRoute;
	
	boolean foundTarget;
	
	Queue<SearchNode> openList;
	List<SearchNode> closedList;
		
	public PathFinder(LocationNode [][] mapNodes){
		this.mapNodes = mapNodes;
		openList = new PriorityQueue<SearchNode>(20, new SearchNodeComparator());
		closedList = new LinkedList<SearchNode>();
		mapSearchNodes = new SearchNode [mapNodes.length][mapNodes[0].length];
	}
	
	
	public class SearchNodeComparator implements Comparator<SearchNode>{

		@Override
		public int compare(SearchNode o1, SearchNode o2) {
			if(o1.getF_totalCost()>o2.getF_totalCost())
				return 1;
			if(o1.getF_totalCost()<o2.getF_totalCost())
				return -1;
			return 0;
		}
		
	} 
	
	public void initSearchMap(LocationNode endNode){
		for(int i=0;i<mapNodes.length;i++){
			for(int j=0;j<mapNodes[i].length;j++){
				LocationNode currentNode = mapNodes[i][j];
				SearchNode newSearchNode;
				if(currentNode.isWalkable){
					newSearchNode = new SearchNode(0,getDistance(endNode, mapNodes[i][j]));
					newSearchNode.locationNode=mapNodes[i][j];
				}
				else
					newSearchNode = null;
				mapSearchNodes[i][j] = newSearchNode;
			}
		}
		for(int i=0;i<mapNodes.length;i++){
			for(int j=0;j<mapNodes[i].length;j++){
				if(mapNodes[i][j].isWalkable){
					if(mapSearchNodes[i+1][j]!=null)
						mapSearchNodes[i][j].neighborNodes.add(mapSearchNodes[i+1][j]);
					if(mapSearchNodes[i][j+1]!=null)
						mapSearchNodes[i][j].neighborNodes.add(mapSearchNodes[i][j+1]);
					if(mapSearchNodes[i-1][j]!=null)
						mapSearchNodes[i][j].neighborNodes.add(mapSearchNodes[i-1][j]);
					if(mapSearchNodes[i][j-1]!=null)
						mapSearchNodes[i][j].neighborNodes.add(mapSearchNodes[i][j-1]);
				}
			}
		}
	}
	
	public List<LocationNode> getShortestPath(LocationNode startNode, LocationNode endNode){
		List<LocationNode> route = new LinkedList<LocationNode>();
		initSearchMap(endNode);
		System.out.println(startNode + " "+ endNode);
		SearchNode startSearchNode = mapSearchNodes[startNode.indexX][startNode.indexY];
		SearchNode endSearchNode = mapSearchNodes[endNode.indexX][endNode.indexY];
		System.out.println(startSearchNode+" "+endSearchNode);
		findPath(startSearchNode,endSearchNode);
		if(endSearchNode.parentSearchNode!=null){
			route.add(0,endSearchNode.locationNode);
			SearchNode tempNode = endSearchNode.parentSearchNode;
			while(tempNode!=null){
				route.add(0,tempNode.locationNode);
				tempNode=tempNode.parentSearchNode;
			}
		}
		route.remove(startNode);
		return route;
	}
	
	public void findPath(SearchNode startSearchNode, SearchNode endSearchNode){
		if(startSearchNode.equals(endSearchNode)){
			foundTarget=true;
			return;
		}
		else{
			closedList.add(startSearchNode);
			List<SearchNode> neighborSearchNodes =startSearchNode.neighborNodes;
			for(SearchNode tempSearchNode : neighborSearchNodes){
				if(closedList.contains(tempSearchNode))
					continue;
				if(openList.contains(tempSearchNode)){
					int newG_Cost = startSearchNode.g_moveCost+1;
					if(newG_Cost<tempSearchNode.g_moveCost){
						tempSearchNode.parentSearchNode = startSearchNode;
						tempSearchNode.g_moveCost = newG_Cost;
						openList.remove(tempSearchNode);
						openList.add(tempSearchNode);
					}
				}
				else{
					tempSearchNode.g_moveCost = startSearchNode.g_moveCost+1;
					tempSearchNode.parentSearchNode = startSearchNode;
					openList.add(tempSearchNode);
				}
			}
			SearchNode nextStarNode = openList.poll();
			if(nextStarNode!=null){
				findPath(nextStarNode,endSearchNode);
			}
			else{
				return;
			}
		}
	}	
	
	public int getDistance(LocationNode endNode, LocationNode currentNode){
		return Math.abs(currentNode.indexX-endNode.indexX)+Math.abs(currentNode.indexY-endNode.indexY);
	}

}
