package com.example.assignment30;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.eclipse.egit.github.core.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * ImageParser --- Utility class (intended to be run in worker thread)
 *                 for obtaining a Bitmap or image information from a URL.
 * @author      Scott Wolfskill, wolfski2
 * @created     02/05/2019
 * @last_edit   02/05/2019
 */
public class ImageParser {

    private ImageParser() {}

    /**
     * Get a GitHub user's profile avatar image as a BitmapDrawable.
     * @param user GitHub User to obtain profile image from.
     * @param profileImage_width Minimum width to compress the profile picture to.
     * @param profileImage_height Minimum height to compress the profile picture to.
     * @return Drawable (BitmapDrawable) profile avatar image.
     * @throws Exception
     */
    public static Drawable getProfileImage(User user, int profileImage_width, int profileImage_height) throws Exception {
        URL profilePicURL = new URL(user.getAvatarUrl());
        Bitmap compressedProfilePic = getImageFromURL(profilePicURL, profileImage_width, profileImage_height);
        BitmapDrawable drawableProfilePic = new BitmapDrawable(compressedProfilePic);
        return drawableProfilePic;
    }

    /**
     * Gets a Bitmap from a URL, and compresses it down to no smaller than reqWidth x reqHeight.
     * @param imageURL URL to obtain the image from.
     * @param reqWidth Width to compress the image near to (reqWidth <= resulting width <= ceiling(log2(reqWidth))
     * @param reqHeight Height to compress the image near to (reqHeight <= resulting height <= ceiling(log2(reqHeight))
     * @return Bitmap of the image obtained.
     * @throws IOException Throws IOException if a stream could not be read from the URL.
     */
    public static Bitmap getImageFromURL(URL imageURL, int reqWidth, int reqHeight) throws IOException {
        BitmapFactory.Options options = getImageDimensions(imageURL);
        InputStream imageStream = imageURL.openStream();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap image = BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();
        return image;
    }

    /**
     * Gets the dimensions of an image from a URL, set in a BitmapFactory.Options.
     * @param imageURL URL to obtain the image from.
     * @return BitmapFactory.Options where image dimensions are accessible in outHeight and outWidth.
     * @throws IOException Throws IOException if a stream could not be read from the URL.
     */
    public static BitmapFactory.Options getImageDimensions(URL imageURL) throws IOException {
        InputStream bitmapStream = imageURL.openStream();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //only obtain dimensions information from bitmapStream
        BitmapFactory.decodeStream(bitmapStream, null, options);
        bitmapStream.close();
        return options;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // https://stackoverflow.com/questions/25719620/how-to-solve-java-lang-outofmemoryerror-trouble-in-android
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
