package me.suszczewicz.dialog;

import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.suszczewicz.compass.BuildConfig;
import me.suszczewicz.compass.R;
import me.suszczewicz.util.GPSUtil;

public class GPSCoordinatesDialog extends DialogFragment {

    public interface Callback { void onNewUserGPSCoordinates(Location location); }

    public static final String TAG = GPSCoordinatesDialog.class.getSimpleName();

    @Bind(R.id.dialog_gps_coordinates_lat)
    protected EditText latitude;

    @Bind(R.id.dialog_gps_coordinates_long)
    protected EditText longitude;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_gps_coordinates_name)
                .setView(createDialogView())
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        initializeDialog(dialog);

        return dialog;
    }

    private void passResult() {
        Location location = new Location("");
        location.setLatitude(Float.parseFloat(latitude.getText().toString()));
        location.setLongitude(Float.parseFloat(longitude.getText().toString()));

        getParent().onNewUserGPSCoordinates(location);
    }

    private Callback getParent() {
        return (Callback) getParentFragment();
    }

    private boolean validateCoordinates() {
        boolean valid = GPSUtil.validateLatitude(latitude.getText().toString())
                && GPSUtil.validateLongitude(longitude.getText().toString());

        if (!valid)
            Snackbar.make(latitude, R.string.dialog_gps_coordinates_incorrect_values, Snackbar.LENGTH_SHORT).show();

        return valid;
    }

    private View createDialogView() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        View v = layoutInflater.inflate(R.layout.dialog_gps_coordinates, null);

        ButterKnife.bind(this, v);
        setupView();

        return v;
    }

    private void initializeDialog(AlertDialog dialog) {
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.setOnShowListener(d -> {

            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(view -> {

                if (validateCoordinates()) {
                    passResult();
                    dismiss();
                }

            });
        });
    }

    private void setupView() {
        if (BuildConfig.DEBUG) {
            latitude.setText(R.string.default_lat);
            longitude.setText(R.string.default_long);
        }
    }

}
