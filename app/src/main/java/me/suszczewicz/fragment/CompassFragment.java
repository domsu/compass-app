package me.suszczewicz.fragment;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mbanje.kurt.fabbutton.FabButton;
import me.suszczewicz.compass.R;
import me.suszczewicz.dialog.GPSCoordinatesDialog;
import me.suszczewicz.provider.AzimuthProvider;
import me.suszczewicz.provider.LocationProvider;
import me.suszczewicz.view.CompassView;

public class CompassFragment extends Fragment implements GPSCoordinatesDialog.Callback {

    private final static String LOCATION_PARCELABLE_KEY = "location";

    private AzimuthProvider azimuthProvider;
    private LocationProvider locationProvider;

    @Bind(R.id.fragment_compass_view)
    protected CompassView compassView;

    @Bind(R.id.fragment_compass_navigate_fab)
    protected FabButton navigateFab;

    @Nullable
    private Location targetLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        restoreState(savedInstanceState);
        initialize();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        subscribeProviders();
    }

    @Override
    public void onResume() {
        super.onResume();

        azimuthProvider.start();

        if (targetLocation != null)
            locationProvider.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        azimuthProvider.stop();
        locationProvider.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(LOCATION_PARCELABLE_KEY, targetLocation);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_compass, container, false);

        ButterKnife.bind(this, v);

        setupView();

        return v;
    }

    @OnClick(R.id.fragment_compass_navigate_fab)
    public void onNavigateFabClick() {
        GPSCoordinatesDialog dialog = new GPSCoordinatesDialog();
        dialog.show(getChildFragmentManager(), GPSCoordinatesDialog.TAG);
    }

    @Override
    public void onNewUserGPSCoordinates(Location location) {
        this.targetLocation = location;

        navigateFab.onProgressVisibilityChanged(true);

        locationProvider.start();
    }

    private void subscribeProviders() {
        azimuthProvider
                .getObservable()
                .map(value -> -value)
                .map(Math::toDegrees)
                .map(Double::floatValue)
                .map(angle -> angle < 0 ? angle + 360 : angle)
                .retry()
                .subscribe(compassView::setNeedleAngle);

        locationProvider
                .getObservable()
                .map(location -> location.bearingTo(targetLocation))
                .doOnNext(bearing -> navigateFab.onProgressVisibilityChanged(false))
                .doOnNext(bearing -> compassView.setGuideArrowEnabled(true))
                .retry((integer, throwable) -> {
                    return handleGPSError();
                })
                .subscribe(compassView::setGuideArrowAngle);
    }

    private boolean handleGPSError() {
        if (getView() != null)
            Snackbar.make(getView(), R.string.fragment_compass_gps_error, Snackbar.LENGTH_SHORT).show();

        return true;
    }

    private void initialize() {
        azimuthProvider = new AzimuthProvider(getActivity());
        locationProvider = new LocationProvider(getActivity());
    }

    private void setupView() {
        if (!deviceHasGPS())
            navigateFab.setVisibility(View.GONE);
    }

    private boolean deviceHasGPS() {
        PackageManager packageManager = getActivity().getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    private void restoreState(Bundle bundle) {
        if (bundle != null) {
            if (bundle.containsKey(LOCATION_PARCELABLE_KEY))
                targetLocation = bundle.getParcelable(LOCATION_PARCELABLE_KEY);
        }
    }

}


