package com.example.ruben.turapp;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ruben.turapp.restklient.GetResponseCallback;
import com.example.ruben.turapp.restklient.NetworkHelper;
import com.example.ruben.turapp.restklient.RestApi;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;


public class TurListeFragment extends Fragment {

    private ListView mListView; // TODO: better name
    private ArrayList<Tur> mTurListe;
    private TurAdapter mAdapter;
    private Location mLokasjon;

    public TurListeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_tur_liste, container, false);

        getActivity().setTitle("TurApp"); // TODO: mActivity;

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
                        // TODO: display error
                    }
                }
            });
        }
        return fragment;
    }

    // Oppdaterer turlisten og sorterer den etter distanse fra brukerens nåværende pos
    public void oppdaterTurListe(ArrayList<Tur> nyListe) {

        mTurListe = nyListe;

        mLokasjon = new Location(LocationManager.GPS_PROVIDER); // Min location
        mLokasjon.setLatitude(59.4089814);
        mLokasjon.setLongitude(9.0562463);
        for (Tur t : mTurListe) {
            Location turLokasjon = new Location(LocationManager.GPS_PROVIDER); // Tur location
            turLokasjon.setLatitude(t.getLatitude());
            turLokasjon.setLongitude(t.getLongitude());
            t.setDistanseTil( (int) mLokasjon.distanceTo(turLokasjon));
        }

        Collections.sort(mTurListe, Tur.DistanseComparator);

        mAdapter = new TurAdapter(getActivity(), nyListe);
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
