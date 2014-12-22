package co.nemiz;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

import co.nemiz.services.DefaultRestService;


public class NemizApplication extends Application implements Thread.UncaughtExceptionHandler {
    private final static String TAG = NemizApplication.class.getSimpleName();

    private Thread.UncaughtExceptionHandler defaultExceptionHandler;

    private File crashOutputFile;

    @Override
    public void onCreate() {
        super.onCreate();

        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        crashOutputFile = new File(Environment.getExternalStoragePublicDirectory("Nemiz"), "crash.report");

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        writer.println("Unhandled exception: " + ex.getMessage());
        writer.println("Date:" + Calendar.getInstance().getTime());
        writer.println("ThreadInfo:" + thread.getId() + "/" + thread.getName());

        writer.println("----------------------------------------");
        ex.printStackTrace(writer);

        final String content = stringWriter.toString();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                sendToServer(content);
                return null;
            }
        }.execute();
        writeToFile(content);

        defaultExceptionHandler.uncaughtException(thread, ex);
    }

    private void writeToFile(String content) {
        if (crashOutputFile.getParentFile() != null
                && !crashOutputFile.getParentFile().exists()) {
            crashOutputFile.mkdirs();
        }
        if (crashOutputFile.exists()) {
            crashOutputFile.delete();
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(crashOutputFile);
            IOUtils.write(content, outputStream);
        } catch (IOException ex) {
            Log.e(TAG, "Failed to write crash report", ex);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void sendToServer(final String content) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(DefaultRestService.absoluteUrl("/report"));
        try {
            httpPost.setEntity(new StringEntity(content, "UTF-8"));
            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
