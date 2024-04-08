package com.example.planthelper;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.planthelper.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static boolean menuVisible = true;
    private static boolean menuCreated = false;

    private String[] permissionNames = {android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT};
    private static int[] permissionStatus = {0, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.planthelper.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();


        assert actionBar != null;
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);


        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //do nothing
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

//        for (int i = 0; i < permissionNames.length; i++) {
//            checkPermission(permissionNames[i], i);
//        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuCreated = true;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.action_global_settingsFragment);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem = menu.findItem(R.id.action_settings);
        menuItem.setVisible(menuVisible);

        return true;
    }

    public static void setMenuItemVis(boolean setTo) {
        menuVisible = setTo;
    }

    public static boolean getMenuCreated() {
        return menuCreated;
    }

//    public void checkPermission(String permission, int requestCode)
//    {
//        // Checking if permission is not granted
//        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
//        }
//        else {
//            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // This function is called when user accept or decline the permission.
//// Request Code is used to check which permission called this function.
//// This request code is provided when user is prompt for permission.
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults)
//    {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        permissionStatus[requestCode] = grantResults[0];
//    }
//
//    public static boolean hasPermissions() {
//        boolean res = true;
//
//        for (int status : permissionStatus) {
//            if (status == 0) {
//                res = false;
//                break;
//            }
//        }
//
//        return res;
//    }

}