package com.example.radbeacontestingapp;

import java.util.List;

import org.altbeacon.beacon.Beacon;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BeaconAdapter extends ArrayAdapter<Beacon> {
	private Context context;
	private int layoutResouceId;
	private List<Beacon> beacons = null;
	
	/**
	 * Constructor for BeaconAdatper.
	 * @param context The application context.
	 * @param resource The layout for the beacon adapter.
	 * @param discoveredBeaconList The discovered beacon list.
	 */
	public BeaconAdapter(Context context, int resource, List<Beacon> discoveredBeaconList) {
		super(context, resource, discoveredBeaconList);
		this.context = context;
		this.layoutResouceId = resource;
		this.beacons = discoveredBeaconList;
	}
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        BeaconHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResouceId, parent, false);
            
            holder = new BeaconHolder();
            holder.uuidValueTextView = (TextView)row.findViewById(R.id.uuid);
            holder.majorValueTextView = (TextView)row.findViewById(R.id.major);
            holder.minorValueTextView = (TextView)row.findViewById(R.id.minor);
            holder.rssiValueTextView = (TextView)row.findViewById(R.id.rssi_reading_value);
            
            row.setTag(holder);
        }
        else
        {
            holder = (BeaconHolder)row.getTag();
        }
        
        Beacon beacon = beacons.get(position);
        holder.uuidValueTextView.setText(beacon.getId1().toString());
        holder.majorValueTextView.setText(beacon.getId2().toString());
        holder.minorValueTextView.setText(beacon.getId3().toString());
        holder.rssiValueTextView.setText(String.valueOf(beacon.getRssi()));
        
        return row;
    }
	
	static class BeaconHolder
    {
        TextView uuidValueTextView;
        TextView majorValueTextView;
        TextView minorValueTextView;
        TextView rssiValueTextView;
    }
}
