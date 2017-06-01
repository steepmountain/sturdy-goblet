package com.example.ruben.turapp.database;

import android.content.Context;
import android.database.Cursor;

import com.example.ruben.turapp.restklient.GetResponseCallback;
import com.example.ruben.turapp.restklient.NetworkHelper;
import com.example.ruben.turapp.restklient.RestApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ruben on 01.06.2017.
 */

public class DatabaseSynk {

    private TurDbAdapter mTurDbAdapter;
    private Context mContext;

    public DatabaseSynk(Context context, TurDbAdapter turDbAdapter) {
        mContext = context;
        mTurDbAdapter = turDbAdapter;
    }

    // Sender JSONArray til REST API
    public void send(JSONArray toInsert) throws JSONException {

        NetworkHelper helper = new NetworkHelper(mContext);
        if (helper.isOnline()) {
            RestApi api = new RestApi();

            for (int i = 0; i < toInsert.length(); i++) {
                JSONObject rad = toInsert.getJSONObject(i);
                final int key = rad.getInt(TurDbAdapter.TID);
                api.settInnTur(new GetResponseCallback() {
                    @Override
                    public void onDataReceived(String item) {
                        mTurDbAdapter.slettTur(key);
                    }
                }, rad);

            }
        }
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
