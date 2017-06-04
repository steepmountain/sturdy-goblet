package com.example.ruben.turapp.restklient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Hjelpeklasse som skal fastslå om applikasjonen har nettverkstilgang.
 * Klassen er direkte importert fra min tidligere oppgave i Applikasjonsutvikling
 */
public class NetworkHelper {

    Context mContext;

    public NetworkHelper(Context context) {
        mContext = context;
    }

    public boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
