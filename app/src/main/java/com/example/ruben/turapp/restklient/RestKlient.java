package com.example.ruben.turapp.restklient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.ruben.turapp.bitmap.BitmapHelper;

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
 * Created by Ruben on 31.05.2017.
 */

// TODO: kode er gjenbrukt fra oblig

public class RestKlient {

    private static String POST = "POST";

    static class GetTask extends AsyncTask<String, String, String> {

        private String mRestUrl;
        private RestTaskCallback mCallback;

        GetTask(String restUrl, RestTaskCallback callback) {
            mRestUrl = restUrl;
            mCallback = callback;
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = null;

            HttpURLConnection connection = null;
            try {
                URL restUrl = new URL(mRestUrl);
                connection = (HttpURLConnection) restUrl.openConnection();
                connection.connect();
                int status = connection.getResponseCode();

                if (status == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder builder = new StringBuilder();
                    while ((response = reader.readLine()) != null) {
                        builder.append(response);
                    }
                    response = builder.toString();
                }

            } catch (MalformedURLException e) {
                // TODO: snackbar feil url
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            mCallback.onTaskComplete(result);
            super.onPostExecute(result);
        }
    }

    static class InsertTask extends AsyncTask<String, String, String> {

        private String mRestUrl;
        private RestTaskCallback mCallback;
        private JSONObject mToInsert;

        InsertTask(String restUrl, RestTaskCallback callback, JSONObject toInsert) {
            mRestUrl = restUrl;
            mCallback = callback;
            mToInsert = toInsert;
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = null;
            HttpURLConnection connection = null;
            try {
                URL restUrl = new URL(mRestUrl);
                connection = (HttpURLConnection) restUrl.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(0);
                connection.setRequestMethod(POST);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.connect();

                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(mToInsert.toString());
                out.close();

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder builder = new StringBuilder();
                    while ((response = reader.readLine()) != null) {
                        builder.append(response);
                    }
                    response = builder.toString();
                }


            } catch (MalformedURLException e) {
                // TODO: snackbar feil url
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                assert connection != null;
                connection.disconnect();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            mCallback.onTaskComplete(result);
            super.onPostExecute(result);
        }
    }

    static class HentBilde extends AsyncTask<String, String, Bitmap> {

        private String mFilsti;
        private RestTaskCallback mCallback;

        HentBilde(String filsti, RestTaskCallback callback) {
            mFilsti = filsti;
            mCallback = callback;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bilde = null;
            HttpURLConnection connection = null;
            try {
                URL filUrl = new URL(mFilsti);
                connection = (HttpURLConnection) filUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    bilde = BitmapFactory.decodeStream(is);
                }


            } catch (IOException e) {
                return null;
            } finally {
                assert connection != null;
                connection.disconnect();
            }
            return bilde;
        }

        @Override
        protected void onPostExecute(Bitmap bilde) {
            mCallback.onTaskComplete(BitmapHelper.bitmapTilString(bilde));
            super.onPostExecute(bilde);
        }
    }

    abstract static class RestTaskCallback {
        public abstract void onTaskComplete(String result);
    }
}
