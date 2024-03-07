package com.example.planthelper;

import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHostHelper;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.planthelper.databinding.ActivityMainBinding;

public class MenuHelper extends MainActivity {

    public MenuHelper() {
        Log.i("MYDEBUG", "menu helper init");
    }

    public void setMenuVis(boolean setTo) {
        Log.i("MYDEBUG", "set menu vis in helper start");
        setMenuItemVis(setTo);
//
//        if (MainActivity.getMenuCreated()) {
//            Log.i("MYDEBUG", "menu exists");
//            invalidateOptionsMenu();
//        }

    }

}

