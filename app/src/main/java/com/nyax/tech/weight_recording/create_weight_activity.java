package com.nyax.tech.weight_recording;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class create_weight_activity extends AppCompatActivity {

    //declare controls
    TextView txtcreateweight_weight;
    TextView txtcreateweight_date;
    Button btncreateweight;
    Button btnweightslist;
    TextView lblerrormsg;

    public static final String TAG = create_weight_activity.class.getSimpleName();
    ArrayList<notificationdto> _lstnotificationdto = new ArrayList<notificationdto>();
    ProgressBar progress_bar;

    // Create Databasehelper class object in your activity.
    private sqlite_database_helper_utilz db_helper;
    responsedto _responsedto = new responsedto();
    String IPADRESS = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_weight_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true); // this will show the back arrow in the tool bar.

        getSupportActionBar().setTitle(Utils.format_info_spannable_string(getSupportActionBar().getTitle().toString()));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String ipaddress = extras.getString("ipaddress");
            //The key argument here must match that used in the other activity
            IPADRESS = ipaddress;
        }

        //initialize controls
        txtcreateweight_weight = (TextView) findViewById(R.id.txtcreateweight_weight);
        txtcreateweight_date = (TextView) findViewById(R.id.txtcreateweight_date);
        lblerrormsg = (TextView) findViewById(R.id.lblmsglog);
        lblerrormsg.setText("");

        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

        btncreateweight = (Button) findViewById(R.id.btncreateweight);
        btnweightslist = (Button) findViewById(R.id.btnweightslist);

        txtcreateweight_weight.setText(Utils.get_random_weight());
        txtcreateweight_date.setText(Utils.get_current_date());

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

        btncreateweight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btncreateweight.setText(("working..."));
                progress_bar.setVisibility(View.GONE);

                log_info_messages("validating...");

                String weight = "";
                String date = "";
                String msg = "";

                if (!TextUtils.isEmpty(txtcreateweight_weight.getText().toString())) {
                    weight = txtcreateweight_weight.getText().toString();
                } else {
                    msg = "weight cannot be null." + Utils.get_new_line();
                }
                if (!TextUtils.isEmpty(txtcreateweight_date.getText().toString())) {
                    date = txtcreateweight_date.getText().toString();
                } else {
                    msg = "date cannot be null.";
                }

                if (msg.length() > 0) {
                    log_error_messages("validation failed.");
                    log_error_messages(msg);

                    progress_bar.setVisibility(View.GONE);

                    return;
                }
                log_info_messages("validation succeeded.");
                log_info_messages("creating record in storage...");

                weight_ui_dto ui_dto = new weight_ui_dto();
                ui_dto.weight_weight = weight;
                ui_dto.weight_date = date;
                ui_dto.weight_status = "active";
                ui_dto.weight_app = "android";
                ui_dto.created_date = Utils.get_current_datetime();

                new create_record_Background_AsyncTask().execute(ui_dto);

                save_record_in_shared_prefs(ui_dto);

                save_weight_in_device_sqlite_database(ui_dto);

            }
        });

        log_info_messages("finished create_weight_activity initialization.");

    }

    public void save_weight_in_device_sqlite_database(weight_ui_dto ui_dto) {
        try {
            responsedto _responsedto = new responsedto();

            db_helper = new sqlite_database_helper_utilz(getApplicationContext());
            boolean _response_from_sqlite = db_helper.create_weight_in_device_sqlite_database(ui_dto);

            if (_response_from_sqlite) {
                _responsedto.setisresponseresultsuccessful(_response_from_sqlite);
                _responsedto.setresponsesuccessmessage("successfully created record in sqlite in device.");
                log_info_messages("successfully created record in sqlite in device.");
            }

        } catch (Exception ex) {
            log_error_messages(ex.toString());
        }
    }

    public void save_record_in_shared_prefs(weight_ui_dto ui_dto) {
        try {
            final String PREFS_FILE = "weights_shared_prefs";
            // PREFS_MODE defines which apps can access the file
            final int PREFS_MODE = Context.MODE_PRIVATE;

            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // write string
            String string_dto = sharedPreferences.getString("weights", "weights");
            String sanitized_dto = string_dto.replace("[", "").replace("]", "");

            String[] weights_arr = {};
            ArrayList<String> arrayList;

            if (sanitized_dto.isEmpty()) {
                arrayList = new ArrayList<String>(Arrays.asList(weights_arr));
                String arr = "#" + ui_dto.weight_weight + ":" + ui_dto.weight_date + ",";
                arrayList.add(arr);
                Collections.reverse(arrayList);
                weights_arr = arrayList.toArray(weights_arr);
            } else {
                weights_arr = sanitized_dto.split(",");
                arrayList = new ArrayList<String>(Arrays.asList(weights_arr));
                String arr = "#" + ui_dto.weight_weight + ":" + ui_dto.weight_date;
                arrayList.add(arr);
                Collections.reverse(arrayList);
                weights_arr = arrayList.toArray(weights_arr);
            }

            editor.putString("weights", Arrays.toString(weights_arr));

            // This will asynchronously save the shared preferences without holding the current thread.
            editor.apply();

            log_info_messages("successfully saved record in shared preferences.");

            int count = weights_arr.length;

            log_info_messages("total records in device : " + count);

            Log.e(TAG, Arrays.toString(weights_arr));

        } catch (Exception ex) {
            log_error_messages(ex.toString());
        }
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
    private class create_record_Background_AsyncTask extends AsyncTask<weight_ui_dto, Void, responsedto> {

        @Override
        protected void onPreExecute() {

            // This runs on the UI thread before the background thread executes.
            // Do pre-thread tasks such as initializing variables.
            Log.e(TAG, "onPreExecute");
            log_info_messages("start executing task on background thread...");

            progress_bar.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }

        @Override
        protected responsedto doInBackground(weight_ui_dto... _weight_ui_dto) {
            // Disk-intensive work. This runs on a background thread.
            // Search through a file for the first line that contains "Hello", and return
            // that line.
            int response_code = 0;
            StringBuffer string_buffer = new StringBuffer();
            responsedto _responsedto = new responsedto();

            try {
                //Thread.sleep(5000);


                Log.e(TAG, "doInBackground");

                int CONNECTION_TIMEOUT = DBContract.CONNECTION_TIMEOUT;
                int READ_TIMEOUT = DBContract.READ_TIMEOUT;

                HttpURLConnection conn;

                URL url = null;

                if (TextUtils.isEmpty(IPADRESS)) {
                    _responsedto.setisresponseresultsuccessful(false);
                    _responsedto.setresponseerrormessage("ip address is empty...");
                    return _responsedto;
                }

                String build_url = DBContract.REMOTE_SERVER_URL_SCHEME + IPADRESS + DBContract.REMOTE_SERVER_URL_DOMAIN;

                String _url = build_url;

                url = new URL(_url);

                // Setup HttpURLConnection class to send and receive data from php and mysql
                //call openConnection() on a URL instance. Since openConnection() returns a URLConnection, you need to explicitly cast the returned value
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("weight_weight", _weight_ui_dto[0].weight_weight)
                        .appendQueryParameter("weight_date", _weight_ui_dto[0].weight_date)
                        .appendQueryParameter("weight_app", _weight_ui_dto[0].weight_app);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

                response_code = conn.getResponseCode();
                Log.e(TAG, String.valueOf(response_code));

                if (response_code == HttpURLConnection.HTTP_OK) { //success
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;

                    while ((inputLine = reader.readLine()) != null) {
                        string_buffer.append(inputLine);
                    }
                    reader.close();

                    // print result
                    System.out.println(string_buffer.toString());
                    Log.e(TAG, string_buffer.toString());

                    _responsedto.setresponsesuccessmessage(string_buffer.toString());
                    _responsedto.setisresponseresultsuccessful(true);
                } else {
                    _responsedto.setresponseerrormessage("error contacting remote server..." + Utils.get_new_line() + " server response code : " + response_code);
                    _responsedto.setisresponseresultsuccessful(false);
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
                _responsedto.setresponseerrormessage("error contacting remote server...response_code: " + response_code + Utils.get_new_line() + ex.toString());
                _responsedto.setisresponseresultsuccessful(false);
            }

            return _responsedto;
        }

        @Override
        protected void onProgressUpdate(Void... p) {
            super.onProgressUpdate(p);
            // Runs on the UI thread after publishProgress is invoked
            Log.e(TAG, "onProgressUpdate");
            log_info_messages("executing task on background thread...");
        }

        @Override
        protected void onPostExecute(responsedto _responsedto) {
            super.onPostExecute(_responsedto);
            // This runs on the UI thread after complete execution of the doInBackground() method
            // This function receives result(String s) returned from the doInBackground() method.
            // Update UI with the found string.
            Log.e(TAG, "onPostExecute");

            String msg = "";
            if (_responsedto.getisresponseresultsuccessful()) {
                msg += _responsedto.getresponsesuccessmessage();
                msg += Utils.get_new_line();
            } else {
                msg += _responsedto.getresponseerrormessage();
            }

            Log.e(TAG, msg);

            log_info_messages(msg);

            log_info_messages("finished executing task on background thread...");

            txtcreateweight_weight.setText(Utils.get_random_weight());
            txtcreateweight_date.setText(Utils.get_current_date());
            btncreateweight.setText(("create weight"));

            progress_bar.setVisibility(View.GONE);
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

            lblerrormsg.setText(_lstmsgdto.toString());

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

            lblerrormsg.setText(_lstmsgdto.toString());

            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            Utils.log_messages_to_file(msg);

        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



}