package courseproject.imagemaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.Stack;

public class ImageViewActivity extends AppCompatImageView {

    static final int NONE = 0;
    static final int SCROLL = 1;
    static final int ZOOM = 2;
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    private Stack<Bitmap> bitmapStack = new Stack<>();

    private int mode = NONE;
    private float dist;
    private PointF sPoint = new PointF();
    private PointF mPoint = new PointF();

    public ImageViewActivity(Context context) {
        super(context);
    }

    public ImageViewActivity(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewActivity(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (bitmapStack.size() < 1 || !(bitmapStack.peek() == bm))
            bitmapStack.push(bm);
    }

    public void undoModification() {
        if (bitmapStack.size() > 1) {
            Bitmap peeked = bitmapStack.peek();
            setImageBitmap(peeked);
        }
    }

    public void resetPicture() {
        Bitmap firstBM = bitmapStack.firstElement();
        if (firstBM != bitmapStack.peek()) {
            bitmapStack.clear();
        }
        setImageBitmap(firstBM);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.setScaleType(ImageViewActivity.ScaleType.MATRIX);
        float scale;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                sPoint.set(event.getX(), event.getY());
                setMode(SCROLL);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                dist = calculateDist(event);
                if (dist > 5f) {
                    savedMatrix.set(matrix);
                    calculateMidPoint(mPoint, event);
                    setMode(ZOOM);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                setMode(NONE);
                break;

            case MotionEvent.ACTION_MOVE:

                if (getMode() == SCROLL) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - sPoint.x, event.getY() - sPoint.y);
                } else if (getMode() == ZOOM) {
                    float newDist = calculateDist(event);
                    if (newDist > 5f) {
                        matrix.set(savedMatrix);
                        scale = newDist / dist;
                        matrix.postScale(scale, scale, mPoint.x, mPoint.y);
                    }
                }
                break;
        }

        this.setImageMatrix(matrix);
        return true;
    }

    private float calculateDist(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void calculateMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

}


