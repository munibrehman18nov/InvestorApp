package com.example.user.investorapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import Model.Driver;
import Model.ImageData;
import butterknife.OnClick;
import stanford.androidlib.SimpleActivity;

public class CustomDriverAdapter extends BaseAdapter
{
    private Activity act;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private DatabaseReference drivers;
    private Gson gson;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor prefsEditor;
    private String investorId;
    //private long totalDrivers = 0;

    private Context context;
    private LinearLayout deleteDriver;

    private ArrayList<Driver>dList;
    private LinearLayout lo_dProfile;

    public CustomDriverAdapter(Context context, Activity act) throws JSONException
    {
        this.act = act;
        dList = new ArrayList<>();
        setSharedPreferences(context);
        getDataFromSharedPreferences();

        setDriversList();
        this.context = context;
        gson = new Gson();
        //ButterKnife.bind(DriversActivity.this);
    }

    private void setSharedPreferences(Context context)
    {
        mPrefs = context.getSharedPreferences("mPrefs", 0); // 0 - for private mode
        prefsEditor = mPrefs.edit();
    }

    private void getDataFromSharedPreferences()
    {
        investorId = mPrefs.getString("INVESTOR_ID", "").trim();
        //totalDrivers = Integer.parseInt(mPrefs.getString("totalDrivers", "").toString().trim());
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

    @Override
    public int getCount()
    {
        return dList.size();
    }

    @Override
    public Object getItem(int i) {
        return dList.get(i).getId().toString();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, final View view, ViewGroup viewGroup)
    {
        View v = View.inflate(context, R.layout.layout_driver_custom, null);
        deleteDriver = v.findViewById(R.id.btn_deleteDriver);
        lo_dProfile = v.findViewById(R.id.lo_dProfile);
        TextView dr_nameVu = v.findViewById(R.id.dr_nameVu);
        dr_nameVu.setText(dList.get(i).getName().toString());
        ImageView dImgVU = v.findViewById(R.id.dImgVU);

        StorageReference filePath = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = filePath.child(dList.get(i).getId());
        GlideApp.with(context)
                .load(fileRef)
                .error(R.drawable.noprofile)
                .into(dImgVU);

        deleteDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ImageView dImgVu = v.findViewById(R.id.delete_imgVu);
                dImgVu.setColorFilter(v.getResources().getColor(R.color.red));
                deleteDriverBtnClicked(i);
                updateDList();

                Intent intent = new Intent(context, DriversActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                act.finish();

            }
        });

        lo_dProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                lo_dProfile_Clicked(i);
            }
        });

        return v;
    }

    private void lo_dProfile_Clicked(final int index)
    {
        setSharedPreferences(context);
        prefsEditor.putString("DriverId", dList.get(index).getId());
        prefsEditor.putString("DriverName", dList.get(index).getName());
        prefsEditor.putString("DriverMobile", dList.get(index).getMobile());
        prefsEditor.putString("DriverEmail", dList.get(index).getEmail());
        prefsEditor.commit();

        Intent intent = new Intent(context, DProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        //act.finish();
    }

    private void deleteDriverBtnClicked(final int index)
    {
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        drivers = users.child("Drivers");
        drivers.child(dList.get(index).getId()).child("mgr").setValue("1000");
        ArrayList<Driver> dListNew = new ArrayList<>();
        for(int i=0; i<dList.size(); i++)
        {
            if(dList.get(i) != dList.get(index))
            {
                dListNew.add(dList.get(i));

            }
        }
        String json = gson.toJson(dListNew);
        prefsEditor.putString("dList", json);
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
    private void setDList(DataSnapshot dataSnapshot)
    {
        getDataFromSharedPreferences();
        int investorsTotalDrivers = 0;
        DataSnapshot driversSS = dataSnapshot.child("Drivers");
        Iterable<DataSnapshot> driversChildren = driversSS.getChildren();
        for(DataSnapshot temp : driversChildren)
        {
            if(temp!=null)
            {
                if (temp.child("mgr").getValue().toString().trim().equals(investorId)) {
                    Driver d = new Driver(temp.child("id").getValue().toString().trim(), temp.child("name").getValue().toString().trim(), temp.child("mgr").getValue().toString().trim(), temp.child("cnic").getValue().toString().trim(), temp.child("mobile").getValue().toString().trim(), temp.child("address").getValue().toString().trim(), temp.child("email").getValue().toString().trim(),temp.child("online").getValue().toString().trim());
                    dList.add(d);
                    investorsTotalDrivers++;
                }
            }
        }
        //System.out.println("setInvestorsDriversList::DLIST: "+dList);
        String json = gson.toJson(dList);
        prefsEditor.putString("dList", json);
        //System.out.println("CustomDriverAdapter::setDList::JSON: "+json);
        prefsEditor.putString("investorsTotalDrivers", investorsTotalDrivers+"");
        prefsEditor.commit();
    }
}
