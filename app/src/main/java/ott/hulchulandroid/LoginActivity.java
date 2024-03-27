package ott.hulchulandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import com.clevertap.android.sdk.CleverTapAPI;
import com.facebook.login.Login;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import ott.hulchulandroid.constant.Constant;
import ott.hulchulandroid.database.DatabaseHelper;
import ott.hulchulandroid.network.RetrofitClient;
import ott.hulchulandroid.network.apis.FirebaseAuthApi;
import ott.hulchulandroid.network.apis.LoginApi;
import ott.hulchulandroid.network.apis.SubscriptionApi;
import ott.hulchulandroid.network.model.ActiveStatus;
import ott.hulchulandroid.network.model.User;
import ott.hulchulandroid.utils.ApiResources;
import ott.hulchulandroid.utils.Constants;
import ott.hulchulandroid.utils.RtlUtils;
import ott.hulchulandroid.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    FirebaseAuth firebaseAuth;
    private EditText etEmail, etPass;
    private TextView tvSignUp, tvReset, txt_error;
    private AppCompatButton btnLogin;
    private ProgressDialog dialog;
    private View backgroundView;
    private CardView cardView, card_facebook, card_google;
    private ProgressBar progressBar;
    private ImageView phoneAuthButton, facebookAuthButton, googleAuthButton;
    private DatabaseHelper databaseHelper;
    private String deviceId = "";
    private String firebaseToken = "";
    String email = "";
    TextView tvsignup;
    CountryCodePicker ccp;
    private FirebaseAuth mAuth;
    CleverTapAPI clevertapDefaultInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);


        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);
        isDark = true;
        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());

        databaseHelper = new DatabaseHelper(LoginActivity.this);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "login_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        etEmail = findViewById(R.id.email);
        tvsignup = findViewById(R.id.tvsignup);
        etPass = findViewById(R.id.password);
        tvSignUp = findViewById(R.id.signup);
        btnLogin = findViewById(R.id.signin);
        tvReset = findViewById(R.id.reset_pass);
        backgroundView = findViewById(R.id.background_view);
//        cardView = findViewById(R.id.card_view);
        progressBar = findViewById(R.id.progress_bar);
        phoneAuthButton = findViewById(R.id.phoneAuthButton);
        facebookAuthButton = findViewById(R.id.facebookAuthButton);
        googleAuthButton = findViewById(R.id.googleAuthButton);
        card_facebook = findViewById(R.id.card_facebook);
        card_google = findViewById(R.id.card_google);
        txt_error = findViewById(R.id.txt_error);
        ccp = findViewById(R.id.ccp);


        tvsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        if (AppConfig.ENABLE_FACEBOOK_LOGIN) {
//            facebookAuthButton.setVisibility(View.VISIBLE);
            card_facebook.setVisibility(View.VISIBLE);
        }
        if (AppConfig.ENABLE_GOOGLE_LOGIN) {
//            googleAuthButton.setVisibility(View.VISIBLE);
            card_google.setVisibility(View.VISIBLE);
        }

//        if (AppConfig.ENABLE_PHONE_LOGIN) {
//            phoneAuthButton.setVisibility(View.VISIBLE);
//        }

        firebaseAuth = FirebaseAuth.getInstance();

      /*  if (isDark) {
            backgroundView.setBackgroundColor(getResources().getColor(R.color.nav_head_bg));
            btnLogin.setBackground(ContextCompat.getDrawable(this, R.drawable.login_field_button_dark));
            cardView.setCardBackgroundColor(getResources().getColor(R.color.black));
        }*/


        tvReset.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, PassResetActivity.class)));


        btnLogin.setOnClickListener(v -> {

            /*if (!isValidEmailAddress(etEmail.getText().toString())) {
                new ToastMsg(LoginActivity.this).toastIconError("Please enter valid email");
            } else*/
//                if (et.getText().toString().equals("")) {
//                    new ToastMsg(LoginActivity.this).toastIconError("Please enter password");
//                } else {

            Constant.googleEmail = "";
            Constant.googleName = "";

            checkMobile();


        });

        tvSignUp.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));

        card_google.setOnClickListener(v -> googleSignIn());

        card_facebook.setOnClickListener(v -> facebookSignIn());


        googleAuthButton.setOnClickListener(v -> googleSignIn());

        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);


