package com.example.radbeacontestingapp;

/*****************************
 * 
 * @author fubao
 * geodetic coordinate
 * longitude and latitude coordinates
 ***********************/
public class GePoint {
	
	public static final double[][] naviPoints =
		{{1, 42.39403447614767, -72.52864435315132},
		 {2, 42.39421845009601,-72.52875432372093},
		 {3, 42,0, -72.0}
		};
	
	//3937128299544
	//52844888716936
	
	//39371332517692
	//52844486385584
	/*
	394064932168284
	52866011112928
	
	39406666543731
	5286627933383
	*/
	
	
	public static double[] naviPointWeightDis = new double[2];
	
	public static final double[][] geodata = 
		{{1, 42.39354668258381, -72.52833187580109},
		{2, 42.39359447166392,	-72.52839054912329},
		{3, 42.393661574404135, -72.52840027213097},
		{4, 42.39370936339676, 	-72.52845861017704},
		{5, 42.39377498034809, 	-72.52846665680408},
		{6, 42.39382895952455, 	-72.52852533012629},
		{7, 42.39389135741379, 	-72.52853605896235},
		{8, 42.393932460751394, -72.52859741449356},
		{9, 42.393999315520105, -72.52860512584448},
		{10, 42.39404537098601, -72.52866346389055},
		{11, 42.394103806904866, -72.52867016941309},
		{12, 42.39417412802318, -72.52873621881008},
		{13, 42.39423256382214, -72.52873655408621},
		{14, 42.39422736402869, -72.52877611666918},
		{15, 42.39416001428392, -72.52882674336433},
		{16, 42.39402060998703, -72.52868391573429},
		{17, 42.39397727821532, -72.52871174365282}
		};
	
	   
	    private  double latitude;    	// latitude
	    private  double longtitude; 		// longitude  
	    public GePoint() {  
	          
	    }  
	    
	   public static void calculate_navi_weight()
	   {
		   double[] weight = new double[2];
		   
		   for(int i = 0; i < 1; i++)
		   {
			  
				   GePoint p1 = new GePoint(naviPoints[i][1],naviPoints[i][2]);
				   GePoint p2 = new GePoint(naviPoints[i+1][1],naviPoints[i+1][2]);
				   
				   double res = GeoPlaneCoordinateConversion.DistanceBetweenGePoints(p1, p2);
				   weight[i+1] = res;
			  
		   }
		   naviPointWeightDis = weight;
	   }
	    
	    
	    public GePoint(double latitude, double longtitude) {  
	        this.latitude = latitude;  
	        this.longtitude = longtitude;  
	    }  
	    public double getLatitude() {  
	    	return this.latitude;
	    }  
	  
	    public double getLongtitude() {  
	       return this.longtitude;
	    }  
	  

}
