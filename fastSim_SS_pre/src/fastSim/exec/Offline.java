package fastSim.exec;

import fastSim.core.Indexer;
import fastSim.data.Graph;
import fastSim.util.Config;
import fastSim.util.IndexManager;

public class Offline {
	
	public static void main(String[] args) throws Exception {
	    Config.hubType = args[0];
		Config.numHubs = Integer.parseInt(args[1]);
		Config.depth = Integer.parseInt(args[2]);
		
		boolean forceUpdate = args[3].equals("1");
		
	    Graph g = new Graph();
	    if (Config.numHubs > 0)
	    	g.loadFromFile(Config.nodeFile, Config.edgeFile, true); // load nodes, edges and hubs into graph
	    else
	    	g.loadFromFile(Config.nodeFile, Config.edgeFile, false);
	    IndexManager.mkSubDirDeep(); // create directories for hubs' subgraphs
	    
	    Indexer.index(g.getHubs(), g, forceUpdate); // save hubs' subgraphs
	}
}
