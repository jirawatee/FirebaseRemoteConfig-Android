package com.example.config;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import static com.example.config.R.id.webview;

public class TMNActivity extends AppCompatActivity implements View.OnClickListener{
	private static final String CONFIG_KEY_IS_PROMOTION = "is_promotion_on";
	private static final String CONFIG_KEY_TMN_PICTURE = "tmn_picture";
	private static final String CONFIG_KEY_TMN_URL = "tmn_url";

	private FirebaseRemoteConfig mFirebaseRemoteConfig;
	private ImageView mImageView;
	private ProgressBar mProgressBar;
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tmn);
		bindWidget();
		MyWebSetting();

		FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings
				.Builder()
				.setDeveloperModeEnabled(BuildConfig.DEBUG)
				.build();

		mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
		mFirebaseRemoteConfig.setConfigSettings(configSettings);
		mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

		fetchData();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.banner) {
			mImageView.setVisibility(View.GONE);
			mWebView.setVisibility(View.VISIBLE);

			String url = mFirebaseRemoteConfig.getString(CONFIG_KEY_TMN_URL);

			Uri.Builder builder = new Uri.Builder();
			builder.appendQueryParameter("tmn_id", "12345");
			builder.appendQueryParameter("email", "jirawatee@gmail.com");
			builder.appendQueryParameter("mobile", "0891477794");
			builder.appendQueryParameter("thaiid", "1212121212121");

			String postData = builder.build().toString().substring(1);
			mWebView.postUrl(url, postData.getBytes());
		}
	}

	@Override
	public void onBackPressed() {
		if (mWebView.canGoBack()) {
			mWebView.goBack();
		} else {
			super.onBackPressed();
		}
	}

	private void bindWidget() {
		mProgressBar = findViewById(R.id.progressBar);
		mImageView = findViewById(R.id.banner);
		mImageView.setOnClickListener(this);
		mWebView = findViewById(webview);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void MyWebSetting() {
		WebSettings ws = mWebView.getSettings();
		ws.setJavaScriptEnabled(true);
		ws.setJavaScriptCanOpenWindowsAutomatically(true);
		ws.setDomStorageEnabled(true);
		ws.setUseWideViewPort(true);
		ws.setLoadWithOverviewMode(true);
		ws.setBuiltInZoomControls(true);
		ws.setSupportZoom(true);
		ws.setDisplayZoomControls(false);
		mWebView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				setTitle("Loading...");
				mProgressBar.setVisibility(View.VISIBLE);
				mProgressBar.setProgress(newProgress);
				if (newProgress == 100) {
					setTitle(R.string.app_name);
					mProgressBar.setVisibility(View.INVISIBLE);
				}
			}
		});
		mWebView.setWebViewClient(new WebViewClient());
	}

	private void fetchData() {
		long cacheExpiration = 3600;
		if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
			cacheExpiration = 0;
		}
		mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful()) {
					mFirebaseRemoteConfig.activateFetched();
				}
				if (mFirebaseRemoteConfig.getBoolean(CONFIG_KEY_IS_PROMOTION)) {
					Glide.with(TMNActivity.this).load(mFirebaseRemoteConfig.getString(CONFIG_KEY_TMN_PICTURE)).thumbnail(0.1f).into(mImageView);
				} else {
					mImageView.setVisibility(View.GONE);
					Toast.makeText(TMNActivity.this, "No campaign available", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}