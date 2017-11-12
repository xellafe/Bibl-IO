package com.federicoxella.biblio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "LauncherActivity";

    private SharedPreferences sharedPreferences;
    private Context context;
    private String userHash = "000000000000000000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        context = this;
        sharedPreferences =
                getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE);

        String firstStartTrue = getString(R.string.is_true);
        String registered =
                sharedPreferences.getString(getString(R.string.first_start), firstStartTrue);

        Intent startMainActivity = new Intent(this, MainActivity.class);


        if(registered.equals(firstStartTrue)){                           // Utente non registrato o logout
            RegisterUser(startMainActivity);
        }
        else {
            startActivity(startMainActivity);
            finish();
        }
    }

    private void RegisterUser(final Intent intent) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);                              // Builder per Alert Dialog
        final LayoutInflater layoutInflater = getLayoutInflater();
        final View loginDialogView = layoutInflater.inflate(R.layout.register_new_user, null);
        builder.setView(loginDialogView);
        final AlertDialog loginDialog = builder
                .setPositiveButton(R.string.register_label, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        TextInputEditText name =
                                (TextInputEditText) loginDialogView.findViewById(R.id.user_name);
                        TextInputEditText surname =
                                (TextInputEditText) loginDialogView.findViewById(R.id.user_surname);

                        final String userName = name.getText().toString();
                        final String userSurname = surname.getText().toString();

                        String registrationUrl = "http://64.137.248.69:3000/register?nome="
                                +userName+"&cognome="
                                +userSurname;

                        JsonObjectRequest jsonObjRequest =
                                new JsonObjectRequest(Request.Method.GET,
                                        registrationUrl,
                                        null,
                                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                try {
                                    int code = response.getInt("code");
                                    if (code == 200) {
                                        JSONObject value = response.getJSONObject("value");
                                        userHash = value.getString("uuid");

                                        SharedPreferences.Editor editor = sharedPreferences.edit()
                                                .putString(getString(R.string.first_start), getString(R.string.is_false))
                                                .putString(getString(R.string.user_name), userName)                    // Nome
                                                .putString(getString(R.string.user_surname), userSurname)             // Cognome
                                                .putString(getString(R.string.user_uid), userHash)                        // Hash dell'utente attualmente connesso
                                                .putString(getString(R.string.check_beacon_mode), getString(R.string.check_beacon_mode_automatic))                     // Modalità di scansione (Automatica/Manuale) DEBUG
                                                .putString(getString(R.string.registered_status), getString(R.string.not_registered));  // Imposta l'utente come non registrato in una biblioteca
                                        editor.apply();

                                        startActivity(intent);
                                        finish();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.getMessage());
                            }
                        });

                        RequestQueueSingleton.getInstance(context)
                                .addToRequestQueue(jsonObjRequest);
                    }
                })
                .create();

        loginDialog.show();
    }
}
