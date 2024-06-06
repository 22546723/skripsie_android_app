package com.example.planthelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BluetoothManager extends AppCompatActivity {
    private BluetoothLeScanner bluetoothLeScanner;
    private final Handler handler = new Handler();
    private boolean scanning = false;
    private final ScanFilter scanFilter;
    private final ScanSettings scanSettings;
    private BluetoothGatt bluetoothGatt;

    private Context context;
    private Activity activity;

    private final String[] permissionNames = {android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT};

    private boolean isConnected;
    private boolean isTimeout;


    private BluetoothGattCharacteristic scanName;
    private BluetoothGattCharacteristic scanNew;
    private BluetoothGattCharacteristic scanStatus;

    private BluetoothGattCharacteristic wifiName;
    private BluetoothGattCharacteristic wifiName2;
    private BluetoothGattCharacteristic wifiPassword;
    private BluetoothGattCharacteristic wifiPassword2;
    private BluetoothGattCharacteristic wifiSet;
    private BluetoothGattCharacteristic wifiConnected;

    private BluetoothGattCharacteristic connectName;
    private BluetoothGattCharacteristic connectedSsid;

    private boolean readDone;
    private boolean writeDone;
    private String charVal;

    private Runnable runnable;

    private final boolean btOnFlag;


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
        ScanFilter.Builder scanBuilder = new ScanFilter.Builder();
        scanBuilder.setDeviceAddress("40:4C:CA:41:59:0E");
        scanFilter = scanBuilder.build();

        if (scanFilter == null) {
            Log.i("MINE", "scanFilter is null");
        }

        // set scan mode to low latency (the scan is only running for +- 15sec)
        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
        settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        scanSettings = settingsBuilder.build();


        // setup bt adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            btOnFlag = true;
        } else {
            // Show turn on BT message
            Toast.makeText(context, "Please enable Bluetooth to continue.", Toast.LENGTH_SHORT).show();
            btOnFlag = false;
        }

    }

    public boolean isBtOn() {
        return btOnFlag;
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

            // add a runnable to the handler that checks bt permission and stops the BT scan after
            // SCAN_PERIOD milliseconds
            runnable = () -> {
                scanning = false;
                // permission check and request
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{permissionNames[0]}, 0);
                }
                bluetoothLeScanner.stopScan(leScanCallback);
                if (!checkConnected()) {
                    isTimeout = true;
                }
            };

            handler.postDelayed(runnable, SCAN_PERIOD);

            // start the bt scan
            scanning = true;
            isTimeout = false;
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
        }

        return readCharacteristic(connectName);
    }

    public void setName(String name) {
        writeCharacteristic(connectName, name);
    }

    public String getConnectedSsid() {
        return readCharacteristic(connectedSsid);
    }


    /**
     * Initialize the esp32 WiFi scan and reads the detected networks
     *
     * @return List of available network names
     */
    public List<String> readNetworks() {
        List<String> networks = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permissionNames[2]}, 2);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // initialise scan
            writeCharacteristic(scanStatus, "1");

            // Read available networks
            String newName;
            String name;
            boolean done = false;
            while (!done) {
                newName = readCharacteristic(scanNew);
                if (newName.equals("1")) {
                    name = readCharacteristic(scanName);
                    networks.add(name);
                    writeCharacteristic(scanNew, "0");

                }
                if (readCharacteristic(scanStatus).equals("0")) {
                    done = true;
                }
            }
        }

        return networks;
    }


    /**
     * Set the value of a BLE characteristic
     *
     * @param characteristic BLE characteristic to write to
     * @param value Value to write to the characteristic
     */
    @SuppressLint("MissingPermission")
    private void writeCharacteristic(BluetoothGattCharacteristic characteristic, String value) {
        writeDone = false;
        int delay = 100; //ms
        final int[] writeCount = {0};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt.writeCharacteristic(characteristic, value.getBytes(), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }

        while (!writeDone) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    writeCount[0] += 1;
                    if (writeCount[0] >= 10) {
                        writeDone = true;
                    }

                    if (!writeDone) {
                        handler.postDelayed(this, delay);
                    }
                }
            }, delay);
        }
    }


    /**
     * Read the value of a BLE characteristic
     *
     * @param characteristic BLE characteristic to read
     * @return Value of the characteristic
     */
    @SuppressLint("MissingPermission")
    private String readCharacteristic(BluetoothGattCharacteristic characteristic) {
        readDone = false;
        int delay = 100; //ms
        final int[] readCount = {0};

        bluetoothGatt.readCharacteristic(characteristic);

        // 1 sec read delay
        while (!readDone) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    readCount[0] += 1;
                    if (readCount[0] >= 10) {
                        readDone = true;
                    }

                    if (!readDone) {
                        handler.postDelayed(this, delay);
                    }
                }
            }, delay);
        }
        return charVal;  // charVal gets set in the onCharacteristicRead callback
    }


    /**
     * Update the activity and context of this BluetoothManager
     *
     * @param a Activity
     * @param c Context
     */
    public void setActCont(Activity a, Context c) {
        activity = a;
        context = c;
    }


    /**
     * Send the selected wifi name and password to the esp32
     *
     * @param ssid Network name.
     * @param password Network password.
     * @return WiFi connection status (true if connected, false if not)
     */
    public boolean connectWifi(@NonNull String ssid, String password) {
        boolean connected;

        String name1;
        String name2 = "";
        String password1;
        String password2 = "";

        // The esp32 can only read up to 20 characters from a single characteristic, so long SSIDs
        // and passwords need to be split up
        if (ssid.length() > 20) {
            name1 = ssid.substring(0, 20);
            name2 = ssid.substring(20);
        }
        else {
            name1 = ssid;
        }

        if (password.length() > 20) {
            password1 = password.substring(0, 20);
            password2 = password.substring(20);
        }
        else {
            password1 = password;
        }

        writeCharacteristic(wifiName, name1);
        writeCharacteristic(wifiName2, name2);
        writeCharacteristic(wifiPassword, password1);
        writeCharacteristic(wifiPassword2, password2);
        writeCharacteristic(wifiSet, "1");

        // Wait for the esp32 to finish attempting to connect to wifi
        boolean done = false;
        while (!done) {
            done = readCharacteristic(wifiSet).equals("0");
        }

        connected = readCharacteristic(wifiConnected).equals("1");
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
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }


    /**
     * Setup service and characteristic variables.
     */
    public void setupChars() {
        // Setup scan service
        String SERVICE_UUID_SCAN = "e1f6fa49-170d-4629-bd77-ea170a0309dc";
        BluetoothGattService scanService = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID_SCAN));
        String CHARACTERISTIC_UUID_SCAN_NAME = "9a61b953-5e81-45f7-b998-4c54d213e710";
        scanName = scanService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_SCAN_NAME));
        String CHARACTERISTIC_UUID_SCAN_NEW = "ffeca8e2-4285-4949-9819-66364ef72d25";
        scanNew = scanService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_SCAN_NEW));
        String CHARACTERISTIC_UUID_SCAN = "27ce423b-bcc6-4fe7-aa98-0d502e79f15a";
        scanStatus = scanService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_SCAN));

        // Setup WiFi service
        String SERVICE_UUID_WIFI_SELECT = "c3313912-4f44-4c5d-abe4-6a99dbe5274f";
        BluetoothGattService wifiService = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID_WIFI_SELECT));
        String CHARACTERISTIC_UUID_WIFI_NAME = "62a140cc-0bcf-49da-94e3-fba705938272";
        wifiName = wifiService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_WIFI_NAME));
        String CHARACTERISTIC_UUID_WIFI_NAME_2 = "2b8787a9-0908-48e5-90ed-18208cf9c197";
        wifiName2 = wifiService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_WIFI_NAME_2));
        String CHARACTERISTIC_UUID_WIFI_PASSWORD = "1603d3a3-7500-4e90-8310-6e7b12e7bfa4";
        wifiPassword = wifiService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_WIFI_PASSWORD));
        String CHARACTERISTIC_UUID_WIFI_PASSWORD_2 = "55799be4-bdcf-4b97-ac6f-783dc4de4757";
        wifiPassword2 = wifiService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_WIFI_PASSWORD_2));
        String CHARACTERISTIC_UUID_WIFI_SET = "1675752a-2a8e-48e8-99ef-ee90ff878d0f";
        wifiSet = wifiService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_WIFI_SET));
        String CHARACTERISTIC_UUID_WIFI_CONNECTED = "792920e9-f1ff-4587-879a-8cefb09c18d2";
        wifiConnected = wifiService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_WIFI_CONNECTED));

        // Setup connect service
        String SERVICE_UUID_CONNECT = "a676b1bc-61f9-4af6-8dae-28831bad2ec6";
        BluetoothGattService connectService = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID_CONNECT));
        String CHARACTERISTIC_UUID_DEVICE = "9fbf707f-c39a-418a-828c-6e291af51ac4";
        connectName = connectService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_DEVICE));
        String CHARACTERISTIC_CONNECTED_SSID = "ee2a9fb2-7f5b-4b84-be0b-b54e4c7e2863";
        connectedSsid = connectService.getCharacteristic(UUID.fromString(CHARACTERISTIC_CONNECTED_SSID));

        isConnected = true;
    }


    /**
     * Disconnect from the esp32
     */
    public void disconnect() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permissionNames[2]}, 2);
            return;
        }
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }


    /**
     * Bluetooth gatt callback.
     * <p>
     * Called when connecting to the esp32. Updates the connection status.
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                setupChars();
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{permissionNames[2]}, 2);
                    return;
                }

                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                bluetoothGatt.close();
                bluetoothGatt = null;
                isConnected = false;
            }
        }


        @Override
        public void onCharacteristicWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            writeDone = true;
        }


        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            super.onCharacteristicRead(gatt, characteristic, value, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                charVal = new String(value, StandardCharsets.UTF_8);
            }
            readDone = true;
        }
    };


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
            handler.removeCallbacks(runnable);


            if (bluetoothGatt != null) {
                return;
            }

            if (checkConnected()) {
                return;
            }

            // connect to the esp32
            BluetoothDevice device = result.getDevice();

            if (device == null) {
                Toast.makeText(context, "Device unavailable, please restart the Plant Helper device.", Toast.LENGTH_SHORT).show();
            }
            else {
                bluetoothGatt = device.connectGatt(context, false, gattCallback);
            }

        }
    };

}
