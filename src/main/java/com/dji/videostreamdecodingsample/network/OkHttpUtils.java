package com.dji.videostreamdecodingsample.network;


import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class OkHttpUtils {

    static {

        mInstance = new OkHttpUtils();
    }

    private static final String TAG = "OkHttpUtils";

    private static final String ACTION = "/detect";
    private static final String PORT = "24380";

    private static final MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/jpeg");

    private static OkHttpUtils mInstance;

    public static OkHttpUtils getInstance(){

        return  mInstance;

    }


    public String uploadImage(String ip, File imgFile) throws Exception{

        String url = "http://" + ip + ":" + PORT;

        OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
        client_builder.cookieJar(new MyCookieJar());
        OkHttpClient client_a = client_builder.build();
        //OkHttpClient client_a = new OkHttpClient();
        Request get_a = new Request.Builder()
                .get()
                .url(url)
                .build();

        Response response_a = client_a.newCall(get_a).execute();
        Document parse_a = Jsoup.parse(response_a.body().string());
        Elements table_a = parse_a.select("input[type=hidden]");
        Element ele_a = table_a.get(0);
        String token_a = ele_a.attr("value");
        Log.i(TAG,token_a);


        RequestBody req_b_body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("csrf_token",token_a)
                .addFormDataPart("photo", imgFile.getName(), RequestBody.create(MEDIA_TYPE_IMAGE, imgFile))
                .build();

        //Buffer req_b_buffer = new Buffer();
        //req_b_body.writeTo(req_b_buffer);
        //Log.i("!!!!!",req_b_buffer.readUtf8());



        Request req_b = new Request.Builder()
                //.addHeader("Connection","close")
                .url(url)
                .post(req_b_body)
                .build();

        //Log.i("111111","00");
        //Log.i("???",req_b.headers().toString());
        client_a.newCall(req_b).execute();
        /*client_a.newCall(req_b).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("infooooo",response.toString());
            }
        });*/

        return req_b.toString();

    }


    public String detectImage(String ip, File imgFile, String dtcType) throws IOException{

        OkHttpClient client_b = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        //OkHttpClient client_b = new OkHttpClient();
        String url = String.format("http://%s:%s%s?nm=%s&dtcType=%s",ip, PORT, ACTION, imgFile.getName(), dtcType);
        //Log.i(TAG, url);
        Request req_b = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response_b = client_b.newCall(req_b).execute();

        //Log.i(TAG,response_b.body().string());
        return response_b.body().string();
    }

}