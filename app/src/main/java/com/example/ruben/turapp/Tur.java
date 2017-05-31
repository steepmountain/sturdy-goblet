package com.example.ruben.turapp;

import android.database.Cursor;

import com.example.ruben.turapp.database.TurDbAdapter;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Ruben on 29.05.2017.
 */

public class Tur implements Serializable {

    private String navn;
    private String beskrivelse;
    private float latitude;
    private float longitude;
    private int moh;
    private String type;
    private String bilde; // URL referanse
    private String registrant;

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


    public Tur() {}

    // TODO: constructor that takes byte[] or blob for image
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

    public Tur(String navn, String beskrivelse, float latitude, float longitude, int moh, String type) {
        this.navn = navn;
        this.beskrivelse = beskrivelse;
        this.latitude = latitude;
        this.longitude = longitude;
        this.moh = moh;
        this.type = type;
    }

    public static Tur getTurFromCursor(Cursor cursor) {
        Tur t = new Tur();
        t.navn = cursor.getString(cursor.getColumnIndex(TurDbAdapter.NAVN));
        t.beskrivelse = cursor.getString(cursor.getColumnIndex(TurDbAdapter.BESKRIVELSE));
        t.latitude = cursor.getFloat(cursor.getColumnIndex(TurDbAdapter.LATITUDE));
        t.longitude = cursor.getFloat(cursor.getColumnIndex(TurDbAdapter.LONGITUTDE));
        t.moh = cursor.getInt(cursor.getColumnIndex(TurDbAdapter.MOH));
        t.type = cursor.getString(cursor.getColumnIndex(TurDbAdapter.TURTYPE));
        t.bilde = cursor.getString(cursor.getColumnIndex(TurDbAdapter.TURBILDE));
        t.registrant = cursor.getString(cursor.getColumnIndex(TurDbAdapter.REGISTRANT));

        return t;
    }

    public static ArrayList<Tur> dummyData() {
        ArrayList<Tur> tempList = new ArrayList<>();
        tempList.add(new Tur("Gygrestolen", "En fottur på 6 bratte kilometer tar deg opp til det mytiske platået der gygra skal ha kastet kampesteiner utover Bø i raseri. Sagnet sier at når Gygrestolen faller skal verden gå under. Fjellplatået Gygrestolen er splittet i to hoveddeler der det ytterste platået siger litt utover hvert år. Gygrestolen er et viktig landemerke med sin karakteristiske form, og et mål for både turgåere og fjellklatrere. Herfra har du utsikt over det rike landbruksområdet og fjellene rundt Midt Telemark, og på vegen opp passerer du et fjellvann som ypperlig for en dukkert. ", 59.3660128f, 8.9787209f, 490, "Utsiktspunkt"));
        tempList.add(new Tur("Bryggefjell", "Bryggefjell kneisar høgt over Bødalen i NV. Her er det lang og bratt oppstigning på 430 høgde-meter frå parkeringsplassen øvst i Liheia opp til store isskurte svaberg. På Bryggefjell-nuten 778 moh har ein panoramautsikt ut over Bøbygda og opp mot Lifjell", 59.4401667f,	8.9663139f,778, "Fjelltop"));
        tempList.add(new Tur("Sletteliberget", "Fram til Sletteliberget er det bred og god sti og mulig å trille med barnevogn. Videre fra Sletteliberget til Dingdangkyrkja er det smalere sti, bæremeis er egnet. De som vil kan bli igjen på Sletteliberget, mens de som vil kan fortsette til Dingdangkyrkja. Vi har felles lunsj ved Sletteliberget og nyter utsikten. Vi ønsker å gjøre oppmerksom på at det er et bratt stup ved Sletteliberget, og at barna her til enhver tid må holdes under oppsyn av sine ledsagere, turlederen har ikke noe ansvar i den forbindelse.",59.4417571f, 9.0031219f, 405, "Utsiktspunkt"));
        return tempList;
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
}
