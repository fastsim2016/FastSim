package fastSim.data;

import java.util.ArrayList;
import java.util.List;

/**
 * node structure.
 * 
 * @author zfw
 * 
 */
public class Node {

	public int id;
	public boolean isHub = false;
	public boolean isVisited = false;
	public int index = -1;
	//public boolean isRead = false;
	//public boolean isDangling = false;
	//public int clusterId = -1;
	
	public List<Node> out;
	public List<Node> in;
	
	//public List<Node> inInSub;
	
	public List<Integer> outId; 
	public List<Integer> inId;
	
	public double outEdgeWeight = 0;
	public double outEdgeWeightInSub = 0;
	public double vOld = 0;
	public double vNew = 0;
	
//	public List<Integer> meetingDepth;
	
	public Node(int id) {
		this.id = id;
		out = new ArrayList<Node>();
		in = new ArrayList<Node>();
	}
	public Node(int id,int index) {
		this.id = id;
		this.index = index;
		out = new ArrayList<Node>();
		in = new ArrayList<Node>();
//		meetingDepth = new ArrayList<Integer>();
	}
	
	@Override
	public int hashCode() {
		return this.id;
	}
	
	@Override
	public boolean equals(Object o) {
		Node n = (Node)o;
		return n.id == this.id;
	}

//	public void addDepth(int length){
//		if (meetingDepth == null){
//			meetingDepth = new ArrayList<Integer>();
//			meetingDepth.add(length);
//		}
//		else{
//			int last_len = meetingDepth.get(meetingDepth.size()-1);
//			if (length > last_len)
//				meetingDepth.add(length);
//		}
//	}
	
	public void initOutEdgeWeight() {
		if (out.size() > 0)
			outEdgeWeight = 1.0 / out.size();
		else
			outEdgeWeight = 0;
	}
	
	public void initOutEdgeWeightUsingNeighborId() {
		if (outId.size() > 0)
			outEdgeWeight = 1.0 / outId.size();
		else
			outEdgeWeight = 0;
	}
	
	
}
