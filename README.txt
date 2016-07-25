For single-pair and single-source, run in 3 steps:

1) Select hubs: projectName->fastSim.exec->SelectHubs.java
parameter: metric for selecting hubs
random: random selection
indeg: indegree based selection

2) Offline precomputation: projectName->fastSim.exec-> Offline.java
para1: hub selection method (eg.  indeg)
para2: # of hubs (eg. 1000)
para3: length of tours handled (eg. 5)
para4: threshold for abandoning distant nodes (eg. 1e-4)

3) Online computation: projectName->fastSim.exec-> Online.java
para1: hub selection method (eg.  indeg)
para2: # of hubs (eg. 1000)
para3: length of tours handled (eg. 5)
para4: online expansion threshold (eg. 1e-4)
para5: threshold for abandoning distant nodes when constructing prime subgraphs (eg. 1e-4)
para6: # of expansions (eg. 1)

For all-pair simrank, there is no offline precompuation. So it only needs 2 steps: 1) select hubs and 3) online computation.
