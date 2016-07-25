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
		System.out.println("Updated fastSim with bug fixed with configcs");
		//init parameters
		Config.hubType = args[0];
    	Config.numHubs = Integer.parseInt(args[1]);
    	Config.depth = Integer.parseInt(args[2]);
    	Config.stopRea=Double.parseDouble(args[3]);
    	Config.eta = Integer.parseInt(args[4]);
    	
    	//Config.delta = Double.parseDouble(args[4]);
    	
    	Graph graph = new Graph();
    	if (Config.numHubs > 0)
    		graph.loadFromFile(Config.nodeFile, Config.edgeFile, true);
    	else
    		graph.loadFromFile(Config.nodeFile, Config.edgeFile, false);
        //load queries
        System.out.println("Loading queries...");
        List<Node> qNodes = new ArrayList<Node>();
        TextReader in = new TextReader(Config.queryFile);
        String line;
        while ( (line = in.readln()) != null) {
        	int id = Integer.parseInt(line);
        	qNodes.add(graph.getNode(id));
        }
        in.close();
        System.out.println("Starting query processing...");
        QueryProcessor2 qp = new QueryProcessor2(graph);
        TextWriter out = new TextWriter(Config.outputDir + "/" + 
        		"fastsimSS-Pre" +Config.hubType + "_DEP"+Config.depth + "_STP"+Config.stopRea+"_H" + Config.numHubs + "_E"+Config.eta);
        int count = 0;
        for (Node q : qNodes) {
            count++;
            if (count % 10 == 0)
            	System.out.print("+");
            
            graph.resetPrimeG(); //otherwise, node.isVisited would be affected by previous queries
           
            List<Entry<Integer, Double>> rankedResult = null;
            
            Map<Integer,Double> result = new HashMap<Integer,Double>(); //actually not ranked
       
           
            long start = System.currentTimeMillis();
//            for (int i = 0; i < Config.numRepetitions; i++) {
            	 
            	result = qp.query(q);
            long elapsed = (System.currentTimeMillis() - start);	
            	
//            }  
           // System.out.println("Unsorted: "+result);
            rankedResult = MapUtil.sortMap(result,Config.resultTop);
        //   System.out.println("sorted: "+rankedResult);
            
           
         
//            long elapsed = (System.currentTimeMillis() - start) / Config.numRepetitions;
           
            out.write(elapsed + "ms ");
           // System.out.println(elapsed + "ms ");
            for (Map.Entry<Integer, Double> e : rankedResult){
                out.write(e.getKey() + "\t" + e.getValue() + "\n");
               // System.out.print(e.getKey() + "\t" + e.getValue() + "\n");
            }
//            out.writeln();
        }
        out.close();
        
       // System.out.println();
	}
}
