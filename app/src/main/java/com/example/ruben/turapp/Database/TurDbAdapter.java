package com.example.ruben.turapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ruben.turapp.Tur;


/**
 * Created by Ruben on 30.05.2017.
 */

public class TurDbAdapter {

    private DatabaseHjelper mDbHjelper;
    private SQLiteDatabase mDb;
    private Context mContext;

    public TurDbAdapter(Context context) {
        mContext = context;
    }

    // Åpner databasen med hjelperen
    public TurDbAdapter open() throws SQLException {
        mDbHjelper = new DatabaseHjelper(mContext);
        mDb = mDbHjelper.getWritableDatabase();
        return this;
    }

    // Lukker databasen med hjelperen
    public void close() {
        if (mDbHjelper != null) {
            mDbHjelper.close();
        }
    }

    // Oppgraderer databasen til ny versjon
    public void upgrade() throws SQLException {
        mDbHjelper = new DatabaseHjelper(mContext);
        mDb = mDbHjelper.getWritableDatabase();
        mDbHjelper.onUpgrade(mDb, 1, 0); // TODO: andre verdier?
    }

    // Databasevariabler for SQLite
    private static final String DB_NAVN = "TUR_DB.db";
    private static final int DB_VERSJON = 1;
    private static final String TUR_TABLE = "turMål";
    public static final String TID = "tid";
    public static final String NAVN = "navn";
    public static final String BESKRIVELSE = "beskrivelse";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUTDE = "longitude";
    public static final String MOH = "moh";
    public static final String TURBILDE = "turBilde";
    public static final String TURTYPE = "turType";
    public static final String REGISTRANT = "registrant";
    private static final String[] TUR_FIELDS = new String[]{
            TID,
            NAVN,
            BESKRIVELSE,
            LATITUDE,
            LONGITUTDE,
            MOH,
            TURBILDE,
            TURTYPE,
            REGISTRANT
    };

    // SQL for å opprette databasen
    private static final String CREATE_TUR_TABLE =
            "create table " + TUR_TABLE + "("
                    + TID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NAVN + " TEXT NOT NULL, "
                    + BESKRIVELSE + " TEXT NOT NULL, "
                    + LATITUDE + " REAL NOT NULL, "
                    + LONGITUTDE + " REAL NOT NULL, "
                    + MOH + " INTEGER NOT NULL, "
                    + TURBILDE + " TEXT NOT NULL, "
                    + TURTYPE + " TEXT NOT NULL, "
                    + REGISTRANT + " TEXT NOT NULL"
                    + ");";

    // Create-metode for tur
    public long nyTur(ContentValues initialValues) {
        return mDb.insertWithOnConflict(TUR_TABLE, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public long nyTur(Tur nyTur) {
        ContentValues newValues = new ContentValues();
        newValues.put(TurDbAdapter.NAVN, nyTur.getNavn());
        newValues.put(TurDbAdapter.BESKRIVELSE, nyTur.getBeskrivelse());
        newValues.put(TurDbAdapter.LATITUDE, nyTur.getLatitude());
        newValues.put(TurDbAdapter.LONGITUTDE, nyTur.getLongitude());
        newValues.put(TurDbAdapter.MOH, nyTur.getMoh());
        newValues.put(TurDbAdapter.TURBILDE, nyTur.getBilde());
        newValues.put(TurDbAdapter.TURTYPE, nyTur.getType());
        newValues.put(TurDbAdapter.REGISTRANT, nyTur.getRegistrant());
        return mDb.insertWithOnConflict(TUR_TABLE, null, newValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    // Read-metode for en enkelt tur
    public Cursor hentTur(int turId) {
        String[] selectionArgs = {String.valueOf(turId)};
        return mDb.query(TUR_TABLE, TUR_FIELDS, TID, selectionArgs, null, null, null);
    }

    // Read-metode for alle turer
    public Cursor hentAlleTurer() {
        return mDb.query(TUR_TABLE, TUR_FIELDS, null, null, null, null, null);
    }

    // Omgjør en Cursor om til et Tur-objekt
    public static Tur cursorTilPie(Cursor cursor) {
        Tur tur = new Tur();
        tur.setNavn(cursor.getString(cursor.getColumnIndex(NAVN)));
        tur.setBeskrivelse(cursor.getString(cursor.getColumnIndex(BESKRIVELSE)));
        tur.setLatitude(cursor.getFloat(cursor.getColumnIndex(LATITUDE)));
        tur.setLongitude(cursor.getFloat(cursor.getColumnIndex(LONGITUTDE)));
        tur.setType(cursor.getString(cursor.getColumnIndex(TURTYPE)));
        tur.setBilde(cursor.getString(cursor.getColumnIndex(TURBILDE)));
        tur.setRegistrant(cursor.getString(cursor.getColumnIndex(REGISTRANT)));
        return tur;
    }


    // Indre klasse for opprettelse av DB
    private static class DatabaseHjelper extends SQLiteOpenHelper {

        SQLiteDatabase mDb;

        public DatabaseHjelper(Context context) {
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
