package fastSim.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

//import fanweizhu.fastSim.util.Config;
//import fanweizhu.fastSim.util.IndexManager;
//import fanweizhu.fastSim.util.KeyValuePair;
//import fanweizhu.fastSim.util.MapCapacity;
import fastSim.util.*;
import fastSim.util.io.*;


public class PrimeSim implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7028575305146090045L;
	private List<Integer> hubs;
	protected Map<Integer, Map<Integer,Double>> map;
	protected boolean outG;
	protected List<Integer> meetingNodes;
	protected List<Integer> meetingNodes_depth;

	/*public PrimeSim(int capacity) {
		super(capacity);
		hubs = new ArrayList<Integer>();
	}*/

	public PrimeSim() {
		map = new HashMap<Integer, Map<Integer,Double>>();
		hubs = new ArrayList<Integer>();
		meetingNodes = new ArrayList<Integer>();
		meetingNodes_depth = new ArrayList<Integer>();
		
	}
	public PrimeSim(int numNodes) {
		
		//need to change MapCapacity when double->Map?
		map = new HashMap<Integer, Map<Integer,Double>>(MapCapacity.compute(numNodes));
		hubs = new ArrayList<Integer>();
	}

	public int numHubs() {
		return hubs.size();
	}
	public int numLength(){
		return map.size();
	}
	public Map<Integer,Map<Integer,Double>> getMap(){
		return map;
	}
	public int getHubId(int index) {
		return hubs.get(index);
	}
	public List<Integer> getMeetingNodes(){
		return meetingNodes;
	}
	
	public List<Integer> getMeetingNodes_depth(){
		return meetingNodes_depth;
	}
	
	public void addNewNode(Node h, String simType){
			
			h.isVisited = true;
			if(h.isHub)
				hubs.add(h.id);
			if(simType=="in" && h.out.size()>1)  //store meeting nodes for ingraphs //meetingnodes refer to >1 nodes (descendants)
				meetingNodes.add(h.id);
		
		 
	}
	
	public void set(int l, Node n, double value) {
//		if (n.isVisited == false){
//			if (n.isHub)
//				hubs.add(n.id);
//			if (graphType=="in" && n.in.size()>1)
//				meetingNodes.add(n.id);
//		}
//		
		
		Map<Integer, Double> nodesVal;
		if (map.get(l)!= null)
		{
			nodesVal = map.get(l);
			nodesVal.put(n.id, value);
			map.put(l, nodesVal);
		}
		else
		{	
			nodesVal = new HashMap<Integer,Double>();
			nodesVal.put(n.id, value);
			map.put(l, nodesVal);
		}
		
	}
	

	public void set(int l, Map<Integer,Double> nodeValuePairs){
		//System.out.println(l);
		Map<Integer, Double> nodesVal = map.get(l);
//		for(Integer i:nodeValuePairs.keySet()) {
//			System.out.println("PS node: "+ i + " rea: " +nodeValuePairs.get(i));
//		}
		if(nodesVal == null)
		{
			map.put(l, nodeValuePairs);
		}
		else{
			System.out.println("####PrimeSim line108: should not go to here.");
			nodesVal.putAll(nodeValuePairs);
			map.put(l, nodesVal);
		}
		//System.out.println("length_Test:" + l + " Map_Size:" + map.get(l).size());
//		for(Integer i: map.get(l).keySet())
//			System.out.println(map.get(l).get(i));
	}
	

	public long computeStorageInBytes() {
		long nodeIdSize = (1 + hubs.size()) * 4;
		long mapSize = (1 + map.size()) * 4 + map.size() * 8;
		return nodeIdSize + mapSize;
	}

	

	

	public String getCountInfo() {
		//int graphSize = map.size();
		int hubSize = hubs.size();
		int meetingNodesSize = meetingNodes.size();

		return   "hub size: " + hubSize + " meetingNodesSize: " + meetingNodesSize ;
	}
	public void saveToDisk(int id,PrimeSim ps,String type) throws IOException {
		String path = "";
		if(type == "out")
			//path = "./outSim/" + Integer.toString(id);
			path = IndexManager.getIndexDeepDir() + "out/" +Integer.toString(id);
		else if(type == "in")
			//path = "./inSim/" + Integer.toString(id);
			path = IndexManager.getIndexDeepDir() + "in/" +Integer.toString(id);
		else{
			System.out.println("Type of prime graph should be either out or in.");
			System.exit(0);
		}
		try{	
//		File file_path = new File(path);
//		if (!file_path.exists()){
//			file_path.mkdirs();
//		}
		File file = new File(path);
		if (!file.exists()){
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		FileOutputStream fo = new FileOutputStream(file);   
		
        ObjectOutputStream so = new ObjectOutputStream(fo);
        
        so.writeObject(ps);
        
        so.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	public void loadFromDisk(int id,String type) throws IOException, ClassNotFoundException {
		String path = "";
		if(type == "out")
			path = IndexManager.getIndexDeepDir() + "out/" + Integer.toString(id);
		else if(type == "in")
			path = IndexManager.getIndexDeepDir() + "in/" + Integer.toString(id);
		else
		{
			System.out.println("Type of prime graph should be either out or in.");
			System.exit(0);
		}
	//	System.out.println("hub path: " + path);
		
		FileInputStream fi = new FileInputStream(path);   
		  
        ObjectInputStream si = new ObjectInputStream(fi); 
        
        PrimeSim res = (PrimeSim)si.readObject();
   //     System.out.println("graph info(size, hubs#, meetingnodes#: "+ res.getCountInfo());
   //     System.out.println("sim loaded from disk");
   //     PrintInfor.printDoubleMap(res.getMap(),"len,<node, score>");
        
        si.close();
        // changed here
        this.hubs = res.hubs;
        this.map = res.map;
        this.meetingNodes = res.meetingNodes;
        
       // return res;
	}
	public PrimeSim duplicate() {
		// TODO Auto-generated method stub
		PrimeSim sim = new PrimeSim();
		sim.map.putAll(this.map);
		return sim;
		
	}
	public void addFrom(PrimeSim nextOut, Map<Integer, Double> oneHubValue) {
		// TODO Auto-generated method stub
		for (int lenToHub : oneHubValue.keySet()){
			double hubScoreoflen = oneHubValue.get(lenToHub);
//			if (hubScoreoflen < Config.clip) // don't expand the hub if its reachability is too small
//				continue;
			for (int lenFromHub : nextOut.getMap().keySet()){
				if(lenFromHub == 0){
					// the new score of hub (over length==0) is just the score on prime graph
					continue;
				}
				int newLen = lenToHub + lenFromHub;
				if (!this.getMap().containsKey(newLen))
					this.getMap().put(newLen, new HashMap<Integer,Double>());
				for(int toNode: nextOut.getMap().get(lenFromHub).keySet()){
					double oldValue = this.getMap().get(newLen).keySet()
							.contains(toNode) ? this.getMap().get(newLen).get(toNode): 0.0;
							//System.out.println(oldValue);
					double newValue = hubScoreoflen *nextOut.getMap().get(lenFromHub).get(toNode);
//				//added aug-29
//					if (newValue<Config.epsilon)
//						continue;
					this.getMap().get(newLen).put(toNode, oldValue +  newValue) ;				
//					PrintInfor.printDoubleMap(this.getMap(), "assemble simout of the hub at length: " + lenFromHub +" node: "+ toNode );
//					System.out.println(this.getMap());
				}				
			}
		}
		 
	}
	
	public void addMeetingNodes(List<Integer> nodes){
		
		for (int nid: nodes){
			if (!this.meetingNodes.contains(nid))
				this.meetingNodes.add(nid);
		}
		//System.out.println("====PrimeSim: line 195: meetingnodes Size " + this.meetingNodes.size());
	}

}
