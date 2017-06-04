package com.example.ruben.turapp;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ruben.turapp.bitmap.BitmapHelper;
import com.example.ruben.turapp.database.DatabaseSynk;
import com.example.ruben.turapp.database.TurDbAdapter;
import com.example.ruben.turapp.restklient.NetworkHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class NyTurFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    // Kontekst og rootview for Snackbar
    private Activity mActivity;
    private Context mContext;
    private View rootView;
    private GoogleApiClient mGoogleApiClient;
    private Location mSistePosisjon;
    private String mBildesti;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private TextView tvMoh;
    private TextInputLayout tvNavnLabel;
    private TextInputEditText etNavn;
    private TextInputLayout tvBeskrivelseLabel;
    private TextInputEditText etBeskrivelse;
    private TextInputLayout tvTurTypeLabel;
    private TextInputEditText etTurTyrpe;
    private Button btnKamera;
    private ImageView ivThumbnail;
    private Button btnSubmit;
    private float latitude;
    private float longitude;
    private int moh;

    // Statiske verdier og meldinger
    private static final String TITTEL = "Ny tur";
    private static final int MAX_NAVN_LENGDE = 200;
    private static final int MAX_BESKRIVELSE_LENGDE = 1000;
    private static final int MAX_TURTYPE_LENGDE = 200;
    private static final int MAX_REGISTRANT_LENGDE = 200;
    private static final int TA_BILDE_REQUEST_CODE = 1;
    private static final int ACTIVITY_RESULT_OK = -1;
    private static final int ACTIVITY_RESULT_KANSELLERT = 0;
    private static final int DB_INSERT_OK = 0;
    private static final String BILDE_UGYLDIG_MELDING = "Bilde er ikke gyldig. Prøv igjen!";
    private static final String FIL_UGYLDIG_MELDING = "Kunne ikke lage en fil for bilde. Prøv igjen!";
    private static final String DB_INSERT_FEIL_MELDING = "Noe gikk galt med databasen. Prøv igjen!";
    private static final String LOGIN_UGYLDIG_MELDING = "Logg inn i Instillinger før du lager en ny tur. Navnet må være mellom 1 og " + MAX_REGISTRANT_LENGDE + " karakterer.";
    private static final String DATABASE_INSERT_OK_MELDING = "Turen er lagt inn med ID ";
    private static final String INGEN_KJENT_POSISJON_MELDING = "Ingen kjent posisjon tilgjengelig. Koble til en posisjonstjeneste og prøv igjen.";

    public NyTurFragment() {
        // Required empty public constructor
    }

    /**
     * OnCreate iverksetter GUI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_ny_tur, container, false);

        // Henter aktivitet og tittel
        mActivity = getActivity();
        mActivity.setTitle(TITTEL);


        // Henter kontekst og rootView for bruk i fragmentet
        mContext = mActivity.getApplicationContext();
        rootView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);

        // Bygger google api klient for å kunne hente siste posisjon
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // TextViews for posisjon i toppen av fragmentet. Settes til default value (0) og blir overskrevet hvis telefonen finner en faktisk posisjon
        tvLatitude = (TextView) fragment.findViewById(R.id.fragment_ny_kunde_latitude);
        tvLongitude = (TextView) fragment.findViewById(R.id.fragment_ny_kunde_longitude);
        tvMoh = (TextView) fragment.findViewById(R.id.fragment_ny_kunde_moh);
        settPosisjonsTekst(latitude, longitude, moh);

        // label og edit for navn
        tvNavnLabel = (TextInputLayout) fragment.findViewById(R.id.fragment_ny_kunde_label_navn);
        etNavn = (TextInputEditText) fragment.findViewById(R.id.fragment_ny_kunde_edit_navn);

        // label og edit for beskrivelse
        tvBeskrivelseLabel = (TextInputLayout) fragment.findViewById(R.id.fragment_ny_kunde_label_beskrivelse);
        etBeskrivelse = (TextInputEditText) fragment.findViewById(R.id.fragment_ny_kunde_edit_beskrivelse);

        // label og edit for turtype
        tvTurTypeLabel = (TextInputLayout) fragment.findViewById(R.id.fragment_ny_kunde_label_turtype);
        etTurTyrpe = (TextInputEditText) fragment.findViewById(R.id.fragment_ny_kunde_edit_turtype);

        // Setter opp button for kamera og legger til en listener for å starte en kamera-aktivitet.
        btnKamera = (Button) fragment.findViewById(R.id.fragment_ny_kunde_button_kamera);
        btnKamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taBildeMedKamera();
            }
        });

        // Sjekker om det ligger et bilde i variabelen hvis man har navigert rundt i appen uten å submitte ny tur
        ivThumbnail = (ImageView) fragment.findViewById(R.id.fragment_ny_kunde_image_thumbnail);

        // Setter opp button for submit og legger til listener for å legge til tur
        btnSubmit = (Button) fragment.findViewById(R.id.fragment_ny_kunde_button_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lagNyTur();
            }
        });

        // Sjekker om det ligger et bilde i "minnet"
        if (savedInstanceState != null) {
            mBildesti = savedInstanceState.getString("bildePath");
        }
        if (mBildesti != null) {
            settBilde(mBildesti);
        }

        return fragment;
    }

    /**
     * Setter igang et intent med telefonens Kamera og får tilbake et bilde å bruke i fragmentet.
     */
    private void taBildeMedKamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Sjekker at appen har lov til å bruke kamera
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            // Oppretter et fil-path som kamera skal lagre bilde på.
            File bilde = null;
            try {
                bilde = lagBildefil();
            } catch (Exception e) {
                Snackbar.make(rootView, FIL_UGYLDIG_MELDING, Snackbar.LENGTH_LONG).show();
            }

            // Hvis filen ble opprettet korrekt sendes den til bruk til Kamera-appen
            if (bilde != null) {
                Uri bildeUri = FileProvider.getUriForFile(mContext, MainActivity.FILE_PROVIDER_AUTHORITY, bilde);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, bildeUri);
                startActivityForResult(intent, TA_BILDE_REQUEST_CODE);
            }
        }
    }

    /**
     * Lager en fil i telefonminne som kun denne appen kan lese. Hentet ut fra Google Tutorials for bilde
     * @return gir tilbake en fil som kamera bruker til å lagre bilde på
     */
    private File lagBildefil() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filnavn = "turApp" + timeStamp;
        File mappe = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File bilde = File.createTempFile(
                filnavn,
                ".jpg",
                mappe);

        mBildesti = bilde.getAbsolutePath();
        return bilde;
    }


    /**
     * Håndterer aktivitetsresultat fra Kamera
     * @param requestCode koden for aktiviteten
     * @param resultCode resultet fra aktiviten
     * @param data inn-data fra aktiviteten
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TA_BILDE_REQUEST_CODE) {
            if (resultCode == ACTIVITY_RESULT_OK) {
                // Henter fullversjonen lagret på disk og viser den i fragmentet.
                settBilde(mBildesti);
            } else if (resultCode == ACTIVITY_RESULT_KANSELLERT) {
                // Hvis bruker kansellerer Camera action, sett bilde til tom
                mBildesti = "";
            }
        }
    }

    /**
     * Håndterer bildestørrelse og bildesetting av thumbnail ved å se på skjermstørrelse
     * @param bildesti bildestien lokalt på telefonminne
     */
    private void settBilde(String bildesti) {
        DisplayMetrics display = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(display);
        int skjermbredde = display.widthPixels;
        Bitmap nyttBilde = BitmapHelper.roterOgSkalerBitmap(bildesti, skjermbredde);
        ivThumbnail.setImageBitmap(nyttBilde);
    }

    /**
     * Fjerner all input fra fragmentet hvis turen er satt inn korrekt
     */
    private void resetTekst() {
        etBeskrivelse.setText("");
        etNavn.setText("");
        etTurTyrpe.setText("");
        ivThumbnail.setImageBitmap(null);
    }


    /**
     * Oppdaterer posisjonsvariabler
     * @param sistePosisjon et Location-objekt med brukers siste posisjon
     */
    private void settPosisjon(Location sistePosisjon) {
        if (sistePosisjon != null) {
            mSistePosisjon = sistePosisjon;
            latitude = (float) mSistePosisjon.getLatitude();
            longitude = (float) mSistePosisjon.getLongitude();
            moh = (int) mSistePosisjon.getAltitude();
            settPosisjonsTekst(latitude, longitude, moh);
        } else {
            // Ingen gyldig posisjon!
            Snackbar.make(rootView, INGEN_KJENT_POSISJON_MELDING, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Håndterer posisjonsteksten øverst i fragmentet
     * @param lat brukers breddegrad
     * @param lng brukers lengdegrad
     * @param moh brukers moh
     */
    private void settPosisjonsTekst(float lat, float lng, int moh) {
        String latTekst = MainActivity.POSISJON_LAT_TEKST + lat;
        String lngTekst = MainActivity.POSISJON_LNG_TEKST + lng;
        String mohTekst = MainActivity.POSISJON_MOH_TEKST + moh;

        tvLatitude.setText(latTekst);
        tvLongitude.setText(lngTekst);
        tvMoh.setText(mohTekst);
    }

    /**
     * Kobler til Google Api klient ved oppstart
     */
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    /**
     * Kobler fra  Google Api klient ved stenging
     */
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    /**
     * Lagrer bildedata ved rotering av fragmentet
     * @param outState et Bundle-objekt som inneholder bildepath
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Lagrer bilde hvis satt
        if (mBildesti != null && !mBildesti.isEmpty()) {
            outState.putString("bildePath", mBildesti);
        }
    }

    /**
     * Metode for Location som lytter på om Location har forandret seg
     * @param location nyeste posisjon
     */
    @Override
    public void onLocationChanged(Location location) {
        settPosisjon(location);
    }

    /**
     * Iverksetter posisjonering når appen kobler seg opp mot Google Api
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Sjekker om appen har permission til å bruke posisjon
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Setter brukerens posisjon til den siste kjente fra API
            settPosisjon(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
        }
    }

    /**
     * Override metode for kart
     */
    @Override
    public void onConnectionSuspended(int i) {}

    /**
     * Override metode for kart
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    /**
     * Metode som sjekker all brukerdata før den prøver å bruke den til å sette en ny tur inn i appens lokale SQLIte database
     */
    private void lagNyTur() {

        boolean error = false;

        // Henter all skreven inndata fra EditText og trimmer den
        String navn = etNavn.getText().toString().trim();
        String beskrivelse = etBeskrivelse.getText().toString().trim();
        String turType = etTurTyrpe.getText().toString().trim();

        // Henter de aller nyeste posisjonsverdiene so mer tilgjengelig
        if (mSistePosisjon != null) {
            latitude = (float) mSistePosisjon.getLatitude();
            longitude = (float) mSistePosisjon.getLongitude();
            moh = (int) mSistePosisjon.getAltitude();
        } else {
            // mSistePosisjon er null, så det finnes ingen siste kjent posisjon.
            Snackbar.make(rootView, INGEN_KJENT_POSISJON_MELDING, Snackbar.LENGTH_LONG).show();
            error = true;
        }

        // Sjekker omm inndata på navn er korrekt
        if (navn.isEmpty() || navn.length() > MAX_NAVN_LENGDE) {
            etNavn.setError("Må være mellom 1 og " + MAX_NAVN_LENGDE + " karakterer.");
            etNavn.requestFocus();
            error = true;
        }

        // Sjekker om inndata på beskrivelse er korrekt
        if (beskrivelse.isEmpty() || beskrivelse.length() > MAX_BESKRIVELSE_LENGDE) {
            etBeskrivelse.setError("Må være mellom 1 og " + MAX_BESKRIVELSE_LENGDE + " karakterer.");
            etBeskrivelse.requestFocus();
            error = true;
        }

        // Sjekker om inndata på turtype er korrekt
        if (turType.isEmpty() || turType.length() > MAX_TURTYPE_LENGDE) {
            etTurTyrpe.setError("Må være mellom 1 og " + MAX_TURTYPE_LENGDE + " karakterer.");
            etTurTyrpe.requestFocus();
            error = true;
        }

        // Sjekker om bildepath er gyldig
        if (mBildesti == null || mBildesti.isEmpty()) {
            Snackbar.make(rootView, BILDE_UGYLDIG_MELDING, Snackbar.LENGTH_LONG).show();
            error = true;
        }

        // Sjekker at bruker er "logget inn" ved å ha lagt til navnet sitt i Instillinger.
        SharedPreferences mSettings = getActivity().getPreferences(Context.MODE_PRIVATE);
        String registrant = mSettings.getString("Navn", null);
        if (registrant == null || registrant.isEmpty() || registrant.length() > MAX_REGISTRANT_LENGDE) {
            error = true;
            Snackbar.make(rootView, LOGIN_UGYLDIG_MELDING, Snackbar.LENGTH_LONG).show();
        }

        // Fortsetter til databaseoperasjon hvis ingen feil
        if (!error) {

            // Lager databaseobjekt, turobjekt og åpner
            Tur nyTur = new Tur(navn, beskrivelse, latitude, longitude, moh, turType, mBildesti, registrant);
            TurDbAdapter turDbAdapter = new TurDbAdapter(mContext);
            turDbAdapter.open();

            // setter Turen inn i DB og gir rowID som tilbakemelding
            long insertID = turDbAdapter.nyTur(nyTur);
            if (insertID > DB_INSERT_OK) {
                Snackbar.make(rootView, DATABASE_INSERT_OK_MELDING + insertID, Snackbar.LENGTH_LONG).show();
                resetTekst();
            } else {
                Snackbar.make(rootView, DB_INSERT_FEIL_MELDING, Snackbar.LENGTH_LONG).show();
            }

            // lukker koblingen til databasen
            turDbAdapter.close();
        }
    }
}
