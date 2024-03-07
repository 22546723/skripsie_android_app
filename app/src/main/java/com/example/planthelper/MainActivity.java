package com.example.planthelper;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuInflater;
import android.view.View;

import androidx.core.view.MenuProvider;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.planthelper.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private static boolean menuVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);



    }
//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.i("MYDEBUG", "on create");
        return true;
    }
//
////    @Override
////    public boolean onPrepareOptionsMenu(Menu menu) {
////        // Inflate the menu; this adds items to the action bar if it is present.
////
////        getMenuInflater().inflate(R.menu.menu_main, menu);
////        menuItem = menu.findItem(R.id.action_settings);
////        String test = String.valueOf(menuItem.getItemId());
////        Log.i("MYDEBUG", test);
////        return true;
////    }
//
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
            setMenuItemVis(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

        @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

//        MenuItem settings_item = menu.getItem(R.id.action_settings);
//        settings_item.setVisible(this.menuVisible);
        Log.i("MYDEBUG", "pleeeeaaaase");
        MenuItem menuItem = menu.findItem(R.id.action_settings);
        menuItem.setVisible(menuVisible);


        Log.i("MYDEBUG", "maybe");
        return true;
    }

    public static void setMenuItemVis(boolean setTo) {
        Log.i("MYDEBUG", "checkpoint");
        menuVisible = setTo;
    }

}