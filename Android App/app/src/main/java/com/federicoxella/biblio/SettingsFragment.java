package com.federicoxella.biblio;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private OnFragmentInteractionListener mListener;
    private SharedPreferences sharedPreferences;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE);
        Switch modeSwitch = (Switch) view.findViewById(R.id.mode_switch);
        RelativeLayout viewUserHash = (RelativeLayout) view.findViewById(R.id.view_user_hash);

        String mode =
                sharedPreferences.getString(getString(R.string.check_beacon_mode), getString(R.string.check_beacon_mode_automatic));

        if (mode.equals(getString(R.string.check_beacon_mode_automatic))) {
            modeSwitch.setChecked(false);
        }
        else if (mode.equals(getString(R.string.check_beacon_mode_manual))){
            modeSwitch.setChecked(true);
        }

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The switch is enabled
                    Log.d(TAG, "The switch is enabled");
                    SharedPreferences.Editor editor;
                    editor = sharedPreferences.edit()
                            .putString(getString(R.string.check_beacon_mode), getString(R.string.check_beacon_mode_manual));
                    editor.apply();
                }
                else {
                    // The switch is disabled
                    Log.d(TAG, "The switch is disabled");
                    SharedPreferences.Editor editor;
                    editor = sharedPreferences.edit()
                            .putString(getString(R.string.check_beacon_mode), getString(R.string.check_beacon_mode_automatic));
                    editor.apply();
                }
            }
        });

        viewUserHash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUuid();
            }
        });

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
        void onSettingsFragmentInteraction(Uri uri);
    }

    private void showUuid() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View showUuidView = layoutInflater.inflate(R.layout.show_uuid, null);

        final String uuid = sharedPreferences.getString(getString(R.string.user_uid), getString(R.string.default_string));
        TextView userUid = (TextView) showUuidView.findViewById(R.id.uuid);

        userUid.setText(uuid);

        builder.setView(showUuidView);
        AlertDialog showUuid = builder
                .setTitle(R.string.user_uid_label)
                .setPositiveButton(R.string.copy_user_uuid_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                            android.text.ClipboardManager clipboard =
                                    (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(uuid);
                        }
                        else {
                            ClipboardManager clipboard =
                                    (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Copied UUID", uuid);
                            clipboard.setPrimaryClip(clip);
                        }

                        Context context = getActivity();
                        String toastText = getString(R.string.uuid_copied_label);

                        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                })
                .setNegativeButton(R.string.close_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        showUuid.show();
    }
}
