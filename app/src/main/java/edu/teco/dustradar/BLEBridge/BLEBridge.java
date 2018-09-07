package edu.teco.dustradar.blebridge;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import edu.teco.dustradar.R;
import edu.teco.dustradar.bluetooth.BLEScan;
import edu.teco.dustradar.bluetooth.BLEService;
import edu.teco.dustradar.data.DataService;
import edu.teco.dustradar.gps.GPSService;

public class BLEBridge extends AppCompatActivity {

    private static final String TAG = BLEBridge.class.getSimpleName();

    private BLEScan bleScan;

    private Long lastTimestamp;
    private boolean inSettings;


    // request codes
    private final int BLE_ENABLE_REQUEST_CODE = 1;
    private final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 2;
    private final int GPS_LOCATION_PERMISSION_REQUEST_CODE = 3;


    // event handlers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blebridge);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            return;
        }

        bleScan = new BLEScan(this);
        if (! bleScan.hasBluetooth()) {
            return;
        }

        PreferenceManager.setDefaultValues(this, R.xml.fragment_blebridge_settings, false);
        inSettings = false;

        BLEBridgeScan firstFragment = new BLEBridgeScan();
        firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, firstFragment).commit();
    }


    @Override
    public void onResume() {
        super.onResume();

        lastTimestamp = System.currentTimeMillis();
        makePermissionChecks();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BLE_ENABLE_REQUEST_CODE:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "You have to enable BLE to use this mode.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case GPS_LOCATION_PERMISSION_REQUEST_CODE:
                if (!GPSService.hasHighAccuracyPermission(this)) {
                    Toast.makeText(this, "You have to enable high accuracy location services to use this mode.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You have to allow location access to use this mode.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blebridge, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            if (!inSettings) {
                inSettings = true;

                BLEBridgeSettings settingsFragment = new BLEBridgeSettings();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, settingsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (inSettings) {
            inSettings = false;
            super.onBackPressed();
            return;
        }

        Long currentTimestamp = System.currentTimeMillis();
        int minBackDifference = 300;
        if ((currentTimestamp - lastTimestamp) > minBackDifference) {
            Snackbar.make(findViewById(R.id.blebridge_content), "Tap twice and fast to exit",
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
            lastTimestamp = currentTimestamp;
            return;
        }

        stopServices();
        super.onBackPressed();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "BLEBridge destroyed");
        stopServices();
        super.onDestroy();
    }


    // public methods

    public void InitiateBLEConnection(BluetoothDevice device) {
        startServices(device);
    }


    // private methods

    private void makePermissionChecks() {
        if (! bleScan.hasBluetooth()) {
            Toast.makeText(this, "BLE is not supported on your device.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bleScan.enable(this, BLE_ENABLE_REQUEST_CODE);
        bleScan.requestLocationPermission(this, FINE_LOCATION_PERMISSION_REQUEST_CODE);
        GPSService.requestHighAccuracyPermission(this, GPS_LOCATION_PERMISSION_REQUEST_CODE);
    }


    private void startServices(BluetoothDevice device) {
        if (GPSService.isRunning(this)) {
            Log.d(TAG, "Service is already running");
            GPSService.stopService(this);
        }

        if (BLEService.isRunning(this)) {
            Log.d(TAG, "Service is already running");
            try {
                unregisterReceiver(mBLEReceiver);
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            BLEService.stopService(this);
        }

        if (DataService.isRunning(this)) {
            Log.d(TAG, "Service is already running");
            DataService.stopService(this);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BLEService.BROADCAST_FIRST_CONNECT);
        filter.addAction(BLEService.BROADCAST_MISSING_SERVICE);
        registerReceiver(mBLEReceiver, filter);

        GPSService.startService(this);
        BLEService.startService(this, device);
        DataService.startService(this);
    }


    private void stopServices() {
        try {
            unregisterReceiver(mBLEReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        DataService.stopService(this);
        BLEService.stopService(this);
        GPSService.stopService(this);
    }


    // BroadcastReceivers

    private final BroadcastReceiver mBLEReceiver = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BLEService.BROADCAST_FIRST_CONNECT.equals(action)) {
                // start BLEBridgeHandler Fragment
                BLEBridgeHandler handlerFragment = new BLEBridgeHandler();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, handlerFragment);
                transaction.commit();
                return;
            }

            if (BLEService.BROADCAST_MISSING_SERVICE.equals(action)) {
                Log.w(TAG, "user selected unsupported device");
                Toast.makeText(context, "You have to select a DustTracker device.",
                        Toast.LENGTH_LONG).show();
                stopServices();
                finish();
                return;
            }
        }
    });

}
