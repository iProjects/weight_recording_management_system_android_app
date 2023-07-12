package com.nyax.tech.weight_recording;

import java.util.Comparator;

public class notificationdto {
    public String _notification_message;
    public String _created_datetime;
    public String TAG;

    /*comparator for sorting the list by datetime*/
    public static Comparator<notificationdto> dateComparator = new Comparator<notificationdto>() {
        @Override
        public int compare(notificationdto msg1, notificationdto msg2) {
            String date1 = msg1._created_datetime;
            String date2 = msg2._created_datetime;

            //descending order
            return date2.compareTo(date1);
        }
    };
}
