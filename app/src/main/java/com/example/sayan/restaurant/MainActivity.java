package com.example.sayan.restaurant;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.android.gms.nearby.messages.Strategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        ActionBar.TabListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    //for picture taken
    Bitmap bitImage;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final String LOG_TAG = "sayan";
    //for google place api
    private GoogleApiClient mGoogleApiClient;
    //for swipe tab
    private ViewPager viewPager;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs = {"Restaurants", "Profile"};

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
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        } else {
            Toast.makeText(MainActivity.this, "permission already granted", Toast.LENGTH_LONG).show();
            callPlaceDetectionApi();
        }
        //for swipe tab
        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getSupportActionBar();
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
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
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
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                Toast.makeText(MainActivity.this, "in onResult", Toast.LENGTH_LONG).show();
                if (likelyPlaces.getCount() == 0) {
                    Toast.makeText(MainActivity.this, "no places found", Toast.LENGTH_LONG).show();
                    Log.d(LOG_TAG, "" + likelyPlaces);
                }
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Toast.makeText(MainActivity.this, "likely places", Toast.LENGTH_LONG).show();
                    Log.i(LOG_TAG, String.format("Place '%s' with " +
                                    "likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {      //resultCode = OK or Cancel
        Toast.makeText(this, "onActivityResult called", Toast.LENGTH_SHORT).show();
        switch (requestCode) {
            case 123:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        bitImage = (Bitmap) data.getExtras().get("data");
                        sendData(bitImage);
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
            case 321:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data == null) {
                            Toast.makeText(this, "picture is not selected!", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                                bitImage = BitmapFactory.decodeStream(inputStream);
                                sendData(bitImage);
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

    private void sendData(final Bitmap bit) {
        ImageView image = (ImageView) findViewById(R.id.imageView2);
        TextView text = (TextView) findViewById(R.id.textView);
        final EditText edit = (EditText) findViewById(R.id.editText);
        final Button bSave = (Button) findViewById(R.id.button3);
        final Button bEdit = (Button) findViewById(R.id.button2);
        bEdit.setVisibility(View.INVISIBLE);
        text.setVisibility(View.GONE);
        edit.setVisibility(View.VISIBLE);
        image.setImageBitmap(bit);
        edit.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                            bSave.setVisibility(View.VISIBLE);
                            bEdit.setVisibility(View.VISIBLE);
                            return true;
                        }
                        return false; // pass on to other listeners.
                    }
                });
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textUserName = edit.getText().toString();
                new SendJSONData().execute(textUserName);
            }
        });
    }

    private class SendJSONData extends AsyncTask<String, Void, String> {
        StringBuilder sb = new StringBuilder();

        String http = "http://111.93.227.162/tour_app/api/editUser";

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            File file = null;
            String username = null;
            try {
                URL url = new URL(http);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Host", "android.schoolportal.gr");
                urlConnection.connect();
                //Create JSONObject here
                if (params[0] == null) {
                    return null;
                }
                username = params[0];
                file = createFile(bitImage, username);
                Log.d(LOG_TAG, "username: " + username + " image: " + file);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_id", "72");
                jsonParam.put("username", username);
                jsonParam.put("profile_img", file);
                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                out.write(jsonParam.toString());
                Log.d(LOG_TAG, "Para: " + jsonParam.toString());
                out.close();

                int HttpResult = urlConnection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    Log.d(LOG_TAG, "Y: " + sb.toString());
                } else {
                    Log.d(LOG_TAG, "N: " + urlConnection.getResponseMessage());
                }
            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return null;
        }
    }

    File createFile(Bitmap bitmap, String filename) {
        //create a file to write bitmap data
        File f = new File(filename + ".png");
        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapdata = bos.toByteArray();
        try {
            f.createNewFile();
//write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }
}