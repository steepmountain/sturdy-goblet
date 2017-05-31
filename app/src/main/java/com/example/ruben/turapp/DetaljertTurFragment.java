package com.example.ruben.turapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetaljertTurFragment extends Fragment implements OnMapReadyCallback {

    // TODO: hent zoom faktor fra shared preferences
    private static int ZOOM_FAKTOR = 10;

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private LatLng mPosition;
    private Tur mTur;

    public DetaljertTurFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflater fragment for dette layout
        View fragment = inflater.inflate(R.layout.fragment_detaljert_tur, container, false);

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
                mPosition = new LatLng(mTur.getLatitude(), mTur.getLongitude());
            }
        }
        return fragment;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPosition, ZOOM_FAKTOR));
        mMap.addMarker(new MarkerOptions().position(mPosition).title(mTur.getNavn()));
    }
}
