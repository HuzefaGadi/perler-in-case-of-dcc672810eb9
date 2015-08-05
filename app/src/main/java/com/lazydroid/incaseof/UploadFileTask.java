package com.lazydroid.incaseof;



import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;

public class UploadFileTask extends AsyncTask<String, Integer, String> {
    private final static String TAG = "UploadFileTask";

    @Override
    protected String doInBackground(String... filenames) {
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.e(TAG, "UPLOAD ERROR: " + result);
            Toast.makeText(InCaseOfApp.getContext(), "UPLOAD ERROR: " + result, Toast.LENGTH_LONG).show();
        }
    }

    private void flushContent(BufferedReader reader) throws IOException {
        char[] buf = new char[200];            // empty the buffer to avoid: "Invalid use of
        while (reader.read(buf) != -1) ;    // SingleClientConnManager: connection still allocated"
    }
}
