package com.SDIOS.ServiceControl.PackageManagerClient;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.SDIOS.ServiceControl.ConfigurationManager;
import com.SDIOS.ServiceControl.R;
import com.SDIOS.ServiceControl.utils.FileUtils;

public class UpdateServerAddress extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_sdios_server);
        EditText server_dest = findViewById(R.id.server_dest);
        ConfigurationManager configurationManager = new ConfigurationManager(new FileUtils(this));
        server_dest.setText(configurationManager.getSDIOSServerAddress());
        Button apply = findViewById(R.id.apply);
        apply.setOnClickListener(view -> {
            configurationManager.setSDIOSServerAddress(server_dest.getText().toString());
            this.finish();
        });
    }
}