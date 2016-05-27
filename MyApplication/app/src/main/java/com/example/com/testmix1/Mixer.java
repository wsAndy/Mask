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

    //static float[] faceFrame;

    private FaceServiceClient faceServiceClient =   new FaceServiceRestClient("92bbba5fbe434af7a34762a8e389d632");

    private ProgressDialog detectionProgressDialog;

    private Image3DSwitchView imageSwitchView;
    private static tool myTool;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mixer);

        //faceFrame = new float[4];
        imgview = (ImageView)findViewById(R.id.img_ori);

        myTool = new tool();

        Intent intent = getIntent();
        if(intent!=null)
        {
            imagePath = intent.getStringExtra("imagePath");
            humanFace = BitmapFactory.decodeFile(imagePath);

            if(humanFace.getHeight() > 300 && humanFace.getWidth() > 300)
            {
                Toast.makeText(Mixer.this,"[test] too big ",Toast.LENGTH_SHORT).show();

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

    private  void jump()
    {

        Intent intent = new Intent(Mixer.this, Output.class);

//        Bundle b = new Bundle();
//        b.putFloatArray("faceFrame",faceFrame);
//        intent.putExtra("imagePath", imagePath);  // 这个的作用几乎就没有了，在剪切那边我们使用一个临时的图片来替代了
        intent.putExtra("bgIndex", bgIndex);
//        intent.putExtras(b);
        Toast.makeText(Mixer.this,"Mixer",Toast.LENGTH_SHORT).show();

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





    // Detect faces by uploading face images
// Frame faces after detection

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
                        Toast.makeText(Mixer.this,"  result  !", Toast.LENGTH_LONG).show();
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();
                        if (result == null) {
                            Toast.makeText(Mixer.this,"Have not accept the message from server.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(result.length > 0) {
                            imgview.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));

//                            Toast.makeText(Mixer.this," HAHAH !", Toast.LENGTH_LONG).show();
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
            {   Face face = faces[0];// 只选择第一个

                FaceLandmarks facelandmarks = face.faceLandmarks;


//                Bitmap map_eyes = myTool.getClipImage(originalBitmap,faceRectF);
                //  x y width height
                double X = facelandmarks.eyebrowLeftOuter.x ;
                double Y = min(facelandmarks.eyebrowLeftOuter.y,facelandmarks.eyebrowRightOuter.y,facelandmarks.noseRootLeft.y,facelandmarks.noseRootRight.y);
                double width = facelandmarks.eyebrowRightOuter.x - facelandmarks.eyebrowLeftOuter.x;
                double height = facelandmarks.underLipBottom.y - min(facelandmarks.eyebrowLeftOuter.y,facelandmarks.eyebrowRightOuter.y)*0.9;

                Bitmap map_eyes = Bitmap.createBitmap(originalBitmap,(int)X,(int)Y,(int)width,(int)height );

                File sd = Environment.getExternalStorageDirectory();
                String path = sd.getPath() + "/CrazyMask/tem";

                myTool.saveMyBitmap(map_eyes, path, "img_face");

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
