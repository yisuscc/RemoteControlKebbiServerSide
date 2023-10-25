package com.nuwarobotics.sample.camera;

import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.nuwarobotics.service.camera.sdk.CameraSDK;

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
	final int WIDTH = 640;
	final int HEIGHT = 480;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		mCameraSDK = new CameraSDK(this);
	}

	@Override
	protected void onDestroy() {
		mCameraSDK.stopCameraStreaming();

		mCameraSDK.release();
		super.onDestroy();
	}

	private void initView() {
		setContentView(R.layout.activity_sample);

		mImageFrame = findViewById(R.id.img_frame);

		// Request a single bitmap.
		findViewById(R.id.btn_take_a_picture)
				.setOnClickListener((v) -> mCameraSDK
						.requestCameraFrame((code, bitmap) -> {
							switch (code) {
								case CameraSDK.CODE_NORMAL:
								case CameraSDK.CODE_NORMAL_RESIZE:
									runOnUiThread(() -> {
										try {
											mImageFrame.setImageBitmap(bitmap);
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
						}));

		// Request the bitmap streaming.
		findViewById(R.id.btn_start_streaming)
				.setOnClickListener((v) -> mCameraSDK
						.requestCameraStreaming(
								WIDTH,
								HEIGHT,
								(code, bitmap) -> {
									switch (code) {
										case CameraSDK.CODE_NORMAL:
										case CameraSDK.CODE_NORMAL_RESIZE:
											runOnUiThread(() -> {
												try {
													mImageFrame.setImageBitmap(bitmap);
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
								}));

		// Stop streaming
		findViewById(R.id.btn_stop_streaming).setOnClickListener((v) -> mCameraSDK.stopCameraStreaming());

		// Lock all applications to use camera service.
		findViewById(R.id.btn_lock).setOnClickListener((v) -> {
			if (mCameraSDK.pauseCameraService()) {
				Log.i(FrameStreamingActivity.class.getSimpleName(), "paused camera service");
			} else {
				Log.e(FrameStreamingActivity.class.getSimpleName(), "pause camera service failed");
			}
		});

		// Important! If the lockCamera is invoked, must invoke unlockCamera to recover.
		findViewById(R.id.btn_unlock).setOnClickListener((v) -> mCameraSDK.resumeCameraService());

		findViewById(R.id.btn_exit).setOnClickListener((v) -> finish());
	}
}