package com.example.user.investorapplication;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Model.Vehicle;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stanford.androidlib.SimpleActivity;

public class DeleteVehicleActivity extends SimpleActivity
{
    private FirebaseDatabase db;
    private DatabaseReference users;
    private DatabaseReference vehicles;
    private SharedPreferences mPrefs;
    private ArrayList<Vehicle> vList;

    private String vin;
    private TextView et_vin;
    private String investorId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_vehicle);

        ButterKnife.bind(this);
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        vehicles = users.child("Vehicles");
        et_vin = (TextView) findViewById(R.id.et_vinInDelete);
        mPrefs = getApplicationContext().getSharedPreferences("mPrefs", 0); // 0 - for private mode
        investorId = mPrefs.getString("INVESTOR_ID", "").trim();
        try {
            setVehiclesList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            String vin = JObj.getString("vin").toString().trim();

            //System.out.println("DeleteVehicleActivity::setVehiclesList::JObj :"+JObj);
            Vehicle v = new Vehicle(id,owner,model,name,vin);
            vList.add(v);
        }
    }



    @OnClick(R.id.btn_deleteDone)
    public void deleteDoneClicked(View view)
    {
        vin = et_vin.getText().toString().trim();
        String vehicleId = "";
        if(vin.isEmpty())
        {
            toast("Enter 17-Digit Vehicle Identification Number");

        }
        else {
            boolean chk = false;
            for(int i=0; i<vList.size(); i++)
            {
                if(vList.get(i).getVIN().equals(vin))
                {
                    chk = true;
                    vehicleId = vList.get(i).getId();
                }
            }
            if(chk==true) {
                int  x = Integer.parseInt(vehicleId);
                vehicles.child((x % 10000) + "").child("owner").setValue("0");
                toast("Vehicle Deleted");
                //deleteDriver();
            }else {
                toast("Vehicle is not under your hierarchy");
            }
        }
    }




}
