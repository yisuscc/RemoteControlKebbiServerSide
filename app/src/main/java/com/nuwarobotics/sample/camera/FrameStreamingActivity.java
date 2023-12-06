package com.nuwarobotics.sample.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nuwarobotics.service.IClientId;
import com.nuwarobotics.service.agent.NuwaRobotAPI;
import com.nuwarobotics.service.agent.RobotEventListener;
import com.nuwarobotics.service.camera.sdk.CameraSDK;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class FrameStreamingActivity extends AppCompatActivity {
    private CameraSDK mCameraSDK;
    //private static Camera mCamera;
    private ImageView mImageFrame;
    private TextView vTextIP;
    private TextView vTextPort;
    private NuwaRobotAPI mRobot;
    private final Handler mHandler = new Handler();

    final int WIDTH = 1280;
    final int HEIGHT = 768;
    Integer portNumber = 4169;
    String ip = "LoremIpsum";

    private ServerSocket server;
    private Socket client;
    private InputStream input;
    private OutputStream output;
    private AtomicBoolean streamingFlag = new AtomicBoolean(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String your_app_package_name = getPackageName();
        IClientId id = new IClientId(your_app_package_name);
        mRobot = new NuwaRobotAPI(this, id);
        mRobot.registerRobotEventListener(robotEventListener);
        mCameraSDK = new CameraSDK(this);
        setContentView(R.layout.activity_sample);

        mImageFrame = findViewById(R.id.img_frame);
        vTextIP = findViewById(R.id.ipView);
        vTextPort = findViewById(R.id.portView);
        new Thread(() -> {
            serverSocketCreation();
            receiveCommand();
        }).start();

        startStreaming();
    }
    private final RobotEventListener robotEventListener= new RobotEventListener() {
        @Override
        public void onWikiServiceStart() {
            mRobot.requestSensor(NuwaRobotAPI.SENSOR_TOUCH);
        }

        @Override
        public void onWikiServiceStop() {

        }

        @Override
        public void onWikiServiceCrash() {

        }

        @Override
        public void onWikiServiceRecovery() {

        }

        @Override
        public void onWikiServiceError(int i) {

        }

        @Override
        public void onStartOfMotionPlay(String s) {

        }

        @Override
        public void onPauseOfMotionPlay(String s) {

        }

        @Override
        public void onStopOfMotionPlay(String s) {

        }

        @Override
        public void onCompleteOfMotionPlay(String s) {

        }

        @Override
        public void onPlayBackOfMotionPlay(String s) {

        }

        @Override
        public void onErrorOfMotionPlay(int i) {

        }

        @Override
        public void onPrepareMotion(boolean b, String s, float v) {

        }

        @Override
        public void onCameraOfMotionPlay(String s) {

        }

        @Override
        public void onGetCameraPose(float v, float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8, float v9, float v10, float v11) {

        }

        @Override
        public void onTouchEvent(int position, int i1) {
        Log.i("jesus", "i="+position+"i1="+i1);
        if(position== 4){
            mRobot.showFace();
        }else if (position==3){
            mRobot.hideFace();
        }
        }

        @Override
        public void onPIREvent(int i) {

        }

        @Override
        public void onTap(int i) {

        }

        @Override
        public void onLongPress(int i) {

        }

        @Override
        public void onWindowSurfaceReady() {

        }

        @Override
        public void onWindowSurfaceDestroy() {

        }

        @Override
        public void onTouchEyes(int i, int i1) {

        }

        @Override
        public void onRawTouch(int i, int i1, int i2) {

        }

        @Override
        public void onFaceSpeaker(float v) {

        }

        @Override
        public void onActionEvent(int i, int i1) {

        }

        @Override
        public void onDropSensorEvent(int i) {

        }

        @Override
        public void onMotorErrorEvent(int i, int i1) {

        }
    };

    private void serverSocketCreation() {
        try {
            //server = new ServerSocket(portNumber);
            if (server != null && !server.isClosed()) {
                server.close();
            }
            server = new ServerSocket(0);
            ip = getLocalIP(this);
            portNumber = server.getLocalPort();
            //  Log.d("ServerSocketCreation", "seted the names" + "ip: " + ip + "port:" + portNumber);
            client = server.accept();
            mRobot.showFace();
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
        mRobot.release();

        mCameraSDK.release();
        try {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
            if (client != null && client.isConnected())
                client.close();
            if ( server != null && !server.isClosed() )
                server.close();
        } catch (IOException e) {

        }
        super.onDestroy();
    }

    private void startStreaming() { // thread instead of void
        mCameraSDK
                .requestCameraStreaming(
                        WIDTH,
                        HEIGHT,
                        (code, bitmap) -> {
                            switch (code) {
                                case CameraSDK.CODE_NORMAL:
                                case CameraSDK.CODE_NORMAL_RESIZE:

                                    vTextIP.setText(ip);
                                    vTextPort.setText(portNumber.toString());
                                    if (null != client && client.isConnected() && streamingFlag.get()) {
                                        if(!streamingFlag.get())
                                            break;
                                       // runOnUiThread(() -> mImageFrame.setImageBitmap(bitmap));
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                                        try {
                                            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                                            oos.writeObject(stream.toByteArray());
                                            oos.flush();
                                            Log.d("jesus", "wrote the bitmat to the socket");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Log.d("jesus", " couldnt write the bitmat to the socket");
                                        }

                                    }

                                    break;
                                case CameraSDK.CODE_TOO_MANY_CLIENTS:
                                    // over 3 clients using currently.
                                case CameraSDK.CODE_ILLEGAL_RESOLUTION:
                                    // assigned resolution is illegal for now.
                            }
                        });

    }


    public String getLocalIP(Context context) {
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

    private final Runnable stopMovingRunnable = new Runnable() {
        @Override
        public void run() {
            if (null != mRobot) {
                mRobot.move(0);
                mRobot.turn(0);
            }
        }
    };

    private void receiveCommand() {
        while (!server.isClosed() && client.isConnected() && !client.isClosed()) {
            try {
                InputStream is = client.getInputStream();
                byte[]bytes=   new byte [1024];
                is.read(bytes);
                String string = new String(bytes);
                JSONObject cmd = string != null? new JSONObject(string):null;

                if (cmd != null) {
                    interpretCommand(cmd);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void interpretCommand(JSONObject command) {
        Log.i("Jesus", "interpretCommand " + command.toString());
        if (command != null) {
            String propiedad = null;
            String accion = null;
            try {
                propiedad = command.getString("property");
                accion = command.getString("action");


            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (propiedad != null && accion != null) {

                switch (propiedad) {
                    case "general":
                        switch (accion) {
                            case "disconnect":
                                streamingFlag.set(false);
                                if (client.isConnected()) {
                                    try {

                                        client.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                // we restart the server socket

                                new Thread(() -> {
                                    serverSocketCreation();
                                    receiveCommand();
                                }).start();

                                break;

                        }
                        break;
                    case "streaming":
                        switch (accion) {
                            case "start":
                                if (!streamingFlag.get()) {
                                    // I think i should use an atominc boolean as a flag to interrupt inside the thread
                                    streamingFlag.set(true);
                                    startStreaming();
                                }
                                break;
                            case "stop":
                                streamingFlag.set(false);
                                break;
                        }
                        break;
                    case "moving":
                        mHandler.removeCallbacks(stopMovingRunnable);
                        switch (accion) {
                            case "backward":
                                mRobot.move(-0.3f);
                                break;
                            case "frontward":
                                mRobot.move(0.3f);
                                break;
                            case "turnLeft":
                                mRobot.turn(90.0f);
                                break;
                            case "turnRight":
                                mRobot.turn(-90.0f);
                                break;
                        }
                        mHandler.postDelayed(stopMovingRunnable, 400);
                        break;
                }
            }
        }


    }

}