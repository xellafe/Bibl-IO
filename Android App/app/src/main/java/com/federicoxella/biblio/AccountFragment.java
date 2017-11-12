package com.federicoxella.biblio;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AccountFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private static final String TAG = "AccountActivity";

    SharedPreferences sharedPreferences;
    Context context;

    TextInputEditText editName;
    TextInputEditText editSurname;
    TextInputEditText editBadge;
    Spinner spinnerCourse;

    String course;
    ArrayList<String> coursesList;
    RequestQueueSingleton requestQueueSingleton;
    String defaultString;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        setHasOptionsMenu(true);

        context = getActivity();

        requestQueueSingleton = RequestQueueSingleton.getInstance(context);

        coursesList = new ArrayList<>();
        coursesList.add(getString(R.string.no_course_label));

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE);

        defaultString = getString(R.string.default_string);

        String userName = sharedPreferences.getString(getString(R.string.user_name), defaultString);
        String userSurname = sharedPreferences.getString(getString(R.string.user_surname), defaultString);
        String badgeNumber = sharedPreferences.getString(getString(R.string.badge_number), defaultString);

        editName = (TextInputEditText) view.findViewById(R.id.edit_name);
        editSurname = (TextInputEditText) view.findViewById(R.id.edit_surname);
        editBadge = (TextInputEditText) view.findViewById(R.id.edit_badge);
        spinnerCourse = (Spinner) view.findViewById(R.id.spinner_course);

        editName.setText(userName);
        editSurname.setText(userSurname);

        if (!badgeNumber.equals(defaultString)) {
            editBadge.setText(badgeNumber);
        }

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: "+position);
                course = coursesList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected");
            }
        });

        String getCoursesUrl = "http://64.137.248.69:3000/courses";

        JsonObjectRequest coursesRequest =
                new JsonObjectRequest(Request.Method.GET,
                        getCoursesUrl,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int code = response.getInt("code");
                                    if (code == 200) {
                                        JSONArray courses = response.getJSONArray("value");
                                        for (int i = 0; i < courses.length(); i++) {
                                            JSONObject course = courses.getJSONObject(i);
                                            coursesList.add(course.getString("nome"));
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                ArrayAdapter<String> spinnerCourseAdapter =
                                        new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, coursesList);

                                spinnerCourseAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                                spinnerCourse.setAdapter(spinnerCourseAdapter);

                                String courseOfStudy = sharedPreferences.getString(getString(R.string.course_of_study_id), defaultString);

                                if (!courseOfStudy.equals(defaultString)) {
                                    int index = Integer.parseInt(courseOfStudy);
                                    Log.d(TAG, "onCreate: "+index);
                                    spinnerCourse.setSelection(index);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.getMessage());
                            }
                        });

        requestQueueSingleton
                .addToRequestQueue(coursesRequest);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.account_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.apply_changes:
                saveChanges();
            default:
                return super.onOptionsItemSelected(item);
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onAccountFragmentInteraction(Uri uri);
    }

    public void saveChanges() {

        final String uuid = sharedPreferences.getString(getString(R.string.user_uid), getString(R.string.default_string));
        final String name = editName.getText().toString();
        final String surname = editSurname.getText().toString();
        String badge = editBadge.getText().toString();

        Toast toast;

        if (name.isEmpty()) {
            toast = Toast.makeText(getActivity(), getString(R.string.name_error_label), Toast.LENGTH_LONG);
            toast.show();
        }
        else if (surname.isEmpty()) {
            toast = Toast.makeText(getActivity(), getString(R.string.surname_error_label), Toast.LENGTH_LONG);
            toast.show();
        }
        else {

            if (badge.isEmpty()) {
                badge = getString(R.string.default_string);
            }

            String getCourseIDUrl = "http://64.137.248.69:3000/courses?nome="+adjustStringToUrl(course);
            Log.d(TAG, "saveChanges: "+getCourseIDUrl);
            final String finalBadge = badge;

            JsonObjectRequest coursesRequest =
                    new JsonObjectRequest(Request.Method.GET,
                            getCourseIDUrl,
                            null,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(TAG, "onResponse: "+response);
                                    int code;
                                    try {
                                        code = response.getInt("code");
                                        if (code == 200) {
                                            JSONObject value = response.getJSONObject("value");
                                            final String courseID = value.getString("id");

                                            String updateUrl = "http://64.137.248.69:3000/update?id="+uuid
                                                    +"&nome="+name
                                                    +"&cognome="+surname
                                                    +"&matricola="+ finalBadge
                                                    +"&corsodistudi="+courseID;

                                            Log.d(TAG, "onResponse updateUrl: "+updateUrl);

                                            JsonObjectRequest updateUserInfoRequest =
                                                    new JsonObjectRequest(Request.Method.GET,
                                                            updateUrl,
                                                            null,
                                                            new Response.Listener<JSONObject>() {
                                                                @Override
                                                                public void onResponse(JSONObject response) {
                                                                    try {
                                                                        int code = response.getInt("code");
                                                                        if (code == 200) {
                                                                            Toast toast;

                                                                            SharedPreferences.Editor editor = sharedPreferences.edit()
                                                                                    .putString(getString(R.string.user_name), name)
                                                                                    .putString(getString(R.string.user_surname), surname)
                                                                                    .putString(getString(R.string.badge_number), finalBadge)
                                                                                    .putString(getString(R.string.course_of_study), course)
                                                                                    .putString(getString(R.string.course_of_study_id), courseID);
                                                                            editor.apply();

                                                                            toast = Toast.makeText(getActivity(), getString(R.string.info_update_label), Toast.LENGTH_SHORT);
                                                                            toast.show();
                                                                        }
                                                                        else if(code == 400) {
                                                                            Toast toast;

                                                                            toast = Toast.makeText(getActivity(), getString(R.string.info_update_label), Toast.LENGTH_SHORT);
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

                                                                }
                                                            });

                                            requestQueueSingleton
                                                    .addToRequestQueue(updateUserInfoRequest);

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            }
                    );

            requestQueueSingleton
                    .addToRequestQueue(coursesRequest);
        }

    }

    private String adjustStringToUrl(String str) {
        return str.replaceAll(" ", "%20");
    }
}
