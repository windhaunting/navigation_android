package com.example.radbeacontestingapp;

import android.util.Log;

public class TriangleCenterPoint {
	
	/*
	public double x0;
    public double y0;
    public double r0;
    public double x1;
    public double y1;
    public double r1;
    */
	
	//Get the final located point of tablet  
    public static PlanePoint GetResultTriangleCenter()
    {		
		double Gex0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][1];
		double Gey0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][2];
		double r0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][3];
		double Gex1 = ShowGoogleMapActivity.CalculatedtagInfoArray[1][1];
		double Gey1 = ShowGoogleMapActivity.CalculatedtagInfoArray[1][2];
		double r1 = ShowGoogleMapActivity.CalculatedtagInfoArray[1][3];
		
		double Gex2 = ShowGoogleMapActivity.CalculatedtagInfoArray[2][1];
		double Gey2 = ShowGoogleMapActivity.CalculatedtagInfoArray[2][2];
		double r2 = ShowGoogleMapActivity.CalculatedtagInfoArray[1][3];
		
		//Log.d("GetIntersectionPoints Gexy0 r1=  ",""+r1);
		
		//Assume x0,y0 as the original point(0,0)
		double x0 = 0;
		double y0 = 0;
		GePoint geptOrigin = new GePoint(Gex0,Gey0);	
		GePoint geptSec1 = new GePoint(Gex1,Gey1);
		PlanePoint planeSec1 = GeoPlaneCoordinateConversion.GetNewPlaneCoordinateWithBase(geptOrigin, geptSec1);
		
		double x1 = planeSec1.getPlanex();
		double y1 = planeSec1.getPlaney();
		
		GePoint geptSec2 = new GePoint(Gex2,Gey2);
		PlanePoint planeSec2 = GeoPlaneCoordinateConversion.GetNewPlaneCoordinateWithBase(geptOrigin, geptSec2);
		
		double x2 = planeSec2.getPlanex();
		double y2 = planeSec2.getPlaney();
	
		double[] centerPoint = new double[2];
		
		double[] triangle1_edge = new double[4];
		triangle1_edge[0] = r0;
		triangle1_edge[1] = r1;
		triangle1_edge[2] = GeoPlaneCoordinateConversion.DistanceBetweenGePoints(geptOrigin, geptSec1);
		if((triangle1_edge[0] + triangle1_edge[1])  == triangle1_edge[2])
		{
			centerPoint[0] = (x0+x1)/2;
			centerPoint[1] = (y0+y1)/2;
			
			PlanePoint tabletPlanePoint = new PlanePoint(centerPoint[0],centerPoint[1]);
	  
	    	return tabletPlanePoint;
		}
		
		double p_tmp = (triangle1_edge[0] + triangle1_edge[1] + triangle1_edge[2])/2;
		triangle1_edge[3] = Math.sqrt(p_tmp * (p_tmp-triangle1_edge[0]) * (p_tmp-triangle1_edge[1]) * (p_tmp-triangle1_edge[2]));        //Area;
				
		double[] triangle2_edge = new double[4];
		triangle2_edge[0] = r0;
		triangle2_edge[1] = r2;
		triangle2_edge[2] = GeoPlaneCoordinateConversion.DistanceBetweenGePoints(geptOrigin, geptSec2);
	
		if((triangle2_edge[0] + triangle2_edge[1])  == triangle2_edge[2])
		{
			centerPoint[0] = (x0+x2)/2;
			centerPoint[1] = (y0+y2)/2;
			
			PlanePoint tabletPlanePoint = new PlanePoint(centerPoint[0],centerPoint[1]);
	  
	    	return tabletPlanePoint;
		}
		
		p_tmp = (triangle2_edge[0] + triangle2_edge[1] + triangle2_edge[2])/2;
		triangle2_edge[3] = Math.sqrt(p_tmp * (p_tmp-triangle2_edge[0]) * (p_tmp-triangle2_edge[1]) * (p_tmp-triangle2_edge[2]));        //Area;
		
		double[] triangle3_edge = new double[4];
		triangle3_edge[0] = r1;
		triangle3_edge[1] = r2;
		triangle3_edge[2] = GeoPlaneCoordinateConversion.DistanceBetweenGePoints(geptSec1, geptSec2);
	
		if((triangle3_edge[0] + triangle3_edge[1])  == triangle3_edge[2])
		{
			centerPoint[0] = (x1+x2)/2;
			centerPoint[1] = (y1+y2)/2;
			
			PlanePoint tabletPlanePoint = new PlanePoint(centerPoint[0],centerPoint[1]);
	  
	    	return tabletPlanePoint;
		}
		
		//p_tmp = (triangle3_edge[0] + triangle3_edge[1] + triangle3_edge[2])/2;
		//triangle3_edge[3] = Math.sqrt(p_tmp * (p_tmp-triangle3_edge[0]) * (p_tmp-triangle3_edge[1]) * (p_tmp-triangle3_edge[2]));        //Area;
		
		
		p_tmp = (triangle1_edge[2] + triangle2_edge[2] + triangle3_edge[2]) / 2;
		double area3tag = Math.sqrt(p_tmp * (p_tmp-triangle1_edge[2]) * (p_tmp-triangle2_edge[2]) * (p_tmp-triangle3_edge[2]));        //Area;
		
		
		Log.e("GetResultTriangleCenter triangle2_edge[3] ", ""+triangle2_edge[3]);
		Log.e("GetResultTriangleCenter triangle1_edge[3] ", ""+triangle1_edge[3]);

		centerPoint[0] = (0 + triangle2_edge[3]*x1 + triangle1_edge[3]*x2) / area3tag;
		centerPoint[1] = (0 + triangle2_edge[3]*y1 + triangle1_edge[3]*y2) / area3tag;
	
		PlanePoint tabletPlanePoint = new PlanePoint(centerPoint[0],centerPoint[1]);
  
		Log.e("GetResultTriangleCenter centerPoint[0] ", ""+centerPoint[0]);
		Log.e("GetResultTriangleCenter centerPoint[1] ", ""+centerPoint[1]);
    	return tabletPlanePoint;
    }

}
