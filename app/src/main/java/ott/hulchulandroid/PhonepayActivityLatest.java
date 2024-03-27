package ott.hulchulandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.phonepe.intent.sdk.api.PhonePe;
import com.phonepe.intent.sdk.api.PhonePeInitException;
import com.phonepe.intent.sdk.api.UPIApplicationInfo;
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment;

import java.util.List;

public class PhonepayActivityLatest extends AppCompatActivity {
    Context context;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonepay_latest);
        context = this;
        button = findViewById(R.id.button1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //  String string_signature = PhonePe.getPackageSignature();

               // PhonePe.init(context, PhonePeEnvironment.UAT, "M22TTNZLGZFA2", "");
                //PhonePe.init(context, PhonePeEnvironment.UAT, "PGTESTPAYUAT", "");

                try {
                    PhonePe.setFlowId("2323PH");// Recommended, not mandatory , An alphanumeric string without any special character
                    List<UPIApplicationInfo> upiApps = PhonePe.getUpiApps();
                } catch (PhonePeInitException exception) {
                    exception.printStackTrace();
                }
            }
        });

    }



}


