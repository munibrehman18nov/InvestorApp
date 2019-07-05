package com.example.user.investorapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stanford.androidlib.SimpleActivity;

public class VehiclesActivity extends SimpleActivity
{
    @BindView(R.id.vehiclesListVU) ListView vehiclesListVU;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles);
        ButterKnife.bind(this);

        CustomVehicleAdapter myAdapter = null;
        try {
            myAdapter = new CustomVehicleAdapter(getApplicationContext(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        vehiclesListVU.setAdapter(myAdapter);
    }

    @OnClick(R.id.btn_addVehicle)
    public void addVehicleBtnClicked(View view)
    {
        Intent intent = new Intent(VehiclesActivity.this, AddVehicleActivity.class);
        startActivity(intent);
        finish();
    }
    /*@OnClick(R.id.btn_deleteVehicle)
    public void deleteVehicleBtnClicked(View view)
    {
        Intent intent = new Intent(VehiclesActivity.this, DeleteVehicleActivity.class);
        startActivity(intent);
        finish();
    }*/
}
