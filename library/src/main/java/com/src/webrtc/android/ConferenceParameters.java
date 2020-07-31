package com.src.webrtc.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.appspot.apprtc.PeerConnectionClient;

import static android.content.Context.MODE_PRIVATE;

class ConferenceParameters {

    private static final String TAG = "[wa]ConferenceParameters";

    // +++++ Copy from CallActivity +++++
    public static final String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
    public static final String EXTRA_URLPARAMETERS = "org.appspot.apprtc.URLPARAMETERS";
    public static final String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
    public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
    public static final String EXTRA_SCREENCAPTURE = "org.appspot.apprtc.SCREENCAPTURE";
    public static final String EXTRA_CAMERA2 = "org.appspot.apprtc.CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_BITRATE = "org.appspot.apprtc.VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC = "org.appspot.apprtc.VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = "org.appspot.apprtc.HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "org.appspot.apprtc.CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = "org.appspot.apprtc.FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = "org.appspot.apprtc.AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC = "org.appspot.apprtc.AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
            "org.appspot.apprtc.NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = "org.appspot.apprtc.AECDUMP";
    public static final String EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED =
            "org.appspot.apprtc.SAVE_INPUT_AUDIO_TO_FILE";
    public static final String EXTRA_OPENSLES_ENABLED = "org.appspot.apprtc.OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = "org.appspot.apprtc.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = "org.appspot.apprtc.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = "org.appspot.apprtc.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
            "org.appspot.apprtc.DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = "org.appspot.apprtc.DISPLAY_HUD";
    public static final String EXTRA_TRACING = "org.appspot.apprtc.TRACING";
    public static final String EXTRA_CMDLINE = "org.appspot.apprtc.CMDLINE";
    public static final String EXTRA_RUNTIME = "org.appspot.apprtc.RUNTIME";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
            "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static final String EXTRA_USE_VALUES_FROM_INTENT =
            "org.appspot.apprtc.USE_VALUES_FROM_INTENT";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = "org.appspot.apprtc.DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = "org.appspot.apprtc.ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS = "org.appspot.apprtc.MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = "org.appspot.apprtc.MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = "org.appspot.apprtc.PROTOCOL";
    public static final String EXTRA_NEGOTIATED = "org.appspot.apprtc.NEGOTIATED";
    public static final String EXTRA_ID = "org.appspot.apprtc.ID";
    public static final String EXTRA_ENABLE_RTCEVENTLOG = "org.appspot.apprtc.ENABLE_RTCEVENTLOG";
    public static final String EXTRA_USE_LEGACY_AUDIO_DEVICE =
            "org.appspot.apprtc.USE_LEGACY_AUDIO_DEVICE";
    // ----- Copy from CallActivity -----

    // +++++ Copy from ConnectActivity +++++
    private SharedPreferences sharedPref;
    // ----- Copy from ConnectActivity -----

    private Context mContext;

    public ConferenceParameters(Context context) {
        mContext = context;
    }

    public Intent prepareVideoCallIntent() {

        PreferenceManager.setDefaultValues(mContext, "webrtc", MODE_PRIVATE, R.xml.preferences, false);
        sharedPref = mContext.getSharedPreferences("webrtc", MODE_PRIVATE);

        // +++++ Copy from ConnectActivity +++++
        String keyprefResolution = mContext.getString(R.string.pref_resolution_key);
        String keyprefFps = mContext.getString(R.string.pref_fps_key);
        String keyprefVideoBitrateType = mContext.getString(R.string.pref_maxvideobitrate_key);
        String keyprefVideoBitrateValue = mContext.getString(R.string.pref_maxvideobitratevalue_key);
        String keyprefAudioBitrateType = mContext.getString(R.string.pref_startaudiobitrate_key);
        String keyprefAudioBitrateValue = mContext.getString(R.string.pref_startaudiobitratevalue_key);
        String keyprefRoomServerUrl = mContext.getString(R.string.pref_room_server_url_key);
        String keyprefRoom = mContext.getString(R.string.pref_room_key);
        String keyprefRoomList = mContext.getString(R.string.pref_room_list_key);
        // ----- Copy from ConnectActivity -----

        boolean useValuesFromIntent = false;

        // +++++ Copy from ConnectActivity +++++

        // Video call enabled flag.
        boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
                EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, EXTRA_CAMERA2,
                R.string.pref_camera2_default, useValuesFromIntent);

