package ott.hulchulandroid;

import static ott.hulchulandroid.MoreActivity.familycontent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appsflyer.AppsFlyerLib;
import com.cashfree.pg.api.CFPaymentGatewayService;
import com.cashfree.pg.core.api.CFSession;
import com.cashfree.pg.core.api.CFTheme;
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback;
import com.cashfree.pg.core.api.exception.CFException;
import com.cashfree.pg.core.api.utils.CFErrorResponse;
import com.cashfree.pg.ui.api.CFDropCheckoutPayment;
import com.cashfree.pg.ui.api.CFPaymentComponent;
import com.clevertap.android.sdk.CleverTapAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import ott.hulchulandroid.database.DatabaseHelper;
import ott.hulchulandroid.network.RetrofitClient;
import ott.hulchulandroid.network.apis.PaymentApi;
import ott.hulchulandroid.network.apis.SubscriptionApi;
import ott.hulchulandroid.network.model.ActiveStatus;
import ott.hulchulandroid.network.model.Package;
import ott.hulchulandroid.network.model.SubscriptionHistory;
import ott.hulchulandroid.network.model.User;
import ott.hulchulandroid.utils.Constants;
import ott.hulchulandroid.utils.PreferenceUtils;
import ott.hulchulandroid.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class CashFreePaymentActivity extends AppCompatActivity implements CFCheckoutResponseCallback {
    String orderID = "ORDER_ID";
    String token = "TOKEN";
    String order_token = "order_token";
    String cf_order_id = "cf_order_id";
    String order_status = "order_status";
    CFSession.Environment cfEnvironment = CFSession.Environment.PRODUCTION;

    ProgressDialog dialog;
    String subscription_end_date = "";

    private static final String TAG = "CashFreePaymentActivity";
    String uid = "", uname = "", mobile = "", email = "", order_id = "", orderIdstr = "";
    static int min, max, create_otp;
    String plantamount = "";

    private Package aPackage;
    private DatabaseHelper databaseHelper;

    TextView txt_txn_id,
            txt_falied_reason;
    LinearLayout lnr_success,
            lnr_failed;
    private ProgressBar progressBar;
    CleverTapAPI clevertapChergedInstance;
    String str_user_age = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_free_payment);

        try {
            //  Block of code to try
            SharedPreferences sharedPreferences = CashFreePaymentActivity.this.getSharedPreferences(Constants.USER_AGE, MODE_PRIVATE);
            str_user_age = sharedPreferences.getString("user_age", "20");

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getIntent() != null) {
            aPackage = (Package) getIntent().getSerializableExtra("package");
            databaseHelper = new DatabaseHelper(this);
        }

        lnr_success = findViewById(R.id.lnr_success);
        lnr_failed = findViewById(R.id.lnr_failed);
        txt_txn_id = findViewById(R.id.txt_txn_id);
        txt_falied_reason = findViewById(R.id.txt_falied_reason);
        progressBar = findViewById(R.id.progressBar);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        User user = databaseHelper.getUserData();
        uid = user.getUserId();
        uname = user.getName();