//        etEmail.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (etEmail.getText().length() != 10) {
//                    txt_error.setVisibility(View.VISIBLE);
//                    txt_error.setText("Mobile number should be valid");
//                    card_facebook.setVisibility(View.VISIBLE);
//                    card_google.setVisibility(View.VISIBLE);
//                    btnLogin.setVisibility(View.GONE);
//                } else {
//                    txt_error.setVisibility(View.GONE);
//                    btnLogin.setVisibility(View.VISIBLE);
//                    card_facebook.setVisibility(View.GONE);
//                    card_google.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
    }


    private void login(String email, final String password) {
        btnLogin.setClickable(false);
        dialog.show();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);
        Call<User> call = api.postLoginStatus(AppConfig.API_KEY, email, password, deviceId, firebaseToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        ApiResources.USER_PHONE = user.getPhone();

                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();


                        //save user login time, expire time
                        updateSubscriptionStatus(db.getUserData().getUserId());
                    } else if (response.body().getStatus().equalsIgnoreCase("0")) {
                        btnLogin.setClickable(true);
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage("Already Login in other device.Do you want to change device?")
                                .setCancelable(false)
                                .setNegativeButton("Cancel", (dialog, id) -> {
                                    finishAffinity();
                                    dialog.dismiss();
                                })
                                .setPositiveButton("OK", (dialog1, id) -> {
                                    dialog1.dismiss();
                                    dialog.dismiss();
//                                    changeDeviceSignIn();
                                    sendOtp();
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        new ToastMsg(LoginActivity.this).toastIconError(response.body().getData());
                        dialog.dismiss();
                        btnLogin.setClickable(true);
                    }
                } else {
                    dialog.cancel();
                    new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
                    btnLogin.setClickable(true);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                dialog.cancel();
                new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
                btnLogin.setClickable(true);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }


    public void updateSubscriptionStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
//                    Log.d("subscription", "" + response.body().toString());
                    if (response.body() != null) {
                        ActiveStatus activeStatus = response.body();

                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                        db.deleteAllActiveStatusData();
                        db.insertActiveStatusData(activeStatus);

                        GoogleSignInOptions gso = new GoogleSignInOptions.
                                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                                build();

                        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
                        googleSignInClient.signOut();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        startActivity(intent);
                        finish();
                        dialog.cancel();

                    }
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /*social login related task*/

    private void changeDeviceSignIn() {
//        progressBar.setVisibility(View.VISIBLE);
       /* if (firebaseAuth.getCurrentUser() != null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
                final String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                //already signed in
                if (!phoneNumber.isEmpty())
                    changeDevice();
            }

        } else {*/
//            progressBar.setVisibility(View.GONE);
        // Choose authentication providers
        // Create and launch sign-in intent
        btnLogin.setClickable(true);
        phoneAuthResultListenerchangeDevice.launch(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.PhoneBuilder().build()
                )).build());
//        }
    }

    ActivityResultLauncher<Intent> phoneAuthResultListenerchangeDevice = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (!user.getPhoneNumber().isEmpty()) {
                            btnLogin.setClickable(true);

                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (task.isComplete()) {
                                        firebaseToken = task.getResult();
                                        Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);
//                                        sendDataToServer();
                                        changeDevice();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                            progressBar.setVisibility(View.GONE);
                            btnLogin.setClickable(true);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                        progressBar.setVisibility(View.GONE);
                        btnLogin.setClickable(true);
                    }
                }
            }
    );

    /*social login related task*/

    private void phoneSignIn() {
       /* progressBar.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
                final String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                //already signed in
                if (!phoneNumber.isEmpty())
                    sendDataToServer();
            }

        } else {
            progressBar.setVisibility(View.GONE);
            // Choose authentication providers
            // Create and launch sign-in intent
            phoneAuthResultListener.launch(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.PhoneBuilder().build()
                    )).build());
        }*/
        startActivity(new Intent(LoginActivity.this, LoginViaMobileActivity.class));
    }

    ActivityResultLauncher<Intent> phoneAuthResultListener = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (!user.getPhoneNumber().isEmpty()) {

                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (task.isComplete()) {
                                        firebaseToken = task.getResult();
                                        Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);
                                        sendDataToServer();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> facebookAuthResultListener = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (!user.getUid().isEmpty()) {
                            String username = user.getDisplayName();
                            String photoUrl = String.valueOf(user.getPhotoUrl());
                            email = user.getEmail();
                            if (email == null) {
                                email = "";
                            }

                            Constant.googleEmail = email;
                            Constant.googleName = username;

                            String phone = "";
                            if (user.getPhoneNumber() != null)
                                phone = user.getPhoneNumber();

//                            if (phone.equals("")) {
//                                progressBar.setVisibility(View.GONE);
//                                startActivity(new Intent(LoginActivity.this, LoginSocialNumberNotFoundActivity.class));
//
//                            } else {
                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                                if (task.isComplete()) {
                                    firebaseToken = task.getResult();
                                    Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);
//                                        sendDataToServer();
                                    sendFacebookDataToServer(username, photoUrl, email);
                                }
                            });

