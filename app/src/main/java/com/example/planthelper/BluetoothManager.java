package com.example.planthelper;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BluetoothManager extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothLeScanner bluetoothLeScanner;
    private final Handler handler = new Handler();
    private boolean scanning = false;
    private BluetoothDevice device;
    private final ScanFilter scanFilter;
    private final ScanSettings scanSettings;
    private BluetoothGatt bluetoothGatt;

    private final Context context;
    private final Activity activity;

    private final String[] permissionNames = {android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT};
    private static final int[] permissionStatus = {0, 0, 0};


    private boolean isConnected;
    private boolean isTimeout;


    private final String SERVICE_UUID_SCAN = "e1f6fa49-170d-4629-bd77-ea170a0309dc";
    private final String CHARACTERISTIC_UUID_SCAN = "27ce423b-bcc6-4fe7-aa98-0d502e79f15a";
    private final String CHARACTERISTIC_UUID_SCAN_NAME = "9a61b953-5e81-45f7-b998-4c54d213e710";
    private final String CHARACTERISTIC_UUID_SCAN_NEW = "ffeca8e2-4285-4949-9819-66364ef72d25";

    private final String SERVICE_UUID_WIFI_SELECT = "c3313912-4f44-4c5d-abe4-6a99dbe5274f";
    private final String CHARACTERISTIC_UUID_WIFI_NAME = "62a140cc-0bcf-49da-94e3-fba705938272";
    private final String CHARACTERISTIC_UUID_WIFI_PASSWORD = "1603d3a3-7500-4e90-8310-6e7b12e7bfa4";
    private final String CHARACTERISTIC_UUID_WIFI_SET = "1675752a-2a8e-48e8-99ef-ee90ff878d0f";
    private final String CHARACTERISTIC_UUID_WIFI_CONNECTED = "792920e9-f1ff-4587-879a-8cefb09c18d2";

    private final String SERVICE_UUID_CONNECT = "a676b1bc-61f9-4af6-8dae-28831bad2ec6";
    private final String CHARACTERISTIC_UUID_DEVICE = "9fbf707f-c39a-418a-828c-6e291af51ac4";


    /**
     * Class that manages all Bluetooth interaction with the ESP32
     *
     * @param c Context to use.
     * @param a Activity to use.
     */
    public BluetoothManager(Context c, Activity a) {
        //init
        this.context = c;
        this.activity = a;
        this.isConnected = false;
        this.isTimeout = false;

        // Check permissions
        for (int i = 0; i < permissionNames.length; i++) {
            checkPermission(permissionNames[i], i);
        }

        // filter scan results to the device id
        // TODO: replace device id with service uuid
        ScanFilter.Builder scanBuilder = new ScanFilter.Builder();
        scanBuilder.setDeviceAddress("40:4C:CA:41:59:0E");
        scanFilter = scanBuilder.build();

        if (scanFilter == null) {
            Log.i("MINE", "scanFilter is null");
        }

        // set scan mode to low latency (the scan is only running for +- 15sec)
        // set the callback type to first match to avoid getting callbacks multiple times for each
        // detected device
        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
        settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        settingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH);
        scanSettings = settingsBuilder.build();

        if (scanSettings == null) {
            Log.i("MINE", "scanSettings is null");
        }


        // setup bt adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        } else {
            // Show turn on BT message
            // TODO: turn on bt automatically instead of showing a toast message
            Toast.makeText(context, "Please enable Bluetooth to continue.", Toast.LENGTH_SHORT).show();
            Log.i("MINE", "bt not turned on");
        }
    }


    /**
     * Scan for and connect to the esp32
     * <p>
     * Initialises the bt scan that connects to the esp32 on detection.
     * @see #leScanCallback
     */
    public void scanForDevice() {
        if (!scanning) {
            long SCAN_PERIOD = 10000; // 10 sec

            // add a runnable to the handler that checks bt permission and stops the BT scan after SCAN_PERIOD milliseconds
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    // permission check and request
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{permissionNames[0]}, 0);
                        return;
                    }
                    bluetoothLeScanner.stopScan(leScanCallback);
                    if (!checkConnected()) {
                        isTimeout = true;
                    }
//                    if (!checkConnected())
//                        Toast.makeText(context, "No Plant Helper device detected", Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);

            // start the bt scan
            scanning = true;
            isTimeout = false;
            isConnected = false;
            bluetoothLeScanner.startScan(Collections.singletonList(scanFilter), scanSettings, leScanCallback);
        } else {
            //stop bt scan
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }


    /**
     * Get bt connection status
     *
     * @return Bt connection status (true if connected, false if not)
     */
    public boolean checkConnected() {
        return isConnected;
    }

    /**
     * Get bt timeout status
     *
     * @return Bt timeout status (true if timeout, false if not)
     */
    public boolean checkTimeout() {
        return isTimeout;
    }


    /**
     * Get the name of the connected device
     *
     * @return Device name
     */
    public String readName() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permissionNames[2]}, 2);
            return "Name not found";
        }

        return device.getName();
    }


    /**
     * Read the WiFi network names sent by the esp32
     *
     * @return List of network names
     */
    public List<String> readNetworks() {
        List<String> networks = new ArrayList<>();

        networks.add("Add names like this");

        // remember to update the scan_new characteristic after reading each name

        return networks;
    }


    /**
     * Send the selected wifi name and password to the esp32
     *
     * @param ssid Network name.
     * @param password Network password.
     * @return WiFi connection status (true if connected, false if not)
     */
    public boolean connectWifi(String ssid, String password) {
        boolean connected = false;

        return connected;
    }

    /**
     * Check and request permissions.
     * <p>
     * Permission names and request codes correspond to the permissionNames array
     *
     * @param permission Permission to check.
     * @param requestCode Permission request code.
     */
    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(activity, new String[] { permission }, requestCode);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        permissionStatus[requestCode] = grantResults[0];
    }


    /**
     * Bluetooth LE scan callback.
     * <p>
     * Connects to the esp32 and stops the running bt scan on detection.
     * @see #gattCallback
     */
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            // permission checks and requests
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{permissionNames[0]}, 0);
                return;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{permissionNames[2]}, 2);
                return;
            }

            bluetoothLeScanner.stopScan(leScanCallback);

            String temp = result.toString();
            Log.i("MINE", temp);
            // TODO: remove this ^

            // connect to the esp32
            device = result.getDevice();
            bluetoothGatt = device.connectGatt(context, false, gattCallback);

        }
    };


    /**
     * Bluetooth gatt callback.
     * <p>
     * Called when connecting to the esp32. Updates the connection status.
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("MINE", "at gatt callback");
            Log.i("MINE", String.valueOf(newState));
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                isConnected = true;
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                isConnected = false;
            }
        }
    };




}
