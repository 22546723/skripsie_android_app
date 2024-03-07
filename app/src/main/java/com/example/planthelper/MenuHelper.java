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

public class MenuHelper extends MainActivity { // AppCompatActivity {
    public MenuItem settings_item;
    private AppBarConfiguration appBarConfiguration;
    private boolean menuVisible;
    private Activity activity;

    public MenuHelper() {
        Log.i("MYDEBUG", "meep");
    }

    public void setMenuVis(boolean setTo) {
        this.menuVisible = setTo;
        Log.i("MYDEBUG", "please get to here");
        setMenuItemVis(setTo);
        invalidateOptionsMenu();
    }

}

