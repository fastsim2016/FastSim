package fastSim.core;


import java.util.HashMap;
import java.util.Map;

import fanweizhu.fastSim.data.Graph;
import fanweizhu.fastSim.data.Node;
import fastSim.util.Config;



public class Simrank {
	protected Graph g;
	private double[][] simrankMatrix;
	private double c=Config.alpha;
	private final int MAX_ITER = 30;
	
	public Simrank (Graph graph) {
		this.g = graph;
		simrankMatrix = new double[graph.numNodes()][graph.numNodes()];
		//System.out.println(simrankMatrix.length);
		double[][] simrank2 = new double[graph.numNodes()][graph.numNodes()];
		for ( int indenti = 0 ; indenti < graph.numNodes() ; indenti++ ) { 
			//System.out.println("initialize simrank matrix with s["+i+"]=1");
			setSimrank(simrankMatrix,indenti,indenti,1.0); 
			setSimrank(simrank2,indenti,indenti,1.0); 
			}
//		System.out.println("simrank");
//		printMatrix(simrankMatrix);
//		System.out.println("temp");
//		printMatrix(simrank2);
		for ( int step=0; step < MAX_ITER && MAX_ITER > 0; step++ ) {
			//double maxDelta = Double.MIN_VALUE;
		//	System.out.println("Iteration: "+ step);
//			System.out.println("simrank");
//			printMatrix(simrankMatrix);
//			System.out.println("temp");
//			printMatrix(simrank2);
//			
			for (int i=0; i< graph.numNodes(); i++){
				Node ni = graph.getNodeByIndex(i);
				for (int j=0; j< i; j++){
					Node nj = graph.getNodeByIndex(j);
					if(ni.in.size()==0||nj.in.size()==0||i==j) continue;
					double sumOfneighs = 0.0;
					for(Node inNeiI:ni.in){
						int neiIndexI = inNeiI.index;
						for (Node inNeiJ:nj.in){
							int neiIndexJ = inNeiJ.index;
							sumOfneighs += getSimrank(simrankMatrix,neiIndexI,neiIndexJ);	
						}
					}
					if(sumOfneighs !=0){
						setSimrank(simrank2,i,j,sumOfneighs*c/nj.in.size()/ni.in.size());
						
					}
				}
				
			}
			simrankMatrix = simrank2.clone();
			simrank2 = new double[graph.numNodes()][graph.numNodes()];
		}
				

		
 	}

	private void printMatrix(double[][] simrankMatrix2) {
		// TODO Auto-generated method stub
		
		for (int row=0; row<simrankMatrix2.length;row++){
			for (int col = 0; col<simrankMatrix2.length;col++)
				System.out.print(simrankMatrix2[row][col]+" ");
			System.out.println();
		}
		
	}

	private static double getSimrank(double[][] simrank, int i, int j) {
		// TODO Auto-generated method stub
		if (i > j) 
			return simrank[i][j];
		else if (j > i) 
			return simrank[j][i];
		else
			return 1;
		
	}

	private void setSimrank(double[][] simrank, int i, int j, double d) {
		// TODO Auto-generated method stub
		if(i > j)
			simrank[i][j] = d;
		else if(j>i)
			simrank[j][i] =d;
		else
			simrank[i][j] = 1.0;
	}

//	public static void main(String[] args) throws Exception{
//		int queryNode = Integer.valueOf(args[0]);
//		int anotherNode = Integer.valueOf(args[1]);
//		Graph g = new Graph();
//		g.loadFromFile(Config.nodeFile, Config.edgeFile, false);
//		Simrank sr = new Simrank(g);
//		double s_ij = getSimrank(sr.simrankMatrix,g.getNode(queryNode).index,g.getNode(anotherNode).index);
//		System.out.println("simrank("+ queryNode+","+anotherNode+")="+s_ij);
//		
//	}

	public Map<Integer, Double> computeResult(int qid) {
		// TODO Auto-generated method stub
		Map<Integer, Double> resultMap  = new HashMap<Integer, Double>();
		//System.out.println("Line 40: g.numNodes = " + g.numNodes());
		double[] result = new double[g.numNodes()];
		System.out.println("size: "+ result.length);
		int qIndex = g.getNode(qid).index;
		for (int i = 0; i < result.length; i++) {
			int nodeId = g.getNodeByIndex(i).id;
			double score = getSimrank(this.simrankMatrix,qIndex, i);
			resultMap.put(nodeId,score);
		}		
		
		return resultMap;
		}
	
}
