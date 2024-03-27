package ott.hulchulandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

//import com.clevertap.android.sdk.CleverTapAPI;
import com.appsflyer.AppsFlyerLib;
import com.clevertap.android.sdk.CleverTapAPI;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mukeshsolanki.OtpView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ott.hulchulandroid.constant.Constant;
import ott.hulchulandroid.database.DatabaseHelper;
import ott.hulchulandroid.network.RetrofitClient;
import ott.hulchulandroid.network.apis.FirebaseAuthApi;
import ott.hulchulandroid.network.apis.SignUpApi;
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

public class SignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "SignUpActivity";
    private static int RC_PHONE_SIGN_IN = 1231;
    private static int RC_FACEBOOK_SIGN_IN = 1241;
    private static int RC_GOOGLE_SIGN_IN = 1251;
    private TextInputEditText etName, etEmail, etPass, mobile, confirmpassword;
    RadioButton checkbox_age;
    CheckBox chk_terms;
    private AppCompatButton btnSignup;
    private ProgressDialog dialog;
    private View backgorundView;
    private TextView tvLogin;
    private ImageView phoneAuthButton, facebookAuthButton, googleAuthButton;
    FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    String strDob = "";
    String deviceId = "";
    String firebaseToken = "", mobileStr = "", countryCode = "91";
    TextView tv_login, txtcountryCode;
    CleverTapAPI clevertapDefaultInstance, clevertapnewRegisterInstance;
    //    String mobile = "";
    LinearLayout ll_pin;
    OtpView otp_viewIndia;

    String[] user_age = {"Select Age","18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31",
     "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47",
     "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "58", "60", "61", "62", "63"};


    String str_user_age = "";
    String userEnterPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        clevertapnewRegisterInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());


        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        otp_viewIndia = findViewById(R.id.otp_viewIndia);
        ll_pin = findViewById(R.id.ll_pin);

        otp_viewIndia.setOtpCompletionListener(otp -> userEnterPin = otp);

        Spinner spin = (Spinner) findViewById(R.id.spinner);

        spin.setOnItemSelectedListener(SignUpActivity.this);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, user_age);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);
       // spin.setPrompt("Select age1");


//        Toolbar toolbar = findViewById(R.id.toolbar);
//
//        if (!isDark) {
//            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//        }else {
//            toolbar.setBackgroundColor(getResources().getColor(R.color.black_window_light));
//        }

