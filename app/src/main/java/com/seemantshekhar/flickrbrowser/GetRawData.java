package com.seemantshekhar.flickrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK}

class GetRawData extends AsyncTask<String, Void, String > {
    private final OnDownloadComplete callBack;
    private static final String TAG = "GetRawData";
    private DownloadStatus downloadStatus;

    interface OnDownloadComplete{
        void onDownloadComplete(String s, DownloadStatus status);
    }

    public GetRawData(OnDownloadComplete callBack) {
        this.downloadStatus = DownloadStatus.IDLE;
        this.callBack = callBack;
    }

    void runInSameThread(String s){
        Log.d(TAG, "runInSameThread starts");
        callBack.onDownloadComplete(doInBackground(s), DownloadStatus.OK);
        Log.d(TAG, "runInSameThread ends");
    }

    @Override
    protected void onPostExecute(String s) {
        //Log.d(TAG, "onPostExecute: " + s);
        callBack.onDownloadComplete(s, downloadStatus);
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if(strings == null){
            downloadStatus = DownloadStatus.NOT_INITIALISED;
            return null;
        }

        try {
            downloadStatus = DownloadStatus.PROCESSING; // setting the status to processing.

            // setting a URL
            URL url = new URL(strings[0]);

            // establishing a HTTP connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int response = connection.getResponseCode();

            // initialising a string builder to store string
            StringBuilder result = new StringBuilder();

            // initialising a buffered reader to fetch data from the server
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // reading line by line and appending to the stringBuilder i.e. result
            for(String line = reader.readLine(); line != null; line = reader.readLine()){
                result.append(line).append("\n");
            }

            downloadStatus = DownloadStatus.OK; // setting the status to OK as the data was downloaded
            return result.toString();

        } catch (MalformedURLException e){
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch (IOException e){
            Log.e(TAG, "doInBackground: IO exception reading data: " + e.getMessage());
        } catch (SecurityException e){
            Log.e(TAG, "doInBackground: Security exception. Needs permission " + e.getMessage());
        } finally {
            // disconnecting the connection
            if(connection != null){
                connection.disconnect();
            }

            //closing the bufferedReader
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e){
                    Log.e(TAG, "doInBackground: Error closing reader " + e.getMessage());
                }
            }
        }
        downloadStatus = DownloadStatus.FAILED_OR_EMPTY; // setting the status to failed or empty as code stream reached here
        return null;
    }


}
