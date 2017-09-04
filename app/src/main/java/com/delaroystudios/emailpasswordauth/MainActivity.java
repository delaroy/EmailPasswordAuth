package com.delaroystudios.emailpasswordauth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

        private AccessTokenTracker accessTokenTracker;

        private CallbackManager callbackManager;
        private FirebaseAuth fbAuth;
        private FirebaseAuth.AuthStateListener authListener;

        private LoginButton loginButton;
        private TextView emailText;
        private TextView statusText;
        private ImageView imageView;

        private static final String TAG = "FacebookAuth";

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            callbackManager = CallbackManager.Factory.create();

            accessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(
                        AccessToken oldAccessToken,
                        AccessToken currentAccessToken) {

                    if (currentAccessToken == null) {
                        fbAuth.signOut();
                    }
                }
            };

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            emailText = (TextView) findViewById(R.id.emailText);
            statusText = (TextView) findViewById(R.id.statusText);
            imageView = (ImageView) findViewById(R.id.profileImage);
            loginButton = (LoginButton) findViewById(R.id.loginButton);

            loginButton.setReadPermissions("email", "public_profile");

            loginButton.registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {

                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            Log.d(TAG, "onSuccess: " + loginResult);
                            exchangeAccessToken(loginResult.getAccessToken());
                        }

                        @Override
                        public void onCancel() {
                            Log.d(TAG, "onCancel: User cancelled sign-in");
                        }

                        @Override
                        public void onError(FacebookException error) {
                            Log.d(TAG, "onError: " + error);
                        }
                    });

            fbAuth = FirebaseAuth.getInstance();

            authListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        emailText.setText(user.getEmail());
                        statusText.setText("Signed In");

                        if (user.getPhotoUrl() != null) {
                            displayImage(user.getPhotoUrl());
                        }
                    } else {
                        emailText.setText("");
                        statusText.setText("Signed Out");
                        imageView.setImageResource(
                                R.drawable.com_facebook_profile_picture_blank_square);
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
        private void exchangeAccessToken(AccessToken token) {

            AuthCredential credential =
                    FacebookAuthProvider.getCredential(token.getToken());

            fbAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (!task.isSuccessful()) {
                                Toast.makeText(MainActivity.this,
                                        "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        void displayImage(Uri imageUrl) {
            new DownloadImageTask((ImageView) findViewById(R.id.profileImage))
                    .execute(imageUrl.toString());
        }

    private List<AuthUI.IdpConfig> getProviderList() {

        List<AuthUI.IdpConfig> providers = new ArrayList<>();

        providers.add(new
                AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
        providers.add(new
                AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        providers.add(new
                AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());

        return providers;
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