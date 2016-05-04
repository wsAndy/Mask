package com.example.com.testmix1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created by 938310808@qq.com on 2016/3/27.
 */
public class tool  {

    public  void tool ()
    {}

    private int mean_int;
    private float var_float;
    Bitmap oriBm;


    public Bitmap getSumiao(Bitmap bm, int mean_int_, float var_float_)
    {

        mean_int = mean_int_;
        var_float=var_float_;
        oriBm = bm;

        int[] pixels = rgb2gray(oriBm);
        int[] copixels = reverseColor(pixels);
        gaussBlur(copixels, oriBm.getWidth(), oriBm.getHeight(), mean_int, var_float);
        colorDodge(pixels, copixels);

        Bitmap outbm = Bitmap.createBitmap(pixels, oriBm.getWidth(), oriBm.getHeight(),
                Bitmap.Config.RGB_565);

        outbm = getTransparentMap(outbm);

        return outbm;
    }
    private Bitmap getTransparentMap(Bitmap bmWithdark)
    {
        int[] pixel = new int[bmWithdark.getWidth()*bmWithdark.getHeight()];

        bmWithdark.getPixels(pixel, 0, bmWithdark.getWidth(), 0, 0, bmWithdark.getWidth(),
                bmWithdark.getHeight());

        for(int i = 0; i<pixel.length; ++i)
        {
            int cir = pixel[i];
            int red = (cir & 0x00ff0000)>>16;
            int green = (cir & 0x0000ff00) >> 8;
            int blue = (cir & 0x000000ff);
            if(red < 10 && green < 10 && blue < 10)
            {
                pixel[i] = (0<<24) | (pixel[i]&0x00FFFFFF);
            }
        }

        bmWithdark = Bitmap.createBitmap(pixel, bmWithdark.getWidth(),
                bmWithdark.getHeight(), Bitmap.Config.ARGB_8888);

        return bmWithdark;
    }

