package com.example.com.testmix1;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;

public class MainActivity extends ActionBarActivity {

    ImageButton btn_cam;
    ImageButton btn_img;
    tool mytool;
    static boolean hasSaved = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_cam = (ImageButton)findViewById(R.id.btn_cam);
        btn_img = (ImageButton)findViewById(R.id.btn_img);
        mytool = new tool();


        btn_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btn_cam_start();
            }
        });

        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_img_start();
            }
        });

        // for test
        if(!hasSaved) {
            hasSaved = true;

            createDoc();

            saveBackground();
        }



    }

    // just a test
    private void saveBackground() {
        Bitmap []bm1 = new Bitmap[11];
        for(int i = 0; i<11; ++i)
        {
            bm1[i] =  ((BitmapDrawable)getResources().getDrawable(getbgIndex(i))).getBitmap();
        }
//        BitmapDrawable bmd = (BitmapDrawable)getResources().getDrawable(R.drawable.bg_image1);
//        Bitmap bm1 = bmd.getBitmap();

        File sd = Environment.getExternalStorageDirectory();
        String path = sd.getPath() + "/CrazyMask/background";

        for(int i = 0; i<11;++i) {
            mytool.saveMyBitmap(bm1[i], path, "bg_image"+i);
        }

    }

    private int getbgIndex(int bgIndex) {

        switch (bgIndex){
            case 1:
                return R.drawable.bg_image1;
            case 2:
                return R.drawable.bg_image2;

            case 3:
                return R.drawable.bg_image3;

            case 4:
                return  R.drawable.bg_image4;

            case 5:
                return R.drawable.bg_image5;

            case 6:
                return R.drawable.bg_image6;

            case 7:
                return R.drawable.bg_image7;

            case 8:
                return R.drawable.bg_image8;
            case 9:
                return R.drawable.bg_image9;
            case 10:
                return R.drawable.bg_image10;
            case 11:
                return R.drawable.bg_image11;
            default:
                return R.drawable.bg_image1;
        }

    }

    private void createDoc() {
        File sd = Environment.getExternalStorageDirectory();
        String path = sd.getPath() + "/CrazyMask";
        String path_base = path;

        File file = new File(path);
        if(!file.exists())
        {
            file.mkdir();
        }

        path = path_base + "/background";
        File file2 = new File(path);
        if(!file2.exists())
        {
            file2.mkdir();
        }

        path = path_base + "/tem";
        File file3 = new File(path);
        if(!file3.exists())
        {
            file3.mkdir();
        }

    }

    private void toMixer(String path)
    {
        Intent intent = new Intent(MainActivity.this, Mixer.class);
        intent.putExtra("imagePath",path);
        startActivity(intent);
    }

    private void btn_cam_start()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);// parameter two is a index
    }

    private void btn_img_start()
    {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 2);// parameter two is a index
    }

    protected void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // camera
        if(resultCode== Activity.RESULT_OK && requestCode == 1)
        {
            String sdStatus = Environment.getExternalStorageState();
            if(!sdStatus.equals(Environment.MEDIA_MOUNTED))
            {
                return ;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            String date_str = formatter.format(new java.util.Date());
//            Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap)bundle.get("data");

            File sd = Environment.getExternalStorageDirectory();
            String path = sd.getPath() + "/CrazyMask/inputFace";

            mytool.saveMyBitmap(bitmap, path,date_str);

            String imagePath = path+"/" + date_str+".png";
            toMixer(imagePath);

        }


        if (requestCode == 2 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            toMixer(picturePath);
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }



}
