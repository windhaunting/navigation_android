package com.example.radbeacontestingapp;

import com.google.android.gms.maps.model.Marker;

public class MyMarker {
	
	private Marker marker;
	private String BleID;
	
	public MyMarker(Marker _marker, String _id){
		this.marker = _marker;
		this.BleID = _id;
	}
	
	public Marker getMarker() {
		return marker;
	}
	public void setMarker(Marker marker) {
		this.marker = marker;
	}
	public String getBleID() {
		return BleID;
	}
	public void setBleID(String bleID) {
		BleID = bleID;
	}
	
	
	
}
