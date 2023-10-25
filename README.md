# `Camera Frame And Streaming Sample`
This is a sample project that can be built and run directly.

# `Support Robot Product`
Robot Generation 2
* Kebbi Air : Taiwan、China、Japan

# `Nuwa Website`
* NuwaRobotics Website (https://www.nuwarobotics.com/)
* NuwaRobotics Developer Website (https://dss.nuwarobotics.com/)
* Nuwa SDK JavaDoc (https://developer-docs.nuwarobotics.com/sdk/javadoc/reference/packages.html)

# `Start to Use`

* Please get NuwaSDK aar from [developer website](https://dss.nuwarobotics.com/).
* Make sure the Camera Service version is later than 2022/12/7.

# `Important Statements`
* FrameStreamingActivity
  - CameraSDK is applied here.
  - There are 5 major methods to show in this activity.
    - requestCameraFrame - This method will response a bitmap via the callback - CameraFrameCallback.
    - requestCameraStreaming - This method will response the streaming (a serial of bitmap) via the callback - CameraFrameCallback.
    - stopCameraStreaming - To stop the streaming that triggered by requestCameraStreaming.
    - pauseCameraService - If you have to open camera directly by your application, you will need to invoke this API before opening the camera.
    - resumeCameraService - If you have invoked the API - pauseCameraService, the resumeCameraService is required to invoke after you closed the camera resource.
  - CameraFrameCallback(int code, Bitmap bitmap) - There are several combinations as:
    - CODE_SDK_ERROR means an exception occurred in local application. Do not connect to Camera Service and the bitmap is null.
    - CODE_NORMAL_RESIZE means the status is normal, but some application applied different resolution camera preview, you should check and handle the bitmap by your requirements.
    - CODE_NORMAL means you can receive the bitmap with you assigned resolution. (default is 640 x 480)
    - CODE_TOO_MANY_CLIENTS means many applications running with camera service concurrently, please try again later. The bitmap will be null.
    - CODE_ILLEGAL_RESOLUTION means you assigned the resolution can not be allowed in current state, please try again later or assign another resolution. The bitmap will be null.
    - CODE_CAMERA_UNAVAILABLE means the Camera Service has been paused by some application invoked the API - pauseCameraService.  The bitmap will be null.