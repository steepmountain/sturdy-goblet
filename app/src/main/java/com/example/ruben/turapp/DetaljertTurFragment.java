package com.example.ruben.turapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ruben.turapp.bitmap.BitmapHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.squareup.picasso.Picasso;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Fragment som gir et detaljert syn inn på en Tur, inkludert bilde, tur, koordinater og beskrivelse.
 */
public class DetaljertTurFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private LatLng mPosisjon;
    private LatLng mMinLatLng;
    private Marker mMinMarker;
    private Tur mTur;
    private int zoomFaktor;
    private Activity mActivity;
    private Context mContext;
    private View rootView;

    private TextView tvNavn;
    private TextView tvBeskrivelse;
    private TextView tvTurType;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private TextView tvMoh;
    private ImageView ivBilde;

    private static String UGYLDIG_BILDE_MESSAGE = "Bildet er ugyldig og kan ikke vises.";
    private static String INGEN_DATA_MESSAGE = "Ingen data å vise.";

    public DetaljertTurFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Henter aktiviteten, konteksten og View for bruk i fragmentet
        View fragment = inflater.inflate(R.layout.fragment_detaljert_tur, container, false);
        mActivity = getActivity();
        mContext = mActivity.getApplicationContext();
        rootView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);

        // Bygger opp en Google Api Klient for bruk til posisjosndata
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Lager et mapfragment som brukes inne i tur-fragmentet
        mMapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_detaljert_tur_map, mMapFragment).commit();
        mMapFragment.getMapAsync(this);

        // Henter data fra bundle
        Bundle data = getArguments();
        if (data != null) {
            mTur = (Tur) data.getSerializable("Tur");
            if (mTur != null) {
                mPosisjon = new LatLng(mTur.getLatitude(), mTur.getLongitude());
                mActivity.setTitle(mTur.getNavn());
            }
        } else {
            Snackbar.make(rootView, INGEN_DATA_MESSAGE, Snackbar.LENGTH_LONG).show();
        }

        // Populerer fragmentet med tekst fra tur-objektet
        tvNavn = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_navn);
        tvNavn.setText(mTur.getNavn());

        tvBeskrivelse = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_beskrivelse);
        tvBeskrivelse.setText(mTur.getBeskrivelse());

        tvTurType = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_turType);
        tvTurType.setText(mTur.getType());

        tvLatitude = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_latitude);
        String latTekst = MainActivity.POSISJON_LAT_TEKST + mTur.getLatitude();
        tvLatitude.setText(latTekst);

        tvLongitude = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_longitude);
        String lngTekst = MainActivity.POSISJON_LNG_TEKST + mTur.getLongitude();
        tvLongitude.setText(lngTekst);

        tvMoh = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_moh);
        String mohTekst = MainActivity.POSISJON_MOH_TEKST + mTur.getMoh();
        tvMoh.setText(mohTekst);

        ivBilde = (ImageView) fragment.findViewById(R.id.fragment_detaljert_tur_bilde);

        // Sjekker bildestien for turens bilde
        sjekkBildesti(mTur.getBilde());

        // Henter zoom faktor fra settings
        SharedPreferences mSettings = getActivity().getPreferences(Context.MODE_PRIVATE);
        zoomFaktor = mSettings.getInt("ZoomFaktor", 10);

        return fragment;
    }

    /**
     * Sjekker hva slags type filsti bildet bruker
     * @param filsti En URI i form av en string
     */
    private void sjekkBildesti(String filsti) {

        // Sjekker om strengen er en gyldig URL
        if (erGyldidUrl(filsti)) {
            // Hvis bildet er en URL som peker på http brukes Picasso for å laste ned bilde
            settBildeMedPicasso(filsti);
        } else if (erLokaltBilde(filsti)) {
            // Hvis bildet er en URI som peker på telefonminne fanges den her
            settBildeMedBitmap(filsti);
        } else {
            // Stien er ikke noen gyldig form
            Snackbar.make(rootView, UGYLDIG_BILDE_MESSAGE, Snackbar.LENGTH_LONG).show();
        }
    }

    // Sjekker om filstien er et lokalt bilde på telefon
    private boolean erLokaltBilde(String filsti) {
        Bitmap bilde = BitmapFactory.decodeFile(mTur.getBilde());
        return bilde != null;
    }

    /**
     * Sjekker om en URL er gyldig formatert
     * @param filsti Den gitte filstien
     * @return true hvis gyldig URL, false hvis ikke
     */
    private boolean erGyldidUrl(String filsti) {
        try {
            URL filstiOnline = new URL(filsti);
            return true;
        } catch (MalformedURLException e) {
            // Ikke gyldig url
            return false;
        }
    }

    /**
     * Setter Thumbnailen i Fragmentet til å være et Bitmap fra den gitte bildestien
     * @param bildesti Filstien der bildet befinnre seg
     */
    private void settBildeMedBitmap(String bildesti) {
        // Bruker display metrics og bruker skjermstørrelsen for å skalere bildet.
        DisplayMetrics display = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(display);
        int skjermbredde = display.widthPixels;

        // Bygger et nytt Bitmap, roterer og skalerer via BitmapHelper
        Bitmap nyttBilde = BitmapHelper.roterOgSkalerBitmap(bildesti, skjermbredde);
        ivBilde.setImageBitmap(nyttBilde);
    }

    /**
     * Henter et bilde fra nettet ved hjelp av Picasso. Picasso er en OpenSource library for
     * å hente og bruke bilder i Android. Bruken om Picasso er skrevet om i vedlagt dokumentasjon.
     * @param filsti den gitte stien der bilde befinner seg
     */
    private void settBildeMedPicasso(String filsti) {
        // Bruker display metrics og bruker skjermstørrelsen for å skalere bildet
        DisplayMetrics display = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(display);
        int skjermbredde = display.widthPixels;
        int skjermhoyde = display.heightPixels;

        // Picasso henter inn bilde med filstien og setter størrelsen ut ifra skjerstørelsen
        Picasso.with(mContext).load(filsti).resize(skjermbredde, skjermhoyde).centerInside().into(ivBilde);
    }

    /**
     * Funksjon som går når SupportMapFragment er ferdig lastet inn. Setter type kart og
     * animerer kamera over til den gitte markøren ut ifra tur-informasjonen.
     * @param googleMap Det gitte googleMap-objektet
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPosisjon, zoomFaktor));
        mMap.addMarker(new MarkerOptions().position(mPosisjon).title(mTur.getNavn()));
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMinLatLng, zoomFaktor));
                return false;
            }
        });
    }

    /**
     * Listener-metode som slår til hver gang brukerens posijon forandrer seg
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        // Den nye brukerposisjonen hentes inn og sjekkes om er gyldid
        if (location != null) {

            // Flytter brukermarkøren hvis den ikke er null
            if (mMinMarker != null) {
                mMinMarker.remove();
            }

            // Lager ny posisjonsmarkør
            mMinLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mMinLatLng);
            markerOptions.title("Her er du");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mMinMarker = mMap.addMarker(markerOptions);
        }
    }

    /**
     * Starter kartet når fragmentet starter
     */
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    /**
     * Stopper kartet når fragmentet stopper
     */
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Listener-metode som slår til når applikasjonen kobler seg opp til LocationCallback
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Gjør et nytt LocationRequest og setter intervaller for spørringer
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sjekker om applikasjonen har rettigheter til posisjon
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Hvis permissions er aktivert, hent siste kjente posisjon og start en listener for nye posisjoner
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mMap.setMyLocationEnabled(true);
            onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
        }
    }


    /**
     * Nødvendig override for ConnectionCallback
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Nødvendig override for ConnectionCallback
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
