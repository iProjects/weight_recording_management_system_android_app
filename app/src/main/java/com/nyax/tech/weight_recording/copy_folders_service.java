package com.nyax.tech.weight_recording;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;

public class copy_folders_service extends Service {

    public static final String TAG = create_weight_activity.class.getSimpleName();

    private final IBinder binder = new localbinder();

    public class localbinder extends Binder {
        copy_folders_service getService() {
            return copy_folders_service.this;
        }
    }
//
//    public void OnCreate() {
//        super.onCreate();
//        Log.e(TAG, "onCreate");
//
//    }
//
//@Override
//    public int OnStartCommand(Intent intent, int flags, int startid) {
//        Log.e(TAG, "OnStartCommand");
//        return super.onStartCommand(intent, flags, startid);
//    }
//
//    public void OnStart(Intent intent, int startid) {
//        //super.OnStart(intent, startid);
//        Log.e(TAG, "OnStart");
//    }
//
//    @Nullable


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void copy_files() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    File whatsapp_dir = new File("Android/media/com.whatsapp/WhatsApp/Media");
                    if (!whatsapp_dir.exists())
                        return;

                    File media_dir = new File("Android/media/WhatsApp/Media");

                    if (!media_dir.exists())
                        media_dir.mkdirs();

                    File[] whats_app_dirs = whatsapp_dir.listFiles();

                    for (File dir : whats_app_dirs) {

                        String dir_path = dir.getPath();
                        Log.e(TAG, dir_path);

                        File[] whats_app_files_in_dir = dir.listFiles();

                        for (File file_in_dir : whats_app_files_in_dir) {

                            String file_or_dir = file_in_dir.getPath();
                            Log.e(TAG, dir_path);
                        }
                    }

                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        };
        handler.post(runnable);

    }


}
