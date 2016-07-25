package fastSim.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import fastSim.core.*;
import fastSim.util.ClusterManager;
import fastSim.util.Config;
import fastSim.util.IndexManager;
import fastSim.util.KeyValuePair;
import fastSim.util.io.DataWriter;
import fastSim.util.io.TextReader;
import fastSim.util.io.TextWriter;

public class Graph {

    protected Map<Integer, Node> nodes;
    protected Set<Node> hubs;
    private Random rnd;
	private final double ITER_STOP = Config.stopRea; //0.001;
	private final double DEPTH = Config.depth;
	private final int maxNode = Config.maxNode;
//only for exact sim
	//private int[] nodesIndex;
	

    // private static final int NUM_DUMMY_EDGES = 1;

    public Graph() {
        nodes = new HashMap<Integer, Node>();
        rnd = new Random(9876804367L);
    //exact
      //  nodesIndex = new int[maxNode];
    }

    public void preprocess() {
        Set<Node> remove = new HashSet<Node>();
        for (Node n : nodes.values()) {
            // if (n.in.size() < minNumEdges || n.out.size() < minNumEdges) {
            if (n.in.isEmpty() || n.out.isEmpty())
                remove.add(n);
        }
        if (remove.isEmpty())
            return; // finished

        System.out.println(remove.size() + " nodes to be removed...");

        for (Node n : remove) {
            // remove from out neighbors' in
            for (Node m : n.out)
                m.in.remove(n);

            // remove from in neighbors' out
            for (Node m : n.in)
                m.out.remove(n);

            nodes.remove(n.id);
        }

        preprocess();
    }

    public void saveToFile(String nodeFile, String edgeFile) throws Exception {
        TextWriter out = new TextWriter(nodeFile);
        for (Node n : nodes.values())
            out.writeln(n.id);
        out.close();

        out = new TextWriter(edgeFile);
        int count = 0;
        for (Node n : nodes.values())
            for (Node m : n.out) {
                out.writeln(n.id + "\t" + m.id);
                count++;
            }
        out.close();

        System.out.println("# Nodes: " + nodes.size());
        System.out.println("# Edges: " + count);
    }

    public void clear() {
        nodes.clear();
    }


    private void loadGraphFromFile(String nodeFile, String edgeFile) throws Exception {
        clear();

        TextReader inN = new TextReader(nodeFile);
        TextReader inE = new TextReader(edgeFile);
        String line;

        System.out.print("Loading graph");
        int count = 0;
        while ((line = inN.readln()) != null) {
            int id = Integer.parseInt(line);
            this.addNode(new Node(id,count));
// for exact
            //this.nodesIndex[count]=id;
//            System.out.println("Test node index----Graph:Line103-----Node id: "+ id+" index: "+count);

            count++;
            if (count % 1000000 == 0)
                System.out.print(".");
        }

        while ((line = inE.readln()) != null) {
            String[] split = line.split("\t");
            int from = Integer.parseInt(split[0]);
          //  System.out.println(from);
            int to = Integer.parseInt(split[1]);
            this.addEdge(from, to);
            
//            count++;
//            if (count % 1000000 == 0)
//                System.out.print(".");
        }
        System.out.println();

        inN.close();
        inE.close();

        init();
    }

    public void loadFromFile(String nodeFile, String edgeFile,
                             boolean identifyHubs) throws Exception {
        loadGraphFromFile(nodeFile, edgeFile);

        if (identifyHubs){
           String hubNodeFile= IndexManager.getHubNodeFile();
         
           loadHubs(hubNodeFile);
        }

    }

    public void loadFromFile(String nodeFile, String edgeFile,
                             String hubFile) throws Exception {
        loadGraphFromFile(nodeFile, edgeFile);

        loadHubs(hubFile);

    }

    public void loadHubs(String hubNodeFile) throws Exception {
        TextReader in = new TextReader(hubNodeFile);
        String line;

        hubs = new HashSet<Node>();
        
        while ((line = in.readln()) != null) {
        	if (hubs.size() == Config.numHubs)
                break;
            int id = Integer.parseInt(line);
            Node n = getNode(id);
            if (n == null)
                n = new Node(id);
            n.isHub = true;
            hubs.add(n);
            
        }

        in.close();
    }

    public Set<Node> getHubs() {
        return hubs;
    }

    public void addNode(Node n) {
        nodes.put(n.id, n);
    }

    public void addEdge(int from, int to) {
        Node nFrom = getNode(from);
        Node nTo = getNode(to);
        nFrom.out.add(nTo);
        nTo.in.add(nFrom);
    }
    
    

  /* public PrimeSim computePrimeSim(Node q, boolean out) {
        // System.out.print("x");
	   PrimeGraph sub = new PrimeGraph(q);
	   sub.reset();
	   if (out){
	    	return sub.computeOutPrimeSim();
	    }
	   else
		   return sub.computeInPrimeSim();

        // System.out.print("y");
       
     
    }
   */
   

