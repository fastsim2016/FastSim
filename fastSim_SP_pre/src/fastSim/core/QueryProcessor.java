package fastSim.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fastSim.data.Graph;
import fastSim.data.Node;
import fastSim.data.PrimeSim;
import fastSim.util.Config;
import fastSim.util.PrintInfor;

public class QueryProcessor {

	private Graph graph;

	public QueryProcessor(Graph graph) {
		this.graph = graph;
	}

	public PrimeSim graphExp(Node q, String graphType)
			throws Exception {
		graph.resetPrimeG();
		// int typeIndicator = outGraph? 1:0;
		int expansion = Config.eta;

		PrimeSim sim = new PrimeSim(); // start node
		
		if (q.isHub) { // change q.isHub to false, so that it will not use precomputation; 4/17 @fw
			sim.loadFromDisk(q.id, graphType);
			// System.out.println("@@@@@QP line 54: print the sim loaded from disk");
			// PrintInfor.printDoubleMap(sim.getMap(),
			// "length-> <node, score>");

		} else {
			if (graphType == "out")
				sim = graph.computeOutPrimeSim(q);
			else if (graphType == "in")
				sim = graph.computeInPrimeSim(q);
			else {
				System.out
						.println("Type of prime graph should be either out or in.");
				System.exit(0);
			}
		}
		// System.out.println("expansion= " + expansion);
//		System.out.println("meeting nodes in prime " + gType + "-graph: "
//				+ sim.getMeetingNodes());

		if (expansion == 0 || sim.numHubs() == 0)
			return sim; // for primeInG, always expand for eta iterations

		// else: expand the out graph

		PrimeSim expSim = sim.duplicate();

		if (graphType == "in")
			expSim.addMeetingNodes(sim.getMeetingNodes());
		// System.out.println("QP#####: meeting nodes in prime subG:");
		// for(int x : expSim.getMeetingNodes())
		// System.out.print(x + " ");
		// System.out.println();

		Map<Integer, Map<Integer, Double>> borderHubsScore = new HashMap<Integer, Map<Integer, Double>>(); // hub->(length,value)

		// extracting borderHubs information
		for (int length : sim.getMap().keySet()) {
			//added 8-27
			if(length==0) continue; //don't expand the query node if itself is a hub
			for (int nid : sim.getMap().get(length).keySet()) {
				Node node = graph.getNode(nid);
				//added 8-27
				//if(node==q) continue; q should also be expanded, it can affect the reachability of other nodes, just s(q,q) wouldn't be affected
				if (node.isHub) {
					// store the reachability to hub
					if (borderHubsScore.get(nid) == null) {
						borderHubsScore
								.put(nid, new HashMap<Integer, Double>());
					}
					if (borderHubsScore.get(nid).get(length) == null) {
						borderHubsScore.get(nid).put(length,
								sim.getMap().get(length).get(nid));
					} else {
						System.out.println("shouldn't go to here.");
						double old_value = borderHubsScore.get(nid).get(length);
						borderHubsScore.get(nid).put(length,
								old_value + sim.getMap().get(length).get(nid));
					}

				}
			}
		}

	

		// recursively adding outG of hubs
		//int i = 0;
		while (expansion > 0) {
			expansion = expansion - 1;
//			i++;
//			if (gType == "out")
//				System.out.println("@@@Iteration " + i);
			// expansion--;
			Map<Integer, Map<Integer, Double>> borderHubsNew = null;
			if(expansion>0)
				borderHubsNew = new HashMap<Integer, Map<Integer, Double>>();
			if(borderHubsScore.size()==0) return expSim;
			for (int hid : borderHubsScore.keySet()) {
				//add expanding threshold for hubs: Config.delta
				double hubScore = 0;
				for(int len: borderHubsScore.get(hid).keySet())
					hubScore += borderHubsScore.get(hid).get(len);
				if(hubScore<Config.delta)
					continue;
				//end. 2/18/2015fw
				PrimeSim nextSim = new PrimeSim();
				//commented the next line to prevent using hub scores 04/17
				nextSim.loadFromDisk(hid, graphType);
				
				
			/*	// modify start 04/17 to do online computation even if it is hub
				Node h = graph.getNode(hid);
				if (graphType == "out")
					nextSim = graph.computeOutPrimeSim(h);
				else if (graphType == "in")
					nextSim = graph.computeInPrimeSim(h);
				
				//modify end
*/			//	System.out.println(graphType+" Graph of hub: " + hid);
			//	System.out.println(nextSim.getMap());
				if (graphType == "in")
					expSim.addMeetingNodes(nextSim.getMeetingNodes());
				
				expSim.addFrom(nextSim, borderHubsScore.get(hid));// expand
																	// graph
				// PrintInfor.printDoubleMap(expSim.getMap(),
				// "&&&=&=&=&& Expanded graph out sim");
				
				
				if(expansion>0){
					//store border hubs in nextSim
					
					for(int i =0; i< nextSim.numHubs(); i++){
						int newHub = nextSim.getHubId(i);
						for(int l=1; l <nextSim.getMap().size();l++){
							if(nextSim.getMap().get(l).containsKey(newHub)){
								double addScore= nextSim.getMap().get(l).get(newHub);
								
								//set borderHubsNew
								if(borderHubsNew.get(newHub)==null)
									borderHubsNew.put(newHub, new HashMap<Integer,Double>());
								for(int oldLen : borderHubsScore.get(hid).keySet()){
									double oldScore = borderHubsScore.get(hid).get(oldLen);
									double existScore;
									if (borderHubsNew.get(newHub).get(l+oldLen)==null)
										existScore =0.0;
									else
										existScore =borderHubsNew.get(newHub).get(l+oldLen);
									borderHubsNew.get(newHub).put(l+oldLen, existScore+oldScore*addScore);
								}
									
								
							}
						}
						
					}
					
					
				}//end if
				
				

			}//end for 
//			if (borderHubsNew.size() == 0)
//				return expSim;

			borderHubsScore = borderHubsNew;
		}

		return expSim;
	}

