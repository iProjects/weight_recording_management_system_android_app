package com.nyax.tech.weight_recording;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class copy_folders_receiver extends BroadcastReceiver {
    public static final String TAG = copy_folders_receiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.e(TAG, "action = " + action);

        Toast.makeText(context, "BroadcastReceiver", Toast.LENGTH_LONG).show();

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.EFFECT_HEAVY_CLICK);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.e(TAG, "received ACTION_SCREEN_ON event");
            Toast.makeText(context, "ACTION_SCREEN_ON", Toast.LENGTH_LONG).show();

            copy_files(context);

            Intent copy_folders_service = new Intent(context, copy_folders_service.class);
            context.startService(copy_folders_service);

        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.e(TAG, "received ACTION_SCREEN_OFF event");
            Toast.makeText(context, "ACTION_SCREEN_OFF", Toast.LENGTH_LONG).show();

            copy_files(context);

//            Intent copy_folders_service = new Intent(context, copy_folders_service.class);
//            context.startService(copy_folders_service);
        }

    }

    public void copy_files(final Context context) {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    int counta = 0;
                    File whatsapp_dir = new File("/storage/self/primary/Android/media/com.whatsapp/WhatsApp/Media");

                    if (!whatsapp_dir.exists())
                        return;

                    File media_back_up_dir = new File("/storage/0E6F-2D71/Android/backup/Media/WhatsApp");

                    if (!media_back_up_dir.exists())
                        media_back_up_dir.mkdirs();

                    File[] whats_app_dirs = whatsapp_dir.listFiles();

                    for (File media_dir : whats_app_dirs) {

                        try {

                            if (media_dir.isDirectory()) {
                                String dir_path = media_dir.getPath();
                                Log.e(TAG, dir_path);

                                File whats_up_dir = new File(dir_path);

                                File[] whats_app_files_in_dir = whats_up_dir.listFiles();

                                counta = whats_app_files_in_dir.length;

                                for (File current_file_being_moved : whats_app_files_in_dir) {

                                    try {

                                        if (current_file_being_moved.isDirectory()) {


                                        } else {
                                            String media_file = current_file_being_moved.getPath();
                                            Log.e(TAG, media_file);

                                            File file_media = new File(media_file);

                                            String current_folder = file_media.getParent();

                                            String[] current_dir_arr = current_folder.split("/");
                                            String current_dir_name = current_dir_arr[current_dir_arr.length - 1];// Get last value in array

                                            Log.e(TAG, "current_dir_name: " + current_dir_name);
                                            Toast.makeText(context, "current_dir_name: " + current_dir_name, Toast.LENGTH_LONG).show();

                                            File destination_dir = new File(media_back_up_dir.getPath(), current_dir_name);

                                            if (!destination_dir.exists())
                                                destination_dir.mkdirs();

                                            String current_file = file_media.getPath();

                                            String[] current_file_arr = current_file.split("/");
                                            String current_file_name = current_file_arr[current_file_arr.length - 1];// Get last value in array

                                            Log.e(TAG, "current_file_name: " + current_file_name);
                                            Toast.makeText(context, "current_file_name: " + current_file_name, Toast.LENGTH_LONG).show();

                                            if (current_file_name.equals(".nomedia")) {
                                                continue;
                                            }

                                            File destination_file = new File(destination_dir.getPath(), current_file_name);

                                            if (!destination_file.exists())
                                                destination_file.createNewFile();

                                            Log.e(TAG, "destination file: " + destination_file.getPath());
                                            Toast.makeText(context, "destination file: " + destination_file.getPath(), Toast.LENGTH_LONG).show();

                                            Log.e(TAG, "current file: " + current_file_being_moved.getPath());
                                            Toast.makeText(context, "current file: " + current_file_being_moved.getPath(), Toast.LENGTH_LONG).show();

                                            InputStream is = new FileInputStream(current_file_being_moved);
                                            OutputStream os = new FileOutputStream(destination_file);

                                            byte[] buffer = new byte[1024];
                                            int length;
                                            while ((length = is.read(buffer)) > 0) {
                                                os.write(buffer, 0, length);
                                            }

                                            boolean isdeleted = current_file_being_moved.delete();

                                            if (isdeleted) {
                                                Log.e(TAG, "deleted file: " + current_file_being_moved.getPath());
                                                Toast.makeText(context, "deleted file: " + current_file_being_moved.getPath(), Toast.LENGTH_LONG).show();
                                            }

                                            File[] files_remaining_in_current_whats_app_dir = whats_up_dir.listFiles();
                                            Log.e(TAG, "files remaining in directory [ " + current_dir_name + " ] " + files_remaining_in_current_whats_app_dir.length);
                                            Toast.makeText(context, "files remaining in directory [ " + current_dir_name + " ] " + files_remaining_in_current_whats_app_dir.length, Toast.LENGTH_LONG).show();

                                            File[] files_in_destination_dir = destination_dir.listFiles();
                                            Log.e(TAG, "files in destination: " + files_in_destination_dir.length);
                                            Toast.makeText(context, "files in destination: " + files_in_destination_dir.length, Toast.LENGTH_LONG).show();

                                            if (is != null) {
                                                try {
                                                    is.close();
                                                } catch (Exception ex) {
                                                    Log.e(TAG, ex.toString());
                                                    is = null;
                                                }
                                            }
                                            if (os != null) {
                                                try {
                                                    os.close();
                                                } catch (Exception ex) {
                                                    Log.e(TAG, ex.toString());
                                                    os = null;
                                                }
                                            }

                                            save_to_database();
                                        }

                                    } catch (Exception ex) {
                                        Log.e(TAG, ex.toString());
                                    }

                                }
                            }

                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }
                    }

                    if (counta > 16) {
                        Log.e(TAG, "counta: " + counta);
                        Toast.makeText(context, "counta: " + counta, Toast.LENGTH_LONG).show();
                        copy_files(context);
                    } else {
                        Log.e(TAG, "counta: " + counta);
                        Toast.makeText(context, "counta: " + counta, Toast.LENGTH_LONG).show();
                        //return;
                    }

                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        };
        handler.post(runnable);

    }

    private void save_to_database()
    {

    }


}
