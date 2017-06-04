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
import android.view.Menu;
import android.view.MenuItem;

import com.example.ruben.turapp.database.DatabaseSynk;
import com.example.ruben.turapp.database.TurDbAdapter;
import com.example.ruben.turapp.restklient.NetworkHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * MainActivity som styrer alle fragmentene i appen.
 */
public class MainActivity extends AppCompatActivity {

    // Statiske variabler og meldinger for appen.
    public static final int REQUEST_CODE_FINE_LOCATION = 0;
    public static final int REQUEST_CODE_COARSE_LOCATION = 1;
    public static final int REQUEST_CODE_INTERNET = 2;
    public static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    public static final String TOM_DATABASE_MELDING = "Ingen innlegg å synkronisere.";
    public static final String RADER_SYNKRONISERT_MELDING = " rad(er) synkronisert.";
    public static final String POSISJON_LAT_TEKST = "Breddegrad: ";
    public static final String POSISJON_LNG_TEKST = "Lengdegrad: ";
    public static final String POSISJON_MOH_TEKST = "Moh.: ";
    public static String JSON_FEIL_MELDING = "Noe gikk galt med overføringen av data.";

    private static String INGEN_NETTVERK_MELDING = "Ingen nettverksforbindelse, kunne ikke synkronisere.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        *
        *  Legg ved SQL-skript

        *  Lage skjermbilder
        *  */


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

        // Sjekker om det er en savedInstanceState i minnet
        if (savedInstanceState == null) {

            // Hvis ingen savedInstance, åpne default fragment
            Fragment fragment = new TurListeFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.activity_main_content_fragment, fragment);
            transaction.commit();
        } else {
            // Åpne savedInstance fragment
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(savedInstanceState.getString("TAG"));
        }


    }

    /**
     * Lager menyen til ActionBar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    /**
     * Håndterer input på menyen
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            // Synkroniser
            case R.id.action_bar_menu_sync:
                try {
                    // Prøv å synkroniser lokal SQLite-database med online MySQL-database
                    synkroniserDatabaser();
                } catch (JSONException e) {
                    Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), JSON_FEIL_MELDING, Snackbar.LENGTH_LONG).show();
                }
                return true;

            // Åpne settings
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

    /**
     * Prøver å synkronsiere lokal database med online database
     */
    private void synkroniserDatabaser() throws JSONException {

        // Sjekker om appen er online før den prøver å synkronisere
        NetworkHelper helper = new NetworkHelper(this);
        if (helper.isOnline()) {

            // Starter et dbADapter
            TurDbAdapter turDbAdapter = new TurDbAdapter(getApplicationContext());
            turDbAdapter.open();

            // Cursor-objekt som inneholder alle turer i lokal database
            Cursor alleTurer = turDbAdapter.hentAlleTurer();
            int antallRader = alleTurer.getCount();
            if (antallRader > 0) {

                // Synker hver rad i Cursor
                DatabaseSynk dbSynk = new DatabaseSynk(turDbAdapter);
                JSONArray alleRader = dbSynk.cursorTilJSONArray(alleTurer);
                for (int i = 0; i < antallRader; i++) {
                    JSONObject rad = alleRader.getJSONObject(i);
                    dbSynk.send(rad);
                }
                Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), antallRader + RADER_SYNKRONISERT_MELDING, Snackbar.LENGTH_LONG).show();
            } else {
                // Ingen rader å synkronisere
                Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), TOM_DATABASE_MELDING, Snackbar.LENGTH_LONG).show();
            }
        } else {
            // Ingen nettverksforbindelse, ikke prøv å synkronisere
            Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), INGEN_NETTVERK_MELDING, Snackbar.LENGTH_LONG).show();
        }
    }
}
