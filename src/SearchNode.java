import java.util.LinkedList;
import java.util.List;


public class SearchNode {
	int g_moveCost=0;
	int h_heuristicValue=0;
	int f_totalCost=0;
	
	LocationNode locationNode;
	SearchNode parentSearchNode;
	
	List<SearchNode> neighborNodes;
	
	public SearchNode(int g, int h){
		this.g_moveCost=g;
		this.h_heuristicValue=h;
		neighborNodes = new LinkedList<SearchNode>();
	}
	
	public String toString(){
		return "["+locationNode.indexX+", "+locationNode.indexY+"] G:"+g_moveCost+" H:"+h_heuristicValue;
	}
	
	public int getF_totalCost(){
		return this.g_moveCost+this.h_heuristicValue;
	}
	
	public void setNeighbor(SearchNode node){
		neighborNodes.add(node);
	}
	
}
