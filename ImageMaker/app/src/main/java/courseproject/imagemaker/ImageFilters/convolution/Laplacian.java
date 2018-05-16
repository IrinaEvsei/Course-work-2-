package courseproject.imagemaker.ImageFilters.convolution;

import android.app.Activity;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import courseproject.imagemaker.ImageFilters.AbstractFilter;

public class Laplacian extends AbstractFilter {

    public Laplacian(Bitmap src, Activity activity) {
        super(src, activity);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);
        Mat mat = new Mat();
        Utils.bitmapToMat(result,mat);
        Imgproc.Laplacian(mat, mat, CvType.CV_8U, 3, 1, 0);
        Utils.matToBitmap(mat,result);
        return result;
    }
}
