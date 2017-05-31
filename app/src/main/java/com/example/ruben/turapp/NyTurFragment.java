package com.example.ruben.turapp;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.ruben.turapp.database.TurDbAdapter;
import com.example.ruben.turapp.restklient.GetResponseCallback;
import com.example.ruben.turapp.restklient.NetworkHelper;
import com.example.ruben.turapp.restklient.RestApi;
import com.example.ruben.turapp.restklient.RestApi.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class NyTurFragment extends Fragment {

    // Kontekst og rootview for Snackbar
    private Context mContext;
    private View rootView;

    private String mBildePath;

    private TextInputLayout tvNavnLabel;
    private TextInputEditText etNavn;
    private TextInputLayout tvBeskrivelseLabel;
    private TextInputEditText etBeskrivelse;
    private TextInputLayout tvTurTypeLabel;
    private TextInputEditText etTurTyrpe;
    private Spinner spinTurType;
    private Button btnKamera;
    private Button btnFilvelger;
    private Button btnSubmit;


    private static final String TITTEL = "Ny tur";
    private static final String BILDE_UGYLDIG_MESSAGE = "Bilde er ikke gyldig. Prøv igjen!";
    private static final String FIL_UGYLDIG_MESSAGE = "Kunne ikke lage en fil for bilde. Prøv igjen!";
    private static final String DB_INSERT_FAIL_MESSAGE = "Noe gikk galt med databasen. Prøv igjen!";
    private static final String DB_INSERT_OK_MESSAGE = "Turen er lagt inn med ID ";
    private static final int MAX_NAVN_LENGDE = 200;
    private static final int MAX_BESKRIVELSE_LENGDE = 1000;
    private static final int MAX_TURTYPE_LENGDE = 200;
    private static final int VELG_BILDE_REQUEST_CODE = 0;
    private static final int TA_BILDE_REQUEST_CODE = 1;
    private static final int ACTIVITY_RESULT_OK = -1;
    private static final int DB_INSERT_OK = 0;



    public NyTurFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_ny_tur, container, false);

        // Henter kontekst og rootView for bruk i fragmentet
        mContext = getActivity().getApplicationContext();
        rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);

        // Setter tittel på actionbar
        //getActivity().getActionBar().setTitle(TITTEL); // TODO: sjekk om det funker uten support

        // label og edit for navn
        tvNavnLabel = (TextInputLayout) fragment.findViewById(R.id.fragment_ny_kunde_label_navn);
        etNavn = (TextInputEditText) fragment.findViewById(R.id.fragment_ny_kunde_edit_navn);

        // Lytter på tekstboks for å resette submit etter errors
        etNavn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                åpneSubmit();
            }
        });

        // label og edit for beskrivelse
        tvBeskrivelseLabel = (TextInputLayout) fragment.findViewById(R.id.fragment_ny_kunde_label_beskrivelse);
        etBeskrivelse = (TextInputEditText) fragment.findViewById(R.id.fragment_ny_kunde_edit_beskrivelse);

        // Lytter på tekstboks for å resette submit etter errors
        etBeskrivelse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                åpneSubmit();
            }
        });

        // setter opp dropdown spinner
        tvTurTypeLabel = (TextInputLayout) fragment.findViewById(R.id.fragment_ny_kunde_label_turtype);
        etTurTyrpe = (TextInputEditText) fragment.findViewById(R.id.fragment_ny_kunde_edit_turtype);

        btnKamera = (Button) fragment.findViewById(R.id.fragment_ny_kunde_button_kamera);
        btnKamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                åpneSubmit();
                taBildeMedKamera();
            }
        });

        btnFilvelger = (Button) fragment.findViewById(R.id.fragment_ny_kunde_button_filvelger);
        btnFilvelger.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                åpneSubmit();
                velgBildeFraFil();
            }
        });

        btnSubmit = (Button) fragment.findViewById(R.id.fragment_ny_kunde_button_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lagNyTur();
            }
        });

        mBildePath = ""; // Setter bildepath til tom

        // TODO: get current/last location

        etNavn.setText("Ruben");
        etBeskrivelse.setText("Stor som et fjell");
        etTurTyrpe.setText("Fjelltopp");
        mBildePath = "bildePath";
        return fragment;
    }

    // bruker intent for å åpne kamera og ta et bilde
    private void taBildeMedKamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Sjekker at appen har lov til å bruke kamera
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            File bilde = null;
            try {
                bilde = lagBildefil();
            } catch (Exception e) {
                Snackbar.make(rootView, FIL_UGYLDIG_MESSAGE, Snackbar.LENGTH_LONG).show();
            }

            // fortsetter hvis filen ble opprettet
            if (bilde != null){
                Uri bildeUri = FileProvider.getUriForFile(mContext, "com.example.android.fileprovider", bilde);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, bildeUri);
                startActivityForResult(intent, TA_BILDE_REQUEST_CODE);
            }
        }
    }

    // bruker intent for å hente et bilde fra fil
    private void velgBildeFraFil() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent, VELG_BILDE_REQUEST_CODE);
    }

    // Lager en fil for å lagre bilde med kamera
    private File lagBildefil() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filnavn = "turApp" + timeStamp;
        File mappe = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File bilde = File.createTempFile(
                filnavn,
                ".jpg",
                mappe);

        return bilde;
    }


    // håndterer intent aktiviteter fra filvelger og kamera
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("NyTur", "request=" + requestCode + "; result=" + (resultCode == ACTIVITY_RESULT_OK));
        // TODO: make switch

        // håndterer filvelger
        if (requestCode == VELG_BILDE_REQUEST_CODE && resultCode == ACTIVITY_RESULT_OK) {
            if (data != null) {
                mBildePath = data.getData().getPath();
                Log.v("NyTur", "Filepicker=" + mBildePath);
            }
        }

        // håndterer kamera
        else if (requestCode == TA_BILDE_REQUEST_CODE && resultCode == ACTIVITY_RESULT_OK) {
            if (data != null) {
                mBildePath = data.getData().getPath();
                Log.v("NyTur", "Kamera=" + mBildePath);
            }
        }
    }

    // Resetter submit-knappen for å gjøre den interaktiv etter en feil
    private void åpneSubmit() {
        btnSubmit.setClickable(true);
        btnSubmit.setAlpha(1f);
    }

    // Gjør submit uninteractive
    private void stengSubmit() {
        btnSubmit.setClickable(false);
        btnSubmit.setAlpha(0.5f);
    }

    private void resetTekst() {
        etBeskrivelse.setText("");
        etNavn.setText("");
        etTurTyrpe.setText("");
    }

    private void lagNyTur() {
        // Resets error and submit button
        boolean error = false;

        // hent all inndata
        String navn = etNavn.getText().toString().trim();
        String beskrivelse = etBeskrivelse.getText().toString().trim();
        String turType = etTurTyrpe.getText().toString().trim();
        float latitude = 0;
        float longitude = 0;
        double moh = 0;

        Location mLastLocation = null;
        if (mLastLocation != null) {
            latitude = (float) mLastLocation.getLatitude();
            longitude = (float) mLastLocation.getLongitude();
            moh = mLastLocation.getAltitude();
            Log.v("NyTurFragment", "lat=" + latitude + ";long=" + longitude + ";moh=" + moh);
        } else {
            latitude = (float) 10f;
            longitude = (float) 10f;
            moh = 1000;
            Log.v("NyTurFragment", "lat=" + latitude + ";long=" + longitude + ";moh=" + moh);
        }

        // Sjekker om brukerinput er korrekt
        if (navn.isEmpty() || navn.length() > MAX_NAVN_LENGDE) {
            etNavn.setError("Må være mellom 1 og " + MAX_NAVN_LENGDE + " karakterer.");
            etNavn.requestFocus();
            error = true;
        }
        if (beskrivelse.isEmpty() || beskrivelse.length() > MAX_BESKRIVELSE_LENGDE) {
            etBeskrivelse.setError("Må være mellom 1 og " + MAX_BESKRIVELSE_LENGDE + " karakterer.");
            etBeskrivelse.requestFocus();
            error = true;
        }
        if (turType.isEmpty() || turType.length() > MAX_TURTYPE_LENGDE) {
            etTurTyrpe.setError("Må være mellom 1 og " + MAX_TURTYPE_LENGDE + " karakterer.");
            etTurTyrpe.requestFocus();
            error = true;
        }

        /* TODO: putt tilbake
        // TODO: fil finnes ikke? Sjekk dette ut seinere !tmp.exists
        // Sjekker om bildepath er gyldig
        File tmp = new File(mBildePath);
        if (mBildePath.isEmpty() || tmp.isDirectory()) {
            Snackbar.make(rootView, BILDE_UGYLDIG_MESSAGE, Snackbar.LENGTH_LONG).show();
            error = true;
        }
        */

        // TODO: sjekk om bruker er logget inn

        // Fortsetter til databaseoperasjon hvis ingen feil
        if (!error) {

            // Lager databaseobjekt, turobjekt og åpner
            Tur nyTur = new Tur(navn, beskrivelse, latitude, longitude, (int) moh, mBildePath, turType, "Ruben B"); // TODO: get navn fra sharedpref
            TurDbAdapter turDbAdapter = new TurDbAdapter(mContext);
            turDbAdapter.open();

            // setter Turen inn i DB og gir rowID som tilbakemelding
            long insertID = turDbAdapter.nyTur(nyTur);
            if (insertID > DB_INSERT_OK) {
                Snackbar.make(rootView, DB_INSERT_OK_MESSAGE + insertID, Snackbar.LENGTH_LONG).show();
                resetTekst();
            }
            else {
                Snackbar.make(rootView, DB_INSERT_FAIL_MESSAGE, Snackbar.LENGTH_LONG).show();
            }

            // lukker koblingen til databasen
            turDbAdapter.close();
        }
        else {
            // fjerner interaktivitet fra submit hvis det er noen feil
            stengSubmit();
        }

    }
}
