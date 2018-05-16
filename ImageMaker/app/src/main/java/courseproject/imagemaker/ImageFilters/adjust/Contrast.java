package courseproject.imagemaker.ImageFilters.adjust;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;

import courseproject.imagemaker.ImageFilters.AbstractFilter;

/**
 * Changes the overall contrast of an image using an adjustment value
 */
public class Contrast extends AbstractFilter {

    private float value;

    /**
     * @param src   Source image to be modified
     * @param value Contract adjusment value, between -100 and 100
     */
    public Contrast(Bitmap src, float value, Activity activity) {
        super(src, activity);

        this.value = value;
    }


    @Override
    protected Bitmap doInBackground(String... params) {
        if (value == 0 || value > 100 || value < -100) {
            return src;
        }

        value = (value * 2.55f);

        // 259 is an arbitrary value used to reduce or increase the histogram range according to the value
        float factor = (259 * (value + 255)) / (255 * (259 - value));

        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);

        int imgHeight = result.getHeight();
        int imgWidth = result.getWidth();

        // Getting each pixel in the Bitmap
        int[] pixels = new int[imgWidth * imgHeight];
        result.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];

            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);

            int newRed = (int) ensureRange(factor * (red - 128) + 128,0,255);
            int newGreen = (int) ensureRange(factor * (green - 128) + 128,0,255);
            int newBlue = (int) ensureRange(factor * (blue - 128) + 128,0,255);
            pixels[i] = Color.rgb(newRed, newGreen, newBlue);
        }

        result.setPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);
        return result;
    }
}
