package com.example.ruben.turapp;

import android.Manifest;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    // Request-koder for permissions
    private static final int REQUEST_CODE_FINE_LOCATION = 0;
    private static final int REQUEST_CODE_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE_INTERNET = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        * 3.  LocationService for siste location
        * 4. Sjekk at mBildePath eksisterer i minnet
        * 4. Hent Tur fra MySQL med REST-API. Sorter etter hvor langt unna currPos de er.
        * 5. Sync SQLite til MySQL. SyncFragment
        * 6. InstillingerFragment - logininfo og default zoom value
        * 6. Hent turtyper fra MySQL rest api
        * 7. Legg med SQL scirpt
        *
         */

        // TODO: SQLIte DB må vite om alle turtyper, lagres lokalt?
        // TODO: FTP bilde til server, få lagringsURL tilbake

        // TODO: get settings
        // Ber om permissions for Coarse og Fine locations, og Internet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_FINE_LOCATION);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_COARSE_LOCATION);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, REQUEST_CODE_INTERNET);
        }

        Fragment fragment = new TurListeFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction().addToBackStack(null);
        transaction.replace(R.id.activity_main_content_fragment, fragment);
        transaction.commit();
    }

    // Lager menyen til action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    // Håndterer meny actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_menu_sync:
                // TODO: attempt to sync the SQLite DB with the API
                return true;

            case R.id.action_bar_menu_settings:
                // TODO: go to settings fragment
                return true;


            default:
                return true;

        }
    }

}
