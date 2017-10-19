package com.dji.videostreamdecodingsample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.graphics.YuvImage;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dji.videostreamdecodingsample.network.OkHttpUtils;
import com.squareup.picasso.Picasso;

/**
 * Created by Shaoqing on 2017/10/9.
 */

/** Activity that shows all the UI elements together */
public class WidgetActivity extends Activity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView total_people;
    private ImageView dendity_map;
    private TextureView fPVWidget;
    private Button single_count;
    private Button cont_count;
    private boolean cont_state = false;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            saveimage(fPVWidget.getBitmap());
            handler.postDelayed(this,20000);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widgets);
        initUi();
    }


    private void initUi(){
        findViewById(R.id.single_count).setOnClickListener(this);
        findViewById(R.id.cont_count).setOnClickListener(this);
        total_people  = (TextView) findViewById(R.id.number);
        fPVWidget = (TextureView)findViewById(R.id.fpvwidget);
        dendity_map = (ImageView) findViewById(R.id.density_map);
        dendity_map.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Hide both the navigation bar and the status bar.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.single_count){
            Toast.makeText(getApplicationContext(), "Single mode, please wait!", Toast.LENGTH_SHORT).show();
            //dendity_map.setImageBitmap(fPVWidget.getBitmap());
            saveimage(fPVWidget.getBitmap());
        }else{
            if(id == R.id.cont_count){
                take_cont();
            }
        }
    }

    private void take_cont(){
        if(cont_state == false){
            cont_state = true;
            Toast.makeText(getApplicationContext(), "Continuous mode starting!", Toast.LENGTH_SHORT).show();
            handler.post(runnable);
            //handler.postDelayed(runnable, 20000);

        }else{
            cont_state = false;
            Toast.makeText(getApplicationContext(), "Continuous mode terminating!", Toast.LENGTH_SHORT).show();
            handler.removeCallbacks(runnable);
        }
    }

    private void saveimage(Bitmap bit_image) {
        File dir = new File(Environment.getExternalStorageDirectory() + "/DJI_ScreenShot");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }

        OutputStream outputFile;
        final File imgFile = new File(dir + "/ScreenShot_" + System.currentTimeMillis() + ".jpg");
        try {
            outputFile = new FileOutputStream(imgFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "save image: new bitmap output file error: " + e);
            return;
        }
        if (outputFile != null) {
            bit_image.compress(Bitmap.CompressFormat.JPEG,100,outputFile);
            caculate(imgFile);
        }
        try {
            outputFile.close();
        } catch (IOException e) {
            Log.e(TAG, "test screenShot: compress bit image error: " + e);
            e.printStackTrace();
        }

    }

    protected void caculate(final File img_path){

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    String ipStr = "scutcvlab.imwork.net";
                    Log.i(TAG, OkHttpUtils.getInstance().uploadImage(ipStr, img_path));
                    final String det_request = OkHttpUtils.getInstance().detectImage(ipStr, img_path, "B");
                    //Toast.makeText(getApplicationContext(), det_request, Toast.LENGTH_LONG).show();
                    final JSONObject det_response = JSON.parseObject(det_request).getJSONObject("result");
                    //byte[] decodedString = Base64.decode(jsonObject.getString("res_img"), Base64.DEFAULT);
                    //final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    total_people.post(new Runnable() {
                        @Override
                        public void run() {
                            total_people.setText(det_response.getInteger("crowd") + "People");
                        }
                    });
                    dendity_map.post(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.with(WidgetActivity.this)
                                    .load(det_response.getString("res_img"))
                                    .into(dendity_map);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}