package com.nyax.tech.weight_recording;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class receive_boot_complete_receiver  extends BroadcastReceiver {
    public static final String TAG = copy_folders_receiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            Toast.makeText(context, "ACTION_BOOT_COMPLETED", Toast.LENGTH_LONG).show();
            Log.e(TAG, "received ACTION_BOOT_COMPLETED event");

            Intent activityIntent = new Intent(context, MainActivity.class);

            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(activityIntent);
        }
    }
}

