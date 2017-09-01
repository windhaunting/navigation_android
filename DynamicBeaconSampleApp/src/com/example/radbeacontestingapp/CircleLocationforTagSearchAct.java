package com.example.radbeacontestingapp;

import android.util.Log;
import android.util.SparseArray;

public class CircleLocationforTagSearchAct {
		
		public SparseArray<double[]> tagsCoordMap;

	  //Get the final located point of tablet  
	    public GePoint GetResultGeoFinalPoint()
	    {
	    	double Gex0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][1];
			double Gey0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][2];
			
			GePoint geptOrigin = new GePoint(Gex0,Gey0);
			
			PlanePoint tabletPlanePoint = GetResultPlanePoint();
			
	    	GePoint GeFinalRes = GeoPlaneCoordinateConversion.GetGeoCoordinateWithBase(geptOrigin, tabletPlanePoint);
	    	
	    	Log.e("GetResultGeoFinalPoint getLatitude=  ",""+GeFinalRes.getLatitude());
			Log.e("GetResultGeoFinalPoint getLongtitude=  ",""+GeFinalRes.getLongtitude());
			
	    	return GeFinalRes;
	    	 
	    }	    
	    
	  //Get the final located point of tablet  
	    public PlanePoint GetResultPlanePoint()
	    {
	    	double[][] resOneTwo = new double[2][2];
	    	
	    	double Gex0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][1];
			double Gey0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][2];
			double r0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][3];
			double Gex1 = ShowGoogleMapActivity.CalculatedtagInfoArray[1][1];
			double Gey1 = ShowGoogleMapActivity.CalculatedtagInfoArray[1][2];
			double r1 = ShowGoogleMapActivity.CalculatedtagInfoArray[1][3];
			//first calculate the first two circle's intersection
			
			double x0 = 0;
			double y0 = 0;
			GePoint geptOrigin = new GePoint(Gex0,Gey0);	
			GePoint geptSec1 = new GePoint(Gex1,Gey1);
			PlanePoint planeSec1 = GeoPlaneCoordinateConversion.GetNewPlaneCoordinateWithBase(geptOrigin, geptSec1);
			
			double x1 = planeSec1.getPlanex();
			double y1 = planeSec1.getPlaney();
				
			
			resOneTwo = CalculateInterPoints(x0, y0, r0, x1, y1, r1);
			
			/*PlanePoint planeres_test = TriangleCenterPoint.GetResultTriangleCenter();
			return planeres_test;
			*/
			
			//If two circle have no intersection, using triangle point to locate
			if((Double.isNaN(resOneTwo[0][0]) && Double.isNaN(resOneTwo[0][1])) || (Double.isNaN(resOneTwo[1][0]) && Double.isNaN(resOneTwo[1][1])))
			{
				PlanePoint planeres  = TriangleCenterPoint.GetResultTriangleCenter();
				
				Log.e("GetResultPlanePoint GetIntersectionPoints ", "isNaN");
				if(Double.isNaN(planeres.getPlanex()) || Double.isNaN(planeres.getPlaney()))
				{
					
					double d = GeoPlaneCoordinateConversion.DistanceBetweenGePoints(geptOrigin, geptSec1);
					double ratio = (d-r1+r0)/(2*d);
					
					double x_new = (x0 + x1) *ratio;
					double y_new = (y0 + y1) *ratio;
					
					return new PlanePoint(x_new,y_new);
				}
				
				return planeres;
			}
				
			PlanePoint PlaneFinalRes = GetProximateFinalPoint(resOneTwo);
	    	
	    	return PlaneFinalRes;
	    	
	    }
	    
	    
		//If the first two circles intersects, consider the third circle's intersection or not
		public PlanePoint GetProximateFinalPoint(double[][] IntersecRes)
		{
			PlanePoint finalRes;    // the final result of coordinate (x,y)
			
			Log.e("CircleIntersecPointCalculate ", "GetProximateFinalPoint started");
			
			double Gex0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][1];
			double Gey0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][2];
			double r0 = ShowGoogleMapActivity.CalculatedtagInfoArray[0][3];
			double Gex2 = ShowGoogleMapActivity.CalculatedtagInfoArray[2][1];
			double Gey2 = ShowGoogleMapActivity.CalculatedtagInfoArray[2][2];
			double r2 = ShowGoogleMapActivity.CalculatedtagInfoArray[2][3];

			
			GePoint geptOrigin = new GePoint(Gex0,Gey0);	
			GePoint geptThird = new GePoint(Gex2,Gey2);
			PlanePoint planeThird = GeoPlaneCoordinateConversion.GetNewPlaneCoordinateWithBase(geptOrigin, geptThird);
			
			//Log.d("Gex2= ", ""+Gex2);
			//Log.d("Gex2= ", ""+Gex2);
			double x0 = 0;
			double y0 = 0;
			
			double x2 = planeThird.getPlanex();
			double y2 = planeThird.getPlaney();
			
			//Log.d("xy 2 key= ", ""+ShowGoogleMapActivity.CalculatedtagInfoArray[2][0]);
			//Log.d("x2= ", ""+x2);
			//Log.d("y2= ", ""+y2);
			double[][] threePointofCircles = new double[3][2];

			double[][] resOneThree = CalculateInterPoints(x0, y0, r0, x2, y2, r2);
			
			//the first and third circle has no intersection,
			if((Double.isNaN(resOneThree[0][0]) && Double.isNaN(resOneThree[0][1])) || (Double.isNaN(resOneThree[1][0]) && Double.isNaN(resOneThree[1][1])))
			{
				double d1 = Math.sqrt( (IntersecRes[0][0]-x2) * (IntersecRes[0][0]-x2) + (IntersecRes[0][1]-y2) * (IntersecRes[0][1]-y2));     // the distance with the third nearest tags
				double d2 = Math.sqrt( (IntersecRes[1][0]-x2) * (IntersecRes[1][0]-x2) + (IntersecRes[1][1]-y2) * (IntersecRes[1][1]-y2));     // the distance with the third nearest tags
				
				double finalresx = 0;
				double finalresy = 0;
				if(d1 <= d2)
				{
					finalresx = IntersecRes[0][0];
					finalresy = IntersecRes[0][1];
				}
				else
				{
					finalresx = IntersecRes[1][0];
					finalresy = IntersecRes[1][1];
				}
			
				finalRes = new PlanePoint(finalresx,finalresy);
				
				Log.e("CircleIntersecPointCalculate ", " resOneThree");
				return finalRes;
			}
			
			double d1 = Math.sqrt( (IntersecRes[0][0]-x2) * (IntersecRes[0][0]-x2) + (IntersecRes[0][1]-y2) * (IntersecRes[0][1]-y2));     // the distance with the third nearest tags
			double d2 = Math.sqrt( (IntersecRes[1][0]-x2) * (IntersecRes[1][0]-x2) + (IntersecRes[1][1]-y2) * (IntersecRes[1][1]-y2));     // the distance with the third nearest tags
			
			if(d1 <= d2)
			{
				threePointofCircles[0][0] = IntersecRes[0][0];
				threePointofCircles[0][1] = IntersecRes[0][1];
			}
			else
			{
				threePointofCircles[0][0] = IntersecRes[1][0];
				threePointofCircles[0][1] = IntersecRes[1][1];
			}
			
			
			//the first and third circle has intersection, then consider the intersection point is in the third circle or not
			double Gex1 = ShowGoogleMapActivity.CalculatedtagInfoArray[1][1];
			double Gey1 = ShowGoogleMapActivity.CalculatedtagInfoArray[1][2];
			double r1 = ShowGoogleMapActivity.CalculatedtagInfoArray[1][3];

			GePoint geptSec1 = new GePoint(Gex1,Gey1);
			PlanePoint planeSec1 = GeoPlaneCoordinateConversion.GetNewPlaneCoordinateWithBase(geptOrigin, geptSec1);
			
			double x1 = planeSec1.getPlanex();
			double y1 = planeSec1.getPlaney();
			
			double dis1 = Math.sqrt( (resOneThree[0][0]-x1) * (resOneThree[0][0]-x1) + (resOneThree[0][1]-y1) * (resOneThree[0][1]-y1));     // the distance with the second tag
			double dis2 = Math.sqrt( (resOneThree[1][0]-x1) * (resOneThree[1][0]-x1) + (resOneThree[1][1]-y1) * (resOneThree[1][1]-y1));     // the distance with the second tag
			
			
			if(dis1 <= r2 && dis2 > r2)
			{
				threePointofCircles[1][0] =  resOneThree[0][0];
				threePointofCircles[1][1] =  resOneThree[0][1];
			}
			else if(dis2 <= r2 && dis1 > r2)
			{
				threePointofCircles[1][0] =  resOneThree[1][0];
				threePointofCircles[1][1] =  resOneThree[1][1];
			}
			else
			{
				
				 d1 = Math.sqrt( (IntersecRes[0][0]-x2) * (IntersecRes[0][0]-x2) + (IntersecRes[0][1]-y2) * (IntersecRes[0][1]-y2));     // the distance with the third nearest tags
				 d2 = Math.sqrt( (IntersecRes[1][0]-x2) * (IntersecRes[1][0]-x2) + (IntersecRes[1][1]-y2) * (IntersecRes[1][1]-y2));     // the distance with the third nearest tags
				
				double finalresx = 0;
				double finalresy = 0;
				if(d1 <= d2)
				{
					finalresx = IntersecRes[0][0];
					finalresy = IntersecRes[0][1];
				}
				else
				{
					finalresx = IntersecRes[1][0];
					finalresy = IntersecRes[1][1];
				}
			
				finalRes = new PlanePoint(finalresx,finalresy);
				
				Log.e("CircleIntersecPointCalculate ", " resOneThree2");
				return finalRes;
			}
			
			
			//consider the second and third circle intersection
			double[][] resTwoThree = CalculateInterPoints(x1, y1, r1, x2, y2, r2);

			//the first and third circle has no intersection,
			if((Double.isNaN(resTwoThree[0][0]) && Double.isNaN(resTwoThree[0][1])) || (Double.isNaN(resTwoThree[1][0]) && Double.isNaN(resTwoThree[1][1])))
			{
				 d1 = Math.sqrt( (IntersecRes[0][0]-x2) * (IntersecRes[0][0]-x2) + (IntersecRes[0][1]-y2) * (IntersecRes[0][1]-y2));     // the distance with the third nearest tags
				 d2 = Math.sqrt( (IntersecRes[1][0]-x2) * (IntersecRes[1][0]-x2) + (IntersecRes[1][1]-y2) * (IntersecRes[1][1]-y2));     // the distance with the third nearest tags
				
				double finalresx = 0;
				double finalresy = 0;
				if(d1 <= d2)
				{
					finalresx = IntersecRes[0][0];
					finalresy = IntersecRes[0][1];
				}
				else
				{
					finalresx = IntersecRes[1][0];
					finalresy = IntersecRes[1][1];
				}
			
				finalRes = new PlanePoint(finalresx,finalresy);
				Log.e("CircleIntersecPointCalculate ", " resTwoThree");

				return finalRes;
			}
			
			
			 dis1 = Math.sqrt( (resTwoThree[0][0]-x0) * (resTwoThree[0][0]-x0) + (resTwoThree[0][1]-y0) * (resTwoThree[0][1]-y0));     // the distance with the second tag
			 dis2 = Math.sqrt( (resTwoThree[1][0]-x0) * (resTwoThree[1][0]-x0) + (resTwoThree[1][1]-y0) * (resTwoThree[1][1]-y0));     // the distance with the second tag
			
			//double[][] threePointofCircles = new double[3][2];
			
			if(dis1 <= r0 && dis2 > r0)
			{
				threePointofCircles[2][0] =  resTwoThree[0][0];
				threePointofCircles[2][1] =  resTwoThree[0][1];
			}
			else if(dis2 <= r0 && dis1 > r0)
			{
				threePointofCircles[2][0] =  resTwoThree[1][0];
				threePointofCircles[2][1] =  resTwoThree[1][1];
			}
			else
			{
				
				 d1 = Math.sqrt( (IntersecRes[0][0]-x2) * (IntersecRes[0][0]-x2) + (IntersecRes[0][1]-y2) * (IntersecRes[0][1]-y2));     // the distance with the third nearest tags
				 d2 = Math.sqrt( (IntersecRes[1][0]-x2) * (IntersecRes[1][0]-x2) + (IntersecRes[1][1]-y2) * (IntersecRes[1][1]-y2));     // the distance with the third nearest tags
				
				double finalresx = 0;
				double finalresy = 0;
				if(d1 <= d2)
				{
					finalresx = IntersecRes[0][0];
					finalresy = IntersecRes[0][1];
				}
				else
				{
					finalresx = IntersecRes[1][0];
					finalresy = IntersecRes[1][1];
				}
			
				finalRes = new PlanePoint(finalresx,finalresy);
				
				Log.e("CircleIntersecPointCalculate ", " resTwoThree2");
				return finalRes;
			}
			
			double finalresx = (threePointofCircles[0][0] +  threePointofCircles[1][0] +  threePointofCircles[2][0])/3;
			double finalresy = (threePointofCircles[0][1] +  threePointofCircles[1][1] +  threePointofCircles[2][1])/3;
			
			Log.e("GetProximateFinalPoint finalresx=  ",""+finalresx);
			Log.e("GetProximateFinalPoint finalresy=  ",""+finalresy);
			
			finalRes = new PlanePoint(finalresx,finalresy);

			return finalRes;
			
		}
		
	    
		public double[][] CalculateInterPoints(double x_a, double y_a, double r_a, double x_b, double y_b, double r_b)
	    {
			double x0 = x_a;
		    double y0 = y_a;
		    double r0 = r_a;
		    double x1 = x_b;
		    double y1 = y_b;
		    double r1 = r_b;
		    
			double[][] res = new double[2][2]; 
			
	        int flag = 0;
	        if(x0 == x1)
	        {
	        	double tmp = x0;
	            x0= y0;
	            y0 =tmp;
	            
	             tmp = x1;
	            x1= y1;
	            y1 =tmp;
	            
	            
	           // swap(x0,y0);
	           // swap(x1, y1);
	            flag = 1;
	        }
	        
	        if(x1 < x0)
	        {
	        	double tmp = x0;
	            x0= x1;
	            x1 =tmp;
	            
	            tmp = y1;
	            y1 =y0;
	            y0 = tmp;
	            
	            tmp = r0;
	            r0 = r1;
	            r1 = tmp;
	            
	           // swap(x0,x1);
	           // swap(y0,y1);
	           // swap(r0,r1);
	        }
	        
	        x1 = x1 - x0;
	        y1 = y1 - y0;
	        
	        double alpha1 = Math.atan(y1/x1);
	        x1 = Math.sqrt(x1*x1+y1*y1);
	        y1 = 0;
	        
	        double xres = (x1*x1 - r1*r1 + r0*r0)/(2*x1);
	        
	        double yres1 = (4*x1*x1*r0*r0-(x1*x1-r1*r1+r0*r0)*(x1*x1-r1*r1+r0*r0))/(4*x1*x1);
	        yres1 = Math.sqrt(yres1);
	        
	        double yres2 = -yres1;
	        
	        double alpha2 = Math.atan(yres1/xres);
	        
	        double alpha3 = Math.atan (yres2/xres);
	        
	        double alpha12 = alpha1 + alpha2;
	        double alpha13 = alpha1 + alpha3;
	        
	        double xres1 = r0 * Math.cos(alpha12);
	        
	        yres1 = r0 * Math.sin(alpha12);
	        
	        double xres2 = r0* Math.cos(alpha13);
	                
	        yres2 = r0*Math.sin(alpha13);
	        
	        xres1 = xres1 + x0;
	        
	        yres1 = yres1 + y0;
	        
	        xres2 = xres2 + x0;
	        
	        yres2 = yres2 + y0;
	        
	        if (1 == flag)
	        {
	        	 double tmp = xres1;
	             xres1 =yres1;
	             yres1 = tmp;
	             
	             tmp = xres2;
	             xres2 = yres2;
	             yres2 = tmp;
	             
	           // swap(xres1,yres1);
	           // swap(xres2,yres2);
	        }
	        
	        res[0][0] = xres1;
	        res[0][1] = yres1;
	        res[1][0] = xres2;
	        res[1][1] = yres2;
	        
	        return res;
	        
	    }
		
	
}
