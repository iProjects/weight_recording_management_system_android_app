package com.nyax.tech.weight_recording;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    TextView txt_server_ip_adress;
    Button btncreateweight;
    Button btnweightslist;
    Button btn_sync_local_db_with_server;
    TextView lblmsglog;
    TextView lbltimedisplay;
    ProgressBar progress_bar;
    ArrayList<notificationdto> _lstnotificationdto = new ArrayList<notificationdto>();
    // Create Databasehelper class object in your activity.
    private sqlite_database_helper_utilz db;
    String IPADRESS = "";
    long _start_time = System.nanoTime();
    static final Timer timer = new Timer("current time");
    final String TAG = MainActivity.class.getSimpleName();
    final int PERMISSION_REQUEST_CODE = 200;
    final String MANAGE_EXTERNAL_STORAGE_PERMISSION = "Manage External Storage Permission";
    final String SHARED_PREFS_FILE = "SHARED_PREFS_FILE_COPY_FILES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_server_ip_adress = (TextView) findViewById(R.id.txt_server_ip_adress);
        btncreateweight = (Button) findViewById(R.id.btncreateweight);
        btnweightslist = (Button) findViewById(R.id.btnweightslist);
        btn_sync_local_db_with_server = (Button) findViewById(R.id.btn_sync_local_db_with_server);
        lbltimedisplay = (TextView) findViewById(R.id.lbltimedisplay);
        lblmsglog = (TextView) findViewById(R.id.lblmsglog);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

        get_ip_address_from_shared_prefs();

        btncreateweight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipaddress = "";
                if (!TextUtils.isEmpty(txt_server_ip_adress.getText())) {
                    ipaddress = txt_server_ip_adress.getText().toString();

                    save_ip_address_in_shared_prefs();
                }
                Intent create_weight_activity = new Intent(getApplicationContext(), create_weight_activity.class);
                create_weight_activity.putExtra("ipaddress", ipaddress);
                startActivity(create_weight_activity);

            }
        });

        btnweightslist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipaddress = "";
                if (!TextUtils.isEmpty(txt_server_ip_adress.getText())) {
                    ipaddress = txt_server_ip_adress.getText().toString();

                    save_ip_address_in_shared_prefs();
                }
                Intent list_weights_activity = new Intent(getApplicationContext(), list_weights_activity.class);
                list_weights_activity.putExtra("ipaddress", ipaddress);
                startActivity(list_weights_activity);

            }
        });

        btn_sync_local_db_with_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress_bar.setVisibility(View.GONE);
                btn_sync_local_db_with_server.setText("working...");

                String ipaddress = txt_server_ip_adress.getText().toString();
                IPADRESS = ipaddress;

                try {

                    String permission_status = get_permission_from_shared_prefs();

                    if (permission_status.equals("DENIED")) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        storageActivityResultLauncher.launch(intent);
                    }

                    IntentFilter filter = new IntentFilter();

                    copy_folders_receiver copy_folders_receiver = new copy_folders_receiver();
                    receive_boot_complete_receiver receive_boot_complete_receiver = new receive_boot_complete_receiver();

                    filter.addAction(Intent.ACTION_SCREEN_ON);
                    filter.addAction(Intent.ACTION_SCREEN_OFF);

                    filter.addAction(Intent.ACTION_BOOT_COMPLETED);
                    filter.addAction(Intent.ACTION_REBOOT);

                    getApplicationContext().registerReceiver(copy_folders_receiver, filter);
                    getApplicationContext().registerReceiver(receive_boot_complete_receiver, filter);

                    copy_folders_receiver.copy_files(getApplicationContext());

                    new get_records_list_from_remote_server_Background_Async_Task();

                } catch (Exception ex) {
                    btn_sync_local_db_with_server.setText(R.string.btn_sync_local_db_with_server);
                    Log.e(TAG, ex.toString());
                    log_error_messages(ex.toString());
                }
            }
        });

        display_running_time();

        try {

            IntentFilter filter = new IntentFilter();

            copy_folders_receiver copy_folders_receiver = new copy_folders_receiver();
            receive_boot_complete_receiver receive_boot_complete_receiver = new receive_boot_complete_receiver();

            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);

            filter.addAction(Intent.ACTION_BOOT_COMPLETED);
            filter.addAction(Intent.ACTION_REBOOT);

            getApplicationContext().registerReceiver(copy_folders_receiver, filter);
            getApplicationContext().registerReceiver(receive_boot_complete_receiver, filter);

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            log_error_messages(ex.toString());
        }

        log_info_messages("finished main_activity initialization.");

    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");
                    String msg = "";
                    //here we will handle the result of our intent
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        //Android is 11(R) or above
                        if (Environment.isExternalStorageManager()) {
                            //Manage External Storage Permission is granted
                            save_permission_in_shared_prefs("GRANTED");
                            msg = "Manage External Storage Permission is granted";
                            Log.e(TAG, "onActivityResult: Manage External Storage Permission is granted");
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            log_info_messages(msg);
                        } else {
                            //Manage External Storage Permission is denied
                            save_permission_in_shared_prefs("DENIED");
                            msg = "Manage External Storage Permission is denied";
                            Log.e(TAG, "onActivityResult: Manage External Storage Permission is denied");
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            log_info_messages(msg);
                        }
                    } else {
                        //Android is below 11(R)
                    }
                }
            }
    );

    public void save_ip_address_in_shared_prefs() {
        try {
            if (TextUtils.isEmpty(txt_server_ip_adress.getText())) {
                return;
            }
            String ipaddress = txt_server_ip_adress.getText().toString();
            final String PREFS_FILE = "weights_shared_prefs";
            // PREFS_MODE defines which apps can access the file
            final int PREFS_MODE = Context.MODE_PRIVATE;

            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // write string
            String string_dto = sharedPreferences.getString("ipaddress", "127.0.0.1");
            String sanitized_dto = string_dto.replace("[", "").replace("]", "");

            String[] weights_arr = {};
            ArrayList<String> arrayList;

            if (sanitized_dto.isEmpty()) {
                arrayList = new ArrayList<String>(Arrays.asList(weights_arr));
                String arr = "#" + ipaddress + ",";
                arrayList.add(arr);
                Collections.reverse(arrayList);
                weights_arr = arrayList.toArray(weights_arr);
            } else {
                weights_arr = sanitized_dto.split(",");
                arrayList = new ArrayList<String>(Arrays.asList(weights_arr));
                String arr = "#" + ipaddress + ",";
                arrayList.add(arr);
                Collections.reverse(arrayList);
                weights_arr = arrayList.toArray(weights_arr);
            }

            editor.putString("ipaddress", Arrays.toString(weights_arr));

            // This will asynchronously save the shared preferences without holding the current thread.
            editor.apply();

            log_info_messages("successfully saved record in shared preferences.");

            int count = weights_arr.length;

            log_info_messages("total records in device : " + count);

            Log.e(TAG, Arrays.toString(weights_arr));

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            log_error_messages(ex.toString());
        }
    }

    public void get_ip_address_from_shared_prefs() {
        try {

            final String PREFS_FILE = "weights_shared_prefs";
            // PREFS_MODE defines which apps can access the file
            final int PREFS_MODE = Context.MODE_PRIVATE;

            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // write string
            String string_dto = sharedPreferences.getString("ipaddress", "127.0.0.1");
            String sanitized_dto = string_dto.replace("[", "").replace("]", "");

            String[] weights_arr = {};
            ArrayList<String> arrayList;

            if (sanitized_dto.isEmpty()) {
                arrayList = new ArrayList<String>(Arrays.asList(weights_arr));
                Collections.reverse(arrayList);
                weights_arr = arrayList.toArray(weights_arr);
            } else {
                weights_arr = sanitized_dto.split(",");
                arrayList = new ArrayList<String>(Arrays.asList(weights_arr));
                weights_arr = arrayList.toArray(weights_arr);
            }

            String ipaddress = arrayList.get(0);
            ipaddress = ipaddress.replace("#", "");
            txt_server_ip_adress.setText(ipaddress);

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            log_error_messages(ex.toString());
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

            //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

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

    public void get_list_of_connected_device_hotspot() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                BufferedReader br = null;
                boolean isfirstline = true;
                try {
                    br = new BufferedReader(new FileReader("/proc/net/arp"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (isfirstline) {
                            isfirstline = false;
                            continue;
                        }
                        String[] splitted = line.split(" +");
                        if (splitted != null && splitted.length >= 4) {
                            String ipaddress = splitted[0];
                            String macaddress = splitted[3];

                            boolean isreachable = InetAddress.getByName(splitted[0]).isReachable(500);

                            if (isreachable) {
                                Log.e(TAG, "device information " +
                                        "ipaddress : " + ipaddress +
                                        "macaddress : " + macaddress);
                            }
                        }
                    }

                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.toString());
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                } finally {
                    try {
                        if (br != null)
                            br.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        });
        thread.start();
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
    private class get_records_list_from_remote_server_Background_Async_Task extends AsyncTask<String, Void, responsedto> {

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
        protected responsedto doInBackground(String... params) {
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
                        .appendQueryParameter("", "");
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

                    StringBuilder _response = new StringBuilder();

                    while ((inputLine = reader.readLine()) != null) {
                        _response.append(inputLine);
                        Log.e(TAG, "inputLine [ " + inputLine + " ]");

                        if (inputLine.contains("<b")) {
                            string_buffer.append(inputLine + "\n");
                        }
                    }

                    reader.close();

                    Log.e(TAG, "_response [ " + _response.toString() + " ]");

                    final ArrayList<weight_ui_dto> lst_dtos = new ArrayList();

                    try {
                        JSONArray json_Array = new JSONArray(_response.toString());

                        for (int i = 0; i < json_Array.length(); i++) {

                            JSONObject json_Object = json_Array.getJSONObject(i);

                            weight_ui_dto _currentdto = new weight_ui_dto();

                            _currentdto.weight_id = json_Object.getString("weight_id");
                            _currentdto.weight_weight = json_Object.getString("weight_weight");
                            _currentdto.weight_date = json_Object.getString("weight_date");
                            _currentdto.weight_status = json_Object.getString("weight_status");
                            _currentdto.weight_app = json_Object.getString("weight_app");
                            _currentdto.created_date = json_Object.getString("created_date");

                            lst_dtos.add(_currentdto);
                        }

                        Log.e(TAG, "records count : [ " + lst_dtos.size() + " ]");
                        string_buffer.append(Utils.format_error_spannable_string("fetched  [ " + lst_dtos.size() + " ] records from remote server." + Utils.get_new_line()));

                        _responsedto.setresponseresultobject(lst_dtos);

                        _responsedto.setisresponseresultsuccessful(true);

                    } catch (final Exception ex) {
                        string_buffer.append(Utils.format_error_spannable_string(ex.toString()) + Utils.get_new_line());
                        Log.e(TAG, ex.toString());
                        _responsedto.setisresponseresultsuccessful(false);
                    }

                } else {
                    string_buffer.append(Utils.format_error_spannable_string("error contacting server. server response code : " + response_code + Utils.get_new_line()));
                    Log.e(TAG, "error contacting server. server response code : " + response_code);
                    _responsedto.setisresponseresultsuccessful(false);
                }

            } catch (Exception ex) {
                string_buffer.append(Utils.format_error_spannable_string(ex.toString()) + Utils.get_new_line());
                Log.e(TAG, ex.toString());
                _responsedto.setisresponseresultsuccessful(false);
            }

            // print result
            System.out.println(string_buffer.toString());
            Log.e(TAG, string_buffer.toString());

            _responsedto.setresponsesuccessmessage(string_buffer.toString());

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

            if (_responsedto.getresponseresultobject() != null) {
                ArrayList<weight_ui_dto> _lst_dtos = (ArrayList<weight_ui_dto>) _responsedto.getresponseresultobject();

                new synchronize_device_records_with_server_records_Background_Async_Task().execute(_lst_dtos);
            }

            Log.e(TAG, msg);

            log_info_messages(msg);

            log_info_messages("finished executing task on background thread...");

            progress_bar.setVisibility(View.GONE);
        }
    }

    private class synchronize_device_records_with_server_records_Background_Async_Task extends AsyncTask<ArrayList<weight_ui_dto>, Void, String> {

        @Override
        protected void onPreExecute() {

            // This runs on the UI thread before the background thread executes.
            // Do pre-thread tasks such as initializing variables.
            Log.e(TAG, "onPreExecute");

            Log.e(TAG, "fetching record from datastore...");
            log_info_messages("fetching record from datastore...");

            progress_bar.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(ArrayList<weight_ui_dto>... param) {
            // Disk-intensive work. This runs on a background thread. Search through a file for the first line that contains "Hello", and return that line.
            Log.e(TAG, "doInBackground");
            StringBuffer string_buffer = new StringBuffer();
            try {

                db = new sqlite_database_helper_utilz(getApplicationContext());

                ArrayList<weight_ui_dto> server_lst_dtos = param[0];
                ArrayList<weight_ui_dto> device_lst_records = db.get_records_list_in_device_sqlite_database();

                int device_count = device_lst_records.size();
                int server_count = server_lst_dtos.size();

                string_buffer.append("fetched [ " + device_count + " ] records from device." + Utils.get_new_line());
                Log.e(TAG, "fetched [ " + device_count + " ] records from device.");
                string_buffer.append("fetched [ " + server_count + " ] records from server." + Utils.get_new_line());
                Log.e(TAG, "fetched [ " + server_count + " ] records from server.");

                for (int i = 0; i < server_count; i++) {

                    weight_ui_dto server_dto = server_lst_dtos.get(i);

                    boolean exists = false;

                    Log.e(TAG, "processing record...");
                    Log.e(TAG, server_dto.weight_id + " : " + server_dto.weight_date + " : " + server_dto.weight_weight + " : " + server_dto.created_date);

                    for (int h = 0; h < device_count; h++) {

                        weight_ui_dto device_dto = device_lst_records.get(h);

                        if (server_dto.weight_weight.equals(device_dto.weight_weight) || server_dto.weight_date.equals(device_dto.weight_date)) {

                            exists = true;
                            Log.e(TAG, "record exists.");
                            break;
                        }
                    }
                    if (!exists) {

                        boolean _response_from_sqlite = db.create_weight_in_device_sqlite_database(server_dto);

                        if (_response_from_sqlite) {
                            string_buffer.append("successfully created record in sqlite in device." + Utils.get_new_line());
                            Log.e(TAG, "successfully created record in sqlite in device.");
                        } else {
                            string_buffer.append("error creating record in sqlite in device." + Utils.get_new_line());
                            Log.e(TAG, "error creating record in sqlite in device.");
                        }
                    } else {
                        string_buffer.append("record exists in sqlite in device." + Utils.get_new_line());
                        Log.e(TAG, "record exists in sqlite in device.");
                    }

                }

            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
                string_buffer.append(ex.toString() + Utils.get_new_line());
            }
            return string_buffer.toString();
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

            btn_sync_local_db_with_server.setText(R.string.btn_sync_local_db_with_server);

            progress_bar.setVisibility(View.GONE);

        }
    }

    private void display_running_time() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //task to be executed every second
                try {

                    long _current_time = System.nanoTime();

                    long timeElapsed = _current_time - _start_time;

                    timeElapsed = timeElapsed / 1000000;

                    String _days = String.valueOf(Math.abs(timeElapsed) / (1000 * 60 * 60 * 24));
                    String _hours = String.valueOf(Math.abs(timeElapsed) / (1000 * 60 * 60) % 24);
                    String _minutes = String.valueOf(Math.abs(timeElapsed) / (1000 * 60) % 60);
                    String _seconds = String.valueOf(Math.abs(timeElapsed) / (1000) % 60);

                    String days = "";
                    String hours = "";
                    String minutes = "";
                    String seconds = "";

                    if (_days.length() < 2)
                        days = "0" + _days;
                    else
                        days = _days;

                    if (_hours.length() < 2)
                        hours = "0" + _hours;
                    else
                        hours = _hours;

                    if (_minutes.length() < 2)
                        minutes = "0" + _minutes;
                    else
                        minutes = _minutes;

                    if (_seconds.length() < 2)
                        seconds = "0" + _seconds;
                    else
                        seconds = _seconds;

                    String _elapsed_time = days + ':' + hours + ':' + minutes + ':' + seconds;

                    String current_date_time = Utils.get_current_datetime();
                    lbltimedisplay.setText(current_date_time);

                    String running_time = current_date_time + Utils.get_new_line() + _elapsed_time;
                    Log.e(TAG, running_time);

                    try {
                        lbltimedisplay.setText(running_time);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }

                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        };

        //this will invoke the timer every second
        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    public void save_permission_in_shared_prefs(String permission_status) {
        try {

            final String PREFS_FILE = SHARED_PREFS_FILE;
            // PREFS_MODE defines which apps can access the file
            final int PREFS_MODE = Context.MODE_PRIVATE;

            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // write string
            String string_dto = sharedPreferences.getString(MANAGE_EXTERNAL_STORAGE_PERMISSION, "DENIED");

            editor.putString(MANAGE_EXTERNAL_STORAGE_PERMISSION, permission_status);

            // This will asynchronously save the shared preferences without holding the current thread.
            editor.apply();

            log_info_messages("successfully saved record in shared preferences.");

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            log_error_messages(ex.toString());
        }
    }

    public String get_permission_from_shared_prefs() {
        try {

            final String PREFS_FILE = SHARED_PREFS_FILE;
            // PREFS_MODE defines which apps can access the file
            final int PREFS_MODE = Context.MODE_PRIVATE;

            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // write string
            String permission_status = sharedPreferences.getString(MANAGE_EXTERNAL_STORAGE_PERMISSION, "DENIED");

            return permission_status;

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            log_error_messages(ex.toString());
            return null;
        }
    }


}