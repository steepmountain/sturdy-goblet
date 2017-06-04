package com.example.ruben.turapp.database;

import android.database.Cursor;
import com.example.ruben.turapp.restklient.GetResponseCallback;
import com.example.ruben.turapp.restklient.RestApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Klasse som har ansvar for å ta data fra SQLite-databasen og gjøre det til JSON-objekt.
 */
public class DatabaseSynk {

    private TurDbAdapter mTurDbAdapter;

    public DatabaseSynk(TurDbAdapter turDbAdapter) {
        mTurDbAdapter = turDbAdapter;
    }

    /**
     * Sender en JSON-rad til RestAPI for Insert
     * @param turRad en enkelt rad fra SQLite-databasen
     */
    public void send(JSONObject turRad) throws JSONException {
        // Sender inn et Tur-objekt til online database og sletter det fra SQLite-database
        final int nøkkel = turRad.getInt(TurDbAdapter.TID);
        RestApi api = new RestApi();
        api.settInnTur(new GetResponseCallback() {
            @Override
            public void onDataReceived(String item) {
                mTurDbAdapter.slettTur(nøkkel);
            }
        }, turRad);
    }

    /**
     * Gjør om en enkelt Cursor-spørring til JSONArray
     * @param cursor Cursor fra SQLite-database
     * @return et JSONArray av Cursor-spørringen
     */
    public JSONArray cursorTilJSONArray(Cursor cursor) throws JSONException {
        JSONArray resultArray = new JSONArray();
        cursor.moveToFirst();

        // KIkker gjennom hver rad i Cursor-objektet og putter det i JSONObject som går inn i JSONArray hvis gyldi
        while (!cursor.isAfterLast()) {
            int antKolonner = cursor.getColumnCount();
            JSONObject rad = new JSONObject();
            for (int i = 0; i < antKolonner; i++) {
                if (cursor.getColumnName(i) != null) {
                        rad.put(cursor.getColumnName(i), cursor.getString(i));
                }
            }
            resultArray.put(rad);
            cursor.moveToNext();
        }
        cursor.close();
        return resultArray;
    }
}
