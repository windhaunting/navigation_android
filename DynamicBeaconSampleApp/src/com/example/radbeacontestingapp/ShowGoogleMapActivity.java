package com.example.radbeacontestingapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Vector;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import com.example.radbeacontestingapp.Landmarks;
import com.example.radbeacontestingapp.ShowGoogleMapActivity.MyUrlTileProvider;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.common.collect.ImmutableSet;
import com.example.radbeacontestingapp.IOnLandmarkSelectedListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ShowGoogleMapActivity extends Activity implements BeaconConsumer, IOnLandmarkSelectedListener, OnMapLongClickListener, OnInitListener, SensorEventListener{
	
	/** A set of valid Gimbal beacon identifier */
	private final Set<String> validGimbalIdentifiers = ImmutableSet.of(
			"00100001", "10011101");

	/** Log for TagSearchingActivity. */
	private static final String TAG_SEARCHING_ACTIVITY_LOG = "TAG_SEA_ACT_LOG";
	public static int average_sample_nums = 4;
	private static final int TAGS_TOTAL_NUM = 20;
	public static final String BUNDLE_RESULT_LOCATION = "resultLocation";
	public static final int TABLET_TO_CEILING_HEIGHT = 1;     //meter

	private static int count = 0;
	
	private static int clear_flag = 0;
	
	/** Declare and initiate the a BeaconManager object.*/
	private BeaconManager beaconManager = BeaconManager
			.getInstanceForApplication(this);
	private List<Beacon> discoveredBeaconList;
	protected HashMap<String, Beacon> discoveredBeaconMap;
	
	protected ArrayList< ArrayList<Double> > distListTempArray = new ArrayList<ArrayList<Double>>(TAGS_TOTAL_NUM);
	protected HashMap<Integer, ArrayList<Double>> TempDistMap= new HashMap<Integer, ArrayList<Double>>();
	public HashMap<Integer, Double> distanceMap;
	
	public static double[][] CalculatedtagInfoArray; //store the searched tag's info, using 3 tags to locate
	
	public static GePoint finalTabletGeoRes;     //final result output geo points
	private File saveFile;
	private FileOutputStream outStream;
	private int WriteOutputNums = 0;
	
	// Google Map
    private GoogleMap mMap;
    private Landmarks landmarks;
    private IOnLandmarkSelectedListener landmarkListener;
    
    private String mUrl = "http://percept.ecs.umass.edu/course/marcusbasement/{z}/{x}/{y}.png";
    public Uri imageUri;
    private Activity activity;
    
    double marcusLat = 42.393985;
	double marcusLng = -72.528622;
	int knowlesZoom = 25;
	private Marker g_markerLocation;
    
	private static int g_flag_marker_clear = 0;
	String finalResPositionTitle = "I AM HERE";
	
	// navigation use here
	private ArrayList<LatLng> navigatePoints;     //store all the point in the navigation path
	
	private double[] destPoint = new double[2];    //store the destination point
	Button NavigateButton;
	private Marker dest_marker;
	private boolean clear_dest_marker_flag = false;
	List<Polyline> g_polylines = new ArrayList<Polyline>();
	
	private static final int MY_DATA_CHECK_CODE = 0;
	private TextToSpeech myTTS;
	
	private boolean flag_after_click_navigation = false;   //Flag for dynamic update navigation
	private int startNode_key = 0;
	private int destNode_key = 3;
	
	private SensorManager mSensorManager; 
	private double g_tablet_bearing = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showgooglemap);
		
		discoveredBeaconMap = new HashMap<String, Beacon>();
		distanceMap = new HashMap<Integer, Double>();
		discoveredBeaconList = new ArrayList<Beacon>();
		CalculatedtagInfoArray = new double[3][4];
		
		finalTabletGeoRes = null;
		
		navigatePoints = new ArrayList<LatLng>();
				
		Log.d("ShowGoogleMapActivity onCreate", "load map begin");
		
		try {
            landmarkListener = (IOnLandmarkSelectedListener) this;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
		
		try {
	            // Loading map
			 //setupMap(finalResPos[0],finalResPos[1]);
			 setupMap();
	 
			// LatLng position = new LatLng(finalResPos[0],finalResPos[1]); 
			// g_marker = mMap.addMarker(new MarkerOptions().position(position).title(finalResPositionTitle).draggable(true));
			//	landmarks.addMarker(finalResPositionTitle, g_marker);
			 // GREEN color icon 
			// g_marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
	        
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

		File sdCardDir = Environment.getExternalStorageDirectory();
		saveFile = new File(sdCardDir,"OutputlocationPoints.txt");
		try{
			outStream = new FileOutputStream(saveFile);
		}catch ( IOException e ) {
		       e.printStackTrace();
		} 

		mMap.setOnMapLongClickListener(this); 
		
		NavigateButton = (Button)findViewById(R.id.btnNavigate);
		NavigateButton.setOnClickListener(myOnClickListenerNavigatePath);
			
		//check for TTS data
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        
        //get sensor service
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);  
        
      //Log.d("ShowGoogleMapActivity onCreate", "finished");

	}
	
	private void setupMap(){
		
		 //if (mMap == null) {
			 mMap = ((MapFragment) getFragmentManager().findFragmentById(
	                    R.id.map)).getMap();
	            // check if map is created successfully or not
	            if (mMap == null) {
	                Toast.makeText(getApplicationContext(),
	                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
	                        .show();
	            }
	            
	            CameraPosition cameraPosition = new CameraPosition.Builder().target(
	                    new LatLng(marcusLat, marcusLng)).zoom(knowlesZoom).build();
	     
	            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	            
			//changeMapPositionAndZoom(new LatLng(marcusLat,marcusLng), knowlesZoom);
			MyUrlTileProvider mTileProvider = new MyUrlTileProvider(256, 256, mUrl);
			mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mTileProvider).zIndex(0));
		    // display all the landmarks
			landmarks = new Landmarks();
			Log.d("ShowGoogleMapActivity setupMap", "show map here");
			InitializeMarker();
		 }

	private void InitializeMarker(){
		String marcusBeacons = "" +
		"@Dis001,42.39354668258381,-72.52833187580109," +
		"@Dis002,42.39359447166392,-72.52839054912329," +
		"@Dis003,42.393661574404135,-72.52840027213097," +
		"@Dis004,42.39370936339676,-72.52845861017704," +
		"@Dis005,42.39377498034809,-72.52846665680408," +
		"@Dis006,42.39382895952455,-72.52852533012629," +
		"@Dis007,42.39389135741379,-72.52853605896235," +
		"@Dis008,42.393932460751394,-72.52859741449356," +
		"@Dis009,42.393999315520105,-72.52860512584448," +
		"@Dis010,42.39404537098601,-72.52866346389055," +
		"@Dis011,42.394103806904866,-72.52867016941309," +
		"@Dis012,42.39417412802318,-72.52873621881008," +
		"@Dis013,42.39423256382214,-72.52873655408621," +
		"@Dis014,42.39422736402869,-72.52877611666918," +
		"@Dis015,42.39416001428392,-72.52882674336433," +
		"@Dis016,42.39402060998703,-72.52868391573429," +
		"@Dis017,42.39397727821532,-72.52871174365282,";

		String[] marcusBeaconsArray = marcusBeacons.split("@");
		for(String marcusBeacon : marcusBeaconsArray){
			
			if(marcusBeacon.equals("")){
				continue;
			}
			int titleIndex = 0;
			int latitudeIndex = 1;
			int longitutdeIndex = 2;
			
			String[] beaconComponents = marcusBeacon.split(",");
			String beaconTitle = beaconComponents[titleIndex];
			double beaconLat = Double.parseDouble(beaconComponents[latitudeIndex]);
			double beaconLong = Double.parseDouble(beaconComponents[longitutdeIndex]);
			
			LatLng position = new LatLng(beaconLat,beaconLong);
			Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(beaconTitle).draggable(true));
			landmarks.addMarker(beaconTitle, marker);

		}
	}
	
	
	public class MyUrlTileProvider extends UrlTileProvider {

		private String baseUrl;

		public MyUrlTileProvider(int width, int height, String url) {
		    super(width, height);
		    this.baseUrl = url;
		}

		@Override
		public URL getTileUrl(int x, int y, int zoom) {
		    try {
		        return new URL(baseUrl.replace("{z}", ""+zoom).replace("{x}",""+x).replace("{y}",""+y));
		    } catch (MalformedURLException e) {
		        e.printStackTrace();
		    }
		    return null;
		}
	}
	
	private void changeMapPositionAndZoom(LatLng moveToPosition, int zoomLevel){
		changeMapPosition(moveToPosition);
		changeMapZoom(zoomLevel);
	}
	
	private void changeMapPosition(LatLng moveToPosition){
		CameraUpdate center = CameraUpdateFactory.newLatLng(moveToPosition);
		mMap.moveCamera(center);
	}
	
	private void changeMapZoom(int zoomLevel){
		CameraUpdate zoom=CameraUpdateFactory.zoomTo(zoomLevel);
		mMap.animateCamera(zoom);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		beaconManager
				.getBeaconParsers()
				.add(new BeaconParser()
						.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
		beaconManager.bind(this);
		
		mSensorManager.registerListener(this,  
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),  
                SensorManager.SENSOR_DELAY_GAME); 
	}

	@Override
	protected void onPause() {
		super.onPause();
		beaconManager.unbind(this);
		mSensorManager.unregisterListener(this);  
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//beaconManager.unbind(this);
		try{
			outStream.close();
		}catch ( IOException e ) {
	        e.printStackTrace();
		}
	}

	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
 
        if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(ShowGoogleMapActivity.this, "Portrait now", Toast.LENGTH_SHORT).show();
        }
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            Toast.makeText(ShowGoogleMapActivity.this, "Landscape now ", Toast.LENGTH_SHORT).show();
        }
    }
	
	
    /**
	 * Refresh the list of beacon according to current values in the map and
	 * then notify the list UI to change.
	 */
	private void updateDiscoveredList() {
		discoveredBeaconList.clear();
		Iterator<Beacon> bIter = discoveredBeaconMap.values().iterator();
		while (bIter.hasNext()) {
			discoveredBeaconList.add(bIter.next());
		}
		runOnUiThread(new Runnable() {
			public void run() {
				//tagSearchListAdapter.notifyDataSetChanged();
				
				if((null != finalTabletGeoRes) && (clear_flag == 1))
				{
					
					LatLng position = new LatLng(finalTabletGeoRes.getLatitude(),finalTabletGeoRes.getLongtitude());
					
					//Log.d(" CalculatedtagInfoArray[0][1]xxx = ", ""+ CalculatedtagInfoArray[0][0]);
					//Log.d(" CalculatedtagInfoArray[1][1]xx2 = ", ""+ CalculatedtagInfoArray[1][0]);
					//Log.d(" finalTabletGeoRes0 = ", ""+ finalTabletGeoRes.getLatitude());
				    
					
					finalResPositionTitle = finalResPositionTitle + " use tagId: " + Integer.toString((int)CalculatedtagInfoArray[0][0]) + ", "+ Integer.toString((int)CalculatedtagInfoArray[1][0])
						+ ", " + Integer.toString((int)CalculatedtagInfoArray[2][0]);
				
					if(g_flag_marker_clear == 1)
					{
						g_markerLocation.remove();
						
					}
					
					g_markerLocation = mMap.addMarker(new MarkerOptions().position(position).title(finalResPositionTitle));
					
					
					landmarks.addMarker(finalResPositionTitle, g_markerLocation);
					// GREEN color icon 
					g_markerLocation.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
					g_markerLocation.setAlpha(1);

					finalResPositionTitle = "I AM HERE";
					g_flag_marker_clear = 1;
					
					clear_flag = 0;
					//Log.d("updateDiscoveredList runOnUiThread", "84 enter here");
					
					if(flag_after_click_navigation)
					{
						speakWords("recalculation");
						List<Vertex> path_node = UpdateNavigation_method3();
						
						try {
							speech_navigation(path_node);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}
				}
				
			}
		});
	}
	
	
	@Override
	public void onBeaconServiceConnect() {
		// TODO Auto-generated method stub
		beaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<Beacon> beacons,
					Region region) {

				if (beacons.size() > 0) {
					Log.i(TAG_SEARCHING_ACTIVITY_LOG, "Found " + beacons.size()
							+ "beacons");
					//Log.i(TAG_SEARCHING_ACTIVITY_LOG, "The RSSI of the first beacon I see is
					//"+beacons.iterator().next().getRSSI()+".");
					for (Iterator<Beacon> bIterator = beacons.iterator(); bIterator
							.hasNext();) {
						final Beacon beacon = bIterator.next();
						if (isGimbalTag(beacon)) {
							// generate the HashMap key, which is the
							// combination of tag's UUID, Major and Minor; But
							// you can always choose your own key
							/*
							final String key = new StringBuilder()
									.append(beacon.getId1())
									.append(beacon.getId2())
									.append(beacon.getId3()).toString();
							*/
							
							Log.d(" beacon id2 1333 ", " "+beacon.getId2().toString());
							//ignore student tags id2 = 1
							if (Integer.parseInt(beacon.getId2().toString()) == 1)
							{
								Log.d(" beacon id2 = 1", "ignore student tags");
								break;
							}
							
							final String key = new StringBuilder()
							        .append(beacon.getId3()).toString();
							discoveredBeaconMap.put(key, beacon);
							
							int rssival = beacon.getRssi();
							
							Log.d("first beacon value", " "+discoveredBeaconMap.values());
							
							SearchNearestPointLocation(key,rssival);

						}
					}
					updateDiscoveredList();
				}
			}
		});

		try {
			beaconManager.startRangingBeaconsInRegion(new Region(
					"myRangingUniqueId", null, null, null));
		} catch (RemoteException e) {
			Log.d(TAG_SEARCHING_ACTIVITY_LOG, e.toString());
		}
	}
    
	public void SearchNearestPointLocation(String key, int rssival)
	{

		int keyInt = Integer.parseInt(key);
		
		//Distance = -0.00903 *RSSI^2 – 2.171*RSSI – 94
		double distanceTemp = (-0.00903) * rssival * rssival - 2.171 * rssival - 94;
		
		distanceTemp  = distanceTemp *0.3048;           //feet to meter
		
		if (0 > distanceTemp)
		{
			distanceTemp = 0;
		}
		
		//remove the distance from tablet to ceiling
		if (TABLET_TO_CEILING_HEIGHT <= distanceTemp)
		{
			distanceTemp =  Math.sqrt(distanceTemp*distanceTemp - TABLET_TO_CEILING_HEIGHT*TABLET_TO_CEILING_HEIGHT);
		}
	//	else
	//	{
		//	return;
		//}

		Log.d("distance value1 ", " "+distanceTemp);

		//store the temp distance values of different key(ibeacon tags)
		if(TempDistMap.containsKey(keyInt))
		{
			Log.d("enter key= ", ""+keyInt);
			ArrayList<Double> Temp = new ArrayList<Double>();
			Temp = TempDistMap.get(keyInt);
			Temp.add(distanceTemp);
			TempDistMap.put(keyInt, Temp);
			double distanceAvg = 0;
			Log.d("distlist'size = ", " "+Temp.size());
			
			//get the first 5 numbers of distance with one tag and remove the largest and smallest ones 
			//and then calculate the average of 5 numbers
			//if(average_sample_nums == Temp.size())
			if(average_sample_nums == TempDistMap.get(keyInt).size())
			{
				Collections.sort(Temp);   // sort for removing the smallest one and largest one and calculate average
				
				//Iterator<Double> it = Temp.iterator();
				Iterator<Double> it = TempDistMap.get(keyInt).iterator();
				double sum = 0;
				boolean flag = true;
				double dis = 0;
				while(it.hasNext())
				{
					if(true == flag)          //remove the smallest one
					{
						flag = false;
						continue;
					}
					
				     dis = it.next();
				    sum += dis;
				}
				sum = sum - dis;  //remove the largest one
				
				distanceAvg = sum/(average_sample_nums-2);
				
				//Log.d("keyInt= ", ""+keyInt);
				//Log.d("distanceAvg= ", " "+distanceAvg);
				distanceMap.put(keyInt, distanceAvg);

				//output sorted tag's key and distance from the nearest distance to larger distance
				//calculate the minimum three tags to locate
				if (3 <= distanceMap.size())   //At least two tags needed to locate
				{
					//DistanceSortMapByValue sortValueDistanceMap = new DistanceSortMapByValue();
					distanceMap = sortByComparator(distanceMap, true);   //natural order
					
					int calNum = 0;
					
					//Log.d("calNum =",""+calNum);
					//Log.d("distanceMap.size= "," "+distanceMap.size());
					for (Map.Entry<Integer, Double> entry : distanceMap.entrySet()) 
					{
						if(calNum >= 3)
						{
						    break;
						}
					    Integer keyId= entry.getKey();
					    Double  Dist = entry.getValue();

					    //lookup the tag's coordinates according to the keyIter
					    double gex = GePoint.geodata[keyId-1][1];
					    double gey = GePoint.geodata[keyId-1][2];
					    
					    Log.d("keyIte219 = ", ""+keyId);
					    Log.d("gex = ", ""+gex);
					    Log.d("gey = ", ""+gey);
					    Log.d("DistIer = ", ""+Dist);
					    
					    //select the tags with minimum distance						    
					    CalculatedtagInfoArray[calNum][0] = keyId;    //key
					    CalculatedtagInfoArray[calNum][1] = gex;  	  //x
					    CalculatedtagInfoArray[calNum][2] = gey;      //y
					    CalculatedtagInfoArray[calNum][3] = Dist;     //distance with tablet, meter
 
					    Log.d("calNum2= ",""+calNum);
					    
					    calNum++;
					}

					//locate using circle
					Log.d(" CalculatedtagInfoArray[0][1]key0 = ", ""+ CalculatedtagInfoArray[0][0]);
					Log.d(" CalculatedtagInfoArray[1][1]key1 = ", ""+ CalculatedtagInfoArray[1][0]);
					Log.d(" CalculatedtagInfoArray[1][1]key2 = ", ""+ CalculatedtagInfoArray[2][0]);
				    CircleLocationforTagSearchAct circlePoint = new CircleLocationforTagSearchAct();
					
					finalTabletGeoRes =  circlePoint.GetResultGeoFinalPoint();
					
					Log.d("finalTabletGeoRes x= ", ""+finalTabletGeoRes.getLatitude());
					Log.d("finalTabletGeoRes y= ", ""+finalTabletGeoRes.getLongtitude());

					String OutputXYPoints = Double.toString(finalTabletGeoRes.getLatitude()) + ", " + Double.toString(finalTabletGeoRes.getLongtitude());
										
					OutputXYPoints = Integer.toString(++WriteOutputNums)+":   "+OutputXYPoints+"	  ";
					WriteFileToSDCard(OutputXYPoints);
				
					//clear searched tags information 
					Temp.clear();			
					TempDistMap.clear();
					distListTempArray.clear();
					count = 0;
					discoveredBeaconMap.clear();
					clear_flag = 1;
					//discoveredBeaconList.clear();
					Log.d("Clear Temp and distace Temp Here 333 = ", "enter here 552");
	
				}
				
			}		
		}
		else
		{
			Log.d("enter key2= ", ""+keyInt);
			ArrayList<Double> distListTmp = new ArrayList<Double>();
			distListTmp.add(distanceTemp);
			distListTempArray.add(count, distListTmp);	
			
			TempDistMap.put(keyInt, distListTempArray.get(count++));

		}
	}
	
	private HashMap<Integer, Double> sortByComparator(HashMap<Integer, Double> unsortMap, final boolean order)
    {

        List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Entry<Integer, Double>>()
        {
            public int compare(Entry<Integer, Double> o1,
                    Entry<Integer, Double> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
        for (Entry<Integer, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
	
	private void WriteFileToSDCard(String text)
	{
        try {
        	
        	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        	{
	        	outStream.write(text.getBytes());	
	        	//Log.d("WriteFileToSDCard = ", "finished");
        	}
        } catch ( IOException e ) {
           e.printStackTrace();
        }
	}

	@Override
	public void onLandmarkSelected(Marker landmark) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onModeChange() {
		// TODO Auto-generated method stub
		
	}
	
	private void addNagivateTrack(double lat, double lng){

		 if (navigatePoints == null) {
			 navigatePoints = new ArrayList<LatLng>();
		}
		 navigatePoints.add(new LatLng(lat, lng));

		 PolylineOptions polylineOpt = new PolylineOptions();
		 for (LatLng latlng : navigatePoints) {
		  polylineOpt.add(latlng);
		 }

		 polylineOpt.color(Color.RED);

		 Polyline line = mMap.addPolyline(polylineOpt);
		 g_polylines.add(line);
		 
		 line.setWidth(5);
     }	
	
	private void test_for_gettingPoint( )
	{
		/*
		for(int i = 0; i <= 13; i=i+2)
		{
			addNagivateTrack(GePoint.geodata[i][1], GePoint.geodata[i][2]);
			
		}
		
		for(int i = 1; i < 14; i=i+2)
		{
			addNagivateTrack(GePoint.geodata[i][1], GePoint.geodata[i][2]);
			
		}
		
		*/
		for(int i = 0; i < 2; i=i+1)
		{
			addNagivateTrack(GePoint.naviPoints[i][1], GePoint.naviPoints[i][2]);
			
		}
		
	}

	@Override    
	public void onMapLongClick(LatLng point) {
		
		if(clear_dest_marker_flag)
		{
			dest_marker.remove();
			
		}
		
		dest_marker = mMap.addMarker(new MarkerOptions()
	        .position(point)
	        .title("Destination")           
	        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
		clear_dest_marker_flag = true;
		
		destPoint[0] = point.latitude;
		destPoint[1] = point.longitude;
		
		GePoint.naviPoints[destNode_key-1][1] = point.latitude;
		GePoint.naviPoints[destNode_key-1][2] = point.longitude;
		
		Log.e("destPoint[0]= ", ""+destPoint[0]);
		Log.e("destPoint[1]= ", ""+destPoint[1]);
	
		
	}
	
	public List<Vertex> UpdateNavigation_method3()
	{
		if(!navigatePoints.isEmpty())
		{
			navigatePoints.clear();
		}
		
		if(0 != g_polylines.size())
		{
			for(Polyline line : g_polylines)
			{
			    line.remove();
			}
			
			//Log.e("myOnClickListenerNavigatePath g_line", "remove");
			
		}
		
		double x_end = destPoint[0];
		double y_end = destPoint[1];
		
		double x_start = 0;
		double y_start = 0;
		if(null != finalTabletGeoRes)
		{
		
			//x_start = finalTabletGeoRes.getLatitude();
			//y_start = finalTabletGeoRes.getLongtitude();				
			//addNagivateTrack(x_start, y_start);

			//to be deleted, for debugging only
			x_start = 42.39398227821532;
			y_start = -72.52868691573429;
			addNagivateTrack(x_start, y_start);
			
			//get the two nearest tag id close to my location
			double[][] NearestkeyStart = new double[2][2];
			NearestkeyStart = nearestTagsByCoordinate(x_start, y_start);
			
			int key_nearest1 = (int) NearestkeyStart[0][0];
			double weight_nearest1 = NearestkeyStart[0][1];
			
			
			
			ArrayList<Vertex> tagNode = ShortestPath.addGraphNode_Method3();
			
			Log.e("tagNode 1", ""+tagNode.get(1).adjacencies[0].weight);
			Log.e("tagNode 2", ""+tagNode.get(2).adjacencies.length);
			Log.e("key_nearest1", ""+key_nearest1);
			
			double[][] NearestkeyRes = new double[2][2];
			NearestkeyRes = nearestTagsByCoordinate(x_end, y_end);
			
			
			
			if(((x_start < 42.39416001428392) 
					&& (x_start > 42.39404462815619))
				||((x_start >= 42.39416001428392)  
						&&(x_start < 42.39421845009601)
						&&(y_start > -72.52876069396734)
						&&(y_start < -72.52868592739105)))
			{
				int key_nearest2 = (int) NearestkeyStart[1][0];
				double weight_nearest2 = NearestkeyStart[1][1];
				
				//added two nearest tag id into the graph
				int len1 = tagNode.get(key_nearest1).adjacencies.length;
				tagNode.get(key_nearest1).adjacencies[len1-1] = new Edge(tagNode.get(startNode_key), weight_nearest1);
				int len2 = tagNode.get(key_nearest2).adjacencies.length;
				tagNode.get(key_nearest2).adjacencies[len2-1] = new Edge(tagNode.get(startNode_key), weight_nearest2);
				
				tagNode.get(startNode_key).adjacencies = new Edge[]{ new Edge(tagNode.get(key_nearest1), weight_nearest1),
																		new Edge(tagNode.get(key_nearest2), weight_nearest2),
																		new Edge(tagNode.get(startNode_key), 0)}; 
				Log.e("key_nearest2", ""+key_nearest2);	
			}
			else
			{
				int len1 = tagNode.get(key_nearest1).adjacencies.length;
				tagNode.get(key_nearest1).adjacencies[len1-1] = new Edge(tagNode.get(startNode_key), weight_nearest1);
				
				tagNode.get(startNode_key).adjacencies = new Edge[]{ new Edge(tagNode.get(key_nearest1), weight_nearest1)}; 
				
			}
		
			
			//select the two nearest key
			if(((GePoint.naviPoints[destNode_key-1][1] < 42.39416001428392)  
				&& (GePoint.naviPoints[destNode_key-1][1] > 42.39404462815619))
				||((GePoint.naviPoints[destNode_key-1][1] >= 42.39416001428392)  
					&&(GePoint.naviPoints[destNode_key-1][1] < 42.39421845009601)
					&&(GePoint.naviPoints[destNode_key-1][2] > -72.52876069396734)
					&&(GePoint.naviPoints[destNode_key-1][2] < -72.52868592739105)))
			{	
				
				//Add destination to the graph
				int len_des_1 = tagNode.get((int)NearestkeyRes[0][0]).adjacencies.length;
				tagNode.get((int)NearestkeyRes[0][0]).adjacencies[len_des_1-1] = new Edge(tagNode.get(destNode_key), NearestkeyRes[0][1]);
				int len_des_2 = tagNode.get((int)NearestkeyRes[1][0]).adjacencies.length;
				tagNode.get((int)NearestkeyRes[1][0]).adjacencies[len_des_2-1] = new Edge(tagNode.get(destNode_key), NearestkeyRes[1][1]);
								
				
				tagNode.get(destNode_key).adjacencies = new Edge[]{ new Edge(tagNode.get((int)NearestkeyRes[0][0]), NearestkeyRes[0][1]),
																new Edge(tagNode.get((int)NearestkeyRes[1][0]), NearestkeyRes[1][1]),
																new Edge(tagNode.get(destNode_key), 0)};		
			}
			else
			{
			
				//Add destination to the graph
				int len_des_1 = tagNode.get((int)NearestkeyRes[0][0]).adjacencies.length;
				tagNode.get((int)NearestkeyRes[0][0]).adjacencies[len_des_1-1] = new Edge(tagNode.get(destNode_key), NearestkeyRes[0][1]);
	
				tagNode.get(destNode_key).adjacencies = new Edge[]{ new Edge(tagNode.get((int)NearestkeyRes[0][0]), NearestkeyRes[0][1])}; 
				
			}
			
			
			 //area 1
			if((((x_start < 42.39416001428392)     
					&& (x_start > 42.39404462815619))
				||((x_start >= 42.39416001428392)  
						&&(x_start < 42.39421845009601)
						&&(y_start > -72.52876069396734)
						&&(y_start < -72.52868592739105)))
			&&(((GePoint.naviPoints[destNode_key-1][1] < 42.39416001428392) 
					&& (GePoint.naviPoints[destNode_key-1][1] > 42.39404462815619))
					||((GePoint.naviPoints[destNode_key-1][1] >= 42.39416001428392)  
						&&(GePoint.naviPoints[destNode_key-1][1] < 42.39421845009601)
						&&(GePoint.naviPoints[destNode_key-1][2] > -72.52876069396734)
						&&(GePoint.naviPoints[destNode_key-1][2] < -72.52868592739105))))
			{
				int len_start = tagNode.get(startNode_key).adjacencies.length;
				GePoint startPt = new GePoint(x_start,y_start);
				GePoint destPt = new GePoint(GePoint.naviPoints[destNode_key-1][1], GePoint.naviPoints[destNode_key-1][2]);
				
				double start_end_weight = GeoPlaneCoordinateConversion.DistanceBetweenGePoints(startPt, destPt);
				tagNode.get(startNode_key).adjacencies[len_start-1] = new Edge(tagNode.get(destNode_key), start_end_weight);
				int len_dest = tagNode.get(destNode_key).adjacencies.length;
				tagNode.get(destNode_key).adjacencies[len_dest-1] = new Edge(tagNode.get(startNode_key), start_end_weight);
				
			}
			else if(((x_start > 42.39416001428392) &&(y_start < -72.52875432372093))
					&& ((GePoint.naviPoints[destNode_key-1][1] > 42.39416001428392) 
					       &&(GePoint.naviPoints[destNode_key-1][2] < -72.52875432372093)))    //area 2
			{
				int len_start = tagNode.get(startNode_key).adjacencies.length;
				GePoint startPt = new GePoint(x_start,y_start);
				GePoint destPt = new GePoint(GePoint.naviPoints[destNode_key-1][1], GePoint.naviPoints[destNode_key-1][2]);
				
				double start_end_weight = GeoPlaneCoordinateConversion.DistanceBetweenGePoints(startPt, destPt);
				tagNode.get(startNode_key).adjacencies[len_start-1] = new Edge(tagNode.get(destNode_key), start_end_weight);
				int len_dest = tagNode.get(destNode_key).adjacencies.length;
				tagNode.get(destNode_key).adjacencies[len_dest-1] = new Edge(tagNode.get(startNode_key), start_end_weight);
				
				
			}
			else if(((x_start > 42.39397127821532)    	//area 3
					  && (x_start < 42.39403060998703) 
					  &&(y_start < -72.52865746389055))
					&&((GePoint.naviPoints[destNode_key-1][1] > 42.39397127821532)
					  && (GePoint.naviPoints[destNode_key-1][1] < 42.39403060998703) 
					  &&(GePoint.naviPoints[destNode_key-1][2] < -72.52865746389055)))
			{
				int len_start = tagNode.get(startNode_key).adjacencies.length;
				GePoint startPt = new GePoint(x_start,y_start);
				GePoint destPt = new GePoint(GePoint.naviPoints[destNode_key-1][1], GePoint.naviPoints[destNode_key-1][2]);
				
				double start_end_weight = GeoPlaneCoordinateConversion.DistanceBetweenGePoints(startPt, destPt);
				tagNode.get(startNode_key).adjacencies[len_start-1] = new Edge(tagNode.get(destNode_key), start_end_weight);
				int len_dest = tagNode.get(destNode_key).adjacencies.length;
				tagNode.get(destNode_key).adjacencies[len_dest-1] = new Edge(tagNode.get(startNode_key), start_end_weight);
				
			}
			else if(((x_start < 42.39403447614767) &&(y_start > -72.52865746389055))   //area 1
					&& ((GePoint.naviPoints[destNode_key-1][1] < 42.39403447614767) 
						       &&(GePoint.naviPoints[destNode_key-1][2] > -72.52865746389055))) 
			{
				int len_start = tagNode.get(startNode_key).adjacencies.length;
				GePoint startPt = new GePoint(x_start,y_start);
				GePoint destPt = new GePoint(GePoint.naviPoints[destNode_key-1][1], GePoint.naviPoints[destNode_key-1][2]);
				
				double start_end_weight = GeoPlaneCoordinateConversion.DistanceBetweenGePoints(startPt, destPt);
				tagNode.get(startNode_key).adjacencies[len_start-1] = new Edge(tagNode.get(destNode_key), start_end_weight);
				int len_dest = tagNode.get(destNode_key).adjacencies.length;
				tagNode.get(destNode_key).adjacencies[len_dest-1] = new Edge(tagNode.get(startNode_key), start_end_weight);
				
			}
			
 				
			//calculate the shortest path
			ShortestPath.computePaths(tagNode.get(startNode_key));
			List<Vertex> path = ShortestPath.getShortestPathTo(tagNode.get(destNode_key));
			Log.e("Shortest_Paths= finish", ""+path);
			
			Iterator<Vertex> itr = path.iterator();
			int i = 0;
		    while(itr.hasNext()) 
		    {
		        Vertex pt = itr.next();
		        if(i == 0 || destNode_key == pt.id)
		        {
		        	i++;
		        	continue;
		        }
		        addNagivateTrack(GePoint.naviPoints[pt.id-1][1], GePoint.naviPoints[pt.id-1][2]);
		    }
		    
		    addNagivateTrack(x_end, y_end);
				
			return path;
		}
		
		return null;
	}
	
	
	
	//speech navigatoin for the blind 
	private void speech_navigation(List<Vertex> path) throws InterruptedException
	{
		if(TabletcloseToTargetNode(finalTabletGeoRes,destNode_key))
		{
		    speakWords("You arrived");
		    return;
		}
		Iterator<Vertex> itera = path.iterator();
	    while(itera.hasNext()) 
	    {
	        Vertex nextNode = itera.next();
	        if(0 == nextNode.id)
	        {
	        	continue;
	
	        }
	        //while(TabletcloseToTargetNode(finalTabletGeoRes,nextNode.id))
	        //{
	        	GePoint nextPt = new GePoint(GePoint.naviPoints[nextNode.id-1][1], GePoint.naviPoints[nextNode.id-1][2]);
	        	int target_degree = (int)bearingTwoPoint(finalTabletGeoRes,nextPt);
	        	
				
	        	//judge whether table is in the direction of target node's path      			
	        	if(!(Math.abs(target_degree - (int)g_tablet_bearing) <= 20 || Math.abs((target_degree+(int)g_tablet_bearing))%360 <= 20))
	        	{ 
	        		int dev_error = target_degree - (int)g_tablet_bearing;
	        		int rotated_degree = 0;
	        		String text ="";
	        		
	        		Log.e("tabelt_degree= ", ""+(int)g_tablet_bearing);
					Log.e("target_degree= ", ""+target_degree);
					
					Log.e("dev_error= ", ""+dev_error);

	        		if((-360 <= dev_error && dev_error < -180))
	        		{
	        			 	Context context = getApplicationContext();
	        		        text = "Turn right ";
	        		        int duration = Toast.LENGTH_LONG;
	        		        
	        		    	Toast toast = Toast.makeText(context, text, duration);
	        				toast.show();
	        				
	        				rotated_degree = 360 + dev_error;
	        				
	        		}
	        		else if (0 <= dev_error && dev_error <= 180)
	        		{
	        			Context context = getApplicationContext();
	    		         text = "Turn right ";
	    		        int duration = Toast.LENGTH_LONG;
	    		        
	    		    	Toast toast = Toast.makeText(context, text, duration);
	    				toast.show();
	    				rotated_degree = dev_error;
	    				
	        		}
	        		else if(-180 <= dev_error && dev_error < 0)
	        		{
	        			 	Context context = getApplicationContext();
	        		         text = "Turn left ";
	        		        int duration = Toast.LENGTH_LONG;
	        		        
	        		    	Toast toast = Toast.makeText(context, text, duration);
	        				toast.show();
	        				
	        				rotated_degree = -dev_error;
	        		}
	        		else if (180 < dev_error && dev_error <= 360)
	        		{
	        			Context context = getApplicationContext();
	    		         text = "Turn left ";
	    		        int duration = Toast.LENGTH_LONG;
	    		        
	    		    	Toast toast = Toast.makeText(context, text, duration);
	    				toast.show();
	    				rotated_degree = 360 - dev_error;
	        		}
	        		
	        		String speak_degree = Integer.toString(rotated_degree);
	        		
	        		speakWords(text+"to about"+speak_degree+"degree");
	        		//wait 3 sec
	        		try {
	        			Thread.sleep(3000);
	        		}
	        		catch (InterruptedException ex) {
	        			Log.e("thread sleep ", "exception");
	        		}

	        	}
	        	else
	        	{
	        		 speakWords("Go straight");
	     	        try {
	         			Thread.sleep(500);
	         		}
	         		catch (InterruptedException ex) {
	         			Log.e("thread sleep 2 ", "exception");
	         		}
	        	}
	        break;	          //only iterate the current and next node, then exit for updating paths to recalculate navigation
	    }

	   
      
	}
	
	//get the bearing angle(degree) of two points from north, clockwise direction
	public double bearingTwoPoint(GePoint p1, GePoint p2) 
	{
		double LatRad1 = p1.getLatitude()*Math.PI/180;
		double LatRad2 = p2.getLatitude()*Math.PI/180;
		
		double deltaLon = (p2.getLongtitude()-p1.getLongtitude())*Math.PI/180;
		
		double y = Math.sin(deltaLon)*Math.cos(LatRad2);
		double x = Math.cos(LatRad1)*Math.sin(LatRad2) - 
				   Math.sin(LatRad1)*Math.cos(LatRad2)*Math.cos(deltaLon);
		
		double bear = Math.atan2(y, x);
		return (bear*180/Math.PI + 360) % 360;
				
	}
	
	//judge whether tablet is close to the next target node in the path of graph
	public boolean TabletcloseToTargetNode(GePoint LocationPt, int target_key)
	{
		GePoint targetPt = new GePoint(GePoint.naviPoints[target_key-1][1], GePoint.naviPoints[target_key-1][2]);
		double distance = GeoPlaneCoordinateConversion.DistanceBetweenGePoints(LocationPt, targetPt);
		
		double destination_error = 1.5;
		if(distance < destination_error)
		{
			return true;
		}
		return false;
	}

    //navigation button click to call      
	public OnClickListener myOnClickListenerNavigatePath = new OnClickListener()
	{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			Log.e("myOnClickListenerNavigatePath= begin", "");
			
			//test only
			//GePoint p1 = new GePoint(42.39354668258381,-72.52833187580109);
			//GePoint p2 = new GePoint(42.393661574404135, -72.52840027213097);
			//double bear = bearingTwoPoint(p1, p2);
			//Log.e("bear= ", ""+bear);
			//Log.e("tablet degree = ", ""+g_tablet_bearing);
			//test_for_gettingPoint();
			
			UpdateNavigation_method3();
			
			flag_after_click_navigation = true;
	
		}
			    
	};
	
	//get the nearest 3 nodes in the graph given one point 
	public double[][] nearestTagsByCoordinate(double latitude, double longitude)
	{
		double[][] NearestkeyRes = new double[2][2];
		
		HashMap<Integer, Double> distTagsMap = new HashMap<Integer, Double>();		
		GePoint destPt = new GePoint(latitude, longitude);
		
		for(int i = 0; i < GePoint.naviPoints.length-1; i++)
		{
			GePoint tagPt = new GePoint(GePoint.naviPoints[i][1], GePoint.naviPoints[i][2]);
			distTagsMap.put(i+1, GeoPlaneCoordinateConversion.DistanceBetweenGePoints(destPt,tagPt));
		}
		
		distTagsMap = sortByComparator(distTagsMap, true);   //natural order
		
		int calNum = 0;
		for (Map.Entry<Integer, Double> entry : distTagsMap.entrySet()) 
		{
			if(calNum >= 2)
			{
			    break;
			}
		    Integer keyId= entry.getKey();
		    Double  Dist = entry.getValue();
		    NearestkeyRes[calNum][0] = keyId;
		    NearestkeyRes[calNum][1] = Dist;
		    calNum++;
		}
		Log.e("NearestkeyRes1", ""+NearestkeyRes[0][0]);
		Log.e("NearestkeyRes2", ""+NearestkeyRes[1][0]);
		
		return NearestkeyRes;
	
	}

	
    //speak the user text
   private void speakWords(String speech) {

           //speak straight away
           myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
   }
    
   //act on result of TTS data check
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    
       if (requestCode == MY_DATA_CHECK_CODE) {
           if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
               //the user has the necessary data - create the TTS
           myTTS = new TextToSpeech(this, this);
           }
           else {
                   //no data - install it now
               Intent installTTSIntent = new Intent();
               installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
               startActivity(installTTSIntent);
           }
       }
   }

   //setup TTS
   public void onInit(int initStatus) {
    
           //check for successful instantiation
       if (initStatus == TextToSpeech.SUCCESS) {
           if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
               myTTS.setLanguage(Locale.US);
       }
       else if (initStatus == TextToSpeech.ERROR) {
           Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
       }
   }
   
	 
	/**
	 * A filter check whether the detected beacon is a Gimbal tag used for
	 * project.
	 * 
	 * @param beacon
	 *            The detected beacon
	 * @return Whether the beacon is a Gimbal tag for project or not.
	 */
	private boolean isGimbalTag(Beacon beacon) {
		final String uuid = beacon.getId1().toString();
		final String tagIdentifier = uuid.split("-")[0];
		if (validGimbalIdentifiers.contains(tagIdentifier)) {
			return true;
		}
		return false;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
		{
			 g_tablet_bearing = event.values[0];             //tablet's bearing from north, clockwise
			//Log.e("tablet degree = ", ""+g_tablet_bearing);
		}
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
}

