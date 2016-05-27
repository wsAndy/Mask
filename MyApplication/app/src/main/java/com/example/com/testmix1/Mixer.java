package com.example.com.testmix1;

import android.app.Notification;;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import com.example.com.testmix1.Image3DSwitchView.OnImageSwitchListener;

/**
 * Created by 938310808@qq.com on 2016/3/27.
 */
public class Mixer extends ActionBarActivity {
    String  imagePath;
    private Bitmap humanFace;
    ImageView imgview ;
    ImageButton img_btn_plus;
    int bgIndex;

    static float[] faceFrame;

    private FaceServiceClient faceServiceClient =
            new FaceServiceRestClient("92bbba5fbe434af7a34762a8e389d632");

    private ProgressDialog detectionProgressDialog;

    private Image3DSwitchView imageSwitchView;
    private static tool myTool;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mixer);


        faceFrame = new float[4];
        imgview = (ImageView)findViewById(R.id.img_ori);

        myTool = new tool();

        Intent intent = getIntent();
        if(intent!=null)
        {
            imagePath = intent.getStringExtra("imagePath");
            humanFace = BitmapFactory.decodeFile(imagePath);

            /**
             *   compress the image to speed up the process
             * */
            if(humanFace.getHeight() > 300 && humanFace.getWidth() > 300)
            {
                humanFace = myTool.compressImage(humanFace);
            }

            imgview.setImageBitmap(humanFace);
        }

        img_btn_plus = (ImageButton)findViewById(R.id.img_btn_plus);
        img_btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_btn_plus_start();
            }
        });

        detectionProgressDialog = new ProgressDialog(this);

        imageSwitchView = (Image3DSwitchView)findViewById(R.id.image_switch_view);

        imageSwitchView.setOnImageSwitchListener(new OnImageSwitchListener()
        {
         @Override
         public void onImageSwitch(int currentImage) {
             //Toast.makeText(Mixer.this,"current image is "+currentImage,Toast.LENGTH_SHORT).show();
             bgIndex = currentImage+1;
         }
         }

        );
        imageSwitchView.setCurrentImage(0);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageSwitchView.clear();
    }

    private void img_btn_plus_start() {
        // this bitmap is detected by Oxford.
        // here you can try to compress the bitmap .
        detectAndFrame(humanFace);
    }

    /**
     *  jump into the next screen
     * */
    private  void jump()
    {
        Intent intent = new Intent(Mixer.this, Output.class);
        intent.putExtra("bgIndex",bgIndex);
        startActivity(intent);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }




    /**
     *   detect the face
     * */
    private void detectAndFrame(final Bitmap imageBitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    true,        // returnFaceLandmarks
                                    null           // returnFaceAttributes: a string like "age, gender"
                            );
                            if (result == null)
                            {
                                publishProgress("Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(
                                    String.format("Detection Finished. %d face(s) detected",
                                            result.length));
                            return result;
                        } catch (Exception e) {
                            publishProgress("Detection failed");
                            return null;
                        }
                    }
                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();
                        if (result == null) {
                             return;
                        }

                        if(result.length > 0) {
                            imgview.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
                            imageBitmap.recycle();
                            jump();
                        }else{
                            Toast.makeText(Mixer.this," There is no face in the picture!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Mixer.this, MainActivity.class);
                            startActivity(intent);
                        }


                    }
                };
        detectTask.execute(inputStream);
    }


// static
    private   Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        int stokeWidth = 1;
        paint.setStrokeWidth(stokeWidth);

        if (faces != null) {
            //for (Face face : faces) {
            {
                /**
                 *   here we only choose the biggest face in the picture
                 * */
                Face face = faces[0];


                /**
                 *    get the features' sites on the face
                 * */
                FaceLandmarks facelandmarks = face.faceLandmarks;
                int left = (int)(min(facelandmarks.eyebrowLeftOuter.x,facelandmarks.eyeLeftTop.x));
                int top = (int)(min(facelandmarks.eyebrowLeftInner.y,facelandmarks.eyebrowRightInner.y));
                int right = (int)(max(facelandmarks.eyebrowRightOuter.x, facelandmarks.eyeRightOuter.y));
                int bottom = (int)(2*facelandmarks.underLipBottom.y-facelandmarks.upperLipBottom.y);

                RectF eyeRectF = new RectF(
                        left,
                        top,
                        right,
                        bottom
                );

                /**
                 *   clip the features from the face and save it in a temporary file
                 * */
                Bitmap map_eyes = myTool.getClipImage(originalBitmap,eyeRectF);
                File sd = Environment.getExternalStorageDirectory();
                String path = sd.getPath() + "/CrazyMask/tem";
                myTool.saveMyBitmap(map_eyes, path, "img_eyes");

            }
        }
        return bitmap;
    }

    static private double min(double  a,double b)
    {
        if(a<b)
        {
            return a;
        }
        else{
            return b;
        }
    }

    static private double min(double a,double b, double c, double d)
    {
        return min(min(a,b),min(c,d));
    }

    static private double max(double  a,double b)
    {
        if(a>b)
        {
            return a;
        }
        else{
            return b;
        }
    }

    static private double max(double a, double b, double c, double d)
    {
        return max(max(a, b), max(c, d));
    }
    static private double max(double a, double b, double c)
    {
        return max(a,max(b,c));
    }

}
