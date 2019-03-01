package com.bikesharev2.davidsen.bikeshareassignment2.HelperClasses;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;


// Helper calls for handling picture scaling
public class PictureUtils {
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth){
            final int halfHeight = height/2;
            final int halfWidth = width/2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth){
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // calculates the dp to pixels
    private static int dpToPixel(int dp){
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static Bitmap getScaledBitmap(String path, int destWith, int destHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, dpToPixel(destWith), dpToPixel(destHeight));

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static byte[] convertBitmapToByteArray(Bitmap bitmap){
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(bitmap.getWidth() * bitmap.getHeight());
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, buffer);
        return buffer.toByteArray();
    }

    public static Bitmap convertByteArrayToBitmap(byte[] blop){
        return BitmapFactory.decodeByteArray(blop, 0, blop.length);
    }

    // Probably should not be here, but is used to show toast to the user
    public static void toaster(Context context, String text){
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
