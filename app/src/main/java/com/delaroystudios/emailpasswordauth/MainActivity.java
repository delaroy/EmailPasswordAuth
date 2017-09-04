package com.delaroystudios.emailpasswordauth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import android.support.annotation.NonNull;
import com.google.firebase.auth.FirebaseUser;

import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.InputStream;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.TwitterAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TWITTER_KEY = "SmxH5mxnQ1mT5vJrZs9mSbJ6Y";
    private static final String TWITTER_SECRET = "ihqB0qyBqJBxIGfaZjpePDtBWJY674irNo6eMMMwRlrCjTw6CY";

    private static final String TAG = "TwitterAuth";

    private FirebaseAuth fbAuth;
    private FirebaseAuth.AuthStateListener authListener;

    private TwitterLoginButton loginButton;
    private TextView userText;
    private TextView statusText;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);


        setContentView(R.layout.activity_main);

        userText = (TextView) findViewById(R.id.userText);
        statusText = (TextView) findViewById(R.id.statusText);
        imageView = (ImageView) findViewById(R.id.profileImage);

        loginButton = (TwitterLoginButton) findViewById(R.id.loginButton);

        loginButton.setCallback(new Callback<TwitterSession>() {

            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "loginButton Callback: Success");
                exchangeTwitterToken(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "loginButton Callback: Failure " +
                        exception.getLocalizedMessage());
            }
        });

        fbAuth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(
                    @NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    userText.setText(user.getDisplayName());
                    statusText.setText("Signed In");

                    if (user.getPhotoUrl() != null) {
                        displayImage(user.getPhotoUrl());
                    }
                } else {
                    userText.setText("");
                    statusText.setText("Signed Out");
                    imageView.setImageResource(
                            R.drawable.tw__composer_logo_blue);

                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        fbAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            fbAuth.removeAuthStateListener(authListener);
        }
    }

    private void exchangeTwitterToken(TwitterSession session) {

        TwitterAuthClient authClient = new TwitterAuthClient();
        authClient.requestEmail(session, new Callback<String>() {
            @Override
            public void success(Result<String> result) {
                Log.d(TAG, "EMAIL = " + result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential",
                                    task.getException());
                            Toast.makeText(MainActivity.this,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signOut(View view) {
        fbAuth.signOut();
        //Twitter.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    void displayImage(Uri imageUrl) {
        new DownloadImageTask((ImageView) findViewById(R.id.profileImage))
                .execute(imageUrl.toString());
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}