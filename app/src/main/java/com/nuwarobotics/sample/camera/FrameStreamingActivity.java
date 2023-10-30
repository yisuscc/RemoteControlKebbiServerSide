package com.nuwarobotics.sample.camera;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.nuwarobotics.service.camera.sdk.CameraSDK;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FrameStreamingActivity extends AppCompatActivity {
    private CameraSDK mCameraSDK;
    //private static Camera mCamera;
    private ImageView mImageFrame;

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
    final int portNumber = 49169;
    private ServerSocket server; // TODO Server socket o websocket server
    private Socket client;// same with the server
    private InputStream input;
    private OutputStream output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// first we create the scoket connection
        try {
            server = new ServerSocket(portNumber);
            new Thread(() -> {
                try { // shoul we put it inside a while loop
                    /*
                    while(clientisnotconnected)
                    {
                    try to connect
                    }
                     */

                    //Socket client = server.accept();
                    client = server.accept();
                    //TODO a handle client socket
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCameraSDK = new CameraSDK(this);
        sendStreaming();
    }

    private void handleClientSocket(Socket client) {
        try {
            // first we create  the inputs and output of the socket
            //what should i use java.io or java websocket
            // the java websocket seem apprpiate  since is a real time app
            //Since most of the documentatio is with normal sockets i am goint to go with those


         /*   InputStream input = client.getInputStream();
            OutputStream ouput = client.getOutputStream();*/
            input = client.getInputStream();
            output = client.getOutputStream();
            sendStreaming();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        mCameraSDK.stopCameraStreaming();

        mCameraSDK.release();
        try {
            server.close();
        } catch (IOException e) {

        }
        super.onDestroy();
    }

    private void sendStreaming() {
        setContentView(R.layout.activity_sample);

        mImageFrame = findViewById(R.id.img_frame);

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
                                    while(client.isConnected()){
                                        mImageFrame.setImageBitmap(bitmap);
                                        //1 we convert the bitmap to a byte array

                                        output.write(bitmapToByteArrayConversor(bitmap));
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
    private void receiveCommand(){
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