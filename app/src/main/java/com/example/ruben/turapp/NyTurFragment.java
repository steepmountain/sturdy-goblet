package com.example.ruben.turapp;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.ruben.turapp.database.TurDbAdapter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class NyTurFragment extends Fragment {

    // Kontekst og rootview for Snackbar
    private Activity mActivity;
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
    private ImageButton btnKamera;
    private ImageButton btnFilvelger;
    private Button btnSubmit;


    private static final String TITTEL = "Ny tur";
    private static final int MAX_NAVN_LENGDE = 200;
    private static final int MAX_BESKRIVELSE_LENGDE = 1000;
    private static final int MAX_TURTYPE_LENGDE = 200;
    private static final int MAX_REGISTRANT_LENGDE = 200;
    private static final int VELG_BILDE_REQUEST_CODE = 0;
    private static final int TA_BILDE_REQUEST_CODE = 1;
    private static final int ACTIVITY_RESULT_OK = -1;
    private static final int DB_INSERT_OK = 0;
    private static final String BILDE_UGYLDIG_MESSAGE = "Bilde er ikke gyldig. Prøv igjen!";
    private static final String FIL_UGYLDIG_MESSAGE = "Kunne ikke lage en fil for bilde. Prøv igjen!";
    private static final String DB_INSERT_FAIL_MESSAGE = "Noe gikk galt med databasen. Prøv igjen!";
    private static final String LOGIN_UGYLDIG_MESSAGE = "Logg inn i Instillinger før du lager en ny tur. Navnet må være mellom 1 og " + MAX_REGISTRANT_LENGDE + " karakterer.";
    private static final String DB_INSERT_OK_MESSAGE = "Turen er lagt inn med ID ";



    public NyTurFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_ny_tur, container, false);
        mActivity = getActivity();
        // Henter kontekst og rootView for bruk i fragmentet
        mContext = mActivity.getApplicationContext();
        rootView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);

        // Setter tittel på actionbar
        //getActivity().getActionBar().setTitle(TITTEL); // TODO: sjekk om det funker uten support

        // label og edit for navn
        tvNavnLabel = (TextInputLayout) fragment.findViewById(R.id.fragment_ny_kunde_label_navn);
        etNavn = (TextInputEditText) fragment.findViewById(R.id.fragment_ny_kunde_edit_navn);

        // label og edit for beskrivelse
        tvBeskrivelseLabel = (TextInputLayout) fragment.findViewById(R.id.fragment_ny_kunde_label_beskrivelse);
        etBeskrivelse = (TextInputEditText) fragment.findViewById(R.id.fragment_ny_kunde_edit_beskrivelse);

        // setter opp dropdown spinner
        tvTurTypeLabel = (TextInputLayout) fragment.findViewById(R.id.fragment_ny_kunde_label_turtype);
        etTurTyrpe = (TextInputEditText) fragment.findViewById(R.id.fragment_ny_kunde_edit_turtype);

        btnKamera = (ImageButton) fragment.findViewById(R.id.fragment_ny_kunde_button_kamera);
        btnKamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taBildeMedKamera();
            }
        });

        btnFilvelger = (ImageButton) fragment.findViewById(R.id.fragment_ny_kunde_button_filvelger);
        btnFilvelger.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
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

        // TODO: get current/last location

        etNavn.setText("Ruben");
        etBeskrivelse.setText("Stor som et fjell");
        etTurTyrpe.setText("Fjelltopp");
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
                Uri bildeUri = FileProvider.getUriForFile(mContext, MainActivity.FILE_PROVIDER_AUTHORITY, bilde);
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

        mBildePath = bilde.getPath();
        return bilde;
    }


    // håndterer intent aktiviteter fra filvelger og kamera
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // håndterer filvelger
        if (requestCode == VELG_BILDE_REQUEST_CODE && resultCode == ACTIVITY_RESULT_OK) {
            if (data != null) {
                mBildePath = data.getData().getPath();
            }
        }
    }

    // Fjerner all tekst når en ny tur blir laget
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

        // TODO: putt tilbake
        // TODO: fil finnes ikke? Sjekk dette ut seinere !tmp.exists
        // Sjekker om bildepath er gyldig
        //File tmp = new File(mBildePath);
        //if (mBildePath.isEmpty() || tmp.isDirectory()) {
        if (mBildePath == null || mBildePath.isEmpty()) {
            Snackbar.make(rootView, BILDE_UGYLDIG_MESSAGE, Snackbar.LENGTH_LONG).show();
            error = true;
        }


        // Sjekker at bruker er "logget inn" ved å ha lagt til navnet sitt i Instillinger.
        SharedPreferences mSettings = getActivity().getPreferences(Context.MODE_PRIVATE);
        String registrant = mSettings.getString("Navn", null);
        if (registrant == null || registrant.isEmpty() || registrant.length() > MAX_REGISTRANT_LENGDE) {
            error = true;
            Snackbar.make(rootView, LOGIN_UGYLDIG_MESSAGE, Snackbar.LENGTH_LONG).show();
        }


        // Fortsetter til databaseoperasjon hvis ingen feil
        if (!error) {

            // Lager databaseobjekt, turobjekt og åpner
            Tur nyTur = new Tur(navn, beskrivelse, latitude, longitude, (int) moh, turType, mBildePath, registrant);
            Log.v("NyTurFrag", nyTur.toString());
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
    }
}
