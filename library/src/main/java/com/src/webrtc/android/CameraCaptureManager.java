package com.src.webrtc.android;

import android.content.Context;
import android.util.Log;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.VideoCapturer;

public class CameraCaptureManager {
    private static final String TAG = "[rtc]CameraCaptureManager";

    private Context context;

    private org.webrtc.VideoCapturer videoCapturer;

    public VideoCapturer getVideoCapturer() {
        Log.d(TAG, "createVideoCapture");
//        org.webrtc.VideoCapturer videoCapturer = null;
        /*String videoFileAsCamera = getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
        if (videoFileAsCamera != null) {
            try {
                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
            } catch (IOException e) {
                reportError("Failed to open video file for emulated camera");
                return null;
            }
        } else if (screencaptureEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return createScreenCapturer();
        } else*/
        if (useCamera2()) {
            /*if (!captureToTexture()) {
                reportError(getString(R.string.camera2_texture_only_error));
                return null;
            }*/

            Log.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(context));
        } else {
            Log.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(false/*captureToTexture()*/));
        }
        if (videoCapturer == null) {
            Log.e(TAG, "Failed to open camera");
        }

        return videoCapturer;
    }

    public void switchCamera(Listener.CameraSwitchListener listener) {
        if (videoCapturer == null) {
            return;
        }

        if (videoCapturer instanceof CameraVideoCapturer) {
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
            cameraVideoCapturer.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
                @Override
                public void onCameraSwitchDone(boolean b) {
                    listener.onCameraSwitchDone(b);
                }

                @Override
                public void onCameraSwitchError(String s) {
                    listener.onCameraSwitchError(s);
                }
            });
        } else {
            Log.e(TAG, "Will not switch camera, video capturer is not a camera");
        }
    }

    public CameraCaptureManager(Context context) {
        this.context = context;
    }

    private org.webrtc.VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Log.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating front facing camera capturer.");
                org.webrtc.VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Log.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating other camera capturer.");
                org.webrtc.VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(context)/* && getIntent().getBooleanExtra(EXTRA_CAMERA2, true)*/;
    }
}
