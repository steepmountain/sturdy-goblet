package com.example.ruben.turapp.restklient;

/**
 * Abstrakt Callback-klasse bruk av RestApi for å kjøre når data kommer tilbake fra et Rest-kall
 * Klassen er direkte importert fra min tidligere oppgave i Applikasjonsutvikling
 */
public abstract class GetResponseCallback {

    public abstract void onDataReceived(String item);

}