//                            }


                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.something_went_text) + result.getResultCode(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
    );


    ActivityResultLauncher<Intent> googleAuthResultListener = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (!user.getUid().isEmpty()) {
                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (task.isComplete()) {
                                        firebaseToken = task.getResult();
                                        Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);
//                                        sendDataToServer();

//                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                                        String username = user.getDisplayName();
//                                        String email = user.getEmail();
//                                        Uri image = user.getPhotoUrl();
//                                        String uid = user.getUid();
//
//                                        Constant.googleEmail = email;
//                                        Constant.googleName = username;
//
//                                        String phone = "";
//                                        if (user.getPhoneNumber() != null)
//                                            phone = user.getPhoneNumber();
//
//                                        if (phone.equals("")) {
//                                            progressBar.setVisibility(View.GONE);
//
//
//                                            startActivity(new Intent(LoginActivity.this, LoginSocialNumberNotFoundActivity.class));
//
//                                        } else {
                                        sendGoogleDataToServer();
//                                        }
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
    );


    private void facebookSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
                //already signed in
                //send data to server
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (task.isComplete()) {
                        firebaseToken = task.getResult();
                        Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);
//                            sendDataToServer();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        String username = user.getDisplayName();
                        String email = user.getEmail();

                        Constant.googleEmail = email;
                        Constant.googleName = username;

                        String phone = "";
                        if (user.getPhoneNumber() != null)
                            phone = user.getPhoneNumber();

//                        if (phone.equals("")) {
//                            progressBar.setVisibility(View.GONE);
//
//                            startActivity(new Intent(LoginActivity.this, LoginSocialNumberNotFoundActivity.class));
//
//                        } else {
                        sendFacebookDataToServer(user.getDisplayName(), String.valueOf(user.getPhotoUrl()), user.getEmail());
//                        }

                    }
                });


            }

        } else {
            progressBar.setVisibility(View.GONE);
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.FacebookBuilder()
                            //.setPermissions(Arrays.asList("email", "default"))
                            .build());
            // Create and launch sign-in intent
            facebookAuthResultListener.launch(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build()
            );
        }
    }


    private void googleSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {


            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isComplete()) {
                        firebaseToken = task.getResult();
                        Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);
//                        sendDataToServer();
//                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                        String phone = "";
//                        if (user.getPhoneNumber() != null)
//                            phone = user.getPhoneNumber();
//
//                        String username = user.getDisplayName();
//                        String email = user.getEmail();
//
//                        Constant.googleEmail = email;
//                        Constant.googleName = username;
//
//                        if (phone.equals("")) {
//                            progressBar.setVisibility(View.GONE);
////                            Toast.makeText(LoginActivity.this, "New account can not be created", Toast.LENGTH_SHORT).show();
//
//                            FirebaseAuth.getInstance().signOut();
//
//                            GoogleSignInOptions gso = new GoogleSignInOptions.
//                                    Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
//                                    build();
//
//                            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
//                            googleSignInClient.signOut();
//
//                            startActivity(new Intent(LoginActivity.this, LoginSocialNumberNotFoundActivity.class));
//
//                        } else {
                        sendGoogleDataToServer();
