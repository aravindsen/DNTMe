package com.example.dntmoi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import com.example.dntmoi.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dntmoi.GeofenceUtils.REMOVE_TYPE;
import com.example.dntmoi.GeofenceUtils.REQUEST_TYPE;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements
		ConnectionCallbacks, OnConnectionFailedListener {
	private GoogleMap map;
	private LatLng ltlng;
	private LocationClient mLocationClient;
	private static final String PROVIDER = "flp";
	private static final double LAT = 37.377166;
	private static final double LNG = -122.086966;
	private static final float ACCURACY = 3.0f;
	private Location mCurrentLocation;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	LocationManager lm;

	// HOME
	Double lati2 = 40.764746, longi2 = -111.857907;
	Float rad2 = (float) 1000.0;

	// COLLEGE
	Double lati1 = 40.767599, longi1 = -111.843995;
	Float rad1 = (float) 1000.0;

	ConnectionResult connectionResult;

	// GEOFENCE CODE START
	/*
	 * Use to set an expiration time for a geofence. After this amount of time
	 * Location Services will stop tracking the geofence. Remember to unregister
	 * a geofence when you're finished with it. Otherwise, your app will use up
	 * battery. To continue monitoring a geofence indefinitely, set the
	 * expiration time to Geofence#NEVER_EXPIRE.
	 */
	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS
			* DateUtils.HOUR_IN_MILLIS;

	// Store the current request
	private REQUEST_TYPE mRequestType;

	// Store the current type of removal
	private REMOVE_TYPE mRemoveType;

	// Persistent storage for geofences
	private SimpleGeofenceStore mPrefs;

	// Store a list of geofences to add
	List<Geofence> mCurrentGeofences;

	// Add geofences handler
	private GeofenceRequester mGeofenceRequester;
	// Remove geofences handler
	private GeofenceRemover mGeofenceRemover;
	// // Handle to geofence 1 latitude in the UI
	// private EditText mLatitude1;
	//
	// // Handle to geofence 1 longitude in the UI
	// private EditText mLongitude1;
	//
	// // Handle to geofence 1 radius in the UI
	// private EditText mRadius1;
	//
	// // Handle to geofence 2 latitude in the UI
	// private EditText mLatitude2;
	//
	// // Handle to geofence 2 longitude in the UI
	// private EditText mLongitude2;

	// Handle to geofence 2 radius in the UI
	private EditText mRadius2;

	/*
	 * Internal lightweight geofence objects for geofence 1 and 2
	 */
	private SimpleGeofence mUIGeofence1;
	private SimpleGeofence mUIGeofence2;

	// decimal formats for latitude, longitude, and radius
	private DecimalFormat mLatLngFormat;
	private DecimalFormat mRadiusFormat;

	/*
	 * An instance of an inner class that receives broadcasts from listeners and
	 * from the IntentService that receives geofence transition events
	 */
	private GeofenceSampleReceiver mBroadcastReceiver;

	// An intent filter for the broadcast receiver
	private IntentFilter mIntentFilter;

	// Store the list of geofences to remove
	private List<String> mGeofenceIdsToRemove;

	// GEOFENCE CODE END

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// GEOFENCE CODE START
		// Set the pattern for the latitude and longitude format
		String latLngPattern = getString(R.string.lat_lng_pattern);

		// Set the format for latitude and longitude
		mLatLngFormat = new DecimalFormat(latLngPattern);

		// Localize the format
		mLatLngFormat.applyLocalizedPattern(mLatLngFormat.toLocalizedPattern());

		// Set the pattern for the radius format
		String radiusPattern = getString(R.string.radius_pattern);

		// Set the format for the radius
		mRadiusFormat = new DecimalFormat(radiusPattern);

		// Localize the pattern
		mRadiusFormat.applyLocalizedPattern(mRadiusFormat.toLocalizedPattern());

		// Create a new broadcast receiver to receive updates from the listeners
		// and service
		mBroadcastReceiver = new GeofenceSampleReceiver();

		// Create an intent filter for the broadcast receiver
		mIntentFilter = new IntentFilter();

		// Action for broadcast Intents that report successful addition of
		// geofences
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

		// Action for broadcast Intents that report successful removal of
		// geofences
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

		// Action for broadcast Intents containing various types of geofencing
		// errors
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

		// All Location Services sample apps use this category
		mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

		// Instantiate a new geofence storage area
		mPrefs = new SimpleGeofenceStore(this);

		// Instantiate the current List of geofences
		mCurrentGeofences = new ArrayList<Geofence>();

		// Instantiate a Geofence requester
		mGeofenceRequester = new GeofenceRequester(this);

		// Instantiate a Geofence remover
		mGeofenceRemover = new GeofenceRemover(this);
		// GEOFENCE CODE END

		// Attach to the main UI
		setContentView(R.layout.activity_main);
		InitialiseMap();

		Button buttonreload1 = (Button) findViewById(R.id.buttonreload1);
		Button buttonreload2 = (Button) findViewById(R.id.buttonreload2);

		mLocationClient = new LocationClient(this, this, this);

		buttonreload1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				map.clear();
				Location loc = mLocationClient.getLastLocation();
				ltlng = new LatLng(loc.getLatitude(), loc.getLongitude());
				map.addMarker(new MarkerOptions().position(ltlng).title(
						"Hello world1:"));
			}

		});

		buttonreload2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				notification();
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
		// mLocationClient.disconnect();
		super.onStop();
	}

	public void alertBox(String text) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Info");
		alert.setMessage(text);
		alert.setPositiveButton("OK", null);
		alert.show();
	}

	public void notification() {
		int mNotificationId = 1;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("My notification")
				.setContentText("Hello World!");
		NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotifyMgr.notify(mNotificationId, mBuilder.build());
		MockingJay();
	}

	// MOCK FUNCTIONS
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		mLocationClient.setMockMode(false);
		// MockingJay();

		onRegisterClicked();

		// Instantiates a new CircleOptions object and defines the center and
		// radius
		CircleOptions circleOptions = new CircleOptions().center(
				new LatLng(lati2, longi2)).radius(rad2); // In meters

		// Get back the mutable Circle
		Circle circle = map.addCircle(circleOptions);

		showCurrentLoc();
	}

	public void showCurrentLoc() {
		Location loc = mLocationClient.getLastLocation();
		ltlng = new LatLng(loc.getLatitude(), loc.getLongitude());
		map.addMarker(new MarkerOptions().position(ltlng).title("You"));
	}

	public void MockingJay() {
		Location loc = mLocationClient.getLastLocation();
		ProjectionPoint.Point originallocation = new ProjectionPoint.Point(
				loc.getLatitude(), loc.getLongitude());
		ProjectionPoint p = new ProjectionPoint();
		ProjectionPoint.Point center = new ProjectionPoint.Point(lati2, longi2);
		ProjectionPoint.Point newloc = p.getCircleProjectedPointIntersection(
				originallocation, center, rad2);
		mLocationClient.setMockMode(true);

		Location newLocation = new Location(MainActivity.PROVIDER);
		// double latitude = 21, longitude = 78;
		float accuracy = 3.0f;
		// newLocation.setLatitude(latitude);
		// newLocation.setLongitude(longitude);
		newLocation.setLatitude(newloc.getLat());
		newLocation.setLongitude(newloc.getLong());
		newLocation.setAccuracy(accuracy);
		newLocation.setTime(System.currentTimeMillis());

		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			newLocation.setElapsedRealtimeNanos(SystemClock
					.elapsedRealtimeNanos());
		}
		mLocationClient.setMockLocation(newLocation);
		alertBox("Mocked !!");
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	// HELPER FUNCTIONS

	private void InitialiseMap() {
		// TODO Auto-generated method stub
		if (map == null) {
			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map_frag)).getMap();

			map.setMyLocationEnabled(true);

		}
	}

	// GEOFENCE FUNCTIONS
	/**
	 * Verify that Google Play services is available before making a request.
	 * 
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {

			// In debug mode, log the status
			Log.d(GeofenceUtils.APPTAG,
					getString(R.string.play_services_available));

			// Continue
			return true;

			// Google Play services was not available for some reason
		} else {

			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
					this, 0);
			if (dialog != null) {
				alertBox("Google Play services was not available");
			}
			return false;
		}
	}

	/**
	 * Called when the user clicks the "Register geofences" button. Get the
	 * geofence parameters for each geofence and add them to a List. Create the
	 * PendingIntent containing an Intent that Location Services sends to this
	 * app's broadcast receiver when Location Services detects a geofence
	 * transition. Send the List and the PendingIntent to Location Services.
	 */
	public void onRegisterClicked() {

		/*
		 * Record the request as an ADD. If a connection error occurs, the app
		 * can automatically restart the add request if Google Play services can
		 * fix the error
		 */
		mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

		/*
		 * Check for Google Play services. Do this after setting the request
		 * type. If connecting to Google Play services fails, onActivityResult
		 * is eventually called, and it needs to know what type of request was
		 * in progress.
		 */
		if (!servicesConnected()) {

			return;
		}

		/*
		 * Create a version of geofence 1 that is "flattened" into individual
		 * fields. This allows it to be stored in SharedPreferences.
		 */

		mUIGeofence1 = new SimpleGeofence("1",
		// Get latitude, longitude, and radius from the UI
				lati1, longi1, rad1,
				// Set the expiration time
				Geofence.NEVER_EXPIRE,
				// Only detect entry transitions
				Geofence.GEOFENCE_TRANSITION_ENTER);

		// Store this flat version in SharedPreferences
		mPrefs.setGeofence("1", mUIGeofence1);

		/*
		 * Create a version of geofence 2 that is "flattened" into individual
		 * fields. This allows it to be stored in SharedPreferences.
		 */

		mUIGeofence2 = new SimpleGeofence("2",
		// Get latitude, longitude, and radius from the UI
				lati2, longi2, rad2,
				// Set the expiration time GEOFENCE_EXPIRATION_IN_MILLISECONDS,
				Geofence.NEVER_EXPIRE,
				// Detect both entry and exit transitions
				Geofence.GEOFENCE_TRANSITION_ENTER
						| Geofence.GEOFENCE_TRANSITION_EXIT);

		// Store this flat version in SharedPreferences
		mPrefs.setGeofence("2", mUIGeofence2);

		/*
		 * Add Geofence objects to a List. toGeofence() creates a Location
		 * Services Geofence object from a flat object
		 */
		mCurrentGeofences.add(mUIGeofence1.toGeofence());
		mCurrentGeofences.add(mUIGeofence2.toGeofence());

		// Start the request. Fail if there's already a request in progress
		try {
			// Try to add geofences
			mGeofenceRequester.addGeofences(mCurrentGeofences);
		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this,
					R.string.add_geofences_already_requested_error,
					Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * Handle results returned to this Activity by other Activities started with
	 * startActivityForResult(). In particular, the method onConnectionFailed()
	 * in GeofenceRemover and GeofenceRequester may call
	 * startResolutionForResult() to start an Activity that handles Google Play
	 * services problems. The result of this call returns here, to
	 * onActivityResult. calls
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// Choose what to do based on the request code
		switch (requestCode) {

		// If the request code matches the code sent in onConnectionFailed
		case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				// If the request was to add geofences
				if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

					// Toggle the request flag and send a new request
					mGeofenceRequester.setInProgressFlag(false);

					// Restart the process of adding the current geofences
					mGeofenceRequester.addGeofences(mCurrentGeofences);

					// If the request was to remove geofences
				} else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType) {

					// Toggle the removal flag and send a new removal request
					mGeofenceRemover.setInProgressFlag(false);

					// If the removal was by Intent
					if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

						// Restart the removal of all geofences for the
						// PendingIntent
						mGeofenceRemover
								.removeGeofencesByIntent(mGeofenceRequester
										.getRequestPendingIntent());

						// If the removal was by a List of geofence IDs
					} else {

						// Restart the removal of the geofence list
						mGeofenceRemover
								.removeGeofencesById(mGeofenceIdsToRemove);
					}
				}
				break;

			// If any other result was returned by Google Play services
			default:

				// Report that Google Play services was unable to resolve the
				// problem.
				Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
			}

			// If any other request code was received
		default:
			// Report that this Activity received an unknown requestCode
			Log.d(GeofenceUtils.APPTAG,
					getString(R.string.unknown_activity_request_code,
							requestCode));

			break;
		}
	}

	/**
	 * Define a Broadcast receiver that receives updates from connection
	 * listeners and the geofence transition service.
	 */
	public class GeofenceSampleReceiver extends BroadcastReceiver {
		/*
		 * Define the required method for broadcast receivers This method is
		 * invoked when a broadcast Intent triggers the receiver
		 */
		@Override
		public void onReceive(Context context, Intent intent) {

			// Check the action code and determine what to do
			String action = intent.getAction();

			// Intent contains information about errors in adding or removing
			// geofences
			if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

				handleGeofenceError(context, intent);

				// Intent contains information about successful addition or
				// removal of geofences
			} else if (TextUtils.equals(action,
					GeofenceUtils.ACTION_GEOFENCES_ADDED)
					|| TextUtils.equals(action,
							GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

				handleGeofenceStatus(context, intent);

				// Intent contains information about a geofence transition
			} else if (TextUtils.equals(action,
					GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

				handleGeofenceTransition(context, intent);

				// The Intent contained an invalid action
			} else {
				Log.e(GeofenceUtils.APPTAG,
						getString(R.string.invalid_action_detail, action));
				Toast.makeText(context, R.string.invalid_action,
						Toast.LENGTH_LONG).show();
			}
		}

		/**
		 * If you want to display a UI message about adding or removing
		 * geofences, put it here.
		 * 
		 * @param context
		 *            A Context for this component
		 * @param intent
		 *            The received broadcast Intent
		 */
		private void handleGeofenceStatus(Context context, Intent intent) {

		}

		/**
		 * Report geofence transitions to the UI
		 * 
		 * @param context
		 *            A Context for this component
		 * @param intent
		 *            The Intent containing the transition
		 */
		private void handleGeofenceTransition(Context context, Intent intent) {
			/*
			 * If you want to change the UI when a transition occurs, put the
			 * code here. The current design of the app uses a notification to
			 * inform the user that a transition has occurred.
			 */
		}

		/**
		 * Report addition or removal errors to the UI, using a Toast
		 * 
		 * @param intent
		 *            A broadcast Intent sent by ReceiveTransitionsIntentService
		 */
		private void handleGeofenceError(Context context, Intent intent) {
			String msg = intent
					.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
			Log.e(GeofenceUtils.APPTAG, msg);
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}

	}

}
