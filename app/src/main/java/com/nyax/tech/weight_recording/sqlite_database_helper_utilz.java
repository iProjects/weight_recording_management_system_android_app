package com.nyax.tech.weight_recording;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class sqlite_database_helper_utilz extends SQLiteOpenHelper {

    public static final String TAG = sqlite_database_helper_utilz.class.getSimpleName();
    public static int flag;
    //Version of the database. Changing the version will call onUpgrade
    //Database name
    public static final int DATABASE_VERSION = 1;
    private String global_database_path;
    private SQLiteDatabase db;
    private final Context context;
    String global_database_connection_string = "";
    public static final String dbfoldername = "databases";
    public static String packagename = "com.nyax.tech.weight_recording";

    public sqlite_database_helper_utilz(Context context) {

        super(context, DBContract.SQLITE_DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

        try {
            packagename = context.getApplicationContext().getPackageName();
            create_default_database();
            create_databases_in_device_given_paths();
            open_default_database();
            create_tables_in_database();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void create_default_database() {
        ContextWrapper cw = new ContextWrapper(context);

        //apps directory
        File app_dir_file_path = new File(context.getFilesDir().getParent(), dbfoldername);

        String app_dir_path = app_dir_file_path.getPath();

        Log.e(TAG, "app_dir_path [ " + app_dir_path + " ]");

        global_database_path = app_dir_path;

        Log.e(TAG, "DB_PATH [ " + global_database_path + " ]");

        File app_file_name = new File(global_database_path, DBContract.SQLITE_DATABASE_NAME);

        global_database_connection_string = app_file_name.getAbsolutePath();

        Log.e(TAG, "outFileName [ " + global_database_connection_string + " ]");

        File file_DB_PATH = new File(global_database_path);

        Log.e(TAG, "file exists? [ " + file_DB_PATH.exists() + " ]");

        if (!file_DB_PATH.exists()) {
            Log.e(TAG, "created path [ " + file_DB_PATH + " ]");
            file_DB_PATH.mkdirs();
        }

        final File _createNewFile = new File(global_database_connection_string);
        try {
            if (!_createNewFile.exists()) {
                Log.e(TAG, "created file [ " + global_database_connection_string + " ]");
                _createNewFile.createNewFile();

                MediaScannerConnection.scanFile(context, new String[]{_createNewFile.getPath()}, null, null);
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(_createNewFile)));

            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void open_database_given_path(String db_path) throws SQLException {
        try {
            //Open the database
            Log.e(TAG, "db_path: " + db_path);
            File db_dir_file_path = new File(db_path);
            String db_dir_path = db_dir_file_path.getAbsolutePath();

            db = SQLiteDatabase.openDatabase(db_dir_path, null, SQLiteDatabase.OPEN_READWRITE);
            Log.e(TAG, "is DataBase: Opened : " + db.isOpen());
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void open_default_database() throws SQLException {
        try {
            //Open the database
            Log.e(TAG, "database_connection_string: " + global_database_connection_string);
            File db_dir_file_path = new File(global_database_connection_string);
            String db_dir_path = db_dir_file_path.getAbsolutePath();

            db = SQLiteDatabase.openDatabase(db_dir_path, null, SQLiteDatabase.OPEN_READWRITE);
            Log.e(TAG, "is DataBase: Opened : " + db.isOpen());
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void create_tables_in_database_given_path(String db_path) {
        try {
            File db_dir_file_path = new File(db_path);
            String db_dir_path = db_dir_file_path.getAbsolutePath();

            SQLiteDatabase sqlite_db = SQLiteDatabase.openDatabase(db_dir_path, null, SQLiteDatabase.OPEN_READWRITE);

            boolean does_table_exist_in_db = check_if_table_exists(DBContract.weightsentitytable.TABLE_NAME);
            Log.e(TAG, "does_table_exist_in_db : " + does_table_exist_in_db);

            //Create the table
            String SQL_CREATE_CROPS_TABLE = " CREATE TABLE IF NOT EXISTS " + DBContract.weightsentitytable.TABLE_NAME + " (" +
                    DBContract.weightsentitytable.WEIGHT_ID + " INTEGER PRIMARY KEY, " +
                    DBContract.weightsentitytable.WEIGHT_WEIGHT + " TEXT, " +
                    DBContract.weightsentitytable.WEIGHT_DATE + " TEXT, " +
                    DBContract.weightsentitytable.WEIGHT_STATUS + " TEXT, " +
                    DBContract.weightsentitytable.CREATED_DATE + " TEXT, " +
                    DBContract.weightsentitytable.WEIGHT_APP + " TEXT " +
                    " ); ";

            sqlite_db.execSQL(SQL_CREATE_CROPS_TABLE);
            sqlite_db.close();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void create_tables_in_database() {
        try {
            boolean does_table_exist_in_db = check_if_table_exists(DBContract.weightsentitytable.TABLE_NAME);
            Log.e(TAG, "does_table_exist_in_db : " + does_table_exist_in_db);

            //Create the table
            String SQL_CREATE_CROPS_TABLE = " CREATE TABLE IF NOT EXISTS " + DBContract.weightsentitytable.TABLE_NAME + " (" +
                    DBContract.weightsentitytable.WEIGHT_ID + " INTEGER PRIMARY KEY, " +
                    DBContract.weightsentitytable.WEIGHT_WEIGHT + " TEXT, " +
                    DBContract.weightsentitytable.WEIGHT_DATE + " TEXT, " +
                    DBContract.weightsentitytable.WEIGHT_STATUS + " TEXT, " +
                    DBContract.weightsentitytable.CREATED_DATE + " TEXT, " +
                    DBContract.weightsentitytable.WEIGHT_APP + " TEXT " +
                    " ); ";

            db.execSQL(SQL_CREATE_CROPS_TABLE);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    boolean check_if_table_exists(String tableName) {
        if (tableName == null || db == null || !db.isOpen()) {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});
        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    /**
     * Check if the underlying SQLiteDatabase is open
     *
     * @return whether the DB is open or not
     */
    public boolean isOpen() {
        return (db != null && db.isOpen());
    }

    /**
     * Creates an empty database on the system.
     */
    public void create_databases_in_device_given_paths() throws IOException {
        try {

            ArrayList<String> database_paths = get_device_paths();

            for (String db_path : database_paths) {

                File db_file_name = new File(db_path, DBContract.SQLITE_DATABASE_NAME);

                String db_file_path = db_file_name.getAbsolutePath();

                try {
                    if (!db_file_name.exists()) {

                        Log.e(TAG, "created new database file [ " + db_file_path + " ]");
                        Utils.log_messages_to_file("created new database file [ " + db_file_path + " ]");

                        db_file_name.mkdirs();
                        db_file_name.createNewFile();

                        MediaScannerConnection.scanFile(context, new String[]{db_file_name.getPath()}, null, null);
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(db_file_name)));

                        boolean does_db_exist = check_if_database_exists(db_file_path);
                        open_database_given_path(db_file_path);
                        create_tables_in_database_given_path(db_file_path);

                    } else {
                        Log.e(TAG, "database file [ " + db_file_path + " ] exists.");
                        Utils.log_messages_to_file("database file [ " + db_file_path + " ] exists.");
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                    Utils.log_exception_to_file(ex);
                }
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            Utils.log_exception_to_file(ex);
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the
     * application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean check_if_database_exists(String database_file_name_with_path) {
        SQLiteDatabase sqlite_db = null;
        try {

            Log.e(TAG, "database_file_name_with_path: " + database_file_name_with_path);

            sqlite_db = SQLiteDatabase.openDatabase(database_file_name_with_path, null, SQLiteDatabase.OPEN_READWRITE);

        } catch (SQLiteException e) {
            try {
                copyDataBase();
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
        if (sqlite_db != null) {
            sqlite_db.close();
        }
        return sqlite_db != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in
     * the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {
        try {
            Log.e(TAG, "New database is being copied to device!");
            byte[] buffer = new byte[1024];
            OutputStream output_stream = null;
            int length;
            // Open your local db as the input stream
            InputStream input_stream = null;

            input_stream = context.getAssets().open(DBContract.SQLITE_DATABASE_NAME);
            // transfer bytes from the inputfile to the outputfile
            output_stream = new FileOutputStream(global_database_path + DBContract.SQLITE_DATABASE_NAME);
            while ((length = input_stream.read(buffer)) > 0) {
                output_stream.write(buffer, 0, length);
            }
            output_stream.close();
            output_stream.flush();
            input_stream.close();
            Log.e("Database", "New database has been copied to device!");
        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void close() {
        if (db != null)
            db.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        create_tables_in_database();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CROPS_TABLE_NAME);
    }

    public boolean create_weight_in_device_sqlite_database(weight_ui_dto ui_dto) {
        try {
            open_default_database();

            // Create a ContentValues instance which contains the data for each column
            // You do not need to specify a value for the PRIMARY KEY column.
            // Unique values for these are automatically generated.
            final ContentValues values = new ContentValues();
            values.put(DBContract.weightsentitytable.WEIGHT_WEIGHT, ui_dto.weight_weight);
            values.put(DBContract.weightsentitytable.WEIGHT_DATE, ui_dto.weight_date);
            values.put(DBContract.weightsentitytable.WEIGHT_STATUS, ui_dto.weight_status);
            values.put(DBContract.weightsentitytable.CREATED_DATE, ui_dto.created_date);
            values.put(DBContract.weightsentitytable.WEIGHT_APP, ui_dto.weight_app);
            // This call performs the insert
            // The return value is the rowId or primary key value for the new row!
            // If this method returns -1 then the insert has failed.
            final long new_id = db.insert(
                    DBContract.weightsentitytable.TABLE_NAME, // The table name in which the data will be inserted
                    null, // String: optional; may be null. If your provided values is empty,
                    // no column names are known and an empty row can't be inserted.
                    // If not set to null, this parameter provides the name
                    // of nullable column name to explicitly insert a NULL
                    values // The ContentValues instance which contains the data
            );
            if (new_id != -1) {

            } else {

            }

            create_weight_in_device_databases_given_databases(values);

            return true;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } finally {
            close();
        }
    }

    public boolean create_weight_in_device_databases_given_databases(ContentValues values) {
        SQLiteDatabase sqlite_db = null;
        try {

            ArrayList<String> database_paths = get_device_paths();

            for (String db_path : database_paths) {

                File db_file_name = new File(db_path, DBContract.SQLITE_DATABASE_NAME);

                String db_file_path = db_file_name.getAbsolutePath();

                sqlite_db = SQLiteDatabase.openDatabase(db_file_path, null, SQLiteDatabase.OPEN_READWRITE);

                // This call performs the insert
                // The return value is the rowId or primary key value for the new row!
                // If this method returns -1 then the insert has failed.
                final long new_id = sqlite_db.insert(
                        DBContract.weightsentitytable.TABLE_NAME, // The table name in which the data will be inserted
                        null, // String: optional; may be null. If your provided values is empty,
                        // no column names are known and an empty row can't be inserted.
                        // If not set to null, this parameter provides the name
                        // of nullable column name to explicitly insert a NULL
                        values // The ContentValues instance which contains the data
                );
                if (new_id != -1) {

                } else {

                }
            }
            return true;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } finally {
            if (sqlite_db != null)
                sqlite_db.close();
        }
    }

    public ArrayList<weight_ui_dto> get_records_list_in_device_sqlite_database() {
        ArrayList<weight_ui_dto> lst_records = new ArrayList<weight_ui_dto>();
        try {

            // When reading data one should always just get a readable database.
            open_default_database();
            final Cursor cursor = db.query(
                    // Name of the table to read from
                    DBContract.weightsentitytable.TABLE_NAME,
                    // String array of the columns which are supposed to be read
                    new String[]{DBContract.weightsentitytable.WEIGHT_ID,
                            DBContract.weightsentitytable.WEIGHT_WEIGHT,
                            DBContract.weightsentitytable.WEIGHT_DATE,
                            DBContract.weightsentitytable.WEIGHT_STATUS,
                            DBContract.weightsentitytable.WEIGHT_APP,
                            DBContract.weightsentitytable.CREATED_DATE},
                    // The selection argument which specifies which row is read.
                    // ? symbols are parameters.
                    null,
                    // The actual parameters values for the selection as a String array.
                    // ? above take the value from here
                    null,
                    // GroupBy clause. Specify a column name to group similar values
                    // in that column together.
                    null,
                    // Having clause. When using the GroupBy clause this allows you to
                    // specify which groups to include.
                    null,
                    // OrderBy clause. Specify a column name here to order the results
                    // according to that column. Optionally append ASC or DESC to specify
                    // an ascending or descending order.
                    null
            );
            // To increase performance first get the index of each column in the cursor
            final int weight_id_index = cursor.getColumnIndex(DBContract.weightsentitytable.WEIGHT_ID);
            final int weight_weight_index = cursor.getColumnIndex(DBContract.weightsentitytable.WEIGHT_WEIGHT);
            final int weight_date_index = cursor.getColumnIndex(DBContract.weightsentitytable.WEIGHT_DATE);
            final int weight_status_index = cursor.getColumnIndex(DBContract.weightsentitytable.WEIGHT_STATUS);
            final int weight_app_index = cursor.getColumnIndex(DBContract.weightsentitytable.WEIGHT_APP);
            final int created_date_index = cursor.getColumnIndex(DBContract.weightsentitytable.CREATED_DATE);

            try {
                // If moveToFirst() returns false then cursor is empty
                if (!cursor.moveToFirst()) {
                    return new ArrayList();
                }

                do {
                    // Read the values of a row in the table using the indexes acquired above
                    weight_ui_dto _weight_ui_dto = new weight_ui_dto();
                    _weight_ui_dto.weight_id = cursor.getString(weight_id_index);
                    _weight_ui_dto.weight_weight = cursor.getString(weight_weight_index);
                    _weight_ui_dto.weight_date = cursor.getString(weight_date_index);
                    _weight_ui_dto.weight_status = cursor.getString(weight_status_index);
                    _weight_ui_dto.weight_app = cursor.getString(weight_app_index);
                    _weight_ui_dto.created_date = cursor.getString(created_date_index);

                    lst_records.add(_weight_ui_dto);

                } while (cursor.moveToNext());

            } finally {
                // Don't forget to close the Cursor once you are done to avoid memory leaks.
                // Using a try/finally like in this example is usually the best way to handle this
                cursor.close();
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            // close the database
            close();
        }
        return lst_records;
    }

    private ArrayList<String> get_device_paths() {
        ArrayList<String> device_paths_list = new ArrayList<String>();
        try {

            try {
                //apps directory
                File app_dir_file_path = new File(context.getFilesDir().getParent(), dbfoldername);

                String app_dir_path = app_dir_file_path.getPath();

                Log.e(TAG, "app_dir_path [ " + app_dir_path + " ]");

                device_paths_list.add(app_dir_path);

            } catch (Exception ex) {
                Utils.log_exception_to_file(ex);
            }

            try {
                // Access your app's directory in the device's Public documents directory
                File public_docs_dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), dbfoldername);

                String public_docs_dir_path = public_docs_dir.getPath();

                Log.e(TAG, "public_docs_dir_path [ " + public_docs_dir_path + " ]");

                device_paths_list.add(public_docs_dir_path);

            } catch (Exception ex) {
                Utils.log_exception_to_file(ex);
            }

            try {
                // Access your app's directory in the device's Public downloads directory
                File public_downloads_dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), dbfoldername);

                String public_downloads_dir_path = public_downloads_dir.getPath();

                Log.e(TAG, "public_downloads_dir_path [ " + public_downloads_dir_path + " ]");

                device_paths_list.add(public_downloads_dir_path);

            } catch (Exception ex) {
                Utils.log_exception_to_file(ex);
            }

            try {
                // Access your app's Private documents directory
                File private_docs_dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), dbfoldername);

                String private_docs_dir_path = private_docs_dir.getPath();

                Log.e(TAG, "private_docs_dir_path [ " + private_docs_dir_path + " ]");

                device_paths_list.add(private_docs_dir_path);


            } catch (Exception ex) {
                Utils.log_exception_to_file(ex);
            }

            try {
                String state = Environment.getExternalStorageState();
                if (state.equals(Environment.MEDIA_MOUNTED)) {
                    // Available to read and write

                    File external_storage_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), dbfoldername);

                    String external_storage_dir_path = external_storage_dir.getPath();

                    Log.e(TAG, "external_storage_dir_path [ " + external_storage_dir_path + " ]");

                    device_paths_list.add(external_storage_dir_path);

                }

            } catch (Exception ex) {
                Utils.log_exception_to_file(ex);
            }

            try {
                File root_dir = new File(Environment.getRootDirectory(), dbfoldername);
                String root_dir_path = root_dir.getAbsolutePath();

                Log.e(TAG, "root_dir_path [ " + root_dir_path + " ]");

                device_paths_list.add(root_dir_path);

            } catch (Exception ex) {
                Utils.log_exception_to_file(ex);
            }

            //Collections.reverse(device_paths_list);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return device_paths_list;
    }


    public boolean update_weight(weight_ui_dto ui_dto) {
        try {

            // When reading data one should always just get a readable database.
            open_default_database();
            // Create a ContentValues instance which contains the data for each column
            // You do not need to specify a value for the PRIMARY KEY column.
            // Unique values for these are automatically generated.
            final ContentValues values = new ContentValues();
            values.put(DBContract.weightsentitytable.WEIGHT_WEIGHT, ui_dto.weight_weight);
            values.put(DBContract.weightsentitytable.WEIGHT_DATE, ui_dto.weight_date);
            values.put(DBContract.weightsentitytable.WEIGHT_STATUS, ui_dto.weight_status);
            values.put(DBContract.weightsentitytable.CREATED_DATE, ui_dto.created_date);
            values.put(DBContract.weightsentitytable.WEIGHT_APP, ui_dto.weight_app);

            db.update(
                    // Name of the table to read from
                    DBContract.weightsentitytable.TABLE_NAME,
                    values,
                    //filter by
                    "weight_id = ? ",
                    new String[]{ui_dto.weight_id});

            return true;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } finally {
            close();
        }
    }

    public weight_ui_dto get_dto_given_id(String dto_id) {
        ArrayList<weight_ui_dto> lst_dtos = filte_dto_given_id(String.valueOf(dto_id));
        weight_ui_dto _weight_ui_dto = lst_dtos.get(0);
        return _weight_ui_dto;
    }

    public ArrayList<weight_ui_dto> filte_dto_given_id(String searchTerm) {
        final ArrayList<weight_ui_dto> lst_dtos = new ArrayList();
        try {
            // When reading data one should always just get a readable database.
            // final SQLiteDatabase database = this.getReadableDatabase();
            open_default_database();
            final Cursor cursor = db.query(
                    // Name of the table to read from
                    DBContract.weightsentitytable.TABLE_NAME,
                    // String array of the columns which are supposed to be read
                    new String[]{DBContract.weightsentitytable.WEIGHT_ID,
                            DBContract.weightsentitytable.WEIGHT_WEIGHT,
                            DBContract.weightsentitytable.WEIGHT_DATE,
                            DBContract.weightsentitytable.WEIGHT_STATUS,
                            DBContract.weightsentitytable.CREATED_DATE,
                            DBContract.weightsentitytable.WEIGHT_APP},
                    // The selection argument which specifies which row is read. ? symbols are parameters.
                    DBContract.weightsentitytable.WEIGHT_ID + " LIKE ?",
                    // The actual parameters values for the selection as a String array. ? above take the value from here
                    new String[]{"%" + searchTerm + "%"},
                    // GroupBy clause. Specify a column name to group similar values in that column together.
                    null,
                    // Having clause. When using the GroupBy clause this allows you to specify which groups to include.
                    null,
                    // OrderBy clause. Specify a column name here to order the results according to that column. Optionally append ASC or DESC to specify an ascending or descending order.
                    null
            );
            // To increase performance first get the index of each column in the cursor
            final int weight_id_Index = cursor.getColumnIndex(DBContract.weightsentitytable.WEIGHT_ID);
            final int weight_weight_Index = cursor.getColumnIndex(DBContract.weightsentitytable.WEIGHT_WEIGHT);
            final int weight_date_Index = cursor.getColumnIndex(DBContract.weightsentitytable.WEIGHT_DATE);
            final int weight_status_Index = cursor.getColumnIndex(DBContract.weightsentitytable.WEIGHT_STATUS);
            final int created_date_Index = cursor.getColumnIndex(DBContract.weightsentitytable.CREATED_DATE);
            final int weight_app_Index = cursor.getColumnIndex(DBContract.weightsentitytable.WEIGHT_APP);

            // If moveToFirst() returns false then cursor is empty
            if (!cursor.moveToFirst()) {
                return new ArrayList();
            }
            do {
                // Read the values of a row in the table using the indexes acquired above
                weight_ui_dto _weight_ui_dto = new weight_ui_dto();
                _weight_ui_dto.weight_id = cursor.getString(weight_id_Index);
                _weight_ui_dto.weight_weight = cursor.getString(weight_weight_Index);
                _weight_ui_dto.weight_date = cursor.getString(weight_date_Index);
                _weight_ui_dto.weight_status = cursor.getString(weight_status_Index);
                _weight_ui_dto.created_date = cursor.getString(created_date_Index);
                _weight_ui_dto.weight_app = cursor.getString(weight_app_Index);

                lst_dtos.add(_weight_ui_dto);

            } while (cursor.moveToNext());

            // Don't forget to close the Cursor once you are done to avoid memory leaks.
            // Using a try/finally like in this example is usually the best way to handle this
            cursor.close();

            return lst_dtos;

        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            try {
                Utils.log_exception_to_file(ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        } finally {
            // close the database
            close();
        }
    }

    public Integer delete_weight_given_id(String weight_id) {
        return db.delete(
                DBContract.weightsentitytable.TABLE_NAME,
                "weight_id = ? ",
                new String[]{weight_id});
    }


}