	public double query(Node q1, Node q2) throws Exception {
		if (q1.id == q2.id)
			return 1;
		double result = 0;

//		System.out.println("Process query: " + q.id);
		PrimeSim inSim1 = graphExp(q1, "in");
		PrimeSim inSim2 = graphExp(q2, "in");
		
		// test:
	//	System.out.println("=====QP Line149: inGraph of query " + q.id);

	//	System.out.println(inSim.getMap());

		if (inSim1.getMeetingNodes().size() == 0 || inSim2.getMeetingNodes().size() == 0) {
	//		System.out.println("No meeting nodes for ("+q1.id +", "+q2.id+")");
			result = 0;
			return result;
		}
	//	else
	//		System.out.println("Found meeting nodes for ("+q1.id +", "+q2.id+")");
		PrimeSim smallPS = inSim1.numLength() < inSim2.numLength() ? inSim1 : inSim2;
		PrimeSim bigPS = inSim1.numLength() < inSim2.numLength() ? inSim2 : inSim1;
	//	System.out.println("SmallPS: "+smallPS.getMap());
	//	System.out.println("BigPS: "+bigPS.getMap());
//		double[] scoreAtLevel = new double[smallPS.numLength()];
//		int[] lenAtLevel = new int[smallPS.numLength()];
	//	int level = 0;
	//	Set<Integer> len_set = smallPS.getLengths();
	//	System.out.println("Smaller length is "+ len_set);
		Iterator<Integer> len_iterator = smallPS.getLengths().iterator();
		while(len_iterator.hasNext()) {
			Integer length = len_iterator.next();
	//		System.out.println("Current length is: "+ length);
			if (bigPS.getMap().get(length) == null)
				continue;
			Map<Integer,Double> small_node_score_map = smallPS.getMap().get(length).size() < bigPS.getMap().get(length).size() ?
					smallPS.getMap().get(length) : bigPS.getMap().get(length);
			Map<Integer,Double> big_node_score_map = smallPS.getMap().get(length).size() < bigPS.getMap().get(length).size() ?
					bigPS.getMap().get(length) : smallPS.getMap().get(length);
	//		System.out.println("smaller: "+small_node_score_map);
	//		System.out.println("bigger: "+ big_node_score_map);
			for (int nid : small_node_score_map.keySet()) {
				if (big_node_score_map.get(nid) == null)
					continue;
				double rea = small_node_score_map.get(nid)
						* big_node_score_map.get(nid); //multiply alpha here
				//*Math.pow(Config.alpha, length)
	//			System.out.println("reachability of one matched tour: "+ rea);
				result += rea;
	//			System.out.println("reachability of all matched tours so far: "+ result);
				
				//below is for correction:
			//	List<Node> n_in_nodes = graph.getNode(nid).in;
			//	int n_in_degree = n_in_nodes.size();
			//	if (n_in_degree > 0 && length < Config.depth){
			//	if (n_in_degree < 0 && length < 0){ //no correction (this line is to replace the previous line?)
			//		result -= Config.alpha*rea/n_in_degree; // correction for the one-hop non-first-meeting nodes
	//				System.out.println("After one-hop correction: "+ result);
					/*if (length < Config.depth - 1){
						for (int i = 1; i < n_in_degree; i++){
							for (int j = 0; j < i; j++){ // only iterate through all unique and unequal pairs of n's in-neighbors
								Set<Integer> vi_in = new HashSet();
								for (Node vi_in_node : n_in_nodes.get(i).in) 
									vi_in.add(vi_in_node.id);
								List<Node> vj_in = n_in_nodes.get(j).in;
								for (Node vj_in_node : vj_in){
									if (vi_in.contains(vj_in_node.id)){
										 subtract the similarity scores of non-first-meeting nodes two hops away
										result -= Math.pow(Config.alpha,2)*rea/Math.pow(n_in_degree,2)*2/vi_in.size()/vj_in.size();
	//									System.out.println("After two-hop correction: "+ result);
									}
								}
									
							}
						}
					}*/
	//			}
				
				
			}		


		}

		return result;
	}

}
