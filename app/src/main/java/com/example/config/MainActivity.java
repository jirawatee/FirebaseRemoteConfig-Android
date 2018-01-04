package com.example.config;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {
    // Remote Config keys
    private static final String CONFIG_KEY_PRICE = "price";
    private static final String CONFIG_KEY_DISCOUNT = "discount";
    private static final String CONFIG_KEY_IS_PROMOTION = "is_promotion_on";

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private TextView mPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPriceTextView = (TextView) findViewById(R.id.txt);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings
                .Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        // Set default Remote Config values. In general you should have in app defaults for all
        // values that you may configure using Remote Config later on. The idea is that you
        // use the in app defaults and when you need to adjust those defaults, you set an updated
        // value in the App Manager console. Then the next time you application fetches from the
        // server, the updated value will be used. You can set defaults via an xml file like done
        // here or you can set defaults inline by using one of the other setDefaults methods.
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        fetchDiscount();

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchDiscount();
            }
        });

        findViewById(R.id.btn).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivity(new Intent(MainActivity.this, TMNActivity.class));
                return false;
            }
        });
    }

    private void fetchDiscount() {
        mPriceTextView.setText(R.string.loading);

        long cacheExpiration = 3600; // 1 hour in seconds.

        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from the server.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        // cacheExpirationSeconds is set to cacheExpiration here, indicating that any previously
        // fetched and cached config would be considered expired because it would have been fetched
        // more than cacheExpiration seconds ago. Thus the next fetch would go to the server unless
        // throttling is in progress. The default expiration duration is 43200 (12 hours).
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("FETCH", getString(R.string.succeeded));
                    // Once the config is successfully fetched it must be activated before newly fetched values are returned.
                    mFirebaseRemoteConfig.activateFetched();
                } else {
                    Log.d("FETCH", getString(R.string.failed));
                }
                displayPrice();
            }
        });
    }

    private void displayPrice() {
        long initialPrice = mFirebaseRemoteConfig.getLong(CONFIG_KEY_PRICE);
        long finalPrice = initialPrice;
        Log.d("PROMOTION", String.valueOf(mFirebaseRemoteConfig.getBoolean(CONFIG_KEY_IS_PROMOTION)));
        Log.d("DISCOUNT", String.valueOf(mFirebaseRemoteConfig.getLong(CONFIG_KEY_DISCOUNT)));
        if (mFirebaseRemoteConfig.getBoolean(CONFIG_KEY_IS_PROMOTION)) {
            finalPrice = initialPrice - mFirebaseRemoteConfig.getLong(CONFIG_KEY_DISCOUNT);
        }
        mPriceTextView.setText(getString(R.string.price_prefix, finalPrice + ""));
    }
}