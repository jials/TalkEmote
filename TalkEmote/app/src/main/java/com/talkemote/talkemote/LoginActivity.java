package com.talkemote.talkemote;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    AssetManager am;
    Typeface typefaceAppName, typefaceSplashMsg;
    TextView appName, splashScreenMessage;

    // Initiated Facebook SDK
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        loginButton = (LoginButton)findViewById(R.id.login_button);

//        LoginManager.getInstance().logInWithPublishPermissions(
//                LoginActivity.this,
//                Arrays.asList("publish_actions"));
        loginButton.setPublishPermissions("publish_actions");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //AccessToken accessToken = loginResult.getAccessToken();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        AssetManager am = getApplicationContext().getAssets();

        typefaceAppName = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/Source_Sans_Pro/%s", "SourceSansPro-Bold.ttf"));
        typefaceSplashMsg = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/Source_Sans_Pro/%s", "SourceSansPro-ExtraLight.ttf"));

        appName = (TextView) findViewById(R.id.app_name);
        appName.setTypeface(typefaceAppName);

        splashScreenMessage = (TextView) findViewById(R.id.splash_screen_message);
        splashScreenMessage.setTypeface(typefaceSplashMsg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
