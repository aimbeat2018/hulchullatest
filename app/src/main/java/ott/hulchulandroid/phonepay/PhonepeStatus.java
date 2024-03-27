package ott.hulchulandroid.phonepay;

import static net.one97.paytm.nativesdk.BasePaytmSDK.getApplicationContext;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.ResponseBody;
import ott.hulchulandroid.AppConfig;
import ott.hulchulandroid.MainActivity;
import ott.hulchulandroid.PhonepayKotline;
import ott.hulchulandroid.database.DatabaseHelper;
import ott.hulchulandroid.network.RetrofitClient;
import ott.hulchulandroid.network.apis.PaymentApi;
import ott.hulchulandroid.network.apis.SubscriptionApi;
import ott.hulchulandroid.network.model.ActiveStatus;
import ott.hulchulandroid.network.model.Package;
import ott.hulchulandroid.network.model.SubscriptionHistory;
import ott.hulchulandroid.utils.PreferenceUtils;
import ott.hulchulandroid.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class PhonepeStatus {

    static String user_id = "";
    static String plantamount = "";

    public static void  checkStatus(PhonepayKotline phonepayKotline, String MERCHANT_TID, String xVerify, String uid, Package aPackage, DatabaseHelper databaseHelper) {
        // progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        Call<ResponseBody> call = paymentApi.check_phonepe_Status(AppConfig.API_KEY, xVerify, MERCHANT_TID
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {


                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
                        String pay_status = jsonObject.getString("success");
                        String status_code = jsonObject.getString("code");
                        String message = jsonObject.getString("message");

                        if (status_code.equals("PAYMENT_SUCCESS")) {

                            JSONObject getdata = jsonObject.getJSONObject("data");

                            String transactionId = String.valueOf(getdata.get("merchantTransactionId"));


                            saveChargeData(transactionId, "phonepegateway",phonepayKotline,uid,aPackage,databaseHelper);


                           // Toast.makeText(phonepayKotline.getApplicationContext(), "Payment Success", Toast.LENGTH_SHORT).show();
                            Toast.makeText(phonepayKotline.getApplicationContext(), transactionId, Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(phonepayKotline.getApplicationContext(), "Payment Failed", Toast.LENGTH_SHORT).show();

                        }

                        Toast.makeText(phonepayKotline.getApplicationContext(), message, Toast.LENGTH_SHORT).show();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
                    // new ToastMsg(phonepayKotline).toastIconError(getString(R.string.something_went_wrong));
                    // finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                // new ToastMsg(phonepayKotline).toastIconError(getString(R.string.something_went_wrong));
                //   finish();
                //progressBar.setVisibility(View.GONE);
            }
        });

    }


    public static void saveChargeData(String token, String from, PhonepayKotline phonepayKotline, String uid, Package aPackage, DatabaseHelper databaseHelper) {
       // progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        Call<ResponseBody> call = paymentApi.savePayment(AppConfig.API_KEY, aPackage.getPlanId(),
                databaseHelper.getUserData().getUserId(),
                aPackage.getPrice(),
                // "1",
                token, "20", from);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.code() == 200) {

                    plantamount = aPackage.getPrice();
                    updateActiveStatus(phonepayKotline);
                    getSubscriptionHistory(plantamount,uid);

                } else {
                    new ToastMsg(phonepayKotline.getApplicationContext()).toastIconError("\"Something went wrong.\"");

                    //  finish();
                    //  progressBar.setVisibility(View.GONE);
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(phonepayKotline.getApplicationContext()).toastIconError("Something went wrong." + t.getMessage());
               // finish();
               // progressBar.setVisibility(View.GONE);
            }
        });

    }



    private static void getSubscriptionHistory(String plantamount, String uid) {
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

                          /*  HashMap<String, Object> paymentAction = new HashMap<String, Object>();
                            paymentAction.put("payment mode", "phonepe");
                            paymentAction.put("amount", plantamount);
                            paymentAction.put("subscription plan", subscriptionHistory.getActiveSubscription().get(0).getPlanTitle());
                            paymentAction.put("Payment ID", subscriptionHistory.getActiveSubscription().get(0).getPaymentInfo());
                            paymentAction.put("Subscription ID", subscriptionHistory.getActiveSubscription().get(0).getSubscriptionId());
                            paymentAction.put("subscription plan id", subscriptionHistory.getActiveSubscription().get(0).getPlanId());
                            paymentAction.put("subscription Start date", subscriptionHistory.getActiveSubscription().get(0).getStartDate());
                            paymentAction.put("subscription End date", subscriptionHistory.getActiveSubscription().get(0).getExpireDate());
                            clevertapChergedInstance.pushEvent("Charged", paymentAction);*/

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<SubscriptionHistory> call, Throwable t) {
               // progressBar.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });

    }





    private static void updateActiveStatus(PhonepayKotline phonepayKotline) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, PreferenceUtils.getUserId(phonepayKotline.getApplicationContext()));
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, retrofit2.Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    ott.hulchulandroid.database.DatabaseHelper db = new ott.hulchulandroid.database.DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                   // new ToastMsg(OneUPIPaymentActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));
                    new ToastMsg(phonepayKotline.getApplicationContext()).toastIconSuccess("Payment Success.");
                //    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(phonepayKotline.getApplicationContext(), MainActivity.class);
                    phonepayKotline.startActivity(intent);
                    phonepayKotline.finish();
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(phonepayKotline.getApplicationContext()).toastIconError("Something went wrong." + t.getMessage());
                phonepayKotline.finish();
                //progressBar.setVisibility(View.GONE);
            }
        });

    }



}
