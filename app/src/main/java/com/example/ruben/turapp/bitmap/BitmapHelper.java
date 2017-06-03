package com.example.ruben.turapp.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ruben on 03.06.2017.
 */

// Hjelpeklasse for å dekode og enkode et Bitmap
public class BitmapHelper {

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

    public static Bitmap roterBitmap(Bitmap bilde, int orientasjon) {
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
        try {
            return Bitmap.createBitmap(bilde, 0, 0, bilde.getWidth(), bilde.getHeight(), matrise, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}