//        mobile = "0000000000";
        mobile = user.getPhone();
        email = user.getEmail();

        try {
            CFPaymentGatewayService.getInstance().setCheckoutCallback(this);

        } catch (CFException e) {
            e.printStackTrace();
        }
        min = 100000;
        max = 999999;
        Random r = new Random();
        create_otp = r.nextInt(max - min + 1) + min;
        order_id = String.valueOf(create_otp);
        orderID = order_id;
        getToken(order_id, aPackage.getPrice());
      //  getToken(order_id, "1");


        clevertapChergedInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
    }


    @Override
    public void onPaymentVerify(String orderID) {
        lnr_failed.setVisibility(View.GONE);
        lnr_success.setVisibility(View.VISIBLE);
//        txt_txn_id.setText("Transaction Id : " + orderID);
        saveChargeData(orderID, "cashfree");
    }

    @Override
    public void onPaymentFailure(CFErrorResponse cfErrorResponse, String orderID) {
        Log.e("onPaymentFailure " + orderID, cfErrorResponse.getMessage());
        lnr_failed.setVisibility(View.VISIBLE);
        lnr_success.setVisibility(View.GONE);
        txt_txn_id.setText(cfErrorResponse.getMessage());

        new Handler().postDelayed(() -> finish(), 1000);
    }

    public void doDropCheckoutPayment() {
        if (order_id.equals("ORDER_ID") || TextUtils.isEmpty(order_id)) {
            Toast.makeText(this, "Please set the orderId (DropCheckoutActivity.class,  line: 22)", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (token.equals("TOKEN") || TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Please set the token (DropCheckoutActivity.class,  line: 23)", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            CFSession cfSession = new CFSession.CFSessionBuilder()
                    .setEnvironment(cfEnvironment)
                    .setOrderToken(token)
                    .setOrderId(order_id)
                    .build();
            CFPaymentComponent cfPaymentComponent = new CFPaymentComponent.CFPaymentComponentBuilder()
                    .add(CFPaymentComponent.CFPaymentModes.CARD)
                    .add(CFPaymentComponent.CFPaymentModes.UPI)
                    .build();
            CFTheme cfTheme = new CFTheme.CFThemeBuilder()
                    .setNavigationBarBackgroundColor("#0d2366")
                    .setNavigationBarTextColor("#ffffff")
                    .setButtonBackgroundColor("#0d2366")
                    .setButtonTextColor("#ffffff")
                    .setPrimaryTextColor("#000000")
                    .setSecondaryTextColor("#000000")
                    .build();
            CFDropCheckoutPayment cfDropCheckoutPayment = new CFDropCheckoutPayment.CFDropCheckoutPaymentBuilder()
                    .setSession(cfSession)
                    //By default all modes are enabled. If you want to restrict the payment modes uncomment the next line
//                        .setCFUIPaymentModes(cfPaymentComponent)
                    .setCFNativeCheckoutUITheme(cfTheme)
                    .build();
            CFPaymentGatewayService gatewayService = CFPaymentGatewayService.getInstance();
            Log.d("cashfreedata", "" + cfDropCheckoutPayment);
            gatewayService.doPayment(CashFreePaymentActivity.this, cfDropCheckoutPayment);
        } catch (CFException exception) {
            exception.printStackTrace();
        }
    }

/*    public void getToken(final String orderId, final String amount) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://primeplay.co.in/webworld_backoffice/rest-api/v130/cashfree"*/
    /*"https://test.cashfree.com/api/v2/cftoken/order"*/
    /*, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
//                    cf_order_id = jsonObject.getString("cf_order_id");
                    order_id = jsonObject.getString("order_id");
                    order_token = jsonObject.getString("order_token");
//                    order_status = jsonObject.getString("order_status");
                    token = order_token;
                    Log.d("cashfreeToken", "" + token);
                    doDropCheckoutPayment();

                } catch (JSONException e) {
                    Toast.makeText(CashFreePaymentActivity.this, "json exception" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CashFreePaymentActivity.this, "json error exception" + error.getMessage(), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonObject = new JSONObject();
                final String requestBody;
                try {
                    jsonObject.put("order_id", orderId);
                    jsonObject.put("order_amount", amount);
                    jsonObject.put("order_currency", "INR");
                    JSONObject customerObject = new JSONObject();
                    customerObject.put("customer_id", uid);
                    customerObject.put("customer_name", uname);
                    customerObject.put("customer_email", email);
                    customerObject.put("customer_phone", mobile);
                    jsonObject.put("customer_details", customerObject);
                    requestBody = jsonObject.toString();
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (JSONException | UnsupportedEncodingException e) {
//                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("API-KEY", AppConfig.API_KEY);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(100000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }*/


    public void getToken(final String orderId, final String amount) {
        dialog.show();
       // StringRequest stringRequest = new StringRequest(Request.Method.POST, /*"https://primeplay.co.in/webworld_backoffice/rest-api/v130/cashfree"*/"https://hunters.co.in/ppv1/rest-api/v130/cashfree_test"/*"https://test.cashfree.com/api/v2/cftoken/order"*/, new com.android.volley.Response.Listener<String>() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, /*"https://primeplay.co.in/webworld_backoffice/rest-api/v130/cashfree"*/"https://hulchul.co.in/ox9_backoffice/rest-api/v130/cashfree_test"/*"https://test.cashfree.com/api/v2/cftoken/order"*/, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
//                    cf_order_id = jsonObject.getString("cf_order_id");

                    if (jsonObject.has("message")) {
                        Toast.makeText(CashFreePaymentActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    } else {
                        if (jsonObject.has("order_id"))
                            order_id = jsonObject.getString("order_id");

                        if (jsonObject.has("order_token"))
                            order_token = jsonObject.getString("order_token");
//                    order_status = jsonObject.getString("order_status");
                        token = order_token;

                        doDropCheckoutPayment();

                        if (jsonObject.has("order_expiry_time")) {
                            subscription_end_date = jsonObject.getString("order_expiry_time");
                        }
                    }
//                    Log.d("orderId", "" + orderId);
                    Log.d("cashfreeToken", "" + jsonObject);
                    Log.d("orderId", "" + order_id);

                    dialog.dismiss();
                } catch (JSONException e) {
                    Toast.makeText(CashFreePaymentActivity.this, "json exception" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                    dialog.dismiss();
                }
            }
        }, error -> {
            Toast.makeText(CashFreePaymentActivity.this, "json error exception" + error.getMessage(), Toast.LENGTH_SHORT).show();
            error.printStackTrace();

            dialog.dismiss();
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonObject = new JSONObject();
                final String requestBody;

                try {
//                    min = 111111;
//                    max = 999999;
//                    Random r = new Random();
//                    create_otp = r.nextInt(max - min + 1) + min;

//                    jsonObject.put("order_id", String.valueOf(create_otp));
                    jsonObject.put("order_amount", amount);
                    jsonObject.put("order_currency", "INR");
                    JSONObject customerObject = new JSONObject();
                    customerObject.put("customer_id", uid);
                    customerObject.put("customer_name", uname);
                    customerObject.put("customer_email", email);
                    customerObject.put("customer_phone", mobile);
                    jsonObject.put("customer_details", customerObject);


                    requestBody = jsonObject.toString();

                    Log.d("object", "" + requestBody);

                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (JSONException | UnsupportedEncodingException e) {
//                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("API-KEY", AppConfig.API_KEY);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(100000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }


    public void saveChargeData(String token, String from) {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        Call<ResponseBody> call = paymentApi.savePayment(AppConfig.API_KEY, aPackage.getPlanId(),
                databaseHelper.getUserData().getUserId(),
                aPackage.getPrice(),
                // "1",
                token, str_user_age, from);


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.code() == 200) {
                    plantamount = aPackage.getPrice();

                    updateActiveStatus();

                    getSubscriptionHistory(plantamount);

                } else {
                    new ToastMsg(CashFreePaymentActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    finish();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(CashFreePaymentActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
                progressBar.setVisibility(View.GONE);
            }
        });

    }


    private void getSubscriptionHistory(String plantamount) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);
        Call<SubscriptionHistory> call = subscriptionApi.getSubscriptionHistory(AppConfig.API_KEY, uid);
        call.enqueue(new Callback<SubscriptionHistory>() {
            @Override
            public void onResponse(Call<SubscriptionHistory> call, retrofit2.Response<SubscriptionHistory> response) {
                SubscriptionHistory subscriptionHistory = response.body();
                if (response.code() == 200) {

                    try {

                        if (subscriptionHistory.getActiveSubscription().size() > 0) {

                            HashMap<String, Object> paymentAction = new HashMap<String, Object>();
                            paymentAction.put("payment mode", "cashFree");
                            paymentAction.put("amount", plantamount);
                            paymentAction.put("subscription plan", subscriptionHistory.getActiveSubscription().get(0).getPlanTitle());
                            paymentAction.put("Payment ID", subscriptionHistory.getActiveSubscription().get(0).getPaymentInfo());
                            paymentAction.put("Subscription ID", subscriptionHistory.getActiveSubscription().get(0).getSubscriptionId());
                            paymentAction.put("subscription plan id", subscriptionHistory.getActiveSubscription().get(0).getPlanId());
                            paymentAction.put("subscription Start date", subscriptionHistory.getActiveSubscription().get(0).getStartDate());
                            paymentAction.put("subscription End date", subscriptionHistory.getActiveSubscription().get(0).getExpireDate());
                            clevertapChergedInstance.pushEvent("Charged", paymentAction);

                            //Appflayer
                            AppsFlyerLib.getInstance().logEvent(getApplicationContext(),
                                    "Charged", paymentAction);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<SubscriptionHistory> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });

    }


    private void updateActiveStatus() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, PreferenceUtils.getUserId(CashFreePaymentActivity.this));
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, retrofit2.Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                    new ToastMsg(CashFreePaymentActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(CashFreePaymentActivity.this, MainActivity.class);
                    intent.putExtra("login_status", "user_login");
                    startActivity(intent);
                    finish();
                }
            }


            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(CashFreePaymentActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
                progressBar.setVisibility(View.GONE);
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}