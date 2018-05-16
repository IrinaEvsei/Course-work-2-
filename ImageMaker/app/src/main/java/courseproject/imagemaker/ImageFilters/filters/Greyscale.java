package courseproject.imagemaker.ImageFilters.filters;

import android.app.Activity;
import android.graphics.Bitmap;

import courseproject.imagemaker.ImageFilters.AbstractFilter;

/**
 * Changes an image to its black and white version
 */
public class Greyscale extends AbstractFilter {

    public Greyscale(Bitmap src, Activity activity) {
        super(src, activity);

    }

    @Override
    protected Bitmap doInBackground(String... params) {
        publishProgress("Editing..."); // Calls onProgressUpdate()
        try {

            int imgHeight = src.getHeight();
            int imgWidth = src.getWidth();

            Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);

            int[] pixels = new int[imgWidth * imgHeight];
            result.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

            for (int i = 0; i < imgHeight * imgWidth; i++) {
                int pixel = pixels[i];
                pixels[i] = greyOutPixel(pixel);
            }
            result.setPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);
            return result;

        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

}