    private int[] rgb2gray(Bitmap bm)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int [] pixels = new int[width*height];
        bm.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int index = i * width + j;
                int color = pixels[index];
                int r = (color & 0x00ff0000) >> 16;
                int g = (color & 0x0000ff00) >> 8;
                int b = (color & 0x000000ff);
                int grey = (int) (0.3*r  + 0.58*g + 0.115*b );
                pixels[index] = grey << 16 | grey << 8 | grey | 0xff000000;
            }
        }

        return pixels;
    }

    private static int[] reverseColor(int[] pixels) {

        int length = pixels.length;
        int[] result = new int[length];
        for (int i = 0; i < length; ++i) {
            int color = pixels[i];
            int b = 255 - (color & 0x000000ff);
            result[i] = b << 16 | b << 8 | b | 0xff000000;
        }
        return result;
    }

    private static void gaussBlur(int[] data, int width, int height, int radius,
                                  float sigma) {

        float pa = (float) (1 / (Math.sqrt(2 * Math.PI) * sigma));
        float pb = -1.0f / (2 * sigma * sigma);

        // generate the Gauss Matrix
        float[] gaussMatrix = new float[radius * 2 + 1];
        float gaussSum = 0f;
        for (int i = 0, x = -radius; x <= radius; ++x, ++i) {
            float g = (float) (pa * Math.exp(pb * x * x));
            gaussMatrix[i] = g;
            gaussSum += g;
        }

        for (int i = 0, length = gaussMatrix.length; i < length; ++i) {
            gaussMatrix[i] /= gaussSum;
        }

        // x direction
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                float r = 0, g = 0, b = 0;
                gaussSum = 0;
                for (int j = -radius; j <= radius; ++j) {
                    int k = x + j;
                    if (k >= 0 && k < width) {
                        int index = y * width + k;
                        int color = data[index];
                        int cr = (color & 0x00ff0000) >> 16;
                        int cg = (color & 0x0000ff00) >> 8;
                        int cb = (color & 0x000000ff);

                        r += cr * gaussMatrix[j + radius];
                        g += cg * gaussMatrix[j + radius];
                        b += cb * gaussMatrix[j + radius];

                        gaussSum += gaussMatrix[j + radius];
                    }
                }

                int index = y * width + x;
                int cr = (int) (r / gaussSum);
                int cg = (int) (g / gaussSum);
                int cb = (int) (b / gaussSum);

                data[index] = cr << 16 | cg << 8 | cb | 0xff000000;
            }
        }

        // y direction
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                float r = 0, g = 0, b = 0;
                gaussSum = 0;
                for (int j = -radius; j <= radius; ++j) {
                    int k = y + j;
                    if (k >= 0 && k < height) {
                        int index = k * width + x;
                        int color = data[index];
                        int cr = (color & 0x00ff0000) >> 16;
                        int cg = (color & 0x0000ff00) >> 8;
                        int cb = (color & 0x000000ff);

                        r += cr * gaussMatrix[j + radius];
                        g += cg * gaussMatrix[j + radius];
                        b += cb * gaussMatrix[j + radius];

                        gaussSum += gaussMatrix[j + radius];
                    }
                }

                int index = y * width + x;
                int cr = (int) (r / gaussSum);
                int cg = (int) (g / gaussSum);
                int cb = (int) (b / gaussSum);
                data[index] = cr << 16 | cg << 8 | cb | 0xff000000;
            }
        }
    }

    private static void colorDodge(int[] baseColor, int[] mixColor) {

        for (int i = 0, length = baseColor.length; i < length; ++i) {
            int bColor = baseColor[i];
            int br = (bColor & 0x00ff0000) >> 16;
            int bg = (bColor & 0x0000ff00) >> 8;
            int bb = (bColor & 0x000000ff);

            int mColor = mixColor[i];
            int mr = (mColor & 0x00ff0000) >> 16;
            int mg = (mColor & 0x0000ff00) >> 8;
            int mb = (mColor & 0x000000ff);

            int nr = colorDodgeFormular(br, mr);
            int ng = colorDodgeFormular(bg, mg);
            int nb = colorDodgeFormular(bb, mb);

            baseColor[i] = nr << 16 | ng << 8 | nb | 0xff000000;
        }

    }

    private static int colorDodgeFormular(int base, int mix) {

        int result = base + (base * mix) / (255 - mix);
        result = result > 255 ? 255 : result;
        return result;

    }



    public void saveMyBitmap(Bitmap mBitmap, String path, String date_str){

        File file = new File(path);
        if(!file.exists())
        {
            file.mkdir();
        }

        File f = new File(path+"/"+ date_str + ".png");

        try {
            f.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        mBitmap.compress(Bitmap.CompressFormat.JPEG,  Integer.parseInt(number), fOut);
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public Bitmap getClipImage(Bitmap bm1, RectF faceRecf)
    {
        int bitWidth = bm1.getWidth();
        int bitHeigh = bm1.getHeight();
        // bm2是建立的空白的图层
        Bitmap bm2 = Bitmap.createBitmap(bitWidth, bitHeigh, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bm2); // bm2上绘制，由于canvas是对bm2操作，所以在下面drawBitmap时，把bm1放入了bm1中
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE.STROKE);

        RectF faceRect = faceRecf;

        float [] faceCornerii = new float[]{
                40, 45, 40, 45,30,80,30,80
        };
//        float [] faceCornerii = new float[]{
//                4, 4, 4, 4,3,8,3,8
//        };

        Path clip = new Path();
        clip.reset();
        clip.addRoundRect(faceRect, faceCornerii, Path.Direction.CW);
//test to judge if the real problem is in faceCornerii
 //       clip.addRect(faceRect,Path.Direction.CW);


        canvas.save();
        canvas.clipPath(clip, Region.Op.INTERSECT);
        canvas.drawPath(clip, mPaint);
        canvas.restore();


        // 范围的滤波，使图片顺滑
        Rect clipfaceRect = new Rect(
                (int)faceRect.left,
                (int)faceRect.top,
                (int)faceRect.right,
                (int)faceRect.bottom);
        PaintFlagsDrawFilter dfd = new PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG,Paint.FILTER_BITMAP_FLAG);
        canvas.setDrawFilter(dfd);

        canvas.clipPath(clip);
        canvas.drawBitmap(bm1,clipfaceRect,faceRect,mPaint);

        //加上这一句就ok了,直接再取一个图层来返回
        Bitmap outbm = Bitmap.createBitmap(bm2, (int)(faceRect.left),
                (int)(faceRect.top),
                (int)(faceRect.right-faceRect.left) ,
                (int)(faceRect.bottom-faceRect.top) );

        bm1.recycle();
        bm1 = null;
        bm2.recycle();
        bm2 = null;

        return outbm;

    }


    public Bitmap fusionImage(Bitmap faceBp, Bitmap bg)
    {

        float sizeWidth = bg.getWidth()/faceBp.getWidth();
        float sizeHeigh =  bg.getHeight()/faceBp.getHeight();

        Matrix matrix = new Matrix();

        if(sizeWidth > 1 || sizeHeigh > 1)
        {
            matrix.postScale((float)(0.6*sizeWidth),(float)(0.6*sizeHeigh));
        }
        else {
            matrix.postScale(1 / sizeWidth, 1 / sizeHeigh); // <1 的缩小
        }

        Bitmap resizeFace = Bitmap.createBitmap(faceBp,0,0,faceBp.getWidth(),faceBp.getHeight()
                ,matrix,true);

        Bitmap outFace = Bitmap.createBitmap(bg.getWidth(),bg.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outFace);
        canvas.drawBitmap(bg,0,0,null);
        canvas.drawBitmap(resizeFace,(bg.getWidth()-resizeFace.getWidth())/2,
                (bg.getHeight()-resizeFace.getHeight())/2,null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        faceBp.recycle();
        faceBp = null;
        bg.recycle();
        bg =null;
        return outFace;
    }

    public  Bitmap compressImage(Bitmap bitmap)
    {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int scale = 10;
//        if(width > height)
//        {
//            scale = 200/width;
//        }
//        else{
//            scale = 200/height;
//        }

        bitmap = ThumbnailUtils.extractThumbnail(bitmap,128, 128);
        return bitmap;
    }

    public Bitmap bigImage(Bitmap bitmap)
    {
        double width = bitmap.getWidth();
        double height = bitmap.getHeight();
        double scale = 512/width;

        bitmap = ThumbnailUtils.extractThumbnail(bitmap,(int)(scale*width),(int)(scale*height));
        return bitmap;
    }


    }
