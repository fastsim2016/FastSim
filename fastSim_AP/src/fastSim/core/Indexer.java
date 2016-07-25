package fastSim.core;

import java.io.File;
import java.util.Set;

import fanweizhu.fastSim.data.Graph;
import fanweizhu.fastSim.data.Node;
import fanweizhu.fastSim.data.PrimeSim;
import fastSim.util.Config;
import fastSim.util.IndexManager;
import fastSim.util.io.TextWriter;

public class Indexer {

	public static void index(Set<Node> hubs, Graph graph, boolean forceUpdate)
			throws Exception {
		
		
		// 1. compute primeOutSimrank for hubs and record the statistics
		long storage = 0;

		long start = System.currentTimeMillis();

		StringBuilder sb = new StringBuilder();
		
		
		
		TextWriter countWriter=new TextWriter(IndexManager.getIndexPPVCountInfoFilename(true));
		countWriter.write(sb.toString());
		countWriter.close();
		
		long time = System.currentTimeMillis() - start;

		TextWriter out = new TextWriter(IndexManager.getIndexDeepDir()
				+ "outStats.txt");
		out.writeln("Space (mb): " + (storage / 1024.0 / 1024.0));
		out.writeln("Time (hr): " + (time / 1000.0 / 3600.0));
		out.close();
		
		
		// 2. compute primeOutSimrank for hubs and record the statistics
		int count2 =0;
		long storage2 = 0;
		long start2 = System.currentTimeMillis();
		StringBuilder sb2 = new StringBuilder();
		
			
			for(Node h: hubs){
				
				if (forceUpdate
						|| !(new File(IndexManager.getPrimeSimFilename(h.id,false)))
								.exists()) {
					count2++;
					if (count2 % 100 == 0)
						System.out.print("+");
					
				
				graph.resetPrimeG();
				
				PrimeSim outSim = graph.computeOutPrimeSim(h,1);
				sb2.append("HubID: "+ h.id+" "+outSim.getCountInfo()+ "  \n");
				 
				
				storage2 += outSim.computeStorageInBytes();
				
			//TO DO: save outSim to disk	
				outSim.saveToDisk(h.id,outSim,"out");
			}
		}
		
		
		TextWriter countWriter2=new TextWriter(IndexManager.getIndexPPVCountInfoFilename(false));
		countWriter2.write(sb2.toString());
		countWriter2.close();
		
		long time2 = System.currentTimeMillis() - start2;

		TextWriter out2 = new TextWriter(IndexManager.getIndexDeepDir()
				+ "inStats.txt");
		out2.writeln("Space (mb): " + (storage2 / 1024.0 / 1024.0));
		out2.writeln("Time (hr): " + (time2 / 1000.0 / 3600.0));
		out2.close();
		
		
		
		
	}
}
