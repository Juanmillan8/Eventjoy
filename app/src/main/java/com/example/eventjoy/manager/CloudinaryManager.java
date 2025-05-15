package com.example.eventjoy.manager;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {

    private static boolean isInitialized = false;
    private static final String CLOUD_NAME = "dotrjlmxg";
    private static final String UPLOAD_PRESET = "my_upload_preset";

    private static void initCloudinary(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            MediaManager.init(context, config);
            isInitialized = true;
        }
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        initCloudinary(context);

        MediaManager.get().upload(imageUri)
                .unsigned(UPLOAD_PRESET)
                .callback(callback)
                .dispatch();
    }

}
