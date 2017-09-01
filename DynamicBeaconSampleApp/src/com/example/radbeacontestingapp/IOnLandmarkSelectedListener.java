package com.example.radbeacontestingapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public interface IOnLandmarkSelectedListener {
	public void onLandmarkSelected(Marker landmark);
	public void onModeChange();
	void onMapLongClick(LatLng point);
}
