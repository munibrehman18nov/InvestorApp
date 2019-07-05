package com.example.user.investorapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stanford.androidlib.SimpleActivity;

public class DProfileActivity extends SimpleActivity
{
    @BindView(R.id.name_tVu)
    TextView name_tVu;
    @BindView(R.id.mobile_tVu)
    TextView mobile_tVu;
    @BindView(R.id.email_tVu)
    TextView email_tVu;
    @BindView(R.id.profile_imgVu)
    ImageView profile_imgVu;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor prefsEditor;


    private BarChart barChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dprofile);
        ButterKnife.bind(this);


        setSharedPreferences();
        String id = mPrefs.getString("DriverId","");
        String name = mPrefs.getString("DriverName","");
        String mobile = mPrefs.getString("DriverMobile","");
        String email = mPrefs.getString("DriverEmail","");

        name_tVu.setText(name);
        mobile_tVu.setText(mobile);
        email_tVu.setText(email);
        setProfileImage();


        barChart = findViewById(R.id.barChart);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0f,0));
        barEntries.add(new BarEntry(10f,1));
        barEntries.add(new BarEntry(20f,2));
        barEntries.add(new BarEntry(30f,3));
        barEntries.add(new BarEntry(40f,4));
        barEntries.add(new BarEntry(50f,5));
        barEntries.add(new BarEntry(60f,6));
        barEntries.add(new BarEntry(70f,7));
        barEntries.add(new BarEntry(80f,8));
        barEntries.add(new BarEntry(90f,9));
        barEntries.add(new BarEntry(100f,10));
        barEntries.add(new BarEntry(110f,11));
        BarDataSet barDataSet = new BarDataSet(barEntries, "Dates");

        ArrayList<String> theDates = new ArrayList<>();
        theDates.add("Jan");
        theDates.add("Feb");
        theDates.add("Mar");
        theDates.add("April");
        theDates.add("May");
        theDates.add("Jun");
        theDates.add("Jul");
        theDates.add("Aug");
        theDates.add("Sep");
        theDates.add("Oct");
        theDates.add("Nov");
        theDates.add("Dec");


        BarData theData = new BarData(theDates, barDataSet);
        barChart.setData(theData);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);


    }

    @OnClick(R.id.mobile_tVu)
    public void makeACall()
    {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mobile_tVu.getText().toString(), null)));
    }

    @OnClick(R.id.email_tVu)
    public void sendEmail()
    {
        Intent intent = new Intent(DProfileActivity.this, SendEmail.class);
        String email = email_tVu.getText().toString().trim();
        intent.putExtra("EMAIL", email);
        startActivity(intent);
    }


    private void setSharedPreferences()
    {
        mPrefs = getSharedPreferences("mPrefs", 0); // 0 - for private mode
        prefsEditor = mPrefs.edit();
    }

    private void setProfileImage()
    {
        setSharedPreferences();
        String id = mPrefs.getString("DriverId","");
        StorageReference filePath = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = filePath.child(id.trim());
        GlideApp.with(this)
                .load(fileRef)
                .error(R.drawable.noprofile)
                .into(profile_imgVu);
    }

}
