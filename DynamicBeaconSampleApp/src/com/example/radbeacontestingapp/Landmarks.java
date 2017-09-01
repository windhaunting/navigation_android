package com.example.radbeacontestingapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Landmarks {
	
	private Map<String,MyMarker> markers;
	
	public Landmarks(){
		markers = new HashMap<String, MyMarker>();
	}
	
	public void addMarker(String id, Marker marker){
		markers.put(id, new MyMarker(marker, id));
	}
	
	public void removeMarker(String id){
		markers.get(id).getMarker().remove();
		markers.remove(id);
	}
	
	public void setBleID(String id, String bleId){
		markers.get(id).setBleID(bleId);
	}
	
	public String getBleID(String id){
		return markers.get(id).getBleID();
	}
	
	public String printResults(){
		String entireResult = "";
		for(MyMarker marker : markers.values()){
			String m = "";
			m += marker.getBleID() + ",";
			LatLng p = marker.getMarker().getPosition();
			m += p.latitude + ",";
			m += p.longitude + ",";
			m += "@";
			entireResult += m;
		}
		return entireResult;
	}
	
}
