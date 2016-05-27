package com.example.com.testmix1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by 938310808@qq.com on 2016/3/27.
 */
public class Output extends Activity implements View.OnTouchListener {

    // just for a test
//    static  float[] faceFrame;
    private int screenWidth ;
    private int screenHeight;
    private int lastX,lastY;

    int bgIndex;

    int mean ;
    float var;
    ImageView img_output;
    ImageView img_face;

    Bitmap bgbitmap;
    Bitmap imgFusion;
    tool mytool;
    Bitmap imgFace;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output);

        mytool = new tool();
        img_output = (ImageView)findViewById(R.id.img_output);
        img_face = (ImageView)findViewById(R.id.img_face);

        Intent intent = getIntent();
        if(intent!=null)
        {
            bgIndex = intent.getIntExtra("bgIndex",1);
        }


        mean = 20;
        var = 15;

        bgbitmap = getBgFromIndex(bgIndex);

        img_output.setImageBitmap(bgbitmap);

        /**
         *    get the feature image from temporary file.
         * */
        File sd = Environment.getExternalStorageDirectory();
        String path = sd.getPath() + "/CrazyMask/tem/";

        imgFace= BitmapFactory.decodeFile(path + "img_eyes.png");
        imgFace = mytool.getSumiao(imgFace, mean, var);
        imgFace = mytool.bigImage(imgFace);
        img_face.setImageBitmap(imgFace);

        /*
        *   listen to move the face
        * */
        img_face.setOnTouchListener(this);


        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels - 30;


        ImageButton btn_trash = (ImageButton)findViewById(R.id.img_btn_trash);
        ImageButton btn_share = (ImageButton)findViewById(R.id.img_btn_share);
        ImageButton btn_save = (ImageButton)findViewById(R.id.img_btn_save);

        btn_trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_btn_trash();
            }
        });

        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_btn_share();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_btn_save();
            }
        });

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
     *    get background image
     * */
    private Bitmap getBgFromIndex(int bgIndex) {
        File sd = Environment.getExternalStorageDirectory();
        String path = sd.getPath() + "/CrazyMask/background";
        Bitmap bm1 =BitmapFactory.decodeFile(path+"/bg_image"+bgIndex+".png");

        return bm1;
    }


    /**
     *    save the image
     * */
    private void img_btn_save() {

        int jud_left = img_face.getLeft() - img_output.getLeft();
        int jud_top = img_face.getTop() - img_output.getTop();
        if(jud_left > 0 && jud_top > 0) {

            /**
             *   get the mixed image, and save it
             * */
            imgFusion = mytool.fusionImage(imgFace, bgbitmap, jud_left, jud_top);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            String date_str = formatter.format(new java.util.Date());
            File sd = Environment.getExternalStorageDirectory();
            String path = sd.getPath() + "/CrazyMask/outMask";
            mytool.saveMyBitmap(imgFusion, path,date_str);
            Toast.makeText(Output.this, "Image saved in "+path+date_str+".png",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(Output.this,"Error: please move the face into the background~",Toast.LENGTH_SHORT).show();
        }

    }

    private void img_btn_share() {
        Toast.makeText(Output.this,"waiting",Toast.LENGTH_SHORT).show();
        }

    private void img_btn_trash() {
        // dialog

        Intent intent = new Intent(Output.this,MainActivity.class);
        startActivity(intent);
    }


    public boolean onTouch(View v, MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
               // Toast.makeText(Output.this,"down",Toast.LENGTH_SHORT).show();
                lastX = (int)event.getRawX();
                lastY = (int)event.getRawY();

                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int)event.getRawX() - lastX;
                int dy = (int)event.getRawY() - lastY;

                int left = v.getLeft() + dx;
                int top = v.getTop() + dy;
                int right = v.getRight() + dx;
                int bottom = v.getBottom() + dy;

                if(left < 0)
                {
                    left = 0;
                    right = left + v.getWidth();
                }
                if(right > screenWidth)
                {
                    right = screenWidth;
                    left = right - v.getWidth();
                }
                if (top < 0) {
                    top = 0;
                    bottom = top + v.getHeight();
                }

                if (bottom > screenHeight) {
                    bottom = screenHeight;
                    top = bottom - v.getHeight();
                }
                v.layout(left,top,right,bottom);
                lastX = (int)event.getRawX();
                lastY = (int)event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

       return true;
    }


}


