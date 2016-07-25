package fastSim.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import fastSim.core.*;
import fastSim.data.*;
import fastSim.util.*;
import fastSim.util.io.TextReader;
import fastSim.util.io.TextWriter;


public class Online {
	public static void main(String args[]) throws Exception {
		//init parameters
		Config.hubType = args[0];
		Config.numHubs = Integer.parseInt(args[1]);
		Config.depth = Integer.parseInt(args[2]);
    	Config.delta = Double.parseDouble(args[3]);
    	Config.stopRea = Double.parseDouble(args[4]);
    	Config.eta = Integer.parseInt(args[5]);
    	

    	Graph graph = new Graph();
        graph.loadFromFile(Config.nodeFile, Config.edgeFile, true);
        //load queries
        System.out.println("Loading queries...");
        List<Node[]> qNodes = new ArrayList<Node[]>();
        TextReader in = new TextReader(Config.queryFile);
        String line;
     //   int i=0;
        while ( (line = in.readln()) != null) {
    //    	i++;
    //    	if (i%2 ==0)
    //    		System.out.println(line);
        	String[] parts = line.split("\t");
        	
			int a = Integer.parseInt(parts[0]);
			//System.out.println(a);
			int b = Integer.parseInt(parts[1]);
		//	System.out.println(b);
			qNodes.add(new Node[]{graph.getNode(a),graph.getNode(b)});
        }
        
        in.close();
        System.out.println("Starting query processing...");
        QueryProcessor qp = new QueryProcessor(graph);
        TextWriter out = new TextWriter(Config.outputDir + "/" + 
        		"fastSingleP_Precomputation_NoCor" + Config.hubType + "_H" + Config.numHubs + "_Depth"+Config.depth +"_theta"+ Config.stopRea+"_delta" + Config.delta +"_OfflineClip"+Config.clip+"_eta"+Config.eta);
        int count = 0;
      //  double[] result_array = new double[qNodes.size()];
        for (Node[] qPair : qNodes) {
            count++;
            if (count % 10 == 0)
            	System.out.print("+");
            
            graph.resetPrimeG(); //otherwise, node.isVisited would be affected by previous queries
           
       //     List<Entry<Integer, Double>> rankedResult = null;
            

       
           
            long start = System.currentTimeMillis();
            double result = qp.query(qPair[0], qPair[1]);
 
      //      System.out.println(result);
     //       result_array[count] = result;
         
            long elapsed = (System.currentTimeMillis() - start);
            
            out.writeln(elapsed + "ms "+result);
         
        }
        out.close();
        
        System.out.println();
	}
}
