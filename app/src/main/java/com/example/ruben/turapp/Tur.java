package com.example.ruben.turapp;

import android.database.Cursor;

import com.example.ruben.turapp.database.TurDbAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Tur-klasse for å holde på informasjon om turer.
 */
public class Tur implements Serializable {

    private String navn;
    private String beskrivelse;
    private float latitude;
    private float longitude;
    private int moh;
    private String type;
    private String bilde;
    private String registrant;
    private int distanseTil;

    // Setters og getters
    public int getDistanseTil() {
        return distanseTil;
    }

    public void setDistanseTil(int distanseTil) {
        this.distanseTil = distanseTil;
    }

    public String getNavn() {
        return navn;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public int getMoh() {
        return moh;
    }

    public String getType() {
        return type;
    }

    public String getBilde() {
        return bilde;
    }

    public String getRegistrant() {
        return registrant;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setMoh(int moh) {
        this.moh = moh;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBilde(String bilde) {
        this.bilde = bilde;
    }

    public void setRegistrant(String registrant) {
        this.registrant = registrant;
    }


    public Tur() {
    }

    /**
     * Default konstruktør for tur
     * @param navn Turens navn
     * @param beskrivelse beskrivelse av turen
     * @param latitude latitude-verdi for turen
     * @param longitude longtitude-verdi for turen
     * @param moh hvor mange meter over havet turen er
     * @param type fritekst type av tur
     * @param bilde bilde-URI for turen
     * @param registrant hvem registranten for turen er
     */
    public Tur(String navn, String beskrivelse, float latitude, float longitude, int moh, String type, String bilde, String registrant) {
        this.navn = navn;
        this.beskrivelse = beskrivelse;
        this.latitude = latitude;
        this.longitude = longitude;
        this.moh = moh;
        this.type = type;
        this.bilde = bilde;
        this.registrant = registrant;
    }

    /**
     * Konstruktfør for turen med JSONObjekt som input
     * @param jsonTur JSONObjekt som inneholder turinformasjon
     */
    public Tur(JSONObject jsonTur) {
        this.navn = jsonTur.optString(TurDbAdapter.NAVN);
        this.beskrivelse = jsonTur.optString(TurDbAdapter.BESKRIVELSE);
        this.latitude = (float) jsonTur.optDouble(TurDbAdapter.LATITUDE);
        this.longitude = (float) jsonTur.optDouble(TurDbAdapter.LONGITUTDE);
        this.moh = jsonTur.optInt(TurDbAdapter.MOH);
        this.type = jsonTur.optString(TurDbAdapter.TURTYPE);
        this.bilde = jsonTur.optString(TurDbAdapter.TURBILDE);
        this.registrant = jsonTur.optString(TurDbAdapter.REGISTRANT);
    }

    /**
     * Konstruktør som tar inn Cursor-objekt
     * @param cursor Cursor som inneholder tur-informasjon
     */
    public Tur(Cursor cursor) {
        this.navn = cursor.getString(cursor.getColumnIndex(TurDbAdapter.NAVN));
        this.beskrivelse = cursor.getString(cursor.getColumnIndex(TurDbAdapter.BESKRIVELSE));
        this.latitude = cursor.getFloat(cursor.getColumnIndex(TurDbAdapter.LATITUDE));
        this.longitude = cursor.getFloat(cursor.getColumnIndex(TurDbAdapter.LONGITUTDE));
        this.moh = cursor.getInt(cursor.getColumnIndex(TurDbAdapter.MOH));
        this.type = cursor.getString(cursor.getColumnIndex(TurDbAdapter.TURTYPE));
        this.bilde = cursor.getString(cursor.getColumnIndex(TurDbAdapter.TURBILDE));
        this.registrant = cursor.getString(cursor.getColumnIndex(TurDbAdapter.REGISTRANT));
    }

    /**
     * Lager en ArrayList av turer basert på JSONObjekt
     * @param jsonString JSONObjektet som inneholder turer
     * @return ArrayList av turer
     */
    public static ArrayList<Tur> lagTurListe(String jsonString) throws JSONException {
        ArrayList<Tur> turListe = new ArrayList<>();
        JSONObject jsonObj = new JSONObject(jsonString);
        JSONArray jsonTurArray = jsonObj.optJSONArray(TurDbAdapter.TUR_TABLE);
        for (int i = 0; i < jsonTurArray.length(); i++) {
            JSONObject jsonTur = (JSONObject) jsonTurArray.get(i);
            Tur t = new Tur(jsonTur);
            turListe.add(t);
        }
        return turListe;
    }

    @Override
    public String toString() {
        return "Tur{" +
                "navn='" + navn + '\'' +
                ", beskrivelse='" + beskrivelse + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", moh=" + moh +
                ", type='" + type + '\'' +
                ", bilde='" + bilde + '\'' +
                ", registrant='" + registrant + '\'' +
                '}';
    }

    /**
     * Indre komparator for å sortere turer basert på distanse fra en brukers nåværende posisjon
     */
    public static Comparator<Tur> DistanseComparator = new Comparator<Tur>() {
        @Override
        public int compare(Tur tur, Tur t1) {
            float dist1 = tur.getDistanseTil();
            float dist2 = t1.getDistanseTil();

            return (int) (dist1 - dist2);
        }
    };
}
