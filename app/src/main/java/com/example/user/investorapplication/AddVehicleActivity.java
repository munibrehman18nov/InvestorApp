package com.example.user.investorapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Model.Driver;
import Model.Vehicle;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stanford.androidlib.SimpleActivity;

public class AddVehicleActivity extends SimpleActivity
{
    private long totalVehicles = 0;
    long id;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private DatabaseReference vehicles;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor prefsEditor;


    private Gson gson;
    private ArrayList<Vehicle> vList;
    private int investorsTotalVehicles;

    private String investorId;
    private String name;
    private String model;
    private String vin;
    private TextView et_vName;
    private TextView et_vModel;
    private TextView et_VIN;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        gson = new Gson();
        setSharedPreferences();
        ButterKnife.bind(this);
        try {
            setVehiclesList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @OnClick(R.id.btn_addDone)
    public void addDoneClicked(View view)
    {
        setAllTextViews();
        setAllStrings();
        checkEmptyTextView();

        setSharedPreferences();
        getDataFromSharedPreferences();
        saveDataToDB();
        finish();
        Intent intent = new Intent(this, VehiclesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void saveDataToDB()
    {
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        vehicles = users.child("Vehicles");
        String id = vehicles.push().getKey();
        Vehicle v = new Vehicle(id,investorId,model,name,vin);
        vehicles.child((id)).setValue(v);
        toast("Vehicle added");
        vList.add(v);
        String json = gson.toJson(vList);
        prefsEditor.putString("vList", json);
        prefsEditor.commit();
        updateVList();
    }

    private void setVehiclesList() throws JSONException
    {
        vList = new ArrayList<>();
        Gson gson = new Gson();
        String json = mPrefs.getString("vList", "");
        JSONArray JArr = new JSONArray(json);
        for(int i=0; i<JArr.length(); i++)
        {
            JSONObject JObj = (JSONObject) JArr.get(i);
            String id = JObj.getString("Id").toString().trim();
            String name = JObj.getString("name").toString().trim();
            String owner = JObj.getString("owner").toString().trim();
            String model = JObj.getString("Model").toString().trim();
            //String vin = JObj.getString("vin").toString().trim();
            String vin = "";
            //System.out.println("CustomVehicleAdapter::setVehiclesList::JObj :"+JObj);
            Vehicle v = new Vehicle(id,owner,model,name,vin);
            vList.add(v);
        }
    }

    private void setVList(DataSnapshot dataSnapshot)
    {
        investorsTotalVehicles=0;
        DataSnapshot vehiclesSS = dataSnapshot.child("Vehicles");
        Iterable<DataSnapshot> vehiclesChildren = vehiclesSS.getChildren();
        for(DataSnapshot temp : vehiclesChildren)
        {
            if(temp.child("owner").getValue().toString().trim().equals(investorId))
            {
                Vehicle v = new Vehicle(temp.child("id").getValue().toString().trim(), temp.child("owner").getValue().toString().trim(),temp.child("model").getValue().toString().trim(),temp.child("name").getValue().toString().trim(),temp.child("vin").getValue().toString().trim());
                vList.add(v);
                investorsTotalVehicles++;
            }
        }
        //System.out.println("setInvestorsDriversList::DLIST: "+dList);
        String json = gson.toJson(vList);
        prefsEditor.putString("vList", json);
        prefsEditor.putString("investorsTotalVehicles", investorsTotalVehicles+"");
        prefsEditor.commit();
    }

    private void updateVList()
    {
        gson = new Gson();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        vehicles = users.child("Vehicles");
        vList = new ArrayList<>();
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                setVList(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setSharedPreferences()
    {
        mPrefs = getApplicationContext().getSharedPreferences("mPrefs", 0); // 0 - for private mode
        prefsEditor = mPrefs.edit();
    }
    private void getDataFromSharedPreferences()
    {
        investorId = mPrefs.getString("INVESTOR_ID", "").trim();
        totalVehicles = Integer.parseInt(mPrefs.getString("totalVehicles", "").toString().trim());
    }

    private void checkEmptyTextView()
    {
        if(name.isEmpty())
        {
            toast("Enter Vehicle Name");
        }
        if(model.isEmpty())
        {
            toast("Enter Vehicle Model");
        }
        if(vin.isEmpty())
        {
            toast("Enter 17-digit Vehicle Identification Number");
        }
    }

    private void setAllTextViews()
    {
        et_vName = findViewById(R.id.et_vName);
        et_vModel = findViewById(R.id.et_vModel);
        et_VIN = findViewById(R.id.et_VIN);
    }

    private void setAllStrings()
    {
        name = et_vName.getText().toString();
        model = et_vModel.getText().toString();
        vin = et_VIN.getText().toString();
    }
}
