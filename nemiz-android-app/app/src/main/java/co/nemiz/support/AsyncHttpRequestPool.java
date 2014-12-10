package co.nemiz.support;


import android.os.AsyncTask;

import com.loopj.android.http.RequestHandle;

import java.util.ArrayList;
import java.util.List;

public class AsyncHttpRequestPool {
    public static interface AsyncHttpRequestPoolListener {
        void onRequestsComplete();
    }

    private List<RequestHandle> handles = new ArrayList<>();
    private AsyncHttpRequestPoolListener listener;

    public void setListener(AsyncHttpRequestPoolListener listener) {
        this.listener = listener;
    }

    public void add(RequestHandle handle) {
        handles.add(handle);
    }

    public void waitForAll(long millis) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (isFinished()) {
                        if (listener != null) {
                            listener.onRequestsComplete();
                        }
                        break;
                    }
                    try {
                        // do not max out the CPU
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };
        thread.start();
        thread.join(millis);
    }

    private boolean isFinished() {
        int complete = 0;
        for (RequestHandle handle : handles) {
            if (handle.isFinished() || handle.isCancelled()) {
                complete++;
            }
        }
        return complete == handles.size();
    }
}
