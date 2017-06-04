package com.example.ruben.turapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ruben.turapp.Tur;


/**
 * Databaseadapter for SQLite-databasen lokalt i applikasjonen
 */
public class TurDbAdapter {

    private DatabaseHjelper mDbHjelper;
    private SQLiteDatabase mDb;
    private Context mContext;

    public TurDbAdapter(Context context) {
        mContext = context;
    }

    /**
     * Åpner koblingen til SQLite-databasen ved hjelp av en Databasehjelper
     * @return en brukende TurDbAdapter
     */
    public TurDbAdapter open() throws SQLException {
        mDbHjelper = new DatabaseHjelper(mContext);
        mDb = mDbHjelper.getWritableDatabase();
        return this;
    }

    /**
     * Lukker databasen ved hjelp av DatabaseHjelper
     */
    public void close() {
        if (mDbHjelper != null) {
            mDbHjelper.close();
        }
    }

    /**
     * Oppgraderer SQLite-databasen til en ny versjon
     */
    public void upgrade() throws SQLException {
        mDbHjelper = new DatabaseHjelper(mContext);
        mDb = mDbHjelper.getWritableDatabase();
        mDbHjelper.onUpgrade(mDb, 1, 0);
    }

    // Databasevariabler for SQLite
    private static final String DB_NAVN = "TUR_DB.db";
    private static final int DB_VERSJON = 1;
    public static final String TUR_TABLE = "Tur";
    public static final String TID = "tid";
    public static final String NAVN = "navn";
    public static final String BESKRIVELSE = "beskrivelse";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUTDE = "longitude";
    public static final String MOH = "moh";
    public static final String TURTYPE = "turType";
    public static final String TURBILDE = "turBilde";
    public static final String REGISTRANT = "registrant";
    private static final String[] TUR_FIELDS = new String[]{
            TID,
            NAVN,
            BESKRIVELSE,
            LATITUDE,
            LONGITUTDE,
            MOH,
            TURTYPE,
            TURBILDE,
            REGISTRANT
    };

    /**
     * Create-query for å opprette SQLite-tabellen for Tur
     */
    private static final String CREATE_TUR_TABLE =
            "create table " + TUR_TABLE + "("
                    + TID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NAVN + " TEXT NOT NULL, "
                    + BESKRIVELSE + " TEXT NOT NULL, "
                    + LATITUDE + " REAL NOT NULL, "
                    + LONGITUTDE + " REAL NOT NULL, "
                    + MOH + " INTEGER NOT NULL, "
                    + TURTYPE + " TEXT NOT NULL, "
                    + TURBILDE + " TEXT NOT NULL, "
                    + REGISTRANT + " TEXT NOT NULL"
                    + ");";


    /**
     * Query for å sette inn en enkelt tur i SQLite-databasen
     * @param nyTur Tur-objekt for input
     * @return en insertID i form av long
     */
    public long nyTur(Tur nyTur) {
        ContentValues newValues = new ContentValues();
        newValues.put(TurDbAdapter.NAVN, nyTur.getNavn());
        newValues.put(TurDbAdapter.BESKRIVELSE, nyTur.getBeskrivelse());
        newValues.put(TurDbAdapter.LATITUDE, nyTur.getLatitude());
        newValues.put(TurDbAdapter.LONGITUTDE, nyTur.getLongitude());
        newValues.put(TurDbAdapter.MOH, nyTur.getMoh());
        newValues.put(TurDbAdapter.TURTYPE, nyTur.getType());
        newValues.put(TurDbAdapter.TURBILDE, nyTur.getBilde());
        newValues.put(TurDbAdapter.REGISTRANT, nyTur.getRegistrant());
        return mDb.insertWithOnConflict(TUR_TABLE, null, newValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Select-query for å få tilbake alle turer i databasen
     * @return et Cursor-objekt med alle radene i databasen
     */
    public Cursor hentAlleTurer() {
        return mDb.query(TUR_TABLE, TUR_FIELDS, null, null, null, null, null);
    }

    /**
     * Sletter en tur fra databasen gitt turId
     * @param turId TurID til Turen som skal slettes
     * @return true hvis slettingen var vellykket, false hvis ikke
     */
    boolean slettTur(long turId) {
        String[] selectionArgs = {String.valueOf(turId)};
        return mDb.delete(TUR_TABLE, TID + "=?",selectionArgs ) > 0;
    }

    /**
     * Gjør om en Cursor til et Tur-objekt
     * @param cursor Cursor som skal bli gjort om til tur
     * @return Tur-objektet fra den gitte Cursor
     */
    public static Tur cursorToTur(Cursor cursor) {
        Tur tur = new Tur();
        tur.setNavn(cursor.getString(cursor.getColumnIndex(NAVN)));
        tur.setBeskrivelse(cursor.getString(cursor.getColumnIndex(BESKRIVELSE)));
        tur.setLatitude(cursor.getFloat(cursor.getColumnIndex(LATITUDE)));
        tur.setLongitude(cursor.getFloat(cursor.getColumnIndex(LONGITUTDE)));
        tur.setMoh(cursor.getInt(cursor.getColumnIndex(MOH)));
        tur.setType(cursor.getString(cursor.getColumnIndex(TURTYPE)));
        tur.setBilde(cursor.getString(cursor.getColumnIndex(TURBILDE)));
        tur.setRegistrant(cursor.getString(cursor.getColumnIndex(REGISTRANT)));
        return tur;
    }


    /**
     * Indre klasse for å hjelpe med opprettelsen av Databasen
     */
    private static class DatabaseHjelper extends SQLiteOpenHelper {

        SQLiteDatabase mDb;

        private DatabaseHjelper(Context context) {
            super(context, DB_NAVN, null, DB_VERSJON);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TUR_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TUR_TABLE);
            onCreate(sqLiteDatabase);
        }
    }
}
