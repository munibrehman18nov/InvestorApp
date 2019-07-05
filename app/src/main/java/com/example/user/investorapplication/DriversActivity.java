package com.example.user.investorapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DriversActivity extends AppCompatActivity
{
    @BindView(R.id.driversListVU) ListView driversListVU;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers);
        ButterKnife.bind(this);


        CustomDriverAdapter myAdapter = null;
        try {
            myAdapter = new CustomDriverAdapter(getApplicationContext(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        driversListVU.setAdapter(myAdapter);
    }


    @OnClick(R.id.btn_addDriver)
    public void addDriverBtnClicked(View view)
    {
        Intent intent = new Intent(DriversActivity.this, AddDriverActivity.class);
        startActivity(intent);
        finish();
    }

    /*@OnClick(R.id.btn_deleteDriver)
    public void deleteDriverBtnClicked(View view)
    {
        Intent intent = new Intent(DriversActivity.this, DeleteDriverActivity.class);
        startActivity(intent);
        finish();
    }*/


}