//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("SignUp");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "sign_up_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        mobileStr = getIntent().getStringExtra("mobile");
        countryCode = getIntent().getStringExtra("countryCode");

        if (countryCode == null || countryCode.equals("")) {
            countryCode = "91";
        }

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        etName = findViewById(R.id.name);
        tv_login = findViewById(R.id.tv_login);
        txtcountryCode = findViewById(R.id.txtcountryCode);

        etEmail = findViewById(R.id.email);
        etPass = findViewById(R.id.password);
        mobile = findViewById(R.id.mobile);
        mobile = findViewById(R.id.mobile);
        confirmpassword = findViewById(R.id.confirmpassword);
        checkbox_age = findViewById(R.id.checkbox_age);
        chk_terms = findViewById(R.id.chk_terms);
        btnSignup = findViewById(R.id.signup);
        backgorundView = findViewById(R.id.background_view);
        progressBar = findViewById(R.id.progress_bar);
        phoneAuthButton = findViewById(R.id.phoneAuthButton);
        facebookAuthButton = findViewById(R.id.facebookAuthButton);
        googleAuthButton = findViewById(R.id.googleAuthButton);
        tvLogin = findViewById(R.id.login);

        txtcountryCode.setText(String.format("+%s", countryCode));
        mobile.setText(mobileStr);

        if (!Constant.googleName.equals("")) {
            etName.setText(Constant.googleName);
        }
        if (!Constant.googleEmail.equals("")) {
            etEmail.setText(Constant.googleEmail);
        }

        tv_login.setOnClickListener(v -> {

            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        if (isDark) {
            backgorundView.setBackgroundColor(getResources().getColor(R.color.nav_head_bg));
            btnSignup.setBackground(ContextCompat.getDrawable(this, R.drawable.login_field_button_dark));
        }


        btnSignup.setOnClickListener(v -> {
            if (etName.getText().toString().equals("")) {
                new ToastMsg(SignUpActivity.this).toastIconError("please enter name");
            } else if (mobile.getText().toString().equals("")) {
                new ToastMsg(SignUpActivity.this).toastIconError("please enter mobile number");
            } else if (!isValidEmailAddress(etEmail.getText().toString())) {
                new ToastMsg(SignUpActivity.this).toastIconError("please enter valid email");
            }
            else if (str_user_age.equals("Select Age")) {
                new ToastMsg(SignUpActivity.this).toastIconError("please select your age");
            }



            /*else if (etPass.getText().toString().equals("")) {
                new ToastMsg(SignUpActivity.this).toastIconError("please enter password");
            } else if (!confirmpassword.getText().toString().equals(etPass.getText().toString())) {
                new ToastMsg(SignUpActivity.this).toastIconError("confirm password should match with password");
            } */

            //else if (!checkbox_age.isChecked()) {
               // new ToastMsg(SignUpActivity.this).toastIconError("please select age");
           // }

            /*else if (!chk_terms.isChecked()) {
                new ToastMsg(SignUpActivity.this).toastIconError("please accept our terms and conditions");
            }*/ else {

                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isComplete()) {
                            firebaseToken = task.getResult();
                            Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);

                            //clevertap
                            clevertapDefaultInstance.pushFcmRegistrationId(firebaseToken, true);
                            String email = etEmail.getText().toString();
                            String pass = etPass.getText().toString();
                            String name = etName.getText().toString();
                            String mobilestr = mobile.getText().toString();
                            if (mobilestr.contains("+91"))
                                mobilestr = mobileStr.replace("+91", "");

                            if (checkbox_age.isChecked()) {
                                strDob = "1";
                            } else {
                                strDob = "";
                            }

                           // signUp(email, pass, name, mobilestr, strDob);
                            signUp(email, pass, name, mobilestr, "1");



                        }
                    }
                });

            }
        });


        //social login button
        if (AppConfig.ENABLE_FACEBOOK_LOGIN) {
            facebookAuthButton.setVisibility(View.VISIBLE);
        }
        if (AppConfig.ENABLE_GOOGLE_LOGIN) {
            googleAuthButton.setVisibility(View.VISIBLE);
        }
        if (AppConfig.ENABLE_PHONE_LOGIN) {
            phoneAuthButton.setVisibility(View.VISIBLE);
        }

        firebaseAuth = FirebaseAuth.getInstance();

        phoneAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneSignIn();
            }
        });

        facebookAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookSignIn();
            }
        });

        googleAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
*/

                Intent i = new Intent(Intent.ACTION_VIEW);
                //i.setData(Uri.parse("https://www.youtube.com/channel/UC2U1WBF9g47zkb3ENy4O5jA"));
                // i.setData(Uri.parse("https://instagram.com/besharamsapp?igshid=NGVhN2U2NjQ0Yg=="));
                i.setData(Uri.parse("https://hulchul.co.in/terms-condition.php/"));
                startActivity(i);

            }
        });

