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
import android.view.Menu;
import android.view.MenuItem;

import com.example.ruben.turapp.database.DatabaseSynk;
import com.example.ruben.turapp.database.TurDbAdapter;
import com.example.ruben.turapp.restklient.NetworkHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // Request-koder for permissions
    public static final int REQUEST_CODE_FINE_LOCATION = 0;
    public static final int REQUEST_CODE_COARSE_LOCATION = 1;
    public static final int REQUEST_CODE_INTERNET = 2;
    public static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    public static final String INGEN_NETTVERK_MELDING = "Ingen nettverkstilgang, kunne ikke synkronisere";
    public static final String TOM_DATABASE_MELDING = "Ingen innlegg å synkronisere.";
    public static final String RADER_SYNKRONISERT_MELDING = " rad(er) synkronisert.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        * 4. Sjekk at mBildePath eksisterer i minnet
        * 7. Legg med SQL scirpt
        * ListView loader ikke før posisjon er ferdig
        * 8. Laste opp/ned bilde
        *  8. STtopp at man kan klikke Back fra første fragment
        *  Gjør Instillinger fint.
        *  Bedre håndtering av permissions
        *
        *  java.lang.NullPointerException: Attempt to invoke virtual method 'java.util.Iterator java.util.ArrayList.iterator()' on a null object reference
        *  Sjekk om appen er koblet til nettet ved launch!
        */

        // Ber om permissions for Coarse og Fine locations, og Internet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
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

        NetworkHelper helper = new NetworkHelper(this);
        if (helper.isOnline()) {
            TurDbAdapter turDbAdapter = new TurDbAdapter(getApplicationContext());
            turDbAdapter.open();

            Cursor alleTurer = turDbAdapter.hentAlleTurer();
            int antallRader = alleTurer.getCount();
            if (antallRader > 0) {
                DatabaseSynk dbSynk = new DatabaseSynk(turDbAdapter);
                JSONArray alleRader = dbSynk.cursorTilJSONArray(alleTurer);
                for (int i = 0; i < antallRader; i++) {
                    JSONObject rad = alleRader.getJSONObject(i);
                    dbSynk.send(rad);
                }
                Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), antallRader + RADER_SYNKRONISERT_MELDING, Snackbar.LENGTH_LONG).show();
            }
            else {
                // Ingen rader å synkronisere
                Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), TOM_DATABASE_MELDING, Snackbar.LENGTH_LONG).show();
            }
        }
        else {
            // Ingen nettverksforbindelse, ikke prøv å synkronisere
            Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Ingen nettverkstilgang, kunne ikke synkronisere.", Snackbar.LENGTH_LONG).show();
        }
    }

}
