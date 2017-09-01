package com.example.radbeacontestingapp;

import android.util.Log;
import android.util.SparseArray;

public class GeoPlaneCoordinateConversion {

	
	private static final double EARTH_RADIUS = 6378.137;  
	
	
	//the Distance between two geographic points  in meter
	public static double DistanceBetweenGePoints(GePoint point1, GePoint point2)
	{
		double radLat1 = point1.getLatitude()*Math.PI/180;
		double radLat2 = point2.getLatitude()*Math.PI/180;
		
		double radLong1 = point1.getLongtitude()*Math.PI/180;
		double radLong2 = point2.getLongtitude()*Math.PI/180;
		
		double dela = radLat1 - radLat2;	
		double delb = radLong1 - radLong2;
		
		double a = Math.pow(Math.sin(dela/2),2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(delb/2), 2);
	    double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
	    
	    double d = EARTH_RADIUS *c;
	    
	    d = d*1000;
		return d;		
		
	}
	
	// Assume longitudes are the same, only latitude is changed a little    X axis is vertical  direction to equator
	private static double deltaPlaneX( )
	{
		GePoint point1 = new GePoint(42.39354668258381,-72.52833187580109);
		GePoint point2 = new GePoint(42.39354668258382,-72.52833187580109);
		
		double dis = DistanceBetweenGePoints(point1,point2);
		double deltax = 0;
		
		//Log.e("deltaPlanex ", "dis= "+dis);
		
		deltax = dis/(point2.getLatitude()-point1.getLatitude());
		
		return deltax;
	}
	
	// Assume  altitudes are the same, only longitude is changed a little
	private static double deltaPlaneY( )
	{
		GePoint point1 = new GePoint(42.39354668258381,-72.52833187580109);
		GePoint point2 = new GePoint(42.39354668258381,-72.52833187580110);
		
		double dis = DistanceBetweenGePoints(point1,point2);
		
		//Log.d("deltaPlaneY ", "dis= "+dis);
		
		double deltay = 0;
			
		deltay = dis/(point2.getLongtitude()-point1.getLongtitude());
			
		return deltay;
	}
		
    // Get the newpoint relative plane coordinate based on the basePoint's plane coordinate(0,0)
	public static PlanePoint GetNewPlaneCoordinateWithBase(GePoint basePoint, GePoint newPoint)
	{
		//basePoint set as (0,0) in the plane coordinate, use this to get the plane coordinate of newPoint
		double planex = 0;
		double planey = 0;
		
		double diffLat = newPoint.getLatitude() - basePoint.getLatitude();
		
		double diffLong = newPoint.getLongtitude() - basePoint.getLongtitude();
		
		planex = diffLat * deltaPlaneX();
		
		planey = diffLong * deltaPlaneY();
		
		PlanePoint plancoord = new PlanePoint(planex,planey);
		
		return plancoord;
	}
	
	
  // Get the newpoint Geographic coordinate based on the basePoint's Geographic coordinate(0,0)
	public static GePoint GetGeoCoordinateWithBase(GePoint baseGePoint, PlanePoint newPlanePoint)
	{
		//basePoint assume as (0,0) in the plane coordinate, use this to get the geographic coordinate of newPoint
		double Gex = 0;
		double Gey = 0;
		
		double disx = newPlanePoint.getPlanex();
		double disy = newPlanePoint.getPlaney();
				
		Gex = disx * (1/deltaPlaneX()) + baseGePoint.getLatitude();
		Gey = disy * (1/deltaPlaneY()) + baseGePoint.getLongtitude();

		GePoint geCoord = new GePoint(Gex,Gey);
		
		return geCoord;
	}
	
	
}
