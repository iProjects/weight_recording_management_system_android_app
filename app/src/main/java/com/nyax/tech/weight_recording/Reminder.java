package com.nyax.tech.weight_recording;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Simple demo that uses java.util.Timer to schedule a task
 * to execute once 1 seconds have passed.
 */

public class Reminder {
    Timer timer;
    long start = System.nanoTime();

    public Reminder(int seconds) {
        timer = new Timer();
        timer.schedule(new RemindTask(), 1000);
    }

    class RemindTask extends TimerTask {
        public void run() {
            long finish = System.nanoTime();
            long timeElapsed = finish - start;
            String elapsed_time="";
        }
    }

}
