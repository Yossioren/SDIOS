package com.SDIOS.ServiceControl;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SDIOS.ServiceControl.PackageManagerClient.GetOptions;
import com.SDIOS.ServiceControl.PackageManagerClient.ModelServer;
import com.SDIOS.ServiceControl.PackageManagerClient.UpdateServerAddress;
import com.SDIOS.ServiceControl.Recycler.ChooseUpdate;
import com.SDIOS.ServiceControl.Recycler.ClassifiersPackage;
import com.SDIOS.ServiceControl.Statistics.ScoreBoard;
import com.SDIOS.ServiceControl.dynamic_ui.UIItemManager;
import com.SDIOS.ServiceControl.utils.ContextHolder;
import com.SDIOS.ServiceControl.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity";
    private ModelServer modelServer;
    private ConfigurationManager configurationManager;
    private LinearLayout dynamic_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getString(R.string.service_demo_tag), "MainActivity thread id: " + Thread.currentThread().getId());
        setContentView(R.layout.activity_main);
        ContextHolder.set_context(this);
        configurationManager = new ConfigurationManager(new FileUtils(this));
        modelServer = new ModelServer(configurationManager);
        dynamic_layout = findViewById(R.id.dynamic_user_config_layout);
        findViewById(R.id.update_model).setOnClickListener(this);
        findViewById(R.id.update_sdios_server).setOnClickListener(this);
        findViewById(R.id.score_board).setOnClickListener(this);
        updateUI_Configuration();
    }

    public void updateUI_Configuration() {
        Log.d(TAG, "Running updateUI_Configuration");
        JSONObject userSettings = configurationManager.getUserSettings();
        dynamic_layout.removeAllViewsInLayout();
        set_dynamic_layout(userSettings);
    }

    private void set_dynamic_layout(JSONObject userSettings) {
        try {
            JSONObject user_config = userSettings.getJSONObject("user_config");
            List<UIItemManager> list = new LinkedList<>();
            for (Iterator<String> it = user_config.keys(); it.hasNext(); )
                add_dynamic_item(list, user_config, it.next());
            list.sort(Comparator.comparing(UIItemManager::item_key));
            list.forEach(item -> dynamic_layout.addView(item.build()));
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject: " + e);
        }
    }

    private void add_dynamic_item(List<UIItemManager> list, JSONObject userSettings, String key) {
        try {
            JSONArray array = userSettings.getJSONArray(key);
            for (int i = 0; i < array.length(); i++)
                list.add(new UIItemManager(this, key, i, array.getJSONObject(i)));
        } catch (JSONException e) {
            Log.e(TAG, "Fail to load views in the dynamic layout: " + e);
        }
    }

    public void openRollMenu(List<ClassifiersPackage> packages) {
        Log.d(TAG, "Open packages menu");
        if (packages == null) {
            Toast.makeText(getApplicationContext(), "Fail to connect to server", Toast.LENGTH_SHORT).show();
        }
        ChooseUpdate.mainActivity = new WeakReference<>(this);
        ChooseUpdate.packages = packages;
        ChooseUpdate.modelServer = this.modelServer;
        Intent intent = new Intent(this, ChooseUpdate.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.update_model) {
            Log.i(TAG, "update model");
            GetOptions get_options = new GetOptions(this, this.modelServer, this.configurationManager.getUsedPackageName());
            get_options.execute();
        } else if (view.getId() == R.id.update_sdios_server) {
            Log.i(TAG, "update SDIOS server address");
            Intent intent = new Intent(this, UpdateServerAddress.class);
            startActivity(intent);
        } else if (view.getId() == R.id.score_board) {
            Log.i(TAG, "score board");
            Intent intent = new Intent(this, ScoreBoard.class);
            startActivity(intent);
        }
        else {
            Log.i(TAG, "Unknown view " + view.getId() + ", " + view);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI_Configuration();
    }
}