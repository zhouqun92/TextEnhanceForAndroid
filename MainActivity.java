
package com.example.administrator.myapplication;

import android.app.Activity;
import android.os.Bundle;
/*
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
*/



        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.net.Uri;

        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;

        import org.opencv.android.OpenCVLoader;
        import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
        import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


        import java.io.InputStream;

public class MainActivity extends Activity {

    private double max_size = 1024;
    private int PICK_IMAGE_REQUEST = 1;
    private ImageView myImageView;
    private Bitmap selectbp;
    private Bitmap selectbptmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        staticLoadCVLibraries();
        myImageView = (ImageView)findViewById(R.id.imageView);
        myImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Button selectImageBtn = (Button)findViewById(R.id.select_btn);
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // makeText(MainActivity.this.getApplicationContext(), "start to browser image", Toast.LENGTH_SHORT).show();
                selectImage();
            }
        });

        Button processBtn = (Button)findViewById(R.id.enhance1_btn);
        processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // makeText(MainActivity.this.getApplicationContext(), "hello, image process", Toast.LENGTH_SHORT).show();
                ReduceBackGround(false);
            }
        });

        Button cannyBtn = (Button)findViewById(R.id.enhance2_btn);
        cannyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // makeText(MainActivity.this.getApplicationContext(), "hello, image process", Toast.LENGTH_SHORT).show();
               // kayCanny();
                AddBackShadow();
            }
        });

        Button srcBtn = (Button)findViewById(R.id.src_btn);
        srcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // makeText(MainActivity.this.getApplicationContext(), "hello, image process", Toast.LENGTH_SHORT).show();
                kaySrc();
            }
        });

        Button hsvBtn = (Button)findViewById(R.id.enhance3_btn);
        hsvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // makeText(MainActivity.this.getApplicationContext(), "hello, image process", Toast.LENGTH_SHORT).show();
                ReduceBackGround(true);
            }
        });
    }

    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i("CV", "Open CV Libraries loaded...");
        }
    }
    private void ReduceBackGround(boolean blurflag) {
        Mat src = new Mat();
        Utils.bitmapToMat(selectbp, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        src.convertTo(src, CvType.CV_32FC1, 1.0 / 255);
        Mat dst3=ReduceBackGroundAlgorithm(src);
        if (blurflag) {
            Imgproc.GaussianBlur(dst3, dst3, new Size(3, 3), 0, 0, 4);
        }
        dst3= ColorGradation(dst3);
       // dst3=QuXian(dst3);
        //Imgproc.adaptiveThreshold(dst3,dst3,255,0,0,31,10);
        Utils.matToBitmap(dst3, selectbptmp);
        myImageView.setImageBitmap(selectbptmp);
    }
    private Mat ImageSharp(Mat src,int nAmount)
    {
        Mat dst= new Mat();
        double sigma = 3;
       // int threshold = 1;
        float amount = nAmount / 100.0f;
        Mat imgBlurred=new Mat();
        Imgproc.GaussianBlur(src, imgBlurred, new Size(7,7), sigma, sigma,4);
        Mat temp_sub= new Mat();
        //Mat temp_abs= new Mat();
        Core.subtract(src,imgBlurred,temp_sub);
       // Core.convertScaleAbs(temp_sub,temp_abs);
       // Mat lowContrastMask = new Mat();
        //Imgproc.threshold(temp_abs,lowContrastMask,threshold,255,1);
        //Mat temp_gen= new Mat();
        Core.addWeighted(src,1,temp_sub,amount,0,dst);
       // dst = src+temp_sub*amount;
        //src.copyTo(dst, lowContrastMask);
        return dst;
    }
    private Mat ReduceBackGroundAlgorithm(Mat src) {
        Mat gauss = new Mat();
        Mat dst2 = new Mat();
        Mat dst3 = new Mat();
        //Imgproc.GaussianBlur(src, gauss, new Size(31,31), 0,0,4);
        Imgproc.blur(src, gauss, new Size(101,101));
        Core.divide(src,gauss,dst2);
        dst2=ImageSharp(dst2, 101);
        //Imgproc.GaussianBlur(dst2, dst2, new Size(3,3), 0,0,4);//
        dst2.convertTo(dst3, CvType.CV_8UC1,255);
        return dst3;
    }
    private void AddBackShadow() {
        Mat src = new Mat();
        Utils.bitmapToMat(selectbp, src);
        Mat src_gray=new Mat();
        if (src.channels() == 3)
            Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
        else if(src.channels() == 4)
            Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGRA2GRAY);
        else
            src.copyTo(src_gray);
        src_gray.convertTo(src_gray, CvType.CV_32FC1, 1.0 / 255);
        Mat src_reduback=ReduceBackGroundAlgorithm(src_gray);
        Mat src_gauss = new Mat();
        Imgproc.GaussianBlur(src_gray, src_gauss, new Size(3,3), 0, 0,4);
        src_gauss.convertTo(src_gauss, CvType.CV_8UC1,255);
        Mat dst=new Mat();
        Mat bw=new Mat();
        int kernel_size = 3;
        int scale = 1;
        int delta = 0;
        //Imgproc.threshold(src_gauss, bw, 130, 255,1);
        Imgproc.adaptiveThreshold(src_gauss,bw,255,0,1,31,1);
        Mat abs_dst=new Mat();
        Mat out=new Mat();
        Imgproc.Laplacian(src_reduback, dst, 5, kernel_size, scale, delta);
       Core.convertScaleAbs(dst, abs_dst);
        Imgproc. threshold(abs_dst, out, 30, 255, 0);
        Mat kernel = Imgproc. getStructuringElement(0, new Size(7, 7), new Point(-1,-1));
       Imgproc. morphologyEx(out, out, 1, kernel);
        Mat shadow = new Mat();
        Core.bitwise_and(bw,out,shadow);
        Mat out_shadow = new Mat();
        Core.subtract(src_reduback,shadow,out_shadow);
        Utils.matToBitmap(out_shadow, selectbptmp);
        myImageView.setImageBitmap(selectbptmp);
       // Utils.matToBitmap(shadow, selectbptmp);
       // myImageView.setImageBitmap(selectbptmp);

    }
    private Mat QuXian(Mat src)
    {
        Mat dst =new Mat();
       int clight=30;int cdarck=40;
       float h= Math.round (255.0*(100-clight)/100);
       float l=Math.round (255.0*(100-cdarck)/100);

        for (int i=0;i<src.rows();i++)
        {
            for (int j=0;j<src.cols();j++)
            {
            //   double data= src.get(i,j)[0];
            //   if (data>=h)
            //       dst.put(i,j,255);
             //  else if (data<=l)
             //      dst.put(i,j,0);
              //else if (l<data&&data<h)
               //    dst.put(i,j,255-(255.0/(h-l)*(h-data)));
            }
        }

        return src;
    }
    private Mat ColorGradation(Mat src)
    {
        src.convertTo(src, CvType.CV_32FC1);
        //Imgproc.medianBlur(src,src,3);
        //Imgproc.GaussianBlur(src, src, new Size(3,3), 0, 0,4);
        int HighLight=255;
        int Shadow=120;
        //int Midtones=1;
        int Diff=HighLight-Shadow;
        Mat rDiff=new Mat();
        Core.subtract(src,new Scalar(Shadow),rDiff);
        Mat temp1=new Mat();
        rDiff.convertTo(temp1, CvType.CV_32FC1, 255.0 / Diff);
       // Core.multiply(rDiff,new Scalar(255/Diff),temp1);
        Mat dst=new Mat();
        temp1.convertTo(dst, CvType.CV_8UC1);
       // Imgproc.adaptiveThreshold(dst,dst,255,0,0,31,10);
        return dst;
    }
    private void convertGray() {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(selectbp, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        Log.i("CV", "image type:" + (temp.type() == CvType.CV_8UC3));
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(dst, selectbptmp);
        myImageView.setImageBitmap(selectbptmp);
    }

    private void kayCanny() {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat gray = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(selectbp, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        Imgproc.cvtColor(temp, gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.Canny(gray, dst, 80, 90);

        Utils.matToBitmap(dst, selectbptmp);
        myImageView.setImageBitmap(selectbptmp);
    }

    private void kaySrc() {

        myImageView.setImageBitmap(selectbp);
    }

    private void kayHsv() {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();

        Utils.bitmapToMat(selectbp, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        Log.i("CV", "image type:" + (temp.type() == CvType.CV_8UC3));
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2HSV);
        Utils.matToBitmap(dst, selectbptmp);
        myImageView.setImageBitmap(selectbptmp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Log.d("image-tag", "start to decode selected image now...");
                InputStream input = getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);
                int raw_width = options.outWidth;
                int raw_height = options.outHeight;
                int max = Math.max(raw_width, raw_height);
                int newWidth = raw_width;
                int newHeight = raw_height;
                int inSampleSize = 1;
                if(max > max_size) {
                    newWidth = raw_width / 2;
                    newHeight = raw_height / 2;
                    while((newWidth/inSampleSize) > max_size || (newHeight/inSampleSize) > max_size) {
                        inSampleSize *=2;
                    }
                }

                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                selectbp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
                selectbptmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
                myImageView.setImageBitmap(selectbptmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"选择图像..."), PICK_IMAGE_REQUEST);
    }
}
