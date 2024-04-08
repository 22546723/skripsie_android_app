package com.example.planthelper;

import static androidx.core.app.ActivityCompat.startActivityForResult;

//import static com.example.planthelper.MainActivity.REQUEST_ENABLE_BT;

import static com.google.android.gms.common.util.ArrayUtils.contains;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//class BluetoothLeService extends Service {
//    private Binder binder = new LocalBinder();
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return binder;
//    }
//
//    class LocalBinder extends Binder {
//        public BluetoothLeService getService() {
//            return BluetoothLeService.this;
//        }
//    }
//}

@RequiresApi(api = Build.VERSION_CODES.S)
public class BluetoothManager extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Handler handler = new Handler();
    private boolean hasScanner = false;
    private boolean scanning = false;
    private final long SCAN_PERIOD = 15000; // 10 sec
    private BluetoothLeService bluetoothLeService;
    private BluetoothDevice device;
    private ScanFilter scanFilter;
    private ScanSettings scanSettings;
    private BluetoothGatt bluetoothGatt;

    private Context context;
    private Activity activity;

    //    private String[] permissionNames = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT};
    private String[] permissionNames = {android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT};
    private static int[] permissionStatus = {0, 0, 0};


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


    public BluetoothManager(Context c, Activity a) {
        //init
        this.context = c;
        this.activity = a;
        if (c == null)
            Log.i("MINE", "empty context");

        for (int i = 0; i < permissionNames.length; i++) {
            checkPermission(permissionNames[i], i);
        }

        // filter scan results to contain the scan service uuid
        ScanFilter.Builder scanBuilder = new ScanFilter.Builder();
//        scanBuilder.setServiceUuid(ParcelUuid.fromString(SERVICE_UUID_CONNECT));
        scanBuilder.setDeviceAddress("40:4C:CA:41:59:0E");
        scanFilter = scanBuilder.build();

        if (scanFilter == null) {
            Log.i("MINE", "scanFilter is null");
        }

        // set scan mode to balanced (doesn't really make a difference but we need scan settings to
        // be able to use the filter)
        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
        settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        settingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH);
        scanSettings = settingsBuilder.build();

        if (scanSettings == null) {
            Log.i("MINE", "scanSettings is null");
        }


        // setup bt adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {

            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            hasScanner = true;
        } else {
            // Show turn on BT message
            Log.i("MINE", "bt not turned on");
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
////            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//
//            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(enableBtIntent), new ActivityResultCallback<>() {
//                @Override
//                public void onActivityResult(Object o) {
//                    Log.i("MINE", "bt on requested");
//                }
//            });
        }
    }

    public void scanForDevice() {
        if (!scanning) {
            // add a runnable to the handler that checks bt permission and stops the BT scan after SCAN_PERIOD milliseconds
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(activity, new String[]{permissionNames[0]}, 0);
                        Log.i("MINE", "no bt permission");
                        return;
                    }
                    bluetoothLeScanner.stopScan(leScanCallback);
                    Log.i("MINE", "bt scan timeout");
                }
            }, SCAN_PERIOD);

            // start the bt scan
            scanning = true;
            bluetoothLeScanner.startScan(Collections.singletonList(scanFilter), scanSettings, leScanCallback);
        } else {
            //stop bt scan
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    // bt scan callback
    // so far it just returns the detected device
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // Add on result code here

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothLeScanner.stopScan(leScanCallback);

            String temp = result.toString();
            Log.i("MINE", temp);

            device = result.getDevice();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothGatt = device.connectGatt(context, false, gattCallback);

        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Log.i("MINE", "connected to esp");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
            }
        }
    };



    // returns true if phone is connected to the esp32
    public boolean checkConnected() {
        boolean isConnected = false;

        return isConnected;
    }

    // return the esp32 device name
    public String readName() {
        String name = "";

        return name;
    }

    // read wifi names from the esp32
    public List<String> readNetworks() {
        List<String> networks = new ArrayList<>();

        networks.add("Add names like this");

        // remember to update the scan_new characteristic after reading each name

        return networks;
    }

    // send the selected wifi name and password to the esp
    public boolean connectWifi(String ssid, String password) {
        boolean connected = false;

        return connected;
    }

    public void checkPermission(String permission, int requestCode)
    {

        // Checking if permission is not granted

        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(activity, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        permissionStatus[requestCode] = grantResults[0];
    }


}

//#define SERVICE_UUID_SCAN             "e1f6fa49-170d-4629-bd77-ea170a0309dc"
//#define CHARACTERISTIC_UUID_SCAN      "27ce423b-bcc6-4fe7-aa98-0d502e79f15a"
//#define CHARACTERISTIC_UUID_SCAN_NAME "9a61b953-5e81-45f7-b998-4c54d213e710"
//#define CHARACTERISTIC_UUID_SCAN_NEW  "ffeca8e2-4285-4949-9819-66364ef72d25"
//
//#define SERVICE_UUID_WIFI_SELECT           "c3313912-4f44-4c5d-abe4-6a99dbe5274f"
//#define CHARACTERISTIC_UUID_WIFI_NAME      "62a140cc-0bcf-49da-94e3-fba705938272"
//#define CHARACTERISTIC_UUID_WIFI_PASSWORD  "1603d3a3-7500-4e90-8310-6e7b12e7bfa4"
//#define CHARACTERISTIC_UUID_WIFI_SET       "1675752a-2a8e-48e8-99ef-ee90ff878d0f"
//#define CHARACTERISTIC_UUID_WIFI_CONNECTED "792920e9-f1ff-4587-879a-8cefb09c18d2"
//
//#define SERVICE_UUID_CONNECT          "a676b1bc-61f9-4af6-8dae-28831bad2ec6"
//#define CHARACTERISTIC_UUID_DEVICE    "9fbf707f-c39a-418a-828c-6e291af51ac4"
