package com.federicoxella.biblio;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements
        MainFragment.OnFragmentInteractionListener,
        AccountFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        BeaconConsumer {

    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private View drawerHeader;

    private static final String MONITOR_ID = "UNIBO";
    private static final String UUID = "494e472d5343492d494e462d42494231";
    private Region region = new Region(MONITOR_ID, Identifier.parse(UUID), null, null);
    private BeaconManager beaconManager;

    private SharedPreferences sharedPreferences;
    private String uuid;
    private Context context;

    String libraryName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        context = this;

        toolbar = (Toolbar) findViewById(R.id.biblio_toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);

        // Find our drawer view
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        sharedPreferences =
                getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        Log.d(TAG, "onCreate: "+ BeaconManager.getRegionExitPeriod());

        String defaultString = getString(R.string.default_string);

        uuid = sharedPreferences.getString(getString(R.string.user_uid), defaultString);
        TextView user = (TextView) drawerHeader.findViewById(R.id.user_name);
        TextView badge = (TextView) drawerHeader.findViewById(R.id.badge_number);
        TextView course = (TextView) drawerHeader.findViewById(R.id.course_of_study);

        String userName =
                sharedPreferences.getString(getString(R.string.user_name), defaultString);
        String userSurname =
                sharedPreferences.getString(getString(R.string.user_surname), defaultString);
        String generateString = userName + " " + userSurname;

        if (user != null) {
            user.setText(generateString);
        }

        String badgeNumber =
                sharedPreferences.getString(getString(R.string.badge_number), defaultString);

        if (!badgeNumber.equals(defaultString)) {
            assert badge != null;
            badge.setText(badgeNumber);
            badge.setVisibility(View.VISIBLE);
        }
        else {
            assert badge != null;
            badge.setVisibility(View.GONE);
        }

        String courseOfStudy =
                sharedPreferences.getString(getString(R.string.course_of_study), defaultString);

        if (!courseOfStudy.equals(defaultString)) {
            assert course != null;
            course.setText(courseOfStudy);
            course.setVisibility(View.VISIBLE);
        }
        else {
            badge.setVisibility(View.GONE);
        }

        TimerTask scanForBeacons = new TimerTask() {
            @Override
            public void run() {
                try {
                    beaconManager.startMonitoringBeaconsInRegion(region);
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    protected void onStart(){
        super.onStart();

        Fragment fragment = null;
        try {
            fragment = MainFragment.class.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
    }

    @Override
    protected void onResume(){
        super.onResume();

        beaconManager.bind(this);

        sharedPreferences.edit()
                .putString(getString(R.string.is_in_foreground), getString(R.string.is_true))
                .apply();

        Log.d(TAG, "onResume: app in foreground");
    }

    @Override
    protected void onPause() {
        super.onPause();

        beaconManager.unbind(this);

        sharedPreferences.edit()
                .putString(getString(R.string.is_in_foreground), getString(R.string.is_false))
                .apply();

        Log.d(TAG, "onPause: app in background");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        exitRequest();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
        drawerHeader = navigationView.getHeaderView(0);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open,  R.string.navigation_drawer_close);
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.main_fragment:
                fragmentClass = MainFragment.class;
                break;
            case R.id.account_fragment:
                fragmentClass = AccountFragment.class;
                break;
            case R.id.settings_fragment:
                fragmentClass = SettingsFragment.class;
                break;
            default:
                fragmentClass = MainFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    @Override
    public void onBackPressed(){
        if(mDrawer.isDrawerOpen(GravityCompat.START)){
            mDrawer.closeDrawers();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d(TAG, "didEnterRegion: " + region);
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d(TAG, "didExitRegion: " + region);

                exitRequest();

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.d(TAG, "didDetermineStateForRegion: " + i);
            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if (collection.size() > 0) {
                    for (Beacon beacon : collection) {
                        Log.d(TAG, "didRangeBeaconsInRegion: " + beacon.toString());

                        if (sharedPreferences.getString(getString(R.string.registered_status),
                                getString(R.string.not_registered))
                                .equals(getString(R.string.not_registered))) {

                            String entranceUrl = "http://64.137.248.69:3000/logon?id="+uuid
                                    +"&biblioteca="+beacon.getId3();

                            final String getLibraryUrl = "http://64.137.248.69:3000/biblioteca?id="+beacon.getId3();

                            Log.d(TAG, "Utente non registrato presso nessuna biblioteca: "+entranceUrl);

                            try {
                                beaconManager.stopMonitoringBeaconsInRegion(region);
                                beaconManager.stopRangingBeaconsInRegion(region);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }

                            sharedPreferences.edit()
                                    .putString(getString(R.string.registered_status), getString(R.string.is_registered))
                                    .apply();

                            JsonObjectRequest entranceRequest =
                                    new JsonObjectRequest(Request.Method.GET,
                                            entranceUrl,
                                            null,
                                            new Response.Listener<JSONObject>() {

                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    try {
                                                        Log.d(TAG, response.toString());

                                                        int code = response.getInt("code");
                                                        if (code == 200) {

                                                            JsonObjectRequest getLibraryRequest =
                                                                    new JsonObjectRequest(Request.Method.GET,
                                                                            getLibraryUrl,
                                                                            null,
                                                                            new Response.Listener<JSONObject>() {
                                                                                @Override
                                                                                public void onResponse(JSONObject response) {
                                                                                    try {
                                                                                        Log.d(TAG, response.toString());

                                                                                        int code = response.getInt("code");
                                                                                        if (code == 200) {
                                                                                            JSONObject value = response.getJSONObject("value");
                                                                                            String library = value.getString("nome");

                                                                                            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();

                                                                                            for (Fragment fragment : fragmentList) {
                                                                                                try {
                                                                                                    TextView libraryName = (TextView) fragment.getView().findViewById(R.id.actual_library);
                                                                                                    libraryName.setText(library);
                                                                                                }
                                                                                                catch (Exception e) {
                                                                                                    Log.d(TAG, "Non è lui: "+e.getMessage());
                                                                                                }
                                                                                            }

                                                                                            String toastString = getString(R.string.access_success_label) + " " + library;

                                                                                            Toast toast = Toast.makeText(context, toastString, Toast.LENGTH_SHORT);
                                                                                            toast.show();
                                                                                        }
                                                                                    } catch (JSONException e) {
                                                                                        e.printStackTrace();
                                                                                    }

                                                                                }
                                                                            },
                                                                            new Response.ErrorListener() {
                                                                                @Override
                                                                                public void onErrorResponse(VolleyError error) {
                                                                                    Log.e(TAG, error.getMessage());
                                                                                }
                                                                            });

                                                            RequestQueueSingleton.getInstance(context)
                                                                    .addToRequestQueue(getLibraryRequest);
                                                        }

                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Log.e(TAG, error.getMessage());
                                                }
                                            });

                            RequestQueueSingleton.getInstance(context)
                                    .addToRequestQueue(entranceRequest);
                        }
                        else {
                            Log.d(TAG, "Utente già registrato: ciccia");
                        }
                    }
                }
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void exitRequest() {

        String registeredStatus = sharedPreferences.getString(getString(R.string.registered_status),
                getString(R.string.is_registered));

        Log.d(TAG, "didExitRegion: "+registeredStatus);

        if (registeredStatus.equals(getString(R.string.is_registered))) {

            sharedPreferences.edit()
                    .putString(getString(R.string.registered_status), getString(R.string.not_registered))
                    .apply();

            String exitUrl = "http://64.137.248.69:3000/logoff?id=" + uuid;
            Log.d(TAG, "didExitRegion: " + exitUrl);

            JsonObjectRequest exitRequest =
                    new JsonObjectRequest(Request.Method.GET,
                            exitUrl,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        int code = response.getInt("code");
                                        if (code == 200) {
                                            Toast toast = Toast.makeText(context, R.string.exit_success_label, Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "exitRequest: ", error.getCause());
                                }
                            });

            RequestQueueSingleton.getInstance(context)
                    .addToRequestQueue(exitRequest);
        }
    }

    @Override
    public void onAccountFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSettingsFragmentInteraction(Uri uri) {

    }

    @Override
    public String getLibraryName() {
        return libraryName;
    }
}
