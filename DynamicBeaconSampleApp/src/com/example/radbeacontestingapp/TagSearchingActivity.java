package com.example.radbeacontestingapp;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import com.objectspace.jgl.*;

import java.util.Map.Entry;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import com.google.common.collect.ImmutableSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TagSearchingActivity extends Activity implements BeaconConsumer {
	
	/** A set of valid Gimbal beacon identifier */
	private final Set<String> validGimbalIdentifiers = ImmutableSet.of(
			"00100001", "10011101");

	//added
	public static int average_sample_nums = 3;
	
	private static final int TAGS_TOTAL_NUM = 20;
	public static final String BUNDLE_RESULT_LOCATION = "resultLocation";
	public static final int TABLET_TO_CEILING_HEIGHT = 1;     //meter
	private static int count = 0;
	/** Log for TagSearchingActivity. */
	private static final String TAG_SEARCHING_ACTIVITY_LOG = "TAG_SEA_ACT_LOG";
	
	private ListView tagSearchListView;
	private ArrayAdapter<Beacon> tagSearchListAdapter;
	private List<Beacon> discoveredBeaconList;

	/** The map used for storing discovered beacons */
	protected HashMap<String, Beacon> discoveredBeaconMap;
	
	//added ;
	protected ArrayList< ArrayList<Double> > distListTempArray = new ArrayList<ArrayList<Double>>(TAGS_TOTAL_NUM);
	protected HashMap<Integer, ArrayList<Double>> TempDistMap= new HashMap<Integer, ArrayList<Double>>();
	public HashMap<Integer, Double> distanceMap;
	
	public static double[][] CalculatedtagInfoArray = new double[3][4];; //store the searched tag's info, using 3 tags to locate

	public static GePoint finalTabletGeoRes;
	Button showMapBtn;
	
	ArrayList<Integer> tagUsedArray;   //store the updated 3 tags that are used to located
	
	TextView viewX;
	TextView viewY;
	EditText SampleNums;
	
	/** Declare and initiate the a BeaconManager object.*/
	private BeaconManager beaconManager = BeaconManager
			.getInstanceForApplication(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_searching);

		discoveredBeaconMap = new HashMap<String, Beacon>();
		distanceMap = new HashMap<Integer, Double>();
		discoveredBeaconList = new ArrayList<Beacon>();
		finalTabletGeoRes = null;
		tagUsedArray = new ArrayList<Integer>(3);
		 
		tagSearchListView = (ListView) findViewById(R.id.search_result_list);
		tagSearchListAdapter = new BeaconAdapter(this,
				R.layout.discovered_beacon_info_view, discoveredBeaconList);
		tagSearchListView.setAdapter(tagSearchListAdapter);
		
		
		showMapBtn = (Button)findViewById(R.id.btnShowMap);
		showMapBtn.setOnClickListener(myOnClickListenerShowMap);
		viewX = (TextView)findViewById(R.id.txtresGeoX);
		viewY = (TextView)findViewById(R.id.txtresGeoY);
		
		SampleNums = (EditText)findViewById(R.id.extSampleNum);
		/*
		// location receiver filter
		IntentFilter locationFilter;
		locationFilter = new IntentFilter(CircleIntersecPointsCalculateService.BROADCAST_RESULT_POINT);
	     CalculateReceiver = new CalculationReceiver();
	    registerReceiver(CalculateReceiver, locationFilter);    
	    */    
	}

	@Override
	protected void onResume() {
		super.onResume();
		beaconManager
				.getBeaconParsers()
				.add(new BeaconParser()
						.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
		beaconManager.bind(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		beaconManager.unbind(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//beaconManager.unbind(this);
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
				tagSearchListAdapter.notifyDataSetChanged();
				if(null != finalTabletGeoRes)
				{
					viewX.setText("x: "+finalTabletGeoRes.getLatitude());
					viewY.setText("y: "+finalTabletGeoRes.getLongtitude());
				}
			}
		});
	}

	/**
	 * Called when the beacon service is successfully connected to beacon.
	 */
	@Override
	public void onBeaconServiceConnect() {
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
							
							Log.d(" beacon id2 ", " "+beacon.getId2().toString());
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
							
							//SearchNearestPointLocation(key,rssival);

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
			Log.e(TAG_SEARCHING_ACTIVITY_LOG, e.toString());
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

		//Log.d("distance value1 ", " "+distanceTemp);

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
			if(average_sample_nums == Temp.size())
			{
				Iterator<Double> it = Temp.iterator();
				double sum = 0;
				while(it.hasNext())
				{
				    double dis = it.next();
				    sum += dis;
				}
				distanceAvg = sum/average_sample_nums;
				
				//Log.d("keyInt= ", ""+keyInt);
				//Log.d("distanceAvg= ", " "+distanceAvg);
				distanceMap.put(keyInt, distanceAvg);
								
				//DistanceSortMapByValue sortValueDistanceMap = new DistanceSortMapByValue();
				distanceMap = sortByComparator(distanceMap, true);   //natural order
				
				//output sorted tag's key and distance from the nearest distance to larger distance
				//calculate the minimum three tags to locate
				if (3 <= distanceMap.size())   //At least two tags needed to locate
				{
					int calNum = 0;
					
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
					    if(calNum < tagUsedArray.size())
					    {
					    	tagUsedArray.set(calNum, keyId);
					    }
					    else
					    {
					    	tagUsedArray.add(calNum, keyId);
					    }
					    calNum++;

					}
				
					if (3 <= tagUsedArray.size())
					{
						Log.d("tagUsedArray 0", ""+tagUsedArray.get(0));
						Log.d("tagUsedArray 1", ""+tagUsedArray.get(1));
						Log.d("tagUsedArray 2", ""+tagUsedArray.get(2));
					}
					//locate using circle
					Log.d(" CalculatedtagInfoArray[0][1]t0 = ", ""+ ShowGoogleMapActivity.CalculatedtagInfoArray[0][1]);
					Log.d(" CalculatedtagInfoArray[1][1]t1 = ", ""+ ShowGoogleMapActivity.CalculatedtagInfoArray[1][1]);
				    CircleLocationforTagSearchAct circlePoint = new CircleLocationforTagSearchAct();
				   
				    //PlanePoint finalPlaneRes = circlePoint.GetResultPlanePoint();
				    
				    //Log.d("finalPlaneRes x= ", ""+finalPlaneRes.getPlanex());
					//Log.d("finalPlaneRes y= ", ""+finalPlaneRes.getPlaney());
					
					finalTabletGeoRes =  circlePoint.GetResultGeoFinalPoint();
					
					//Log.d("finalTabletGeoRes x= ", ""+finalTabletGeoRes.getLatitude());
					//Log.d("finalTabletGeoRes y= ", ""+finalTabletGeoRes.getLongtitude());

					String OutputXYPoints = Double.toString(finalTabletGeoRes.getLatitude()) + ", " + Double.toString(finalTabletGeoRes.getLongtitude());
					
					//viewX.setText(" "+finalTabletGeoRes.getLatitude());
					//viewY.setText(" "+finalTabletGeoRes.getLongtitude());
					
					//clear searched tags information 
					Temp.clear();			
					TempDistMap.clear();
					distListTempArray.clear();
					count = 0;
					//discoveredBeaconList.clear();
					Log.d("Clear Temp and distace Temp Here = ", "enter here 336");

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

        // Sorting the list based on values
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
	
	
	 public OnClickListener myOnClickListenerShowMap = new OnClickListener()
	 {
	    	@Override
	    	public void onClick(View v)
	    	{
	    		//Log.d("myOnClickListenerShowMap = ", "enter here");

				Intent GoogleMapIntent = new Intent(getApplicationContext(), ShowGoogleMapActivity.class);
				/*Bundle resBundle = new Bundle();
				
				double[] locationResArray = new double[2];
				
				if(finalTabletGeoRes == null)
				{
					Log.d("myOnClickListenerShowMap finalTabletGeoRes = ", "null");
					return;
				}
				locationResArray[0] = finalTabletGeoRes.getLatitude();
				locationResArray[1] = finalTabletGeoRes.getLongtitude();
				
				resBundle.putDoubleArray(BUNDLE_RESULT_LOCATION, locationResArray);

				GoogleMapIntent.putExtras(resBundle);
				
				//Log.d("myOnClickListenerShowMap = ", "startActivity GoogleMapIntent");
				int duration = Toast.LENGTH_LONG;
				
				Context context = getApplicationContext();
				
				/*String key = "";
				
				if(2 == tagUsedArray.size())
				{
					key = Integer.toString((int)tagUsedArray.get(0)) + ", "+ Integer.toString((int)tagUsedArray.get(1));
				}
				else if(3 == tagUsedArray.size())
				{
					key = Integer.toString((int)tagUsedArray.get(0)) + ", "+ Integer.toString((int)tagUsedArray.get(1))+ ", " 
						 +Integer.toString((int)tagUsedArray.get(2));
				}
				//else if(2 > tagUsedArray.size())
				//{
				//	return;     // wait until at least 2 tags find
				//}
				
				String text = "We use key " + key + " to locate";
				
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();

				*/
				String strSampleNum = SampleNums.getText().toString();
				if (!strSampleNum.matches("")) 
	    		{   	
					average_sample_nums = Integer.parseInt(strSampleNum);
					
					ShowGoogleMapActivity.average_sample_nums = average_sample_nums;
	    		}
				
	    		startActivity(GoogleMapIntent);
	    		
	    	}
	
	};


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
}
