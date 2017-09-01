package com.example.radbeacontestingapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

public class ShortestPath {
	
	public static void computePaths(Vertex source)
    {
        source.minDistance = 0.;
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
      	vertexQueue.add(source);

	while (!vertexQueue.isEmpty()) {
	    Vertex u = vertexQueue.poll();

            // Visit each edge exiting u
            for (Edge e : u.adjacencies)
            {
                Vertex v = e.target;
                double weight = e.weight;
                double distanceThroughU = u.minDistance + weight;
		if (distanceThroughU < v.minDistance) {
		    vertexQueue.remove(v);
		    v.minDistance = distanceThroughU ;
		    v.previous = u;
		    vertexQueue.add(v);
		}
       }
     }
  }

    public static List<Vertex> getShortestPathTo(Vertex target)
    {
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
            path.add(vertex);
        Collections.reverse(path);
        return path;
    }
    
    
    
    public static ArrayList<Vertex> addGraphNode_Method3()
    {
    	ArrayList<Vertex> vnode = new ArrayList<Vertex>(47);
    	
    	for(int i = 0; i < 47; i++)       //0 is my location point,48 is the destination points
    	{
			Vertex vi = new Vertex(i);
			vnode.add(i,vi);
    	}
    	
    	GePoint.calculate_navi_weight();
    	
 

    		vnode.get(1).adjacencies = new Edge[]{  new Edge(vnode.get(2), GePoint.naviPointWeightDis[1]),
													new Edge(vnode.get(1), 0)};
    		vnode.get(2).adjacencies = new Edge[]{  new Edge(vnode.get(1), GePoint.naviPointWeightDis[1]),
													new Edge(vnode.get(2), 0)};
    		return vnode;
    	
    }
    	
    
}
