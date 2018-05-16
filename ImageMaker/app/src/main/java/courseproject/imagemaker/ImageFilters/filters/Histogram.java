package courseproject.imagemaker.ImageFilters.filters;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;

import courseproject.imagemaker.ImageFilters.AbstractFilter;

public class Histogram extends AbstractFilter {

    private float[] hsv = new float[3];
    private int[] pixels;
    private int color;
    private int height;
    private int width;

    public Histogram(Bitmap src, int color, Activity activity){
        super(src, activity);
        this.color = color;
        this.height = src.getHeight();
        this.width = src.getWidth();
        pixels = new int[this.src.getHeight() * this.src.getWidth()];
    }

    private static float getHueValue(int integerColor) {
        float[] hsvValues = new float[3];
        Color.colorToHSV(integerColor, hsvValues);
        return hsvValues[0];
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888,true);
        result.getPixels(pixels,0,width,0,0,width,height);

        float h = getHueValue(color);
        for(int i = 0; i < this.height * this.width ; i++){
            Color.RGBToHSV(Color.red(pixels[i]),Color.green(pixels[i]),Color.blue(pixels[i]),hsv);
            hsv[0] = h;
            pixels[i] = Color.HSVToColor(hsv);
        }
        result.setPixels(pixels,0,this.width,0,0,this.width,this.height);
        return result;
    }

}
