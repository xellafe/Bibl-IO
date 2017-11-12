package com.federicoxella.biblio;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.json.JSONException;
import org.json.JSONObject;

public class BackgroundScan extends Application implements BootstrapNotifier {

    private static final String TAG = "BackgroundScan";
    private static final String MONITOR_ID = "UNIBO";
    private static final String UUID = "494e472d5343492d494e462d42494231";

    private RegionBootstrap regionBootstrap;
    private SharedPreferences sharedPreferences;

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        sharedPreferences =
                getSharedPreferences(getString(R.string.shared_preference), MODE_PRIVATE);

        BeaconManager mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        Region region = new Region(MONITOR_ID, Identifier.parse(UUID), null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "Got a didEnterRegion call");

        /** Se l'app è in background viene mostrata la notifica */
        if (sharedPreferences.getString(getString(R.string.is_in_foreground),
                getString(R.string.is_false))
                .equals(getString(R.string.is_false))) {

            Intent intent = new Intent(this, LauncherActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.unibo_logo)
                    .setContentTitle(getString(R.string.notify_title))
                    .setContentText(getString(R.string.notify_text));                                   // Better than Pokémon Go

            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setAutoCancel(true);
            mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);



            int mNotificationID = 001;
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(mNotificationID, mBuilder.build());
        }
    }

    @Override
    public void didExitRegion(Region region) {
        Log.d(TAG, "didExitRegion");
        exitRequest();
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        Log.d(TAG, "didDetermineStateForRegion: " + i);
    }

    private void exitRequest() {

        String registerdStatus = sharedPreferences.getString(getString(R.string.registered_status), getString(R.string.is_registered));

        String uuid = sharedPreferences.getString(getString(R.string.user_uid), getString(R.string.default_string));

        sharedPreferences.edit()
                .putString(getString(R.string.registered_status), getString(R.string.not_registered))
                .apply();


        String exitUrl = "http://64.137.248.69:3000/logoff?id="+uuid;
        Log.d(TAG, "didExitRegion: "+exitUrl);

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
