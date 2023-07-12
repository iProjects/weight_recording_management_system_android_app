package com.nyax.tech.weight_recording;

import android.graphics.Color;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public final class Utils {
    public static final String TAG = Utils.class.getSimpleName();

    public static String get_current_datetime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(("dd-MM-yyyy HH:mm:ss a"));
        LocalDateTime now = LocalDateTime.now();
        String dateTimenow = dtf.format(now);
        return dateTimenow;
    }

    public static String get_current_date() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(("dd-MM-yyyy"));
        LocalDateTime now = LocalDateTime.now();
        String dateTimenow = dtf.format(now);
        return dateTimenow;
    }

    public static String get_new_line() {
        String newline = System.getProperty("line.separator");
        return newline;
    }

    public static String get_random_weight() {
        String virtual_weight = generate_random_integer();
        int number = Integer.parseInt(virtual_weight);
        while (number < 10) {
            virtual_weight = generate_random_integer();
            number = Integer.parseInt(virtual_weight);
            if (number >= 10) {
                break;
            }
        }
        return virtual_weight;
    }

    public static String generate_random_integer() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(("SSS"));
        LocalDateTime now = LocalDateTime.now();
        String dateTimenow = dtf.format(now);
        String milliseconds = dateTimenow;
        String virtual_weight = milliseconds.substring(0, 2);
        return virtual_weight;
    }

    public static void log_messages_to_file(String message) throws IOException {
        BufferedWriter writer = null;
        FileWriter fileWriter = null;
        try {
            String log_file_name = "log.txt";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), log_file_name);

            if (file.exists()) {
            } else {
                file.createNewFile();
            }

            fileWriter = new FileWriter(file, true);
            writer = new BufferedWriter(fileWriter);

            writer.append("==================================================================");
            writer.append("\n\r");
            writer.append("LOG AT :" + Utils.get_current_datetime());
            writer.append("\n\r");
            writer.append("MESSAGE:" + message);
            writer.append("\n\r");
            writer.append("==================================================================");
            writer.append("\n\r");
            writer.append("");

        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }

            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    public static void log_exception_to_file(Exception sourceException) throws IOException {
        BufferedWriter writer = null;
        FileWriter fileWriter = null;
        try {
            String log_file_name = "log.txt";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), log_file_name);

            if (file.exists()) {
            } else {
                file.createNewFile();
            }

            fileWriter = new FileWriter(file, true);
            writer = new BufferedWriter(fileWriter);

            writer.append("==================================================================");
            writer.append("\n\r");
            writer.append("ERROR OCCOURED AT :" + Utils.get_current_datetime());
            writer.append("\n\r");
            writer.append("SOURCE:" + sourceException.getClass().getCanonicalName());
            writer.append("\n\r");
            writer.append("MESSAGE:" + sourceException.getMessage());
            writer.append("\n\r");
            writer.append("Whole Exception:" + sourceException.toString());
            writer.append("\n\r");
            writer.append("==================================================================");
            writer.append("\n\r");
            writer.append("");

        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }

            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    public static SpannableString format_info_spannable_string(String texttoformat) {
        SpannableString spannable_string = new SpannableString(texttoformat);
        spannable_string.setSpan(new ForegroundColorSpan(Color.GREEN), 0, texttoformat.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable_string;
    }

    public static SpannableString format_error_spannable_string(String texttoformat) {
        SpannableString spannable_string = new SpannableString(texttoformat);
        spannable_string.setSpan(new ForegroundColorSpan(Color.RED), 0, texttoformat.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable_string;
    }

public static String  get_elaped_time(long start, long current)
{
    long _start = System.nanoTime();
    long _current = System.nanoTime();

    long timeElapsed = current - start;

    TimeUnit.MINUTES.toSeconds(timeElapsed);

    String elapsed_time=String.valueOf(timeElapsed);
    return elapsed_time;
}



}
