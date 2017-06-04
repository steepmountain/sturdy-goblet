package com.example.ruben.turapp.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

/**
 * Hjelpemetoder for Bitmaps
 */
public class BitmapHelper {

    /**
     * Skalerer og roterer et bilde basert på en gitt målbredde
     * @param bildesti en lokal bildesti til et bitmap
     * @param målbredde hvor bredt bilde kan bli
     * @return et skalert, og rotert hvis mulig, bitmap
     */
    public static Bitmap roterOgSkalerBitmap(String bildesti, int målbredde) {
        // Setter opp BitmapFactory.Options i forhold til hvor bredt bilde kan bli
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bildesti, bmOptions);
        int bildebredde = bmOptions.outWidth;

        // Lager skalering basert på hvor bredt bilde kan bli
        int skalering = bildebredde/målbredde;
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = skalering;
        Bitmap skalertBilde = BitmapFactory.decodeFile(bildesti, bmOptions);


        // Prøver å lese EXIF-informasjonen til den gitte bildestien
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(bildesti);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exif != null) {
            // Hvis den fant EXIF-informasjon roterer den bilde basert på EXIF og returnerer dette
            int orientering = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            return BitmapHelper.roterBitmap(skalertBilde, orientering);
        }
        else {
            // Hvis ingen EXIF-informasjon roterer den kun det skalerte bilde
            return skalertBilde;
        }
    }

    /**
     * Roterer et Bitmap basert på orienteringer fr EXIF-informasjonen
     * @param bilde et bitmap som skal roteres
     * @param orientasjon en orienteringsverdi basert på EXIF
     * @return et rotert bitmap.
     */
    private static Bitmap roterBitmap(Bitmap bilde, int orientasjon) {
        // En tom matrise som får roteringsverdier ettersom hva orienteringen er
        Matrix matrise = new Matrix();
        switch (orientasjon) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bilde;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrise.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrise.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrise.setRotate(180);
                matrise.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrise.setRotate(90);
                matrise.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrise.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrise.setRotate(-90);
                matrise.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrise.setRotate(-90);
                break;
            default:
                return bilde;
        }
        // Lager et nytt bitmap ut av det roterte bildet og sender det tilbake
        try {
            return Bitmap.createBitmap(bilde, 0, 0, bilde.getWidth(), bilde.getHeight(), matrise, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}
