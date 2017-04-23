package com.example.sayan.restaurant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements
        ActionBar.TabListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    //for picture taken
    Bitmap bitImage;
    EditText edit;

    private static final int MY_PERMISSIONS_REQUEST = 100;
    private static final String LOG_TAG = "sayan";
    //for google place api
    private GoogleApiClient mGoogleApiClient;
    //for swipe tab
    private ViewPager viewPager;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs = {"List", "Profile"};

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
        Toast.makeText(MainActivity.this, "before permission check", Toast.LENGTH_LONG).show();
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
                        MY_PERMISSIONS_REQUEST);
            }
        } else {
            Toast.makeText(MainActivity.this, "permission already granted", Toast.LENGTH_LONG).show();
            callPlaceDetectionApi();
        }
        //for swipe tab
        // Initialization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getSupportActionBar();
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
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
        Toast.makeText(MainActivity.this, "connection failed", Toast.LENGTH_LONG).show();
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
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    Toast.makeText(MainActivity.this, "granted", Toast.LENGTH_LONG).show();
                    callPlaceDetectionApi();

                } else {
                    Toast.makeText(MainActivity.this, "denied", Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    private void callPlaceDetectionApi() throws SecurityException {
        Toast.makeText(MainActivity.this, "in callPlaceDetectionApi", Toast.LENGTH_LONG).show();
        ArrayList<String> restrictToRestaurants = new ArrayList<>();
        restrictToRestaurants.add(Integer.toString(Place.TYPE_RESTAURANT));
        PlaceFilter pf;
        pf = new PlaceFilter(false, restrictToRestaurants);
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, pf);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                Toast.makeText(MainActivity.this, "in onResult", Toast.LENGTH_LONG).show();
                if (likelyPlaces.getCount() == 0) {
                    Toast.makeText(MainActivity.this, "no places found", Toast.LENGTH_LONG).show();
                    Log.d(LOG_TAG, "" + likelyPlaces);
                }
                ArrayList<String> names = new ArrayList<String>();
                ArrayList<String> adds = new ArrayList<String>();
                ArrayList<Float> ratings = new ArrayList<Float>();
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Toast.makeText(MainActivity.this, "likely places", Toast.LENGTH_LONG).show();
                    Log.i(LOG_TAG, String.format("Place '%s' with " +
                                    "likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
                    names.add(placeLikelihood.getPlace().getName().toString());
                    adds.add(placeLikelihood.getPlace().getAddress().toString());
                    ratings.add(placeLikelihood.getPlace().getRating());
                    Toast.makeText(MainActivity.this, "places found", Toast.LENGTH_LONG).show();
                }


                likelyPlaces.release();
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(MainActivity.this, "onconnected" + bundle, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(MainActivity.this, "onconnectionsuspended", Toast.LENGTH_LONG).show();

    }


    //result come from camera or gallery activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {      //resultCode = OK or Cancel
        Toast.makeText(this, "onActivityResult called", Toast.LENGTH_SHORT).show();
        switch (requestCode) {
            case 123:                           // from camera request code 123
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        bitImage = (Bitmap) data.getExtras().get("data");
                        saveData();
                        Toast.makeText(this, "Picture captured successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Canceled!", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(this, "Something went wrong!" + bitImage, Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case 321:                              // from gallery request code 321
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data == null) {
                            Toast.makeText(this, "picture is not selected!", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                                bitImage = BitmapFactory.decodeStream(inputStream);
                                saveData();
                                Toast.makeText(this, "picture selected successfully", Toast.LENGTH_SHORT).show();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Canceled!", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(this, "Something went wrong!" + bitImage, Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            default:
                Toast.makeText(this, "Something went wrong! Cannot add picture!" + bitImage, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //process image data to get all user details and to upload image to server
    private void saveData() {
        ImageView image = (ImageView) findViewById(R.id.imageView2);
        final TextView text = (TextView) findViewById(R.id.textView);
        edit = (EditText) findViewById(R.id.editText);
        final Button bSave = (Button) findViewById(R.id.button3);
        final Button bEdit = (Button) findViewById(R.id.button2);
        text.setVisibility(View.INVISIBLE);
        edit.setVisibility(View.VISIBLE);
        bSave.setVisibility(View.VISIBLE);
        image.setImageBitmap(bitImage);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textUserName = edit.getText().toString();
                uploadData();
                edit.setVisibility(View.INVISIBLE);
                bSave.setVisibility(View.INVISIBLE);
                text.setVisibility(View.VISIBLE);
                text.setText(edit.getText());
            }
        });
    }

    //for uploading profile data into server using Volley
    private void uploadData(){
        String uploadUrl = "http://111.93.227.162/tour_app/api/editUser";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", "72");
                params.put("username", edit.getText().toString());
                params.put("profile_img", imageToString(bitImage));
                return params;
            }
        };
        VolleySingleton.getInstance(MainActivity.this).getRequestQueue().add(stringRequest);
    }

    // for converting bitmap into image string
    private String imageToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }
}