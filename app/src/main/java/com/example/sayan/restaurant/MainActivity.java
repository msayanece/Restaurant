package com.example.sayan.restaurant;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.location.places.Places;

public class MainActivity extends AppCompatActivity implements
        ActionBar.TabListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final String LOG_TAG = "sayan";
    //for google place api
    private GoogleApiClient mGoogleApiClient;
    //for swipe tab
    private ViewPager viewPager;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs = { "Restaurants", "Profile" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //for google place api
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, 0, this)
                .build();
        mGoogleApiClient.connect();
        Toast.makeText(MainActivity.this,"before permission check",Toast.LENGTH_LONG).show();
        //check if permission granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "permission check1", Toast.LENGTH_LONG).show();
            // explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "For getting information about nearby Restaurants," +
                        "Location permission is needed.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "permission check2", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        } else {
            Toast.makeText(MainActivity.this, "permission already granted", Toast.LENGTH_LONG).show();
            callPlaceDetectionApi();
        }
        //for swipe tab
        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar =  getSupportActionBar();
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
//        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }


    //for google place api
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this,"connection failed",Toast.LENGTH_LONG).show();
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(MainActivity.this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    Toast.makeText(MainActivity.this,"granted",Toast.LENGTH_LONG).show();
                    callPlaceDetectionApi();

                } else {
                    Toast.makeText(MainActivity.this,"denied",Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    private void callPlaceDetectionApi() throws SecurityException {
        Toast.makeText(MainActivity.this,"in callPlaceDetectionApi",Toast.LENGTH_LONG).show();
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                Toast.makeText(MainActivity.this,"in onResult",Toast.LENGTH_LONG).show();
                if (likelyPlaces.getCount()==0){
                    Toast.makeText(MainActivity.this,"no places found",Toast.LENGTH_LONG).show();
                    Log.d(LOG_TAG,""+likelyPlaces);
                }
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Toast.makeText(MainActivity.this,"likely places",Toast.LENGTH_LONG).show();
                    Log.i(LOG_TAG, String.format("Place '%s' with " +
                                    "likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
                    Toast.makeText(MainActivity.this,"places found",Toast.LENGTH_LONG).show();
                }
                likelyPlaces.release();
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(MainActivity.this,"onconnected"+bundle,Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(MainActivity.this,"onconnectionsuspended",Toast.LENGTH_LONG).show();

    }
}