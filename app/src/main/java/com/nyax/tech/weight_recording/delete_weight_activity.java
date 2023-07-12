package com.nyax.tech.weight_recording;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class delete_weight_activity extends AppCompatActivity {

    private final static String TAG = delete_weight_activity.class.getSimpleName();
    ArrayList<notificationdto> _lstnotificationdto = new ArrayList<notificationdto>();

    Button btnweightslist;
    Button btn_delete_weight;
    TextView txt_weight_id;
    TextView txt_weight_date;
    TextView txt_weight_weight;
    TextView lblmsglog;

    String dto_id;
    weight_ui_dto _weight_ui_dto = new weight_ui_dto();

    // Create Databasehelper class object in your activity.
    private sqlite_database_helper_utilz db;
    String IPADRESS = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_weight_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // this will show the back arrow in the tool bar.

        getSupportActionBar().setTitle(Utils.format_info_spannable_string(getSupportActionBar().getTitle().toString()));

        txt_weight_id = (TextView) findViewById(R.id.txt_weight_id);
        txt_weight_date = (TextView) findViewById(R.id.txt_weight_date);
        txt_weight_weight = (TextView) findViewById(R.id.txt_weight_weight);

        lblmsglog = (TextView) findViewById(R.id.lblmsglog);

        btn_delete_weight = (Button) findViewById(R.id.btn_delete_weight);
        btnweightslist = (Button) findViewById(R.id.btnweightslist);

        btnweightslist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipaddress = "";
                if (TextUtils.isEmpty(IPADRESS)) {
                    log_info_messages(Utils.format_error_spannable_string("ip address is empty...").toString());
                }
                Intent list_weights_activity = new Intent(getApplicationContext(), list_weights_activity.class);
                list_weights_activity.putExtra("ipaddress", IPADRESS);
                startActivity(list_weights_activity);
            }
        });

        btn_delete_weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    if(_weight_ui_dto != null) {
                        _weight_ui_dto.weight_id = dto_id;
//                    _weight_ui_dto.weight_weight = txt_weight_weight.getText().toString();
//                    _weight_ui_dto.weight_date = txt_weight_date.getText().toString();

                        new delete_record_Background_Async_Task().execute(_weight_ui_dto);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                    log_error_messages(ex.toString());
                }
            }
        });

        // getting id from bundle
        Bundle _bundle_extras = getIntent().getExtras();

        if (_bundle_extras != null) {

            //The key argument here must match that used in the other activity
            String id = _bundle_extras.getString("id");
            dto_id = id;

            String ipaddress = _bundle_extras.getString("ipaddress");
            IPADRESS = ipaddress;

            // Getting complete record details in background thread
            new get_record_Background_Async_Task().execute(dto_id);
        }

        log_info_messages("finished delete_weight_activity initialization.");

    }

    /*Params the type of the parameters sent to the task upon execution.
    Progress the type of the progress units published during the background computation
    Result the type of the result of the background computation.*/

    /*When defining an AsyncTask we can pass three types between < > brackets.
    Defined as <Params, Progress, Result>*/

    /*onPreExecute() : invoked on the UI thread before the task is executed
    doInBackground(): invoked on the background thread immediately after onPreExecute() finishes executing.
    onProgressUpdate(): invoked on the UI thread after a call to publishProgress(Progress...).
    onPostExecute(): invoked on the UI thread after the background computation finishes*/

    /* AsyncTasks should ideally be used for short operations (a few seconds at the most.)
     An asynchronous task is defined by 3 generic types, called Params, Progress and Result, and 4 steps,
     called onPreExecute(), doInBackground(), onProgressUpdate() and onPostExecute().
     In onPreExecute() you can define code, which need to be executed before background processing starts.
     doInBackground have code which needs to be executed in background, here in doInBackground() we can
     send results to multiple times to event thread by publishProgress() method, to notify background processing
     has been completed we can return results simply.
     onProgressUpdate() method receives progress updates from doInBackground() method, which is published
     via publishProgress() method, and this method can use this progress update to update event thread
     onPostExecute() method handles results returned by doInBackground() method.
     The generic types used are
     Params, the type of the parameters sent to the task upon execution
     Progress, the type of the progress units published during the background computation.
     Result, the type of the result of the background computation.
     If an async task not using any types, then it can be marked as Void type.
     An running async task can be cancelled by calling cancel(boolean) method.
     */

    private class get_record_Background_Async_Task extends AsyncTask<String, Void, weight_ui_dto> {

        @Override
        protected void onPreExecute() {

            // This runs on the UI thread before the background thread executes.
            // Do pre-thread tasks such as initializing variables.
            Log.e(TAG, "onPreExecute");

            Log.e(TAG, "fetching record from datastore...");
            log_info_messages("fetching record from datastore...");

            super.onPreExecute();
        }

        @Override
        protected weight_ui_dto doInBackground(String... param) {
            // Disk-intensive work. This runs on a background thread. Search through a file for the first line that contains "Hello", and return that line.
            Log.e(TAG, "doInBackground");
            try {
                db = new sqlite_database_helper_utilz(getApplicationContext());
                String weight_id = param[0];
                weight_ui_dto weight_ui_dto = db.get_dto_given_id(weight_id);

                return weight_ui_dto;
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Void... p) {
            super.onProgressUpdate(p);
            // Runs on the UI thread after publishProgress is invoked
            Log.e(TAG, "onProgressUpdate");
        }

        @Override
        protected void onPostExecute(weight_ui_dto weight_ui_dto) {
            super.onPostExecute(weight_ui_dto);
            // This runs on the UI thread after complete execution of the doInBackground() method
            // This function receives result(String s) returned from the doInBackground() method.
            // Update UI with the found string.
            Log.e(TAG, "onPostExecute");

            if (weight_ui_dto == null) {
                Log.e(TAG, "error retrieving record.");
                log_info_messages("error retrieving record.");
            } else {
                _weight_ui_dto = weight_ui_dto;

                txt_weight_id.setText(String.valueOf(weight_ui_dto.weight_id));
                txt_weight_weight.setText(weight_ui_dto.weight_weight);
                txt_weight_date.setText(weight_ui_dto.weight_date);

                Log.e(TAG, "successfully retrieved record.");
                log_info_messages("successfully retrieved record.");
            }

        }

    }

    private class delete_record_Background_Async_Task extends AsyncTask<weight_ui_dto, Void, String> {

        @Override
        protected void onPreExecute() {

            // This runs on the UI thread before the background thread executes.
            // Do pre-thread tasks such as initializing variables.
            Log.e(TAG, "onPreExecute");

            Log.e(TAG, "purging record from datastore...");
            log_info_messages("purging record from datastore...");

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(weight_ui_dto... _weight_ui_dto) {
            // Disk-intensive work. This runs on a background thread. Search through a file for the first line that contains "Hello", and return that line.
            Log.e(TAG, "doInBackground");
            try {
                db = new sqlite_database_helper_utilz(getApplicationContext());
                db.delete_weight_given_id(_weight_ui_dto[0].weight_id);
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
            return "record successfully purged from datastore.";
        }

        @Override
        protected void onProgressUpdate(Void... p) {
            super.onProgressUpdate(p);
            // Runs on the UI thread after publishProgress is invoked
            Log.e(TAG, "onProgressUpdate");
        }

        @Override
        protected void onPostExecute(String msg) {
            super.onPostExecute(msg);
            // This runs on the UI thread after complete execution of the doInBackground() method
            // This function receives result(String s) returned from the doInBackground() method.
            // Update UI with the found string.
            Log.e(TAG, "onPostExecute");

            Log.e(TAG, msg);
            log_info_messages(msg);
        }
    }

    private void log_info_messages(String msg) {
        try {
            msg = Utils.format_info_spannable_string(msg).toString();
            notificationdto _notificationdto = new notificationdto();

            Log.e(TAG, msg);

            String dateTimenow = Utils.get_current_datetime();

            String _logtext = Utils.get_new_line() + "[ " + dateTimenow + " ]   " + msg;

            _notificationdto._notification_message = _logtext;
            _notificationdto._created_datetime = dateTimenow;
            _notificationdto.TAG = TAG;

            _lstnotificationdto.add(_notificationdto);

            Collections.sort(_lstnotificationdto, notificationdto.dateComparator);

            ArrayList<String> _lstmsgdto = new ArrayList<String>();
            for (notificationdto _msg : _lstnotificationdto) {
                _lstmsgdto.add(_msg._notification_message);
            }

            lblmsglog.setText(_lstmsgdto.toString());

            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            Utils.log_messages_to_file(msg);

        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    private void log_error_messages(String msg) {
        try {
            msg = Utils.format_error_spannable_string(msg).toString();
            notificationdto _notificationdto = new notificationdto();

            Log.e(TAG, msg);

            String dateTimenow = Utils.get_current_datetime();

            String _logtext = Utils.get_new_line() + "[ " + dateTimenow + " ]   " + msg;

            _notificationdto._notification_message = _logtext;
            _notificationdto._created_datetime = dateTimenow;
            _notificationdto.TAG = TAG;

            _lstnotificationdto.add(_notificationdto);

            Collections.sort(_lstnotificationdto, notificationdto.dateComparator);

            ArrayList<String> _lstmsgdto = new ArrayList<String>();
            for (notificationdto _msg : _lstnotificationdto) {
                _lstmsgdto.add(_msg._notification_message);
            }

            lblmsglog.setText(_lstmsgdto.toString());

            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            Utils.log_messages_to_file(msg);

        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
        }
    }


}
