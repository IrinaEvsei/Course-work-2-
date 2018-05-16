package courseproject.imagemaker;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.coloromatic.ColorOMaticDialog;
import es.dmoral.coloromatic.IndicatorMode;
import es.dmoral.coloromatic.OnColorSelectedListener;
import es.dmoral.coloromatic.colormode.ColorMode;
import courseproject.imagemaker.ImageFilters.adjust.Brightness;
import courseproject.imagemaker.ImageFilters.filters.ColorPicker;
import courseproject.imagemaker.ImageFilters.adjust.Contrast;
import courseproject.imagemaker.ImageFilters.filters.Greyscale;
import courseproject.imagemaker.ImageFilters.filters.histogramequalization.HistogramEqualizationOCV;
import courseproject.imagemaker.ImageFilters.filters.Histogram;
import courseproject.imagemaker.ImageFilters.filters.Negative;
import courseproject.imagemaker.ImageFilters.filters.Sepia;
import courseproject.imagemaker.ImageFilters.filters.Sketch;
import courseproject.imagemaker.ImageFilters.convolution.Laplacian;
import courseproject.imagemaker.ImageFilters.convolution.Sobel;

public class MainActivity extends AppCompatActivity {

    private static final int LOAD_PICTURE_GALLERY = 1;
    private static final int LOAD_PICTURE_CAMERA = 2;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String TAG = "MainActivityLog";
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public ImageViewActivity mainImageView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private LinearLayout menu;
    private LinearLayout filterOptions;
    private String imagePath = "";
    private LinearLayout currentActiveFiltersMenu;

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainImageView = (ImageViewActivity) findViewById(R.id.imageView);
        menu = (LinearLayout) findViewById(R.id.menuPickerLinearLayout);
        filterOptions = (LinearLayout) findViewById(R.id.filterOptionsLinearLayout);
        Button undoButton = (Button) findViewById(R.id.undoButton);

        if (undoButton != null) {
            undoButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mainImageView.resetPicture();
                    return true;
                }
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clearFilter();
        onMenuBack(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOAD_PICTURE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap loadedBitmap = BitmapFactory.decodeFile(picturePath);
            mainImageView.setImageBitmap(loadedBitmap);
            mainImageView.setAdjustViewBounds(true);
        }