//        deviceId = getAndroidDeviceId();
        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void acceptTermsConditions(View view) {
        Intent intent = new Intent(SignUpActivity.this, TermsActivity.class);
        intent.putExtra("from", "");
        startActivity(intent);
    }


    /*private String getAndroidDeviceId() {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }*/

    public void addUserToCleverTap(String name, String id, String email, String mobile) {
        HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
        profileUpdate.put("Name", name);    // String
        profileUpdate.put("Identity", id);      // String or number
        profileUpdate.put("Email", email); // Email address of the user
        profileUpdate.put("Phone", mobile);   // Phone (with the country code, starting with +)
        // profileUpdate.put("Gender", "M");             // Can be either M or F
        // profileUpdate.put("DOB", new Date());         // Date of Birth. Set the Date object to the appropriate value first
// optional fields. controls whether the user will be sent email, push etc.
        profileUpdate.put("MSG-email", true);        // Disable email notifications
        profileUpdate.put("MSG-push", true);          // Enable push notifications
        profileUpdate.put("MSG-sms", true);          // Disable SMS notifications
        profileUpdate.put("MSG-whatsapp", true);      // Enable WhatsApp notifications
        // ArrayList<String> stuff = new ArrayList<String>();
        //  stuff.add("bag");
        // stuff.add("shoes");
        //  profileUpdate.put("MyStuff", stuff);                        //ArrayList of Strings
        //  String[] otherStuff = {"Jeans", "Perfume"};
        //  profileUpdate.put("MyStuff", otherStuff);                   //String Array


        clevertapDefaultInstance.onUserLogin(profileUpdate);

        HashMap<String, Object> newregisteruseraction = new HashMap<String, Object>();
        newregisteruseraction.put("Name", name);    // String
        newregisteruseraction.put("Identity", id);      // String or number
        newregisteruseraction.put("Email", email); // Email address of the user
        newregisteruseraction.put("Phone", mobile);   // Phone (with the country code, starting with +)
        newregisteruseraction.put("Device Type", "Android");
        clevertapnewRegisterInstance.pushEvent("New Register user", newregisteruseraction);
    }


    private void signUp(String email, String pass, String name, String mobile, String age) {
        dialog.show();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SignUpApi signUpApi = retrofit.create(SignUpApi.class);
        String countryCodeStr = "+" + countryCode;
        Call<User> call = signUpApi.signUp(AppConfig.API_KEY, email, pass, name, mobile, countryCodeStr, deviceId, age, firebaseToken, str_user_age, userEnterPin);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();

                if (user.getStatus().equals("success")) {
                    new ToastMsg(SignUpActivity.this).toastIconSuccess("Successfully registered");


                    SharedPreferences.Editor editor = SignUpActivity.this.getSharedPreferences(Constants.USER_REGISTER_AGE, MODE_PRIVATE).edit();
                    editor.putString("user_register_age", user.getUser_age());
                    editor.apply();

                    SharedPreferences.Editor editor1 = getSharedPreferences(Constants.USER_PIN, MODE_PRIVATE).edit();
                    editor1.putString("user_pin", user.getPin());
                    editor1.apply();


                    // save user info to sharedPref
                    saveUserInfo(user, user.getName(), etEmail.getText().toString(),
                            user.getUserId(), mobile);

                    String ccode = user.getCountry_code();
                    if (ccode == null) {
                        addUserToCleverTap(user.getName(), user.getUserId(), user.getEmail(), "+91" + user.getPhone());
                        addUserToAppsflyer(user.getName(), user.getUserId(), user.getEmail(), "+91" + user.getPhone());
                    } else {
                        if (user.getPhone().contains(ccode)) {
                            addUserToCleverTap(user.getName(), user.getUserId(), user.getEmail(), user.getPhone());
                            addUserToAppsflyer(user.getName(), user.getUserId(), user.getEmail(), user.getPhone());
                        } else {
                            addUserToCleverTap(user.getName(), user.getUserId(), user.getEmail(), ccode + user.getPhone());
                            addUserToAppsflyer(user.getName(), user.getUserId(), user.getEmail(), ccode + user.getPhone());
                        }
                    }

                } else if (user.getStatus().equals("202")) {
                    new ToastMsg(SignUpActivity.this).toastIconError(user.getData());
                    dialog.cancel();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                new ToastMsg(SignUpActivity.this).toastIconError("Something went wrong." + t.getMessage());
                t.printStackTrace();
                dialog.cancel();

            }
        });
    }


    private void addUserToAppsflyer(String name, String userId, String email, String s) {

        HashMap<String, Object> aFnewregisteruseraction = new HashMap<String, Object>();
        aFnewregisteruseraction.put("Name", name);    // String
        aFnewregisteruseraction.put("Identity", userId);      // String or number
        aFnewregisteruseraction.put("Email", email); // Email address of the user
        aFnewregisteruseraction.put("Phone", mobile);   // Phone (with the country code, starting with +)
        aFnewregisteruseraction.put("Device Type", "Android");

        AppsFlyerLib.getInstance().logEvent(getApplicationContext(),
                "New Register user", aFnewregisteruseraction);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public void saveUserInfo(User user, String name, String email, String id, String mobile) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("mobile", mobile);
        editor.putString("id", id);
        editor.putBoolean("status", true);
        editor.putBoolean(Constants.USER_LOGIN_STATUS, true);
        editor.apply();

        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
        db.deleteUserData();
        db.insertUserData(user);
        ApiResources.USER_PHONE = user.getPhone();
        //save user login time, expire time
        updateSubscriptionStatus(user.getUserId());

    }


    private void updateSubscriptionStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        ActiveStatus activeStatus = response.body();

                        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
                        db.deleteAllActiveStatusData();
                        db.insertActiveStatusData(activeStatus);

                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
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
                new ToastMsg(SignUpActivity.this).toastIconError(getResources().getString(R.string.something_went_wrong));
            }
        });
    }

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
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.PhoneBuilder().build());
            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_PHONE_SIGN_IN);
        }*/
        startActivity(new Intent(SignUpActivity.this, LoginViaMobileActivity.class));
    }

    private void facebookSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
                //already signed in
                //send data to server
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isComplete()) {
                            firebaseToken = task.getResult();
                            Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            sendFacebookDataToServer(user.getDisplayName(), String.valueOf(user.getPhotoUrl()), user.getEmail());
                        }
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
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_FACEBOOK_SIGN_IN);
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
                        sendGoogleDataToServer();
                    }
                }
            });

        } else {
            progressBar.setVisibility(View.VISIBLE);
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
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_GOOGLE_SIGN_IN);
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
                        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
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

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                phoneSignIn();
            }
        });


    }

    private void sendGoogleDataToServer() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = user.getDisplayName();
        String email = user.getEmail();
        Uri image = user.getPhotoUrl();
        String uid = user.getUid();
        String phone = "";
        if (user.getPhoneNumber() != null)
            phone = user.getPhoneNumber();

        Log.d(TAG, "onActivityResult: " + user.getEmail());
        Log.d(TAG, "onActivityResult: " + user.getDisplayName());
        Log.d(TAG, "onActivityResult: " + user.getPhoneNumber());

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FirebaseAuthApi api = retrofit.create(FirebaseAuthApi.class);
        Call<User> call = api.getGoogleAuthStatus(AppConfig.API_KEY, uid, email, username, /*image,*/ phone, deviceId, firebaseToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equals("success")) {
                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
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

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                googleSignIn();
            }
        });
    }

    private void sendFacebookDataToServer(String username, String photoUrl, String email) {
        progressBar.setVisibility(View.VISIBLE);
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
                        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
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

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                facebookSignIn();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHONE_SIGN_IN) {

            final IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
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
//                    sendDataToServer();
                } else {
                    //empty
                    phoneSignIn();
                }
            } else {
                // sign in failed
                if (response == null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Error !!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }

            }
        } else if (requestCode == RC_FACEBOOK_SIGN_IN) {
            final IdpResponse response = com.firebase.ui.auth.IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (!user.getUid().isEmpty()) {
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isComplete()) {
                                firebaseToken = task.getResult();
                                Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);
                                String username = user.getDisplayName();
                                String photoUrl = String.valueOf(user.getPhotoUrl());
                                String email = user.getEmail();

                                sendFacebookDataToServer(username, photoUrl, email);
                            }
                        }
                    });


                } else {
                    //empty
                    facebookSignIn();
                }
            } else {
                // sign in failed
                if (response == null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Error !!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        } else if (requestCode == RC_GOOGLE_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (!user.getUid().isEmpty()) {

                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isComplete()) {
                                firebaseToken = task.getResult();
                                Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);
//                                sendDataToServer();
                                sendGoogleDataToServer();
                            }
                        }
                    });

                } else {
                    //empty
                    googleSignIn();
                }
            } else {
                // sign in failed
                if (response == null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Error !!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // Toast.makeText(getApplicationContext(), user_age[position], Toast.LENGTH_LONG).show();

        str_user_age = user_age[position];


        if (str_user_age.equals("Select Age")) {

          //  Toast.makeText(SignUpActivity.this, user_age[position], Toast.LENGTH_LONG).show();
        } else {

            int age = Integer.parseInt(str_user_age);

            if (age >= 18) {
               // ll_pin.setVisibility(View.VISIBLE);
                ll_pin.setVisibility(View.GONE);
            } else {
                ll_pin.setVisibility(View.GONE);
            }
        }

    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


}
