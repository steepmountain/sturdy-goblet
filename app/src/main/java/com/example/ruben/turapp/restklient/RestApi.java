package com.example.ruben.turapp.restklient;

import com.example.ruben.turapp.database.TurDbAdapter;

import org.json.JSONObject;

/**
 * Kontroller-nivå RestKlient for å kommunisere med API.
 * Klassen er direkte importert fra min tidligere oppgave i Applikasjonsutvikling
 */
public class RestApi {

    private static final String BASE_URL = "http://itfag.usn.no/~141175/api.php/Tur/";
    private static final String TRANSFORM = "transform=1";

    public RestApi() {
        // Tom konstruktør
    }

    /**
     * Henter alle turene i MySQL-databasen
     * @param callback respons-callback etter metoden er utført
     */
    public void hentAlleTurer(GetResponseCallback callback) {
        String restUrl = BASE_URL + "?" + TRANSFORM;
        doExecuteGetCall(callback, restUrl);
    }


    /**
     * Setter inn en tur i form av JSONObjet til MySQL-databasen
     * @param callback respons-callback etter metoden er utført
     * @param toInsert JSON-objektet som skal settes inn
     */
    public void settInnTur(GetResponseCallback callback, JSONObject toInsert) {
        String restUrl = BASE_URL;
        doExecuteInsertCall(callback, restUrl, toInsert);
    }

    /**
     * Metode som utfører kallet på den asynkrone Get-oppgaven.
     * @param callback respons-callback etter metoden er utført
     * @param restUrl URLen som skal brukes av get-oppgaven
     */
    private void doExecuteGetCall(final GetResponseCallback callback, String restUrl) {
        new RestKlient.GetTask(restUrl, new RestKlient.RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }).execute(restUrl);
    }

    /**
     * Metode som utfører kallet på den asynkrone Insert-oppgaven.
     * @param callback respons-callback etter metoden er utført
     * @param restUrl URLen som skal brukes av get-oppgaven
     * @param toInsert JSON-objektet som skal settes inn i MySQL-databasen
     */
    private void doExecuteInsertCall(final GetResponseCallback callback, final String restUrl, JSONObject toInsert) {
        new RestKlient.InsertTask(restUrl, new RestKlient.RestTaskCallback() {
            @Override
            public void onTaskComplete(String result) {
                callback.onDataReceived(result);
            }
        }, toInsert).execute(restUrl);
    }
}