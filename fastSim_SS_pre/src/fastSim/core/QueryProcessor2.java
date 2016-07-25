package fastSim.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fastSim.data.Graph;
import fastSim.data.Node;
import fastSim.data.PrimeSim;
import fastSim.util.Config;
import fastSim.util.PrintInfor;

public class QueryProcessor2 {

	private Graph graph;

	public QueryProcessor2(Graph graph) {
		this.graph = graph;
	}

	public PrimeSim graphExp(Node q, String graphType)
			throws Exception {
		graph.resetPrimeG();
		// int typeIndicator = outGraph? 1:0;
		int expansion = Config.eta;

		PrimeSim sim = new PrimeSim(); // start node
		if (q.isHub){
//		if (false) { // change q.isHub to false, so that it will not use precomputation; 4/17 @fw
			sim.loadFromDisk(q.id, graphType);
			// System.out.println("@@@@@QP line 54: print the sim loaded from disk");
			// PrintInfor.printDoubleMap(sim.getMap(),
			// "length-> <node, score>");

		} else {
			if (graphType == "out")
				sim = graph.computeOutPrimeSim(q,1);
			else if (graphType == "in")
				sim = graph.computeInPrimeSim(q,1);
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
			//	double hubScore = 0;
				double maxHubScore = 0;
				for(int len: borderHubsScore.get(hid).keySet()){
					double current_hubscore = borderHubsScore.get(hid).get(len);
					if (current_hubscore > maxHubScore){
						maxHubScore = current_hubscore;
					}
				//	hubScore += current_hubscore;
				}
			//	if(hubScore<Config.delta)
			//		continue;
				//end. 2/18/2015fw
				PrimeSim nextSim = new PrimeSim();
				//commented the next line to prevent using hub scores 04/17
//				nextSim.loadFromDisk(hid, graphType);
				
				
				// modify start 04/17 to do online computation even if it is hub
				Node h = graph.getNode(hid);
				if (graphType == "out")
					nextSim = graph.computeOutPrimeSim(h,maxHubScore);
				else if (graphType == "in")
					nextSim = graph.computeInPrimeSim(h,maxHubScore);
				
				//modify end
			//	System.out.println(graphType+" Graph of hub: " + hid);
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
	
//	public Map<Integer, Double> query(Node q) throws Exception {
//		Map<Integer, Double> result = new HashMap<Integer, Double>();
//
//		//		System.out.println("Process query: " + q.id);
////		PrimeSim inSim = graphExp(q, "in", null);
//		PrimeSim inSim = graphExp(q, "in");
//		// test:
//		//	System.out.println("=====QP Line149: inGraph of query " + q.id);
//
//		//	System.out.println(inSim.getMap());
//
//		if (inSim.getMeetingNodes().size() == 0) {
//			System.out.println("No meeting nodes in the graph ");
//			result.put(q.id, 1.0);
//			return result;
//		}
//
//			
//		for (int mid : inSim.getMeetingNodes()) {
//			//added Aug-29: if the average important tour ending at meeting node is ignorable, then don't expand it
//			//			if(checkMeetigNodeImportance(inSim,mid)<Config.delta)
//			//				continue;
//			// System.out.println("=====QP Line163: outgraph of node " + mid );
//			//		System.out.println("Merge with meeting node " + mid + " out graph: ");
//			Node meetingNode = graph.getNode(mid);
////			PrimeSim outSim = graphExp(meetingNode, "out", meetingNode.meetingDepth);
//			PrimeSim outSim = graphExp(meetingNode, "out");
//			// test Ingraph
//			//		System.out.println(outSim.getMap());
//			// PrintInfor.printDoubleMap(insim.getMap(), "len-><node,score>");
//
//			int minLen = inSim.numLength() > outSim.numLength() ? outSim
//					.numLength() : inSim.numLength();
////					if (meetingNode.meetingDepth != null){
//						for (int length = 1; length < minLen; length++) {
//
//							//						for (int length : meetingNode.meetingDepth) {
//							if (inSim.getMap().get(length).get(mid) == null)
//								continue;
//
//							if (outSim.getMap().get(length) == null)
//								continue;
//
//							//double reaTomeetingNode = inSim.getMap().get(length).get(mid);
////							double reaTomeetingNode = inSim.getMap().get(length).get(mid)*Math.pow(Config.alpha, length); //multiply alpha here
//							double reaTomeetingNode = inSim.getMap().get(length).get(mid);
//							for (Map.Entry<Integer, Double> endingNode : outSim.getMap()
//									.get(length).entrySet()) {
//								if (endingNode.getKey() == q.id) // don't need to compute
//									// the similarity of
//									// same node
//									continue;
//								if (endingNode.getKey() == 1)
//									System.out.println("level= "+length+": from "+mid+" with "+ reaTomeetingNode
//											* endingNode.getValue());
//								double oldValue = (result.get(endingNode.getKey()) == null) ? 0.0
//										: result.get(endingNode.getKey());
//								double rea = reaTomeetingNode * endingNode.getValue();
//								double increment = rea;
//								if (Config.correctionLevel >0){
//									List<Node> n_in_nodes = meetingNode.in;
//									int n_in_degree = n_in_nodes.size();
//									if (n_in_degree > 0 && length < Config.depth){
//										increment -= Config.alpha*rea/n_in_degree; // correction for the one-hop non-first-meeting nodes
//										if (Config.correctionLevel == 2 && length < Config.depth - 1){
//											for (int i = 1; i < n_in_degree; i++){
//												for (int j = 0; j < i; j++){ // only iterate through all unique and unequal pairs of n's in-neighbors
//													Set<Integer> vi_in = new HashSet();
//													for (Node vi_in_node : n_in_nodes.get(i).in) 
//														vi_in.add(vi_in_node.id);
//													List<Node> vj_in = n_in_nodes.get(j).in;
//													for (Node vj_in_node : vj_in){
//														if (vi_in.contains(vj_in_node.id)){
//															/* subtract the similarity scores of non-first-meeting nodes two hops away*/
//															increment -= Math.pow(Config.alpha,2)*rea/Math.pow(n_in_degree,2)*2/vi_in.size()/vj_in.size();
//														}
//													}
//
//												}
//											}
//										}
//									}
//								}
//								result.put(endingNode.getKey(),
//										oldValue + increment);
//							}
//
//						}
//						
////					}
//
//		}
//		result.put(q.id, 1.0);
//		return result;
//
//	}

	public Map<Integer, Double> query(Node q) throws Exception {
		Map<Integer, Double> result = new HashMap<Integer, Double>();
		
//		System.out.println("Process query: " + q.id);
		PrimeSim inSim = graphExp(q, "in");

		// test:
	//	System.out.println("=====QP Line149: inGraph of query " + q.id);

	//	System.out.println(inSim.getMap());

		if (inSim.getMeetingNodes().size() == 0) {
			System.out.println("No meeting nodes in the graph ");
			result.put(q.id, 1.0);
			return result;
		}

		for (int mid : inSim.getMeetingNodes()) {
			//added Aug-29: if the average important tour ending at meeting node is ignorable, then don't expand it
//			if(checkMeetigNodeImportance(inSim,mid)<Config.delta)
//				continue;
			// System.out.println("=====QP Line163: outgraph of node " + mid );
	//		System.out.println("Merge with meeting node " + mid + " out graph: ");
			PrimeSim outSim = graphExp(graph.getNode(mid), "out");
			// test Ingraph
	//		System.out.println(outSim.getMap());
			// PrintInfor.printDoubleMap(insim.getMap(), "len-><node,score>");
			
			int minLen = inSim.numLength() > outSim.numLength() ? outSim
					.numLength() : inSim.numLength();
			for (int length = 1; length < minLen; length++) {
				
				if (inSim.getMap().get(length).get(mid) == null)
					continue;
				
				double reaTomeetingNode = inSim.getMap().get(length).get(mid);
//				double reaTomeetingNode = inSim.getMap().get(length).get(mid)*Math.pow(Config.alpha, length); //multiply alpha here

				for (Map.Entry<Integer, Double> endingNode : outSim.getMap()
						.get(length).entrySet()) {
					if (endingNode.getKey() == q.id) // don't need to compute
														// the similarity of
														// same node
						continue;
					double oldValue = (result.get(endingNode.getKey()) == null) ? 0.0
							: result.get(endingNode.getKey());
					result.put(endingNode.getKey(),
							oldValue + reaTomeetingNode
									* endingNode.getValue());
				}
				// System.out.println("$$$$$RESULT: Length: " +length +
				// " meeting node: " + node);
				// for (Map.Entry<Integer, Double> v : result.entrySet())
				// System.out.println("<" + v.getKey() + " " + v.getValue()
				// +" >");
			}
//			System.out.println("Results after merging with: "+mid);
//			System.out.println(result);

		}
		result.put(q.id, 1.0);
		return result;

	}
	private double checkMeetigNodeImportance(PrimeSim inSim, int mid) {
		// TODO Auto-generated method stub
		double sum =0.0;
		int count=0;
		for(int length = 1; length < inSim.getMap().size(); length++){
			if(inSim.getMap().get(length).get(mid)==null)
				continue;

			double rea =inSim.getMap().get(length).get(mid);
			sum += rea;
			count++;
		}
		return sum/count;
	}

}
