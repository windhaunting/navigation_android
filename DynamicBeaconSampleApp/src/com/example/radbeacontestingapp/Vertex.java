package com.example.radbeacontestingapp;


public class Vertex implements Comparable<Vertex>
{
	public final int id;
	public Edge[] adjacencies;
	 
	public double minDistance = Double.POSITIVE_INFINITY;
	public Vertex previous;
	public Vertex(int arg_id)
	{
		id = arg_id;
	}
	@Override
	public int compareTo(Vertex another) {
		// TODO Auto-generated method stub
		return Double.compare(minDistance, another.minDistance);
	}
	
}