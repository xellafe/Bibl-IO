package com.federicoxella.biblio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private static final String TAG = "MainFragment";

    private SharedPreferences sharedPreferences;

    private SwipeRefreshLayout swipeContainer;
    private ListView logListView;
    private ArrayList<JSONObject> logArrayList;
    private String uuid;
    private LogAdapter logAdapter;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences =
                getActivity().getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE);

        logArrayList = new ArrayList<>();

        uuid = sharedPreferences.getString(getString(R.string.user_uid), getString(R.string.default_string));

        logAdapter = new LogAdapter(getActivity(),logArrayList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        String registeredStatus = sharedPreferences.getString(getString(R.string.registered_status), getString(R.string.not_registered));
        String mode = sharedPreferences.getString(getString(R.string.check_beacon_mode), getString(R.string.check_beacon_mode_automatic));

        logListView = (ListView) view.findViewById(R.id.access_listView);
        logListView.setAdapter(logAdapter);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateUserLog();

                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        getUserLog(view);

        Log.d(TAG, mode);

        RelativeLayout manualModeLayout =
                (RelativeLayout) view.findViewById(R.id.manual_mode_container);                          // In modalità manuale rende visibili i pulsanti

        if (manualModeLayout != null) {
            if (mode.equals(getString(R.string.check_beacon_mode_manual))) {
                manualModeLayout.setVisibility(View.VISIBLE);
            }
            else {
                manualModeLayout.setVisibility(View.GONE);
            }
        }

        final Button manualEntrance = (Button) view.findViewById(R.id.manual_entrance);
        final Button manualExit = (Button) view.findViewById(R.id.manual_exit);

        if (manualExit != null && manualEntrance != null) {
            manualEntrance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    manualExit.setVisibility(View.VISIBLE);
                    manualEntrance.setVisibility(View.GONE);
                }
            });

            manualExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    manualEntrance.setVisibility(View.VISIBLE);
                    manualExit.setVisibility(View.GONE);
                }
            });

            if (registeredStatus.equals(getString(R.string.not_registered))) {
                manualEntrance.setVisibility(View.VISIBLE);                                         // Entrata visibile
                manualExit.setVisibility(View.GONE);                                                // Uscita invisibile
            }
            else {
                manualExit.setVisibility(View.VISIBLE);                                             // Entrata invisibile
                manualEntrance.setVisibility(View.GONE);                                            // Uscita visibile
            }
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void getUserLog(final View view) {

        String userLogUrl = "http://64.137.248.69:3000/frequenza?id="+uuid;

        JsonObjectRequest userLogRequest = new JsonObjectRequest(Request.Method.GET,
                userLogUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int code = response.getInt("code");
                            if (code == 200) {
                                JSONArray userLogs = response.getJSONArray("value");

                                for (int i = 0; i < userLogs.length(); i++) {
                                    logArrayList.add(userLogs.getJSONObject(i));
                                }

                                logAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: ",error.getCause() );
            }
        });

        RequestQueueSingleton.getInstance(getActivity())
                .addToRequestQueue(userLogRequest);

    }

    private void updateUserLog() {

        String userLogUrl = "http://64.137.248.69:3000/frequenza?id="+uuid;

        JsonObjectRequest userLogRequest = new JsonObjectRequest(Request.Method.GET,
                userLogUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int code = response.getInt("code");
                            if (code == 200) {
                                JSONArray userLogs = response.getJSONArray("value");

                                logArrayList.clear();

                                for (int i = 0; i < userLogs.length(); i++) {
                                    logArrayList.add(userLogs.getJSONObject(i));
                                }

                                logAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: ",error.getCause() );
            }
        });

        RequestQueueSingleton.getInstance(getActivity())
                .addToRequestQueue(userLogRequest);

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        String getLibraryName();
    }
}

class LogAdapter extends ArrayAdapter<JSONObject> {

    public LogAdapter(Context context, ArrayList<JSONObject> logList) {
        super(context, 0, logList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        JSONObject log = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.log_list_adapter, parent, false);
        }

        TextView libraryName = (TextView) convertView.findViewById(R.id.library_name);
        TextView dateTW = (TextView) convertView.findViewById(R.id.date);
        TextView timeInTW = (TextView) convertView.findViewById(R.id.time_in);
        TextView timeOutTW = (TextView) convertView.findViewById(R.id.time_out);

        try {
            String libraryNameStr = log.getString("Nome");
            String dateStr = log.getString("timeIN").substring(0, 9);
            String timeInStr = log.getString("timeIN").substring(11, 16);
            String timeOutStr = log.getString("timeOUT");

            Log.d("getView", "Stringa: "+timeOutStr);

            if (!timeOutStr.equals("null")) {
                timeOutStr = timeOutStr.substring(11, 16);
            }
            else {
                timeOutStr = "";
            }

            libraryName.setText(libraryNameStr);
            dateTW.setText(dateStr);
            timeInTW.setText(timeInStr);
            timeOutTW.setText(timeOutStr);

        } catch (JSONException e) {
            e.getMessage();
        }

        return convertView;
    }
}
