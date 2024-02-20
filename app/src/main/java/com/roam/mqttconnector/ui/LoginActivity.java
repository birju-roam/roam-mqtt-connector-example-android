package com.roam.mqttconnector.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.roam.mqttconnector.R;
import com.roam.mqttconnector.databinding.ActivityLoginBinding;
import com.roam.mqttconnector.util.Preferences;
import com.roam.sdk.Roam;
import com.roam.sdk.callback.RoamCallback;
import com.roam.sdk.models.RoamError;
import com.roam.sdk.models.RoamUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
    }

    private void initialize() {

        binding.btWithoutUser.setOnClickListener(this);
        binding.btWithUser.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btWithUser:
                login();
                break;

            case R.id.btWithoutUser:
                mainActivityIntent();
                break;
        }
    }

    private void login() {
        if(TextUtils.isEmpty(Preferences.getUserId(this))){
            show();
            Roam.createUser("ROAM_USER", null, new RoamCallback() {
                @Override
                public void onSuccess(RoamUser roamUser) {
                    hide();
                    Log.d("TAG", "createUser: "+roamUser.getUserId() );
                    Preferences.setUserId(LoginActivity.this,roamUser.getUserId());
                    mainActivityIntent();
                }

                @Override
                public void onFailure(RoamError roamError) {
                    hide();
                    showMsg(roamError.getMessage());
                }
            });
        }else {
            show();
            Roam.getUser(Preferences.getUserId(this), new RoamCallback() {
                @Override
                public void onSuccess(RoamUser roamUser) {
                    hide();
                    Log.d("TAG", "getUser: "+roamUser.getUserId() );
                    mainActivityIntent();
                }

                @Override
                public void onFailure(RoamError roamError) {
                    hide();
                    showMsg(roamError.getMessage());
                }
            });
        }

    }

    private void mainActivityIntent() {
        startActivity(new Intent(this, MapActivity.class));
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void show() {
        binding.progressbar.setVisibility(View.VISIBLE);
    }

    private void hide() {
        binding.progressbar.setVisibility(View.GONE);
    }

}