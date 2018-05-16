package courseproject.imagemaker.ImageFilters.filters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;

import courseproject.imagemaker.ImageFilters.AbstractFilter;


public class ColorPicker extends AbstractFilter {

    private final int colorPicker;
    private int COLOR_RANGE = 25;

    public ColorPicker(Bitmap src, int colorPicker, Activity activity) {
        super(src, activity);
        this.colorPicker = colorPicker;
    }


    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);

        int imgHeight = result.getHeight();
        int imgWidth = result.getWidth();

        int[] pixels = new int[imgWidth * imgHeight];
        result.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        float[] hsv = new float[3];
        Color.colorToHSV(colorPicker, hsv);
        float hue = hsv[0];
        for (int i = 0; i < imgHeight * imgWidth; i++) {

            int pixel = pixels[i];
            Color.colorToHSV(pixel, hsv);

            if (!(hsv[0] - COLOR_RANGE < hue && hue < hsv[0] + COLOR_RANGE && hsv[1] > 0.10)) {
                pixels[i] = greyOutPixel(pixel);
            }

        }
        result.setPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        return result;
    }
}
