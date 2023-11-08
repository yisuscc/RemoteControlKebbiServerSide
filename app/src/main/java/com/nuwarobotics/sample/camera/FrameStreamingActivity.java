package com.nuwarobotics.sample.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nuwarobotics.service.camera.sdk.CameraSDK;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

public class FrameStreamingActivity extends AppCompatActivity {
    private CameraSDK mCameraSDK;
    //private static Camera mCamera;
    private ImageView mImageFrame;
    private TextView vTextIP;
    private TextView vTextPort;
    /*
     * Available resolutions for NB2 are:
     * width=1920 height=1080
     * width=1600 height=1200
     * width=1440 height=1080
     * width=1280 height=960
     * width=1280 height=768
     * width=1280 height=720
     * width=1024 height=768
     * width=800 height=600
     * width=800 height=480
     * width=720 height=480
     * width=640 height=480
     * width=640 height=360
     * width=480 height=360
     * width=480 height=320
     * width=352 height=288
     * width=320 height=240
     * width=176 height=144
     * width=160 height=120
     */
    final int WIDTH = 1280;
    final int HEIGHT = 768;
    Integer portNumber= 4169;
    String ip = "LoremIpsum";

    private ServerSocket server;
    private Socket client;
    private InputStream input;
    private OutputStream output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraSDK = new CameraSDK(this);
        new Thread(() -> serverSocketCreation()).start();
        startStreaming();
// first we create the scoket connection


    }

    private void serverSocketCreation() {
        try {
            //server = new ServerSocket(portNumber);
            server = new ServerSocket(0);
            ip = getLocalIP(this);
            portNumber = server.getLocalPort();
            Log.d("ServerSocketCreation", "seted the names" +"ip: "+ ip+"port:"+portNumber);
            client = server.accept();
            Log.i("jesus ", "Client connected");

            input = client.getInputStream();
            output = client.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onDestroy() {
        mCameraSDK.stopCameraStreaming();

        mCameraSDK.release();
        try {
            if(input!= null)
                input.close();
            if(output!= null)
                output.close();
            if(client != null && client.isConnected())
                client.close();
            if(!server.isClosed() || server != null)
                server.close();
        } catch (IOException e) {

        }
        super.onDestroy();
    }

    private void startStreaming() {
        setContentView(R.layout.activity_sample);

        mImageFrame = findViewById(R.id.img_frame);
        vTextIP = findViewById(R.id.ipView);
        vTextPort = findViewById(R.id.portView);

        // Request the bitmap streaming.
        mCameraSDK.requestCameraStreaming(
                WIDTH,
                HEIGHT,
                (code, bitmap) -> {
                    switch (code) {
                        case CameraSDK.CODE_NORMAL:
                        case CameraSDK.CODE_NORMAL_RESIZE:
                            runOnUiThread(() -> {
                                try {

                                    /*
                                    here i should add/replace the  mImage frame
                                    to a ouput.write(bitmap)
                                     */
                                    mImageFrame.setImageBitmap(bitmap);
                                    //server != null && !server.isClosed() && !client.isConnected()
                                    if (true) {
                                        Log.d("epic", "seted the names" + "ip: " + ip + "port:" + portNumber);
                                        vTextIP.setText(ip);
                                        vTextPort.setText(portNumber.toString());
                                    }
                                    if (client != null && client.isConnected()) {

                                        //1 we convert the bitmap to a byte array
                                        byte[] btmp = bitmapToByteArrayConversor(bitmap);
                                        //TODO ver como puedo averiguar y enviar  el tamaño del array
                                        output.write(btmp);
                                        output.flush();
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            break;
                        case CameraSDK.CODE_TOO_MANY_CLIENTS:
                            // over 3 clients using currently.
                        case CameraSDK.CODE_ILLEGAL_RESOLUTION:
                            // assigned resolution is illegal for now.
                    }
                });
    }


        public String getLocalIP(Context context){
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            assert wifiManager != null;
            WifiInfo info = wifiManager.getConnectionInfo();
            int ipAddress = info.getIpAddress();
            return String.format(Locale.ENGLISH, "%1$d.%2$d.%3$d.%4$d"
                    , ipAddress & 0xff
                    , ipAddress >> 8 & 0xff
                    , ipAddress >> 16 & 0xff
                    , ipAddress >> 24 & 0xff);
    }

    private void receiveCommand() {
        //TODO afterwecheckedthatthesendingofthestreamingserviceworkscorrectly
        // my space bar didnt work correctly

    }

    private static byte[] bitmapToByteArrayConversor(Bitmap bm) {
        ByteArrayOutputStream strm = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, strm);
        byte[] byteArray = strm.toByteArray();
        return byteArray;
    }
}