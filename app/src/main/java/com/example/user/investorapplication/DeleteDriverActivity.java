package com.example.user.investorapplication;

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
import butterknife.ButterKnife;
import butterknife.OnClick;
import stanford.androidlib.SimpleActivity;

public class DeleteDriverActivity extends SimpleActivity
{
    private FirebaseDatabase db;
    private DatabaseReference users;
    private DatabaseReference drivers;
    private SharedPreferences mPrefs;
    private ArrayList<Driver> dList;

    private String id;
    private TextView et_dId;
    private String investorId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_driver);
        ButterKnife.bind(this);
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        drivers = users.child("Drivers");
        et_dId = (TextView) findViewById(R.id.et_dId);
        mPrefs = getApplicationContext().getSharedPreferences("mPrefs", 0); // 0 - for private mode
        investorId = mPrefs.getString("INVESTOR_ID", "").trim();
        try {
            setDriversList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @OnClick(R.id.btn_deleteDone)
    public void deleteDoneClicked(View view)
    {
        id = et_dId.getText().toString().trim();
        if(id.isEmpty())
        {
            toast("Enter Driver ID");

        }
        else {
            boolean chk = false;
            for(int i=0; i<dList.size(); i++)
            {
                if(dList.get(i).getId().equals(id))
                {
                    chk = true;
                }
            }
            if(chk==true) {
                int x = Integer.parseInt(id);
                drivers.child((x % 5000) + "").child("mgr").setValue("1000");
                toast("Driver Deleted");
                //deleteDriver();
            }else {
                toast("Driver is not under your hierarchy");
            }
        }
    }

    private void setDriversList() throws JSONException
    {
        dList = new ArrayList<>();
        Gson gson = new Gson();
        String json = mPrefs.getString("dList", "");
        JSONArray JArr = new JSONArray(json);
        for(int i=0; i<JArr.length(); i++)
        {
            JSONObject JObj = (JSONObject) JArr.get(i);
            String id = JObj.getString("id").trim();
            String name = JObj.getString("name").trim();
            String mgr = JObj.getString("mgr").trim();
            String cnic = JObj.getString("cnic").trim();
            String mobile = JObj.getString("mobile").trim();
            String address = JObj.getString("address").trim();
            String email = JObj.getString("email").trim();
            Driver d = new Driver(id,name,mgr,cnic,mobile,address,email,"0");
            dList.add(d);
        }
    }



}
