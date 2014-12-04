package com.example.dntmoi;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.example.dntmoi.R;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener {
	private GoogleMap map;
	private LatLng ltlng;
	private LocationClient mLocationClient;
	private static final String PROVIDER = "flp";
	private static final double LAT = 37.377166;
	private static final double LNG = -122.086966;
	private static final float ACCURACY = 3.0f;
	private Location mCurrentLocation;

    LocationManager lm;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		InitialiseMap();
		Button buttonreload1 = (Button) findViewById(R.id.buttonreload);
		Button buttonreload2 = (Button) findViewById(R.id.buttonreload2);
		
		mLocationClient = new LocationClient(this, this, this);
		
		buttonreload1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				map.clear();
				ltlng = new LatLng(55, 45);
				map.addMarker(new MarkerOptions().position(ltlng).title(
						"Hello world1:"));
			}

		});
		
		buttonreload2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				 * // TODO Auto-generated method stub map.clear(); ltlng = new
				 * LatLng(75, 65); map.addMarker(new
				 * MarkerOptions().position(ltlng).title( "Hello world2:"));
				 */
								

			}
	
		});
		
	}
	/*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        //mLocationClient.disconnect();
        super.onStop();
    }
    
	public void alertBox(String text) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Info");
		alert.setMessage(text);
		alert.setPositiveButton("OK", null);
		alert.show();
	}

	private void InitialiseMap() {
		// TODO Auto-generated method stub
		if (map == null) {
			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map_frag)).getMap();

			if (map != null) {

			}

			map.setMyLocationEnabled(true);
			for (int i = 60; i < 70; i++) {
				ltlng = new LatLng(i, i + 30);

				//map.addMarker(new MarkerOptions().position(ltlng).title("Hello world:" + i));
			}
		}
	}
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		mLocationClient.setMockMode(true);
		Location newLocation = new Location(
				MainActivity.PROVIDER);
		double latitude = 55, longitude = 65;
		float accuracy = 3.0f;
		newLocation.setLatitude(latitude);
		newLocation.setLongitude(longitude);
		newLocation.setAccuracy(accuracy);
		newLocation.setTime(System.currentTimeMillis());

	    int sdk = android.os.Build.VERSION.SDK_INT;
	    if(sdk >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
	        newLocation.setElapsedRealtimeNanos(
	             SystemClock.elapsedRealtimeNanos());
	    }
		mLocationClient.setMockLocation(newLocation);
		alertBox("Mocked !!");
	}
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
}