        if (requestCode == LOAD_PICTURE_CAMERA && resultCode == Activity.RESULT_OK) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                mainImageView.setImageBitmap(myBitmap);
            }

        }

    }

    public void onMenuBack(View view) {
        clearFilter();
        if (currentActiveFiltersMenu != null) {
            currentActiveFiltersMenu.setVisibility(View.GONE);
            menu.setVisibility(View.VISIBLE);
        }
    }

    public void onCategoriesMenu(View view) {
        menu.setVisibility(View.INVISIBLE);
        switch (view.getId()) {
            case R.id.adjustButton:
                LinearLayout adjustLinearLayout = (LinearLayout) findViewById(R.id.adjustLinearLayout);
                if (adjustLinearLayout != null) {
                    adjustLinearLayout.setVisibility(View.VISIBLE);
                    currentActiveFiltersMenu = adjustLinearLayout;
                }
                break;
            case R.id.filterButton:
                LinearLayout filterLinearLayout = (LinearLayout) findViewById(R.id.filterLinearLayout);
                if (filterLinearLayout != null) {
                    filterLinearLayout.setVisibility(View.VISIBLE);
                    currentActiveFiltersMenu = filterLinearLayout;
                }
                break;
            case R.id.convolutionButton:
                LinearLayout convolutionLinearLayout = (LinearLayout) findViewById(R.id.convolutionLinearLayout);
                if (convolutionLinearLayout != null) {
                    convolutionLinearLayout.setVisibility(View.VISIBLE);
                    currentActiveFiltersMenu = convolutionLinearLayout;
                }
                break;
        }
    }

    private Bitmap getImageViewBitmap() {
        if (mainImageView != null) {
            return ((BitmapDrawable) mainImageView.getDrawable()).getBitmap();
        }
        return null;
    }

    private void clearFilter() {

        if (filterOptions != null) {
            filterOptions.removeAllViews();
        }
    }

    public void onBrightness(View view) {
        clearFilter();
        SeekBar s = new SeekBar(this);
        s.setMax(200);
        s.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        s.setProgress(100);

        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                try {
                    Brightness brightness = new Brightness(getImageViewBitmap(), (seekBar.getProgress() - 100), MainActivity.this);
                    brightness.execute();
                    clearFilter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        filterOptions.addView(s);
    }

    public void onContrast(View view) {
        clearFilter();
        SeekBar s = new SeekBar(this);
        s.setMax(200);
        s.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        s.setProgress(100);

        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Contrast contrast = new Contrast(getImageViewBitmap(), (seekBar.getProgress() - 100), MainActivity.this);
                contrast.execute();
            }
        });

        filterOptions.addView(s);
    }

    public void onHistogramEqualization(View view) {
        clearFilter();

        HistogramEqualizationOCV histogramEqualization = new HistogramEqualizationOCV(getImageViewBitmap(), this);
        histogramEqualization.execute();
    }

    public void onColorPicker(View view) {
        clearFilter();
        this.showDialog(false);

    }

    public void onHistogram(View view) {
        clearFilter();
        this.showDialog(true);

    }

    public void onGreyscale(View view) {
        clearFilter();

        Greyscale greyscale = new Greyscale(getImageViewBitmap(), this);
        greyscale.execute();

    }

    public void onSepia(View view) {
        clearFilter();

        Sepia sepia = new Sepia(getImageViewBitmap(), this);
        sepia.execute();

    }

    public void onSketch(View view) {
        clearFilter();

        Sketch sketch = new Sketch(getImageViewBitmap(),this);
        sketch.execute();
    }

    public void onNegative(View view) {
        clearFilter();

        Negative negative = new Negative(getImageViewBitmap(), this);
        negative.execute();
    }

    public void onLaplacian(View view) {
        clearFilter();
        Laplacian laplacian = new Laplacian(getImageViewBitmap(), this);
        laplacian.execute();
    }

    public void onSobel(View view) {
        clearFilter();

        Sobel sobel = new Sobel(getImageViewBitmap(), this);
        sobel.execute();

    }

    public void onUndo(View view) {
        mainImageView.undoModification();
    }

    public void onSaveButtonClicked(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.NameSavedImageDialog);
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.saveimg_activity, null);
        builder.setView(dialogView);
        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTxtDialogSaveImage);
        builder.setTitle("Enter a name");
        builder.setPositiveButton("SAVE",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String filename = editTextName.getText().toString();
                        if(!filename.isEmpty()){
                            AsyncTaskSaveImage asyncTaskSaveImage = new AsyncTaskSaveImage(filename);
                            asyncTaskSaveImage.execute();
                        }else{
                            final Snackbar snackbar = Snackbar
                                    .make(mainImageView, R.string.no_filename, Snackbar.LENGTH_LONG);
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    }
                });
        builder.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onLoadFromGallery(View view) {
        verifyStoragePermissions(MainActivity.this);
        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, LOAD_PICTURE_GALLERY);
    }

    public void onLoadFromCamera(View view) {
        verifyStoragePermissions(MainActivity.this);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        imagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(imagePath);
        Uri outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, LOAD_PICTURE_CAMERA);

    }

    private class AsyncTaskSaveImage extends AsyncTask {
        String fileName;

        private AsyncTaskSaveImage(String fn) {
            super();
            this.fileName = fn;
        }

        @Override
        protected Void doInBackground(Object[] params) {
            verifyStoragePermissions(MainActivity.this);
            Bitmap bmp = getImageViewBitmap();
            OutputStream fOut = null;
            try {
                File root = new File(Environment.getExternalStorageDirectory()
                        + File.separator + "ImageMaker" + File.separator);
                root.mkdirs();
                File sdImageMainDirectory = new File(root, this.fileName + ".png");
                fOut = new FileOutputStream(sdImageMainDirectory);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
            }
            try {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
                final Snackbar snackbar = Snackbar
                        .make(mainImageView, R.string.image_saved, Snackbar.LENGTH_LONG);
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            } catch (Exception e) {
                Log.d("exception", e.getMessage());
                Toast.makeText(MainActivity.this, R.string.image_not_saved, Toast.LENGTH_SHORT).show();
            }

            return null;
        }

    }

    private void showDialog(final boolean isHue) {
        new ColorOMaticDialog.Builder()
                .initialColor(Color.RED)
                .colorMode(ColorMode.RGB)
                .indicatorMode(IndicatorMode.DECIMAL)
                .onColorSelected(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(@ColorInt int color) {
                        try {
                            if (!isHue) {
                                final ColorPicker colorFilter = new ColorPicker(getImageViewBitmap(), color, MainActivity.this);
                                colorFilter.execute();
                            } else {
                                final Histogram hueColorize = new Histogram(getImageViewBitmap(), color, MainActivity.this);
                                hueColorize.execute();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .showColorIndicator(true)
                .create()
                .show(getSupportFragmentManager(), "ColorDialog");
    }

}