        // Get default codecs.
        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);
        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);

        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default, useValuesFromIntent);

        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default,
                useValuesFromIntent);

        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default, useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default,
                useValuesFromIntent);

        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, useValuesFromIntent);

        boolean saveInputAudioToFile =
                sharedPrefGetBoolean(R.string.pref_enable_save_input_audio_to_file_key,
                        EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED,
                        R.string.pref_enable_save_input_audio_to_file_default, useValuesFromIntent);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, useValuesFromIntent);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default,
                useValuesFromIntent);

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default,
                useValuesFromIntent);

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default,
                useValuesFromIntent);

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key, useValuesFromIntent);

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
//        if (useValuesFromIntent) {
//            videoWidth = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_WIDTH, 0);
//            videoHeight = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 0);
//        }
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution =
                    sharedPref.getString(keyprefResolution, mContext.getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
//        if (useValuesFromIntent) {
//            cameraFps = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_FPS, 0);
//        }
        if (cameraFps == 0) {
            String fps = sharedPref.getString(keyprefFps, mContext.getString(R.string.pref_fps_default));
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
                EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                R.string.pref_capturequalityslider_default, useValuesFromIntent);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
//        if (useValuesFromIntent) {
//            videoStartBitrate = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_BITRATE, 0);
//        }
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = mContext.getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, mContext.getString(R.string.pref_maxvideobitratevalue_default));
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
//        if (useValuesFromIntent) {
//            audioStartBitrate = getIntent().getIntExtra(CallActivity.EXTRA_AUDIO_BITRATE, 0);
//        }
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = mContext.getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefAudioBitrateValue, mContext.getString(R.string.pref_startaudiobitratevalue_default));
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        // Check statistics display option.
        boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
                EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, useValuesFromIntent);

        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, EXTRA_TRACING,
                R.string.pref_tracing_default, useValuesFromIntent);

        // Check Enable RtcEventLog.
        boolean rtcEventLogEnabled = sharedPrefGetBoolean(R.string.pref_enable_rtceventlog_key,
                EXTRA_ENABLE_RTCEVENTLOG, R.string.pref_enable_rtceventlog_default,
                useValuesFromIntent);

        boolean useLegacyAudioDevice = sharedPrefGetBoolean(R.string.pref_use_legacy_audio_device_key,
                EXTRA_USE_LEGACY_AUDIO_DEVICE, R.string.pref_use_legacy_audio_device_default,
                useValuesFromIntent);

        // Get datachannel options
//        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
//                EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default,
//                useValuesFromIntent);
        boolean dataChannelEnabled = true;
        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, EXTRA_ORDERED,
                R.string.pref_ordered_default, useValuesFromIntent);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                EXTRA_NEGOTIATED, R.string.pref_negotiated_default, useValuesFromIntent);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
                useValuesFromIntent);
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default, useValuesFromIntent);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, EXTRA_ID,
                R.string.pref_data_id_default, useValuesFromIntent);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);

        // ----- Copy from ConnectActivity -----

        // +++++ Copy from ConnectActivity +++++

