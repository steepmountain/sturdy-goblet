package com.example.ruben.turapp.database;

import android.database.Cursor;
import android.util.Log;

import com.example.ruben.turapp.restklient.GetResponseCallback;
import com.example.ruben.turapp.restklient.RestApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ruben on 01.06.2017.
 */

public class DatabaseSynk {

    private TurDbAdapter mTurDbAdapter;

    public DatabaseSynk(TurDbAdapter turDbAdapter) {
        mTurDbAdapter = turDbAdapter;
    }

    // Sender et JSONObjekt til MySQL-databasen. Hvis sukkess fjernes den fra SQLite databasen
    public void send(JSONObject turRad) throws JSONException {
        final int nøkkel = turRad.getInt(TurDbAdapter.TID);
        RestApi api = new RestApi();
        api.settInnTur(new GetResponseCallback() {
            @Override
            public void onDataReceived(String item) {
                mTurDbAdapter.slettTur(nøkkel);
            }
        }, turRad);
    }

    // Gjør om et Cursor-objekt til et JSONArray
    public JSONArray cursorTilJSONArray(Cursor cursor) {
        JSONArray resultArray = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int antKolonner = cursor.getColumnCount();
            JSONObject rad = new JSONObject();
            for (int i = 0; i < antKolonner; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        rad.put(cursor.getColumnName(i), cursor.getString(i));
                    } catch (JSONException e) {
                        // TODO: returner med feilmelding
                    }
                }
            }
            resultArray.put(rad);
            cursor.moveToNext();
        }
        cursor.close();
        return resultArray;
    }


}
