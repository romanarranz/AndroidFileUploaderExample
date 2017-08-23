package com.github.romanarranz.androidfileuploaderexample.network;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.github.romanarranz.androidfileuploaderexample.R;
import com.github.romanarranz.androidfileuploaderexample.events.NotificationProgress;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by romanarranzguerrero on 23/8/17.
 */

public class FileUploaderLoader extends AsyncTaskLoader<Integer> {

    private static final String LOG_TAG = FileUploaderLoader.class.getSimpleName();

    private String mUrl, mSelectedFilePath;
    private NotificationProgress mNP;

    public FileUploaderLoader(Context context, String url, String filepath) {
        super(context);
        mUrl = url;
        mSelectedFilePath = filepath;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Integer loadInBackground() {
        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream outStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAviables, bufferSize;
        byte[] buffer;
        int maxBufferSize = 2 * 1024 * 1024; // 2MB
        File selectedFile = new File(mSelectedFilePath);

        String[] parts = mSelectedFilePath.split("/");
        final String filename = parts[parts.length - 1];

        if (!selectedFile.isFile()) {
            return 0;
        } else {
            try {
                FileInputStream inputStream = new FileInputStream(selectedFile);
                URL url = new URL(mUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", mSelectedFilePath);

                // crear nuevo outputstream
                outStream = new DataOutputStream(connection.getOutputStream());

                // escribir los bytes al outputstream
                outStream.writeBytes(twoHyphens + boundary + lineEnd);
                outStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + mSelectedFilePath + "\"" + lineEnd);
                outStream.writeBytes(lineEnd);

                // devolver numero de bytes presentes en el inputstream
                bytesAviables = inputStream.available();

                // seleccionar el tamaño del buffer como minimo del tamaño del archivo o de 2Mb
                bufferSize = Math.min(bytesAviables, maxBufferSize);

                // establecer el buffer como un array de bytes
                buffer = new byte[bufferSize];

                // leer bytes del inputstream desde el indice 0 hasta el bufferSize
                bytesRead = inputStream.read(buffer, 0, bufferSize);

                mNP = new NotificationProgress(getContext(), getContext().getString(R.string.app_name), R.drawable.ic_android);
                mNP.setProgressText("Uploading...");
                mNP.setFinishText("Upload ended");
                mNP.setMax(bufferSize);
                mNP.start();

                // recorrer hasta bytesRead = -1, es decir hasta que no queden bytes
                while (bytesRead > 0) {
                    // escribir los bytes leidos del inputstream
                    outStream.write(buffer, 0, bufferSize);
                    bytesAviables = inputStream.available();
                    bufferSize = Math.min(bytesAviables, maxBufferSize);
                    bytesRead = inputStream.read(buffer, 0, bufferSize);

                    mNP.update(bytesRead);
                }

                mNP.stop();

                outStream.writeBytes(lineEnd);
                outStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(LOG_TAG, "Server response: " + serverResponseMessage + " : status " + serverResponseCode);

                // cerrar flujos
                inputStream.close();
                outStream.flush();
                outStream.close();

            } catch (FileNotFoundException e) {
                Log.e(LOG_TAG, "Archivo no encontrado: " + e.getMessage());
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "URL error: " + e.getMessage());
            } catch (IOException e) {
                Log.e(LOG_TAG, "No se pudo leer/escribir el archivo: " + e.getMessage());
            }
        }

        return serverResponseCode;
    }
}
