package fastSim.exec;



import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import fanweizhu.fastSim.core.*;
import fanweizhu.fastSim.core.QueryProcessor2.nodePairVal;
import fanweizhu.fastSim.data.*;
import fastSim.util.*;
import fastSim.util.io.TextWriter;


public class Online {
	public static void main(String args[]) throws Exception {
		//init parameters
		Config.hubType = args[0];
		Config.numHubs = Integer.parseInt(args[1]);
		Config.eta = Integer.parseInt(args[2]);
		Config.depth = Integer.parseInt(args[3]);
    	Config.stopRea = Double.parseDouble(args[4]);
    	int k = Integer.parseInt(args[5]);
    	Config.correctionLevel = Integer.parseInt(args[6]);
		Graph graph = new Graph();
		if (Config.numHubs > 0)
    		graph.loadFromFile(Config.nodeFile, Config.edgeFile, true);
    	else
    		graph.loadFromFile(Config.nodeFile, Config.edgeFile, false);
		QueryProcessor2 qp = new QueryProcessor2(graph);
		String outfile = Config.outputDir + "/" + 
				"fs-AP-" + Config.hubType + "_k"+ k + "_H" + Config.numHubs + "_D" +Config.depth +"_CR"+Config.correctionLevel+"_E"+Config.eta
				+"_sR"+String.format("%1.0e",Config.stopRea);
		TextWriter out = new TextWriter(outfile);
		System.out.println(outfile);
		long start = System.currentTimeMillis();
		PriorityQueue<nodePairVal> H = qp.query(k);
		for (int i = 1; i < Config.numRepetitions; i++){
			 H = qp.query(k);
		}
		double elapsed = (System.currentTimeMillis() - start)/(double)Config.numRepetitions;
		out.writeln(elapsed + "ms ");
		int count = 0;
		List<nodePairVal> result = new ArrayList<nodePairVal>();
		while (H.size() > 0){
//			System.out.println(H.peek().n1+" "+H.peek().n2);
			result.add(H.poll());
		}
		for (int i = result.size() - 1; i >= 0; i--){
			nodePairVal npv = result.get(i);
			if (npv != null){
				
				out.writeln(graph.nodesIndex[npv.n1]+"\t"+graph.nodesIndex[npv.n2]+"\t"+npv.val);
//				System.out.println(graph.nodesIndex[npv.n1]+"\t"+graph.nodesIndex[npv.n2]+"\t"+npv.val);
				count++;
				if (count == k)
					break;
			}
			
		}
		out.close();
	}
}
