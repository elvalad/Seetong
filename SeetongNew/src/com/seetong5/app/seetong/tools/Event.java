package com.seetong5.app.seetong.tools;

/**
 * Created by Administrator on 2014-06-13.
 */
public class Event {
    public final boolean timedWait(long timeout) throws InterruptedException {
        long start = System.currentTimeMillis ();
        synchronized (this) {
            this.wait(timeout);
            long now = System.currentTimeMillis ();
            long timeSoFar = now - start;
            return timeSoFar >= timeout;
        }
    }
}
