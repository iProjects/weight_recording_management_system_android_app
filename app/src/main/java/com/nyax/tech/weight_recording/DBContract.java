package com.nyax.tech.weight_recording;

public final class DBContract {

    // To prevent someone from accidentally instantiating the contract class,
// give it an empty constructor.
    public DBContract() {
    }

    public static final String SQLITE_DATABASE_NAME = "july.sqlite3";
    public static final String REMOTE_SERVER_URL_SCHEME = "http://";
    public static final String REMOTE_SERVER_URL_DOMAIN = ":90/weight_recording_app/get_to_android_api.php";

    public static final Integer CONNECTION_TIMEOUT = 20000;
    public static final Integer READ_TIMEOUT = 20000;

    //weights table
    public static class weightsentitytable
    {
        public static String TABLE_NAME = "tblweights";
        //Columns of the table
        public static String WEIGHT_ID = "weight_id";
        public static String WEIGHT_WEIGHT = "weight_weight";
        public static String WEIGHT_DATE = "weight_date";
        public static String WEIGHT_STATUS = "weight_status";
        public static String CREATED_DATE = "created_date";
        public static String WEIGHT_APP = "weight_app";

    }

    public static final class app_entities_wrapper {
        public static final String weights = "weights";

    }






}
