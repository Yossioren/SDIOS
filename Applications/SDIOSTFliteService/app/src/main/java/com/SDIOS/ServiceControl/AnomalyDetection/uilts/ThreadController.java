package com.SDIOS.ServiceControl.AnomalyDetection.uilts;

import android.util.Log;

public class ThreadController implements AutoCloseable {
    /*
    This class can manage a long running runnable as thread, to start and to stop it.
    This thread controller is not thread safe
    */
    private final static String TAG = "ThreadController";
    private static final int STATUS_UNINITIALIZED = 0;
    private static final int STATUS_FAIL = 1;
    private static final int STATUS_WAITING = 2;
    private static final int STATUS_RUNNING = 4;
    private static final int STATUS_DONE = 8;
    private static final int STATUS_STOPPING = 16;
    private Thread thread = null;
    private Runnable execute_on_finish = null;
    private int status = STATUS_UNINITIALIZED;
    private boolean closing = false;

    public void run(Runnable runnable) {
        Log.d(TAG, "Start runnable as new thread");
        assert execute_on_finish == null;
        execute_on_finish = runnable;
        close();
    }

    private void run_logic_after_close() {
        if (execute_on_finish == null) {
            Log.w(TAG, "Execute on finish is null!");
            return;
        }
        run_logic(execute_on_finish);
        execute_on_finish = null;
    }

    private void run_logic(Runnable runnable) {
        Log.d(TAG, "Attempting to create the thread");
        assert thread == null;
        status = STATUS_WAITING;
        thread = new Thread(() -> {
            status = STATUS_RUNNING;
            try {
                runnable.run();
                status = STATUS_DONE;
            } catch (Exception e) {
                Log.e(TAG, "Exception on thread run: " + e);
                status = STATUS_FAIL;
            }
            Log.w(TAG, "Runnable has exited");
        });
        thread.start();
    }

    @Override
    public void close() {
        if (status == STATUS_STOPPING || closing) {
            Log.w(TAG, "Thread controller close called more than once, ignoring");
            return;
        }
        if (thread != null) {
            closing = true;
            status = STATUS_STOPPING;
            new Thread(this::close_thread_logic).start();
        } else run_logic_after_close();
    }

    public void wait_for_thread_blocking() {
        Log.i(TAG, "Waiting for thread startup");
        while (closing || (status != STATUS_RUNNING && status != STATUS_FAIL && status != STATUS_DONE))
            Thread.yield();
        Log.i(TAG, "Thread has started up " + status);
    }

    private void close_thread_logic() {
        if (thread == null) {
            closing = false;
            run_logic_after_close();
            return;
        }
        if (thread.isAlive()) {
            Log.d(TAG, "Attempting to stop the thread");
            thread.interrupt();
            try {
                thread.join(3000);
            } catch (InterruptedException ignored) {
            }
        }
        closing = false;
        if (thread.isAlive()) {
            Log.e(TAG, "Thread has not stopped");
            throw new AssertionError("Thread has not stopped");
        } else {
            Log.d(TAG, "Thread stopped");
            thread = null;
            status = STATUS_UNINITIALIZED;
        }
        run_logic_after_close();
    }

    public void wait_for_previous_close_blocking() {
        Log.i(TAG, "Waiting for thread close");
        while (closing || status != STATUS_UNINITIALIZED || execute_on_finish != null)
            Thread.yield();
        Log.i(TAG, "Thread is ready to restart " + status);
    }

    public boolean is_status_running() {
        return status == STATUS_RUNNING;
    }

    public boolean is_status_failed() {
        return status == STATUS_FAIL;
    }
}
