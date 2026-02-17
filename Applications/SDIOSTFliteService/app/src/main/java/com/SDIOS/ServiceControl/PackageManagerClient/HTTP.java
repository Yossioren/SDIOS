package com.SDIOS.ServiceControl.PackageManagerClient;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

class HTTP {
    private final static String TAG = "HTTP";

    @NonNull
    private static HttpURLConnection getConnection(String address, String method) throws IOException {
        URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        connection.setRequestMethod(method);
        connection.setUseCaches(false);
        return connection;
    }

    private static String getResponse(InputStream inputStream) throws IOException, TimeoutException {
        if (inputStream == null)
            return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        try {
            char[] buffer = new char[4096];
            int read_length;
            while ((read_length = reader.read(buffer)) != -1)
                response.append(buffer, 0, read_length);
        } finally {
            reader.close();
            inputStream.close();
        }
        Log.d(TAG, "got response: " + response.length());
        return response.toString();
    }

    private static void writeFile(Context context, String file, InputStream input) throws IOException {
        try (OutputStream output = context.openFileOutput(file, Context.MODE_PRIVATE)) {
            byte[] buffer = new byte[4096];
            int read_length;
            while ((read_length = input.read(buffer)) != -1)
                output.write(buffer, 0, read_length);
        } finally {
            input.close();
        }
    }

    static String send(String address, String method, String body) {
        HttpURLConnection connection = null;
        try {
            connection = send_data(address, method, body);
            assert connection != null;
            return getResponse(connection.getInputStream());
        } catch (IOException | TimeoutException | AssertionError e) {
            Log.e(TAG, "Error on send message to:" + address + ", " + e);
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    private static HttpURLConnection send_data(String address, String method, String body) {
        try {
            HttpURLConnection connection = getConnection(address, method);
            if (body != null && !body.isEmpty()) {
                OutputStream outputStream = connection.getOutputStream();
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
                outputStream.close();
            }
            return connection;
        } catch (IOException e) {
            Log.e(TAG, "Error on send message to:" + address + ", " + e);
        }
        return null;
    }

    static boolean download_file(Context context, String address, String method, String body, String save_path) {
        HttpURLConnection connection = null;
        try {
            connection = send_data(address, method, body);
            assert connection != null;
            writeFile(context, save_path, connection.getInputStream());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error on file download from:" + address + ", " + e);
            return false;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }
}
