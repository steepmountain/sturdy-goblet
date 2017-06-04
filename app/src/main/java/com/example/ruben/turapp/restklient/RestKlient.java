package com.example.ruben.turapp.restklient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Model-nivå klasse som skal håndtere direkte kontakt med PHP API
 * Klassen er direkte importert fra min tidligere oppgave i Applikasjonsutvikling
 */
public class RestKlient {

    private static String POST = "POST";

    /**
     * Klasse for håndtering av asynkrone Get-oppgaver.
     */
    static class GetTask extends AsyncTask<String, Void, String> {

        private String mRestUrl;
        private RestTaskCallback mCallback;

        GetTask(String restUrl, RestTaskCallback callback) {
            mRestUrl = restUrl;
            mCallback = callback;
        }

        /**
         * Utfører bakgrunnsoppgaven for en Get-oppgave.
         * @return Returnerer et rad-objekt fra den gitte REST-URL-en
         */
        @Override
        protected String doInBackground(String... strings) {
            String response = null;

            // Åpner Http-forbindelse
            HttpURLConnection connection = null;
            try {
                URL restUrl = new URL(mRestUrl);
                connection = (HttpURLConnection) restUrl.openConnection();
                connection.connect();
                int status = connection.getResponseCode();

                // Sjekker om status er OK før den tar input
                if (status == HttpURLConnection.HTTP_OK) {

                    // Bygger opp en strenge med Input-data
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder builder = new StringBuilder();
                    while ((response = reader.readLine()) != null) {
                        builder.append(response);
                    }
                    response = builder.toString();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                assert connection != null;
                connection.disconnect();
            }

            // Returner responsen fra Http-requests
            return response;
        }

        /**
         * Utfører onPostExecute etter at get-oppgaven er ferdig
         * @param result resultet av get-oppgaven
         */
        @Override
        protected void onPostExecute(String result) {
            mCallback.onTaskComplete(result);
            super.onPostExecute(result);
        }
    }

    /**
     * Klasse for håndtering av asynkrone Insert-oppgaver
     */
    static class InsertTask extends AsyncTask<String, Void, String> {

        private String mRestUrl;
        private RestTaskCallback mCallback;
        private JSONObject mToInsert;

        InsertTask(String restUrl, RestTaskCallback callback, JSONObject toInsert) {
            mRestUrl = restUrl;
            mCallback = callback;
            mToInsert = toInsert;
        }

        /**
         * Utfører bakgrunnsoppgaven for en Insert-oppgave.
         * @param strings
         * @return Rad-nummeret til objektet som ble satt inn i databasen
         */
        @Override
        protected String doInBackground(String... strings) {
            String response = null;
            HttpURLConnection connection = null;
            try {

                // Åpner HTTP-forbindelse og gjør klar til å skrive et JSON-objekt
                URL restUrl = new URL(mRestUrl);
                connection = (HttpURLConnection) restUrl.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(0);
                connection.setRequestMethod(POST);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.connect();

                // Skriver til databasen
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(mToInsert.toString());
                out.close();

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {

                    // Hvis sukksess, skriver radnummer tilbake
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder builder = new StringBuilder();
                    while ((response = reader.readLine()) != null) {
                        builder.append(response);
                    }
                    response = builder.toString();
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                assert connection != null;
                connection.disconnect();
            }

            // Returnerer respons fra insert
            return response;
        }

        /**
         * Utfører onPostExecute-kallet med resultatet
         * @param result resultet fra insert-oppgaven
         */
        @Override
        protected void onPostExecute(String result) {
            mCallback.onTaskComplete(result);
            super.onPostExecute(result);
        }
    }

    /**
     * Abstrakt klasse for å bruke onTaskComplete i Get- og Insert-oppgavene
     */
    abstract static class RestTaskCallback {
        public abstract void onTaskComplete(String result);
    }
}
