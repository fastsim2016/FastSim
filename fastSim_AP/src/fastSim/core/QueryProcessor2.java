package fastSim.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import fanweizhu.fastSim.data.Graph;
import fanweizhu.fastSim.data.Node;
import fanweizhu.fastSim.data.PrimeSim;
import fastSim.util.Config;
import fastSim.util.PrintInfor;

public class QueryProcessor2 {

	private Graph graph;

	public QueryProcessor2(Graph graph) {
		this.graph = graph;
	}

	public PrimeSim graphExp(Node q, String graphType)
			throws ClassNotFoundException, IOException {
		graph.resetPrimeG();
		// int typeIndicator = outGraph? 1:0;
		int expansion = Config.eta;

		PrimeSim sim = new PrimeSim(); // start node

		if (false && q.isHub) { // no pre-computation 04/23
			sim.loadFromDisk(q.id, graphType);
			// System.out.println("@@@@@QP line 54: print the sim loaded from disk");
			// PrintInfor.printDoubleMap(sim.getMap(),
			// "length-> <node, score>");

		} else {
			if (graphType == "out")
				sim = graph.computeOutPrimeSim(q, 1);
			else if (graphType == "in")
				sim = graph.computeInPrimeSim(q, 1);
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
				// skip hubs < delta
//				double hubScore = 0;
				double maxHubScore = 0;
				for(int len: borderHubsScore.get(hid).keySet()){
					double current_hubscore = borderHubsScore.get(hid).get(len);
					if (current_hubscore > maxHubScore){
						maxHubScore = current_hubscore;
					}
//					hubScore += current_hubscore;
				}
//				if(hubScore<Config.delta)
				if(maxHubScore < Config.stopRea)
					continue;
				PrimeSim nextSim = new PrimeSim();
//				nextSim.loadFromDisk(hid, graphType); // no pre-computation @04/22
				// modify start 04/17 to do online computation even if it is hub
				Node h = graph.getNode(hid);
				if (graphType == "out")
					nextSim = graph.computeOutPrimeSim(h, maxHubScore);
				else if (graphType == "in")
					nextSim = graph.computeInPrimeSim(h, maxHubScore);
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
	public static class NPComparator implements Comparator<nodePairVal> {

		public int compare(nodePairVal one, nodePairVal two) {
			return Double.compare(one.val, two.val);
		}
	}
	public class nodePairVal{
		public int n1;
		public int n2;
		public double val;
		public nodePairVal(int node1, int node2, double v){ // idx1 < idx2
			n1 = node1;
			n2 = node2;
			val = v;
		}
	}
	public PriorityQueue<nodePairVal> query(int k) throws Exception {
		Collection<Node> allNodes = graph.getNodes();
		int numNodes = allNodes.size();
//		Set<Long> nodesProduct = new HashSet<Long>();
		Map<Integer, Map<Integer, Double>> scores = new HashMap<Integer, Map<Integer, Double>>();
//		nodePairVal[] result = new nodePairVal[numNodes*(numNodes-1)/2]; // result to store score of all pairs 
		PriorityQueue<nodePairVal> result = new PriorityQueue<nodePairVal>(k, new NPComparator());
		for (Node meetingNode : allNodes) {
			if (meetingNode.out.size() < 2)
				continue;
			PrimeSim outSim = graphExp(meetingNode, "out");
			//			int count = 0;
			for (int length : outSim.getMap().keySet()) {
				List<Integer> endingNodes = new ArrayList<Integer>(outSim.getMap().get(length).keySet());
				Map<Integer, Double> reaMap = outSim.getMap().get(length);
				for (int idx1 = 0; idx1 < endingNodes.size() - 1; idx1++){
					for (int idx2 = idx1 + 1; idx2 < endingNodes.size(); idx2++){
						int n1 = endingNodes.get(idx1);
						int n2 = endingNodes.get(idx2);
						double rea = reaMap.get(n1)*reaMap.get(n2);
						double increment = rea;
						if (Config.correctionLevel >0){ // no correction
							List<Node> n_in_nodes = meetingNode.in;
							int n_in_degree = n_in_nodes.size();

							if (n_in_degree > 0 && length < Config.depth){
								increment -= Config.alpha*rea/n_in_degree; // correction for the one-hop non-first-meeting nodes
								if (Config.correctionLevel == 2 && length < Config.depth - 1){
									for (int i = 1; i < n_in_degree; i++){
										for (int j = 0; j < i; j++){ // only iterate through all unique and unequal pairs of n's in-neighbors
											Set<Integer> vi_in = new HashSet<Integer>();
											for (Node vi_in_node : n_in_nodes.get(i).in) 
												vi_in.add(vi_in_node.id);
											List<Node> vj_in = n_in_nodes.get(j).in;
											for (Node vj_in_node : vj_in){
												if (vi_in.contains(vj_in_node.id)){
													/* subtract the similarity scores of non-first-meeting nodes two hops away*/
													increment -= Math.pow(Config.alpha,2)*rea/Math.pow(n_in_degree,2)*2/vi_in.size()/vj_in.size();
												}
											}

										}
									}
								}
							}
						}
						int nodeidx1 = graph.getNode(n1).index;
						int nodeidx2 = graph.getNode(n2).index;
						int larger_idx = nodeidx1 > nodeidx2 ? nodeidx1 : nodeidx2;
						int smaller_idx = nodeidx1 < nodeidx2 ? nodeidx1 : nodeidx2;
						/* generate the unique index for node pairs to fit in the array*/
						int index = larger_idx*(larger_idx-1)/2+smaller_idx; 
						Map<Integer, Double> smallerIdx_map = scores.get(smaller_idx);
						if (smallerIdx_map == null){
							smallerIdx_map = new HashMap<Integer, Double>();
							smallerIdx_map.put(larger_idx, increment);
							scores.put(smaller_idx, smallerIdx_map);
						}
						else{
							Double old_val = smallerIdx_map.get(larger_idx);
							if (old_val != null){
								increment += old_val;
							}
							smallerIdx_map.put(larger_idx, increment);
						}
						
//						if (result[index] == null) //
//							result[index] = new nodePairVal(n1, n2, increment);
//						else
//							result[index].val += increment;
						//						count++;
						//System.out.println(idx1+" "+idx2);
					}
				}

			}
		
		}
		for ( Integer smallerIdx : scores.keySet()){
			
			Map<Integer, Double> smallerIdx_map = scores.get(smallerIdx);
//			System.out.print("smallerID = "+smallerId+" ;");
				for (Integer largerIdx : smallerIdx_map.keySet()){
					double val = smallerIdx_map.get(largerIdx);
//					System.out.print(largerId+" ");
					if (result.size() < k){
						result.add(new nodePairVal(smallerIdx, largerIdx, val));
					}
					else if (val > result.peek().val){
						result.poll();
						result.add(new nodePairVal(smallerIdx, largerIdx, val));
					}
					
				}
			System.out.println();
		}
		return result;
//		Arrays.sort(result, new Comparator<nodePairVal>() {
//			@Override
//			public int compare(nodePairVal o1, nodePairVal o2) {
//				double v1;
//				if (o1 == null)
//					v1 = 0;
//				else
//					v1 = o1.val;
//				double v2;
//				if (o2 == null)
//					v2 = 0;
//				else
//					v2 = o2.val;
//
//				return Double.compare(v2, v1);
//			}
//		});
//		return result;
//
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
