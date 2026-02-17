package com.SDIOS.ServiceControl.Recycler;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SDIOS.ServiceControl.AnomalyDetection.PackageParser;
import com.SDIOS.ServiceControl.AnomalyDetection.uilts.UserConfigManager;
import com.SDIOS.ServiceControl.ConfigurationManager;
import com.SDIOS.ServiceControl.MainActivity;
import com.SDIOS.ServiceControl.PackageManagerClient.DownloadPackage;
import com.SDIOS.ServiceControl.PackageManagerClient.ModelServer;
import com.SDIOS.ServiceControl.R;
import com.SDIOS.ServiceControl.Service.PassCommandsToService;
import com.SDIOS.ServiceControl.utils.FileUtils;

import java.lang.ref.WeakReference;
import java.util.List;

public class ChooseUpdate extends AppCompatActivity {
    private final static String TAG = "ChooseUpdate";
    public static ModelServer modelServer;
    public static WeakReference<MainActivity> mainActivity;
    public static List<ClassifiersPackage> packages;
    private static boolean package_picked;
    private PassCommandsToService serviceManager;
    private ConfigurationManager configurationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_update);
        serviceManager = PassCommandsToService.getInstance();
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        configurationManager = new ConfigurationManager(new FileUtils(this));
        package_picked = false;
        Button defaults = findViewById(R.id.use_defaults_button);
        defaults.setOnClickListener(view -> {
            configurationManager.setDefaultConfiguration();
            succeedDownloadPackage();
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // specify an adapter (see also next example)
        if (packages != null) {
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            RecyclerView.Adapter<PackageAdapter.MyViewHolder> mAdapter = new PackageAdapter(this, packages);
            recyclerView.setAdapter(mAdapter);
        }
    }

    public void getPackage(ClassifiersPackage chosen_package) {
        /*
            chosen_package.extract_default() works as a side-effect of loading the package
            with can_load_package where we initialize the static features of 'user_config'
         */
        if (!package_picked) {
            Toast.makeText(this, "Getting package", Toast.LENGTH_SHORT).show();
            UserConfigManager.clear_default();
            if (!can_load_package(chosen_package)) return;
            apply_package(chosen_package);
        } else {
            Toast.makeText(this, "Already downloading a package", Toast.LENGTH_SHORT).show();
        }
    }

    private void apply_package(ClassifiersPackage chosen_package) {
        package_picked = true;
        configurationManager.setPackageDefaultUserSettings(chosen_package.extract_default());
        configurationManager.addConfigurations(chosen_package.origin_json);
        DownloadPackage download_package = new DownloadPackage(this, modelServer);
        download_package.execute(chosen_package);
        configurationManager.setDefaultConfiguration(chosen_package.package_name);
    }

    private boolean can_load_package(ClassifiersPackage chosen_package) {
        Log.d(TAG, "Testing is package loadable");
        try {
            new PackageParser(chosen_package, true);
        } catch (Throwable e) {
            Log.e(TAG, "Could not load new package: " + e);
            Log.e(TAG, "New package json snippet: " + chosen_package.origin_json.toString().substring(0, 100));
            Toast.makeText(this, "Package is malformed!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void succeedDownloadPackage() {
        package_picked = false;
        this.onBackPressed();
        serviceManager.pushUpdate();
        if (mainActivity != null) mainActivity.get().updateUI_Configuration();
        else Log.e(TAG, "WeakReference is null!");
        Toast.makeText(this, "Installed package!", Toast.LENGTH_SHORT).show();
    }

    public void failDownloadPackage() {
        package_picked = false;
        Log.e(TAG, "Fail to download package!");
        Toast.makeText(this, "Failed tp download package!", Toast.LENGTH_SHORT).show();
    }
}