    public void resetPrimeG(){
    	for (Node n : nodes.values()) {
			n.isVisited = false;
			
		}
    	
    }
public PrimeSim computeOutPrimeSim(Node q) throws IOException {
		
		
		PrimeSim outSim = new PrimeSim();
		
		List<Node> expNodes = new ArrayList <Node>();
		
		Map<Integer,Double> valInLastLevel = new HashMap<Integer,Double>();
		
		
		expNodes.add(q);
		
		q.isVisited = true; //don't save query node as hub or meeting node
		outSim.addNewNode(q, "out");
		outSim.set(0, q, 1.0);
		valInLastLevel.put(q.id, 1.0);
		
		int length =1;
		while (length <= DEPTH){
	
			if (expNodes == null)
				break;
			List<Node> newExpNodes = new ArrayList <Node>();
			Map<Integer,Double> valInThisLevel = new HashMap<Integer,Double>();
			for (Node cur:expNodes){
				//System.out.println("current node: " + cur.id);
				for (Node n: cur.out){ // an edge cur-->n, where cur is meeting node, so for n, should store the Reversed reachability: R(n->cur)=1*1/In(n)
					//System.out.println("out:" + n.id);
					if(!n.isVisited)
						outSim.addNewNode(n, "out");
				double rea = valInLastLevel.get(cur.id)/n.in.size()*Math.sqrt(Config.alpha); //*(Config.alpha)2.26: ensure the prime subgraphs are not too large
				//know the reachability when expanding hubs
				//	double rea = valInLastLevel.get(cur.id)/n.in.size();//modified 8.27, don't multiply alpha at this time, otherwise will double multiply alpha^length as another tour also has this alpha, so leave it for online merging.
					if(rea > ITER_STOP){
						if (valInThisLevel.get(n.id)==null){
							//System.out.println("value:" + rea);
							valInThisLevel.put(n.id, rea);
							if (n.out.size()>0 && !n.isHub)
									newExpNodes.add(n);	
							
						}
						else
							valInThisLevel.put(n.id, valInThisLevel.get(n.id)+rea);
					}															
				}								
			}
						
			outSim.set(length,valInThisLevel);
//			System.out.println(length);
//			for(Integer i:valInThisLevel.keySet()) {
//				System.out.println("node:" + i + " rea:" + valInThisLevel.get(i));
//			}
			expNodes = newExpNodes;
			//newExpNodes.clear();
			
			valInLastLevel = valInThisLevel;
			//valInThisLevel.clear();
			
			length++;
		}
	/*	for(Integer m:outSim.map.keySet()) {
			System.out.println("length:" + m);
			//System.out.println(outSim.map.get(m).size());
			for(Integer n:outSim.map.get(m).keySet()) {
				System.out.println("node:" + n + " rea:" + outSim.map.get(m).get(n));
			}
		}*/
	//	System.out.println("Remove empty length (Out) for node " + q.id);
		List<Integer> toRemoveLength = new ArrayList<Integer>();
		for (int l : outSim.getMap().keySet()){
			if (outSim.getMap().get(l).size()==0){
	//			System.out.println("No such length in outsim " + l);
				toRemoveLength.add(l);				
			}
		}
		for(int i : toRemoveLength)
			outSim.getMap().remove(i);
		
		return outSim;
	}
	
	
public PrimeSim computeInPrimeSim(Node q) throws IOException {
		
		PrimeSim inSim = new PrimeSim();
		
		List<Node> expNodes = new ArrayList <Node>();
		//List<Node> newExpNodes = new ArrayList <Node>();
		Map<Integer,Double> valInLastLevel = new HashMap<Integer,Double>();
		//Map<Integer,Double> valInThisLevel = new HashMap<Integer,Double>();
		
		expNodes.add(q); // Nodes to be expanded, initially only q
		//add 2/23/2015
		inSim.addNewNode(q, "in");
		//q.isVisited = true;
		inSim.set(0, q, 1.0);
		valInLastLevel.put(q.id, 1.0);
		
		int length =1;
		while (length <= DEPTH){
			if (expNodes == null)
				break;
			List<Node> newExpNodes = new ArrayList <Node>();
			Map<Integer,Double> valInThisLevel = new HashMap<Integer,Double>();
			for (Node cur:expNodes){
				for (Node n: cur.in){ //a edge n-->cur, if R(cur)==a, then R(n)==1/In(cur)*a, because of the reversed walk from cur to n.
					if(!n.isVisited)
						inSim.addNewNode(n, "in"); // mark n as visited and add n to meetingNodes in PrimeSim
					double rea = valInLastLevel.get(cur.id)/cur.in.size()*Math.sqrt(Config.alpha); //
				//	System.out.println(n+": "+ rea);
				//	double rea = valInLastLevel.get(cur.id)/cur.in.size(); // modified 8.27, mutiply alpha later when merge two tours
					if (rea > ITER_STOP){
						if (valInThisLevel.get(n.id)==null)
						{
							valInThisLevel.put(n.id, rea);
							if (n.in.size()>0 && !n.isHub)
								//newExpNodes.add(n);
								//if(!n.isHub)
									newExpNodes.add(n);
	//							else
	//								continue;
						}
					
						else
							valInThisLevel.put(n.id, valInThisLevel.get(n.id)+rea);						
					}										
					
				}								
			}
			
			inSim.set(length,valInThisLevel);

			expNodes = newExpNodes;
			//newExpNodes.clear();
			
			valInLastLevel = valInThisLevel;
			//valInThisLevel.clear();
			
			length++;
		}
	//	System.out.println("Remove empty length for node " + q.id);
		List<Integer> toRemoveLen = new ArrayList<Integer>();
		for (int l : inSim.getMap().keySet()){
			if (inSim.getMap().get(l).size()==0){
	//			System.out.println("No such length in Insim " + l);
				toRemoveLen.add(l);
				
			}
		}	
		for (int i : toRemoveLen)
			inSim.getMap().remove(i);
		
		return inSim;
	}


    public int numNodes() {
        return nodes.size();
    }

    public Node getNode(int id) {
        return nodes.get(id);
    }

    public Collection<Node> getNodes() {
        return nodes.values();
    }

    public boolean containsNode(int id) {
        return nodes.containsKey(id);
    }

   
    public void init() {
        for (Node n : nodes.values())
            n.initOutEdgeWeight();
    }

	/*public Node getNodeByIndex(int i) {
		// TODO Auto-generated method stub
		int nodeId = nodesIndex[i];
		return nodes.get(nodeId);
		
	}*/
}
