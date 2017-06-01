package com.example.ruben.turapp.restklient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.ruben.turapp.database.TurDbAdapter;

import org.json.JSONObject;

/**
 * Created by Ruben on 31.05.2017.
 */

// TODO: kode er gjenbrukt fra oblig

public class RestApi {

    private static final String BASE_URL = "http://itfag.usn.no/~141175/api.php/Tur/";
    private static final String TRANSFORM = "transform=1";

    public RestApi() {
        // Tom konstruktør
    }

    public void hentAlleTurer(GetResponseCallback callback) {
        String restUrl = BASE_URL + "?" + TRANSFORM;
        doExecuteGetCall(callback, restUrl);
    }

    public void hentTur(GetResponseCallback callback, int turNr) {
        String restUrl = BASE_URL + "?" + TurDbAdapter.TID + "=" + turNr + "&" + TRANSFORM;
        doExecuteGetCall(callback, restUrl);
    }

    public void settInnTur(GetResponseCallback callback, JSONObject toInsert) {
        String restUrl = BASE_URL;
        doExecuteInsertCall(callback, restUrl, toInsert);
    }

    private void doExecuteGetCall(final GetResponseCallback callback, String restUrl) {
        new RestKlient.GetTask(restUrl, new RestKlient.RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }).execute(restUrl);
    }

    private void doExecuteInsertCall(final GetResponseCallback callback, String restUrl, JSONObject toInsert) {
        new RestKlient.InsertTask(restUrl, new RestKlient.RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }, toInsert).execute(restUrl);
    }
}