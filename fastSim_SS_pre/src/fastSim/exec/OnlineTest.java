
package fastSim.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fastSim.core.*;
import fastSim.data.*;
import fastSim.util.*;
import fastSim.util.io.TextReader;
import fastSim.util.io.TextWriter;


public class OnlineTest {
	public static void main(String args[]) throws Exception {
		//init parameters
		Config.hubType = args[0];
    	Config.numHubs = Integer.parseInt(args[1]);
    	Config.eta = Integer.parseInt(args[2]);
    	int qid = Integer.parseInt(args[3]);
    	Graph graph = new Graph();
        graph.loadFromFile(Config.nodeFile, Config.edgeFile, true);
        
        System.out.println("Starting query processing...");
        QueryProcessor2 qp = new QueryProcessor2(graph);
        Node q = graph.getNode(qid);
       
            
            //List<KeyValuePair> rankedResult = null;
            Map<Integer,Double> result = new HashMap<Integer,Double>(); //actually not ranked
            long start = System.currentTimeMillis();
          
            	result = qp.query(q);
            	System.out.println("result size: " + result.size());
                       
            long elapsed = (System.currentTimeMillis() - start) ;
            List<Entry<Integer, Double>> rankedResult = MapUtil.sortMap(result,Config.resultTop);
            
            System.out.println("Time: " + elapsed);
            for (Map.Entry<Integer, Double> e  : rankedResult)
            	System.out.println(e.getKey() + " " + e.getValue());
      
        
	}
}