//                        }
                    }
                }
            });

        } else {
            progressBar.setVisibility(View.GONE);
            // Choose authentication providers
            GoogleSignInOptions googleOptions = new GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .requestProfile()
                    .build();


            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().setSignInOptions(googleOptions).build());

           /* List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build());*/

            // Create and launch sign-in intent
            googleAuthResultListener.launch(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false)
                    .build());
        }
    }

    private void sendDataToServer() {
        progressBar.setVisibility(View.VISIBLE);
        String phoneNo = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FirebaseAuthApi api = retrofit.create(FirebaseAuthApi.class);
        Call<User> call = api.getPhoneAuthStatus(AppConfig.API_KEY, uid, phoneNo, deviceId, firebaseToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equals("success")) {

                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        ApiResources.USER_PHONE = user.getPhone();

                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();
                        //save user login time, expire time
                        updateSubscriptionStatus(user.getUserId());
                    }

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                phoneSignIn();
            }
        });

    }

    private void changeDevice() {
//        progressBar.setVisibility(View.VISIBLE);
        String phoneNo = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        if (phoneNo.contains("+91")) {
            phoneNo = phoneNo.replace("+91", "");
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);
        Call<User> call = api.postChangeDeviceStatus(AppConfig.API_KEY, phoneNo, deviceId, firebaseToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equals("success")) {
//                        Toast.makeText(LoginActivity.this, deviceId, Toast.LENGTH_LONG).show();
                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        ApiResources.USER_PHONE = user.getPhone();


                        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_REGISTER_AGE, MODE_PRIVATE).edit();
                        editor.putString("user_register_age", response.body().getUser_age());
                        editor.apply();


                        SharedPreferences.Editor editor1 = getSharedPreferences(Constants.USER_PIN, MODE_PRIVATE).edit();
                        editor1.putString("user_pin", response.body().getPin());
                        editor1.apply();


                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();

                        //save user login time, expire time
                        updateSubscriptionStatus(db.getUserData().getUserId());
                    }

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
//                progressBar.setVisibility(View.GONE);
                changeDeviceSignIn();
            }
        });


    }

    public boolean validCellPhone(String number) {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }

    private void sendOtp() {
//        progressBar.setVisibility(View.VISIBLE);
        dialog.show();
  /*      String phoneNo = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        if (phoneNo.contains("+91")) {
            phoneNo = phoneNo.replace("+91", "");
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();*/
        String mobile = "", email = "";
        String key = etEmail.getText().toString();
        boolean validEmail = isValidEmailAddress(key);
        boolean validMobile = validCellPhone(key);
        if (validEmail) {
            email = etEmail.getText().toString();
            mobile = "";
        } else if (validMobile) {
            mobile = etEmail.getText().toString();
            email = "";
        }

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);
        Call<ResponseBody> call = api.login_otp(AppConfig.API_KEY, mobile, email);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            Toast.makeText(LoginActivity.this, jsonObject.getString("data"), Toast.LENGTH_SHORT).show();
                            String otp = jsonObject.getString("otp");
                            Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                            intent.putExtra("from", "device_change");
                            intent.putExtra("otp", otp);
//                            intent.putExtra("countryCode", ccp.getSelectedCountryCode());
                            intent.putExtra("countryCode", "91");
                            intent.putExtra("mobile_no", etEmail.getText().toString());
                            startActivity(intent);
                        } else {
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dialog.cancel();
                  /*  if (response.body().getStatus().equals("success")) {
//                        Toast.makeText(LoginActivity.this, deviceId, Toast.LENGTH_LONG).show();
                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        ApiResources.USER_PHONE = user.getPhone();

                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();

                        //save user login time, expire time
                        updateSubscriptionStatus(db.getUserData().getUserId());
                    }*/

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
/*//                progressBar.setVisibility(View.GONE);
                changeDeviceSignIn();*/
                dialog.cancel();
            }
        });


    }

    private void sendOtpSignUp() {
        dialog.show();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);
        Call<ResponseBody> call = api.verify_otp(AppConfig.API_KEY, etEmail.getText().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            Toast.makeText(LoginActivity.this, jsonObject.getString("data"), Toast.LENGTH_SHORT).show();
                            String otp = jsonObject.getString("otp");
                            Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                            intent.putExtra("from", "signup");
                            intent.putExtra("otp", otp);

                            Log.d("mobile_otp", otp);

//                            intent.putExtra("countryCode", ccp.getSelectedCountryCode());
                            intent.putExtra("countryCode", "91");
                            intent.putExtra("mobile_no", etEmail.getText().toString());
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, jsonObject.getString("data"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dialog.dismiss();
            }
        });
    }


    private void checkMobile() {
        dialog.show();
        String mobile = etEmail.getText().toString();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);
        Call<ResponseBody> call = api.mobile_check(AppConfig.API_KEY, mobile);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String status = jsonObject.getString("status");
