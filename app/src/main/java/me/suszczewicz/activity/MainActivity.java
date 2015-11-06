package me.suszczewicz.activity;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import me.suszczewicz.compass.BuildConfig;
import me.suszczewicz.compass.R;
import me.suszczewicz.fragment.CompassFragment;
import me.suszczewicz.fragment.DeviceNotSupportedFragment;
import me.suszczewicz.util.AndroidUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFabric();

        requestDeviceDefaultOrientation();
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null)
            setupContent();
    }

    private void setupContent() {
        Fragment content = getContentFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.activity_main_root, content)
                .commit();
    }

    private Fragment getContentFragment() {
        return isDeviceSupported()
                ? new CompassFragment()
                : new DeviceNotSupportedFragment();
    }

    private boolean isDeviceSupported() {
        PackageManager manager = getPackageManager();

        return manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
                && manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    }

    private void requestDeviceDefaultOrientation() {
        switch (AndroidUtil.getDeviceDefaultOrientation(this)) {
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            default:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void setupFabric() {
        if (!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());
    }
}
