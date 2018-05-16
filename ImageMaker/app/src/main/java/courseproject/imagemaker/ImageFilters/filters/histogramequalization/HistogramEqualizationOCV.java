package courseproject.imagemaker.ImageFilters.filters.histogramequalization;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import courseproject.imagemaker.ImageFilters.AbstractFilter;

import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.split;

public class HistogramEqualizationOCV extends AbstractFilter {


    public HistogramEqualizationOCV(Bitmap src, Activity activity) {
        super(src, activity);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        long startTime = System.currentTimeMillis();

        Mat oldImg = new Mat();
        result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);

        Utils.bitmapToMat(src, oldImg);

        Imgproc.cvtColor(oldImg, oldImg, Imgproc.COLOR_BGR2YCrCb);

        List<Mat> channels = new ArrayList<>();
        split(oldImg, channels);

        Imgproc.equalizeHist(channels.get(0), channels.get(0));

        Mat newImg = new Mat();
        merge(channels, oldImg);

        Imgproc.cvtColor(oldImg, newImg, Imgproc.COLOR_YCrCb2BGR);

        Utils.matToBitmap(newImg, result);
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Log.i("HistEqualOCV", "HistEqualOCV Duration: " + elapsedTime);

        return result;
    }
}