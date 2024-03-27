package ott.hulchulandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

import ott.hulchulandroid.utils.RtlUtils;

public class GrievanceSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grievance_support);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        if (!isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.black_window_light));
        }

     /*   //hide back button toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);

        }
*/


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Grievance Support");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "GrievanceSupportActivity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}