//        Uri uri = Uri.parse(roomUrl);
//        Intent intent = new Intent(this, CallActivity.class);
//        intent.setData(uri);
//        intent.putExtra(CallActivity.EXTRA_ROOMID, roomId);
//        intent.putExtra(CallActivity.EXTRA_LOOPBACK, loopback);
        Intent intent = new Intent();
        intent.putExtra(EXTRA_VIDEO_CALL, videoCallEnabled);
        intent.putExtra(EXTRA_SCREENCAPTURE, useScreencapture);
        intent.putExtra(EXTRA_CAMERA2, useCamera2);
        intent.putExtra(EXTRA_VIDEO_WIDTH, videoWidth);
        intent.putExtra(EXTRA_VIDEO_HEIGHT, videoHeight);
        intent.putExtra(EXTRA_VIDEO_FPS, cameraFps);
        intent.putExtra(EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
        intent.putExtra(EXTRA_VIDEO_BITRATE, videoStartBitrate);
        intent.putExtra(EXTRA_VIDEOCODEC, videoCodec);
        intent.putExtra(EXTRA_HWCODEC_ENABLED, hwCodec);
        intent.putExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
        intent.putExtra(EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
        intent.putExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
        intent.putExtra(EXTRA_AECDUMP_ENABLED, aecDump);
        intent.putExtra(EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, saveInputAudioToFile);
        intent.putExtra(EXTRA_OPENSLES_ENABLED, useOpenSLES);
        intent.putExtra(EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
        intent.putExtra(EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
        intent.putExtra(EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
        intent.putExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
        intent.putExtra(EXTRA_AUDIO_BITRATE, audioStartBitrate);
        intent.putExtra(EXTRA_AUDIOCODEC, audioCodec);
        intent.putExtra(EXTRA_DISPLAY_HUD, displayHud);
        intent.putExtra(EXTRA_TRACING, tracing);
        intent.putExtra(EXTRA_ENABLE_RTCEVENTLOG, rtcEventLogEnabled);
//        intent.putExtra(EXTRA_CMDLINE, commandLineRun);
//        intent.putExtra(EXTRA_RUNTIME, runTimeMs);
        intent.putExtra(EXTRA_USE_LEGACY_AUDIO_DEVICE, useLegacyAudioDevice);

        intent.putExtra(EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

        if (dataChannelEnabled) {
            intent.putExtra(EXTRA_ORDERED, ordered);
            intent.putExtra(EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
            intent.putExtra(EXTRA_MAX_RETRANSMITS, maxRetr);
            intent.putExtra(EXTRA_PROTOCOL, protocol);
            intent.putExtra(EXTRA_NEGOTIATED, negotiated);
            intent.putExtra(EXTRA_ID, id);
        }

        // ----- Copy from ConnectActivity -----

        return intent;
    }

    public PeerConnectionClient.PeerConnectionParameters getPeerConnectionParameters(ConnectParameters params) {
        String videoCodec;
        switch (params.getPreferredVideoCodec()) {
            case H264:
                videoCodec = VideoCodec.H264.toString() + " Baseline";
                break;
            case VP9:
                videoCodec = VideoCodec.VP9.toString();
                break;
            default:
                videoCodec = VideoCodec.VP8.toString();
                break;
        }

        String audioCodec;
        switch (params.getPreferredAudioCodec()) {
            case ISAC:
                audioCodec = AudioCodec.ISAC.toString();
                break;
            default:
                audioCodec = AudioCodec.OPUS.toString().toLowerCase();
        }


        Intent intent = prepareVideoCallIntent();

        boolean loopback = intent.getBooleanExtra(EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(EXTRA_TRACING, false);

        int videoWidth = intent.getIntExtra(EXTRA_VIDEO_WIDTH, 0);
        int videoHeight = intent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0);

        final int videoBitrate = params.getMaxVideoBitrate();
        final int audioBitrate = params.getStartAudioBitrate();

//        boolean screencaptureEnabled = intent.getBooleanExtra(EXTRA_SCREENCAPTURE, false);
//        // If capturing format is not specified for screencapture, use screen resolution.
//        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
//            DisplayMetrics displayMetrics = getDisplayMetrics();
//            videoWidth = displayMetrics.widthPixels;
//            videoHeight = displayMetrics.heightPixels;
//        }
        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
        if (intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(EXTRA_PROTOCOL),
                    intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, -1));
        }

        PeerConnectionClient.PeerConnectionParameters peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(intent.getBooleanExtra(EXTRA_VIDEO_CALL, true), loopback,
                        tracing, videoWidth, videoHeight, intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
                        /*intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0)*/videoBitrate, /*intent.getStringExtra(EXTRA_VIDEOCODEC)*/videoCodec,
                        intent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
                        intent.getBooleanExtra(EXTRA_FLEXFEC_ENABLED, false),
                        /*intent.getIntExtra(EXTRA_AUDIO_BITRATE, 0)*/audioBitrate, /*intent.getStringExtra(EXTRA_AUDIOCODEC)*/audioCodec,
                        intent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false),
                        intent.getBooleanExtra(EXTRA_ENABLE_RTCEVENTLOG, false),dataChannelParameters);

        return peerConnectionParameters;
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = mContext.getString(defaultId);
        /*if (useFromIntent) {
            String value = getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {*/
            String attributeName = mContext.getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        /*}*/
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        boolean defaultValue = Boolean.parseBoolean(mContext.getString(defaultId));
        /*if (useFromIntent) {
            return getIntent().getBooleanExtra(intentName, defaultValue);
        } else {*/
            String attributeName = mContext.getString(attributeId);
            return sharedPref.getBoolean(attributeName, defaultValue);
        /*}*/
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = mContext.getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        /*if (useFromIntent) {
            return getIntent().getIntExtra(intentName, defaultValue);
        } else {*/
            String attributeName = mContext.getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        /*}*/
    }
}
