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

import com.nuwarobotics.service.IClientId;
import com.nuwarobotics.service.agent.NuwaRobotAPI;
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
        mCameraSDK = new CameraSDK(this);
        setContentView(R.layout.activity_sample);

        mImageFrame = findViewById(R.id.img_frame);
        vTextIP = findViewById(R.id.ipView);
        vTextPort = findViewById(R.id.portView);
        new Thread(() -> serverSocketCreation()).start();

        startStreaming();


    }

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
            if (!server.isClosed() && server != null)
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
                                        runOnUiThread(() -> mImageFrame.setImageBitmap(bitmap));
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

    private void receiveCommand() {
        //TODO afterwecheckedthatthesendingofthestreamingserviceworkscorrectly
        // my space bar didnt work correctly
        while (true) {//TODO: Chage the while condition
            try {
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                String strng = (String)ois.readObject();
                JSONObject cmd = strng != null? new JSONObject(strng):null;

                if (cmd != null) {
                    interpretCommand(cmd);
                }
            } catch (IOException | JSONException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private void interpretCommand(JSONObject command) {
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
                        switch (accion) {
                            case "backward":
                                break;
                            case "frontward":
                                break;
                            case "turnLeft":
                                break;
                            case "turnRight":
                                break;

                        }
                        break;
                }
            }
        }


    }

}