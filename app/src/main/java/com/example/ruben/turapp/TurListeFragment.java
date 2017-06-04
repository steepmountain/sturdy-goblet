package com.example.ruben.turapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ruben.turapp.restklient.GetResponseCallback;
import com.example.ruben.turapp.restklient.NetworkHelper;
import com.example.ruben.turapp.restklient.RestApi;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Fragment for å holde på en ListView av alle Tur-objekt hentet fra MySQL-databasen
 */
public class TurListeFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private ListView mListView;
    private ArrayList<Tur> mTurListe;
    private TurAdapter mAdapter;
    private Location mSistePosisjon;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private Activity mActivity;
    private View rootView;

    private static String INGEN_NETTVERKSTILKOBLING_MELDING = "Ingen nettverkstilkobling, kan ikke hente turer.";


    public TurListeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_tur_liste, container, false);
        mActivity = getActivity();
        mActivity.setTitle("TurApp");
        mContext = mActivity.getApplicationContext();
        rootView = mActivity.getWindow().getDecorView();

        // Bygger google api klient for å kunne hente siste posisjon
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mListView = (ListView) fragment.findViewById(R.id.fragment_tur_liste_listView);


        // Setter opp Floating Action Button
        FloatingActionButton btnFab = (FloatingActionButton) fragment.findViewById(R.id.fragment_tur_liste_fab);
        btnFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Fragment fragment = new NyTurFragment();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction().addToBackStack(null);
                transaction.replace(R.id.activity_main_content_fragment, fragment);
                transaction.commit();
            }
        });

        // Populerer ListView med Turer fra REST-API
        NetworkHelper helper = new NetworkHelper(getActivity());
        if (helper.isOnline()) {
            RestApi api = new RestApi();
            api.hentAlleTurer(new GetResponseCallback() {
                @Override
                public void onDataReceived(String item) {
                    try {
                        oppdaterTurListe(Tur.lagTurListe(item));
                    } catch (JSONException e) {
                        Snackbar.make(rootView, MainActivity.JSON_FEIL_MELDING, Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity().getApplicationContext();
    }

    // Oppdaterer turlisten og sorterer den etter distanse fra brukerens nåværende pos
    public void oppdaterTurListe(ArrayList<Tur> nyListe) {

        if (nyListe != null) {

            mTurListe = nyListe;

            // Hvis appen finner siste posisjon, sorter alle turer etter hvor langt unna current posisjon de er.
            if (mSistePosisjon != null) {
                for (Tur t : mTurListe) {
                    Location turPosisjon = new Location("Temp Provider"); // Tur location
                    turPosisjon.setLatitude(t.getLatitude());
                    turPosisjon.setLongitude(t.getLongitude());
                    turPosisjon.setAltitude(t.getMoh());
                    t.setDistanseTil((int) mSistePosisjon.distanceTo(turPosisjon));
                }
                // Sorterer listen basert på distanse fra brukers nåværende posisjon
                Collections.sort(mTurListe, Tur.DistanseComparator);
            }

            // Setter adapter til ListView
            mAdapter = new TurAdapter(mContext, nyListe);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // Lager en Bundle med Tur-objektet som skal åpnes i DetaljertTurFragment
                    Bundle data = new Bundle();
                    data.putSerializable("Tur", mTurListe.get(i));
                    Fragment fragment = new DetaljertTurFragment();
                    fragment.setArguments(data);
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction().addToBackStack(null);
                    transaction.replace(R.id.activity_main_content_fragment, fragment);
                    transaction.commit();
                }
            });
            mAdapter.notifyDataSetChanged();

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
        // Sjekker om appen har permission til å bruke posisjon
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mSistePosisjon = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            // Oppdaterer turlisten etter at appen har fått posisjon, siden asynk gjør at dette skjer etter at listview er laget første gangen.Henter bare listen hvis mobilen har nett
            NetworkHelper helper = new NetworkHelper(mContext);
            if (helper.isOnline()) {
                oppdaterTurListe(mTurListe);
            } else {
                Snackbar.make(rootView, INGEN_NETTVERKSTILKOBLING_MELDING , Snackbar.LENGTH_LONG).show();
            }

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
