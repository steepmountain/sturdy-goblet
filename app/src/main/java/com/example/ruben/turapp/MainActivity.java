package com.example.ruben.turapp;

import android.Manifest;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ruben.turapp.database.DatabaseSynk;
import com.example.ruben.turapp.database.TurDbAdapter;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    // Request-koder for permissions
    private static final int REQUEST_CODE_FINE_LOCATION = 0;
    private static final int REQUEST_CODE_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE_INTERNET = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* ListView viser hvor mange meter til hver
        * 3.  LocationService for siste location
        *     Sett min lokasjon på kartet
        * 4. Sjekk at mBildePath eksisterer i minnetSorter etter hvor langt unna currPos de er.
        * 6. InstillingerFragment - logininfo og default zoom value
        * 6. Unike NAVN i tabeller?
        * 7. Legg med SQL scirpt
        *  8. STtopp at man kan klikke Back fra første fragment
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
                try {
                    synkroniserDatabaser();
                } catch (JSONException e) {
                    // TODO: static final feilmelding
                    Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Noe gikk galt med overføringen av data!", Snackbar.LENGTH_LONG).show();
                }
                return true;


            case R.id.action_bar_menu_settings:
                Fragment fragment = new InstillingerFragment();
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction().addToBackStack(null);
                transaction.replace(R.id.activity_main_content_fragment, fragment);
                transaction.commit();
                return true;


            default:
                return true;

        }
    }

    // Kaller på et DatabaseSynk-objekt som gjør om spørringen av DB i fra Cursor til JSON og prøver å sende det til online DB.
    private void synkroniserDatabaser() throws JSONException {

        TurDbAdapter turDbAdapter = new TurDbAdapter(getApplicationContext());
        turDbAdapter.open();

        Cursor alleTurer = turDbAdapter.hentAlleTurer();
        int antallRader = alleTurer.getCount();
        if (antallRader > 0) {
            // Hvis flere enn 0 rader, prøv å send til DB
            DatabaseSynk dbSynk = new DatabaseSynk(getApplicationContext(), turDbAdapter);
            JSONArray toSend = dbSynk.cursorTilJSONArray(alleTurer);
            dbSynk.send(toSend);
            Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Synkroniserte " + antallRader + " inlegg.", Snackbar.LENGTH_LONG).show();
        } else {
            // Ingen rader i Cursor, betyr at DB er tom
            Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Ingen innlegg å synkronisere.", Snackbar.LENGTH_LONG).show();
        }

        // TODO: når skal DB lukkes?
        //turDbAdapter.close();
    }

}
