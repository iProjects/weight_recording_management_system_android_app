package com.nyax.tech.weight_recording;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class list_weights_activity extends AppCompatActivity {

    public static final String TAG = list_weights_activity.class.getSimpleName();

    // Create DatabasehelperUtilz class object in your activity.
    private sqlite_database_helper_utilz db;

    ArrayList<notificationdto> _lstnotificationdto = new ArrayList<notificationdto>();
    ArrayList<weight_ui_dto> device_lst_records = new ArrayList<weight_ui_dto>();
    String IPADRESS = "";
    TextView lblmsglog;
    TextView lbl_weights_list_title;

    //The minimum amount of items to have below your current scroll position before loading more.
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_weights_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // this will show the back arrow in the tool bar.

        getSupportActionBar().setTitle(Utils.format_info_spannable_string(getSupportActionBar().getTitle().toString()));

        lblmsglog = (TextView) findViewById(R.id.lblmsglog);
        lbl_weights_list_title = (TextView) findViewById(R.id.lbl_weights_list_title);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String ipaddress = extras.getString("ipaddress");
            //The key argument here must match that used in the other activity
            IPADRESS = ipaddress;
            log_info_messages(Utils.format_info_spannable_string("contacting remote server at ip address [ " + IPADRESS + " ]").toString());
        }

        get_records_list_in_device_sqlite_database();

        new get_records_list_from_remote_server_Background_Async_Task().execute("");

        log_info_messages("finished list_weights_activity initialization...");
    }

    public void get_records_list_in_device_sqlite_database() {
        try {

            db = new sqlite_database_helper_utilz(getApplicationContext());
            device_lst_records = db.get_records_list_in_device_sqlite_database();

            int count = device_lst_records.size();

            if (count > 0) {

                log_info_messages("fetched [ " + count + " ] records from device");
                Log.e(TAG, "fetched [ " + count + " ] records from device");

                for (weight_ui_dto _weight_ui_dto : device_lst_records) {
                    Log.e(TAG, _weight_ui_dto.weight_id + " : " + _weight_ui_dto.weight_date + " : " + _weight_ui_dto.weight_weight + " : " + _weight_ui_dto.created_date);
                }
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
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
    private class get_records_list_from_remote_server_Background_Async_Task extends AsyncTask<String, Void, responsedto> {

        @Override
        protected void onPreExecute() {

            // This runs on the UI thread before the background thread executes.
            // Do pre-thread tasks such as initializing variables.
            Log.e(TAG, "onPreExecute");
            log_info_messages("start executing task on background thread...");
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
                    //return _responsedto;
                }

                String build_url = "http://" + IPADRESS + ":90/weight_recording_app/get_to_android_api.php";

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

                ArrayList<weight_ui_dto> server_lst_dtos = (ArrayList<weight_ui_dto>) _responsedto.getresponseresultobject();

                if (server_lst_dtos != null && server_lst_dtos.size() > 0) {

                    int count = server_lst_dtos.size();
                    String title = getString(R.string.lbl_weights_list_title);
                    lbl_weights_list_title.setText(title + " [ " + count + " ]");

                    populate_recycler_view_list_control(server_lst_dtos);
                }
            } else {

                int count = device_lst_records.size();
                String title = lbl_weights_list_title.getText().toString();
                lbl_weights_list_title.setText(title + " [ " + count + " ]");

                populate_recycler_view_list_control(device_lst_records);
            }

            Log.e(TAG, msg);
            log_info_messages(msg);

            log_info_messages("finished executing task on background thread...");
            Log.e(TAG, "finished executing task on background thread...");

        }
    }

    private void populate_recycler_view_list_control(final ArrayList<weight_ui_dto> lst_dtos) {

        runOnUiThread(new Runnable() {
            public void run() {
                /**
                 * Updating data into ListView
                 * */
                try {

                    //get handle
                    RecyclerView _recyclerview = findViewById(R.id.list_recycler_view);

                    //set a layout manager (LinearLayoutManager in this example)
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());

                    _recyclerview.setLayoutManager(mLayoutManager);

                    _recyclerview.setHasFixedSize(true);

                    _recyclerview.setItemAnimator(new DefaultItemAnimator());
                    //_recyclerview.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
                    //specify an adapter
                    weights_list_recycler_view_adapter _recyclerviewadapter = new weights_list_recycler_view_adapter(getApplicationContext(), lst_dtos, IPADRESS)
                            /*{
                    @Override
                    public void load() {
                    do your stuff here */
                    /* This method is automatically call while user reach at end of your list.
                    }}*/;

                    _recyclerview.setAdapter(_recyclerviewadapter);

                    // adding custom divider line
                    _recyclerview.addItemDecoration(new SimpleBlueDivider(getApplicationContext()));

                    // row click listener
                    /* _recyclerview.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), _recyclerview, new RecyclerTouchListener.ClickListener() {

                    @Override
                    public void onClick(View view, int position) {

                    Toast.makeText(getApplicationContext(), _dto.getcrop_name() + " is selected!", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                    })); */

                    if (_recyclerview.getLayoutManager() instanceof LinearLayoutManager) {

                        final LinearLayoutManager _linearLayoutManager = (LinearLayoutManager)
                                _recyclerview.getLayoutManager();

                        _recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {

                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                                super.onScrolled(recyclerView, dx, dy);

                                totalItemCount = _linearLayoutManager.getItemCount();

                                lastVisibleItem = _linearLayoutManager.findLastVisibleItemPosition();

                                if (!loading && totalItemCount <= (lastVisibleItem +
                                        visibleThreshold)) {

                                    if (onLoadMoreListener != null) {
                                        onLoadMoreListener.onLoadMore();
                                    }

                                    loading = true;
                                }
                            }
                        });
                    }

                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                    log_error_messages(ex.toString());
                }

            }
        });

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