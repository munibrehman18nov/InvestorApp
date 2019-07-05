package com.example.user.investorapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.regex.Pattern;

import Model.Driver;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stanford.androidlib.SimpleActivity;

public class AddDriverActivity extends SimpleActivity
{
    private long totalDrivers = 0;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private DatabaseReference drivers;
    private FirebaseAuth firebaseAuth;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor prefsEditor;
    private String investorId;

    private Gson gson;
    private ArrayList<Driver> dList;
    private int investorsTotalDrivers;
    private String name;
    private String address;
    private String cnic;
    private String mobile;
    private String email;
    private TextView et_name;
    private TextView et_address;
    private TextView et_cnic;
    private TextView et_mobile;
    private TextView et_email;

    private boolean isChecked;
    private boolean isUnique;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_driver);
        ButterKnife.bind(this);

        //db = FirebaseDatabase.getInstance();
        //users = db.getReference("Users");
        //drivers = users.child("Drivers");
        //investorsTotalDrivers=0;
        setSharedPreferences();
        gson = new Gson();
        try {
            setDriversList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setDriversList() throws JSONException
    {
        Gson gson = new Gson();
        dList = new ArrayList<>();
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

    private void setSharedPreferences()
    {
        mPrefs = getApplicationContext().getSharedPreferences("mPrefs", 0); // 0 - for private mode
        prefsEditor = mPrefs.edit();
    }
    private void getDataFromSharedPreferences()
    {
        investorId = mPrefs.getString("INVESTOR_ID", "").trim();
        totalDrivers = Integer.parseInt(mPrefs.getString("totalDrivers", "").toString().trim());
    }

    @OnClick(R.id.btn_addDone)
    public void addDoneClicked(View view)
    {
        isChecked = false;
        isUnique = false;

        //while(isUnique == false)
        //{
          //  while (isChecked == false)
            //{
                setAllTextViews();
                setAllStrings();
                if (checkEmptyTextViews())
                {
                    isChecked = true;
                    setSharedPreferences();
                    getDataFromSharedPreferences();
                    db = FirebaseDatabase.getInstance();
                    users = db.getReference("Users");
                    drivers = users.child("Drivers");

                    firebaseAuth = FirebaseAuth.getInstance();

                    final String id = drivers.push().getKey();
                    int online = 0;
                    final Driver d = new Driver(id, name, investorId, cnic, mobile, address, email, online + "");
                    firebaseAuth.createUserWithEmailAndPassword(d.getEmail().trim(), cnic.trim())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    //adding-Driver
                                                    drivers.child((id)).setValue(d);
                                                    //toast("Driver added");
                                                    toast("Registered successfully, Driver should check email for verification");
                                                    isUnique = true;
                                                    dList.add(d);
                                                    String json = gson.toJson(dList);
                                                    prefsEditor.putString("dList", json);
                                                    prefsEditor.commit();
                                                    updateDList();
                                                    finish();
                                                    Intent intent = new Intent(getApplicationContext(), DriversActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                }
                                            }
                                        });

                                    }
                                    else {
                                        et_email.setError("This email is already registered");
                                        //toast("This email is already registered");
                                    }
                                }
                            });
                //}
            //}
        }
    }
    private void setDList(DataSnapshot dataSnapshot)
    {
        investorsTotalDrivers = 0;
        DataSnapshot driversSS = dataSnapshot.child("Drivers");
        Iterable<DataSnapshot> driversChildren = driversSS.getChildren();
        for(DataSnapshot temp : driversChildren)
        {
            if(temp!=null)
            {
                if (temp.child("mgr").getValue().toString().trim().equals(investorId)) {
                    Driver d = new Driver(temp.child("id").getValue().toString().trim(), temp.child("name").getValue().toString().trim(), temp.child("mgr").getValue().toString().trim(), "", "", "", "","");
                    dList.add(d);
                    investorsTotalDrivers++;
                }
            }
        }
        //System.out.println("setInvestorsDriversList::DLIST: "+dList);
        String json = gson.toJson(dList);
        prefsEditor.putString("dList", json);
        prefsEditor.putString("investorsTotalDrivers", investorsTotalDrivers+"");
        prefsEditor.commit();
    }
    private void updateDList()
    {
        dList = new ArrayList<>();
        gson = new Gson();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        drivers = users.child("Drivers");
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                setDList(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setAllTextViews()
    {
        et_name = findViewById(R.id.et_name);
        et_address = findViewById(R.id.et_address);
        et_cnic = findViewById(R.id.et_cnic);
        et_mobile = findViewById(R.id.et_contactNo);
        et_email = findViewById(R.id.et_email);
    }
    private void setAllStrings()
    {
        name = et_name.getText().toString();
        address = et_address.getText().toString();
        cnic = et_cnic.getText().toString();
        mobile = et_mobile.getText().toString();
        email = et_email.getText().toString();
    }
    private boolean checkEmptyTextViews()
    {
        if(name.isEmpty())
        {
            //toast("Enter Driver Name");
            et_name.setError("Cannot be empty");
            return false;
        }
        /*if(address.isEmpty())
        {
            toast("Enter Driver Address");
        }*/
        if(cnic.isEmpty())
        {
            et_cnic.setError("Cannot be empty");
            //toast("Enter Driver CNIC");
            return false;
        }
        //if(Patterns.PHONE.matcher(mobile).matches())
        if(mobile.isEmpty())
        {
            et_mobile.setError("Enter Valid Mobile Number");
            return false;
        }
        //if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        if(!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            et_email.setError("Enter Valid Email Address");
            return false;
        }
        return true;
    }


}
