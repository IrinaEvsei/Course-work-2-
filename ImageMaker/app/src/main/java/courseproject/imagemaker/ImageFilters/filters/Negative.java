package courseproject.imagemaker.ImageFilters.filters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import courseproject.imagemaker.ImageFilters.AbstractFilter;

public class Negative extends AbstractFilter {

    public Negative(Bitmap src, Activity activity) {
        super(src, activity);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        -1, 0, 0, 0, 255,
                        0, -1, 0, 0, 255,
                        0, 0, -1, 0, 255,
                        0, 0, 0, 1, 0
                });

        Bitmap result = this.src.copy(Bitmap.Config.ARGB_8888,true);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(result, 0, 0, paint);

        return result;
    }
}
