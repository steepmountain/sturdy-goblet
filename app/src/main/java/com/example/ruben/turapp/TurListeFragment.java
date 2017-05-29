package com.example.ruben.turapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class TurListeFragment extends Fragment {

    ListView mListView; // TODO: better name
    ArrayList<Tur> mTurListe;
    TurAdapter mAdapter;

    public TurListeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_tur_liste, container, false);

        getActivity().setTitle("TurApp"); // TODO: mActivity;

        mListView = (ListView) fragment.findViewById(R.id.fragment_tur_liste_listView);

        ArrayList<Tur> tempList = Tur.dummyData();

        // Setter opp Floating Action Button
        FloatingActionButton btnFab = (FloatingActionButton) fragment.findViewById(R.id.fragment_tur_liste_fab);
        btnFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Fragment fragment = new DetaljertTurFragment();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction().addToBackStack(null);
                transaction.replace(R.id.activity_main_content_fragment, fragment);
                transaction.commit();
            }
        });

        oppdaterTurListe(mTurListe = tempList);
        return fragment;
    }

    public void oppdaterTurListe(ArrayList<Tur> nyListe) {
        mTurListe = nyListe;
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
