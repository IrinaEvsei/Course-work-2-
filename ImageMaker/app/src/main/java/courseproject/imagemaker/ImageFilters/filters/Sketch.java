package courseproject.imagemaker.ImageFilters.filters;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.nio.IntBuffer;

import courseproject.imagemaker.ImageFilters.AbstractFilter;

public class Sketch extends AbstractFilter {

    public Sketch(Bitmap src, Activity activity) {
        super(src, activity);
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        Bitmap copy = src.copy(Bitmap.Config.ARGB_8888, true);
        copy = greyscale(copy);
        copy = invertImage(copy);

        Mat mat = new Mat();
        Utils.bitmapToMat(copy,mat);
        org.opencv.core.Size s = new Size(5,5);
        Imgproc.GaussianBlur(mat,mat,s,2);
        Utils.matToBitmap(mat,copy);

        Bitmap copy2 = src.copy(Bitmap.Config.ARGB_8888, true);
        copy2 = greyscale(copy2);

        Bitmap result = colorDodgeBlend(copy, copy2);
        return result;
    }

    public Bitmap colorDodgeBlend(Bitmap source, Bitmap layer) {
        Bitmap base = source.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap blend = layer.copy(Bitmap.Config.ARGB_8888, false);

        IntBuffer buffBase = IntBuffer.allocate(base.getWidth() * base.getHeight());
        base.copyPixelsToBuffer(buffBase);
        buffBase.rewind();

        IntBuffer buffBlend = IntBuffer.allocate(blend.getWidth() * blend.getHeight());
        blend.copyPixelsToBuffer(buffBlend);
        buffBlend.rewind();

        IntBuffer buffOut = IntBuffer.allocate(base.getWidth() * base.getHeight());
        buffOut.rewind();

        while (buffOut.position() < buffOut.limit()) {

            int filterInt = buffBlend.get();
            int srcInt = buffBase.get();

            int redValueFilter = Color.red(filterInt);
            int greenValueFilter = Color.green(filterInt);
            int blueValueFilter = Color.blue(filterInt);

            int redValueSrc = Color.red(srcInt);
            int greenValueSrc = Color.green(srcInt);
            int blueValueSrc = Color.blue(srcInt);

            int redValueFinal = colorChange(redValueFilter, redValueSrc);
            int greenValueFinal = colorChange(greenValueFilter, greenValueSrc);
            int blueValueFinal = colorChange(blueValueFilter, blueValueSrc);


            int pixel = Color.argb(255, redValueFinal, greenValueFinal, blueValueFinal);


            buffOut.put(pixel);
        }

        buffOut.rewind();

        base.copyPixelsFromBuffer(buffOut);
        blend.recycle();

        return base;
    }

    private int colorChange(int in1, int in2) {
        float image = (float)in2;
        float mask = (float)in1;
        return ((int) ((image == 255) ? image:Math.min(255, (((long)mask << 8 ) / (255 - image)))));
    }

    private Bitmap invertImage(Bitmap source){
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        -1, 0, 0, 0, 255,
                        0, -1, 0, 0, 255,
                        0, 0, -1, 0, 255,
                        0, 0, 0, 1, 0
                });

        Bitmap result = source.copy(Bitmap.Config.ARGB_8888,true);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(result, 0, 0, paint);

        return result;
    }

    private Bitmap greyscale(Bitmap src) {
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
    }
}