//                        String countryCode = ccp.getSelectedCountryCode();  //and disable phone in firebase console-app-authentication-sign in method-phone (disable) county code
                        String countryCode = "91";
                        if (status.equals("1")) {
                            startActivity(new Intent(LoginActivity.this, LoginViaMobileActivity.class).putExtra("mobile", etEmail.getText().toString()).putExtra("countryCode", countryCode));
                        } else {
                            if (countryCode.contains("91")) {
                                sendOtpSignUp();
                            } else {
                                startActivity(new Intent(LoginActivity.this, OtpActivity.class)
                                        .putExtra("mobile_no", etEmail.getText().toString()).putExtra("countryCode", countryCode).putExtra("from", "signup").putExtra("otp", ""));
                            }
//                            startActivity(new Intent(LoginActivity.this, SignUpActivity.class).putExtra("mobile", etEmail.getText().toString()));

//                            startActivity(new Intent(LoginActivity.this, OtpActivity.class)
//                                    .putExtra("mobile_no", etEmail.getText().toString()).putExtra("countryCode", countryCode).putExtra("from", "signup").putExtra("otp", ""));
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    dialog.cancel();
                  /*  if (response.body().getStatus().equals("success")) {
//                        Toast.makeText(LoginActivity.this, deviceId, Toast.LENGTH_LONG).show();
                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        ApiResources.USER_PHONE = user.getPhone();

                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();

                        //save user login time, expire time
                        updateSubscriptionStatus(db.getUserData().getUserId());
                    }*/

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
/*//                progressBar.setVisibility(View.GONE);
                changeDeviceSignIn();*/
                dialog.cancel();
            }
        });

    }

    private void sendGoogleDataToServer() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d("google_login", "in response");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = user.getDisplayName();
        String email = user.getEmail();
        Uri image = user.getPhotoUrl();
        String uid = user.getUid();
        String phone = "";
        if (user.getPhoneNumber() != null)
            phone = user.getPhoneNumber();

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FirebaseAuthApi api = retrofit.create(FirebaseAuthApi.class);
        Call<User> call = api.getGoogleAuthStatus(AppConfig.API_KEY, uid, email, username, /*image,*/ phone, deviceId, firebaseToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equals("success")) {
                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        ApiResources.USER_PHONE = user.getPhone();

                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();


                        //clevertap
                      /*  String ccode = user.getCountry_code();
                        if (ccode != null) {
                            if (user.getPhone().contains(ccode))
                                addUserToCleverTap(user.getName(), user.getUserId(), user.getEmail(), user.getPhone());
                            else
                                addUserToCleverTap(user.getName(), user.getUserId(), user.getEmail(), ccode + user.getPhone());

                        } else {
                            addUserToCleverTap(user.getName(), user.getUserId(), user.getEmail(), "+91" + user.getPhone());
                        }

*/
                        //save user login time, expire time
                        updateSubscriptionStatus(user.getUserId());
                    } else {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String username = user.getDisplayName();
                        String email = user.getEmail();
                        Uri image = user.getPhotoUrl();
                        String uid = user.getUid();

                        Constant.googleEmail = email;
                        Constant.googleName = username;

                        String phone = "";
                        if (user.getPhoneNumber() != null)
                            phone = user.getPhoneNumber();

                        if (phone.equals("")) {
                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(LoginActivity.this, LoginSocialNumberNotFoundActivity.class));

                        }
                    }

                } else {
//                    Log.d("google_login", "" + uid);
//                    Log.d("google_login", "" + email);
//                    Log.d("google_login", "" + username);
//                    Log.d("google_login", "" + finalPhone);
//                    Log.d("google_login", "" + deviceId);
//                    Log.d("google_login", "" + firebaseToken);
//                    Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).show();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String username = user.getDisplayName();
                    String email = user.getEmail();
                    Uri image = user.getPhotoUrl();
                    String uid = user.getUid();

                    Constant.googleEmail = email;
                    Constant.googleName = username;

                    String phone = "";
                    if (user.getPhoneNumber() != null)
                        phone = user.getPhoneNumber();

                    if (phone.equals("")) {
                        progressBar.setVisibility(View.GONE);


                        startActivity(new Intent(LoginActivity.this, LoginSocialNumberNotFoundActivity.class));

                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
//                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                googleSignIn();
            }
        });
    }


    public void addUserToCleverTap(String name, String id, String email, String mobile) {
        HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
        profileUpdate.put("Name", name);    // String
        profileUpdate.put("Identity", id);      // String or number
        profileUpdate.put("Email", email); // Email address of the user
        profileUpdate.put("Phone", mobile);   // Phone (with the country code, starting with +)
//        profileUpdate.put("Gender", "M");             // Can be either M or F
//        profileUpdate.put("DOB", new Date());         // Date of Birth. Set the Date object to the appropriate value first
//// optional fields. controls whether the user will be sent email, push etc.
//        profileUpdate.put("MSG-email", false);        // Disable email notifications
        profileUpdate.put("MSG-push", true);          // Enable push notifications
//        profileUpdate.put("MSG-sms", false);          // Disable SMS notifications
//        profileUpdate.put("MSG-whatsapp", true);      // Enable WhatsApp notifications
//        ArrayList<String> stuff = new ArrayList<String>();
//        stuff.add("bag");
//        stuff.add("shoes");
//        profileUpdate.put("MyStuff", stuff);                        //ArrayList of Strings
//        String[] otherStuff = {"Jeans", "Perfume"};
//        profileUpdate.put("MyStuff", otherStuff);                   //String Array

        clevertapDefaultInstance.onUserLogin(profileUpdate);
        // clevertapDefaultInstance.pushProfile(profileUpdate);
    }


    private void sendFacebookDataToServer(String username, String photoUrl, String email) {
        progressBar.setVisibility(View.VISIBLE);
        if (email == null) {
            email = "";
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FirebaseAuthApi api = retrofit.create(FirebaseAuthApi.class);
        Call<User> call = api.getFacebookAuthStatus(AppConfig.API_KEY, uid, username, email, /*Uri.parse(photoUrl),*/ deviceId, firebaseToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equals("success")) {

                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        ApiResources.USER_PHONE = user.getPhone();

                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();

                        //save user login time, expire time
                        updateSubscriptionStatus(user.getUserId());
                    } else {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (!user.getUid().isEmpty()) {
                            String username = user.getDisplayName();
                            String photoUrl = String.valueOf(user.getPhotoUrl());
                            String email = user.getEmail();
                            if (email == null) {
                                email = "";
                            }

                            Constant.googleEmail = email;
                            Constant.googleName = username;

                            String phone = "";
                            if (user.getPhoneNumber() != null)
                                phone = user.getPhoneNumber();

                            if (phone.equals("")) {
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(LoginActivity.this, LoginSocialNumberNotFoundActivity.class));

                            }
                        }

                        progressBar.setVisibility(View.GONE);
                    }

                } else {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (!user.getUid().isEmpty()) {
                        String username = user.getDisplayName();
                        String photoUrl = String.valueOf(user.getPhotoUrl());
                        String email = user.getEmail();
                        if (email == null) {
                            email = "";
                        }


                        Constant.googleEmail = email;
                        Constant.googleName = username;

                        String phone = "";
                        if (user.getPhoneNumber() != null)
                            phone = user.getPhoneNumber();

                        if (phone.equals("")) {
                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(LoginActivity.this, LoginSocialNumberNotFoundActivity.class));

                        }


                        progressBar.setVisibility(View.GONE);
                    }

                }
            }


            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                facebookSignIn();
            }
        });
    }


}
