package com.SDIOS.ServiceControl.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {
    private final static String TAG = "FileUtils";
    private final Context context;

    public FileUtils(Context context) {
        this.context = context;
    }

    public static FileUtils get_instance() throws AssertionError {
        Context stored_context = ContextHolder.get();
        assert stored_context != null;
        return new FileUtils(stored_context);
    }

    public void writeToFile(String data, String path) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(path, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e);
        }
    }

    @NonNull
    public JSONObject getJsonFile(String path) {
        try {
            return new JSONObject(this.readFileString(path));
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject: " + e);
        }
        return new JSONObject();
    }

    public String readFileString(String path) {
        try {
            InputStream inputStream = context.openFileInput(path);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                return stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e);
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e);
        }
        return "";
    }

    public ByteBuffer readFileBinary(String path) {
        try {
            ByteBuffer output;
            try (FileInputStream inputStream = context.openFileInput(path);
                 FileChannel fileChannel = inputStream.getChannel()) {
                output = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            }
            return output;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e);
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e);
        }
        return null;
    }

    public void copyFile(String src, String dst) {
        Log.d(TAG, String.format("Copy file from %s to %s", src, dst));
        try (InputStream in = context.openFileInput(src)) {
            try (OutputStream out = context.openFileOutput(dst, Context.MODE_PRIVATE)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
            }
        } catch (IOException e) {
            Log.e(TAG, "Can not copy file: " + e);
        }
    }
}
