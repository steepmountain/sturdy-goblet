package com.example.ruben.turapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContentResolverCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DetaljertTurFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String FILE_NOT_FOUND_MESSAGE = "Kunne ikke lese bildefilen.";

    private GoogleApiClient mGoogleApiClient;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private LatLng mPosisjon;
    private Location mSistePosisjon;
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

    public DetaljertTurFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflater fragment for dette layout
        View fragment = inflater.inflate(R.layout.fragment_detaljert_tur, container, false);
        mActivity = getActivity();
        mContext = mActivity.getApplicationContext();
        rootView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);

        // Kobler opp til google api for å hente lokasjon
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
        // TODO: vis Snackbar hvis ikke data i bundle og/eller tur
        Bundle data = getArguments();
        if (data != null) {
            mTur = (Tur) data.getSerializable("Tur");
            if (mTur != null) {
                mPosisjon = new LatLng(mTur.getLatitude(), mTur.getLongitude());
            }
        }

        // TODO: gjør en bedre sjekk?
        mActivity.setTitle(mTur.getNavn());

        // Populerer fragmentet med tekst
        tvNavn = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_navn);
        tvNavn.setText(mTur.getNavn());

        tvBeskrivelse = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_beskrivelse);
        tvBeskrivelse.setText(mTur.getBeskrivelse());

        tvTurType = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_turType);
        tvTurType.setText(mTur.getType());

        tvLatitude = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_latitude);
        tvLatitude.setText(mTur.getLatitude() + "");

        tvLongitude = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_longitude);
        tvLongitude.setText(mTur.getLongitude() + "");

        tvMoh = (TextView) fragment.findViewById(R.id.fragment_detaljert_tur_moh);
        tvMoh.setText(mTur.getMoh() + "");


        // TODO: hvordan fange unntakstilfelle ved at bildet ikke blir vist?
        // TODO: Skaler bitmap etter skjermen
        // Dekoder den gitte bildeURI og lager et Bitmap som vises.
        ivBilde = (ImageView) fragment.findViewById(R.id.fragment_detaljert_tur_bilde);
        Bitmap bilde = BitmapFactory.decodeFile(mTur.getBilde());
        ivBilde.setImageBitmap(bilde);

        // Henter zoom faktor fra settings
        SharedPreferences mSettings = getActivity().getPreferences(Context.MODE_PRIVATE);
        zoomFaktor = mSettings.getInt("ZoomFaktor", 10);

        return fragment;
    }

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

    @Override
    public void onLocationChanged(Location location) {

        mSistePosisjon = location;
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

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);

        // TODO: settings for accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Hvis permissions er aktivert, hent siste kjente posisjon og start en listener for nye posisjoner
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mMap.setMyLocationEnabled(true);
            onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
