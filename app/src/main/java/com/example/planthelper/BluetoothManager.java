package com.example.planthelper;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
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

public class BluetoothManager extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Handler handler = new Handler();
    private boolean hasScanner = false;
    private boolean scanning = false;
    private final long SCAN_PERIOD = 10000; // 10 sec
    private BluetoothLeService bluetoothLeService;
    private BluetoothDevice device;
    private ScanFilter scanFilter;

    private final String SERVICE_UUID_SCAN = "e1f6fa49-170d-4629-bd77-ea170a0309dc";


    public BluetoothManager() {//Activity activity) {
        //init
        ScanFilter.Builder scanBuilder = new ScanFilter.Builder();
        scanBuilder.setServiceUuid(ParcelUuid.fromString(SERVICE_UUID_SCAN));
        scanFilter = scanBuilder.build();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {

            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            hasScanner = true;
        } else {
            // Show turn on BT message
        }
    }

    private void scanForDevice() {
        if (!scanning) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
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
                }
            }, SCAN_PERIOD);
            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        }
        else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // Add on result code here
            device = result.getDevice();

        }
    };


    public boolean checkConnected() {
        boolean isConnected = false;

        return isConnected;
    }

    public String readName() {
        String name = "";

        return name;
    }

    public List<String> readNetworks() {
        List<String> networks = new ArrayList<>();

        networks.add("Add names like this");

        return networks;
    }

    public boolean connectWifi(String ssid, String password) {
        boolean connected = false;

        return connected;
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
