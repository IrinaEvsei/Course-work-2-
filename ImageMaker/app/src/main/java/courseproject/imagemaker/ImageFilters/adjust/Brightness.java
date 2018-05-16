package courseproject.imagemaker.ImageFilters.adjust;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;

import courseproject.imagemaker.ImageFilters.AbstractFilter;

public class Brightness extends AbstractFilter {

    private float value;

    public Brightness(Bitmap src, float value, Activity activity) {
        super(src, activity);

        this.value = value;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        publishProgress("Editing...");
        if (value == 0 || value > 100 || value < -100) {
            return src;
        }
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);

        int imgHeight = result.getHeight();
        int imgWidth = result.getWidth();

        int[] pixels = new int[imgWidth * imgHeight];
        result.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        for (int i = 0; i < pixels.length; i++) {
            float[] hsv = new float[3];
            Color.colorToHSV(pixels[i], hsv);
            hsv[2] = hsv[2] + (value / 100);

            pixels[i] = Color.HSVToColor(hsv);
        }

        result.setPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);
        return result;
    }

}
