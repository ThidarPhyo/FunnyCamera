package jp.ac.jec.cm0129.funnycamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import android.media.MediaPlayer;
import android.widget.RelativeLayout;

public class CameraActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = "CameraActivity";
    private ImageCapture imageCapture;
    PreviewView previewView;
    private ImageButton btnCapture;
    String fileName, dateTime;

    private int screenX,screenY;
    private ImageView target;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private boolean isCaptureButtonVisible = true;
    int mode;

    private MediaPlayer mediaPlayer;

    private  EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAGS_CHANGED);

        setContentView(R.layout.activity_camera);
        target = findViewById(R.id.imageView2);
        target.setOnTouchListener(this);
        mediaPlayer = MediaPlayer.create(this, R.raw.screen_shot);


        parseMode();

        previewView = findViewById(R.id.previewView);

        cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        btnCapture = findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCaptureButtonVisible) {

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                    dateTime = dateFormat.format(new Date());
                    fileName = "photo_" + dateTime + ".jpg";
                    //saveScreenShot();
                    File file = new File(getFilesDir(), fileName);

                    ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                    imageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(), new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Bitmap photo = BitmapFactory.decodeFile(file.getPath());
                            Bitmap result = combineBitmap(photo,screenShot());
                            OutputStream out = null;
                            try {
                                out = new FileOutputStream(file);
                                result.compress(Bitmap.CompressFormat.JPEG,100,out);
                                out.close();
                                putGallery(file);
                            } catch (FileNotFoundException e){
                                e.printStackTrace();
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                            // After saving the image, make the capture button visible again
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    btnCapture.setVisibility(View.VISIBLE);
//                                }
//                            });

                            btnCapture.setVisibility(View.INVISIBLE);
                            // Play the screenshot sound after the screenshot is saved
                            playScreenshotSound();
                            //Snackbar.make(getWindow().getDecorView(), "Image Saved！！", Snackbar.LENGTH_LONG).show();
                            Log.i(TAG, "onImageSaved");
                            Log.i(TAG, outputFileResults.getSavedUri().toString());
                            // Post a delayed action to make the capture button visible again after some time (e.g., 2 seconds)
                            view.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    btnCapture.setVisibility(View.VISIBLE);
                                }
                            }, 2000);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.i(TAG, "onError");
                            Log.i(TAG, exception.getMessage());
                        }

                    });
                }

            }
        });


        startCamera();


    }

    @Override
    protected void onResume() {
        super.onResume();
        parseMode();
    }
    // Add the following method to release the MediaPlayer resources when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    public void parseMode() {
        target = findViewById(R.id.imageView2);
        RelativeLayout imageLayout = findViewById(R.id.imageLayout); // Reference to the RelativeLayout containing ImageView and EditText

        // Retrieve the mode value from the intent extras
        mode = getIntent().getIntExtra("mode", -1);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        int imageWidthPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics()
        );
        int imageHeightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics()
        );

        // Use the 'mode' value to handle different camera modes
        switch (mode) {
            case 1:
                target.setVisibility(View.GONE);
                imageLayout.setVisibility(View.GONE);
                break;
            case 2:
                // Set the visibility of the existing ImageView
                target.setImageResource(R.drawable.item2);
                target.setVisibility(View.VISIBLE);
                imageLayout.setVisibility(View.GONE);

                // Set fixed width and height for the default image (convert 250dp to pixels)

                layoutParams.width = imageWidthPx;
                layoutParams.height = imageHeightPx;

                // Reset margins
                layoutParams.setMargins(0, 0, 0, 0);
                break;
            case 3:
                target.setImageResource(R.drawable.f00330);
                target.setVisibility(View.VISIBLE);
                imageLayout.setVisibility(View.GONE);

                int spacingPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()
                );
                layoutParams.setMargins(spacingPx, spacingPx, spacingPx, spacingPx);
                break;
            case 4:
                // Hide the original ImageView and show the RelativeLayout

                target.setVisibility(View.VISIBLE);
                imageLayout.setVisibility(View.VISIBLE); // Show the RelativeLayout in case 4
                // Set fixed width and height for the default image
                layoutParams.width = imageWidthPx;
                layoutParams.height = imageHeightPx;
                // Reset margins
                layoutParams.setMargins(0, 0, 0, 0);

                break;
            default:
                // Handle the case when an invalid or no mode is selected
                break;
        }

        // Apply the layoutParams to the target ImageView
        target.setLayoutParams(layoutParams);
    }

    // Add the following method to play the sound
    private void playScreenshotSound() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    public void startCamera(){
        // 余力のある人は「ラムダ式」を調べて導入してみよう
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // Camera provider is now guaranteed to be available
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    // Set up the view finder use case to display camera preview
                    Preview preview = new Preview.Builder().build();

                    // Choose the camera by requiring a lens facing
                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();
                    imageCapture = new ImageCapture.Builder().build();

                    // Attach use cases to the camera with the same lifecycle owner
                    Camera camera = cameraProvider.bindToLifecycle(
                            ((LifecycleOwner) CameraActivity.this),
                            cameraSelector,
                            preview, imageCapture);

                    // Connect the preview use case to the previewView
                    preview.setSurfaceProvider(
                            previewView.getSurfaceProvider());


                } catch (InterruptedException | ExecutionException e) {
                    // Currently no exceptions thrown. cameraProviderFuture.get()
                    // shouldn't block since the listener is being called, so no need to
                    // handle InterruptedException.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void putGallery(File file){
        ContentResolver resolver = getApplicationContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        //contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"photo.jpg");
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"photo_"+dateTime);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
        Uri imageUrl = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        try {
            OutputStream fos = resolver.openOutputStream(imageUrl);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private Bitmap screenShot() {
        Bitmap retBitmap = null;
        ViewGroup rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        View frameView = null;
        if(rootView != null && rootView.getChildCount() != 0){
            frameView = rootView.getChildAt(0);
        }
        if(frameView != null){
            frameView.setDrawingCacheEnabled(true);
            retBitmap = Bitmap.createBitmap(frameView.getDrawingCache());
            frameView.setDrawingCacheEnabled(false);
        }
        return  retBitmap;
    }
    private void saveScreenShot() {
        final String fName = "screenshot.jpg";
        //final String tmpFileName = "temp.jpg";
        File file = new File(getFilesDir(),fName);
        Bitmap bitmap = screenShot();
        //ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.close();
            //putGallery(file);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private Bitmap combineBitmap(Bitmap b1, Bitmap b2){
        Bitmap result = Bitmap.createBitmap(b1.getWidth(),b1.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(b1,0,0,null);
        b1.recycle();
        Rect srcRect = new Rect(0,0,b2.getWidth(),b2.getHeight());
        Rect dstRect = new Rect(0,0,result.getWidth(),result.getHeight());
        canvas.drawBitmap(b2,srcRect,dstRect,null);
        b2.recycle();
        return  result;
    }
    public boolean onTouch(View v, MotionEvent event){
        int x = (int)event.getRawX();
        int y = (int)event.getRawY();
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                int dx = target.getLeft() + (x - screenX);
                int dy = target.getTop() + (y - screenY);
                target.layout(dx,dy,dx + target.getWidth(), dy + target.getHeight());
                break;
        }
        screenX = x;
        screenY = y;

        return true;
    }



}