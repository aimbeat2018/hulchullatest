package ott.hulchulandroid;

import static org.apache.commons.codec.digest.DigestUtils.sha256;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.phonepe.intent.sdk.api.B2BPGRequestBuilder;
import com.phonepe.intent.sdk.api.PhonePe;
import com.phonepe.intent.sdk.api.PhonePeInitException;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;


public class PhonepayActivity extends AppCompatActivity {

    Button pay;
    // PhonePeEnvironment environment;
    //   var apiEndPoint = "https://api.phonepe.com/apis/hermes/pg/v1/pay"
    String apiEndPoint = "/pg/v1/pay";
    //  var apiEndPoint = "https://api-preprod.phonepe.com/apis/pg-sandbox"
    // val salt = "3901e920-aab1-44eb-b8ac-932b56fdb531" // salt key
    String salt = "099eb0cd-02cf-4e2a-8aca-3e6c6aff0399"; // salt key
    //  val MERCHANT_ID = "M1GQPJSNH1HW"  // Merhcant id
    String MERCHANT_ID = "PGTESTPAYUAT"; // Merhcant id
    //  val salt = "4cda0702-6bd3-4e45-9716-98c0d8899339" // salt key
    //val MERCHANT_ID = "M22TTNZLGZFA2"  // Merhcant id
    // val MERCHANT_TID = "181020231969857"
    String MERCHANT_TID = UUID.randomUUID().toString().substring(0, 34);
    String BASE_URL = "https://api-preprod.phonepe.com/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonepay_kotline);
        pay = (Button) findViewById(R.id.button);


        PhonePe.init(this);


        try {
            PhonePe.getUpiApps();
        } catch (PhonePeInitException e) {
            e.printStackTrace();
        }


        JSONObject data = new JSONObject();
        try {
            data.put("merchantTransactionId", MERCHANT_TID);//String. Mandatory
            data.put("merchantId", MERCHANT_ID);//String. Mandatory

            data.put("amount", 200);//Long. Mandatory

            data.put("mobileNumber", "9920352485"); //String. Optional
            data.put("callbackUrl", "https://webhook.site/50f50687-45e7-44f4-bcb0-36faf315ec33");//String. Mandatory


        } catch (JSONException e) {
            e.printStackTrace();
        }


        //  data.put("callbackUrl", "https://webhook.site/callback-url") //String. Mandatory
        // data.put("callbackUrl", "https://hulchul.co.in/create_payment_request/webhook") //String. Mandatory

        JSONObject paymentInstrument = new JSONObject();
        // paymentInstrument.put("type", "UPI_INTENT")
        try {
            paymentInstrument.put("type", "PAY_PAGE");
            paymentInstrument.put("targetApp", "com.phonepe.simulator");
            data.put("paymentInstrument", paymentInstrument);//OBJECT. Mandatory

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // paymentInstrument.put("targetApp", "com.phonepe.app");


        JSONObject deviceContext = new JSONObject();
        try {
            deviceContext.put("deviceOS", "ANDROID");
            data.put("deviceContext", deviceContext);

        } catch (JSONException e) {
            e.printStackTrace();
        }


//        val base64Body = android.util.Base64(Gson().toJson(data))
        //  val payloadBase64 = android.util.Base64.encodeToString(
        //        data.toString().toByteArray(Charset.defaultCharset()), android.util.Base64.NO_WRAP
        //  );

        String payloadBase64 = android.util.Base64.encodeToString(data.toString().getBytes(Charset.defaultCharset()), android.util.Base64.NO_WRAP);

/*

        String checksum = Arrays.toString(sha256(payloadBase64 + apiEndPoint + salt)) + "###1";

        Log.d("PAPAYACODERS", "onCreate: $payloadBase64");
        Log.d("PAPAYACODERS", "onCreate: $checksum");


        String b2BPGRequest = String.valueOf(new B2BPGRequestBuilder()
                .setData(payloadBase64)
                .setChecksum(checksum)
                .setUrl(apiEndPoint)
                .build());


        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PAPAYACODERS", "onCreate: $payloadBase64");
                Log.d("PAPAYACODERS", "onCreate: $checksum");



            }
        });

*/

    }

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == B2B_PG_REQUEST_CODE) {

      *//*This callback indicates only about completion of UI flow.
            Inform your server to make the transaction
            status call to get the status. Update your app with the
            success/failure status.*//*

        }
    }



    private fun sha256(input:String String s): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }*/
}
