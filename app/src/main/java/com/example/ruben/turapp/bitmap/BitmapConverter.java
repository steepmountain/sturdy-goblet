package com.example.ruben.turapp.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ruben on 03.06.2017.
 */

// Hjelpeklasse for å dekode og enkode et Bitmap
public class BitmapConverter {

    public static String bitmapTilString(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] enkod = out.toByteArray();
        String bitmapString = Base64.encodeToString(enkod, Base64.DEFAULT);
        return bitmapString;
    }

    public static Bitmap stringTilBitmap(String string) {
        try {
            byte[] dekod = Base64.decode(string, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(dekod, 0, dekod.length);
            return bitmap;
        } catch (Exception e) {
            // Noe gikk galt med dekoding av bilde
            return null;
        }
    }
}
